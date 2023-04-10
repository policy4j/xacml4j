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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableMap;
import com.google.common.truth.Truth8;


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
		AttributeDesignatorKey expectedKey0 =
				AttributeDesignatorKey.builder()
				.category("test")
				.attributeId("attr1")
				.dataType(XacmlTypes.BOOLEAN)
				.build();

		AttributeDesignatorKey expectedKey1 =
				AttributeDesignatorKey.builder()
				.category("test")
				.attributeId("attr2")
				.issuer("test")
				.dataType(XacmlTypes.INTEGER)
				.build();

		Method m = getMethod(this.getClass(), "resolve1");
		assertNotNull(m);

		expect(context.resolve(AttributeDesignatorKey.builder()
		                                             .category(CategoryId.of("test"))
		                                             .dataType(XacmlTypes.BOOLEAN)
				                       .attributeId("attr1").build()))
				.andReturn(Optional.of(XacmlTypes.BOOLEAN.of(true).toBag()));
		expect(context.resolve(AttributeDesignatorKey.builder()
		                                             .category(CategoryId.of("test"))
		                                             .issuer("test")
		                                             .dataType(XacmlTypes.INTEGER)
		                                             .attributeId("attr2").build()))
				.andReturn(Optional.of(XacmlTypes.INTEGER.of(10).toBag()));
		expect(context.getClock()).andReturn(Clock.systemUTC());

		control.replay();

		AttributeResolverDescriptor d = p.parseAttributeResolver(this, m);
		LoggerFactory.getLogger(getClass()).debug(d.toString());
		assertEquals(d.canResolve("testId1"), true);
		assertEquals(d.canResolve("testId2"), true);
		assertEquals(d.canResolve("testId3"), true);
		assertEquals(d.canResolve("testId4"), true);

		ResolverContext pipContext = ResolverContext.createContext(context, d);

		d.getResolverFunction().apply(pipContext);
		control.verify();

		assertEquals("Test", d.getName());
		Truth8.assertThat(CategoryId.parse("subject")).hasValue(d.getCategory());
		assertEquals("issuer", d.getIssuer());
		assertEquals(Duration.ofSeconds(30), d.getPreferredCacheTTL());

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

		List<AttributeReferenceKey> keys = d.getContextKeyRefs();
		assertEquals(2, keys.size());

		assertEquals(expectedKey0, keys.get(0));
		assertEquals(expectedKey1, keys.get(1));



		control.verify();
	}

	@Test
	public void testParseAttributeResolverWithoutKeysWithContext() throws Exception
	{
		Method m = getMethod(this.getClass(), "resolve2");
		assertNotNull(m);
		replay(context);
		AttributeResolverDescriptor d = p.parseAttributeResolver(this, m);
		ResolverContext pipContext = ResolverContext.createContext(context, d);
		d.getResolverFunction().apply(pipContext);

		assertEquals("Test", d.getName());
		assertEquals(CategoryId.parse("subject").get(), d.getCategory());
		assertEquals("issuer", d.getIssuer());
		assertEquals(Duration.ofSeconds(30), d.getPreferredCacheTTL());
		verify(context);

	}

	@Test
	public void testParseAttributeResolverWithoutParameters()
	{
		Method m = getMethod(this.getClass(), "resolve3");
		assertNotNull(m);
		AttributeResolverDescriptor d = p.parseAttributeResolver(this, m);
		assertTrue(d.getContextKeyRefs().isEmpty());
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
		                                            .dataType(XacmlTypes.INTEGER)
		                                            .jpath("aaaa").build()));
	}


	@XacmlAttributeResolverDescriptor(id="testResolver1", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId1"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#boolean", id="testId2"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#string", id="testId3"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#double", id="testId4")
	})
	public Map<String, BagOfValues> resolve1(
			@XacmlAttributeDesignator(category="test", attributeId="attr1",
					dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfValues k1,
			@XacmlAttributeDesignator(category="test", attributeId="attr2",
					dataType="http://www.w3.org/2001/XMLSchema#integer", issuer="test") BagOfValues k2)
	{
		return ImmutableMap.<String, BagOfValues>builder()
		                   .put("testId1", XacmlTypes.INTEGER.of(10).toBag())
		                   .put("testId2", XacmlTypes.BOOLEAN.of(false).toBag())
		                   .put("testId3", XacmlTypes.STRING.of("testId3Value").toBag())
				           .build();
	}

	@XacmlAttributeResolverDescriptor(id="testId", name="Test", category="subject", issuer="issuer", cacheTTL=30,
			attributes={
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId1"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId2"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId3"),
				@XacmlAttributeDescriptor(dataType="http://www.w3.org/2001/XMLSchema#integer", id="testId4")
	})
	public Map<String, BagOfValues> resolve2(ResolverContext context)
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
	public Map<String, BagOfValues> resolve3()
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
	public Map<String, BagOfValues> resolve4(@XacmlAttributeDesignator(category="test", attributeId="aaaTTr",
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfValues k1, ResolverContext context)
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
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfValues k1, ResolverContext context)
	{
		return null;
	}

	@XacmlContentResolverDescriptor(id="testId", name="Test", category="subject", cacheTTL=30)
	public ContentRef resolveContent1(@XacmlAttributeDesignator(category="test", attributeId="aaaTTr",
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfValues k1)
	{
		return null;
	}

	@XacmlContentResolverDescriptor(id="testId", name="Test", category="subject", cacheTTL=30)
	public Node resolveContent2(@XacmlAttributeDesignator(category="test", attributeId="aaaTTr",
			dataType="http://www.w3.org/2001/XMLSchema#boolean") BagOfValues k1)
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
