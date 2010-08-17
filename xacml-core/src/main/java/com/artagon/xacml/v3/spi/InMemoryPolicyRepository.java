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
	public Collection<Policy> getPolicies(String id, VersionMatch version) 
	{
		Map<Version, Policy> byId = policies.get(id);
		return find((byId != null)?byId.values():null, version, null, null);
	}

	@Override
	public Collection<PolicySet> getPolicySets(String id, VersionMatch version) {
		Map<Version, PolicySet> byId = policySets.get(id);
		return find((byId != null)?byId.values():null, version, null, null);	
	}

	@Override
	public Collection<Policy> getPolicies(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) 
	{
		Map<Version, Policy> byId = policies.get(id);
		return find((byId != null)?byId.values():null, version, earliest, latest);
	}

	@Override
	public Collection<PolicySet> getPolicySets(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) 
	{
		Map<Version, PolicySet> byId = policySets.get(id);
		return find((byId != null)?byId.values():null, version, earliest, latest);
	}
	
	@Override
	public void add(Policy policy) 
	{
		Preconditions.checkArgument(policy != null);
		String id = policy.getId();
		Version v = policy.getVersion();
		Map<Version, Policy> versions = policies.get(id);
		if(versions == null || 
				versions.isEmpty()){
			versions = new TreeMap<Version, Policy>();
			policies.put(id, versions);
		}
		Preconditions.checkArgument(!versions.containsKey(v), 
				"Repository already contains a policy with id=\"%s\" and version=\"%s\"", 
							id, v);
		versions.put(v, policy);
	}

	@Override
	public void add(PolicySet policySet) 
	{
		Preconditions.checkArgument(policySet != null);
		String id = policySet.getId();
		Version v = policySet.getVersion();
		Map<Version, PolicySet> versions = policySets.get(id);
		if(versions == null || 
				versions.isEmpty()){
			versions = new TreeMap<Version, PolicySet>();
			policySets.put(id, versions);
		}
		Preconditions.checkArgument(!versions.containsKey(v), 
				"Repository already contains a policy with id=\"%s\" and version=\"%s\"", 
				id, v);
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
