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

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xacml4j.v30.*;
import org.xacml4j.v30.types.XacmlTypes;

import java.lang.reflect.Method;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;


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

		expect(context.resolve(eq(excpectedKey0))).andReturn(Optional.of(XacmlTypes.BOOLEAN.of(false).toBag()));
		expect(context.resolve(eq(excpectedKey1))).andReturn(Optional.of(XacmlTypes.INTEGER.of(1).toBag()));
		expect(context.getClock()).andReturn(Clock.systemUTC());

		control.replay();

		AttributeResolverDescriptor d = p.parseAttributeResolver(this, m);

		ResolverContext pipContext = ResolverContext.createContext(context, d);

		d.getResolver().apply(pipContext);

		assertEquals("Test", d.getName());
		assertEquals(CategoryId.parse("subject"), d.getCategory());
		assertEquals("issuer", d.getIssuer());
		assertEquals(30, d.getPreferredCacheTTL());

		AttributeDescriptor a1 = d.getAttribute("testId1").get();
		assertNotNull(a1);
		assertEquals("testId1", a1.getAttributeId());
		assertEquals(XacmlTypes.INTEGER, a1.getDataType());

		AttributeDescriptor a2 = d.getAttribute("testId2").get();
		assertNotNull(a2);
		assertEquals("testId2", a2.getAttributeId());
		assertEquals(XacmlTypes.BOOLEAN, a2.getDataType());

		AttributeDescriptor a3 = d.getAttribute("testId3").get();
		assertNotNull(a3);
		assertEquals("testId3", a3.getAttributeId());
		assertEquals(XacmlTypes.STRING, a3.getDataType());

		AttributeDescriptor a4 = d.getAttribute("testId4").get();
		assertNotNull(a4);
		assertEquals("testId4", a4.getAttributeId());
		assertEquals(XacmlTypes.DOUBLE, a4.getDataType());

		List<AttributeReferenceKey> keys = d.getAllContextKeyRefs();
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
		expect(context.getClock()).andReturn(Clock.systemUTC());
		replay(context);
		AttributeResolverDescriptor d = p.parseAttributeResolver(this, m);
		ResolverContext pipContext = ResolverContext.createContext(context, d);
		d.getResolver().apply(pipContext);

		assertEquals("Test", d.getName());
		assertEquals(CategoryId.parse("subject"), d.getCategory());
		assertEquals("issuer", d.getIssuer());
		assertEquals(30, d.getPreferredCacheTTL());
		verify(context);

	}

	@Test
	public void testParseAttributeResolverWithoutParameters()
	{
		Method m = getMethod(this.getClass(), "resolve3");
		assertNotNull(m);
		AttributeResolverDescriptor d = p.parseAttributeResolver(this, m);
		assertTrue(d.getAllContextKeyRefs().isEmpty());
	}

	@Test(expected= SyntaxException.class)
	public void testParseAttributeResolverWithKeyParametersFirst()
	{
		Method m = getMethod(this.getClass(), "resolve4");
		assertNotNull(m);
		p.parseAttributeResolver(this, m);
	}

	@Test(expected= SyntaxException.class)
	public void testParseAttributeResolverWrongReturnType()
	{
		Method m = getMethod(this.getClass(), "resolve5");
		assertNotNull(m);
		p.parseAttributeResolver(this, m);
	}

	@Test
	public void testParseContentResolver()
	{
		Method m = getMethod(this.getClass(), "resolveContent1");
		ContentResolverDescriptor r = p.parseContentResolver(this, m);
		assertTrue(r.canResolve(AttributeSelectorKey.builder()
		                                            .category("subject")
		                                            .jpath("aaaa").build()));
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
	public Map<String, BagOfAttributeValues> resolve2(ResolverContext context)
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
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeValues k1, ResolverContext context)
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
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeValues k1, ResolverContext context)
	{
		return null;
	}

	@XacmlContentResolverDescriptor(id="testId", name="Test", category="subject", cacheTTL=30)
	public Node resolveContent1(@XacmlAttributeDesignator(category="test", attributeId="aaaTTr",
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfAttributeValues k1)
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
