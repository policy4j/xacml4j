package com.artagon.xacml.v30.spi.pip;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.artagon.xacml.v30.XacmlSyntaxException;
import com.google.common.base.Preconditions;

public final class ResolverRegistryBuilder 
{
	private Map<String, AttributeResolver> policyScopedAttributeResolvers;
	private Map<String, ContentResolver> policyScopedContentResolvers;
	private Collection<AttributeResolver> attributeResolvers;
	private Collection<ContentResolver> contentResolvers;
	private AnnotatedResolverFactory annotatedResolverFactory;
	
	private ResolverRegistryBuilder(){
		this.policyScopedAttributeResolvers = new HashMap<String, AttributeResolver>();
		this.policyScopedContentResolvers = new HashMap<String, ContentResolver>();
		this.attributeResolvers = new LinkedList<AttributeResolver>();
		this.contentResolvers = new LinkedList<ContentResolver>();
		this.annotatedResolverFactory = new AnnotatedResolverFactory();
	}
	
	/**
	 * Creates a builder with a default 
	 * {@link ResolverRegistry} implementation
	 * 
	 * @return {@link ResolverRegistryBuilder}
	 */
	public static ResolverRegistryBuilder builder(){
		return new ResolverRegistryBuilder();
	}
	
	/**
	 * Adds default XACML 3.0 resolvers
	 * 
	 * @return {@link ResolverRegistryBuilder}
	 */
	public ResolverRegistryBuilder withDefaultResolvers(){
		return withAttributeResolver(new DefaultEnviromentAttributeResolver());
	}
	
	/**
	 * Adds a given {@link AttributeResolver}
	 * 
	 * @param resolver an attribute resolver
	 * @return {@link ResolverRegistryBuilder}
	 */
	public ResolverRegistryBuilder withAttributeResolver(AttributeResolver resolver){
		Preconditions.checkNotNull(resolver);
		this.attributeResolvers.add(resolver);
		return this;
	}
	
	public ResolverRegistryBuilder withAttributeResolvers(Iterable<AttributeResolver> resolvers){
		for(AttributeResolver r : resolvers){
			withAttributeResolver(r);
		}
		return this;
	}
	
	public ResolverRegistryBuilder withContentResolvers(Iterable<ContentResolver> resolvers){
		for(ContentResolver r : resolvers){
			withContentResolver(r);
		}
		return this;
	}
	
	public ResolverRegistryBuilder withContentResolver(ContentResolver resolver){
		Preconditions.checkNotNull(resolver);
		this.contentResolvers.add(resolver);
		return this;
	}
	
	public ResolverRegistryBuilder withResolver(Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		
		if((annotatedResolver instanceof AttributeResolver) ||
				annotatedResolver instanceof ContentResolver){	
			if(annotatedResolver instanceof AttributeResolver){
				withAttributeResolver(
						(AttributeResolver)annotatedResolver);
			}
			if(annotatedResolver instanceof ContentResolver){
				withContentResolver(
						(ContentResolver)annotatedResolver);
			}
			return this;
		}
		try{
			withAttributeResolvers(annotatedResolverFactory.getAttributeResolvers(annotatedResolver));
			withContentResolvers(annotatedResolverFactory.getContentResolvers(annotatedResolver));
			return this;
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public ResolverRegistryBuilder withPolicyScopedResolver(
			String policyId, 
			Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		if((annotatedResolver instanceof AttributeResolver) ||
				annotatedResolver instanceof ContentResolver){
			
			if(annotatedResolver instanceof AttributeResolver){
				withPolicyScopedAttributeResolver(
						policyId, (AttributeResolver)annotatedResolver);
			}
			if(annotatedResolver instanceof ContentResolver){
				return withPolicyScopedContentResolver(
						policyId, (ContentResolver)annotatedResolver);
			}
			return this;
		}
		try{
			for(AttributeResolver r : 
				annotatedResolverFactory.getAttributeResolvers(annotatedResolver)){
				withPolicyScopedAttributeResolver(policyId, r);
			}
			for(ContentResolver r : 
				annotatedResolverFactory.getContentResolvers(annotatedResolver)){
				withPolicyScopedContentResolver(policyId, r);
			}
			return this;
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public ResolverRegistryBuilder withPolicyScopedAttributeResolver(
			String policyId, AttributeResolver resolver){
		Preconditions.checkNotNull(resolver);
		if(policyId == null){
			withAttributeResolver(resolver);
			return this;
		}
		this.policyScopedAttributeResolvers.put(policyId, resolver);
		return this;
	}
	
	public ResolverRegistryBuilder withPolicyScopedAttributeResolvers(
			String policyId, Iterable<AttributeResolver> resolvers){
		for(AttributeResolver r : resolvers){
			withPolicyScopedAttributeResolver(policyId, r);
		}
		return this;
	}
	
	public ResolverRegistryBuilder withPolicyScopedContentResolver(
			String policyId, ContentResolver resolver){
		Preconditions.checkNotNull(resolver);
		if(policyId == null){
			withContentResolver(resolver);
			return this;
		}
		this.policyScopedContentResolvers.put(policyId, resolver);
		return this;
	}
	
	public ResolverRegistryBuilder withPolicyScopedContentResolvers(
			String policyId, Iterable<ContentResolver> resolvers){
		for(ContentResolver r : resolvers){
			withPolicyScopedContentResolver(policyId, r);
		}
		return this;
	}
	
	public ResolverRegistry build()
	{
		return build(new DefaultResolverRegistry());
	}
	
	public ResolverRegistry build(ResolverRegistry r)
	{
		if(r == null){
			r = new DefaultResolverRegistry();
		}
		for(AttributeResolver resolver : attributeResolvers){
			r.addAttributeResolver(resolver);
		}
		for(ContentResolver resolver : contentResolvers){
			r.addContentResolver(resolver);
		}
		for(Entry<String, AttributeResolver> e : policyScopedAttributeResolvers.entrySet()){
			r.addAttributeResolver(e.getKey(), e.getValue());
		}
		for(Entry<String, ContentResolver> e : policyScopedContentResolvers.entrySet()){
			r.addContentResolver(e.getKey(), e.getValue());
		}
		return r;
	}
}
