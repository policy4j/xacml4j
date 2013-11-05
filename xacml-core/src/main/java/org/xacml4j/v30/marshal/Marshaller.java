package org.xacml4j.v30.marshal;

import java.io.IOException;

public interface Marshaller <T>
{
	Object marshal(T source)
		throws IOException;

	void marshal(T object, Object source)
		throws IOException;
}
