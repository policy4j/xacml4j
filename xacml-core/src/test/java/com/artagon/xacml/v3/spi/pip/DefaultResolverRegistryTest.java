package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.types.IntegerType;

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
		this.control = EasyMock.createStrictControl();
		this.context = control.createMock(EvaluationContext.class);
		this.r1 = control.createMock(AttributeResolver.class);
		this.r2 = control.createMock(AttributeResolver.class);
		
		this.d1 = AttributeResolverDescriptorBuilder.
		create("testId1", "Test1", AttributeCategories.SUBJECT_ACCESS)
		.attribute("testAttr1", IntegerType.INTEGER).build();
	
	}
	
	@Test
	@Ignore
	public void testAddResolver()
	{
		expect(r1.getDescriptor()).andReturn(d1);
		expect(r2.getDescriptor()).andReturn(d1);
		control.replay();
		r.addResolver(r1);
		control.verify();
	}
}
