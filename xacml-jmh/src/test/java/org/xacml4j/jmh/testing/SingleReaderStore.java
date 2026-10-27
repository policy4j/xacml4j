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

import java.util.EnumSet;
import java.util.Set;

import org.xacml4j.jmh.harness.PolicyStore;
import org.xacml4j.jmh.harness.PolicyStoreContext;
import org.xacml4j.jmh.harness.StoreCapability;
import org.xacml4j.jmh.harness.StoreSession;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * Store {@code single-reader}: the production repository, declared without
 * {@link StoreCapability#CONCURRENT_READS}, so it must run with one
 * benchmark thread only.
 */
public final class SingleReaderStore implements PolicyStore
{
	public static final String NAME = "single-reader";

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public Set<StoreCapability> capabilities(){
		return EnumSet.of(StoreCapability.LOAD);
	}

	@Override
	public StoreSession open(PolicyStoreContext context){
		PolicyRepository repository = TestStores.inMemory(context);
		return () -> repository;
	}
}
