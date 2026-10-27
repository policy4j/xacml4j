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
import java.util.List;
import java.util.Objects;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * A repository that says it added the rule with one id but does not keep it.
 * Every lookup of that id finds nothing ({@code null} or an empty
 * collection, as the {@link PolicyRepository} contract states), or throws
 * {@link LostPolicyException}. Lookups of other ids go to the delegate.
 */
public final class LosingPolicyRepository extends ForwardingPolicyRepository
{
	private final String lost;
	private final boolean throwing;

	/**
	 * @param delegate the repository that keeps every other rule
	 * @param lost the id of the rule to lose
	 * @param throwing whether a lookup of the lost id throws instead of finding nothing
	 */
	public LosingPolicyRepository(PolicyRepository delegate, String lost, boolean throwing){
		super(delegate);
		this.lost = Objects.requireNonNull(lost, "lost");
		this.throwing = throwing;
	}

	@Override
	public Collection<Policy> getPolicies(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return isLost(id) ? List.of() : super.getPolicies(id, version, earliest, latest);
	}

	@Override
	public Collection<PolicySet> getPolicySets(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return isLost(id) ? List.of() : super.getPolicySets(id, version, earliest, latest);
	}

	@Override
	public Policy getPolicy(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return isLost(id) ? null : super.getPolicy(id, version, earliest, latest);
	}

	@Override
	public PolicySet getPolicySet(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return isLost(id) ? null : super.getPolicySet(id, version, earliest, latest);
	}

	@Override
	public CompositeDecisionRule get(String id, Version v){
		return isLost(id) ? null : super.get(id, v);
	}

	/** Reports the lost rule as added, as a store that drops a write silently would. */
	@Override
	public boolean add(CompositeDecisionRule r){
		if(lost.equals(r.getId())){
			return true;
		}
		return super.add(r);
	}

	private boolean isLost(String id){
		if(!lost.equals(id)){
			return false;
		}
		if(throwing){
			throw new LostPolicyException(getId(), id);
		}
		return true;
	}
}
