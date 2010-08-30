package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.capture;

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
	
	@Test
	public void testRootResolverAddAndResolve()
	{
//		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
//		EvaluationContext context = createStrictMock(EvaluationContext.class);
//		Policy p = createStrictMock(Policy.class);
//		AttributeDesignator ref = createStrictMock(AttributeDesignator.class);
//		RequestContextAttributesCallback callback = createStrictMock(RequestContextAttributesCallback.class);
//		
//		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder.create(
//				AttributeCategoryId.ACTION).attribute("test1").build();
//		expect(r1.getDescriptor()).andReturn(d);
//		
//		expect(context.getCurrentPolicy()).andReturn(p).times(2);
//		expect(p.getId()).andReturn("testId");
//		expect(context.getParentContext()).andReturn(null);
//		expect(ref.getCategory()).andReturn(AttributeCategoryId.ACTION);
//		expect(ref.getAttributeId()).andReturn("test1");
//		Capture<PolicyInformationPointContext> pipCtx = new Capture<PolicyInformationPointContext>();
//		Capture<AttributeDesignator> refCap = new Capture<AttributeDesignator>();
//		Capture<RequestContextAttributesCallback> callbackCapt = new Capture<RequestContextAttributesCallback>();
//		BagOfAttributeValues<? extends AttributeValue> result = XacmlDataTypes.BOOLEAN.emptyBag();
//		expect(r1.resolve(capture(pipCtx), capture(refCap), capture(callbackCapt))).andStubReturn(result);
//		replay(r1, context, callback, ref, p);
//		
//		pip.addResolver(r1);
//		pip.resolve(context, ref , callback);
//		
//		verify(r1, context, callback, ref, p);
	}
}
