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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.policy.function.impl.AggregatingFunctionProvider;
import org.xacml4j.v30.policy.function.impl.AnnotationBasedFunctionProvider;
import org.xacml4j.v30.policy.function.impl.RegExpFunctions;
import org.xacml4j.v30.policy.function.impl.XacmlDefaultFunctions;
import org.xacml4j.v30.policy.function.regex.RegexEngine;
import org.xacml4j.v30.policy.function.regex.RegexLimits;
import org.xacml4j.v30.policy.function.regex.RegexProfile;

/**
 * A builder for function provider
 *
 * @author Giedrius Trumpickas
 */
public final class FunctionProviderBuilder {
	private final static Logger LOG = LoggerFactory.getLogger(FunctionProviderBuilder.class);


	private List<FunctionProvider> providers;
	private FunctionInvocationFactory invocationFactory;
	private int defaultPosition = -1;
	/** The regexp functions of the standard functions, when an engine was selected (design D3 of add-regex-engine-spi) */
	private Optional<Supplier<RegExpFunctions.Matching>> regexp = Optional.empty();

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
		return discoveredProviders(ServiceLoader.load(FunctionProvider.class)
		                                    .stream()
		                                    .filter(p -> defaultPosition < 0 && regexp.isEmpty()
				                                    || !XacmlDefaultFunctions.class.isAssignableFrom(p.type()))
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
	 * Adds the standard XACML functions: a discovered {@link XacmlDefaultFunctions} already added,
	 * or a new one, with the regexp functions of the engine selected by {@link #withRegexEngine}
	 * when one was
	 *
	 * @return {@link FunctionProviderBuilder}
	 */
	public FunctionProviderBuilder withDefaultFunctions() {
		if (defaultPosition < 0) {
			defaultPosition = providers.size();
		}
		return this;
	}

	/**
	 * Selects the regular-expression engine and profile of the standard regexp-match functions by
	 * name (change add-regex-engine-spi, design D3): the standard functions this builder adds, or
	 * has added, evaluate the six {@code *-regexp-match} functions with that engine, with the
	 * limits of the system properties of {@link RegExpFunctions}. Without a selection they use the
	 * engine and profile of the properties {@link RegExpFunctions#ENGINE_PROPERTY} and
	 * {@link RegExpFunctions#PROFILE_PROPERTY}, or {@code jdk} with {@code legacy}.
	 *
	 * @param engine the name of an engine on the class path, such as {@code jdk}
	 * @param profile the name of a profile the engine offers, {@code legacy} or {@code xf}
	 * @return {@link FunctionProviderBuilder}
	 * @throws IllegalArgumentException when no engine or profile has the name, or the engine does
	 * not offer the profile
	 * @throws IllegalStateException when more than one engine provider has the name
	 */
	public FunctionProviderBuilder withRegexEngine(String engine, String profile) {
		return withRegexFunctions(() -> RegExpFunctions.matching(engine, profile));
	}

	/**
	 * Selects the regular-expression engine and profile of the standard regexp-match functions, with
	 * the limits of the system properties of {@link RegExpFunctions} (design D3)
	 *
	 * @param engine an engine
	 * @param profile a profile the engine offers
	 * @return {@link FunctionProviderBuilder}
	 * @throws IllegalArgumentException when the engine does not offer the profile
	 */
	public FunctionProviderBuilder withRegexEngine(RegexEngine engine, RegexProfile profile) {
		return withRegexFunctions(() -> RegExpFunctions.matching(engine, profile));
	}

	/**
	 * Selects the regular-expression engine, profile and limits of the standard regexp-match
	 * functions (design D3); the limits also size the cache of compiled patterns (design D6)
	 *
	 * @param engine an engine
	 * @param profile a profile the engine offers
	 * @param limits the limits of the patterns and of their matches
	 * @return {@link FunctionProviderBuilder}
	 * @throws IllegalArgumentException when the engine does not offer the profile
	 */
	public FunctionProviderBuilder withRegexEngine(RegexEngine engine, RegexProfile profile, RegexLimits limits) {
		return withRegexFunctions(() -> new RegExpFunctions.Matching(engine, profile, limits));
	}

	/** Standard functions added before are built again with the regexp functions */
	private FunctionProviderBuilder withRegexFunctions(Supplier<RegExpFunctions.Matching> regexp) {
		this.regexp = Optional.of(regexp);
		int existingPosition = -1;
		for (int i = 0; i < providers.size(); i++) {
			if (providers.get(i) instanceof XacmlDefaultFunctions) {
				existingPosition = i;
				break;
			}
		}
		boolean hadDefaultFunctions = providers.removeIf(XacmlDefaultFunctions.class::isInstance);
		if (hadDefaultFunctions && defaultPosition < 0) defaultPosition = existingPosition;
		return this;
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
		return discoveredProviders(ServiceLoader.load(FunctionProvider.class, classLoader).stream()
		                                    .filter(p -> defaultPosition < 0 && regexp.isEmpty()
				                                    || !XacmlDefaultFunctions.class.isAssignableFrom(p.type()))
		                                    .map(p -> p.get())
		                                    .collect(Collectors.toList()));
	}

	private FunctionProviderBuilder discoveredProviders(Collection<FunctionProvider> discovered) {
		for (FunctionProvider provider : discovered) {
			// The standard functions of this builder, or those with the selected regex engine, take precedence
			if (provider instanceof XacmlDefaultFunctions && (defaultPosition >= 0 || regexp.isPresent())) {
				continue;
			}
			provider(provider);
		}
		return this;
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
		for (FunctionProvider provider : providers) {
			if (this.providers.stream().noneMatch(existing -> existing == provider)) {
				this.providers.add(provider);
			}
		}
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
		List<FunctionProvider> selected = new ArrayList<>(providers);
		if (defaultPosition < 0) {
			regexp.ifPresent(Supplier::get);
		}
		if (defaultPosition >= 0) {
			FunctionProvider standard = regexp.<FunctionProvider>map(r -> new XacmlDefaultFunctions(invocationFactory, r.get()))
					.or(() -> selected.stream().filter(XacmlDefaultFunctions.class::isInstance).findFirst())
					.orElseGet(() -> new XacmlDefaultFunctions(invocationFactory));
			if (selected.stream().noneMatch(p -> p == standard)) {
				selected.add(Math.min(defaultPosition, selected.size()), standard);
			}
		}
		return new AggregatingFunctionProvider(selected);
	}
}
