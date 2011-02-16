package com.artagon.xacml.spring;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.function.FunctionProviderBuilder;
import com.google.common.base.Preconditions;

public class FunctionProvidersFactoryBean extends AbstractFactoryBean<com.artagon.xacml.v30.spi.function.FunctionProvider>
{
	private FunctionProviderBuilder builder;
	
	public FunctionProvidersFactoryBean() throws Exception
	{
		this.builder = FunctionProviderBuilder.builder();
	}
	
	public void setProviders(Collection<FunctionProvider> providers){
		Preconditions.checkNotNull(providers);
		for(FunctionProvider p : providers){
			Preconditions.checkState(p.getProviderClass() != null);
			builder.withFunctions(p.getProviderClass());
		}
	}
	
	@Override
	public Class<com.artagon.xacml.v30.spi.function.FunctionProvider> getObjectType() {
		return com.artagon.xacml.v30.spi.function.FunctionProvider.class;
	}

	@Override
	protected com.artagon.xacml.v30.spi.function.FunctionProvider createInstance() throws Exception {	
		return builder.build();
	}	
}
