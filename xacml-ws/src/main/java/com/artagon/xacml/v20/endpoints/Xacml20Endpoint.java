package com.artagon.xacml.v20.endpoints;

import org.springframework.oxm.Marshaller;
import org.springframework.ws.server.endpoint.AbstractMarshallingPayloadEndpoint;

public class Xacml20Endpoint extends AbstractMarshallingPayloadEndpoint 
{
	
	public Xacml20Endpoint(Marshaller marshaller) {
		super(marshaller);
	}
	
	protected Object invokeInternal(Object request) throws Exception 
	{
	}
}
