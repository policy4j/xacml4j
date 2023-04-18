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

import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.WellKnownAttributeIds;

import com.google.common.base.Preconditions;


public interface ResolverRegistry
{
    /**
     * Gets a matching {@link ResolverDescriptor} for a given
     * evaluation context and given {@link AttributeDesignatorKey}
     *
     * @param context an evaluation context
     * @param key an attribute designator key
     * @return iterable over found matching {@link ResolverDescriptor>} instances
     */
	Collection<AttributeResolverDescriptor> getMatchingAttributeResolver(
			EvaluationContext context, AttributeDesignatorKey key);

	/**
	 * Gets a matching {@link ResolverDescriptor} for a given
	 * evaluation context and given {@link AttributeSelectorKey}
	 *
	 * @param context an evaluation context
	 * @param key an attribute designator key
	 * @return iterable over found matching {@link ResolverDescriptor} instances
	 */
	Collection<ContentResolverDescriptor> getMatchingContentResolver(
			EvaluationContext context, AttributeSelectorKey selectorKey);


	/**
	 * Adds resolver to this registry
	 *
	 * @param r a resolver
	 */
	void addResolver(AttributeResolverDescriptor r);

	/**
	 * Adds resolver to this registry
	 *
	 * @param r a resolver
	 */
	void addResolver(ContentResolverDescriptor r);

	static Builder builder(){
		return new Builder() {
			@Override
			public Builder<?> getThis() {
				return this;
			}
			public ResolverRegistry build(){
				return new DefaultResolverRegistry(attributeResolvers, contentResolvers);
			}
		};
	}

	abstract class Builder<T extends Builder<?>>
	{
		Collection<AttributeResolverDescriptor> attributeResolvers;
		Collection<ContentResolverDescriptor> contentResolvers;
		AnnotatedResolverFactory annotatedResolverFactory;


		private final static AttributeResolverDescriptor ENVIRONMENT_RESOLVER = AttributeResolverDescriptor
				.builder("XacmlEnvironmentResolver",
				         "XACML Environment Attributes Resolver", CategoryId.ENVIRONMENT)
				.noCache()
				.attribute(WellKnownAttributeIds.CURRENT_TIME.getId(), TIME, WellKnownAttributeIds.CURRENT_TIME.getAbbreviatedId())
				.attribute(WellKnownAttributeIds.CURRENT_DATE.getId(), DATE, WellKnownAttributeIds.CURRENT_DATE.getAbbreviatedId())
				.attribute(WellKnownAttributeIds.CURRENT_DATETIME.getId(),DATETIME, WellKnownAttributeIds.CURRENT_DATETIME.getAbbreviatedId())
				.build((context)->{
					ZonedDateTime currentDateTime = context.getCurrentDateTime();
					Map<String, BagOfValues> v = new HashMap<String, BagOfValues>();
					v.put(WellKnownAttributeIds.CURRENT_TIME.getId(),
					      TIME.of(currentDateTime).toBag());
					v.put(WellKnownAttributeIds.CURRENT_DATE.getId(),
					      DATE.of(currentDateTime).toBag());
					v.put(WellKnownAttributeIds.CURRENT_DATETIME.getId(),
					      DATETIME.of(currentDateTime).toBag());
					return v;
				});


		protected Builder(){
			this.attributeResolvers = new LinkedList<>();
			this.contentResolvers = new LinkedList<>();
			this.annotatedResolverFactory = new AnnotatedResolverFactory();
		}

		public abstract T getThis();
		public abstract ResolverRegistry build();

		/**
		 * Adds default XACML 3.0 resolvers
		 *
		 * @return {@link T}
		 */
		public T withDefaultResolvers(){
			return withAttributeResolver(ENVIRONMENT_RESOLVER);
		}

		/**
		 * Adds a given {@link AttributeResolverDescriptor}
		 *
		 * @param resolver an attribute resolver
		 * @return {@link T}
		 */
		public T withAttributeResolver(AttributeResolverDescriptor resolver){
			Preconditions.checkNotNull(resolver);
			this.attributeResolvers.add(resolver);
			return getThis();
		}

		public T withAttributeResolvers(Iterable<AttributeResolverDescriptor> resolvers){
			for(AttributeResolverDescriptor r : resolvers){
				withAttributeResolver(r);
			}
			return getThis();
		}

		public T withContentResolvers(Iterable<ContentResolverDescriptor> resolvers){
			for(ContentResolverDescriptor r : resolvers){
				withContentResolver(r);
			}
			return getThis();
		}

		public T withContentResolver(ContentResolverDescriptor resolver){
			Preconditions.checkNotNull(resolver);
			this.contentResolvers.add(resolver);
			return getThis();
		}

		public T withResolver(Object annotatedResolver){
			Preconditions.checkNotNull(annotatedResolver);
			try
			{
				withAttributeResolvers(annotatedResolverFactory.getAttributeResolvers(annotatedResolver));
				withContentResolvers(annotatedResolverFactory.getContentResolvers(annotatedResolver));
				return getThis();
			}catch(SyntaxException e){
				throw new IllegalArgumentException(e);
			}
		}
	}

}
