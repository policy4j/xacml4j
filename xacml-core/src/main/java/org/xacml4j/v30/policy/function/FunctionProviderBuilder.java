package org.xacml4j.v30.policy.function;

/*
 * #%L
 * Xacml4J PDP
 * %%
 * Copyright (C) 2009 - 2022 Xacml4J.org
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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.policy.function.impl.XacmlDefaultFunctions;

/**
 * A builder for function provider
 *
 * @author Giedrius Trumpickas
 */
public final class FunctionProviderBuilder {
	private final static Logger LOG = LoggerFactory.getLogger(FunctionProviderBuilder.class);


	private List<FunctionProvider> providers;
	private FunctionInvocationFactory invocationFactory;

	FunctionProviderBuilder(
			FunctionInvocationFactory invocationFactory) {
		this.providers = new LinkedList<>();
		this.invocationFactory = Objects.requireNonNull(
				invocationFactory, "invocationFactory");
	}

	FunctionProviderBuilder() {
		this(FunctionInvocationFactory.defaultFactory());
	}


	public FunctionProviderBuilder withDiscoveredFunctions() {
		return providers(ServiceLoader.load(FunctionProvider.class)
		                              .stream()
		                              .map(p -> p.get())
		                              .collect(Collectors.toList()));

	}

	/**
	 * Creates {@link FunctionProviderBuilder} defaultProvider with
	 * {@Link Builder#withStandardFunctions}
	 *
	 * @return {@link FunctionProviderBuilder} defaultProvider with standard XACML functions
	 */
	public static FunctionProviderBuilder builder(){
		return new FunctionProviderBuilder()
				.withDefaultFunctions();
	}

	public static FunctionProviderBuilder builder(FunctionInvocationFactory invocation){
		return new FunctionProviderBuilder(invocation)
				.withDefaultFunctions();
	}

	static FunctionProviderBuilder emptyBuilder(){
		return new FunctionProviderBuilder();
	}

	/**
	 * Discovers abvailable {@link FunctionProvider} implementations
	 * via {@link ServiceLoader#load(Class)}
	 *
	 * @return {@link FunctionProviderBuilder}
	 */
	public FunctionProviderBuilder withDefaultFunctions() {
		return provider(new XacmlDefaultFunctions(invocationFactory));
	}

	/**
	 * Discovers abvailable {@link FunctionProvider} implementations
	 * via {@link ServiceLoader#load(Class)} available via given
	 * {@link ClassLoader}
	 *
	 * @param classLoader a class loader
	 * @return {@link FunctionProviderBuilder}
	 */
	public FunctionProviderBuilder withDiscoveredFunctions(ClassLoader classLoader) {
		return providers(ServiceLoader.load(FunctionProvider.class, classLoader).stream()
		                              .map(p -> p.get())
		                              .collect(Collectors.toList()));
	}


	/**
	 * Adds function provider from a given annotated class
	 *
	 * @param clazz an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder fromClass(Class<?>... clazz) {
		Objects.requireNonNull(clazz, "clazz");
		try {
			return providers(AnnotationBasedFunctionProvider
					                 .toProviders(invocationFactory, clazz));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Adds function provider from a given annotated class
	 *
	 * @param insa an annotated function provider as instance
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder fromInstance(Object instance) {
		Objects.requireNonNull(instance, "instance");
		try {
			return providers(AnnotationBasedFunctionProvider
					                 .toProviders(invocationFactory, instance));
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Adds function providers from a given collection
	 *
	 * @param providers a collection of function providers
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder providers(Collection<FunctionProvider> providers) {
		Objects.requireNonNull(providers, "providers");
		if (LOG.isDebugEnabled()) {
			providers.forEach((p) -> LOG.debug("Adding FunctionProviderId=\"{}\" description=\"{}\"",
			                                   p.getId(), p.getDescription()));
		}
		this.providers.addAll(providers);
		return this;
	}

	/**
	 * Adds function provider from a given annotated class
	 *
	 * @param provider an annotated function provider
	 * @return {@link FunctionProviderBuilder} reference it itself
	 */
	public FunctionProviderBuilder provider(FunctionProvider... provider) {
		this.providers(Arrays.asList(provider));
		return this;
	}

	/**
	 * Builds {@Link FunctionProvider} defaultProvider with all functions
	 *
	 * @return {@link FunctionProvider} with all functions
	 */
	public FunctionProvider build() {
		return new AggregatingFunctionProvider(providers);
	}
}
