package com.artagon.xacml.v3.spi.pip;

import org.w3c.dom.Node;

public interface ContentResolver 
{
	Node getContent(PolicyInformationPointContext context) throws Exception;
}
