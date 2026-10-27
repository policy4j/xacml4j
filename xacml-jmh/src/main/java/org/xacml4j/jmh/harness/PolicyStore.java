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

import java.util.EnumSet;
import java.util.Set;

/**
 * A policy store that the benchmarks can measure: a factory of sessions on a
 * {@link org.xacml4j.v30.spi.repository.PolicyRepository}.
 * <p>
 * Implementations are found with {@link java.util.ServiceLoader}: register
 * one with {@code @AutoService(PolicyStore.class)} or a
 * {@code META-INF/services/org.xacml4j.jmh.harness.PolicyStore} file, and
 * select it with the benchmark parameter {@code store=<name>} or
 * {@code store=<name>:<argument>}.
 */
public interface PolicyStore
{
	/**
	 * @return the name that the benchmark parameter {@code store} selects
	 */
	String name();

	/**
	 * @return what this store supports; all capabilities unless overridden
	 */
	default Set<StoreCapability> capabilities(){
		return EnumSet.allOf(StoreCapability.class);
	}

	/**
	 * Opens a new, isolated session. The harness closes every session it
	 * opens. A store without {@link StoreCapability#LOAD} attaches to its
	 * existing data here.
	 *
	 * @param context the repository id of the session, the argument of the
	 * parameter, and the providers that the repository needs
	 * @return a new session
	 * @throws Exception if the store cannot open a session
	 */
	StoreSession open(PolicyStoreContext context) throws Exception;
}
