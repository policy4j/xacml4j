package com.artagon.xacml.v3.policy.jaxb;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis.xacml.v20.policy.PolicySetType;
import org.oasis.xacml.v20.policy.PolicyType;

import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.policy.impl.DefaultPolicyFactory;
import com.artagon.xacml.v3.policy.impl.combine.DefaultDecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.policy.spi.function.DefaultFunctionProvidersRegistry;

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
	
	@Before
	public void init() throws Exception
	{
		PolicyFactory policyFactory = new DefaultPolicyFactory(
				new DefaultFunctionProvidersRegistry(), new DefaultDecisionCombiningAlgorithmProvider());
		mapper = new JAXBPolicyMapper(policyFactory);
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
		Policy p = mapper.create(policyF005);
		System.out.println(p.toString());
	}
}
