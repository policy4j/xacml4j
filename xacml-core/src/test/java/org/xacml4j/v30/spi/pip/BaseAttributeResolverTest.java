package org.xacml4j.v30.spi.pip;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.types.IntegerType;
import org.xacml4j.v30.types.StringType;


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
	@Ignore
	public void testMBeanInvocationStats() throws Exception
	{
		expect(context.getDescriptor()).andReturn(d);
		c.replay();
		r.resolve(context);
		c.verify();
	}
}
