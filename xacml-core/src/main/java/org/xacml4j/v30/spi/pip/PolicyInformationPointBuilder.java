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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import org.xacml4j.v30.BagOfAttributeValues;
import org.xacml4j.v30.CategoryId;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.xacml4j.v30.types.XacmlTypes.*;
import static org.xacml4j.v30.types.XacmlTypes.DATETIME;

public final class PolicyInformationPointBuilder
{
	private final static String CURRENT_TIME = "urn:oasis:names:tc:xacml:1.0:environment:current-time";
	private final static String CURRENT_DATE = "urn:oasis:names:tc:xacml:1.0:environment:current-date";
	private final static String CURRENT_DATETIME = "urn:oasis:names:tc:xacml:1.0:environment:current-dateTime";

	private final static String SHORT_CURRENT_TIME = "current-time";
	private final static String SHORT_CURRENT_DATE = "current-date";
	private final static String SHORT_CURRENT_DATETIME = "current-dateTime";

	private final static Resolver<AttributeSet> ENV_RESOLVER =
			AttributeResolverDescriptor.of(AttributeResolverDescriptorBuilder.builder("XacmlEnvironmentResolver",
					"XACML Environment Attributes Resolver",
					CategoryId.ENVIRONMENT).noCache()
					.attribute(CURRENT_TIME, TIME, SHORT_CURRENT_TIME)
					.attribute(CURRENT_DATE, DATE, SHORT_CURRENT_DATE)
					.attribute(CURRENT_DATETIME,DATETIME, SHORT_CURRENT_DATETIME)
					.build(), resolverContext -> {
				Calendar currentDateTime = GregorianCalendar.from(resolverContext.getCurrentDateTime());
				Map<String, BagOfAttributeValues> v = new HashMap<String, BagOfAttributeValues>();
				v.put(CURRENT_TIME, TIME.of(currentDateTime).toBag());
				v.put(CURRENT_DATE, DATE.of(currentDateTime).toBag());
				v.put(CURRENT_DATETIME, DATETIME.of(currentDateTime).toBag());
				return v;});


	private final static Logger log = LoggerFactory.getLogger(
			PolicyInformationPointBuilder.class);

	private String id;
	private PolicyInformationPointCacheProvider cache;
	private ResolverRegistryBuilder registryBuilder;
	private PolicyInformationPoint.ResolutionStrategy contentStrategy = PolicyInformationPoint.ResolutionStrategy.FIRST_NON_EMPTY_IGNORE_ERROR;
	private PolicyInformationPoint.ResolutionStrategy attributeStrategy = PolicyInformationPoint.ResolutionStrategy.FIRST_NON_EMPTY_IGNORE_ERROR;

	public PolicyInformationPointBuilder(String id){
		Preconditions.checkNotNull(id);
		this.id = id;
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
		return resolver(ENV_RESOLVER);
	}

	/**
	 * Adds root resolver to this builder
	 *
	 * @param resolver a resolver
	 * @return {@link PolicyInformationPointBuilder}
	 */
	public PolicyInformationPointBuilder resolver(Resolver<?> resolver){
		if(CON)
		registryBuilder.withAttributeResolver(resolver);
		return this;
	}


	public PolicyInformationPointBuilder resolverFromInstance(Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		registryBuilder.withResolver(annotatedResolver);
		return this;
	}

	public PolicyInformationPoint build(){
		return new DefaultPolicyInformationPoint(id,
				registryBuilder.build(),
				contentStrategy,
				attributeStrategy,
				cache);
	}
}
