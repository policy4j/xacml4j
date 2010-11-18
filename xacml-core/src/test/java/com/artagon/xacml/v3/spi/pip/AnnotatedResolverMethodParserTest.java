package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.AttributeReferenceKey;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.sdk.resolver.XacmlAttributeDescriptor;
import com.artagon.xacml.v3.sdk.resolver.XacmlAttributeDesignator;
import com.artagon.xacml.v3.sdk.resolver.XacmlAttributeResolverDescriptor;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;

public class AnnotatedResolverMethodParserTest 
{
	private AnnotatedResolverMethodParser p;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.p = new AnnotatedResolverMethodParser();
		this.context = createControl().createMock(EvaluationContext.class);
	}
	
	@Test
	public void testParseAttributeResolverWithKeys() throws Exception
	{
		AttributeDesignatorKey excpectedKey0 = new AttributeDesignatorKey(
				AttributeCategories.parse("test"), "attr1", BooleanType.BOOLEAN, null);
		
		AttributeDesignatorKey excpectedKey1 = new AttributeDesignatorKey(
				AttributeCategories.parse("test"), "attr2", IntegerType.INTEGER, "test");
		
		Method m = getMethod(this.getClass(), "resolve1");
		assertNotNull(m);
		
		expect(context.resolve(excpectedKey0)).andReturn(BooleanType.BOOLEAN.bagOf(BooleanType.BOOLEAN.create(false)));
		expect(context.resolve(excpectedKey1)).andReturn(IntegerType.INTEGER.bagOf(IntegerType.INTEGER.create(1)));
		replay(context);
		
		AttributeResolver r = p.parseAttributeResolver(this, m);
		AttributeResolverDescriptor d = r.getDescriptor();
		
		PolicyInformationPointContext pipContext = new DefaultPolicyInformationPointContext(context, d);
		
		r.resolve(pipContext);
		
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
		
		List<AttributeReferenceKey> keys = d.getKeyRefs();
		assertEquals(2, keys.size());
		
		assertEquals(excpectedKey0, keys.get(0));
		assertEquals(excpectedKey1, keys.get(1));
		
		
		

		
		verify(context);
	}
	
	@Test
	public void testParseAttributeResolverWithoutKeysWithContext() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve2");
		assertNotNull(m);
		replay(context);
		AttributeResolver r = p.parseAttributeResolver(this, m);
		AttributeResolverDescriptor d = r.getDescriptor();
		PolicyInformationPointContext pipContext = new DefaultPolicyInformationPointContext(context, d);
		r.resolve(pipContext);
		
		assertEquals("Test", d.getName());
		assertEquals(AttributeCategories.parse("subject"), d.getCategory());
		assertEquals("issuer", d.getIssuer());
		assertEquals(30, d.getPreferreredCacheTTL());
		verify(context);
		
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void testParseAttributeResolverWithoutParameters() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve3");
		assertNotNull(m);
		p.parseAttributeResolver(this, m);		
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void testParseAttributeResolverWithKeyParametersFirst() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve4");
		assertNotNull(m);
		p.parseAttributeResolver(this, m);		
	}
	
	@Test(expected=XacmlSyntaxException.class)
	public void testParseAttributeResolverWrongReturnType() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve5");
		assertNotNull(m);
		p.parseAttributeResolver(this, m);		
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
	
	@XacmlAttributeResolverDescriptor(id="testId", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId1"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId2"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId3"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId4")
	})
	public Collection<String> resolve5(@XacmlAttributeDesignator(category="test", attributeId="aaaTTr", 
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
