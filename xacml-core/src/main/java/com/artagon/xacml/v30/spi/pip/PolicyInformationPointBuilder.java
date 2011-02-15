package com.artagon.xacml.v30.spi.pip;

import com.artagon.xacml.v30.XacmlSyntaxException;
import com.google.common.base.Preconditions;

public final class PolicyInformationPointBuilder 
{
	private PolicyInformationPointCacheProvider cache;
	private ResolverRegistry registry;
	private AnnotatedResolverFactory annotatedResolverFactory;
	
	public PolicyInformationPointBuilder(){
		this(new DefaultResolverRegistry());
	}
	
	public PolicyInformationPointBuilder(ResolverRegistry registry){
		Preconditions.checkNotNull(registry);
		this.cache = new NoCachePolicyInformationPointCacheProvider();
		this.registry = registry;
		this.annotatedResolverFactory = new AnnotatedResolverFactory();
	}
	
	public static PolicyInformationPointBuilder builder(){
		return new PolicyInformationPointBuilder();
	}
	
	public static PolicyInformationPointBuilder builder(ResolverRegistry registry){
		return new PolicyInformationPointBuilder(registry);
	}
	
	public PolicyInformationPointBuilder withCache(PolicyInformationPointCacheProvider cache){
		Preconditions.checkNotNull(cache);
		this.cache = cache;
		return this;
	}
	
	public PolicyInformationPointBuilder withDefaultResolvers(){
		return withResolver(new DefaultEnviromentAttributeResolver());
	}
	
	public PolicyInformationPointBuilder withResolver(AttributeResolver resolver){
		Preconditions.checkNotNull(resolver);
		this.registry.addAttributeResolver(resolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withAnnotatedResolvers(Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		try{
			for(AttributeResolver r : 
				annotatedResolverFactory.getAttributeResolvers(annotatedResolver)){
				registry.addAttributeResolver(r);
			}
			for(ContentResolver r : 
				annotatedResolverFactory.getContentResolvers(annotatedResolver)){
				registry.addContentResolver(r);
			}
			return this;
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public PolicyInformationPointBuilder withPolicyScopedAnnotatedResolvers(
			String policyId, Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		Preconditions.checkNotNull(policyId);
		try{
			for(AttributeResolver r : 
				annotatedResolverFactory.getAttributeResolvers(annotatedResolver)){
				registry.addAttributeResolver(policyId, r);
			}
			for(ContentResolver r : 
				annotatedResolverFactory.getContentResolvers(annotatedResolver)){
				registry.addContentResolver(policyId, r);
			}
			return this;
		}catch(XacmlSyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}
	
	public PolicyInformationPointBuilder withPolicyScopedResolver(
			String policyId, AttributeResolver resolver){
		Preconditions.checkNotNull(policyId);
		Preconditions.checkNotNull(resolver);
		this.registry.addAttributeResolver(policyId, resolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withPolicyScopedResolver(
			String policyId, ContentResolver resolver){
		Preconditions.checkNotNull(policyId);
		Preconditions.checkNotNull(resolver);
		this.registry.addContentResolver(policyId, resolver);
		return this;
	}
	
	public PolicyInformationPoint build(){
		return new DefaultPolicyInformationPoint(registry, cache);
	}
}
