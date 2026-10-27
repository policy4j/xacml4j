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

import org.xacml4j.jmh.harness.PolicyStore;
import org.xacml4j.jmh.harness.PolicyStoreContext;
import org.xacml4j.jmh.harness.StoreSession;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * Store {@code counting}: the production repository, with a record of the
 * repository id of every session that was opened and of every
 * {@code close} call. The record is static, as in {@link ProbeStore}: a test
 * resets it first.
 */
public final class CountingStore implements PolicyStore
{
	public static final String NAME = "counting";

	private static final List<String> OPENED = Collections.synchronizedList(new ArrayList<>());
	private static final List<String> CLOSED = Collections.synchronizedList(new ArrayList<>());

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public StoreSession open(PolicyStoreContext context){
		OPENED.add(context.repositoryId());
		return new Session(TestStores.inMemory(context));
	}

	/** @return the repository ids of the opened sessions since the last reset, in order */
	public static List<String> opened(){
		synchronized(OPENED){
			return List.copyOf(OPENED);
		}
	}

	/** @return the repository id of every {@code close} call since the last reset, in order */
	public static List<String> closed(){
		synchronized(CLOSED){
			return List.copyOf(CLOSED);
		}
	}

	public static void reset(){
		OPENED.clear();
		CLOSED.clear();
	}

	private record Session(PolicyRepository repository) implements StoreSession
	{
		@Override
		public void close(){
			CLOSED.add(repository.getId());
		}
	}
}
