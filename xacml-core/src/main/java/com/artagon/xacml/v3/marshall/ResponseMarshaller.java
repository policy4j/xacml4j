package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.Response;

public interface ResponseMarshaller 
{
	Object marshall(Response request) throws IOException;
	void marshall(Response request,  Object source) throws IOException;
}
