package com.artagon.xacml.springws;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.springframework.ws.server.endpoint.AbstractStaxStreamPayloadEndpoint;

public class XacmlEndpoint extends AbstractStaxStreamPayloadEndpoint 
{
	@Override
	protected void invokeInternal(XMLStreamReader streamReader,
			XMLStreamWriter streamWriter) throws Exception 
	{
		throw new NullPointerException();
	}
}


