package org.xacml4j.spring;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;

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
		return builder.create();
	}

	@Override
	public Class<DecisionCombiningAlgorithmProvider> getObjectType() {
		return DecisionCombiningAlgorithmProvider.class;
	}
	
	
}
