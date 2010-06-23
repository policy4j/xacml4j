package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.ResponseSyntaxException;

public interface ResponseUnmarshaller 
{
	Response unmarshal(Object source) 
		throws ResponseSyntaxException, IOException;
}
