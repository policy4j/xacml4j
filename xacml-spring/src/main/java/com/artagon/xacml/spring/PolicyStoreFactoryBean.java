package com.artagon.xacml.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v3.spi.PolicyStore;

public class PolicyStoreFactoryBean extends AbstractFactoryBean
{

	@Override
	public Class<PolicyStore> getObjectType() {
		return PolicyStore.class;
	}

	@Override
	protected Object createInstance() throws Exception {
		return null;
	}
	
}
