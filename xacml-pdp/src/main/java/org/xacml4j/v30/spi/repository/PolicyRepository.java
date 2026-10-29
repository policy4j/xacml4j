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

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.marshal.MarshallingProvider;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;


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

	/**
	 * The functions of the policies of this repository: the policies it loads, also through
	 * {@link #newImportTool()}, call them (change add-regex-engine-spi, design D3). The default is
	 * the standard functions of the marshalling providers, which every repository loaded with
	 * before.
	 *
	 * @return the function provider of this repository
	 */
	default FunctionProvider getFunctionProvider(){
		return MarshallingProvider.funcSystemProvider;
	}

	/**
	 * The combining algorithms of the policies of this repository (design D3)
	 *
	 * @return the algorithm provider of this repository
	 */
	default DecisionCombiningAlgorithmProvider getAlgorithmProvider(){
		return MarshallingProvider.combineSystemProvider;
	}

	/**
	 * Creates a policy import tool, which unmarshals policies with the functions and algorithms
	 * of this repository and adds them to it
	 *
	 * @return {@link PolicyImportTool}
	 */
	default PolicyImportTool newImportTool(){
		return new PolicyImportTool(this);
	}

	static Builder build(String id){
		return new Builder(id);
	}

	class Builder
	{
		private final String id;
		/** The functions, or the standard functions built when the repository is (design D3) */
		private Optional<FunctionProvider> functionProvider = Optional.empty();
		private DecisionCombiningAlgorithmProvider algorithmProvider = DecisionCombiningAlgorithmProvider
				.builder()
				.withDefaultAlgorithms()
				.build();

		public Builder(String id){
			this.id = Objects.requireNonNull(id, "id");
		}

		public Builder withFunctionProvider(FunctionProvider functionProvider){
			this.functionProvider = Optional.of(Objects.requireNonNull(functionProvider, "functionProvider"));
			return this;
		}

		/**
		 * The standard functions with the given regular-expression engine and profile (change
		 * add-regex-engine-spi, design D3); replaces a function provider given before
		 *
		 * @param engine the name of an engine on the class path, such as {@code jdk}
		 * @param profile the name of a profile the engine offers, {@code legacy} or {@code xf}
		 * @return this builder
		 * @throws IllegalArgumentException when no engine or profile has the name, or the engine
		 * does not offer the profile
		 * @throws IllegalStateException when more than one engine provider has the name
		 */
		public Builder withRegexEngine(String engine, String profile){
			return withFunctionProvider(FunctionProvider.builder()
					.withRegexEngine(engine, profile)
					.withDefaultFunctions()
					.build());
		}

		public Builder withAlgorithmProvider(DecisionCombiningAlgorithmProvider algorithmProvider){
			this.algorithmProvider = Objects.requireNonNull(algorithmProvider, "algorithmProvider");
			return this;
		}

		public PolicyRepository build(){
			return new InMemoryPolicyRepository(id,
					functionProvider.orElseGet(() -> FunctionProvider.builder().withDefaultFunctions().build()),
					algorithmProvider);
		}
	}
}
