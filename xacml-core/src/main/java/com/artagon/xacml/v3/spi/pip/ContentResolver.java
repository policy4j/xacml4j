package com.artagon.xacml.v3.spi.pip;

import org.w3c.dom.Node;

public interface ContentResolver 
{
	ContentResolverDescriptor getDescriptor();
	
	Node resolve(
			PolicyInformationPointContext context) throws Exception;
}
