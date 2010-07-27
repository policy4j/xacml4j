package com.artagon.xacml.v20;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DefaultEvaluationContextFactory;
import com.artagon.xacml.v3.DefaultPolicyFactory;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.artagon.xacml.v3.pdp.DefaultPolicyDecisionPoint;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.pip.DefaultPolicyInformationPoint;
import com.artagon.xacml.v3.spi.repository.DefaultPolicyStore;
import com.google.common.collect.Iterables;

public class RSA2008InteropTest 
{
	private PolicyUnmarshaller policyReader;
	private RequestUnmarshaller requestUnmarshaller;
	private  PolicyDecisionPoint pdp;
	private PolicyInformationPoint pip;
	
	@Before
	public void init() throws Exception
	{
		this.policyReader = new Xacml20PolicyUnmarshaller(new DefaultPolicyFactory());
		this.requestUnmarshaller = new Xacml20RequestUnmarshaller();
		DefaultPolicyStore store = new DefaultPolicyStore();
		store.addPolicy(getPolicy("XacmlPolicySet-01-top-level.xml"));
		store.addReferencedPolicy(getPolicy("XacmlPolicySet-02a-CDA.xml"));
		store.addReferencedPolicy(getPolicy("XacmlPolicySet-02b-N.xml"));
		store.addReferencedPolicy(getPolicy("XacmlPolicySet-02c-N-PermCollections.xml"));
		store.addReferencedPolicy(getPolicy("XacmlPolicySet-02d-prog-note.xml"));
		store.addReferencedPolicy(getPolicy("XacmlPolicySet-02e-MA.xml"));
		store.addReferencedPolicy(getPolicy("XacmlPolicySet-02f-emergency.xml"));
		store.addReferencedPolicy(getPolicy("XacmlPolicySet-03-N-RPS-med-rec-vrole.xml"));
		store.addReferencedPolicy(getPolicy("XacmlPolicySet-04-N-PPS-PRD-004.xml"));
		this.pip = new DefaultPolicyInformationPoint();
		this.pdp = new DefaultPolicyDecisionPoint(new DefaultEvaluationContextFactory(store, pip), store);
		
	}
	
	@Test
	public void testCase1Request01() throws Exception
	{
		Request request = getRequest("XacmlRequest-01-01.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase1Request02() throws Exception
	{
		Request request = getRequest("XacmlRequest-01-02.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase1Request03() throws Exception
	{
		Request request = getRequest("XacmlRequest-01-03.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase1Request04() throws Exception
	{
		Request request = getRequest("XacmlRequest-01-04.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase2Request01() throws Exception
	{
		Request request = getRequest("XacmlRequest-02-01.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase2Request02() throws Exception
	{
		Request request = getRequest("XacmlRequest-02-02.xml");
		Response response = pdp.decide(request);
		assertEquals(Decision.DENY, Iterables.getOnlyElement(response.getResults()).getDecision());
	}
	
	@Test
	public void testCase2Request03() throws Exception
	{
		Request request = getRequest("XacmlRequest-02-03.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase2Request04() throws Exception
	{
		Request request = getRequest("XacmlRequest-02-04.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase3Request01() throws Exception
	{
		Request request = getRequest("XacmlRequest-03-01.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase3Request02() throws Exception
	{
		Request request = getRequest("XacmlRequest-03-02.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase3Request03() throws Exception
	{
		Request request = getRequest("XacmlRequest-03-03.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase4Request01() throws Exception
	{
		Request request = getRequest("XacmlRequest-04-01.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase4Request02() throws Exception
	{
		Request request = getRequest("XacmlRequest-04-02.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase4Request03() throws Exception
	{
		Request request = getRequest("XacmlRequest-04-03.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase4Request04() throws Exception
	{
		Request request = getRequest("XacmlRequest-04-04.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.DENY, r.getDecision());
	}
	
	@Test
	public void testCase5Request01() throws Exception
	{
		Request request = getRequest("XacmlRequest-05-01.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	@Test
	public void testCase5Request02() throws Exception
	{
		Request request = getRequest("XacmlRequest-05-02.xml");
		Response response = pdp.decide(request);
		Result r = Iterables.getOnlyElement(response.getResults());
		assertEquals(Decision.PERMIT, r.getDecision());
	}
	
	private CompositeDecisionRule getPolicy(String name) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String path = "rsa2008-interop/" + name;
		return policyReader.unmarshall(cl.getResourceAsStream(path));
	}
	
	private Request getRequest(String name) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String path = "rsa2008-interop/" + name;
		return requestUnmarshaller.unmarshalRequest(cl.getResourceAsStream(path));
	}
}
