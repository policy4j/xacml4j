package com.artagon.xacml.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.artagon.xacml.spring.pdp.PolicyDecisionPointDefinitionParser;
import com.artagon.xacml.spring.pdp.RequestContextHandlerChainDefinitionParser;
import com.artagon.xacml.spring.pip.PolicyInformationPointDefinitionParser;
import com.artagon.xacml.spring.pip.ResolverRegistryDefinitionParser;
import com.artagon.xacml.spring.repository.InMemoryPolicyRepositoryDefinitionParser;

public class NamespaceHandler extends NamespaceHandlerSupport
{

	@Override
	public void init() 
	{
		registerBeanDefinitionParser("FunctionProviders", new FunctionProvidersDefintionParser());
		registerBeanDefinitionParser("DecisionCombingingAlgorithms", new DecisionCombiningAlgorithmProvidersDefinitionParser());
		registerBeanDefinitionParser("PolicyInformationPoint",  new PolicyInformationPointDefinitionParser());
		registerBeanDefinitionParser("RequestContextHandlerChain",  new RequestContextHandlerChainDefinitionParser());
		registerBeanDefinitionParser("ResolverRegistry",  new ResolverRegistryDefinitionParser());
		registerBeanDefinitionParser("PolicyDecisionPoint",  new PolicyDecisionPointDefinitionParser());
		registerBeanDefinitionParser("InMemoryPolicyRepository",  new InMemoryPolicyRepositoryDefinitionParser());
	}
}
