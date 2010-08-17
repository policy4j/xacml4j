package com.artagon.xacml.v20;

import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBElement;

import org.junit.Before;
import org.junit.Test;
import org.oasis.xacml.v20.jaxb.context.ResponseType;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DefaultEvaluationContextFactory;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.RequestContext;
import com.artagon.xacml.v3.ResponseContext;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.artagon.xacml.v3.marshall.ResponseMarshaller;
import com.artagon.xacml.v3.pdp.DefaultPolicyDecisionPoint;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v3.spi.InMemoryPolicyRepository;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.spi.PolicyDomain;
import com.artagon.xacml.v3.spi.PolicyRepository;
import com.artagon.xacml.v3.spi.pip.DefaultPolicyInformationPoint;
import com.artagon.xacml.v3.spi.store.DefaultPolicyStore;

public class Xacml20ConformanceTest 
{
	private PolicyUnmarshaller policyReader;
	private RequestUnmarshaller requestUnmarshaller;
	private ResponseMarshaller responseMarshaller;
	private PolicyDomain store;
	private  PolicyDecisionPoint pdp;
	private PolicyInformationPoint pip;
	
	@Before
	public void init() throws Exception
	{
		this.policyReader = new Xacml20PolicyUnmarshaller();
		this.responseMarshaller = new Xacml20ResponseMarshaller();
		this.requestUnmarshaller = new Xacml20RequestUnmarshaller();
		this.store = new DefaultPolicyStore();
		this.pip = new DefaultPolicyInformationPoint();
		this.pdp = new DefaultPolicyDecisionPoint(new DefaultEvaluationContextFactory(store, pip), store);
		
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
	
	
	public void testRSA2008() throws Exception
	{
			
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testIIE001() throws Exception
	{	
		store.add(getPolicy("IIE", 1, "Policy.xml"));
		store.add(getPolicy("IIE", 1, "PolicyId1.xml"), false);
		store.add(getPolicy("IIE", 1, "PolicySetId1.xml"), false);
		RequestContext request = getRequest("IIE", 1);
		ResponseContext response = pdp.decide(request);
		ResponseType expected = ((JAXBElement<ResponseType>)responseMarshaller.marshal(response)).getValue();
		Xacml20ConformanceUtility.assertResponse(expected, Xacml20ConformanceUtility.getResponse("IIE", 1));	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testIIE002() throws Exception
	{	
		store.add(getPolicy("IIE", 2, "Policy.xml"));
		store.add(getPolicy("IIE", 2, "PolicyId1.xml"), false);
		store.add(getPolicy("IIE", 2, "PolicySetId1.xml"), false);
		RequestContext request = getRequest("IIE", 2);
		ResponseContext response = pdp.decide(request);
		ResponseType expected = ((JAXBElement<ResponseType>)responseMarshaller.marshal(response)).getValue();
		Xacml20ConformanceUtility.assertResponse(expected, Xacml20ConformanceUtility.getResponse("IIE", 2));	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testIIE003() throws Exception
	{	
		store.add(getPolicy("IIE", 3, "Policy.xml"));
		store.add(getPolicy("IIE", 3, "PolicyId1.xml"), false);
		try{
			store.add(getPolicy("IIE", 3, "PolicyId2.xml"), false);
			fail();
		}catch(XacmlSyntaxException e){	
		}
		RequestContext request = getRequest("IIE", 3);
		ResponseContext response = pdp.decide(request);
		ResponseType expected = ((JAXBElement<ResponseType>)responseMarshaller.marshal(response)).getValue();
		Xacml20ConformanceUtility.assertResponse(expected, Xacml20ConformanceUtility.getResponse("IIE", 3));	
	}
	
	@Test
	public void testIIC168() throws Exception
	{
		executeTestCase("IIC", 168);
	}
			
	private void executeXacmlConformanceTestCase(Set<Integer> exclude, final String testPrefix, int testCount) throws Exception
	{
		for(int i = 1; i < testCount; i++)
		{
			if(exclude.contains(i)){
				continue;
			}
			executeTestCase(testPrefix, i);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void executeTestCase(String testPrefix, int testCaseNum) throws Exception
	{
		System.out.printf("Executing test=\"%s\"\n", Xacml20ConformanceUtility.createTestAssetName(testPrefix, testCaseNum, "Policy.xml"));
		PolicyDomain store = new DefaultPolicyStore();
		store.add(getPolicy(testPrefix, testCaseNum, "Policy.xml"));
		RequestContext request = getRequest(testPrefix, testCaseNum);
		this.pdp = new DefaultPolicyDecisionPoint(new DefaultEvaluationContextFactory(store, pip), store);
		ResponseContext response = pdp.decide(request);
		ResponseType actual = ((JAXBElement<ResponseType>)responseMarshaller.marshal(response)).getValue();
		Xacml20ConformanceUtility.assertResponse(Xacml20ConformanceUtility.getResponse(testPrefix, testCaseNum), actual);
	}
	
	private RequestContext getRequest(String prefix, int number) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String requestPath = "oasis-xacml20-compat-test/" + Xacml20ConformanceUtility.createTestAssetName(prefix, number, "Request.xml");
		return requestUnmarshaller.unmarshal(cl.getResourceAsStream(requestPath));
	}
	
	@SuppressWarnings("unchecked")
	private <T extends CompositeDecisionRule> T getPolicy(String prefix, int number, String sufix) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String path = "oasis-xacml20-compat-test/" + Xacml20ConformanceUtility.createTestAssetName(prefix, number, sufix);
		InputStream in = cl.getResourceAsStream(path);
		if(in == null){
			return null;
		}
		return (T)policyReader.unmarshal(in);
	}
	
	@Test
	public void addAllPolicies() throws Exception
	{
		PolicyRepository r = new InMemoryPolicyRepository();
		for(int i = 1; i < 233; i++)
		{
			try{
				CompositeDecisionRule rule = getPolicy("IIC", i, "Policy.xml");
				if(rule == null){
					continue;
				}
				if(rule instanceof Policy){
					r.add((Policy)rule);
				}
				if(rule instanceof PolicySet){
					r.add((PolicySet)rule);
				}
			}catch(XacmlSyntaxException e){
				
			}
		}
	}
}
