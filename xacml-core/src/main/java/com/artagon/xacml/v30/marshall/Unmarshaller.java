package com.artagon.xacml.v30.marshall;

import java.io.IOException;

import com.artagon.xacml.v30.XacmlSyntaxException;

public interface Unmarshaller<T>
{
	T unmarshal(Object source) 
		throws XacmlSyntaxException, IOException;
}
