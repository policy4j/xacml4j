package com.artagon.xacml.v30;


import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DefaultPolicyFactory;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.marshall.PolicyUnmarshaller;

public class Xacml30PolicyUnmarshallerTest 
{
	private static PolicyUnmarshaller reader; 
	
	@BeforeClass
	public static void init_static() throws Exception
	{
		reader = new Xacml30PolicyUnmarshaller(new DefaultPolicyFactory());
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends CompositeDecisionRule> T getPolicy(String name) throws Exception
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		assertNotNull(stream);
		return  (T)reader.unmarshall(stream);
	}
	
	
	@Test
	public void testPolicy1Mapping() throws Exception
	{
		Policy p0 = getPolicy("Policy1.xml");
	}
	
	

}

