package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.ResponseContext;

public interface ResponseMarshaller 
{
	Object marshal(ResponseContext request) 
		throws IOException;
	
	void marshal(ResponseContext response, Object source) 
		throws IOException;
}
