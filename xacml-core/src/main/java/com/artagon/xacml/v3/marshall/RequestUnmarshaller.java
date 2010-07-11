package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestSyntaxException;

public interface RequestUnmarshaller 
{
	/**
	 * Creates XACML request from a given source
	 * 
	 * @param source an XACML request
	 * @return {@link Request} instance
	 * @throws RequestSyntaxException
	 * @throws IOException
	 */
	Request unmarshalRequest(Object source) 
		throws RequestSyntaxException, IOException;
}
