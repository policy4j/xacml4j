package org.xacml4j.v30.spi.function;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.policy.function.XacmlDefaultFunctions;

/**
 * A builder for function provider
 *
 * @author Giedrius Trumpickas
 */
public final class FunctionProviderBuilder {
	private final static Logger LOG = LoggerFactory.getLogger(FunctionProviderBuilder.class);

	private static AtomicReference<FunctionProvider> STANDARD_FUNCTIONS;

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
				.withStandardFunctions();
	}

	public static FunctionProviderBuilder builder(FunctionInvocationFactory invocation){
		return new FunctionProviderBuilder(invocation)
				.withStandardFunctions();
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
	public FunctionProviderBuilder withStandardFunctions() {
		if (STANDARD_FUNCTIONS == null) {
			STANDARD_FUNCTIONS = new AtomicReference<>(new XacmlDefaultFunctions(invocationFactory));
			provider(STANDARD_FUNCTIONS.get());
		}
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
