package com.artagon.xacml.v3.spi.pip;

import static com.artagon.xacml.v3.types.BooleanType.BOOLEAN;
import static com.artagon.xacml.v3.types.IntegerType.INTEGER;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.RequestContextCallback;

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
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("TestResolver").
		attribute(AttributeCategories.ACTION, "test1", BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create("TestResolver", "TestIssuer").
		attribute(AttributeCategories.ACTION, "test1", INTEGER).build();
		
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
	
		
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("TestResolver", "TestIssuer")
		.attribute(AttributeCategories.ACTION, "test1", BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create("Test Resolver").
		attribute(AttributeCategories.ACTION, "test1", BOOLEAN).build();
		
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
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("TestResolver", "test").
		attribute(AttributeCategories.ACTION, "test1", BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create("TestResolver").
		attribute(AttributeCategories.ENVIRONMENT, "test1", BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2);
	}
	
	@Test
	public void testResolveAttributeViaRootResolver() throws Exception
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		EvaluationContext context = createStrictMock(EvaluationContext.class);
		RequestContextCallback callback = createStrictMock(RequestContextCallback.class);
		Policy p = createStrictMock(Policy.class);
		
		AttributeDesignator ref = createStrictMock(AttributeDesignator.class);
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("TestResolver", "test").
		attribute(AttributeCategories.ACTION, "testAttributeId", BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testPolicyId");
		expect(context.getParentContext()).andReturn(null);
		expect(ref.getCategory()).andReturn(AttributeCategories.ACTION);
		expect(ref.getAttributeId()).andReturn("testAttributeId");
		
		expect(ref.getCategory()).andReturn(AttributeCategories.ACTION);
		expect(ref.getAttributeId()).andReturn("testAttributeId");
		expect(ref.getDataType()).andReturn(BOOLEAN);
		expect(ref.getIssuer()).andReturn(null);
		
		expect(r1.getDescriptor()).andReturn(d1);
		
		
		expect(ref.getCategory()).andReturn(AttributeCategories.ACTION);
		expect(ref.getAttributeId()).andReturn("testAttributeId");
		expect(ref.getDataType()).andReturn(BOOLEAN);
		expect(ref.getIssuer()).andReturn(null);
		
		Capture<PolicyInformationPointContext> pipContext = new Capture<PolicyInformationPointContext>();
		
		BagOfAttributeValues v = BOOLEAN.bagOf(BOOLEAN.create(true));
		expect(r1.resolve(capture(pipContext), 
				eq(AttributeCategories.ACTION), 
				eq("testAttributeId"), 
				eq(BOOLEAN), eq((String)null))).andReturn(v);
		replay(r1, context, callback, ref, p);
		
		
		pip.addResolver(r1);
		assertEquals(BOOLEAN.bagOf(BOOLEAN.create(true)), pip.resolve(context, ref, callback));
		
		verify(r1, context, callback, ref, p);
	}
	
	@Test
	@Ignore
	public void testResolveAttributeViaPolicySetBoundResolver() throws Exception
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		EvaluationContext context = createStrictMock(EvaluationContext.class);
		EvaluationContext parentContext1 = createStrictMock(EvaluationContext.class);
		EvaluationContext parentContext2 = createStrictMock(EvaluationContext.class);
		
		RequestContextCallback callback = createStrictMock(RequestContextCallback.class);
		Policy p = createStrictMock(Policy.class);
		PolicySet ps1 = createStrictMock(PolicySet.class);
		PolicySet ps2 = createStrictMock(PolicySet.class);
		
		AttributeDesignator ref = createStrictMock(AttributeDesignator.class);
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("TestResolver", "test").
		attribute(AttributeCategories.ACTION, "testAttributeId", BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testPolicyId1");
		expect(context.getParentContext()).andReturn(parentContext1);
		
		expect(parentContext1.getCurrentPolicy()).andReturn(null);
		expect(parentContext1.getCurrentPolicySet()).andReturn(ps1);
		expect(ps1.getId()).andReturn("testPolicySetId1");
		expect(parentContext1.getParentContext()).andReturn(parentContext2);
		
		expect(parentContext2.getCurrentPolicy()).andReturn(null);
		expect(parentContext2.getCurrentPolicySet()).andReturn(ps2);
		expect(ps2.getId()).andReturn("testPolicySetId2");
		
		expect(ref.getCategory()).andReturn(AttributeCategories.ACTION);
		expect(ref.getAttributeId()).andReturn("testAttributeId");
		expect(ref.getDataType()).andReturn(BOOLEAN);
		expect(ref.getIssuer()).andReturn(null);
		
		
		expect(ref.getCategory()).andReturn(AttributeCategories.ACTION);
		expect(ref.getAttributeId()).andReturn("testAttributeId");
		expect(ref.getDataType()).andReturn(BOOLEAN);
		expect(ref.getIssuer()).andReturn(null);
		
		Capture<PolicyInformationPointContext> pipContext = new Capture<PolicyInformationPointContext>();
		
		expect(r1.resolve(capture(pipContext), 
				eq(AttributeCategories.ACTION), 
				eq("testAttributeId"), 
				eq(BOOLEAN), eq((String)null))).andReturn(
					BOOLEAN.bagOf(BOOLEAN.create(true)));
		replay(r1, context, callback, ref, p, ps1, ps2, parentContext1, parentContext2);
		
		
		pip.addResolver("testPolicySetId2", r1);
		assertEquals(BOOLEAN.bagOf(BOOLEAN.create(true)), pip.resolve(context, ref, callback));
		
		verify(r1, context, callback, ref, p, ps1, ps2, parentContext1, parentContext2);
	}
}