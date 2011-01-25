package com.artagon.xacml.spring.pip;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.pip.AttributeResolver;
import com.artagon.xacml.v30.spi.pip.DefaultResolverRegistry;
import com.artagon.xacml.v30.spi.pip.ResolverRegistry;

public class ResolverRegistryFactoryBean extends AbstractFactoryBean<ResolverRegistry>
{
	private Collection<AttributeResolverRegistration> attributeResolvers;
	
	
	public ResolverRegistryFactoryBean(){
		this.attributeResolvers = new LinkedList<AttributeResolverRegistration>();
	}
	
	public void setAttributeResolvers(
			Collection<AttributeResolverRegistration> resolvers){
		this.attributeResolvers = resolvers;
	}
	
	@Override
	protected ResolverRegistry createInstance() 
		throws Exception 
	{
		ResolverRegistry r = new DefaultResolverRegistry();
		for(AttributeResolverRegistration areg : attributeResolvers){
			if(areg.getPolicyId() == null){
				for(AttributeResolver ar : areg.getResolvers()){
					r.addResolver(ar);
				}
				continue;
			}
			if(areg.getPolicyId() != null){
				for(AttributeResolver ar : areg.getResolvers()){
					r.addResolver(areg.getPolicyId(), ar);
				}
				continue;
			}
		}
		return r;
	}
	
	@Override
	public Class<ResolverRegistry> getObjectType() 
	{
		return ResolverRegistry.class;
	}
}
