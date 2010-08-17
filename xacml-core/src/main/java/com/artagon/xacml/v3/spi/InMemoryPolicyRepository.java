package com.artagon.xacml.v3.spi;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.Versionable;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class InMemoryPolicyRepository implements PolicyRepository
{
	private Map<String, Map<Version, Policy>> policies;
	private Map<String, Map<Version, PolicySet>> policySets;
	
	public InMemoryPolicyRepository(){
		this.policies = new ConcurrentHashMap<String, Map<Version, Policy>>();
		this.policySets = new ConcurrentHashMap<String, Map<Version, PolicySet>>();
	}

	@Override
	public Iterable<Policy> getPolicies(String id, VersionMatch version) {
		return Collections.<Policy>emptyList();
	}

	@Override
	public Iterable<PolicySet> getPolicySets(String id, VersionMatch version) {
		return Collections.<PolicySet>emptyList();	
	}

	@Override
	public Iterable<Policy> getPolicies(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) 
	{
		return Collections.<Policy>emptyList();
	}

	@Override
	public Iterable<PolicySet> getPolicySets(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) {
		return Collections.<PolicySet>emptyList();
	}
	
	@Override
	public void add(Policy policy) 
	{
		Map<Version, Policy> versions = policies.get(policy.getId());
		if(versions == null || 
				versions.isEmpty()){
			versions = new TreeMap<Version, Policy>();
			policies.put(policy.getId(), versions);
		}
		Preconditions.checkArgument(versions.containsKey(policy.getVersion()), 
				"Repository already contains a policy with id=\"%s\" and version=\"%s\"", 
				policy.getId(), policy.getVersion());
		versions.put(policy.getVersion(), policy);
	}

	@Override
	public void add(PolicySet policySet) 
	{
		Map<Version, PolicySet> versions = policySets.get(policySet.getId());
		if(versions == null || 
				versions.isEmpty()){
			versions = new TreeMap<Version, PolicySet>();
			policySets.put(policySet.getId(), versions);
		}
		Preconditions.checkArgument(versions.containsKey(policySet.getVersion()), 
				"Repository already contains a policy with id=\"%s\" and version=\"%s\"", 
				policySet.getId(), policySet.getVersion());
		versions.put(policySet.getVersion(), policySet);
	}

	private <T extends Versionable> Collection<T> find(Collection<T> c, 
			final VersionMatch version, final VersionMatch earliest, final VersionMatch latest)
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
