package com.artagon.xacml.v3.spi.pip;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.*;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;

public class DefaultResolverRegistryTest 
{
	private ResolverRegistry r;
	private IMocksControl control;
	private EvaluationContext context;
	private AttributeResolver r1;
	
	@Before
	public void init(){
		this.r = new DefaultResolverRegistry();
		this.control = EasyMock.createStrictControl();
		this.context = control.createMock(EvaluationContext.class);
		this.r1 = control.createMock(AttributeResolver.class);
	
	}
	
	@Test
	@Ignore
	public void testAddResolver()
	{
		AttributeResolverDescriptor d = control.createMock(AttributeResolverDescriptor.class);
		expect(r1.getDescriptor()).andReturn(d);
		replay(r1, d);
		r.addResolver(r1);
		verify(r1, d);
	}
}
