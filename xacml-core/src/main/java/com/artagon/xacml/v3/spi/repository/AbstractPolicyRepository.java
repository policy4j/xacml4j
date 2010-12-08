package com.artagon.xacml.v3.spi.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.PolicyRepositoryCapability;
import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

/**
 * A base class for {@link PolicyRepository} implementations
 * 
 * @author Giedrius Trumpickas
 */
public abstract class AbstractPolicyRepository implements PolicyRepository
{	
	private ClassToInstanceMap<PolicyRepositoryCapability> capabilities;
	
	
	protected AbstractPolicyRepository(){
		this.capabilities = MutableClassToInstanceMap.create();
	}
	
	/**
	 * Adds new capability to this repository
	 * 
	 * @param <T>
	 * @param c
	 * @param capability
	 */
	protected <T extends PolicyRepositoryCapability> 
		void addCapability(Class<T> c, T capability)
	{
		Preconditions.checkState(
				this.capabilities.put(c, capability) != null);
	}
	
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

	@Override
	public final <C extends PolicyRepositoryCapability> C getCapability(Class<C> c) {
		Preconditions.checkNotNull(c);
		return capabilities.getInstance(c);
	}
}
