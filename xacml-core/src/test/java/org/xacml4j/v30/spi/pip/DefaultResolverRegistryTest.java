package org.xacml4j.v30.spi.pip;

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.pdp.Policy;
import org.xacml4j.v30.pdp.PolicySet;
import org.xacml4j.v30.spi.pip.AttributeResolver;
import org.xacml4j.v30.spi.pip.AttributeResolverDescriptor;
import org.xacml4j.v30.spi.pip.AttributeResolverDescriptorBuilder;
import org.xacml4j.v30.spi.pip.DefaultResolverRegistry;
import org.xacml4j.v30.spi.pip.ResolverRegistry;
import org.xacml4j.v30.types.IntegerType;

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
		builder("testId1", "Test1", AttributeCategories.SUBJECT_ACCESS)
		.attribute("testAttr1", IntegerType.INTEGER).build();
		
	
	}
	
	@Test()
	public void testAddDifferentResolversWithTheSameAttributesNoIssuer()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
				.build();
		expect(r1.getDescriptor()).andReturn(d);
		
		expect(r2.getDescriptor()).andReturn(
				AttributeResolverDescriptorBuilder
				.builder("test2", "Test2", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
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
				.builder("test1", "Test1", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
				.build();
		expect(r1.getDescriptor()).andReturn(d);
		
		expect(r2.getDescriptor()).andReturn(
				AttributeResolverDescriptorBuilder
				.builder("test1", "Test2", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
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
				.builder("test1", "Test1", "Issuer1", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
				.build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder
				.builder("test2", "Test2", "Issuer2", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
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
		
		Iterable<AttributeResolver> matchNoIssuer = r.getMatchingAttributeResolvers(context, new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, null));
		
		assertEquals(2, Iterables.size(matchNoIssuer));
		assertSame(r1, Iterables.get(matchNoIssuer, 0));
		assertSame(r2, Iterables.get(matchNoIssuer, 1));
		
		Iterable<AttributeResolver> matchWithMatchingIssuer1 = r.getMatchingAttributeResolvers(context, new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, "Issuer1"));
		
		assertEquals(1, Iterables.size(matchWithMatchingIssuer1));
		assertSame(r1, Iterables.get(matchWithMatchingIssuer1, 0));
		
		Iterable<AttributeResolver> matchWithtMatchingIssuer2 = r.getMatchingAttributeResolvers(context, new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, "Issuer2"));
		
		assertEquals(1, Iterables.size(matchWithtMatchingIssuer2));
		assertSame(r2, Iterables.get(matchWithtMatchingIssuer2, 0));
		
		control.verify();
	}
	
	@Test
	public void testAddResolverWithTheSameAttributesFirstWithNullIssuerSecondWithIssuer()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
				.build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder
				.builder("test2", "Test2", "Issuer", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
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
		Iterable<AttributeResolver> matchNoIssuer = r.getMatchingAttributeResolvers(context, new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, null));
		
		assertEquals(2, Iterables.size(matchNoIssuer));
		assertSame(r1, Iterables.get(matchNoIssuer, 0));
		assertSame(r2, Iterables.get(matchNoIssuer, 1));
		
		Iterable<AttributeResolver> matchWithMatchingIssuer = r.getMatchingAttributeResolvers(context, new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, "Issuer"));
		
		assertEquals(1, Iterables.size(matchWithMatchingIssuer));
		assertSame(r2, Iterables.get(matchWithMatchingIssuer, 0));
		
		Iterable<AttributeResolver> matchWithNotMatchingIssuer = r.getMatchingAttributeResolvers(context, new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, "Issuer1"));
		
		assertEquals(0, Iterables.size(matchWithNotMatchingIssuer));
		
		control.verify();
	}
	
	@Test
	public void testAddResolverSameGloballyAndScoped()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
				.build();
		
		AttributeDesignatorKey key = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, null);
		
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
		
		Iterable<AttributeResolver> resolvers = r.getMatchingAttributeResolvers(context, key);
		
		assertFalse(Iterables.isEmpty(resolvers));
		assertSame(r2, Iterables.get(resolvers, 0));
		assertSame(r1, Iterables.get(resolvers, 1));
		
		control.verify();
	}
	
	@Test
	public void testAddResolverMatchingResolverGloballyAndScoped()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
				.build();
		
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder
				.builder("test2", "Test2", "Issuer", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
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
		
		r.addAttributeResolver(r1);
		r.addAttributeResolver(r2);
		Iterable<AttributeResolver> matchNoIssuer = r.getMatchingAttributeResolvers(context, new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, null));
		
		assertEquals(2, Iterables.size(matchNoIssuer));
		assertSame(r1, Iterables.get(matchNoIssuer, 0));
		assertSame(r2, Iterables.get(matchNoIssuer, 1));
		
		Iterable<AttributeResolver> matchWithMatchingIssuer = r.getMatchingAttributeResolvers(context, new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, "Issuer"));
		
		assertEquals(1, Iterables.size(matchWithMatchingIssuer));
		assertSame(r2, Iterables.get(matchWithMatchingIssuer, 0));
		
		Iterable<AttributeResolver> matchWithNotMatchingIssuer = r.getMatchingAttributeResolvers(context, new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, "Issuer1"));
		
		assertEquals(0, Iterables.size(matchWithNotMatchingIssuer));
		
		control.verify();
	}
	
	@Test
	public void testAddResolverForPolicyIdCurrentPolicyHasMatchingResolver()
	{
		AttributeDesignatorKey key = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, null);
		assertTrue(d1.canResolve(key));
		expect(r1.getDescriptor()).andReturn(d1);
		Policy p = control.createMock(Policy.class);
		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testId");
		expect(r1.getDescriptor()).andReturn(d1);
		expect(context.getParentContext()).andReturn(null);
		control.replay();
		r.addAttributeResolver("testId", r1);
		Iterable<AttributeResolver> resolvers = r.getMatchingAttributeResolvers(context, key);
		assertFalse(Iterables.isEmpty(resolvers));
		assertSame(r1, Iterables.getOnlyElement(resolvers));
		control.verify();
	}
	
	@Test
	public void testResolverResolutionWith2LevelPolicyHierarchy()
	{
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder
				.builder("test1", "Test1", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testAttr1", IntegerType.INTEGER)
				.build();
		
		AttributeResolver resolver = control.createMock(AttributeResolver.class);
		
		AttributeDesignatorKey key = new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, 
				"testAttr1", IntegerType.INTEGER, null);
		
		assertTrue(d1.canResolve(key));
		
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
		
		Iterable<AttributeResolver> resolvers = r.getMatchingAttributeResolvers(context, key);
		
		assertFalse(Iterables.isEmpty(resolvers));
		assertSame(resolver, Iterables.getOnlyElement(resolvers));
		control.verify();
	}
}
