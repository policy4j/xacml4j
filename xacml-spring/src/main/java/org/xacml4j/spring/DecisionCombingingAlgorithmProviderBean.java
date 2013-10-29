package org.xacml4j.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;

import com.google.common.base.Preconditions;

public class DecisionCombingingAlgorithmProviderBean
	extends AbstractFactoryBean<DecisionCombiningAlgorithmProvider>
{
	private Class<?> providerClass;
	private DecisionCombiningAlgorithmProvider providerInstance;

	public void setClass(Class<?> clazz){
		Preconditions.checkNotNull(clazz);
		Preconditions.checkState(providerInstance == null,
				"Either provider instance or class can be specified, NOT both");
		this.providerClass = clazz;
	}

	public void setRef(DecisionCombiningAlgorithmProvider provider){
		Preconditions.checkNotNull(provider);
		Preconditions.checkState(providerClass == null,
				"Either provider instance or class can be specified, NOT both");
		this.providerInstance = provider;
	}

	@Override
	protected DecisionCombiningAlgorithmProvider createInstance()
			throws Exception {
		Preconditions.checkState(providerInstance != null);
		return providerInstance;
	}

	public Class<?> getProviderClass(){
		return providerClass;
	}

	@Override
	public Class<?> getObjectType() {
		return DecisionCombiningAlgorithmProvider.class;
	}
}
