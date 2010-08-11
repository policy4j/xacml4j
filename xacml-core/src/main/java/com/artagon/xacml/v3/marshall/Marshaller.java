package com.artagon.xacml.v3.marshall;

import java.io.IOException;

public interface Marshaller <T> 
{
	T marshal(Object source) throws IOException;
	
	void marshal(T object, Object source) throws IOException;
}
