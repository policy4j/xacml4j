package org.xacml4j.spring;

/*
 * #%L
 * Xacml4J Spring 3.x Support Module
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
		registerBeanDefinitionParser("FunctionProviders", new FunctionProvidersDefinitionParser());
		registerBeanDefinitionParser("DecisionCombiningAlgorithms", new DecisionCombiningAlgorithmProvidersDefinitionParser());
		registerBeanDefinitionParser("PolicyInformationPoint",  new PolicyInformationPointDefinitionParser());
		registerBeanDefinitionParser("RequestContextHandlerChain",  new RequestContextHandlerChainDefinitionParser());
		registerBeanDefinitionParser("ResolverRegistry",  new ResolverRegistryDefinitionParser());
		registerBeanDefinitionParser("PolicyDecisionPoint",  new PolicyDecisionPointDefinitionParser());
		registerBeanDefinitionParser("InMemoryPolicyRepository",  new InMemoryPolicyRepositoryDefinitionParser());
	}
}
