package org.xacml4j.v30.marshall;

import java.io.IOException;

import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.XacmlSyntaxException;


public interface ResponseUnmarshaller 
{
	ResponseContext unmarshal(Object source) 
		throws XacmlSyntaxException, IOException;
}
