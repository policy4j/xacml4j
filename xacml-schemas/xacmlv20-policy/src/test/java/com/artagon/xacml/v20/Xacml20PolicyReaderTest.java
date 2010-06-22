package com.artagon.xacml.v20;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.Iterator;

import org.junit.BeforeClass;
import org.junit.Test;

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.DefaultPolicyFactory;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.MatchAnyOf;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.XacmlPolicyReader;

public class Xacml20PolicyReaderTest 
{
	private static XacmlPolicyReader reader; 
	
	@BeforeClass
	public static void init_static() throws Exception
	{
		reader = new Xacml20PolicyReader(new DefaultPolicyFactory());
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends CompositeDecisionRule> T getPolicy(String name) throws Exception
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		assertNotNull(stream);
		return  (T)reader.getPolicy(stream);
	}
	
	
	@Test
	public void testPolicyIIIF005Mapping() throws Exception
	{
		Policy p0 = getPolicy("IIIF005Policy.xml");
		assertEquals("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIF005:policy", p0.getId());
		assertEquals("Policy for Conformance Test IIIF005.", p0.getDescription());
		assertNotNull(p0.getDefaults());
		assertEquals(XPathVersion.XPATH1, p0.getDefaults().getXPathVersion());
		assertNull(p0.getTarget());
		assertEquals(1, p0.getRules().size());
		Rule r = p0.getRules().get(0);
		assertEquals("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIF005:rule", r.getId());
		assertEquals("Julius Hibbert can read or write Bart Simpson's medical record.", r.getDescription());
		assertEquals(Effect.PERMIT, r.getEffect());
		assertNotNull(r.getTarget());
		assertNull(r.getCondition());
		Target target = r.getTarget();
		assertEquals(3, target.getAnyOf().size());
		Iterator<MatchAnyOf> it = target.getAnyOf().iterator();
		MatchAnyOf m0 = it.next();
		MatchAnyOf m1 = it.next();
		MatchAnyOf m2 = it.next();
		assertEquals(2, m0.getAllOf().size());
		assertEquals(1, m1.getAllOf().size());
		assertEquals(1, m2.getAllOf().size());
	}
	
	@Test
	public void testPolicyIIIF006Mapping() throws Exception
	{
		PolicySet p0 = getPolicy("IIIF006Policy.xml");
		assertNotNull(p0);
		assertEquals("urn:oasis:names:tc:xacml:2.0:conformance-test:IIIF006:policySet", p0.getId());
		assertEquals("Policy Set for Conformance Test IIIF006.", p0.getDescription());
		assertNotNull(p0.getDefaults());
		assertEquals(XPathVersion.XPATH1, p0.getDefaults().getXPathVersion());
		assertNotNull(p0.getTarget());
		assertEquals(1, p0.getDecisions().size());
	}
	
	@Test
	public void testPolicyIIIF007Mapping() throws Exception
	{
		Policy p = getPolicy("IIIF007Policy.xml");
		assertNotNull(p);
		
	}
	
	
	@Test
	public void testPolicyIIC231Mapping() throws Exception
	{
		Policy p = getPolicy("IIC231Policy.xml");
		assertNotNull(p);
		
	}
	
	@Test
	public void testFeatures001Policy() throws Exception
	{
		Policy p = getPolicy("001B-Policy.xml");
		assertEquals(5, p.getVariableDefinitions().size());
		assertNotNull(p.getVariableDefinition("VAR01"));
		assertNotNull(p.getVariableDefinition("VAR02"));
		assertNotNull(p.getVariableDefinition("VAR03"));
		assertNotNull(p.getVariableDefinition("VAR04"));
		assertNotNull(p.getVariableDefinition("VAR05"));
	}
	
	@Test(expected=PolicySyntaxException.class)
	public void testFeatures002Policy() throws Exception
	{
		Policy p = getPolicy("002B-Policy.xml");
		assertEquals(2, p.getVariableDefinitions().size());
		assertNotNull(p.getVariableDefinition("VAR01"));
		assertNotNull(p.getVariableDefinition("VAR02"));
	}

}
