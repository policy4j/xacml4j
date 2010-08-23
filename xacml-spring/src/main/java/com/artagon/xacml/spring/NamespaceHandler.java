package com.artagon.xacml.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.artagon.xacml.spring.domain.PolicyDomainDefinitionParser;

public class NamespaceHandler extends NamespaceHandlerSupport
{

	@Override
	public void init() 
	{
		registerBeanDefinitionParser("FunctionProviders", new FunctionProvidersDefintionParser());
		registerBeanDefinitionParser("PolicyInformationPoint",  new PolicyInformationPointDefinitionParser());
		registerBeanDefinitionParser("PolicyDomain",  new PolicyDomainDefinitionParser());
	}
}
