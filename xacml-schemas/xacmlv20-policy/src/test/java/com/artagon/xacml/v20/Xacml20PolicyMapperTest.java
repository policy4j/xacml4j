package com.artagon.xacml.v20;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis.xacml.v20.policy.PolicySetType;
import org.oasis.xacml.v20.policy.PolicyType;

import com.artagon.xacml.v3.DefaultPolicyFactory;
import com.artagon.xacml.v3.Effect;
import com.artagon.xacml.v3.MatchAnyOf;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.Rule;
import com.artagon.xacml.v3.Target;
import com.artagon.xacml.v3.XPathVersion;

public class Xacml20PolicyMapperTest 
{
	private static JAXBContext context;
	private Xacml20PolicyMapper mapper;
	
	@BeforeClass
	public static void init_static() throws Exception
	{
		try{
			context = JAXBContext.newInstance("org.oasis.xacml.v20.policy");
		}catch(JAXBException e){
			System.err.println(e.getMessage());
		}
	}
	
	@Before
	public void init() throws Exception
	{
		PolicyFactory policyFactory = new DefaultPolicyFactory();
		mapper = new Xacml20PolicyMapper(policyFactory);
	}
	
	@SuppressWarnings({"unchecked" })
	private static <T> T getPolicy(String name) throws Exception
	{
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		assertNotNull(stream);
		assertNotNull(context);
		JAXBElement<T> policy = (JAXBElement<T>)context.createUnmarshaller().unmarshal(stream);
		assertNotNull(policy);
		return policy.getValue();
	}
	
	
	@Test
	public void testPolicyIIIF005Mapping() throws Exception
	{
		PolicyType policy = getPolicy("IIIF005Policy.xml");
		Policy p0 = mapper.create(policy);
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
		PolicySetType policy = getPolicy("IIIF006Policy.xml");
		PolicySet p0 = mapper.create(policy);
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
		PolicyType policy = getPolicy("IIIF007Policy.xml");
		Policy p = mapper.create(policy);
		assertNotNull(p);
		
	}
	
	
	@Test
	public void testPolicyIIC231Mapping() throws Exception
	{
		PolicyType policy = getPolicy("IIC231Policy.xml");
		Policy p = mapper.create(policy);
		assertNotNull(p);
		
	}
	
	@Test
	public void testFeatures001Policy() throws Exception
	{
		PolicyType policy = getPolicy("001B-Policy.xml");
		Policy p = mapper.create(policy);
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
		PolicyType policy = getPolicy("002B-Policy.xml");
		Policy p = mapper.create(policy);
		assertEquals(2, p.getVariableDefinitions().size());
		assertNotNull(p.getVariableDefinition("VAR01"));
		assertNotNull(p.getVariableDefinition("VAR02"));
	}

	@Test
	public void testXpathTransformation() throws Exception
	{
		assertEquals("//md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("//Request/Resource/ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("//md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("//Resource/ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("//md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("//xacml-context:Request/xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("/xacml-context:Request/xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("//md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("//xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("/xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("//md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("//xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("//md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("//md:record/md:patient/md:patient-number/text()"));
		assertEquals("/md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("/md:record/md:patient/md:patient-number/text()"));
		
		assertEquals("./md:record/md:patient/md:patient-number/text()", Xacml20PolicyMapper.transform20PathTo30("./xacml-context:Resource/xacml-context:ResourceContent/md:record/md:patient/md:patient-number/text()"));
		
	}
}
