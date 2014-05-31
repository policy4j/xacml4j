package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Ticker;


public class AnnotatedResolverFactoryTest
{
	private AnnotatedResolverFactory p;
	private EvaluationContext context;
	private IMocksControl control;

	@Before
	public void init(){
		this.p = new AnnotatedResolverFactory();
		this.control = createControl();
		this.context = control.createMock(EvaluationContext.class);
	}

	@Test
	public void testParseAttributeResolverWithKeys() throws Exception
	{
		AttributeDesignatorKey excpectedKey0 =
				AttributeDesignatorKey.builder()
				.category("test")
				.attributeId("attr1")
				.dataType(XacmlTypes.BOOLEAN)
				.build();

		AttributeDesignatorKey excpectedKey1 =
				AttributeDesignatorKey.builder()
				.category("test")
				.attributeId("attr2")
				.issuer("test")
				.dataType(XacmlTypes.INTEGER)
				.build();

		Method m = getMethod(this.getClass(), "resolve1");
		assertNotNull(m);

		expect(context.resolve(eq(excpectedKey0))).andReturn(BooleanExp.valueOf(false).toBag());
		expect(context.resolve(eq(excpectedKey1))).andReturn(IntegerExp.valueOf(1).toBag());
		expect(context.getTicker()).andReturn(Ticker.systemTicker());

		control.replay();

		AttributeResolver r = p.parseAttributeResolver(this, m);
		AttributeResolverDescriptor d = r.getDescriptor();

		ResolverContext pipContext = new DefaultResolverContext(context, d);

		r.resolve(pipContext);

		assertEquals("Test", d.getName());
		assertEquals(Categories.parse("subject"), d.getCategory());
		assertEquals("issuer", d.getIssuer());
		assertEquals(30, d.getPreferredCacheTTL());

		AttributeDescriptor a1 = d.getAttribute("testId1");
		assertNotNull(a1);
		assertEquals("testId1", a1.getAttributeId());
		assertEquals(XacmlTypes.INTEGER, a1.getDataType());

		AttributeDescriptor a2 = d.getAttribute("testId2");
		assertNotNull(a2);
		assertEquals("testId2", a2.getAttributeId());
		assertEquals(XacmlTypes.BOOLEAN, a2.getDataType());

		AttributeDescriptor a3 = d.getAttribute("testId3");
		assertNotNull(a3);
		assertEquals("testId3", a3.getAttributeId());
		assertEquals(XacmlTypes.STRING, a3.getDataType());

		AttributeDescriptor a4 = d.getAttribute("testId4");
		assertNotNull(a4);
		assertEquals("testId4", a4.getAttributeId());
		assertEquals(XacmlTypes.DOUBLE, a4.getDataType());

		List<AttributeReferenceKey> keys = d.getKeyRefs();
		assertEquals(2, keys.size());

		assertEquals(excpectedKey0, keys.get(0));
		assertEquals(excpectedKey1, keys.get(1));



		control.verify();
	}

	@Test
	public void testParseAttributeResolverWithoutKeysWithContext() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve2");
		assertNotNull(m);
		expect(context.getTicker()).andReturn(Ticker.systemTicker());
		replay(context);
		AttributeResolver r = p.parseAttributeResolver(this, m);
		AttributeResolverDescriptor d = r.getDescriptor();
		ResolverContext pipContext = new DefaultResolverContext(context, d);
		r.resolve(pipContext);

		assertEquals("Test", d.getName());
		assertEquals(Categories.parse("subject"), d.getCategory());
		assertEquals("issuer", d.getIssuer());
		assertEquals(30, d.getPreferredCacheTTL());
		verify(context);

	}

	@Test
	public void testParseAttributeResolverWithoutParameters() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve3");
		assertNotNull(m);
		AttributeResolver r = p.parseAttributeResolver(this, m);
		assertTrue(r.getDescriptor().getKeyRefs().isEmpty());
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

	@Test
	public void testParseContentResolver() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolveContent1");
		ContentResolver r = p.parseContentResolver(this, m);
		assertTrue(r.getDescriptor().canResolve(Categories.parse("subject")));
	}


	@XacmlAttributeResolverDescriptor(id="testId", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId1"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#boolean", id="testId2"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#string", id="testId3"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#double", id="testId4")
	})
	public Map<String, BagOfAttributeExp> resolve1(
			@XacmlAttributeDesignator(category="test", attributeId="attr1",
					dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeExp k1,
			@XacmlAttributeDesignator(category="test", attributeId="attr2",
					dataType="http://www.w3.org/2001/XMLSchema#integer", issuer="test") BagOfAttributeExp k2)
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
	public Map<String, BagOfAttributeExp> resolve2(ResolverContext context)
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
	public Map<String, BagOfAttributeExp> resolve3()
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
	public Map<String, BagOfAttributeExp> resolve4(@XacmlAttributeDesignator(category="test", attributeId="aaaTTr",
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeExp k1, ResolverContext context)
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
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeExp k1, ResolverContext context)
	{
		return null;
	}

	@XacmlContentResolverDescriptor(id="testId", name="Test", category="subject", cacheTTL=30)
	public Node resolveContent1(@XacmlAttributeDesignator(category="test", attributeId="aaaTTr",
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeExp k1)
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
