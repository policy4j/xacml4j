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
	
	public ResolverRegistryBuilder withAnnotatedResolvers(Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		if(annotatedResolver instanceof AttributeResolver){
			return withAttributeResolver((AttributeResolver)annotatedResolver);
		}
		if(annotatedResolver instanceof ContentResolver){
			return withContentResolver((ContentResolver)annotatedResolver);
		}
		try{
			for(AttributeResolver r : 
				annotatedResolverFactory.getAttributeResolvers(annotatedResolver)){
				withAttributeResolver(r);
			}
			for(ContentResolver r : 
				annotatedResolverFactory.getContentResolvers(annotatedResolver)){
				withContentResolver(r);
			}
			return this;
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public ResolverRegistryBuilder withPolicyScopedAnnotatedResolvers(
			String policyId, Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		Preconditions.checkNotNull(policyId);
		if(annotatedResolver instanceof AttributeResolver){
			return withPolicyScopedResolver(
					policyId, (AttributeResolver)annotatedResolver);
		}
		if(annotatedResolver instanceof ContentResolver){
			return withPolicyScopedResolver(
					policyId, (ContentResolver)annotatedResolver);
		}
		try{
			for(AttributeResolver r : 
				annotatedResolverFactory.getAttributeResolvers(annotatedResolver)){
				withPolicyScopedResolver(policyId, r);
			}
			for(ContentResolver r : 
				annotatedResolverFactory.getContentResolvers(annotatedResolver)){
				withPolicyScopedResolver(policyId, r);
			}
			return this;
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public ResolverRegistryBuilder withPolicyScopedResolver(
			String policyId, AttributeResolver resolver){
		Preconditions.checkNotNull(policyId);
		Preconditions.checkNotNull(resolver);
		this.policyScopedAttributeResolvers.put(policyId, resolver);
		return this;
	}
	
	public ResolverRegistryBuilder withPolicyScopedResolver(
			String policyId, ContentResolver resolver){
		Preconditions.checkNotNull(policyId);
		Preconditions.checkNotNull(resolver);
		this.policyScopedContentResolvers.put(policyId, resolver);
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
