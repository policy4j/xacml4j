package com.artagon.xacml.spring;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.policy.combine.DefaultXacml30DecisionCombiningAlgorithms;
import com.artagon.xacml.v30.spi.combine.AggregatingDecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import com.google.common.base.Preconditions;

public class DecisionCombiningAlgorithmsProvider 
	extends AbstractFactoryBean<DecisionCombiningAlgorithmProvider>
{
	private Collection<DecisionCombiningAlgorithmProvider> providers;
	
	public DecisionCombiningAlgorithmsProvider(){
		this.providers = new LinkedList<DecisionCombiningAlgorithmProvider>();
		this.providers.add(new DefaultXacml30DecisionCombiningAlgorithms());
	}
	public void setProviders(Collection<DecisionCombiningAlgorithmProvider> providers)
	{
		Preconditions.checkNotNull(providers);
		this.providers.addAll(providers);
	}

	@Override
	protected DecisionCombiningAlgorithmProvider createInstance()
			throws Exception 
	{
		return new AggregatingDecisionCombiningAlgorithmProvider(providers);
	}

	@Override
	public Class<DecisionCombiningAlgorithmProvider> getObjectType() {
		return DecisionCombiningAlgorithmProvider.class;
	}
	
	
}
