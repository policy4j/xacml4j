package com.artagon.xacml.spring;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.spi.combine.AggregatingDecisionCombiningAlgorithmProvider;
import com.google.common.base.Preconditions;

public class CombingingAlgorithmProviderFactoryBean extends AbstractFactoryBean
{
	private Collection<DecisionCombiningAlgorithm<CompositeDecisionRule>> policyAlgorithms;
	private Collection<DecisionCombiningAlgorithm<Rule>> ruleAlgorithms;
	private Collection<DecisionCombiningAlgorithmProvider> providers;
	
	public CombingingAlgorithmProviderFactoryBean(){
		this.providers = new LinkedList<DecisionCombiningAlgorithmProvider>();
	}
	
	public void setProviders(Collection<DecisionCombiningAlgorithmProvider> providers){
		this.providers.addAll(providers);
	}
	
	@Override
	public Class<DecisionCombiningAlgorithmProvider> getObjectType() {
		return DecisionCombiningAlgorithmProvider.class;
	}

	@Override
	protected Object createInstance() throws Exception 
	{
		Preconditions.checkState(!providers.isEmpty());
		return new AggregatingDecisionCombiningAlgorithmProvider(providers);
	}
	
}
