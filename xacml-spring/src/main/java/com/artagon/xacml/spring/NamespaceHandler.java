package com.artagon.xacml.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.artagon.xacml.spring.domain.PolicyDomainDefinitionParser;
import com.artagon.xacml.spring.pdp.PolicyDecisionPointDefinitionParser;
import com.artagon.xacml.spring.pip.PolicyInformationPointDefinitionParser;
import com.artagon.xacml.spring.repository.InMemoryPolicyRepositoryDefinitionParser;

public class NamespaceHandler extends NamespaceHandlerSupport
{

	@Override
	public void init() 
	{
		registerBeanDefinitionParser("FunctionProviders", new FunctionProvidersDefintionParser());
		registerBeanDefinitionParser("PolicyInformationPoint",  new PolicyInformationPointDefinitionParser());
		registerBeanDefinitionParser("PolicyDomain",  new PolicyDomainDefinitionParser());
		registerBeanDefinitionParser("PolicyDecisionPoint",  new PolicyDecisionPointDefinitionParser());
		registerBeanDefinitionParser("InMemoryPolicyRepository",  new InMemoryPolicyRepositoryDefinitionParser());
	}
}
