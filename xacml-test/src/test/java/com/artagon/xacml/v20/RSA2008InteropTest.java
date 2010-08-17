package com.artagon.xacml.v20;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DefaultEvaluationContextFactory;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.artagon.xacml.v3.pdp.DefaultPolicyDecisionPoint;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v3.spi.DefaultPolicyDomain;
import com.artagon.xacml.v3.spi.InMemoryPolicyRepository;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.pip.DefaultPolicyInformationPoint;
import com.google.common.collect.Iterables;

public class RSA2008InteropTest 
{
	private static PolicyUnmarshaller policyReader;
	private static RequestUnmarshaller requestUnmarshaller;
	private static PolicyDecisionPoint pdp;
	private static PolicyInformationPoint pip;
	
	@BeforeClass
	public static void init() throws Exception
	{
		policyReader = new Xacml20PolicyUnmarshaller();
		requestUnmarshaller = new Xacml20RequestUnmarshaller();
		
		PolicyDomain store = new DefaultPolicyDomain("test");
		PolicyRepository repository = new InMemoryPolicyRepository();
		store.add(getPolicy("XacmlPolicySet-01-top-level.xml"));
		repository.add(getPolicy("XacmlPolicySet-01-top-level.xml"));
		repository.add(getPolicy("XacmlPolicySet-02a-CDA.xml"));
		repository.add(getPolicy("XacmlPolicySet-02b-N.xml"));
		repository.add(getPolicy("XacmlPolicySet-02c-N-PermCollections.xml"));
		repository.add(getPolicy("XacmlPolicySet-02d-prog-note.xml"));
		repository.add(getPolicy("XacmlPolicySet-02e-MA.xml"));
		repository.add(getPolicy("XacmlPolicySet-02f-emergency.xml"));
		repository.add(getPolicy("XacmlPolicySet-03-N-RPS-med-rec-vrole.xml"));
		repository.add(getPolicy("XacmlPolicySet-04-N-PPS-PRD-004.xml"));
		pip = new DefaultPolicyInformationPoint();
		pdp = new DefaultPolicyDecisionPoint(new DefaultEvaluationContextFactory(repository, pip), store);
		
	}
	
	@Test
	public void testCase1Request01() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-01-01.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase1Request02() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-01-02.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase1Request03() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-01-03.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase1Request04() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-01-04.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase2Request01() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-02-01.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase2Request02() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-02-02.xml");
		ResponseContext response = pdp.decide(request);
		assertEquals(Decision.DENY, Iterables.getOnlyElement(response.getResults()).getDecision());
	}
	
	@Test
	public void testCase2Request03() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-02-03.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase2Request04() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-02-04.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase3Request01() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-03-01.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase3Request02() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-03-02.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase3Request03() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-03-03.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase4Request01() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-04-01.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase4Request02() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-04-02.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase4Request03() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-04-03.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase4Request04() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-04-04.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase5Request01() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-05-01.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase5Request02() throws Exception
	{
		RequestContext request = getRequest("XacmlRequest-05-02.xml");
		ResponseContext response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	private static CompositeDecisionRule getPolicy(String name) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String path = "rsa2008-interop/" + name;
		return policyReader.unmarshal(cl.getResourceAsStream(path));
	}
	
	private RequestContext getRequest(String name) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String path = "rsa2008-interop/" + name;
		return requestUnmarshaller.unmarshal(cl.getResourceAsStream(path));
	}
}
