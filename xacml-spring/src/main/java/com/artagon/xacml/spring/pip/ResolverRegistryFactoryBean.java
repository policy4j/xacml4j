package com.artagon.xacml.spring.pip;

import java.util.Collection;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.artagon.xacml.v30.spi.pip.DefaultResolverRegistry;
import com.artagon.xacml.v30.spi.pip.ResolverRegistry;

public class ResolverRegistryFactoryBean extends AbstractFactoryBean<ResolverRegistry>
{
	private Collection<AttributeResolverRegistration> attributeResolverReg;
	private Collection<ContentResolverRegistration> contentResolverReg;
	
	
	public void setAttributeResolvers(
			Collection<AttributeResolverRegistration> resolvers){
		this.attributeResolverReg = resolvers;
	}
	
	public void setContentResolvers(
			Collection<ContentResolverRegistration> resolvers){
		this.contentResolverReg = resolvers;
	}
	
	@Override
	protected ResolverRegistry createInstance() 
		throws Exception 
	{
		ResolverRegistry r = new DefaultResolverRegistry();
		for(AttributeResolverRegistration areg : attributeResolverReg){
			r.addAttributeResolvers(areg.getPolicyId(), areg.getResolvers());
		}
		for(ContentResolverRegistration areg : contentResolverReg){
			r.addContentResolvers(areg.getPolicyId(), areg.getResolvers());
		}
		return r;
	}
	
	@Override
	public Class<ResolverRegistry> getObjectType() 
	{
		return ResolverRegistry.class;
	}
}
