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

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.Iterables;

public class DefaultResolverRegistryTest
{
	private ResolverRegistry r;
	private IMocksControl control;
	private EvaluationContext context;

	private AttributeResolver r1;
	private AttributeResolver r2;

	private AttributeResolverDescriptor d1;

	@Before
	public void init(){
		this.r = new DefaultResolverRegistry();
		this.control = createStrictControl();
		this.context = control.createMock(EvaluationContext.class);
		this.r1 = control.createMock(AttributeResolver.class);
		this.r2 = control.createMock(AttributeResolver.class);

		this.d1 = AttributeResolverDescriptorBuilder.
		builder("testId1", "Test1", Categories.SUBJECT_ACCESS)
		.attribute("testAttr1", XacmlTypes.INTEGER).build();


	}

	@Test()
	public void testAddDifferentResolversWithTheSameAttributesNoIssuer()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();
		expect(r1.getDescriptor()).andReturn(d);

		expect(r2.getDescriptor()).andReturn(
				AttributeResolverDescriptorBuilder
				.builder("test2", "Test2", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build());

		control.replay();

		r.addAttributeResolver(r1);
		r.addAttributeResolver(r2);

		control.verify();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddResolversWithTheSameIdsAndAttributesNoIssuer()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();
		expect(r1.getDescriptor()).andReturn(d);

		expect(r2.getDescriptor()).andReturn(
				AttributeResolverDescriptorBuilder
				.builder("test1", "Test2", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build());

		control.replay();

		r.addAttributeResolver(r1);
		r.addAttributeResolver(r2);

		control.verify();
	}

	@Test
	public void testAddResolverWithTheSameAttributesWithDifferentIssuers()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", "Issuer1", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder
				.builder("test2", "Test2", "Issuer2", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();

		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicySet()).andReturn(null);
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicySet()).andReturn(null);
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicySet()).andReturn(null);
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		control.replay();

		r.addAttributeResolver(r1);
		r.addAttributeResolver(r2);

		AttributeDesignatorKey.Builder keyB = AttributeDesignatorKey.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testAttr1")
				.dataType(XacmlTypes.INTEGER);

		Iterable<AttributeResolver> matchNoIssuer = r.getMatchingAttributeResolvers(context, keyB.build());

		assertEquals(2, Iterables.size(matchNoIssuer));
		assertSame(r1, Iterables.get(matchNoIssuer, 0));
		assertSame(r2, Iterables.get(matchNoIssuer, 1));

		Iterable<AttributeResolver> matchWithMatchingIssuer1 = r.getMatchingAttributeResolvers(context, keyB.issuer("Issuer1").build());

		assertEquals(1, Iterables.size(matchWithMatchingIssuer1));
		assertSame(r1, Iterables.get(matchWithMatchingIssuer1, 0));

		Iterable<AttributeResolver> matchWithtMatchingIssuer2 = r.getMatchingAttributeResolvers(context, keyB.issuer("Issuer2").build());

		assertEquals(1, Iterables.size(matchWithtMatchingIssuer2));
		assertSame(r2, Iterables.get(matchWithtMatchingIssuer2, 0));

		control.verify();
	}

	@Test
	public void testAddResolverWithTheSameAttributesFirstWithNullIssuerSecondWithIssuer()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder
				.builder("test2", "Test2", "Issuer", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();

		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicySet()).andReturn(null);
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicySet()).andReturn(null);
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicySet()).andReturn(null);
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		control.replay();

		AttributeDesignatorKey.Builder keyB = AttributeDesignatorKey.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testAttr1")
				.dataType(XacmlTypes.INTEGER);

		r.addAttributeResolver(r1);
		r.addAttributeResolver(r2);
		Iterable<AttributeResolver> matchNoIssuer = r.getMatchingAttributeResolvers(context, keyB.build());

		assertEquals(2, Iterables.size(matchNoIssuer));
		assertSame(r1, Iterables.get(matchNoIssuer, 0));
		assertSame(r2, Iterables.get(matchNoIssuer, 1));

		Iterable<AttributeResolver> matchWithMatchingIssuer = r.getMatchingAttributeResolvers(context, keyB.issuer("Issuer").build());

		assertEquals(1, Iterables.size(matchWithMatchingIssuer));
		assertSame(r2, Iterables.get(matchWithMatchingIssuer, 0));

		Iterable<AttributeResolver> matchWithNotMatchingIssuer = r.getMatchingAttributeResolvers(context, keyB.issuer("Issuer1").build());

		assertEquals(0, Iterables.size(matchWithNotMatchingIssuer));

		control.verify();
	}

	@Test
	public void testAddResolverSameGloballyAndScoped()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();

		AttributeDesignatorKey.Builder keyB = AttributeDesignatorKey.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testAttr1")
				.dataType(XacmlTypes.INTEGER);

		// add
		expect(r1.getDescriptor()).andReturn(d);
		expect(r2.getDescriptor()).andReturn(d);

		// get matching
		Policy p = control.createMock(Policy.class);

		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testId");

		expect(r2.getDescriptor()).andReturn(d);
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d);


		control.replay();

		r.addAttributeResolver(r1);
		r.addAttributeResolver("testId", r2);

		Iterable<AttributeResolver> resolvers = r.getMatchingAttributeResolvers(context, keyB.build());

		assertFalse(Iterables.isEmpty(resolvers));
		assertSame(r2, Iterables.get(resolvers, 0));
		assertSame(r1, Iterables.get(resolvers, 1));

		control.verify();
	}

	@Test
	public void testAddResolverMatchingResolverGloballyAndScoped()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();

		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder
				.builder("test2", "Test2", "Issuer", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();

		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		// get matching
		Policy p = control.createMock(Policy.class);

		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testId");

		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicySet()).andReturn(null);
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicySet()).andReturn(null);
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d2);

		control.replay();

		AttributeDesignatorKey.Builder keyB = AttributeDesignatorKey.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testAttr1")
				.dataType(XacmlTypes.INTEGER);


		r.addAttributeResolver(r1);
		r.addAttributeResolver(r2);
		Iterable<AttributeResolver> matchNoIssuer = r.getMatchingAttributeResolvers(context, keyB.build());

		assertEquals(2, Iterables.size(matchNoIssuer));
		assertSame(r1, Iterables.get(matchNoIssuer, 0));
		assertSame(r2, Iterables.get(matchNoIssuer, 1));

		Iterable<AttributeResolver> matchWithMatchingIssuer = r.getMatchingAttributeResolvers(context, keyB.issuer("Issuer").build());

		assertEquals(1, Iterables.size(matchWithMatchingIssuer));
		assertSame(r2, Iterables.get(matchWithMatchingIssuer, 0));

		Iterable<AttributeResolver> matchWithNotMatchingIssuer = r.getMatchingAttributeResolvers(context, keyB.issuer("Issuer1").build());

		assertEquals(0, Iterables.size(matchWithNotMatchingIssuer));

		control.verify();
	}

	@Test
	public void testAddResolverForPolicyIdCurrentPolicyHasMatchingResolver()
	{
		AttributeDesignatorKey.Builder keyB = AttributeDesignatorKey.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testAttr1")
				.dataType(XacmlTypes.INTEGER);

		assertTrue(d1.canResolve(keyB.build()));
		expect(r1.getDescriptor()).andReturn(d1);
		Policy p = control.createMock(Policy.class);
		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testId");
		expect(r1.getDescriptor()).andReturn(d1);
		expect(context.getParentContext()).andReturn(null);
		control.replay();
		r.addAttributeResolver("testId", r1);
		Iterable<AttributeResolver> resolvers = r.getMatchingAttributeResolvers(context, keyB.build());
		assertFalse(Iterables.isEmpty(resolvers));
		assertSame(r1, Iterables.getOnlyElement(resolvers));
		control.verify();
	}

	@Test
	public void testResolverResolutionWith2LevelPolicyHierarchy()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", Categories.SUBJECT_ACCESS)
				.attribute("testAttr1", XacmlTypes.INTEGER)
				.build();

		AttributeResolver resolver = control.createMock(AttributeResolver.class);

		AttributeDesignatorKey.Builder keyB = AttributeDesignatorKey.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testAttr1")
				.dataType(XacmlTypes.INTEGER);


		assertTrue(d1.canResolve(keyB.build()));

		expect(resolver.getDescriptor()).andReturn(d1);
		Policy p1 = control.createMock(Policy.class);
		expect(context.getCurrentPolicy()).andReturn(p1);
		expect(p1.getId()).andReturn("testIdP1");

		EvaluationContext context1 = control.createMock(EvaluationContext.class);

		expect(context.getParentContext()).andReturn(context1);

		PolicySet p2 = control.createMock(PolicySet.class);
		expect(context1.getCurrentPolicy()).andReturn(null);
		expect(context1.getCurrentPolicySet()).andReturn(p2);
		expect(p2.getId()).andReturn("testIdP2");
		expect(resolver.getDescriptor()).andReturn(d1);

		expect(context1.getParentContext()).andReturn(null);


		control.replay();
		r.addAttributeResolver("testIdP2", resolver);

		Iterable<AttributeResolver> resolvers = r.getMatchingAttributeResolvers(context, keyB.build());

		assertFalse(Iterables.isEmpty(resolvers));
		assertSame(resolver, Iterables.getOnlyElement(resolvers));
		control.verify();
	}
}
