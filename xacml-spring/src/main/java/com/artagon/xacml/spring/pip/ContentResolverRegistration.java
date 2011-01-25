package com.artagon.xacml.spring.pip;

import java.util.Collection;
import java.util.Collections;

import com.artagon.xacml.v30.spi.pip.ContentResolver;

public class ContentResolverRegistration 
{
	private String policyId;
	private Collection<ContentResolver> resolvers;
	
	public ContentResolverRegistration(String policyId, 
			Collection<ContentResolver> resolvers){
		this.policyId = policyId;
		this.resolvers = resolvers;
	}
	
	public ContentResolverRegistration(
			Collection<ContentResolver> resolvers){
		this(null, resolvers);
	}
	
	public ContentResolverRegistration(String policyId, 
			ContentResolver resolver){
		this(policyId, Collections.singleton(resolver));
	}
	
	public ContentResolverRegistration(ContentResolver resolver){
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
	public Collection<ContentResolver> getResolvers(){
		return resolvers;
	}
}
