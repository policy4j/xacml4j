package com.artagon.xacml.v3.spi.repository;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.Version;
import com.artagon.xacml.v3.policy.VersionMatch;
import com.artagon.xacml.v3.policy.Versionable;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class InMemoryPolicyRepositoryWithChm extends AbstractPolicyRepository
{
	private final static Logger log = LoggerFactory.getLogger(InMemoryPolicyRepositoryWithChm.class);
	
	private ConcurrentHashMap<String, ConcurrentNavigableMap<Version, Policy>> policies;
	private ConcurrentHashMap<String, ConcurrentNavigableMap<Version, PolicySet>> policySets;
	
	
	public InMemoryPolicyRepositoryWithChm()
	{
		this.policies = new ConcurrentHashMap<String, ConcurrentNavigableMap<Version, Policy>>();
		this.policySets = new ConcurrentHashMap<String, ConcurrentNavigableMap<Version, PolicySet>>();
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
	public void add(CompositeDecisionRule policy) {
		if(policy instanceof Policy){
			addInternal((Policy)policy);
			return;
		}
		if(policy instanceof PolicySet){
			addInternal((PolicySet)policy);
			return;
		}
	}

	private void addInternal(Policy policy) 
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
		Preconditions.checkState(
				versions.put(v, policy) == null, 
				"Repository already contains a policy with id=\"%s\" and version=\"%s\"", 
					id, v);
	
	}

	private void addInternal(PolicySet policySet) 
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
		Preconditions.checkState(
				versions.put(policySet.getVersion(), policySet) == null, 
				"Repository already contains a policy with id=\"%s\" and version=\"%s\"", 
					id, v);

		
	}
	
	private <T extends Versionable> Collection<T> find(
			Collection<T> c, 
			final VersionMatch version, 
			final VersionMatch earliest, 
			final VersionMatch latest)
	{
		if(c == null){
			return Collections.<T>emptyList();
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
