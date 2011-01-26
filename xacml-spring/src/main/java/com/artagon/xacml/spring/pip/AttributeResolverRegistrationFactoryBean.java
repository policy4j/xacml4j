package com.artagon.xacml.spring.pip;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.pip.AnnotatedResolverFactory;
import com.artagon.xacml.v30.spi.pip.AttributeResolver;
import com.google.common.base.Preconditions;

public class AttributeResolverRegistrationFactoryBean 
extends AbstractFactoryBean<AttributeResolverRegistration>
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
	protected AttributeResolverRegistration createInstance() throws Exception 
	{
		Preconditions.checkState(instance != null, 
				"Resolver instance can not be null");
		if(instance instanceof AttributeResolver){
			return new AttributeResolverRegistration(policyId, (AttributeResolver)instance);
		}
		return new AttributeResolverRegistration(policyId, 
				new AnnotatedResolverFactory().getAttributeResolvers(instance));
	}

	@Override
	public Class<AttributeResolverRegistration> getObjectType() {
		return AttributeResolverRegistration.class;
	}
}


