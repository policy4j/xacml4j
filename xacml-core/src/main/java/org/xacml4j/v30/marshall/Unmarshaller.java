package org.xacml4j.v30.marshall;

import java.io.IOException;

import org.xacml4j.v30.XacmlSyntaxException;


public interface Unmarshaller<T>
{
	T unmarshal(Object source)
		throws XacmlSyntaxException, IOException;
}
