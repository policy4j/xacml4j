package com.artagon.xacml.v3.spi.pip;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

import com.artagon.xacml.v3.spi.PolicyInformationPoint;

public class DefaultPolicyInformationPointTest 
{
	private DefaultPolicyInformationPoint pip;
	
	private AttributeResolver r1;
	private AttributeResolver r2;
	private AttributeResolver r3;
	
	@Before
	public void init()
	{
		DefaultPolicyInformationPoint pip = new DefaultPolicyInformationPoint();
		this.r1 = createStrictMock(AttributeResolver.class);
		this.r2 = createStrictMock(AttributeResolver.class);
		this.r3 = createStrictMock(AttributeResolver.class);
		this.pip = pip;
	}
	
	@Test
	public void testResolverResolution()
	{
		
	}
}
