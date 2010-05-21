package com.artagon.xacml.v3.policy.jaxb;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis.xacml.v20.policy.PolicySetType;
import org.oasis.xacml.v20.policy.PolicyType;

import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.policy.impl.DefaultPolicyFactory;

public class JAXBPolicyMapperTest 
{
	private static JAXBContext context;
	private static Unmarshaller unmarshaller;
	private JAXBPolicyMapper mapper;
	
	@BeforeClass
	public static void init_static() throws Exception
	{
		try{
			context = JAXBContext.newInstance("org.oasis.xacml.v20.policy");
			unmarshaller = context.createUnmarshaller();
		}catch(JAXBException e){
			System.err.println(e.getMessage());
		}
	}
	
	public void init() throws Exception
	{
		PolicyFactory p = new DefaultPolicyFactory(, combiningAlgorithms);
		this.mapper = new JAXBPolicyMapper();
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private static <T> T getPolicy(String name) throws Exception
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("oasis-xacml20-compat-test/" + name);
		assertNotNull(stream);
		JAXBElement<T> policy = (JAXBElement<T>)context.createUnmarshaller().unmarshal(stream);
		assertNotNull(policy);
		return policy.getValue();
	}
	
	@Test
	public void testPolicyMapping() throws Exception
	{
		PolicyType policyF005 = getPolicy("IIIF005Policy.xml");
		PolicySetType policyF006 = getPolicy("IIIF006Policy.xml");
		PolicyType policyF007 = getPolicy("IIIF007Policy.xml");
	}
}
