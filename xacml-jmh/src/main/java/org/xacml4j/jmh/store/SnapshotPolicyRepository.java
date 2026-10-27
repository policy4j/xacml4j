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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.Versionable;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.repository.AbstractPolicyRepository;

import com.google.common.base.Preconditions;

/**
 * A benchmark repository with immutable version indexes. Writers publish
 * both policy maps together; readers use one snapshot without taking a lock.
 */
public final class SnapshotPolicyRepository extends AbstractPolicyRepository
{
	private final Object updateLock = new Object();
	private volatile Snapshot snapshot = new Snapshot(Map.of(), Map.of());

	public SnapshotPolicyRepository(String id, FunctionProvider functions,
			DecisionCombiningAlgorithmProvider algorithms){
		super(id, functions, algorithms, Runnable::run);
	}

	@Override
	public CompositeDecisionRule get(String id, Version version){
		Snapshot current = snapshot;
		NavigableMap<Version, Policy> policies = current.policies().get(id);
		if(policies != null){
			return policies.get(version);
		}
		NavigableMap<Version, PolicySet> policySets = current.policySets().get(id);
		return policySets == null ? null : policySets.get(version);
	}

	@Override
	public Policy getPolicy(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return latest(versions(snapshot.policies(), id), version, earliest, latest);
	}

	@Override
	public PolicySet getPolicySet(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return latest(versions(snapshot.policySets(), id), version, earliest, latest);
	}

	@Override
	public Collection<Policy> getPolicies(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return matching(versions(snapshot.policies(), id), version, earliest, latest);
	}

	@Override
	public Collection<PolicySet> getPolicySets(String id, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return matching(versions(snapshot.policySets(), id), version, earliest, latest);
	}

	@Override
	protected boolean addPolicy(Policy policy){
		Preconditions.checkArgument(policy != null);
		synchronized(updateLock){
			Snapshot current = snapshot;
			NavigableMap<Version, Policy> existing = versions(current.policies(), policy.getId());
			if(existing.containsKey(policy.getVersion())){
				return false;
			}
			NavigableMap<Version, Policy> updated = new TreeMap<>(existing);
			updated.put(policy.getVersion(), policy);
			snapshot = new Snapshot(replace(current.policies(), policy.getId(), updated), current.policySets());
			return true;
		}
	}

	@Override
	protected boolean addPolicySet(PolicySet policySet){
		Preconditions.checkArgument(policySet != null);
		synchronized(updateLock){
			Snapshot current = snapshot;
			NavigableMap<Version, PolicySet> existing = versions(current.policySets(), policySet.getId());
			if(existing.containsKey(policySet.getVersion())){
				return false;
			}
			NavigableMap<Version, PolicySet> updated = new TreeMap<>(existing);
			updated.put(policySet.getVersion(), policySet);
			snapshot = new Snapshot(current.policies(), replace(current.policySets(), policySet.getId(), updated));
			return true;
		}
	}

	@Override
	protected boolean removePolicy(Policy policy){
		Preconditions.checkArgument(policy != null);
		synchronized(updateLock){
			Snapshot current = snapshot;
			NavigableMap<Version, Policy> existing = versions(current.policies(), policy.getId());
			if(!existing.containsKey(policy.getVersion())){
				return false;
			}
			NavigableMap<Version, Policy> updated = new TreeMap<>(existing);
			updated.remove(policy.getVersion());
			snapshot = new Snapshot(replace(current.policies(), policy.getId(), updated), current.policySets());
			return true;
		}
	}

	@Override
	protected boolean removePolicySet(PolicySet policySet){
		Preconditions.checkArgument(policySet != null);
		synchronized(updateLock){
			Snapshot current = snapshot;
			NavigableMap<Version, PolicySet> existing = versions(current.policySets(), policySet.getId());
			if(!existing.containsKey(policySet.getVersion())){
				return false;
			}
			NavigableMap<Version, PolicySet> updated = new TreeMap<>(existing);
			updated.remove(policySet.getVersion());
			snapshot = new Snapshot(current.policies(), replace(current.policySets(), policySet.getId(), updated));
			return true;
		}
	}

	private static <T> NavigableMap<Version, T> versions(Map<String, NavigableMap<Version, T>> rules, String id){
		return rules.getOrDefault(id, Collections.emptyNavigableMap());
	}

	private static <T> Map<String, NavigableMap<Version, T>> replace(
			Map<String, NavigableMap<Version, T>> rules, String id, NavigableMap<Version, T> versions){
		Map<String, NavigableMap<Version, T>> updated = new HashMap<>(rules);
		// Retain empty indexes, as the in-memory store does: get gives policy ids precedence over policy-set ids.
		updated.put(id, Collections.unmodifiableNavigableMap(versions));
		return Map.copyOf(updated);
	}

	/**
	 * The highest matching version, or {@code null} as the
	 * {@code PolicyRepository} contract of {@code getPolicy} and
	 * {@code getPolicySet} requires. It walks the keys downward without an
	 * entry, view, or {@code Optional}, so a lookup allocates nothing here
	 * (review rv004).
	 */
	private static <T extends Versionable> T latest(NavigableMap<Version, T> versions,
			VersionMatch version, VersionMatch earliest, VersionMatch latest){
		if(versions.isEmpty()){
			return null;
		}
		if(version == null && earliest == null && latest == null){
			return versions.get(versions.lastKey());
		}
		for(Version candidate = versions.lastKey(); candidate != null; candidate = versions.lowerKey(candidate)){
			if(matches(candidate, version, earliest, latest)){
				return versions.get(candidate);
			}
		}
		return null;
	}

	private static <T extends Versionable> List<T> matching(NavigableMap<Version, T> versions,
			VersionMatch version, VersionMatch earliest, VersionMatch latest){
		if(versions.isEmpty()){
			return List.of();
		}
		List<T> found = new ArrayList<>(versions.size());
		for(Map.Entry<Version, T> entry : versions.entrySet()){
			if(matches(entry.getKey(), version, earliest, latest)){
				found.add(entry.getValue());
			}
		}
		return Collections.unmodifiableList(found);
	}

	private static boolean matches(Version candidate, VersionMatch version, VersionMatch earliest, VersionMatch latest){
		return (version == null || version.match(candidate))
				&& (earliest == null || earliest.match(candidate))
				&& (latest == null || latest.match(candidate));
	}

	private record Snapshot(Map<String, NavigableMap<Version, Policy>> policies,
			Map<String, NavigableMap<Version, PolicySet>> policySets)
	{
	}
}
