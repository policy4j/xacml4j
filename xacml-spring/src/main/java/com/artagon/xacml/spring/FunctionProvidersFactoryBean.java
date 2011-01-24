package com.artagon.xacml.spring;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.policy.function.DefaultXacml30Functions;
import com.artagon.xacml.v30.spi.function.AggregatingFunctionProvider;
import com.artagon.xacml.v30.spi.function.FunctionProvider;

public class FunctionProvidersFactoryBean extends AbstractFactoryBean<FunctionProvider>
{
	private Collection<FunctionProvider> providers;
	
	public FunctionProvidersFactoryBean() throws Exception
	{
		this.providers = new LinkedList<FunctionProvider>();
		this.providers.add(new DefaultXacml30Functions());
	}
	
	public void setProviders(Collection<FunctionProvider> providers){
		this.providers.addAll(providers);
	}
	
	@Override
	public Class<FunctionProvider> getObjectType() {
		return FunctionProvider.class;
	}

	@Override
	protected FunctionProvider createInstance() throws Exception {	
		return new AggregatingFunctionProvider(providers);
	}	
}
