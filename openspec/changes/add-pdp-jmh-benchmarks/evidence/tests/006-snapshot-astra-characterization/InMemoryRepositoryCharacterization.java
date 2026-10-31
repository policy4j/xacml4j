package org.xacml4j.jmh.evidence;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import java.util.List;

import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;

/** Standalone characterization of production PDP code; no JUnit or JMH runner. */
public final class InMemoryRepositoryCharacterization
{
	private static int checks;

	public static void main(String[] args){
		FunctionProvider functions = FunctionProvider.builder().withDefaultFunctions().build();
		DecisionCombiningAlgorithmProvider algorithms =
				DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
		InMemoryPolicyRepository repository = new InMemoryPolicyRepository("characterization", functions, algorithms);
		Policy p10 = policy("1.0", algorithms);
		Policy p12 = policy("1.2", algorithms);
		Policy p11 = policy("1.1", algorithms);
		check(repository.add(p10) && repository.add(p12) && repository.add(p11), "policy additions");
		check(List.copyOf(repository.getPolicies("policy")).equals(List.of(p10, p11, p12)), "policy version order");
		check(repository.getPolicy("policy", null, null, null) == p12, "latest policy");
		check(repository.get("policy", Version.parse("1.0.0")) == p10, "version ordering equivalence");
		Policy duplicate = policy("1.1", algorithms);
		check(!repository.add(duplicate), "duplicate policy rejected");
		check(repository.get("policy", Version.parse("1.1")) == p11, "first policy retained");
		check(repository.getPolicy("policy", VersionMatch.parse("1.*"), VersionMatch.parse("1.1"),
				VersionMatch.parse("1.*")) == p11, "three policy constraints intersect");
		check(repository.getPolicy("policy", null, VersionMatch.parse("1.0"),
				VersionMatch.parse("1.2")) == null, "bounds use match predicates rather than numeric comparison");
		check(repository.getPolicies("policy", null, VersionMatch.parse("1.1"), null).equals(List.of(p11)),
				"filtered policy collection");
		check(repository.remove(duplicate), "remove policy by id and version");
		check(repository.get("policy", Version.parse("1.1")) == null, "removed policy absent");
		check(!repository.remove(duplicate), "second policy removal false");

		PolicySet s10 = policySet("1.0", algorithms);
		PolicySet s12 = policySet("1.2", algorithms);
		PolicySet s11 = policySet("1.1", algorithms);
		check(repository.add(s10) && repository.add(s12) && repository.add(s11), "policy-set additions");
		check(List.copyOf(repository.getPolicySets("set", null)).equals(List.of(s10, s11, s12)), "policy-set version order");
		check(repository.getPolicySet("set", null, null, null) == s12, "latest policy set");
		check(repository.get("set", Version.parse("1.0.0")) == s10, "exact policy-set lookup");
		PolicySet duplicateSet = policySet("1.1", algorithms);
		check(!repository.add(duplicateSet), "duplicate policy set rejected");
		check(repository.get("set", Version.parse("1.1")) == s11, "first policy set retained");
		check(repository.getPolicySet("set", VersionMatch.parse("1.*"), VersionMatch.parse("1.1"),
				VersionMatch.parse("1.*")) == s11, "three policy-set constraints intersect");
		check(repository.getPolicySets("set", null, null, VersionMatch.parse("1.1")).equals(List.of(s11)),
				"filtered policy-set collection");
		check(repository.remove(duplicateSet), "remove policy set by id and version");
		check(!repository.remove(duplicateSet), "second policy-set removal false");
		check(repository.get("missing", Version.parse("1.0")) == null, "missing exact lookup");
		try{
			repository.getPolicies("missing");
			throw new AssertionError("Expected known policy4j-dofm unknown-policy defect");
		}catch(NullPointerException expected){
			checks++;
		}
		try{
			repository.getPolicySets("missing", null);
			throw new AssertionError("Expected known policy4j-dofm unknown-policy-set defect");
		}catch(NullPointerException expected){
			checks++;
		}
		LoggerFactory.getLogger(InMemoryRepositoryCharacterization.class)
				.warn("CHARACTERIZATION PASS: {} checks; unknown-id exceptions are the known defect, not desired snapshot behavior", checks);
	}

	private static Policy policy(String version, DecisionCombiningAlgorithmProvider algorithms){
		return Policy.builder("policy").version(version).combiningAlgorithm(
				algorithms.getRuleAlgorithm("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides")).build();
	}

	private static PolicySet policySet(String version, DecisionCombiningAlgorithmProvider algorithms){
		return PolicySet.builder("set").version(version).withCombiningAlgorithm(
				algorithms.getPolicyAlgorithm("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides")).build();
	}

	private static void check(boolean result, String label){
		if(!result){
			throw new AssertionError(label);
		}
		checks++;
	}
}
