package com.artagon.xacml.v20.endpoints;

import org.springframework.oxm.Marshaller;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.artagon.xacml.v20.Xacml20RequestUnmarshaller;
import com.artagon.xacml.v20.Xacml20ResponseMarshaller;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.artagon.xacml.v3.marshall.ResponseMarshaller;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;

public class Xacml20Endpoint extends AbstractMarshallingPayloadEndpoint 
{
	private PolicyDecisionPoint pdp;
	private RequestUnmarshaller requestUnmarshaller;
	private ResponseMarshaller responseMarshaller;
	
	public Xacml20Endpoint(Marshaller marshaller, 
			PolicyDecisionPoint pdp) {
		super(marshaller);
		this.pdp = pdp;
		this.requestUnmarshaller = new Xacml20RequestUnmarshaller();
		this.responseMarshaller = new Xacml20ResponseMarshaller();
	}
	
	protected Object invokeInternal(Object source) throws Exception 
	{
		RequestContext request = requestUnmarshaller.unmarshal(source);
		ResponseContext response = pdp.decide(request);
		return responseMarshaller.marshal(response);
	}
}
