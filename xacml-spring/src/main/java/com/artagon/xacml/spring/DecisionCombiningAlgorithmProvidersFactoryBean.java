package com.artagon.xacml.spring;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import com.google.common.base.Preconditions;

public class DecisionCombiningAlgorithmProvidersFactoryBean 
	extends AbstractFactoryBean<DecisionCombiningAlgorithmProvider>
{
	private DecisionCombiningAlgorithmProviderBuilder builder;
	
	public DecisionCombiningAlgorithmProvidersFactoryBean(){
		this.builder = DecisionCombiningAlgorithmProviderBuilder.builder();
	}
	public void setProviders(Collection<DecisionCombiningAlgorithmProvider> providers)
	{
		Preconditions.checkNotNull(providers);
		this.builder.withAlgorithmProviders(providers);
	}

	@Override
	protected DecisionCombiningAlgorithmProvider createInstance()
			throws Exception 
	{
		return builder.build();
	}

	@Override
	public Class<DecisionCombiningAlgorithmProvider> getObjectType() {
		return DecisionCombiningAlgorithmProvider.class;
	}
	
	
}
