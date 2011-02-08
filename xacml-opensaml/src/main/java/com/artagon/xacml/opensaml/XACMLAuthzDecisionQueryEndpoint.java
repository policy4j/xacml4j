package com.artagon.xacml.opensaml;

import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.RequestAbstractType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.MetadataCriteria;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xacml.ctx.RequestType;
import org.opensaml.xacml.profile.saml.XACMLAuthzDecisionQueryType;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.signature.SignatureTrustEngine;
import org.opensaml.xml.validation.ValidationException;

import com.artagon.xacml.v30.marshall.jaxb.Xacml20RequestContextUnmarshaller;
import com.artagon.xacml.v30.marshall.jaxb.Xacml20ResponseContextMarshaller;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;

public class XACMLAuthzDecisionQueryEndpoint 
{

	private IDPConfiguration idpConfig;
	
	private PolicyDecisionPoint pdp;
	private Xacml20RequestContextUnmarshaller xacmlRequest20Unmarshaller;
	private Xacml20ResponseContextMarshaller xacmlResponse20Unmarshaller;
	private SignatureTrustEngine trustEngine;
	
	public XACMLAuthzDecisionQueryEndpoint(
			IDPConfiguration idpConfig, 
			PolicyDecisionPoint pdp){
		this.idpConfig = idpConfig;
		this.pdp = pdp;
	}
	
	public Response handle(RequestAbstractType request)
	{
		if(!(request instanceof XACMLAuthzDecisionQueryType)){
			return makeErrorResponse(request);
		}
		XACMLAuthzDecisionQueryType xacml20DecisionQuery = (XACMLAuthzDecisionQueryType)request;
		RequestType xacml20Request = xacml20DecisionQuery.getRequest();
		if(xacml20Request == null){
			return makeErrorResponse(request);
		}
		try{
			if(!validateRequestSignature(request)){
				return makeErrorResponse(request);
			}
			return makeErrorResponse(request);
		}catch(Exception e){
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
		validator.validate(request.getSignature());
		if(request.getIssuer() == null){
			return false;
		}
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add( new EntityIDCriteria(request.getIssuer().getValue()));
		criteriaSet.add( new MetadataCriteria(SPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS));
		criteriaSet.add( new UsageCriteria(UsageType.SIGNING));
	    return trustEngine.validate(request.getSignature(), criteriaSet);
	}
	
	public boolean validateRequest(RequestAbstractType request)
	{
		return true;
	}
}
