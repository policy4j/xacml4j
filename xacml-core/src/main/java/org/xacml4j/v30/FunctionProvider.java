package org.xacml4j.v30;

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
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.policy.function.XacmlDefaultFunctions;
import org.xacml4j.v30.spi.function.AggregatingFunctionProvider;
import org.xacml4j.v30.spi.function.AnnotationBasedFunctionProvider;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provider interface for XACML functions
 *
 * @author Giedrius Trumpickas
 */
public interface FunctionProvider
{
	/**
	 * Gets function provider id
	 *
	 * @return this function provider id
	 */
	default String getId(){
		return getClass().getName();
	}

	/**
	 * Gets function provider description
	 *
	 * @return this function provider descriptor
	 */
	default String getDescription() {
		return getClass().getSimpleName();
	}

	/**
	 * Gets function defaultProvider for a given function
	 * identifier.
	 *
	 * @param functionId a function identifier
	 * @return {@link FunctionSpec} defaultProvider for a given
	 * identifier or {@code null} if function
	 * can not be found for a given identifier
	 */
	Optional<FunctionSpec> getFunction(String functionId);


	/**
	 * Gets all supported function by this factory
	 *
	 * @return {@link Iterable} over all supported
	 * function identifiers
	 */
	Collection<FunctionSpec> getProvidedFunctions();

	/**
	 * Creates {@link Builder} defaultProvider with
	 * {@Link Builder#withStandardFunctions}
	 *
	 * @return {@link Builder} defaultProvider with standard XACML functions
	 */
	static Builder builder(){
		return new Builder()
				.withStandardFunctions();
	}

	static Builder builder(FunctionInvocationFactory invocation){
		return new Builder(invocation)
				.withStandardFunctions();
	}

	static Builder emptyBuilder(){
		return new Builder();
	}

	/**
	 * A builder for function provider
	 *
	 * @author Giedrius Trumpickas
	 */
	final class Builder
	{
		private final static Logger LOG = LoggerFactory.getLogger(Builder.class);

		private static FunctionProvider STANDARD_FUNCTIONS;

		private List<FunctionProvider> providers;
		private FunctionInvocationFactory invocationFactory;

		private Builder(
				FunctionInvocationFactory invocationFactory){
			this.providers = new LinkedList<>();
			this.invocationFactory = Objects.requireNonNull(
					invocationFactory, "invocationFactory");
		}

		private Builder(){
			this(FunctionInvocationFactory.defaultFactory());
		}


		public Builder withDiscoveredFunctions(){
			return providers(ServiceLoader.load(FunctionProvider.class)
					.stream()
					.map(p->p.get())
					.collect(Collectors.toList()));

		}

		/**
		 * Discovers abvailable {@link FunctionProvider} implementations
		 * via {@link ServiceLoader#load(Class)}
		 *
		 * @return {@link Builder}
		 */
		public Builder withStandardFunctions(){
			if(STANDARD_FUNCTIONS  == null){
				STANDARD_FUNCTIONS  = new XacmlDefaultFunctions(invocationFactory);
				provider(STANDARD_FUNCTIONS);
			}
			return this;
		}

		/**
		 * Discovers abvailable {@link FunctionProvider} implementations
		 * via {@link ServiceLoader#load(Class)} available via given
		 * {@link ClassLoader}
		 *
		 * @param classLoader a class loader
		 * @return {@link Builder}
		 */
		public Builder withDiscoveredFunctions(ClassLoader classLoader){
			return providers(ServiceLoader.load(FunctionProvider.class, classLoader).stream()
					.map(p->p.get())
					.collect(Collectors.toList()));
		}


		/**
		 * Adds function provider from a given annotated class
		 *
		 * @param clazz an annotated function provider
		 * @return {@link Builder} reference it itself
		 */
		public Builder fromClass(Class<?> ... clazz)
		{
			Objects.requireNonNull(clazz, "clazz");
			try{
				return providers(AnnotationBasedFunctionProvider
						.toProviders(invocationFactory, clazz));
			}catch(Exception e){
				LOG.error(e.getMessage(), e);
				throw new IllegalArgumentException(e);
			}
		}

		/**
		 * Adds function providers from a given collection
		 *
		 * @param providers a collection of function providers
		 * @return {@link Builder} reference it itself
		 */
		public Builder providers(Collection<FunctionProvider> providers){
			Objects.requireNonNull(providers, "providers");
			if(LOG.isDebugEnabled()){
				providers.forEach((p)->LOG.debug("Adding FunctionProviderId=\"()\"", p.getId()));
			}
			this.providers.addAll(providers);
			return this;
		}

		/**
		 * Adds function provider from a given annotated class
		 *
		 * @param provider an annotated function provider
		 * @return {@link Builder} reference it itself
		 */
		public Builder provider(FunctionProvider ... provider){
			this.providers(Arrays.asList(provider));
			return this;
		}

		/**
		 * Builds {@Link FunctionProvider} defaultProvider with all functions
		 *
		 * @return {@link FunctionProvider} with all functions
		 */
		public FunctionProvider build(){
			return new AggregatingFunctionProvider(providers);
		}
	}
}
