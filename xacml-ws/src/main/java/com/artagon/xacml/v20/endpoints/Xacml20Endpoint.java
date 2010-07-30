package com.artagon.xacml.v20.endpoints;

import org.springframework.oxm.Marshaller;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

import com.artagon.xacml.v20.Xacml20RequestUnmarshaller;
import com.artagon.xacml.v20.Xacml20ResponseMarshaller;
import com.artagon.xacml.v3.DefaultXacmlFactory;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;
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
		this.requestUnmarshaller = new Xacml20RequestUnmarshaller(new DefaultXacmlFactory());
		this.responseMarshaller = new Xacml20ResponseMarshaller(new DefaultXacmlFactory());
	}
	
	protected Object invokeInternal(Object source) throws Exception 
	{
		Request request = requestUnmarshaller.unmarshalRequest(source);
		Response response = pdp.decide(request);
		return responseMarshaller.marshall(response);
	}
}
