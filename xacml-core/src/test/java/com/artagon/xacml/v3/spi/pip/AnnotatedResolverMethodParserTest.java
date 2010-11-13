package com.artagon.xacml.v3.spi.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.sdk.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.XacmlAttributeDesignator;
import com.artagon.xacml.v3.sdk.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;

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
		
		AttributeDescriptor a1 = d.getAttribute("testId1");
		assertNotNull(a1);
		assertEquals("testId1", a1.getAttributeId());
		assertEquals(IntegerType.INTEGER, a1.getDataType());
		
		AttributeDescriptor a2 = d.getAttribute("testId2");
		assertNotNull(a2);
		assertEquals("testId2", a2.getAttributeId());
		assertEquals(BooleanType.BOOLEAN, a2.getDataType());
		
		AttributeDescriptor a3 = d.getAttribute("testId3");
		assertNotNull(a3);
		assertEquals("testId3", a3.getAttributeId());
		assertEquals(StringType.STRING, a3.getDataType());
		
		AttributeDescriptor a4 = d.getAttribute("testId4");
		assertNotNull(a4);
		assertEquals("testId4", a4.getAttributeId());
		assertEquals(DoubleType.DOUBLE, a4.getDataType());
		
		assertEquals(2, d.getKeysCount());
		
		AttributeDesignatorKey k0 = (AttributeDesignatorKey)d.getKeyAt(0);
		assertEquals(AttributeCategories.parse("test"), k0.getCategory());
		assertEquals("attr1", k0.getAttributeId());
		assertEquals(BooleanType.BOOLEAN, k0.getDataType());
		assertEquals(null, k0.getIssuer());
		
		AttributeDesignatorKey k1 = (AttributeDesignatorKey)d.getKeyAt(1);
		
		assertEquals(AttributeCategories.parse("test"), k1.getCategory());
		assertEquals("attr2", k1.getAttributeId());
		assertEquals(IntegerType.INTEGER, k1.getDataType());
		assertEquals("test", k1.getIssuer());
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
	
	@Test(expected=XacmlSyntaxException.class)
	public void testParseAttributeResolverWithoutParameters() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve3");
		assertNotNull(m);
		p.parse(m);		
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void testParseAttributeResolverWithKeyParametersFirst() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve4");
		assertNotNull(m);
		p.parse(m);		
	}
	
	
	@XacmlAttributeResolverDescriptor(id="testId", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId1"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#boolean", id="testId2"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#string", id="testId3"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#double", id="testId4")
	})
	public Map<String, BagOfAttributeValues> resolve1(
			@XacmlAttributeDesignator(category="test", attributeId="attr1", 
					dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeValues k1, 
			@XacmlAttributeDesignator(category="test", attributeId="attr2", 
					dataType="http://www.w3.org/2001/XMLSchema#integer", issuer="test") BagOfAttributeValues k2)
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
	
	@XacmlAttributeResolverDescriptor(id="testId", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId1"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId2"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId3"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId4")
	})
	public Map<String, BagOfAttributeValues> resolve3()
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
	public Map<String, BagOfAttributeValues> resolve4(@XacmlAttributeDesignator(category="test", attributeId="aaaTTr", 
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeValues k1, PolicyInformationPointContext context)
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
