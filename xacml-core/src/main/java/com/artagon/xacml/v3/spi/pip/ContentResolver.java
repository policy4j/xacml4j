package com.artagon.xacml.v3.spi.pip;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.BagOfAttributeValues;

public interface ContentResolver 
{
	ContentResolverDescriptor getDescriptor();
	
	Node resolve(
			PolicyInformationPointContext context, 
			BagOfAttributeValues ...keys) throws Exception;
}
