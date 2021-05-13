package org.xacml4j.v30.spi.repository;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.pdp.Versionable;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

/**
 * An implementation of {@link AbstractPolicyRepository} which keeps
 * all policies and policy sets in memory indexed by identifier and version
 * for fast search queries
 *
 * @author Giedrius Trumpickas
 */
public class InMemoryPolicyRepository extends AbstractPolicyRepository
{
	private final static Logger log = LoggerFactory.getLogger(InMemoryPolicyRepository.class);

	private final static int INITIAL_POLICYSET_MAP_SIZE = 128;
	private final static int INITIAL_POLICY_MAP_SIZE = 128;
	private ConcurrentHashMap<String, ConcurrentNavigableMap<Version, Policy>> policies;
	private ConcurrentHashMap<String, ConcurrentNavigableMap<Version, PolicySet>> policySets;

	public InMemoryPolicyRepository(
			String id,
			FunctionProvider functions,
			DecisionCombiningAlgorithmProvider decisionAlgorithms)
		throws Exception
	{
		super(id, functions, decisionAlgorithms);
		this.policies = new ConcurrentHashMap<String, ConcurrentNavigableMap<Version, Policy>>(INITIAL_POLICY_MAP_SIZE);
		this.policySets = new ConcurrentHashMap<String, ConcurrentNavigableMap<Version, PolicySet>>(INITIAL_POLICYSET_MAP_SIZE);
	}

	@Override
	public Collection<Policy> getPolicies(
			String id,
			VersionMatch version,
			VersionMatch earliest,
			VersionMatch latest)
	{
		Map<Version, Policy> byId = policies.get(id);
		if(log.isDebugEnabled() &&
				byId != null){
			log.debug("Found=\"{}\" versions of policy with id=\"{}\"",
					byId.size(), id);
		}
		return find((byId != null)?byId.values():null, version, earliest, latest);
	}

	@Override
	public Collection<PolicySet> getPolicySets(
			String id,
			VersionMatch version,
			VersionMatch earliest,
			VersionMatch latest)
	{
		Map<Version, PolicySet> byId = policySets.get(id);
		if(log.isDebugEnabled() &&
				byId != null){
			log.debug("Found=\"{}\" versions of policy set with id=\"{}\"",
					byId.size(), id);
		}
		return find((byId != null)?byId.values():null, version, earliest, latest);
	}

	@Override
	public CompositeDecisionRule get(String id, Version v) {
		Map<Version, Policy> pv = policies.get(id);
		if(pv != null){
			return pv.get(v);
		}
		Map<Version, PolicySet> psv = policySets.get(id);
		return (psv != null)?psv.get(v):null;
	}

	@Override
	protected  boolean addPolicy(Policy policy)
	{
		Preconditions.checkArgument(policy != null);
		String id = policy.getId();
		Version v = policy.getVersion();
		if(log.isDebugEnabled()){
			log.debug("Adding Policy with " +
					"id=\"{}\" version=\"{}\"", id, v);
		}
		ConcurrentNavigableMap<Version, Policy> versions = policies.get(id);
		if(versions == null){
			versions = new ConcurrentSkipListMap<Version, Policy>();
			ConcurrentNavigableMap<Version, Policy> existing = policies.putIfAbsent(id, versions);
			if(existing != null){
				versions = existing;
			}
		}
		return versions.putIfAbsent(v, policy) == null;
	}

	@Override
	protected boolean addPolicySet(PolicySet policySet)
	{
		Preconditions.checkArgument(policySet != null);
		String id = policySet.getId();
		Version v = policySet.getVersion();
		if(log.isDebugEnabled()){
			log.debug("Adding PolicySet with " +
					"id=\"{}\" version=\"{}\"", id, v);
		}
		ConcurrentNavigableMap<Version, PolicySet> versions = policySets.get(id);
		if(versions == null){
			versions = new ConcurrentSkipListMap<Version, PolicySet>();
			ConcurrentNavigableMap<Version, PolicySet> existing = policySets.putIfAbsent(id, versions);
			if(existing != null){
				versions = existing;
			}
		}
		return versions.putIfAbsent(v, policySet) == null;
	}

	@Override
	protected boolean removePolicy(Policy p)
	{
		Preconditions.checkArgument(p != null);
		String id = p.getId();
		Version v = p.getVersion();
		if(log.isDebugEnabled()){
			log.debug("Removing Policy with " +
					"id=\"{}\" version=\"{}\"", id, v);
		}
		ConcurrentNavigableMap<Version, Policy> versions = policies.get(id);
		if(versions != null){
			return versions.remove(v) != null;
		}
		return (versions == null)?false:(versions.remove(v) != null);
	}

	@Override
	protected boolean removePolicySet(PolicySet p)
	{
		Preconditions.checkArgument(p != null);
		String id = p.getId();
		Version v = p.getVersion();
		if(log.isDebugEnabled()){
			log.debug("Removing PolicySet with " +
					"id=\"{}\" version=\"{}\"", id, v);
		}
		ConcurrentNavigableMap<Version, PolicySet> versions = policySets.get(id);
		if(versions != null){
			return versions.remove(v) != null;
		}
		return (versions == null)?false:(versions.remove(v) != null);
	}

	private <T extends Versionable> Collection<T> find(
			Collection<T> c,
			final VersionMatch version,
			final VersionMatch earliest,
			final VersionMatch latest)
	{
		if(c == null){
			return ImmutableList.of();
		}
		return Collections2.filter(c, new Predicate<T>() {
			@Override
			public boolean apply(T p) {
				return (version == null || version.match(p.getVersion())) &&
						(earliest == null || earliest.match(p.getVersion())) &&
						(latest == null || latest.match(p.getVersion()));
			}

		});
	}

}
