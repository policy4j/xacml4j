package com.artagon.xacml.v3.marshall;

import java.io.IOException;

import com.artagon.xacml.v3.XacmlSyntaxException;

public interface Unmarshaller<T>
{
	T unmarshal(Object source) 
		throws XacmlSyntaxException, IOException;
}
