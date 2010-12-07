package com.artagon.xacml.v3.spi;


import java.util.Collection;

import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.VersionMatch;

/**
 * A repository for XACML policies, implements
 * {@link PolicyReferenceResolver} to resolve
 * policies referenced by the top level policies
 * in the {@link PolicyDomain}
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyRepository 
{
	/**
	 * Gets all versions of the policy with a given
	 * identifier matching given version constraints
	 * 
	 * @param id a policy identifier
	 * @param version a policy version match constraint
	 * @return an {@link Collection} of matching policies
	 * sorted from the earliest to latest version 
	 */
	Collection<Policy> getPolicies(String id, VersionMatch version);
	
	/**
	 * Gets all versions of the policy with a given
	 * identifier
	 * 
	 * @param id a policy identifier
	 * @return an {@link Collection} of matching policies
	 * sorted from the earliest to latest version 
	 */
	Collection<Policy> getPolicies(String id);
	
	/**
	 * Gets all versions of the policy with a given
	 * identifier matching given version constraints
	 * 
	 * @param id a policy identifier
	 * @param earliest an earliest policy version 
	 * match constraint
	 * @param latest a latest policy version matching
	 * constraint
	 * @return an {@link Collection} of matching policies
	 * sorted from the earliest to latest version 
	 */
	Collection<Policy> getPolicies(String id, 
			VersionMatch earliest, VersionMatch latest);
	
	Collection<Policy> getPolicies(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest);
	
	/**
	 * Queries policy repository for a given
	 * policy set via policy set identifier and version
	 * match constraint
	 * 
	 * @param id a policy identifier
	 * @param version a policy version match constraint
	 * @return an {@link Iterable} to found policies
	 */
	Collection<PolicySet> getPolicySets(
			String id, VersionMatch version);
	
	/**
	 * Gets all matching policy sets
	 * 
	 * @param id a policy set identifier
	 * @param version a  version match
	 * @param earliest an earliest version match
	 * @param latest a latest version match
	 * @return a collection of matching {
	 */
	Collection<PolicySet> getPolicySets(
			String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest);
	
	/**
	 * Gets latest policy matching given version constraints
	 * 
	 * @param id a policy identifier
	 * @param version a  version match constraint
	 * @param earliest a lower bound version match constraint
	 * @param latest an upper bound version match constraint
	 * @return {@link Policy} or <code>null</code> if this 
	 * repository does not contain matching policy
	 */
	Policy getPolicy(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest);
	
	
	/**
	 * Gets latest policy set matching given version constraints
	 * 
	 * @param id a policy identifier
	 * @param version a  version match constraint
	 * @param earliest a lower bound version match constraint
	 * @param latest an upper bound version match constraint
	 * @return {@link PolicySet} or <code>null</code> if this 
	 * repository does not contain matching policy set
	 */
	PolicySet getPolicySet(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest);
		
	/**
	 * Adds a new version of a given policy or policy set
	 * 
	 * @param policy a new version of the policy
	 */
	void add(CompositeDecisionRule policy);
}
