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

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.request.RequestContext;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.pip.DefaultPolicyInformationPointCacheProvider;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

public class RSA2008PerformanceTestCase extends XacmlPolicyTestSupport
{
	private static PolicyDecisionPoint pdp;
	private static RequestContext[][] requests;

	@BeforeClass
	public static void init() throws Exception
	{
		PolicyRepository repository = new InMemoryPolicyRepository(
				"testId",
				FunctionProviderBuilder.builder()
				                       .defaultFunctions()
				                       .build(),
				DecisionCombiningAlgorithmProviderBuilder.builder()
				                                         .withDefaultAlgorithms()
				                                         .build());

		ImmutableList<Supplier<InputStream>> policyStreams = ImmutableList.of(
				_getPolicy("XacmlPolicySet-01-top-level.xml"),
				_getPolicy("XacmlPolicySet-02a-CDA.xml"),
				_getPolicy("XacmlPolicySet-02b-N.xml"),
				_getPolicy("XacmlPolicySet-02c-N-PermCollections.xml"),
				_getPolicy("XacmlPolicySet-02d-prog-note.xml"),
				_getPolicy("XacmlPolicySet-02e-MA.xml"),
				_getPolicy("XacmlPolicySet-02f-emergency.xml"),
				_getPolicy("XacmlPolicySet-03-N-RPS-med-rec-vrole.xml"),
				_getPolicy("XacmlPolicySet-04-N-PPS-PRD-004.xml"));

		ImmutableList.Builder<CompositeDecisionRule> policies = ImmutableList.builder();
		for (Supplier<InputStream> policyStream : policyStreams) {
			policies.add(repository.importPolicy(policyStream));
		}

		pdp = PolicyDecisionPointBuilder.builder("testPdp")
			.policyRepository(repository)
			.pip(
					PolicyInformationPointBuilder
					.builder("testPip")
					.defaultResolvers()
					.withCacheProvider(new DefaultPolicyInformationPointCacheProvider(0, 0))
					.build())
			.rootPolicy(policies.build().get(0))
			.build();

		requests = new RequestContext[5][4];
	}

	private void addTestCase(int caseId, int requestId) throws Exception {
		requests[caseId-1][requestId-1] = getXacml20Request(
			String.format("rsa2008-interop/XacmlRequest-%02d-%02d.xml", caseId, requestId));
	}

	@Test
	public void testStuff() throws Exception {
		addTestCase(1, 1);
		addTestCase(1, 2);
		addTestCase(1, 3);
		addTestCase(1, 4);
		addTestCase(2, 1);
		addTestCase(2, 2);
		addTestCase(2, 3);
		addTestCase(2, 4);
		addTestCase(3, 1);
		addTestCase(3, 2);
		addTestCase(3, 3);
		addTestCase(4, 1);
		addTestCase(4, 2);
		addTestCase(4, 3);
		addTestCase(4, 4);
		addTestCase(5, 1);
		addTestCase(5, 2);

		long t1 = System.currentTimeMillis();
		for(int i = 0; i < 1000000; i++) {
			runTestCase(1, 1);
			runTestCase(1, 2);
			runTestCase(1, 3);
			runTestCase(1, 4);
			runTestCase(2, 1);
			runTestCase(2, 2);
			runTestCase(2, 3);
			runTestCase(2, 4);
			runTestCase(3, 1);
			runTestCase(3, 2);
			runTestCase(3, 3);
			runTestCase(4, 1);
			runTestCase(4, 2);
			runTestCase(4, 3);
			runTestCase(4, 4);
			runTestCase(5, 1);
			runTestCase(5, 2);
		}
		long t2 = System.currentTimeMillis();

		System.err.println("TimeValue taken: " + (t2-t1));
	}

	private void runTestCase(int caseId, int requestId) throws Exception {
		RequestContext req = requests[caseId-1][requestId-1];
		assertNotNull(req);
		ResponseContext resp = pdp.decide(req);
		assertNotNull(resp);
	}

	private static Supplier<InputStream> _getPolicy(final String name) {
		return Xacml20TestUtility.getClasspathResource("rsa2008-interop/" + name);
	}
}
