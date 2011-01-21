package com.artagon.xacml.v3.spi.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.xml.transform.Source;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.VersionMatch;
import com.artagon.xacml.v3.XacmlSyntaxException;


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
	 * Gets repository identifier
	 * 
	 * @return a unique repository identifier
	 */
	//String getId();
	
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
	 * Gets {@link Policy} or {@link PolicySet} via
	 * identifier and version
	 * 
	 * @param id an identifier
	 * @param v a version
	 * @return {@link CompositeDecisionRule} or <code>null</code>
	 * if no matching policy or policy set is found in
	 * this repository
	 */
	CompositeDecisionRule get(String id, Version v);
	
	/**
	 * Adds {@link Policy} or {@link PolicySet}
	 * to this repository
	 * 
	 * @param r a policy or policy set
	 */
	boolean add(CompositeDecisionRule r);
	
	/**
	 * Removes {@link Policy} or {@link PolicySet}
	 * from this repository
	 * 
	 * @param r a policy or policy set
	 * @return <code>true</code> if given
	 * policy or policy set was removed
	 * from this repository
	 */
	boolean remove(CompositeDecisionRule r);
	
	/**
	 * Imports a given policy from a given {@link Source}
	 * 
	 * @param source a policy source
	 * @return {@link CompositeDecisionRule}
	 * @exception XacmlSyntaxException
	 * @exception IOException
	 */
	CompositeDecisionRule importPolicy(InputStream source) 
		throws XacmlSyntaxException, IOException;
	
	/**
	 * Adds {@link PolicyRepositoryListener} to this repository
	 * 
	 * @param l a listener
	 */
	void addPolicyRepositoryListener(PolicyRepositoryListener l);
	
	/**
	 * Removes {@link PolicyRepositoryListener} from this repository
	 * 
	 * @param l a listener
	 */
	void removePolicyRepositoryListener(PolicyRepositoryListener l);
}
