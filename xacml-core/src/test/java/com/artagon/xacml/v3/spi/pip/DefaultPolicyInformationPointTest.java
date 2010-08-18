package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.createStrictMock;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
	@Ignore
	public void testResolverResolution()
	{
		pip.addResolver(r1);
		pip.addResolver(r2);
		pip.addResolver(r3);
	}
}
