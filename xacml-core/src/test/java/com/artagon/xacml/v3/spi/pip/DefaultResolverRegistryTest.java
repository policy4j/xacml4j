package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;

public class DefaultResolverRegistryTest 
{
	private ResolverRegistry r;
	private IMocksControl control;
	private EvaluationContext context;
	private AttributeResolver r1;
	private AttributeResolver r2;
	private AttributeResolver r3;
	
	@Before
	public void init(){
		this.r = new DefaultResolverRegistry();
		this.control = EasyMock.createStrictControl();
		this.context = control.createMock(EvaluationContext.class);
		this.r1 = control.createMock(AttributeResolver.class);
		this.r2 = control.createMock(AttributeResolver.class);
		this.r3 = control.createMock(AttributeResolver.class);
	}
	
	@Test
	public void testAddResolver()
	{
		
	}
}
