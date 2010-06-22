package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;

public interface XacmlContextMarshaller 
{
	Object marshall(Request request) throws IOException;
	Object marshall(Response response) throws IOException;
	void marshall(Request request,  Object source) throws IOException;
}
