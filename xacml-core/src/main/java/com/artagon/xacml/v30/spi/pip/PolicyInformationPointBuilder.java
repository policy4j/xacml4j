package com.artagon.xacml.v30.spi.pip;

import com.google.common.base.Preconditions;

public final class PolicyInformationPointBuilder 
{
	private PolicyInformationPointCacheProvider cache;
	private ResolverRegistryBuilder registryBuilder;
	private ResolverRegistry registry;
	
	public PolicyInformationPointBuilder(){
		this(null);
	}
	
	public PolicyInformationPointBuilder(ResolverRegistry registry){
		this.registry = registry;
		this.cache = new NoCachePolicyInformationPointCacheProvider();
		this.registryBuilder = ResolverRegistryBuilder.builder();
	}
	
	public static PolicyInformationPointBuilder builder(){
		return new PolicyInformationPointBuilder();
	}
	
	public static PolicyInformationPointBuilder builder(
			ResolverRegistry registry){
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
		registryBuilder.withAttributeResolver(resolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withResolver(ContentResolver resolver){
		Preconditions.checkNotNull(resolver);
		registryBuilder.withContentResolver(resolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withAnnotatedResolvers(Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		registryBuilder.withAnnotatedResolvers(annotatedResolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withPolicyScopedAnnotatedResolvers(
			String policyId, Object annotatedResolver){
		registryBuilder.withPolicyScopedAnnotatedResolvers(policyId, annotatedResolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withPolicyScopedResolver(
			String policyId, AttributeResolver resolver){
		registryBuilder.withPolicyScopedResolver(policyId, resolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withPolicyScopedResolver(
			String policyId, ContentResolver resolver){
		Preconditions.checkNotNull(policyId);
		Preconditions.checkNotNull(resolver);
		registryBuilder.withPolicyScopedResolver(policyId, resolver);
		return this;
	}
	
	public PolicyInformationPoint build(){
		return new DefaultPolicyInformationPoint(
				registryBuilder.build(registry), cache);
	}
}
