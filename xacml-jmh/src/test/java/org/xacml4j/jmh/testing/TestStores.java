package org.xacml4j.jmh.testing;

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

import java.util.List;
import java.util.Optional;

import org.xacml4j.jmh.harness.PolicyStore;
import org.xacml4j.jmh.harness.PolicyStoreContext;
import org.xacml4j.jmh.harness.StoreSession;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * Helpers of the test stores. The test stores are registered in
 * {@code src/test/resources/META-INF/services}, a classpath entry outside
 * the main code of the module, as a store in another jar would be.
 */
public final class TestStores
{
	/** The default providers, as the harness gives them to every session. */
	public static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	public static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();

	private TestStores(){
	}

	/** @return an empty production repository with the id and the providers of the context */
	public static PolicyRepository inMemory(PolicyStoreContext context){
		return new InMemoryPolicyRepository(context.repositoryId(), context.functions(), context.algorithms());
	}

	/**
	 * Opens a session of a store outside a fixture and loads rules into it,
	 * for tests that compare a fixture with what the store does on its own.
	 * The caller closes the session.
	 *
	 * @param store the store
	 * @param argument the store argument
	 * @param repositoryId the repository id of the session
	 * @param rules the rules to load
	 * @return the open session
	 */
	public static StoreSession openLoaded(PolicyStore store, Optional<String> argument, String repositoryId,
			List<CompositeDecisionRule> rules) throws Exception {
		StoreSession session = store.open(new PolicyStoreContext(repositoryId, argument, FUNCTIONS, ALGORITHMS));
		session.load(rules);
		return session;
	}

	/** @return the id of the rule to lose, from the store argument */
	static String lostId(String store, PolicyStoreContext context){
		return context.argument().filter(id -> !id.isEmpty()).orElseThrow(() -> new IllegalArgumentException(
				"Store \"" + store + "\" needs the id of the rule to lose: " + store + ":<id>"));
	}
}
