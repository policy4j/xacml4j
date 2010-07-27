package com.artagon.xacml.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;

import com.google.common.base.Preconditions;

public class FunctionProviderFactoryBean extends AbstractFactoryBean
{
	private Class<FunctionProvider> providerClass;
	private Object providerInstance;
		
	public void setClass(Class<FunctionProvider> providerClazz)
	{
		this.providerClass = providerClazz;
	}
	
	public void setRef(Object beanRef){
		this.providerInstance = beanRef;
	}
	
	@Override
	public Class<FunctionProvider> getObjectType() {
		return FunctionProvider.class;
	}

	@Override
	protected Object createInstance() throws Exception 
	{
		Preconditions.checkState((providerClass == null ^ providerInstance == null), 
				"Either fucnction provider class or " +
				"instance reference needs to be specified not both");
		return (providerClass != null)?
				new AnnotiationBasedFunctionProvider(providerClass):
					new AnnotiationBasedFunctionProvider(providerInstance);
	}
}
