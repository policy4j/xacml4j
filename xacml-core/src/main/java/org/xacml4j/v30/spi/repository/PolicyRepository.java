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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;



/**
 * Defines an interface for XACML policies repository.
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
	String getId();

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
	 * @return {@link Policy} or {@code null} if this
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
	 * @return {@link PolicySet} or {@code null} if this
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
	 * @return {@link CompositeDecisionRule} or {@code null}
	 * if no matching policy or policy set is found in
	 * this repository
	 */
	CompositeDecisionRule get(String id, Version v);

	/**
	 * Adds {@link Policy} or {@link PolicySet}
	 * to this repository
	 *
	 * @param r a policy or policy set
	 * @return {@code true} if
	 * policy was added successfully
	 */
	boolean add(CompositeDecisionRule r);

	/**
	 * Removes {@link Policy} or {@link PolicySet}
	 * from this repository
	 *
	 * @param r a policy or policy set
	 * @return {@code true} if given
	 * policy or policy set was removed
	 * from this repository
	 */
	boolean remove(CompositeDecisionRule r);

	/**
	 * Imports a given XACML policy from a given
	 * {@link InputStream}
	 *
	 * @param source a policy source
	 * @return {@link CompositeDecisionRule} an imported
	 * {@link Policy} or {@link PolicySet}
	 * @exception XacmlSyntaxException if an syntax error
	 * occurs while parsing XACML policy or policy set
	 * @exception IOException if an IO error occurs
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
