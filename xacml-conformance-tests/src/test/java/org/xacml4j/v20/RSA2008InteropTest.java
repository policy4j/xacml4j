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
import org.xacml4j.v30.pdp.MetricsSupport;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPointBuilder;
import org.xacml4j.v30.spi.repository.ImmutablePolicySource;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.codahale.metrics.CsvReporter;
import com.google.common.base.Supplier;
import org.xacml4j.v30.spi.repository.PolicySource;

public class RSA2008InteropTest extends XacmlPolicyTestSupport
{
	private static PolicyDecisionPoint pdp;

	@BeforeClass
	public static void init() throws Exception
	{

		PolicySource source  = ImmutablePolicySource
                .builder("testPolicySource")
                .policyFromClasspath("rsa2008-interop/XacmlPolicySet-01-top-level.xml")
                .policyFromClasspath("rsa2008-interop/XacmlPolicySet-02a-CDA.xml")
                .policyFromClasspath("rsa2008-interop/XacmlPolicySet-02b-N.xml")
                .policyFromClasspath("rsa2008-interop/XacmlPolicySet-02c-N-PermCollections.xml")
                .policyFromClasspath("rsa2008-interop/XacmlPolicySet-02d-prog-note.xml")
                .policyFromClasspath("rsa2008-interop/XacmlPolicySet-02e-MA.xml")
                .policyFromClasspath("rsa2008-interop/XacmlPolicySet-02f-emergency.xml")
                .policyFromClasspath("rsa2008-interop/XacmlPolicySet-03-N-RPS-med-rec-vrole.xml")
                .policyFromClasspath("rsa2008-interop/XacmlPolicySet-04-N-PPS-PRD-004.xml")
                .build();

		pdp = PolicyDecisionPointBuilder.builder("testPdp")
             .rootPolicy("urn:va:xacml:2.0:interop:rsa8:policysetid:toplevel", "1.0")
			.policyResolver(source.createResolver())
			.pip(
					PolicyInformationPointBuilder
					.builder("testPip")
					.defaultResolvers()
					.build())

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
}
