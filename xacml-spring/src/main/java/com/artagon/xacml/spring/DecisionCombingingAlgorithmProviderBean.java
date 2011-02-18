package com.artagon.xacml.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import com.google.common.base.Preconditions;

public class DecisionCombingingAlgorithmProviderBean 
	extends AbstractFactoryBean<DecisionCombiningAlgorithmProvider>
{
	private Class<?> providerClass;
	private DecisionCombiningAlgorithmProvider providerInstance;
	
	public void setClass(Class<?> clazz){
		Preconditions.checkNotNull(clazz);
		this.providerClass = clazz;
	}
	
	public void setRef(DecisionCombiningAlgorithmProvider provider){
		Preconditions.checkNotNull(provider);
		this.providerInstance = provider;
	}

	@Override
	protected DecisionCombiningAlgorithmProvider createInstance()
			throws Exception {
		Preconditions.checkState(providerInstance != null);
		return providerInstance;
	}

	@Override
	public Class<?> getObjectType() {
		return DecisionCombiningAlgorithmProvider.class;
	}
}
