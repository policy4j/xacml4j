package org.xacml4j.jmh.harness;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import java.util.Optional;
import java.util.SortedSet;

import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * A named factory of policy repositories, the store that a benchmark measures. Implementations
 * are found with {@link java.util.ServiceLoader} (register with
 * {@code @AutoService(PolicyStore.class)} or a {@code META-INF/services} file) and selected with
 * the benchmark parameter {@code store=<name>} or {@code store=<name>:<argument>}.
 */
public interface PolicyStore
{
	/**
	 * @return the name that the benchmark parameter {@code store} selects
	 */
	String name();

	/**
	 * Opens a new, empty repository. The harness closes it with {@link #close}.
	 *
	 * @param id unique among the repositories of all JVMs; letters, digits, {@code .}, {@code _}, {@code -}
	 * @param argument the text after the first colon of the parameter, if any
	 * @param functions the functions that policies may use
	 * @param algorithms the combining algorithms that policies may use
	 * @return the repository
	 * @throws Exception if the store cannot open a repository
	 */
	PolicyRepository open(String id, Optional<String> argument,
			FunctionProvider functions, DecisionCombiningAlgorithmProvider algorithms) throws Exception;

	/**
	 * Releases a repository that {@link #open} returned. Nothing by default.
	 *
	 * @param repository the repository
	 * @throws Exception if the store cannot release it
	 */
	default void close(PolicyRepository repository) throws Exception {
	}

	/**
	 * @param name a store name
	 * @return the store with that name
	 * @throws IllegalArgumentException if no store has that name; the message lists the known names
	 * @throws IllegalStateException if several stores have that name; the message names their classes
	 */
	static PolicyStore named(String name){
		return Services.find(PolicyStore.class, PolicyStore::name, "store", name);
	}

	/**
	 * @return the names of the stores on the class path
	 */
	static SortedSet<String> names(){
		return Services.names(PolicyStore.class, PolicyStore::name);
	}
}
