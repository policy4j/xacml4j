package com.artagon.xacml.spring.pip;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.pip.ResolverRegistry;
import com.artagon.xacml.v30.spi.pip.ResolverRegistryBuilder;
import com.google.common.base.Preconditions;

public class ResolverRegistryFactoryBean extends AbstractFactoryBean<ResolverRegistry>
{
	private ResolverRegistryBuilder registryBuilder;
	
	public ResolverRegistryFactoryBean(){
		this.registryBuilder = ResolverRegistryBuilder.builder();
	}
	
	public void setResolvers(
			Collection<ResolverRegistration> resolvers){
		Preconditions.checkNotNull(resolvers);
		for(ResolverRegistration registration : resolvers){
			if(registration.getPolicyId() == null){
				registryBuilder.withResolver(
						registration.getResolver());
				continue;
			}
			registryBuilder.withPolicyScopedResolver(
					registration.getPolicyId(), 
					registration.getResolver());
		}
	}
	
	@Override
	protected ResolverRegistry createInstance() 
		throws Exception 
	{
		return registryBuilder.build();
	}
	
	@Override
	public Class<ResolverRegistry> getObjectType() 
	{
		return ResolverRegistry.class;
	}
}
