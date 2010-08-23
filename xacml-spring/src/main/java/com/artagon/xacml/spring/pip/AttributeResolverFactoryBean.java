package com.artagon.xacml.spring.pip;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.spi.pip.AttributeResolver;
import com.google.common.base.Preconditions;

public class AttributeResolverFactoryBean extends AbstractFactoryBean
{
	private AttributeResolver providerInstance;
		
	public void setRef(AttributeResolver beanRef){
		this.providerInstance = beanRef;
	}
	
	@Override
	public Class<AttributeResolver> getObjectType() {
		return AttributeResolver.class;
	}
	
	@Override
	protected Object createInstance() throws Exception 
	{
		Preconditions.checkState(providerInstance != null);
		return providerInstance;
	}
}


