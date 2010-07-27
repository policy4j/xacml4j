package com.artagon.xacml.spring;

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AggregatingFunctionProvider;

public class FunctionProvidersFactoryBean extends AbstractFactoryBean
{
	private Collection<FunctionProvider> providers;
	
	public void setProviders(Collection<FunctionProvider> providers)
	{
		this.providers = providers;
	}
	
	@Override
	public Class<FunctionProvider> getObjectType() {
		return FunctionProvider.class;
	}

	@Override
	protected Object createInstance() throws Exception 
	{	
		return new AggregatingFunctionProvider(
				(providers == null)?Collections.<FunctionProvider>emptyList():providers);
	}
	
}
