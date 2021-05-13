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

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis.xacml.v20.jaxb.context.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.marshal.ResponseMarshaller;
import org.xacml4j.v30.marshal.jaxb.Xacml20ResponseContextMarshaller;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPointBuilder;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.common.base.Strings;


public class Xacml20ConformanceTest
{
	/** Log */
	private static final Logger LOG = LoggerFactory.getLogger(Xacml20ConformanceTest.class);

	private static ResponseMarshaller responseMarshaller;
	private static PolicyRepository repository;

	private PolicyDecisionPointBuilder pdpBuilder;

	@BeforeClass
	public static void init_static() throws Exception
	{
		repository = new InMemoryPolicyRepository(
				"testRepositoryId",
				FunctionProviderBuilder.builder()
				.defaultFunctions()
				.build(),
				DecisionCombiningAlgorithmProviderBuilder.builder()
				.withDefaultAlgorithms()
				.create());
		responseMarshaller = new Xacml20ResponseContextMarshaller();

		addAllPolicies(repository, "IIA", 22);
		addAllPolicies(repository, "IIB", 54);
		addAllPolicies(repository, "IIC", 233);
		addAllPolicies(repository, "IID", 30);

		addAllPolicies(repository, "IIIA", 28);
		addAllPolicies(repository, "IIIF", 7);
		addAllPolicies(repository, "IIIG", 7);

		addPolicy(repository, "IIE", "Policy.xml", 1);
		addPolicy(repository, "IIE", "PolicyId1.xml", 1);
		addPolicy(repository, "IIE", "PolicySetId1.xml", 1);

		addPolicy(repository, "IIE", "Policy.xml", 2);
		addPolicy(repository, "IIE", "PolicyId1.xml", 2);
		addPolicy(repository, "IIE", "PolicySetId1.xml", 2);

		addPolicy(repository, "IIE", "Policy.xml", 2);
		addPolicy(repository, "IIE", "PolicyId1.xml", 2);
		addPolicy(repository, "IIE", "PolicyId2.xml", 2);
	}

	@Before
	public void init(){
		this.pdpBuilder = PolicyDecisionPointBuilder.builder("testPdp")
		.pip(PolicyInformationPointBuilder.builder("testPip")
				.defaultResolvers()
				.resolverFromInstance(new Xacml20ConformanceAttributeResolver())
				.build())
		.policyRepository(repository);
	}

	@Test
	public void testIIATests() throws Exception
	{
		Set<Integer> skipTests = new HashSet<Integer>();
		skipTests.add(4);
		skipTests.add(5);
		executeXacmlConformanceTestCase(skipTests, "IIA", 22);
	}

	@Test
	public void testIIIATests() throws Exception
	{
		Set<Integer> skipTests = new HashSet<Integer>();
		skipTests.add(1);
		skipTests.add(2);
		skipTests.add(3);
		skipTests.add(4);
		skipTests.add(5);
		executeXacmlConformanceTestCase(skipTests, "IIIA", 28);
	}

	@Test
	public void testIIBTests() throws Exception
	{
		executeXacmlConformanceTestCase(Collections.<Integer>emptySet(), "IIB", 54);
	}

	@Test
	public void testIICTests() throws Exception
	{
		Set<Integer> skipTests = new HashSet<Integer>();
		skipTests.add(3);
		skipTests.add(12);
		skipTests.add(14);
		skipTests.add(20);
		skipTests.add(23);
		skipTests.add(28);
		skipTests.add(29);
		skipTests.add(32);
		skipTests.add(33);
		skipTests.add(54);
		skipTests.add(55);
		skipTests.add(88);
		skipTests.add(89);
		skipTests.add(92);
		skipTests.add(93);
		skipTests.add(98);
		skipTests.add(99);
		executeXacmlConformanceTestCase(skipTests, "IIC", 233);
	}

	@Test
	public void testIIDTests() throws Exception
	{
		Set<Integer> skipTests = new HashSet<Integer>();
		skipTests.add(29);
		executeXacmlConformanceTestCase(skipTests, "IID", 30);
	}

	@Test
	public void testIIETests() throws Exception
	{
		executeXacmlConformanceTestCase(Collections.<Integer>emptySet(), "IIE", 3);
	}

	@Test
	public void testIIIFTests() throws Exception
	{
		executeXacmlConformanceTestCase(Collections.<Integer>emptySet(), "IIIF", 7);
	}

	@Test
	public void testIIIGTests() throws Exception
	{
		Set<Integer> skipTests = new HashSet<Integer>();
		skipTests.add(1);
		skipTests.add(2);
		skipTests.add(3);
		skipTests.add(4);
		skipTests.add(5);
		executeXacmlConformanceTestCase(skipTests, "IIIG", 7);
	}

	@Test
	public void testExecuteIIIG006() throws Exception
	{
		executeTestCase("IIIG", 6);
	}

	private void executeXacmlConformanceTestCase(Set<Integer> exclude, final String testPrefix, int testCount) throws Exception
	{
		for(int i = 1; i < testCount; i++)
		{
			if (!exclude.contains(i)) {
				executeTestCase(testPrefix, i);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void executeTestCase(String testPrefix, int testCaseNum) throws Exception
	{
		final String name = testPrefix + Strings.padStart(Integer.toString(testCaseNum), 3, '0');
		RequestContext request = getRequest(testPrefix, testCaseNum);
		final PolicyDecisionPoint pdp = pdpBuilder
				.rootPolicy(
						repository.importPolicy(
								getPolicy(testPrefix, testCaseNum, "Policy.xml")))
				.build();
		long start = System.currentTimeMillis();
		ResponseContext response = pdp.decide(request);
		long end = System.currentTimeMillis();
		System.out.printf("Executing test=\"%s\", " +
				"execution took=\"%d\" milliseconds\n", name, (end - start));
		ResponseType actual = ((JAXBElement<ResponseType>)responseMarshaller.marshal(response)).getValue();
		Xacml20TestUtility.assertResponse(getResponse(testPrefix, testCaseNum), actual);
	}

	private static InputStream getPolicy(
			String prefix, int number, String suffix) throws Exception
	{
		String path = "oasis-xacml20-compat-test/" + createTestAssetName(prefix, number, suffix);
		return Xacml20TestUtility.getClasspathResource(path);
	}

	private static void addAllPolicies(PolicyRepository r,
	                                   String prefix, int count)
	{
		for (int i = 1; i < count; i++) {
			addPolicy(r, prefix, "Policy.xml", i);
		}
	}

	private static void addPolicy(PolicyRepository r, String prefix, String suffix, int index)
	{
		try{
			final InputStream policyStream = getPolicy(prefix, index, suffix);
			CompositeDecisionRule rule = r.importPolicy(policyStream);
			if (rule == null) {
				LOG.info("Could not load policy with prefix: {}, index: {}, suffix {}",
						new Object[]{prefix, index, suffix});
			}
		}catch(Exception e){
			LOG.info("Could not load policy with prefix: {}, index: {}, suffix {}",
					new Object[]{prefix, index, suffix, e});
		}
	}

	private static String createTestAssetName(String prefix, int testCaseNum, String suffix)
	{
		return prefix + Strings.padStart(Integer.toString(testCaseNum), 3, '0') + suffix;
	}

	private static ResponseType getResponse(String prefix, int num) throws Exception {
		return Xacml20TestUtility.getResponse("oasis-xacml20-compat-test/" + createTestAssetName(prefix, num, "Response.xml"));
	}

	private static RequestContext getRequest(String prefix, int number) throws Exception {
		return Xacml20TestUtility.getRequest("oasis-xacml20-compat-test/" + createTestAssetName(prefix, number, "Request.xml"));
	}

	@Test
	public void testName()
	{
		assertEquals("AA003B", createTestAssetName("AA", 3, "B"));
	}
}
