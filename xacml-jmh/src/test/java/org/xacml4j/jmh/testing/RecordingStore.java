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
 * Store {@code recording}: the production repository wrapped in a
 * {@link RecordingPolicyRepository}. The lookups of every session go to one
 * static log, in call order; a test resets it first, as in {@link ProbeStore}.
 */
public final class RecordingStore implements PolicyStore
{
	public static final String NAME = "recording";

	private static final List<RecordingPolicyRepository.Read> READS = Collections.synchronizedList(new ArrayList<>());

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public StoreSession open(PolicyStoreContext context){
		PolicyRepository repository = new RecordingPolicyRepository(TestStores.inMemory(context), READS::add);
		return () -> repository;
	}

	/** @return the lookups of all sessions since the last reset, in call order */
	public static List<RecordingPolicyRepository.Read> reads(){
		synchronized(READS){
			return List.copyOf(READS);
		}
	}

	/** @return how many lookups all sessions made since the last reset */
	public static int count(){
		return READS.size();
	}

	public static void reset(){
		READS.clear();
	}
}
