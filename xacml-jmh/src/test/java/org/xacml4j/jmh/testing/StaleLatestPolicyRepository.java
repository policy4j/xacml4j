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

import java.util.Collection;

import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.Versionable;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * A repository that answers {@code getPolicy} and {@code getPolicySet} with
 * the earliest matching version instead of the latest one. Exact lookups
 * with {@code get(id, version)} stay correct.
 */
public final class StaleLatestPolicyRepository extends ForwardingPolicyRepository
{
	public StaleLatestPolicyRepository(PolicyRepository delegate){
		super(delegate);
	}

	@Override
	public Policy getPolicy(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return earliestOf(getPolicies(id, version, earliest, latest));
	}

	@Override
	public PolicySet getPolicySet(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return earliestOf(getPolicySets(id, version, earliest, latest));
	}

	/** @return the earliest version, or {@code null} as the repository contract states for no match */
	private static <T extends Versionable> T earliestOf(Collection<T> found){
		T earliest = null;
		for(T candidate : found){
			if(earliest == null || candidate.getVersion().compareTo(earliest.getVersion()) < 0){
				earliest = candidate;
			}
		}
		return earliest;
	}
}
