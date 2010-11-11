package com.artagon.xacml.v3.spi.pip;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeDesignator;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;

public class AnnotatedResolverMethodParserTest 
{
	private AnnotatedResolverMethodParser p;
	
	@Before
	public void init(){
		this.p = new AnnotatedResolverMethodParser();
	}
	
	@Test
	public void testParseAttributeResolverWithKeys() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve1");
		assertNotNull(m);
		AttributeResolverDescriptor d = p.parse(m);
		assertEquals("Test", d.getName());
		assertEquals(AttributeCategories.parse("subject"), d.getCategory());
		assertEquals("issuer", d.getIssuer());
		assertEquals(30, d.getPreferreredCacheTTL());
	}
	
	@Test
	public void testParseAttributeResolverWithoutKeysWithContext() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve2");
		assertNotNull(m);
		AttributeResolverDescriptor d = p.parse(m);
		assertEquals("Test", d.getName());
		assertEquals(AttributeCategories.parse("subject"), d.getCategory());
		assertEquals("issuer", d.getIssuer());
		assertEquals(30, d.getPreferreredCacheTTL());
	}
	
	
	@XacmlAttributeResolverDescriptor(id="testId", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId1"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId2"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId3"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId4")
	})
	public Map<String, BagOfAttributeValues> resolve1(
			@XacmlAttributeDesignator(category="test", attributeId="aaaTTr", 
					dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeValues k1, 
			@XacmlAttributeDesignator(category="test", attributeId="aaaTTr", 
					dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeValues k2)
	{
		return null;
	}
	
	@XacmlAttributeResolverDescriptor(id="testId", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId1"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId2"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId3"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId4")
	})
	public Map<String, BagOfAttributeValues> resolve2(PolicyInformationPointContext context)
	{
		return null;
	}
	
	private static Method getMethod(Class<?> clazz, String name)
	{
		for(Method m : clazz.getDeclaredMethods()){
			if(m.getName().equals(name)){
				return m;
			}
		}
		return null;
	}
}
