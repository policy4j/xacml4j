package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.Response;

public interface XacmlContextUnmarshaller 
{
	Request unmarshalRequest(Object source) 
		throws RequestSyntaxException, IOException;
	
	Response unmarshalResponse(Object source) 
		throws RequestSyntaxException, IOException;
}
