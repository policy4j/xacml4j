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

import java.util.List;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * One use of a {@link PolicyStore}. The harness closes every session it
 * opens, also when a check fails.
 */
public interface StoreSession extends AutoCloseable
{
	/**
	 * @return the repository of this session
	 */
	PolicyRepository repository();

	/**
	 * Adds the parsed corpus policies to the repository. A store without
	 * {@link StoreCapability#LOAD} must override this method: to check that
	 * it already holds them, or as a no-op, because the fixture checks every
	 * corpus lookup and decision right after this call. A store with
	 * {@code LOAD} must also accept policies through
	 * {@link PolicyRepository#add}: the {@code importXml} benchmark adds them
	 * through the production import tool.
	 *
	 * @param policies the parsed policies, in corpus order
	 * @throws IllegalStateException if the repository rejects a policy
	 */
	default void load(List<CompositeDecisionRule> policies){
		PolicyRepository repository = repository();
		for(CompositeDecisionRule policy : policies){
			if(!repository.add(policy)){
				throw new IllegalStateException(String.format(
						"Repository \"%s\" did not add \"%s\" version \"%s\"",
						repository.getId(), policy.getId(), Rules.version(policy)));
			}
		}
	}

	/** Releases what the session holds. Nothing by default. */
	@Override
	default void close() throws Exception {
	}
}
