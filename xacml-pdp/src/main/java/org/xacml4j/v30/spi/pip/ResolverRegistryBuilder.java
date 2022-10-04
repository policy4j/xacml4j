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

import static org.xacml4j.v30.types.XacmlTypes.DATE;
import static org.xacml4j.v30.types.XacmlTypes.DATETIME;
import static org.xacml4j.v30.types.XacmlTypes.TIME;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.SyntaxException;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

public final class ResolverRegistryBuilder
{
	private final static String CURRENT_TIME = "urn:oasis:names:tc:xacml:1.0:environment:current-time";
	private final static String CURRENT_DATE = "urn:oasis:names:tc:xacml:1.0:environment:current-date";
	private final static String CURRENT_DATETIME = "urn:oasis:names:tc:xacml:1.0:environment:current-dateTime";

	private final static String SHORT_CURRENT_TIME = "current-time";
	private final static String SHORT_CURRENT_DATE = "current-date";
	private final static String SHORT_CURRENT_DATETIME = "current-dateTime";

	private Collection<AttributeResolverDescriptor> attributeResolvers;
	private Collection<ContentResolverDescriptor> contentResolvers;
	private AnnotatedResolverFactory annotatedResolverFactory;

	private final static TypeToken<Resolver<AttributeSet>> ATTRIBUTE_RESOLVER_TYPE = new TypeToken<>(){};
	private final static TypeToken<Resolver<ContentRef>> CONTENT_RESOLVER_TYPE = new TypeToken<>(){};

	private final static AttributeResolverDescriptor ENVIRONMENT_RESOLVER = AttributeResolverDescriptor
			.builder("XacmlEnvironmentResolver",
					"XACML Environment Attributes Resolver", CategoryId.ENVIRONMENT)
			.noCache()
			.attribute(CURRENT_TIME, TIME, SHORT_CURRENT_TIME)
			.attribute(CURRENT_DATE, DATE, SHORT_CURRENT_DATE)
			.attribute(CURRENT_DATETIME,DATETIME, SHORT_CURRENT_DATETIME)
			.build((context)->{
						ZonedDateTime currentDateTime = context.getCurrentDateTime();
						Map<String, BagOfValues> v = new HashMap<String, BagOfValues>();
						v.put("urn:oasis:names:tc:xacml:1.0:environment:current-time",
								TIME.of(currentDateTime).toBag());
						v.put("urn:oasis:names:tc:xacml:1.0:environment:current-date",
								DATE.of(currentDateTime).toBag());
						v.put("urn:oasis:names:tc:xacml:1.0:environment:current-dateTime",
								DATETIME.of(currentDateTime).toBag());
						return v;
			});


	private ResolverRegistryBuilder(){
		this.attributeResolvers = new LinkedList<>();
		this.contentResolvers = new LinkedList<>();
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
		return withAttributeResolver(ENVIRONMENT_RESOLVER);
	}

	/**
	 * Adds a given {@link AttributeResolverDescriptor}
	 *
	 * @param resolver an attribute resolver
	 * @return {@link ResolverRegistryBuilder}
	 */
	public ResolverRegistryBuilder withAttributeResolver(AttributeResolverDescriptor resolver){
		Preconditions.checkNotNull(resolver);
		this.attributeResolvers.add(resolver);
		return this;
	}

	public ResolverRegistryBuilder withAttributeResolvers(Iterable<AttributeResolverDescriptor> resolvers){
		for(AttributeResolverDescriptor r : resolvers){
			withAttributeResolver(r);
		}
		return this;
	}

	public ResolverRegistryBuilder withContentResolvers(Iterable<ContentResolverDescriptor> resolvers){
		for(ContentResolverDescriptor r : resolvers){
			withContentResolver(r);
		}
		return this;
	}

	public ResolverRegistryBuilder withContentResolver(ContentResolverDescriptor resolver){
		Preconditions.checkNotNull(resolver);
		this.contentResolvers.add(resolver);
		return this;
	}

	public ResolverRegistryBuilder withResolver(Object annotatedResolver){
		Preconditions.checkNotNull(annotatedResolver);
		try
		{
			withAttributeResolvers(annotatedResolverFactory.getAttributeResolvers(annotatedResolver));
			withContentResolvers(annotatedResolverFactory.getContentResolvers(annotatedResolver));
			return this;
		}catch(SyntaxException e){
			throw new IllegalArgumentException(e);
		}
	}

	public ResolverRegistry build(){
		return new DefaultResolverRegistry(attributeResolvers, contentResolvers);
	}
}
