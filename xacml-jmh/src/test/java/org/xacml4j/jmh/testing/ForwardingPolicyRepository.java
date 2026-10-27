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
import java.util.Objects;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepositoryListener;

/**
 * A repository that forwards every call to another one. The short
 * {@code getPolicies} and {@code getPolicySets} forms call the four-argument
 * forms of this class, so a subclass changes a lookup in one place.
 */
public class ForwardingPolicyRepository implements PolicyRepository
{
	private final PolicyRepository delegate;

	public ForwardingPolicyRepository(PolicyRepository delegate){
		this.delegate = Objects.requireNonNull(delegate, "delegate");
	}

	@Override
	public String getId(){
		return delegate.getId();
	}

	@Override
	public Collection<Policy> getPolicies(String id, VersionMatch version){
		return getPolicies(id, version, null, null);
	}

	@Override
	public Collection<Policy> getPolicies(String id){
		return getPolicies(id, null, null, null);
	}

	@Override
	public Collection<Policy> getPolicies(String id, VersionMatch earliest, VersionMatch latest){
		return getPolicies(id, null, earliest, latest);
	}

	@Override
	public Collection<Policy> getPolicies(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return delegate.getPolicies(id, version, earliest, latest);
	}

	@Override
	public Collection<PolicySet> getPolicySets(String id, VersionMatch version){
		return getPolicySets(id, version, null, null);
	}

	@Override
	public Collection<PolicySet> getPolicySets(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return delegate.getPolicySets(id, version, earliest, latest);
	}

	@Override
	public Policy getPolicy(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return delegate.getPolicy(id, version, earliest, latest);
	}

	@Override
	public PolicySet getPolicySet(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return delegate.getPolicySet(id, version, earliest, latest);
	}

	@Override
	public CompositeDecisionRule get(String id, Version v){
		return delegate.get(id, v);
	}

	@Override
	public boolean add(CompositeDecisionRule r){
		return delegate.add(r);
	}

	@Override
	public boolean remove(CompositeDecisionRule r){
		return delegate.remove(r);
	}

	@Override
	public void addPolicyRepositoryListener(PolicyRepositoryListener l){
		delegate.addPolicyRepositoryListener(l);
	}

	@Override
	public void removePolicyRepositoryListener(PolicyRepositoryListener l){
		delegate.removePolicyRepositoryListener(l);
	}
}
