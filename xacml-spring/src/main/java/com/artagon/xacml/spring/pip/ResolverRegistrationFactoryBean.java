package com.artagon.xacml.spring.pip;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.google.common.base.Preconditions;

public class ResolverRegistrationFactoryBean 
extends AbstractFactoryBean<ResolverRegistration>
{
	private String policyId;
	private Object instance;
	
	/**
	 * Sets optionally resolver policy 
	 * or policy set identifier
	 * @param policyId a policy or policy set identifier
	 */
	public void setPolicyId(String policyId){
		this.policyId = policyId;
	}
	
	/**
	 * Sets actual resolver instance
	 * 
	 * @param instance a resolver bean instance
	 */
	public void setResolver(Object instance){
		this.instance = instance;
	}

	@Override
	protected ResolverRegistration createInstance() throws Exception 
	{
		Preconditions.checkState(instance != null, 
				"Resolver instance can not be null");
		return new ResolverRegistration(policyId, instance);
	}

	@Override
	public Class<ResolverRegistration> getObjectType() {
		return ResolverRegistration.class;
	}
}


