package com.artagon.xacml.v3.spi.pip;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;

public interface ContentResolver 
{
	ContentResolverDescriptor getDescriptor();
	
	Node getContent(AttributeCategory category, 
			PolicyInformationPointContext context);
}
