package com.artagon.xacml.spring.pip;

import java.util.Collection;

import com.artagon.xacml.v30.spi.pip.AttributeResolver;

public interface AttributeResolverRegistration 
{
	/**
	 * Gets policy identifier
	 * 
	 * @return
	 */
	String getPolicyId();
	
	/**
	 * Gets resolvers
	 * 
	 * @return
	 */
	Collection<AttributeResolver> getResolvers();
}
