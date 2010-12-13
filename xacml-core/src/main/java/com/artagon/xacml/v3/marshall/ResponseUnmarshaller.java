package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.XacmlSyntaxException;

public interface ResponseUnmarshaller 
{
	ResponseContext unmarshal(Object source) 
		throws XacmlSyntaxException, IOException;
}
