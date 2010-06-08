package com.artagon.xacml.v20.endpoints;

import org.oasis.xacml.v20.context.RequestType;
import org.springframework.oxm.Marshaller;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v20.Xacml20ContextMapper;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;

public class Xacml20Endpoint extends AbstractMarshallingPayloadEndpoint 
{
	private PolicyDecisionPoint pdp;
	private Xacml20ContextMapper mapper;
	
	public Xacml20Endpoint(Marshaller marshaller, 
			Xacml20ContextMapper mapper, 
			PolicyDecisionPoint pdp) {
		super(marshaller);
		this.mapper = mapper;
		this.pdp = pdp;
	}
	
	protected Object invokeInternal(Object request) throws Exception 
	{
		Request xacmlReq = mapper.create((RequestType)request);
		return mapper.create(pdp.decide(xacmlReq));
	}
}
