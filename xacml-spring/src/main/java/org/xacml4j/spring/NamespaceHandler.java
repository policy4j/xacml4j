package org.xacml4j.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.xacml4j.spring.pdp.PolicyDecisionPointDefinitionParser;
import org.xacml4j.spring.pdp.RequestContextHandlerChainDefinitionParser;
import org.xacml4j.spring.pip.PolicyInformationPointDefinitionParser;
import org.xacml4j.spring.pip.ResolverRegistryDefinitionParser;
import org.xacml4j.spring.repository.InMemoryPolicyRepositoryDefinitionParser;


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
