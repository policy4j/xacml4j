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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.XacmlPolicyTestSupport;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.pdp.MetricsSupport;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyImportTool;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.codahale.metrics.CsvReporter;
import com.google.common.base.Supplier;

public class RSA2008InteropTest extends XacmlPolicyTestSupport
{
	private static PolicyDecisionPoint pdp;

	@BeforeClass
	public static void init() throws Exception
	{
		CsvReporter reporter = CsvReporter.forRegistry(MetricsSupport.getOrCreate())
	                .formatFor(Locale.US)
	                .convertRatesTo(TimeUnit.MILLISECONDS)
	                .convertDurationsTo(TimeUnit.MILLISECONDS)
	                .build(new File("target/"));
		reporter.start(1, TimeUnit.MILLISECONDS);


		PolicyRepository repository = new InMemoryPolicyRepository(
				"testId",
				FunctionProviderBuilder.builder()
				                       .withDefaultFunctions()
				                       .build(),
				DecisionCombiningAlgorithmProviderBuilder.builder()
				                                         .withDefaultAlgorithms()
				                                         .build());

		List<Supplier<InputStream>> policyStreams = Arrays.asList(
				_getPolicy("XacmlPolicySet-01-top-level.xml"),
				_getPolicy("XacmlPolicySet-02a-CDA.xml"),
				_getPolicy("XacmlPolicySet-02b-N.xml"),
				_getPolicy("XacmlPolicySet-02c-N-PermCollections.xml"),
				_getPolicy("XacmlPolicySet-02d-prog-note.xml"),
				_getPolicy("XacmlPolicySet-02e-MA.xml"),
				_getPolicy("XacmlPolicySet-02f-emergency.xml"),
				_getPolicy("XacmlPolicySet-03-N-RPS-med-rec-vrole.xml"),
				_getPolicy("XacmlPolicySet-04-N-PPS-PRD-004.xml"));

		List<CompositeDecisionRule> policies = new ArrayList<CompositeDecisionRule>();
		PolicyImportTool tool = repository.newImportTool();
		for (Supplier<InputStream> policyStream : policyStreams) {
			CompositeDecisionRule policy = tool.importPolicy(MediaType.Type.XACML20_XML, policyStream);
			policies.add(policy);
		}

		pdp = PolicyDecisionPointBuilder.builder("testPdp")
			.policyRepository(repository)
			.pip(
					PolicyInformationPoint
					.builder("testPip")
					.withDefaultRegistry()
					.noCacheProvider()
					.build())
			.rootPolicy(policies.get(0))
			.build();
	}

	@Test
	public void testCase1Request01() throws Exception {
		runTestCase(1, 1);
	}

	@Test
	public void testCase1Request02() throws Exception {
		runTestCase(1, 2);
	}

	@Test
	public void testCase1Request03() throws Exception {
		runTestCase(1, 3);
	}

	@Test
	public void testCase1Request04() throws Exception {
		runTestCase(1, 4);
	}

	@Test
	public void testCase2Request01() throws Exception
	{
		runTestCase(2, 1);
	}

	@Test
	public void testCase2Request02() throws Exception {
		runTestCase(2, 2);
	}

	@Test
	public void testCase2Request03() throws Exception {
		runTestCase(2, 3);
	}

	@Test
	public void testCase2Request04() throws Exception {
		runTestCase(2, 4);
	}

	@Test
	public void testCase3Request01() throws Exception {
		runTestCase(3, 1);
	}

	@Test
	public void testCase3Request02() throws Exception {
		runTestCase(3, 2);
	}

	@Test
	public void testCase3Request03() throws Exception {
		runTestCase(3, 3);
	}

	@Test
	public void testCase4Request01() throws Exception {
		runTestCase(4, 1);
	}

	@Test
	public void testCase4Request02() throws Exception {
		runTestCase(4, 2);
	}

	@Test
	public void testCase4Request03() throws Exception {
		runTestCase(4, 3);
	}

	@Test
	public void testCase4Request04() throws Exception {
		runTestCase(4, 4);
	}

	@Test
	public void testCase5Request01() throws Exception {
		runTestCase(5, 1);
	}

	@Test
	public void testCase5Request02() throws Exception {
		runTestCase(5, 2);
	}

	private void runTestCase(int caseId, int requestId) throws Exception {
		verifyXacml20Response(
				pdp,
				String.format("rsa2008-interop/XacmlRequest-%02d-%02d.xml", caseId, requestId),
				String.format("rsa2008-interop/XacmlResponse-%02d-%02d.xml", caseId, requestId));
	}

	private static Supplier<InputStream> _getPolicy(final String name) {
		return Xacml20TestUtility.getClasspathResource("rsa2008-interop/" + name);
	}
}
