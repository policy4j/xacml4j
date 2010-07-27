package com.artagon.xacml.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class NamespaceHandler extends NamespaceHandlerSupport
{

	@Override
	public void init() 
	{
		registerBeanDefinitionParser("function-providers", new FunctionProviderDefinitionParser());
	}
}
