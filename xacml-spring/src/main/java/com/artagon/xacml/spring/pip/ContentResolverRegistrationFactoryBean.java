package com.artagon.xacml.spring.pip;


import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.pip.AnnotatedResolverFactory;
import com.artagon.xacml.v30.spi.pip.ContentResolver;
import com.google.common.base.Preconditions;

public class ContentResolverRegistrationFactoryBean extends AbstractFactoryBean<ContentResolverRegistration>
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
	protected ContentResolverRegistration createInstance() throws Exception 
	{
		Preconditions.checkState(instance != null);
		if(instance instanceof ContentResolver){
			return new ContentResolverRegistration(policyId, (ContentResolver)instance);
		}
		return new ContentResolverRegistration(policyId, 
				new AnnotatedResolverFactory().getContentResolvers(instance));
	}

	@Override
	public Class<ContentResolverRegistration> getObjectType() {
		return ContentResolverRegistration.class;
	}
}



