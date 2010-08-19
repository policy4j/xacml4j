package com.artagon.xacml.v3.spi;


import java.util.Collection;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyReferenceResolver;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.VersionMatch;

/**
 * A repository for XACML policies, implements
 * {@link PolicyReferenceResolver} to resolve
 * policies referenced by the top level policies
 * in the {@link PolicyDomain}
 * 
 * @author Giedrius Trumpickas
 */
public interface PolicyRepository extends PolicyReferenceResolver
{
	/**
	 * Queries policy repository for a given
	 * policy via policy identifier and version
	 * match constraint
	 * 
	 * @param id a policy identifier
	 * @param version a policy version match constraint
	 * @return an {@link Iterable} to found policies
	 */
	Collection<Policy> getPolicies(String id, VersionMatch version);
	
	Collection<Policy> getPolicies(String id);
	
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
	Collection<PolicySet> getPolicySets(String id, VersionMatch version);
	
	
	
	Collection<PolicySet> getPolicySets(String id, VersionMatch version, 
			VersionMatch earliest, VersionMatch latest);
	
	/**
	 * Adds a new version of a given policy
	 * 
	 * @param policy a new version of the policy
	 */
	void add(CompositeDecisionRule policy);
	
}
