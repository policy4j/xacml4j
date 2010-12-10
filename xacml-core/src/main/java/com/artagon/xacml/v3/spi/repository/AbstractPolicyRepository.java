package com.artagon.xacml.v3.spi.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;

/**
 * A base class for {@link PolicyRepository} implementations
 * every {@link PolicyRepository} implementation supports
 * at least {@link PolicyRepositorySearch} capability
 * 
 * @author Giedrius Trumpickas
 */
public abstract class AbstractPolicyRepository 
	implements PolicyRepository
{	
	/**
	 * Implementation assumes that 
	 * {@link #getPolicies(String, VersionMatch, VersionMatch, VersionMatch)}
	 * returns unordered collection of policies
	 */
	@Override
	public Policy getPolicy(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest){
		Collection<Policy> found = getPolicies(id, version, earliest, latest);
		if(found.isEmpty()){
			return null;
		}
		return Collections.<Policy>max(found, new Comparator<Policy>() {
			@Override
			public int compare(Policy a, Policy b) {
				return a.getVersion().compareTo(b.getVersion());
			}
		});
	}
	
	/**
	 * Implementation assumes that 
	 * {@link #getPolicies(String, VersionMatch, VersionMatch, VersionMatch)}
	 * returns unordered collection of policies. 
	 */
	@Override
	public PolicySet getPolicySet(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest)
	{
		Collection<PolicySet> found = getPolicySets(id, version, earliest, latest);
		if(found.isEmpty()){
			return null;
		}
		return Collections.<PolicySet>max(found, new Comparator<PolicySet>() {
			@Override
			public int compare(PolicySet a, PolicySet b) {
				return a.getVersion().compareTo(b.getVersion());
			}
			
		});
	}
	
	@Override
	public final Collection<Policy> getPolicies(String id) {
		return getPolicies(id, null);
	}

	@Override
	public final Collection<Policy> getPolicies(String id, VersionMatch earliest,
			VersionMatch latest) {
		return getPolicies(id, null, earliest, latest);
	}

	@Override
	public final Collection<Policy> getPolicies(String id, VersionMatch version){
		return getPolicies(id, version, null, null);
	}
	
	@Override
	public final Collection<PolicySet> getPolicySets(String id, VersionMatch version){
		return getPolicySets(id, version, null, null);
	}
	
	public CompositeDecisionRule get(String id, Version v)
	{
		VersionMatch m = new VersionMatch(v.getValue());
		Policy p = getPolicy(id, m, null, null);
		return p != null?p:getPolicySet(id, m, null, null);
	}
	@Override
	public void add(CompositeDecisionRule r) {
		if(r instanceof Policy){
			addPolicy((Policy)r);
			return;
		}
		if(r instanceof PolicySet){
			addPolicySet((PolicySet)r);
			return;
		}
	}
	
	protected abstract void addPolicy(Policy p);
	protected abstract void addPolicySet(PolicySet p);
	
	@Override
	public boolean remove(CompositeDecisionRule r) {
		if(r instanceof Policy){
			return removePolicy((Policy)r);
		}
		if(r instanceof PolicySet){
			return removePolicySet((PolicySet)r);
		}
		return false;
	}
	
	protected abstract  boolean removePolicy(Policy p);
	protected abstract boolean removePolicySet(PolicySet p);
}
