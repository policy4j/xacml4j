package com.artagon.xacml.v20;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
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

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DefaultEvaluationContextFactory;
import com.artagon.xacml.v3.DefaultPolicyFactory;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.RequestSyntaxException;
import com.artagon.xacml.v3.Response;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;
import com.artagon.xacml.v3.marshall.RequestUnmarshaller;
import com.artagon.xacml.v3.marshall.ResponseMarshaller;
import com.artagon.xacml.v3.pdp.DefaultPolicyDecisionPoint;
import com.artagon.xacml.v3.pdp.PolicyDecisionPoint;
import com.artagon.xacml.v3.spi.repository.InMemoryPolicyStore;

public class Xacml20ConformanceTest 
{
	private PolicyUnmarshaller policyReader;
	private RequestUnmarshaller requestUnmarshaller;
	private ResponseMarshaller responseMarshaller;
	private InMemoryPolicyStore store;
	private  PolicyDecisionPoint pdp;
	
	@Before
	public void init() throws Exception
	{
		this.policyReader = new Xacml20PolicyUnmarshaller(new DefaultPolicyFactory());
		this.responseMarshaller = new Xacml20ResponseMarshaller();
		this.requestUnmarshaller = new Xacml20RequestUnmarshaller();
		this.store = new InMemoryPolicyStore();
		this.pdp = new DefaultPolicyDecisionPoint(new DefaultEvaluationContextFactory(store), store);
		
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testIIE001() throws Exception
	{	
		InMemoryPolicyStore repository = new InMemoryPolicyStore();
		repository.addTopLevelPolicy(getPolicy("IIE", 1, "Policy.xml"));
		repository.addReferencedPolicy(getPolicy("IIE", 1, "PolicyId1.xml"));
		repository.addReferencedPolicy(getPolicy("IIE", 1, "PolicySetId1.xml"));
		Request request = getRequest("IIE", 1);
		PolicyDecisionPoint pdp = new DefaultPolicyDecisionPoint(new DefaultEvaluationContextFactory(repository), repository);
		Response response = pdp.decide(request);
		ResponseType expected = ((JAXBElement<ResponseType>)responseMarshaller.marshall(response)).getValue();
		Xacml20ConformanceUtility.assertResponse(expected, Xacml20ConformanceUtility.getResponse("IIE", 1));	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testIIE002() throws Exception
	{	
		InMemoryPolicyStore repository = new InMemoryPolicyStore();
		repository.addTopLevelPolicy(getPolicy("IIE", 2, "Policy.xml"));
		repository.addReferencedPolicy(getPolicy("IIE", 2, "PolicyId1.xml"));
		repository.addReferencedPolicy(getPolicy("IIE", 2, "PolicySetId1.xml"));
		Request request = getRequest("IIE", 2);
		PolicyDecisionPoint pdp = new DefaultPolicyDecisionPoint(new DefaultEvaluationContextFactory(repository), repository);
		Response response = pdp.decide(request);
		ResponseType expected = ((JAXBElement<ResponseType>)responseMarshaller.marshall(response)).getValue();
		Xacml20ConformanceUtility.assertResponse(expected, Xacml20ConformanceUtility.getResponse("IIE", 2));	
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testIIE003() throws Exception
	{	
		store.addTopLevelPolicy(getPolicy("IIE", 3, "Policy.xml"));
		store.addReferencedPolicy(getPolicy("IIE", 3, "PolicyId1.xml"));
		try{
			store.addReferencedPolicy(getPolicy("IIE", 3, "PolicyId2.xml"));
			fail();
		}catch(PolicySyntaxException e){	
		}
		Request request = getRequest("IIE", 3);
		Response response = pdp.decide(request);
		ResponseType expected = ((JAXBElement<ResponseType>)responseMarshaller.marshall(response)).getValue();
		Xacml20ConformanceUtility.assertResponse(expected, Xacml20ConformanceUtility.getResponse("IIE", 3));	
	}
			
	@SuppressWarnings("unchecked")
	private void executeXacmlConformanceTestCase(Set<Integer> exclude, final String testPrefix, int testCount) throws Exception
	{
		for(int i = 1; i < testCount; i++)
		{
			if(exclude.contains(i)){
				continue;
			}
			InMemoryPolicyStore repository = new InMemoryPolicyStore();
			repository.addTopLevelPolicy(getPolicy(testPrefix, i, "Policy.xml"));
			Request request = getRequest(testPrefix, i);
			this.pdp = new DefaultPolicyDecisionPoint(new DefaultEvaluationContextFactory(repository), repository);
			Response response = pdp.decide(request);
			ResponseType expected = ((JAXBElement<ResponseType>)responseMarshaller.marshall(response)).getValue();
			Xacml20ConformanceUtility.assertResponse(expected, Xacml20ConformanceUtility.getResponse(testPrefix, i));
		}
	}
	
	private Request getRequest(String prefix, int number) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String requestPath = "oasis-xacml20-compat-test/" + Xacml20ConformanceUtility.createTestAssetName(prefix, number, "Request.xml");
		return requestUnmarshaller.unmarshalRequest(cl.getResourceAsStream(requestPath));
	}
	
	private CompositeDecisionRule getPolicy(String prefix, int number, String sufix) throws Exception
	{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String path = "oasis-xacml20-compat-test/" + Xacml20ConformanceUtility.createTestAssetName(prefix, number, sufix);
		return policyReader.getPolicy(cl.getResourceAsStream(path));
	}
}
