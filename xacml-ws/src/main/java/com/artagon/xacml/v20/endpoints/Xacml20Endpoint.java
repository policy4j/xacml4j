package com.artagon.xacml.v20.endpoints;

import javax.xml.bind.JAXBElement;

import org.oasis.xacml.v20.context.ObjectFactory;
import org.oasis.xacml.v20.context.RequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.Marshaller;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.artagon.xacml.v20.Xacml20ContextMapper;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;

public class Xacml20Endpoint extends AbstractMarshallingPayloadEndpoint 
{
	private final static Logger log = LoggerFactory.getLogger(Xacml20Endpoint.class);
	
	private PolicyDecisionPoint pdp;
	private Xacml20ContextMapper mapper;
	private ObjectFactory contextFactory;
	
	public Xacml20Endpoint(Marshaller marshaller, 
			PolicyDecisionPoint pdp) {
		super(marshaller);
		this.mapper = new Xacml20ContextMapper();
		this.pdp = pdp;
		this.contextFactory = new ObjectFactory();
	}
	
	@SuppressWarnings("unchecked")
	protected Object invokeInternal(Object request) throws Exception 
	{
		JAXBElement<RequestType> jaxbRequest = (JAXBElement<RequestType>)request;
		Request xacmlReq = mapper.create(jaxbRequest.getValue());
		Response xacmlRes = pdp.decide(xacmlReq);
		if(log.isDebugEnabled()){
			log.debug("Response=\"{}\"", xacmlRes);
		}
		return contextFactory.createResponse(mapper.create(xacmlRes));
	}
}
