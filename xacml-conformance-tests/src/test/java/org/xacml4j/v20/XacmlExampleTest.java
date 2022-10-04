package org.xacml4j.v20;

/*
 * #%L
 * Xacml4J Conformance Tests
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.XacmlPolicyTestSupport;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPointBuilder;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

public class XacmlExampleTest extends XacmlPolicyTestSupport
{
	@Test
	public void testXacmlExample1() throws Exception {
		PolicyDecisionPoint pdp = buildPDP("Example_1/Rule_1.xml");
		verifyXacml20Response(
				pdp,
				"Example_1/Request.xml",
				"Example_1/Response.xml");
	}

	@Test
	public void testXacmlExample2() throws Exception {
		PolicyDecisionPoint pdp = buildPDP("Example_2/Policy_set.xml",
				"Example_2/Rule_1.xml",
				"Example_2/Rule_2.xml",
				"Example_2/Rule_3.xml",
				"Example_2/Rule_4.xml");
		verifyXacml20Response(
				pdp,
				"Example_2/Request.xml",
				"Example_2/Response.xml");
	}

	private PolicyDecisionPoint buildPDP(String ...policyResources) throws Exception
	{
		PolicyRepository repository = new InMemoryPolicyRepository(
				"tes-repository",
				FunctionProviderBuilder.builder()
				                       .defaultFunctions()
				                       .build(),
				DecisionCombiningAlgorithmProviderBuilder.builder()
				                                         .withDefaultAlgorithms()
				                                         .build());

		List<CompositeDecisionRule> policies = new ArrayList<CompositeDecisionRule>(policyResources.length);
		for (String policyResource : policyResources) {
			CompositeDecisionRule policy = repository.importPolicy(Xacml20TestUtility.getClasspathResource(policyResource));
			log.info("Policy: {}", policy);
			policies.add(policy);
		}

		return PolicyDecisionPointBuilder
				.builder("testPdp")
                .policyRepository(repository)
                .pip(
                        PolicyInformationPointBuilder
                                .builder("testPip")
                                .defaultResolvers()
                                .build())
                .rootPolicy(policies.get(0))
                .build();
	}
}
