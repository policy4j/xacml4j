package org.xacml4j.v20;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.PolicyDecisionPoint;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.spi.function.FunctionProviderBuilder;
import org.xacml4j.v30.spi.pip.PolicyInformationPointBuilder;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;

import com.google.common.collect.Iterables;

public class RSA2008InteropTest
{
	private static PolicyDecisionPoint pdp;

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
				.create());

		repository.importPolicy(getPolicy("XacmlPolicySet-01-top-level.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02a-CDA.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02b-N.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02c-N-PermCollections.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02d-prog-note.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02e-MA.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02f-emergency.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-03-N-RPS-med-rec-vrole.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-04-N-PPS-PRD-004.xml"));

		pdp = PolicyDecisionPointBuilder.builder("testPdp")
			.policyRepository(repository)
			.pip(
					PolicyInformationPointBuilder
					.builder("testPip")
					.withDefaultResolvers()
					.build())
			.rootPolicy(repository.importPolicy(getPolicy("XacmlPolicySet-01-top-level.xml")))
			.build();

	}

	@Test
	public void testCase1Request01() throws Exception
	{
		Result r = executeTest("XacmlRequest-01-01.xml");
		assertEquals(Decision.PERMIT, r.getDecision());
	}

	@Test
	public void testCase1Request02() throws Exception
	{

		Result r = executeTest("XacmlRequest-01-02.xml");
		assertEquals(Decision.DENY, r.getDecision());
	}

	@Test
	public void testCase1Request03() throws Exception
	{
		Result r = executeTest("XacmlRequest-01-03.xml");
		assertEquals(Decision.PERMIT, r.getDecision());
	}

	@Test
	public void testCase1Request04() throws Exception
	{
		Result r = executeTest("XacmlRequest-01-04.xml");
		assertEquals(Decision.DENY, r.getDecision());
	}

	@Test
	public void testCase2Request01() throws Exception
	{
		Result r = executeTest("XacmlRequest-02-01.xml");
		assertEquals(Decision.DENY, r.getDecision());
	}

	@Test
	public void testCase2Request02() throws Exception
	{
		Result r = executeTest("XacmlRequest-02-02.xml");
		assertEquals(Decision.DENY, r.getDecision());
	}

	@Test
	public void testCase2Request03() throws Exception
	{
		Result r = executeTest("XacmlRequest-02-03.xml");
		assertEquals(Decision.PERMIT, r.getDecision());
	}

	@Test
	public void testCase2Request04() throws Exception
	{
		Result r = executeTest("XacmlRequest-02-04.xml");
		assertEquals(Decision.DENY, r.getDecision());
	}

	@Test
	public void testCase3Request01() throws Exception
	{
		Result r = executeTest("XacmlRequest-03-01.xml");
		assertEquals(Decision.DENY, r.getDecision());
	}

	@Test
	public void testCase3Request02() throws Exception
	{
		Result r = executeTest("XacmlRequest-03-02.xml");
		assertEquals(Decision.PERMIT, r.getDecision());
	}

	@Test
	public void testCase3Request03() throws Exception
	{
		Result r = executeTest("XacmlRequest-03-03.xml");
		assertEquals(Decision.PERMIT, r.getDecision());
	}

	@Test
	public void testCase4Request01() throws Exception
	{
		Result r = executeTest("XacmlRequest-04-01.xml");
		assertEquals(Decision.DENY, r.getDecision());
	}

	@Test
	public void testCase4Request02() throws Exception
	{
		Result r = executeTest("XacmlRequest-04-02.xml");
		assertEquals(Decision.PERMIT, r.getDecision());
	}

	@Test
	public void testCase4Request03() throws Exception
	{
		Result r = executeTest("XacmlRequest-04-03.xml");
		assertEquals(Decision.PERMIT, r.getDecision());
	}

	@Test
	public void testCase4Request04() throws Exception
	{
		Result r = executeTest("XacmlRequest-04-04.xml");
		assertEquals(Decision.DENY, r.getDecision());
	}

	@Test
	public void testCase5Request01() throws Exception
	{
		Result r = executeTest("XacmlRequest-05-01.xml");
		assertEquals(Decision.PERMIT, r.getDecision());
	}

	@Test
	public void testCase5Request02() throws Exception
	{
		Result r = executeTest("XacmlRequest-05-02.xml");
		assertEquals(Decision.PERMIT, r.getDecision());
	}

	private static InputStream getPolicy(String name) throws Exception
	{
		return Xacml20TestUtility.getClasspathResource("rsa2008-interop/" + name);
	}

	private RequestContext getRequest(String name) throws Exception
	{
		return Xacml20TestUtility.getRequest("rsa2008-interop/" + name);
	}

	private Result executeTest(String name) throws Exception
	{
		RequestContext request = getRequest(name);
		ResponseContext response = null;
		int n = 1;
		long time = 0;
		for(int i = 0; i < n; i++){
			long start = System.nanoTime();
			response = pdp.decide(request);
			long end = System.nanoTime();
			time += (end - start);
		}
		System.out.printf("Test=\"%s\" avg execution took=\"%d\" " +
				"nano seconds and took=\"%d\" iterations\n", name, time/n, n);
		Result r = Iterables.getOnlyElement(response.getResults());
		return r;
	}
}
