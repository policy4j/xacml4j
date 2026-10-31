package org.xacml4j.jmh.store;

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

import org.xacml4j.jmh.harness.PolicyStore;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.auto.service.AutoService;

/**
 * The snapshot store ({@link SnapshotPolicyRepository}). It takes no argument and
 * owns no resources that need closing.
 */
@AutoService(PolicyStore.class)
public final class SnapshotPolicyStore implements PolicyStore
{
	public static final String NAME = "snapshot";

	@Override
	public String name(){
		return NAME;
	}


	@Override
	public PolicyRepository open(String id, Optional<String> argument,
			FunctionProvider functions, DecisionCombiningAlgorithmProvider algorithms){
		if(argument.isPresent()){
			throw new IllegalArgumentException("Store \"" + NAME + "\" takes no argument, got \"" + argument.get() + "\"");
		}
		return new SnapshotPolicyRepository(id, functions, algorithms);
	}
}
