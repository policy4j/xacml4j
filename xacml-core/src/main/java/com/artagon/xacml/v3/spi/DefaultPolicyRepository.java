package com.artagon.xacml.v3.spi;

import java.util.Collection;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.Versionable;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class DefaultPolicyRepository implements PolicyRepository
{
	private Multimap<String, Policy> policy;
	private Multimap<String, PolicySet> policySet;
	
	public DefaultPolicyRepository(){
		this.policy = HashMultimap.create();
		this.policySet = HashMultimap.create();
	}

	@Override
	public Iterable<Policy> getPolicies(String id, VersionMatch version) {
		return find(policy.get(id), version, null, null);
	}

	@Override
	public Iterable<PolicySet> getPolicySets(String id, VersionMatch version) {
		return find(policySet.get(id), version, null, null);
	}

	@Override
	public Iterable<Policy> getPolicies(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) {
		// TODO Auto-generated method stub
		return find(policy.get(id), version, earliest, latest);
	}

	@Override
	public Iterable<PolicySet> getPolicySets(String id, VersionMatch version,
			VersionMatch earliest, VersionMatch latest) {
		return find(policySet.get(id), version, earliest, latest);
	}
	
	private <T extends Versionable> Collection<T> find(Collection<T> c, 
			final VersionMatch version, final VersionMatch earliest, final VersionMatch latest)
	{
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
