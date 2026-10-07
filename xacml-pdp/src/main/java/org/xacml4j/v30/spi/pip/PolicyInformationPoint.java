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


import java.util.Objects;
import java.util.Optional;

import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;

import com.google.common.base.Preconditions;

/**
 * A XACML Policy Information Point
 *
 * @author Giedrius Trumpickas
 */
public interface PolicyInformationPoint
{
	/**
	 * Gets identifier for this policy
	 * information point
	 *
	 * @return a unique identifier
	 */
	String getId();

	/**
	 * Resolves a given {@link AttributeDesignatorKey}
	 * <p>
	 * An empty result means "no value". Every exception is a resolution
	 * failure: the engine reports it as status processing-error with a fixed
	 * message, whatever status and message the exception carries.
	 *
	 * @param context an evaluation context
	 * @param ref an attribute designator
	 * @return {@link BagOfValues}
	 * @throws RuntimeException if an error occurs
	 */
	Optional<BagOfValues> resolve(
			EvaluationContext context,
			AttributeDesignatorKey ref);

	static Builder<?> builder(String id){
		return DefaultPolicyInformationPoint.builder(id);
	}

	/**
	 * Resolves a given {@link AttributeSelectorKey} on content that this
	 * information point supplies
	 * <p>
	 * An empty result means "no content" or "no selected node". Every
	 * exception is a resolution failure: the engine reports it as status
	 * processing-error with a fixed message, whatever status and message
	 * the exception carries.
	 *
	 * @param context an evaluation context
	 * @param selectorKey content selector
	 * @return {@link BagOfValues}
	 * @throws RuntimeException if an error occurs
	 */
	Optional<BagOfValues> resolve(
			EvaluationContext context,
			AttributeSelectorKey selectorKey);

	abstract class Builder<T extends Builder<?>>
	{

		String id;
		PolicyInformationPointCacheProvider cache = new PolicyInformationPointCacheProvider.NoCachePolicyInformationPointCacheProvider();
		ResolverRegistry registry = ResolverRegistry.builder()
		                                            .withDefaultResolvers()
		                                            .build();

		public Builder(String id){

			this.id = Objects.requireNonNull(id, "id");
		}

		public abstract T getThis();

		public T cacheProvider(
				PolicyInformationPointCacheProvider cache){
			this.cache = Preconditions.checkNotNull(cache, "cache");;
			return getThis();
		}

		public T noCacheProvider(){
			this.cache = new PolicyInformationPointCacheProvider.NoCachePolicyInformationPointCacheProvider();
			return getThis();
		}

		public T registry(
				ResolverRegistry registry){
			this.registry = Objects.requireNonNull(registry, "registry");
			return getThis();
		}

		public T withDefaultRegistry(){
			this.registry = ResolverRegistry.builder()
			                                .withDefaultResolvers()
			                                .build();
			return getThis();
		}


		public PolicyInformationPoint build(){
			return new DefaultPolicyInformationPoint(this);
		}
	}
}
