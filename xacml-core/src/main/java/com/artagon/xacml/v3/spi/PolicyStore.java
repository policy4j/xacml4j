package com.artagon.xacml.v3.spi;

import java.util.Collection;

import com.artagon.xacml.v3.CompositeDecisionRule;

public interface PolicyStore extends PolicyReferenceResolver
{
	/**
	 * Gets root policies
	 * 
	 * @return a collection of {@link CompositeDecisionRule} instances
	 */
	Collection<CompositeDecisionRule> getPolicies();
}
