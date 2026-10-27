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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.xacml4j.jmh.harness.PolicyStore;
import org.xacml4j.jmh.harness.PolicyStoreContext;
import org.xacml4j.jmh.harness.StoreSession;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * Store {@code probe}: the production repository, with a record of the
 * argument and the repository id of every session. {@link java.util.ServiceLoader}
 * makes a new instance for each lookup, so the record is static: a test
 * resets it first, and tests that read it run one at a time (the JUnit
 * default).
 */
public final class ProbeStore implements PolicyStore
{
	public static final String NAME = "probe";

	private static final List<Optional<String>> ARGUMENTS = Collections.synchronizedList(new ArrayList<>());
	private static final List<String> REPOSITORY_IDS = Collections.synchronizedList(new ArrayList<>());

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public StoreSession open(PolicyStoreContext context){
		ARGUMENTS.add(context.argument());
		REPOSITORY_IDS.add(context.repositoryId());
		PolicyRepository repository = TestStores.inMemory(context);
		return () -> repository;
	}

	/** @return the arguments of the sessions since the last reset, in order */
	public static List<Optional<String>> arguments(){
		synchronized(ARGUMENTS){
			return List.copyOf(ARGUMENTS);
		}
	}

	/** @return the repository ids of the sessions since the last reset, in order */
	public static List<String> repositoryIds(){
		synchronized(REPOSITORY_IDS){
			return List.copyOf(REPOSITORY_IDS);
		}
	}

	public static void reset(){
		ARGUMENTS.clear();
		REPOSITORY_IDS.clear();
	}
}
