package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		this.cache = new DefaultPolicyInformationPointCacheProvider();
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
	public PolicyInformationPointBuilder defaultResolvers(){
		return resolver(new DefaultEnvironmentAttributeResolver());
	}

	/**
	 * Adds root resolver to this builder
	 *
	 * @param resolver a resolver
	 * @return {@link PolicyInformationPointBuilder}
	 */
	public PolicyInformationPointBuilder resolver(AttributeResolver resolver){
		registryBuilder.withAttributeResolver(resolver);
		return this;
	}

	public PolicyInformationPointBuilder resolver(ContentResolver resolver){
		Preconditions.checkNotNull(resolver);
		registryBuilder.withContentResolver(resolver);
		return this;
	}

	public PolicyInformationPointBuilder resolverFromInstance(Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		registryBuilder.withResolver(annotatedResolver);
		return this;
	}

	public PolicyInformationPointBuilder policyScopedResolverFromInstance(
			String policyId, Object annotatedResolver){
		registryBuilder.withPolicyScopedResolver(policyId, annotatedResolver);
		return this;
	}

	public PolicyInformationPointBuilder policyScopedResolver(
			String policyId, AttributeResolver resolver){
		registryBuilder.withPolicyScopedAttributeResolver(policyId, resolver);
		return this;
	}

	public PolicyInformationPointBuilder policyScopedResolver(
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
			log.debug("Creating PIP attributeId=\"{}\"", id);
		}
		return new DefaultPolicyInformationPoint(id,
				registryBuilder.build(registry), cache);
	}
}
