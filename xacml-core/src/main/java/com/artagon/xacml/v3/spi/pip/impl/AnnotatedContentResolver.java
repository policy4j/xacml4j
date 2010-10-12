package com.artagon.xacml.v3.spi.pip.impl;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.spi.pip.BaseContentResolver;
import com.artagon.xacml.v3.spi.pip.ContentResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.PolicyInformationPointContext;

public class AnnotatedContentResolver extends BaseContentResolver
{
	private AnnotatedContentResolver(ContentResolverDescriptor descriptor){
		super(descriptor);
	}
	
	@Override
	protected Node doResolve(AttributeCategory category,
			PolicyInformationPointContext context) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
