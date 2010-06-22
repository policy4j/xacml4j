package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestSyntaxException;

public interface RequestUnmarshaller 
{
	Request unmarshalRequest(Object source) 
		throws RequestSyntaxException, IOException;
}
