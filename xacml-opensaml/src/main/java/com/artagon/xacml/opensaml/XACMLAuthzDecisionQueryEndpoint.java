package com.artagon.xacml.opensaml;

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.AuthzService;
import org.opensaml.saml2.metadata.PDPDescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.MetadataCriteria;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.marshall.jaxb.Xacml20RequestContextUnmarshaller;
import com.artagon.xacml.v30.marshall.jaxb.Xacml20ResponseContextMarshaller;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;

public class XACMLAuthzDecisionQueryEndpoint 
{
	
	private final static Logger log = LoggerFactory.getLogger(XACMLAuthzDecisionQueryEndpoint.class);
	
	private IDPConfiguration idpConfig;
	
	private PolicyDecisionPoint pdp;
	private Xacml20RequestContextUnmarshaller xacmlRequest20Unmarshaller;
	private Xacml20ResponseContextMarshaller xacmlResponse20Unmarshaller;
	
	private DocumentBuilderFactory dbf;
	
	public XACMLAuthzDecisionQueryEndpoint(
			IDPConfiguration idpConfig, 
			PolicyDecisionPoint pdp){
		this.idpConfig = idpConfig;
		this.pdp = pdp;
		this.dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		this.xacmlRequest20Unmarshaller = new Xacml20RequestContextUnmarshaller();
		this.xacmlResponse20Unmarshaller = new Xacml20ResponseContextMarshaller();
	}
	
	public Response handle(RequestAbstractType request)
	{
		if(log.isDebugEnabled()){
			QName n = request.getElementQName();
			log.debug("Processing SAML request type=\"{}:{}\"", 
					n.getNamespaceURI(), n.getLocalPart());
		}
		if(!(request instanceof XACMLAuthzDecisionQueryType)){	
			return makeErrorResponse(request);
		}
		XACMLAuthzDecisionQueryType xacml20DecisionQuery = (XACMLAuthzDecisionQueryType)request;
		RequestType xacmlRequest = xacml20DecisionQuery.getRequest();
		if(xacmlRequest == null){
			if(log.isDebugEnabled()){
				log.debug("No XACML request found in the given request");
			}
			return makeErrorResponse(request);
		}
		try
		{
			if(!validateRequestSignature(request)){
				if(log.isDebugEnabled()){
					log.debug("Failed to validate signature");
				}
				return makeErrorResponse(request);
			}
			if(!validateRequest(request)){
				if(log.isDebugEnabled()){
					log.debug("Failed to validate request");
				}
				return makeErrorResponse(request);
			}
			Document reqDom = dbf.newDocumentBuilder().newDocument();
			OpenSamlObjectBuilder.marshallXacml20Request(xacmlRequest, reqDom);
			Document resDom = performXacmlRequest(reqDom);
			ResponseType xacmlResponse = OpenSamlObjectBuilder.unmarshallXacml20Response(resDom.getDocumentElement());
			Assertion assertion = OpenSamlObjectBuilder.makeXacml20AuthzDecisionAssertion(
					idpConfig.getLocalEntity().getEntityID(), 
					xacml20DecisionQuery.isReturnContext()?xacmlRequest:null, xacmlResponse);
			Response samlResponse = OpenSamlObjectBuilder.makeXacml20AuthzDecisionQueryResponse(
					idpConfig.getLocalEntity().getEntityID(), xacml20DecisionQuery, assertion);
			signResponse(samlResponse);
			return samlResponse;
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			return makeErrorResponse(request);
		}
	}
	
	private Response makeErrorResponse(RequestAbstractType request)
	{
		Response response = OpenSamlObjectBuilder.makeResponse(request, 
				OpenSamlObjectBuilder.makeStatus(StatusCode.REQUESTER_URI));
		response.setIssuer(OpenSamlObjectBuilder.makeIssuer(idpConfig.getLocalEntity().getEntityID()));
		return response;
	}
	
	public boolean validateRequestSignature(RequestAbstractType request) 
		throws ValidationException, SecurityException
	{
		SAMLSignatureProfileValidator validator = new SAMLSignatureProfileValidator();
		if(request.getSignature() == null){
			log.debug("Request is not signed");
			return false;
		}
		validator.validate(request.getSignature());
		if(request.getIssuer() == null 
				|| request.getIssuer().getValue() == null){
			if(log.isDebugEnabled()){
				log.debug("Request does not have issuer");
			}
			return false;
		}
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add( new EntityIDCriteria(request.getIssuer().getValue()));
		criteriaSet.add( new MetadataCriteria(SPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS));
		criteriaSet.add( new UsageCriteria(UsageType.SIGNING));
	    boolean dsigTrusted = idpConfig.getSignatureTrustEngine().validate(request.getSignature(), criteriaSet);
	    if(log.isDebugEnabled()){
	    	log.debug("Is SAML request XML dsig trusted=\"{}\"", dsigTrusted);
	    }
	    return dsigTrusted;
	}
	
	private boolean validateRequest(RequestAbstractType request)
	{ 
		AuthzService authzService = idpConfig.getAuthzServiceByLocation(request.getDestination());
		if(authzService == null){
			return false;
		}
		return true;
	}
		
	public Document performXacmlRequest(Document reqDom) throws Exception
	{
		RequestContext xacmlReq = xacmlRequest20Unmarshaller.unmarshal(reqDom);
		ResponseContext xacmlRes = pdp.decide(xacmlReq);
		Document resDom = dbf.newDocumentBuilder().newDocument();
		xacmlResponse20Unmarshaller.marshal(xacmlRes, new DOMResult(resDom));
		return resDom;
	}
	
	private void signResponse(Response response) throws Exception
	{
				
		Signature dsig = (Signature) Configuration.getBuilderFactory()
        .getBuilder(Signature.DEFAULT_ELEMENT_NAME)
        .buildObject(Signature.DEFAULT_ELEMENT_NAME);

		dsig.setSigningCredential(idpConfig.getSigningCredential());
		dsig.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
		dsig.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
		response.setSignature(dsig);
		SecurityHelper.prepareSignatureParams(dsig, idpConfig.getSigningCredential(), null, null);
		Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
		Signer.signObject(dsig);
	}
}
