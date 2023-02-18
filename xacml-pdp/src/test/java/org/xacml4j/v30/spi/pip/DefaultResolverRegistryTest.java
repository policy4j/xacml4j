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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.types.XacmlTypes;

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

import java.util.Collection;

public class DefaultResolverRegistryTest
{
	private DefaultResolverRegistry r;
	private IMocksControl control;
	private EvaluationContext context;


	private AttributeResolverDescriptor d1;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void init(){
		this.r = new DefaultResolverRegistry();
		this.control = createStrictControl();
		this.context = control.createMock(EvaluationContext.class);
		this.d1 = AttributeResolverDescriptor.builder(
				"testId1", "Test1", CategoryId.SUBJECT_ACCESS)
		                                     .attribute("testAttr1", XacmlTypes.INTEGER)
		                                     .build((c)-> ImmutableMap.of());
	}

	@Test
	public void testAddDifferentResolversWithTheSameAttributesNoIssuer()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptor
				.builder("test1", "Test1", CategoryId.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build((c)-> ImmutableMap.of());

		AttributeResolverDescriptor d2 = AttributeResolverDescriptor
				.builder("test2", "Test2", CategoryId.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build((c)-> ImmutableMap.of());

		control.replay();

		r.addResolver(d1);
		r.addResolver(d2);

		control.verify();
	}

	@Test
	public void testAddResolversWithTheSameIdsAndAttributesNoIssuer()
	{
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Attribute resolver with id=\"test1\" is already registered with this registry");

		AttributeResolverDescriptor d1 = AttributeResolverDescriptor
				.builder("test1", "Test1", CategoryId.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build(c->ImmutableMap.of());

		AttributeResolverDescriptor d2 =
				AttributeResolverDescriptor
				.builder("test1", "Test2", CategoryId.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build(c->ImmutableMap.of());

		control.replay();

		r.addResolver(d1);
		r.addResolver(d2);

		control.verify();
	}

	@Test
	public void testAddResolverWithTheSameAttributesWithDifferentIssuers()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptor
				.builder("test1", "Test1", "Issuer1", CategoryId.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build(c->ImmutableMap.of());
		AttributeResolverDescriptor d2 = AttributeResolverDescriptor
				.builder("test2", "Test2", "Issuer2", CategoryId.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build(c->ImmutableMap.of());

		control.replay();

		r.addResolver(d1);
		r.addResolver(d2);

		AttributeDesignatorKey.Builder keyB = AttributeDesignatorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testAttr1")
				.dataType(XacmlTypes.INTEGER);

		Iterable<AttributeResolverDescriptor> matchNoIssuer = r.getMatchingAttributeResolver(context, keyB.build());

		assertEquals(2, Iterables.size(matchNoIssuer));
		assertSame(d1, Iterables.get(matchNoIssuer, 0));
		assertSame(d2, Iterables.get(matchNoIssuer, 1));

		Iterable<AttributeResolverDescriptor> matchWithMatchingIssuer1 = r.getMatchingAttributeResolver(context, keyB.issuer("Issuer1").build());

		assertEquals(1, Iterables.size(matchWithMatchingIssuer1));
		assertSame(d1, Iterables.get(matchWithMatchingIssuer1, 0));

		Iterable<AttributeResolverDescriptor> matchWithtMatchingIssuer2 = r.getMatchingAttributeResolver(context, keyB.issuer("Issuer2").build());

		assertEquals(1, Iterables.size(matchWithtMatchingIssuer2));
		assertSame(d2, Iterables.get(matchWithtMatchingIssuer2, 0));

		control.verify();
	}

	@Test
	public void testAddResolverWithTheSameAttributesFirstWithNullIssuerSecondWithIssuer()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptor
				.builder("test1", "Test1", CategoryId.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build(c->ImmutableMap.of());
		AttributeResolverDescriptor d2 = AttributeResolverDescriptor
				.builder("test2", "Test2", "Issuer", CategoryId.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build(c->ImmutableMap.of());

		control.replay();

		AttributeDesignatorKey.Builder keyB = AttributeDesignatorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testAttr1")
				.dataType(XacmlTypes.INTEGER);

		r.addResolver(d1);
		r.addResolver(d2);
		Iterable<AttributeResolverDescriptor> matchNoIssuer = r.getMatchingAttributeResolver(context, keyB.build());

		assertEquals(2, Iterables.size(matchNoIssuer));
		assertSame(d1, Iterables.get(matchNoIssuer, 0));
		assertSame(d2, Iterables.get(matchNoIssuer, 1));

		Iterable<AttributeResolverDescriptor> matchWithMatchingIssuer = r.getMatchingAttributeResolver(context, keyB.issuer("Issuer").build());

		assertEquals(1, Iterables.size(matchWithMatchingIssuer));
		assertSame(d2, Iterables.get(matchWithMatchingIssuer, 0));

		Iterable<AttributeResolverDescriptor> matchWithNotMatchingIssuer = r.getMatchingAttributeResolver(context, keyB.issuer("Issuer1").build());

		assertEquals(0, Iterables.size(matchWithNotMatchingIssuer));

		control.verify();
	}

	@Test
	public void testAddResolverSameGloballyAndScoped()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptor
				.builder("test1", "Test1", CategoryId.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build(c->ImmutableMap.of());

		AttributeDesignatorKey.Builder keyB = AttributeDesignatorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testAttr1")
				.dataType(XacmlTypes.INTEGER);


		control.replay();

		r.addResolver(d1);
		r.addResolver(d);

		Collection<AttributeResolverDescriptor> resolvers = r.getMatchingAttributeResolver(context, keyB.build());

		assertFalse(Iterables.isEmpty(resolvers));
		assertTrue(resolvers.contains(d1));
		assertTrue(resolvers.contains(d));

		control.verify();
	}

}
