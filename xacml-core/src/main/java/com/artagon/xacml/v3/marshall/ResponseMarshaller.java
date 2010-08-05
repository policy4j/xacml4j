package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.ResponseContext;

public interface ResponseMarshaller 
{
	Object marshall(ResponseContext request) throws IOException;
	void marshall(ResponseContext request,  Object source) throws IOException;
}
