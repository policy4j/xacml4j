package com.artagon.xacml.v30.spi.pip;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.*;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;

public class BaseAttributeResolverTest 
{
	private AttributeResolver r;
	private ResolverContext context;
	private AttributeResolverDescriptor d;
	private IMocksControl c;
	
	@Before
	public void init(){
		this.c = createControl();
		this.context = c.createMock(ResolverContext.class);
		this.d = AttributeResolverDescriptorBuilder
		.builder("test", "Test", AttributeCategories.SUBJECT_ACCESS)
		.attribute("testId1", StringType.STRING)
		.attribute("testId2", IntegerType.INTEGER)
		.build();
		this.r =  createMockBuilder( BaseAttributeResolver.class)
		.addMockedMethod("doResolve")
		.withConstructor(d)
		.createMock();
	}
	
	@Test
	public void testMBeanInvocationStats() throws Exception
	{
		expect(context.getDescriptor()).andReturn(d);
		assertEquals(0, r.getInvocationCount());
		assertEquals(0, r.getFailuresCount());
		assertEquals(0, r.getSuccessCount());
		assertEquals(0, r.getSuccessInvocationTimeCMA());
		c.replay();
		r.resolve(context);
		assertEquals(1, r.getInvocationCount());
		assertEquals(0, r.getFailuresCount());
		assertEquals(1, r.getSuccessCount());
		c.verify();
	}
}
