package com.artagon.xacml.v30.spi.pip;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.pdp.AttributeDesignatorKey;
import com.artagon.xacml.v30.pdp.EvaluationContext;
import com.artagon.xacml.v30.pdp.Policy;
import com.artagon.xacml.v30.pdp.PolicySet;
import com.artagon.xacml.v30.types.IntegerType;

public class DefaultResolverRegistryTest 
{
	private ResolverRegistry r;
	private IMocksControl control;
	private EvaluationContext context;
	private AttributeResolver r1;
	private AttributeResolver r2;
	
	private AttributeResolverDescriptor d1;
	private AttributeResolverDescriptor d2;
	
	@Before
	public void init(){
		this.r = new DefaultResolverRegistry();
		this.control = EasyMock.createStrictControl();
		this.context = control.createMock(EvaluationContext.class);
		this.r1 = control.createMock(AttributeResolver.class);
		this.r2 = control.createMock(AttributeResolver.class);
		
		this.d1 = AttributeResolverDescriptorBuilder.
		builder("testId1", "Test1", AttributeCategories.SUBJECT_ACCESS)
		.attribute("testAttr1", IntegerType.INTEGER).build();
		
		this.d2 = AttributeResolverDescriptorBuilder.
		builder("testId1", "Test1", "TestIssuer", AttributeCategories.SUBJECT_ACCESS)
		.attribute("testAttr1", IntegerType.INTEGER).build();
	
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddRootResolverWithTheSameAttributes()
	{
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d1);
		expect(r1.getDescriptor()).andReturn(d1);
		control.replay();
		r.addAttributeResolver(r1);
		r.addAttributeResolver(r2);
		control.verify();
	}
	
	@Test
	public void testAddRootResolverAndTheSamePolicyBoundResolverAndResolveWithThePolicyScoped()
	{
		AttributeDesignatorKey key = new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, null);
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		Policy p = control.createMock(Policy.class);
		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testId1");
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		control.replay();
		r.addAttributeResolver(r1);
		r.addAttributeResolver("testId", r1);
		AttributeResolver resolver = r.getAttributeResolver(context, key);
		assertNotNull(resolver);
		assertSame(r1, resolver);
		control.verify();
	}
	
	@Test
	public void testAddRootResolverWithIssuerAndTheSamePolicyBoundResolverAndResolveWithThePolicyScoped()
	{
		AttributeDesignatorKey key = new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, "TestIssuer");
		expect(r1.getDescriptor()).andReturn(d2).times(2);
		Policy p = control.createMock(Policy.class);
		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testId1");
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d2);
		control.replay();
		r.addAttributeResolver(r1);
		r.addAttributeResolver("testId", r1);
		AttributeResolver resolver = r.getAttributeResolver(context, key);
		assertNotNull(resolver);
		assertSame(r1, resolver);
		control.verify();
	}
	
	@Test
	public void testAddRootResolverAndTheSamePolicyBoundResolverAndResolveWithTheRoot()
	{
		AttributeDesignatorKey key = new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, null);
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		Policy p = control.createMock(Policy.class);
		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("AAAAA");
		expect(context.getParentContext()).andReturn(null);
		expect(r1.getDescriptor()).andReturn(d1);
		control.replay();
		r.addAttributeResolver(r1);
		r.addAttributeResolver("testId", r1);
		AttributeResolver resolver = r.getAttributeResolver(context, key);
		assertNotNull(resolver);
		assertSame(r1, resolver);
		control.verify();
	}
	
	@Test
	public void testAddResolverForPolicyIdCurrentPolicyHasMatchingResolver()
	{
		AttributeDesignatorKey key = new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, null);
		assertTrue(d1.canResolve(key));
		expect(r1.getDescriptor()).andReturn(d1);
		Policy p = control.createMock(Policy.class);
		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testId");
		expect(r1.getDescriptor()).andReturn(d1);
		control.replay();
		r.addAttributeResolver("testId", r1);
		AttributeResolver resolver = r.getAttributeResolver(context, key);
		assertNotNull(resolver);
		assertSame(r1, resolver);
		control.verify();
	}
	
	@Test
	public void testAddResolverForPolicyIdSecondLevelPolicyHasResolver()
	{
		AttributeDesignatorKey key = new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testAttr1", IntegerType.INTEGER, null);
		assertTrue(d1.canResolve(key));
		expect(r1.getDescriptor()).andReturn(d1);
		Policy p1 = control.createMock(Policy.class);
		expect(context.getCurrentPolicy()).andReturn(p1);
		expect(p1.getId()).andReturn("testIdP1");
		EvaluationContext context1 = control.createMock(EvaluationContext.class);
		expect(context.getParentContext()).andReturn(context1);
		PolicySet p2 = control.createMock(PolicySet.class);
		expect(context1.getCurrentPolicy()).andReturn(null);
		expect(context1.getCurrentPolicySet()).andReturn(p2);
		expect(p2.getId()).andReturn("testIdP2");
		expect(r1.getDescriptor()).andReturn(d1);
		control.replay();
		r.addAttributeResolver("testIdP2", r1);
		AttributeResolver resolver = r.getAttributeResolver(context, key);
		assertNotNull(resolver);
		assertSame(r1, resolver);
		control.verify();
	}
}
