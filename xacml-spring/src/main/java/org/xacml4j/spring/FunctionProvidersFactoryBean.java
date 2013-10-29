package org.xacml4j.spring;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;

import com.google.common.base.Preconditions;

public class FunctionProvidersFactoryBean extends AbstractFactoryBean<org.xacml4j.v30.spi.function.FunctionProvider>
{
	private FunctionProviderBuilder builder;

	public FunctionProvidersFactoryBean() throws Exception
	{
		this.builder = FunctionProviderBuilder.builder();
	}

	public void setProviders(Collection<FunctionProvider> providers){
		Preconditions.checkNotNull(providers);
		for(FunctionProvider p : providers){
			Preconditions.checkState(p.getProviderClass() != null
					|| p.getProviderInstance() != null);
			if(p.getProviderClass() != null){
				builder.fromClass(p.getProviderClass());
			}
			if(p.getProviderInstance() != null){
				builder.fromInstance(p.getProviderInstance());
			}
		}
	}

	@Override
	public Class<org.xacml4j.v30.spi.function.FunctionProvider> getObjectType() {
		return org.xacml4j.v30.spi.function.FunctionProvider.class;
	}

	@Override
	protected org.xacml4j.v30.spi.function.FunctionProvider createInstance() throws Exception {
		return builder.build();
	}
}
