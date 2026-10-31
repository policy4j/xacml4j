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
import java.util.function.Consumer;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * A repository that reports every lookup to a sink before it forwards the
 * call. A short {@code getPolicies} or {@code getPolicySets} form is reported
 * once, as the four-argument form it calls.
 */
public final class RecordingPolicyRepository extends ForwardingPolicyRepository
{
	/**
	 * One lookup.
	 *
	 * @param method the repository method: {@code get}, {@code getPolicy},
	 * {@code getPolicySet}, {@code getPolicies}, or {@code getPolicySets}
	 * @param id the id that was looked up
	 */
	public record Read(String method, String id)
	{
	}

	private final Consumer<Read> sink;

	public RecordingPolicyRepository(PolicyRepository delegate, Consumer<Read> sink){
		super(delegate);
		this.sink = Objects.requireNonNull(sink, "sink");
	}

	@Override
	public Collection<Policy> getPolicies(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		sink.accept(new Read("getPolicies", id));
		return super.getPolicies(id, version, earliest, latest);
	}

	@Override
	public Collection<PolicySet> getPolicySets(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		sink.accept(new Read("getPolicySets", id));
		return super.getPolicySets(id, version, earliest, latest);
	}

	@Override
	public Policy getPolicy(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		sink.accept(new Read("getPolicy", id));
		return super.getPolicy(id, version, earliest, latest);
	}

	@Override
	public PolicySet getPolicySet(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		sink.accept(new Read("getPolicySet", id));
		return super.getPolicySet(id, version, earliest, latest);
	}

	@Override
	public CompositeDecisionRule get(String id, Version v){
		sink.accept(new Read("get", id));
		return super.get(id, v);
	}
}
