package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.context.ResponseContext;

public interface ResponseUnmarshaller 
{
	ResponseContext unmarshal(Object source) 
		throws XacmlSyntaxException, IOException;
}
