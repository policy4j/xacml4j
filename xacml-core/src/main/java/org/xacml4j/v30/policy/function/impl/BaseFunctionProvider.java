package org.xacml4j.v30.policy.function.impl;

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
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Base class for function providers
 *
 * @author Giedrius Trumpickas
 */
public class BaseFunctionProvider implements FunctionProvider
{
	private final static Logger LOG = LoggerFactory.getLogger(BaseFunctionProvider.class);

	private Map<String, FunctionSpec> functionsById;
	private Map<String, FunctionSpec> functionsByLegacyId;
	private Map<String, FunctionSpec> functionsByShortId;

	protected BaseFunctionProvider(Collection<FunctionSpec> functions,
								   Supplier<Map<String, FunctionSpec>> mapSupplier) {
		Map<String, FunctionSpec> identifiers = mapSupplier.get();
		Map<String, FunctionSpec> byId = mapSupplier.get();
		Map<String, FunctionSpec> byLegacyId = mapSupplier.get();
		Map<String, FunctionSpec> byShortId = mapSupplier.get();
		Set<FunctionSpec> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		for (FunctionSpec function : functions) {
			if (!seen.add(function)) {
				continue;
			}
			register(identifiers, function.getId(), function);
			register(byId, function.getId(), function);
			if (function.getLegacyId().isPresent()) {
				String legacyId = function.getLegacyId().get();
				register(identifiers, legacyId, function);
				register(byLegacyId, legacyId, function);
			}
			register(identifiers, function.getShortId(), function);
			register(byShortId, function.getShortId(), function);
		}
		this.functionsById = Collections.unmodifiableMap(byId);
		this.functionsByLegacyId = Collections.unmodifiableMap(byLegacyId);
		this.functionsByShortId = Collections.unmodifiableMap(byShortId);

	}

	protected BaseFunctionProvider(Collection<FunctionSpec> functions){
		this(functions, ()->new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

	}

	private static void register(Map<String, FunctionSpec> identifiers,
							 String id, FunctionSpec function) {
		FunctionSpec previous = identifiers.putIfAbsent(id, function);
		if (previous != null && previous != function) {
			throw new IllegalArgumentException("Duplicate function identifier '" + id
					+ "' claimed by " + describe(previous) + " and " + describe(function));
		}
	}

	private static String describe(FunctionSpec function) {
		return "'" + function.getId() + "' (short id '" + function.getShortId()
				+ "', legacy id '" + function.getLegacyId().orElse("") + "', "
				+ function.getClass().getName() + ")";
	}

	protected static Collection<FunctionSpec> toFunctionSpec(Collection<FunctionProvider> providers){
		Map<String, FunctionRegistration> identifiers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		List<FunctionSpec> functions = new LinkedList<>();
		Set<FunctionProvider> seenProviders = Collections.newSetFromMap(new IdentityHashMap<>());
		Set<FunctionSpec> seenFunctions = Collections.newSetFromMap(new IdentityHashMap<>());
		for (FunctionProvider provider : providers) {
			if (!seenProviders.add(provider)) {
				continue;
			}
			for (FunctionSpec function : provider.getProvidedFunctions()) {
				registerProviderIdentifier(identifiers, function.getId(), function, provider);
				function.getLegacyId().ifPresent(id ->
						registerProviderIdentifier(identifiers, id, function, provider));
				registerProviderIdentifier(identifiers, function.getShortId(), function, provider);
				if (seenFunctions.add(function)) {
					functions.add(function);
				}
			}
		}
		return functions;
	}

	private static void registerProviderIdentifier(Map<String, FunctionRegistration> identifiers,
												String id, FunctionSpec function,
												FunctionProvider provider) {
		FunctionRegistration current = new FunctionRegistration(function, provider);
		FunctionRegistration previous = identifiers.putIfAbsent(id, current);
		if (previous != null && previous.function() != function) {
			throw new IllegalArgumentException("Duplicate function identifier '" + id
					+ "' claimed by " + describe(previous.function()) + " from "
					+ providerSource(previous.provider()) + " and " + describe(function)
					+ " from " + providerSource(provider));
		}
	}

	private record FunctionRegistration(FunctionSpec function, FunctionProvider provider) { }

	private static String providerSource(FunctionProvider provider) {
		return "provider '" + provider.getId() + "'@"
				+ Integer.toHexString(System.identityHashCode(provider));
	}
	protected static <T>  Collection<T> accumulate(
			Collection<T> identity,  Collection<T> v){
		identity.addAll(v);
		return identity;
	}

	@Override
	public final Optional<FunctionSpec> getFunction(String functionId) {
		return Optional.ofNullable(
				functionsById.get(functionId))
				.or(()->Optional.ofNullable(functionsByShortId.get(functionId)))
				.or(()->Optional.ofNullable(functionsByLegacyId.get(functionId)));
	}

	@Override
	public final Collection<FunctionSpec> getProvidedFunctions() {
		 return functionsById.values()
				.stream()
				 .collect(Collectors.toSet());
	}
}
