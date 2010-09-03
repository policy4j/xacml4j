package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.RequestContextAttributesCallback;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class DefaultPolicyInformationPointTest 
{
	private DefaultPolicyInformationPoint pip;
	
	
	@Before
	public void init()
	{
		DefaultPolicyInformationPoint pip = new DefaultPolicyInformationPoint();
	
		this.pip = pip;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddResolversWithTheSameAttributeIdDifferentAttributeDataTypes()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		AttributeResolver r2 = createStrictMock(AttributeResolver.class);
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create(
				AttributeCategoryId.ACTION).attribute("test1", XacmlDataTypes.BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create(
				AttributeCategoryId.ACTION).attribute("test1", XacmlDataTypes.INTEGER).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddResolversWithTheSameAttributeIdTheSameTypeDifferentIssuers()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		AttributeResolver r2 = createStrictMock(AttributeResolver.class);
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create(
				AttributeCategoryId.ACTION, "test").attribute("test1", XacmlDataTypes.BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create(
				AttributeCategoryId.ACTION).attribute("test1", XacmlDataTypes.BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2);
	}
	
	@Test
	public void testAddResolversDifferentCategoryTheSameAttributeId()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		AttributeResolver r2 = createStrictMock(AttributeResolver.class);
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create(
				AttributeCategoryId.ACTION, "test").attribute("test1", XacmlDataTypes.BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create(
				AttributeCategoryId.SUBJECT_ACCESS).attribute("test1", XacmlDataTypes.BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2);
	}

	
	@Test
	public void testRootResolverAddAndResolve()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		EvaluationContext context = createStrictMock(EvaluationContext.class);
		Policy p = createStrictMock(Policy.class);
		AttributeDesignator ref = createStrictMock(AttributeDesignator.class);
		RequestContextAttributesCallback callback = createStrictMock(RequestContextAttributesCallback.class);
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder.create(
				AttributeCategoryId.ACTION).attribute("test1", XacmlDataTypes.BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d).times(2);
		
		// resolve expectations
		expect(context.getCurrentPolicy()).andReturn(p).times(2);
		expect(p.getId()).andReturn("testId");
		expect(context.getParentContext()).andReturn(null);
		expect(ref.getCategory()).andReturn(AttributeCategoryId.ACTION);
		expect(ref.getAttributeId()).andReturn("test1");
		
		Capture<PolicyInformationPointContext> pipCtx = new Capture<PolicyInformationPointContext>();
		Capture<AttributeDesignator> refCap = new Capture<AttributeDesignator>();
		Capture<RequestContextAttributesCallback> callbackCapt = new Capture<RequestContextAttributesCallback>();
		BagOfAttributeValues<AttributeValue> result = XacmlDataTypes.BOOLEAN.emptyBag();
		
		expect(r1.canResolve(ref)).andReturn(true);
		expect(r1.resolve(capture(pipCtx), capture(refCap), capture(callbackCapt))).andReturn(result);	
		replay(r1, context, callback, ref, p);
		
		pip.addResolver(r1);
		BagOfAttributeValues<AttributeValue> r = pip.resolve(context, ref , callback);
		
		verify(r1, context, callback, ref, p);
		
		assertEquals(result, r);
		assertSame(ref, refCap.getValue());
	}
	
	@Test
	public void testPolicyBoundResolverAddAndResolve()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		EvaluationContext context = createStrictMock(EvaluationContext.class);
		Policy p = createStrictMock(Policy.class);
		AttributeDesignator ref = createStrictMock(AttributeDesignator.class);
		RequestContextAttributesCallback callback = createStrictMock(RequestContextAttributesCallback.class);
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder.create(
				AttributeCategoryId.ACTION).attribute("test1", XacmlDataTypes.STRING).build();
		
		expect(r1.getDescriptor()).andReturn(d).times(2);
		
		// resolve
		expect(context.getCurrentPolicy()).andReturn(p).times(2);
		expect(p.getId()).andReturn("testId");
		expect(r1.canResolve(same(ref))).andReturn(true).times(2);
		
		
		Capture<PolicyInformationPointContext> pipCtx = new Capture<PolicyInformationPointContext>();
		Capture<AttributeDesignator> refCap = new Capture<AttributeDesignator>();
		Capture<RequestContextAttributesCallback> callbackCapt = new Capture<RequestContextAttributesCallback>();
		BagOfAttributeValues<AttributeValue> result = XacmlDataTypes.STRING.emptyBag();
		
		expect(r1.resolve(capture(pipCtx), capture(refCap), capture(callbackCapt))).andReturn(result);	
		replay(r1, context, callback, ref, p);
		
		pip.addResolver("testId", r1);
		
		BagOfAttributeValues<AttributeValue> r = pip.resolve(context, ref , callback);
		
		verify(r1, context, callback, ref, p);
		
		assertEquals(result, r);
		assertSame(ref, refCap.getValue());
	}
}