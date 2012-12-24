package org.xacml4j.v30.spi.pip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public final class PolicyInformationPointBuilder 
{
	private final static Logger log = LoggerFactory.getLogger(
			PolicyInformationPointBuilder.class);
	
	private String id;
	private PolicyInformationPointCacheProvider cache;
	private ResolverRegistryBuilder registryBuilder;
	
	public PolicyInformationPointBuilder(String id){
		Preconditions.checkNotNull(id);
		this.id = id;
		this.cache = new NoCachePolicyInformationPointCacheProvider();
		this.registryBuilder = ResolverRegistryBuilder.builder();
	}
	
	public static PolicyInformationPointBuilder builder(String id){
		return new PolicyInformationPointBuilder(id);
	}
	
	public PolicyInformationPointBuilder withCacheProvider(
			PolicyInformationPointCacheProvider cache){
		Preconditions.checkNotNull(cache);
		this.cache = cache;
		return this;
	}
	
	/**
	 * Adds default XACML 3.0 resolvers to this builder
	 * 
	 * @return {@link PolicyInformationPointBuilder}
	 */
	public PolicyInformationPointBuilder withDefaultResolvers(){
		return withResolver(new DefaultEnviromentAttributeResolver());
	}
	
	/**
	 * Adds root resolver to this builder
	 * 
	 * @param resolver a resolver
	 * @return {@link PolicyInformationPointBuilder}
	 */
	public PolicyInformationPointBuilder withResolver(AttributeResolver resolver){
		registryBuilder.withAttributeResolver(resolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withResolver(ContentResolver resolver){
		Preconditions.checkNotNull(resolver);
		registryBuilder.withContentResolver(resolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withResolver(Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		registryBuilder.withResolver(annotatedResolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withPolicyScopedResolver(
			String policyId, Object annotatedResolver){
		registryBuilder.withPolicyScopedResolver(policyId, annotatedResolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withPolicyScopedResolver(
			String policyId, AttributeResolver resolver){
		registryBuilder.withPolicyScopedAttributeResolver(policyId, resolver);
		return this;
	}
	
	public PolicyInformationPointBuilder withPolicyScopedResolver(
			String policyId, ContentResolver resolver){
		Preconditions.checkNotNull(policyId);
		Preconditions.checkNotNull(resolver);
		registryBuilder.withPolicyScopedContentResolver(policyId, resolver);
		return this;
	}
	
	public PolicyInformationPoint build(){
		return new DefaultPolicyInformationPoint(id,
				registryBuilder.build(), cache);
	}
	
	public PolicyInformationPoint build(ResolverRegistry registry){
		if(log.isDebugEnabled()){
			log.debug("Creating PIP id=\"{}\"", id);
		}
		return new DefaultPolicyInformationPoint(id,
				registryBuilder.build(registry), cache);
	}
}
