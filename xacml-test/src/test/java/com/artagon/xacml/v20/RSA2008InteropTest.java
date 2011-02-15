package com.artagon.xacml.v20;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.RequestContext;
import com.artagon.xacml.v30.ResponseContext;
import com.artagon.xacml.v30.Result;
import com.artagon.xacml.v30.marshall.RequestUnmarshaller;
import com.artagon.xacml.v30.marshall.jaxb.Xacml20RequestContextUnmarshaller;
import com.artagon.xacml.v30.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v30.pdp.PolicyDecisionPointBuilder;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPoint;
import com.artagon.xacml.v30.spi.pip.PolicyInformationPointBuilder;
import com.artagon.xacml.v30.spi.repository.InMemoryPolicyRepository;
import com.artagon.xacml.v30.spi.repository.PolicyRepository;
import com.google.common.collect.Iterables;

public class RSA2008InteropTest 
{
	private static RequestUnmarshaller requestUnmarshaller;
	private static PolicyDecisionPoint pdp;
	
	@BeforeClass
	public static void init() throws Exception
	{
		requestUnmarshaller = new Xacml20RequestContextUnmarshaller();
				
		PolicyRepository repository = new InMemoryPolicyRepository();
	
		repository.importPolicy(getPolicy("XacmlPolicySet-01-top-level.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02a-CDA.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02b-N.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02c-N-PermCollections.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02d-prog-note.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02e-MA.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-02f-emergency.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-03-N-RPS-med-rec-vrole.xml"));
		repository.importPolicy(getPolicy("XacmlPolicySet-04-N-PPS-PRD-004.xml"));
		
		pdp = PolicyDecisionPointBuilder.builder()
			.withPolicyRepository(repository)
			.withPolicyInformationPoint(
					PolicyInformationPointBuilder
					.builder()
					.withDefaultResolvers().build())
			.withRootPolicy(repository.importPolicy(getPolicy("XacmlPolicySet-01-top-level.xml")))
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
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String path = "rsa2008-interop/" + name;
		return cl.getResourceAsStream(path);
	}
	
	private RequestContext getRequest(String name) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String path = "rsa2008-interop/" + name;
		return requestUnmarshaller.unmarshal(cl.getResourceAsStream(path));
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
