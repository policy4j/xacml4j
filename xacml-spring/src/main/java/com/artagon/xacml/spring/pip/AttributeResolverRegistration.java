package com.artagon.xacml.spring.pip;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v30.spi.pip.AttributeResolver;

public class AttributeResolverRegistration 
{
	private String policyId;
	private Collection<AttributeResolver> resolvers;
	
	public AttributeResolverRegistration(String policyId, 
			Collection<AttributeResolver> resolvers){
		this.policyId = policyId;
		this.resolvers = resolvers;
	}
	
	public AttributeResolverRegistration(
			Collection<AttributeResolver> resolvers){
		this(null, resolvers);
	}
	
	public AttributeResolverRegistration(String policyId, 
			AttributeResolver resolver){
		this(policyId, Collections.singleton(resolver));
	}
	
	public AttributeResolverRegistration( AttributeResolver resolver){
		this(null, Collections.singleton(resolver));
	}
	
	/**
	 * Gets policy identifier
	 * 
	 * @return
	 */
	public String getPolicyId(){
		return policyId;
	}
	
	/**
	 * Gets resolvers
	 * 
	 * @return
	 */
	public Collection<AttributeResolver> getResolvers(){
		return resolvers;
	}
}
