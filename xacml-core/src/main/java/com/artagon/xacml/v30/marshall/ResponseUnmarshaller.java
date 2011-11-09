package com.artagon.xacml.v30.marshall;

import java.io.IOException;

import com.artagon.xacml.v30.pdp.ResponseContext;
import com.artagon.xacml.v30.pdp.XacmlSyntaxException;

public interface ResponseUnmarshaller 
{
	ResponseContext unmarshal(Object source) 
		throws XacmlSyntaxException, IOException;
}
