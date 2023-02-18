package org.xacml4j.v30.spi.function;

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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProvider;


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
		this.functionsById = functions
				.stream()
				.collect(Collectors
						.toMap(
								v -> v.getId(),
								v -> v,
								(a, b) -> merge(a, b),
								mapSupplier));
		this.functionsByLegacyId = functions
				.stream()
				.filter(f->f.getLegacyId().isPresent())
				.collect(Collectors
						         .toMap(
								         v -> v.getLegacyId().get(),
								         v -> v,
								         (a, b) -> merge(a, b),
								         mapSupplier));
		this.functionsByShortId = functions
				.stream()
				.collect(Collectors
						.toMap(
								v -> v.getShortId(),
								v -> v,
								(a, b) -> merge(a, b),
								mapSupplier));

	}

	protected BaseFunctionProvider(Collection<FunctionSpec> functions){
		this(functions, ()->new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

	}

	private static FunctionSpec merge(
			FunctionSpec a, FunctionSpec b){
		return a;
	}

	protected static Collection<FunctionSpec> toFunctionSpec(Collection<FunctionProvider> providers){
		return providers.stream()
				.map(p->p.getProvidedFunctions())
				.reduce(new LinkedList<>(),
						(a, b)-> accumulate(a, b));
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
