package com.artagon.xacml.v20;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis.xacml.v20.context.RequestType;
import org.oasis.xacml.v20.context.ResponseType;
import org.oasis.xacml.v20.policy.PolicySetType;
import org.oasis.xacml.v20.policy.PolicyType;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DefaultEvaluationContextFactory;
import com.artagon.xacml.v3.DefaultPolicyFactory;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.Result;
import com.artagon.xacml.v3.StatusCodeId;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v3.pdp.SimplePolicyDecisionPoint;
import com.google.common.collect.Iterables;

public class Xacml20ConformanceTest 
{
	private static JAXBContext context1;
	private static JAXBContext context2;
	private Xacml20PolicyMapper policyMapper;
	private Xacml20ContextMapper contextMapper;
	private PolicyDecisionPoint pdp;
	private EvaluationContextFactory factory;
	
	
	@BeforeClass
	public static void init_static() throws Exception
	{
		try{
			context1 = JAXBContext.newInstance("org.oasis.xacml.v20.policy");
			context2 = JAXBContext.newInstance("org.oasis.xacml.v20.context");
		}catch(JAXBException e){
			System.err.println(e.getMessage());
		}
	}
	
	@Before
	public void init() throws Exception
	{
		PolicyFactory policyFactory = new DefaultPolicyFactory();
		policyMapper = new Xacml20PolicyMapper(policyFactory);
		contextMapper = new Xacml20ContextMapper();
		this.factory = new DefaultEvaluationContextFactory();
		
	}
	
	@SuppressWarnings({"unchecked" })
	private static <T> T getPolicy(String name) throws Exception
	{
		System.out.println("Laading policy + " + name);
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		assertNotNull(stream);
		assertNotNull(context1);
		JAXBElement<T> e = (JAXBElement<T>)context1.createUnmarshaller().unmarshal(stream);
		assertNotNull(e);
		return e.getValue();
	}
	
	@SuppressWarnings({"unchecked" })
	private static <T> T getContext(String name) throws Exception
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		assertNotNull(stream);
		assertNotNull(context2);
		JAXBElement<T> e = (JAXBElement<T>)context2.createUnmarshaller().unmarshal(stream);
		assertNotNull(e);
		return e.getValue();
	}
	
	@Test
	public void testIIATests() throws Exception
	{	
		Set<Integer> skipTests = new HashSet<Integer>();
		skipTests.add(1);
		skipTests.add(2);
		skipTests.add(3);
		skipTests.add(4);
		skipTests.add(5);
		skipTests.add(6);
		executeXacmlConformanceTestCase(skipTests, "IIA", 22);	
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
		
	private void executeXacmlConformanceTestCase(Set<Integer> exclude, String testPrefix, int testCount) throws Exception
	{
		for(int i = 1; i < testCount; i++)
		{

			String policyPath = "oasis-xacml20-compat-test/" + Xacml20ConformanceUtility.createName(testPrefix, i, "Policy.xml");
			String requestPath = "oasis-xacml20-compat-test/" + Xacml20ConformanceUtility.createName(testPrefix, i, "Request.xml");
			String responsePath = "oasis-xacml20-compat-test/" + Xacml20ConformanceUtility.createName(testPrefix, i, "Response.xml");
			if(exclude.contains(i)){
				System.out.println("Skipping - " + policyPath);
				continue;
			}
			System.out.println("Executing - " + policyPath);
			Object policy = getPolicy(policyPath);
			RequestType request = getContext(requestPath);
			this.pdp = new SimplePolicyDecisionPoint(factory, policyMapper.create(policy));
			Response response = pdp.decide(contextMapper.create(request));
			ResponseType expected = getContext(responsePath);
			Xacml20ConformanceUtility.assertResponse(expected, contextMapper.create(response));
		}
	}
}
