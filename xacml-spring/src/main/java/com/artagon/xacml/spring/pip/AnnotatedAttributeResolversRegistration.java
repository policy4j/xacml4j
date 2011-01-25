package com.artagon.xacml.spring.pip;

import java.util.Collection;

import com.artagon.xacml.v30.spi.pip.AnnotatedResolverFactory;
import com.artagon.xacml.v30.spi.pip.AttributeResolver;
import com.google.common.base.Preconditions;

public class AnnotatedAttributeResolversRegistration implements AttributeResolverRegistration
{
	private String policyId;
	private Collection<AttributeResolver> resolvers;
	
	public AnnotatedAttributeResolversRegistration(
			Object instance, String policyId) 
		throws Exception
	{
		Preconditions.checkNotNull(instance);
		this.resolvers = new AnnotatedResolverFactory().getAttributeResolvers(instance);
	}
	
	public AnnotatedAttributeResolversRegistration(
			Object instance) throws Exception
	{
		this(instance, null);
	}
	
	public Collection<AttributeResolver> getResolvers(){
		return resolvers;
	}
	
	public String getPolicyId(){
		return policyId;
	}
}


