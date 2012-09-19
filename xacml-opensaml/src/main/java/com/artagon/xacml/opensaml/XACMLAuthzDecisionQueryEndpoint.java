package com.artagon.xacml.opensaml;

import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.AuthzService;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.MetadataCriteria;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.ctx.ResponseType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.parse.BasicParserPool;
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

import com.artagon.xacml.v30.Attribute;
import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.Attributes;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.SubjectAttributes;
import com.artagon.xacml.v30.marshall.jaxb.Xacml20RequestContextUnmarshaller;
import com.artagon.xacml.v30.marshall.jaxb.Xacml20ResponseContextMarshaller;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.types.StringType;

public class XACMLAuthzDecisionQueryEndpoint implements OpenSamlEndpoint
{

	private final static Logger log = LoggerFactory.getLogger(XACMLAuthzDecisionQueryEndpoint.class);

	private IDPConfiguration idpConfig;

	private PolicyDecisionPoint pdp;
	private Xacml20RequestContextUnmarshaller xacmlRequest20Unmarshaller;
	private Xacml20ResponseContextMarshaller xacmlResponse20Unmarshaller;

	private DocumentBuilderFactory dbf;
	private BasicParserPool parserPool;

	private boolean requireSignatureValidation;

	public XACMLAuthzDecisionQueryEndpoint(
			IDPConfiguration idpConfig,
			PolicyDecisionPoint pdp){
		this.idpConfig = idpConfig;
		this.pdp = pdp;

		dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		xacmlRequest20Unmarshaller = new Xacml20RequestContextUnmarshaller();
		xacmlResponse20Unmarshaller = new Xacml20ResponseContextMarshaller();
		parserPool = new BasicParserPool();
		parserPool.setNamespaceAware(true);
		requireSignatureValidation = true;
	}

	public void setRequireSignatureValidation(boolean flag) {
		requireSignatureValidation = flag;
	}

	public Response handle(RequestAbstractType request)
	{
		if(log.isDebugEnabled()){
			QName n = request.getElementQName();
			log.debug("Processing SAML request type=\"{}:{}\"",
					n.getNamespaceURI(), n.getLocalPart());
		}
		if(!(request instanceof XACMLAuthzDecisionQueryType)){
			return makeErrorResponse(request, "Invalid request");
		}
		XACMLAuthzDecisionQueryType xacml20DecisionQuery = (XACMLAuthzDecisionQueryType)request;
		RequestType xacmlRequest = xacml20DecisionQuery.getRequest();
		if(xacmlRequest == null){
			if(log.isDebugEnabled()){
				log.debug("No XACML request found in the given request");
			}
			return makeErrorResponse(request, "Invalid request");
		}
		try
		{
			if (requireSignatureValidation) {
				if(!validateRequestSignature(request)){
					if(log.isDebugEnabled()){
						log.debug("Failed to validate signature");
					}
					return makeErrorResponse(request, "Failed to validate signature");
				}
			} else {
				log.info("Signature validation has been disabled");
			}
			if(!validateRequest(request)){
				if(log.isDebugEnabled()){
					log.debug("Failed to validate request");
				}
				return makeErrorResponse(request, "Failed to validate request");
			}
			Document reqDom = dbf.newDocumentBuilder().newDocument();
			OpenSamlObjectBuilder.marshallXacml20Request(xacmlRequest, reqDom);
			Document resDom = performXacmlRequest(xacml20DecisionQuery.getIssuer().getValue(), reqDom);
			ResponseType xacmlResponse = OpenSamlObjectBuilder.unmarshallXacml20Response(resDom.getDocumentElement());
			Assertion assertion = OpenSamlObjectBuilder.makeXacml20AuthzDecisionAssertion(
					idpConfig.getLocalEntity().getEntityID(),
					xacml20DecisionQuery.isReturnContext()?xacmlRequest:null, xacmlResponse);
			Response samlResponse = OpenSamlObjectBuilder.makeXacml20AuthzDecisionQueryResponse(
					idpConfig.getLocalEntity().getEntityID(), xacml20DecisionQuery, assertion);
			signResponse(samlResponse);
			return samlResponse;
		}catch(Exception e){
			log.error(e.getMessage(), e);
			return makeErrorResponse(request, "Internal error");
		}
	}

	private Response makeErrorResponse(RequestAbstractType request, String errorMessage)
	{
		Response response = OpenSamlObjectBuilder.makeResponse(request,
				OpenSamlObjectBuilder.makeStatus(StatusCode.REQUESTER_URI, errorMessage));
		response.setIssuer(OpenSamlObjectBuilder.makeIssuer(idpConfig.getLocalEntity().getEntityID()));
		return response;
	}

	private boolean validateRequestSignature(RequestAbstractType request)
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
			if(log.isDebugEnabled()) {
				log.debug("Failed to get authorization service by destination location");
			}
			return false;
		}
		return true;
	}

	public Document performXacmlRequest(String issuer, Document reqDom) throws Exception
	{
		try
		{
			RequestContext xacmlReq = xacmlRequest20Unmarshaller.unmarshal(reqDom);
			xacmlReq = addIssuerToRequest(issuer, xacmlReq);
			if(log.isDebugEnabled()){
				log.debug("XACML request=\"{}\"", xacmlReq);
			}
			ResponseContext xacmlRes = pdp.decide(xacmlReq);
			Document resDom = dbf.newDocumentBuilder().newDocument();
			xacmlResponse20Unmarshaller.marshal(xacmlRes, new DOMResult(resDom));
			return resDom;
		}catch(Exception e){
			if(log.isDebugEnabled()){
				log.debug(e.getMessage(), e);
			}
			throw e;
		}

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

	private RequestContext addIssuerToRequest(String issuer, RequestContext req)
	{
		Attributes indermediarySubject = Attributes.builder()
				.category(AttributeCategories.SUBJECT_INTERMEDIARY)
				.attributes(Attribute
						.builder(SubjectAttributes.SUBJECT_ID.toString())
						.value(StringType.STRING, issuer).build())
				.build();
		Collection<Attributes> filtered = new LinkedList<Attributes>();
		filtered.add(indermediarySubject);
		for(Attributes a : req.getAttributes()){
			if(!a.getCategory().equals(
					AttributeCategories.SUBJECT_INTERMEDIARY)){
				filtered.add(a);
			}
		}
		return new RequestContext(req.isReturnPolicyIdList(), req.isCombinedDecision(), filtered);
	}
}
