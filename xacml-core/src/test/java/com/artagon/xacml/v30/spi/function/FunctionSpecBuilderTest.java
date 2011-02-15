package com.artagon.xacml.v30.spi.function;


import static com.artagon.xacml.v30.types.IntegerType.INTEGER;
import static com.artagon.xacml.v30.types.StringType.STRING;
import static org.easymock.EasyMock.createControl;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.FunctionSpec;
import com.google.common.collect.ImmutableList;

public class FunctionSpecBuilderTest
{	
	private FunctionSpec specSameTypeArgs;
	private FunctionSpec specDiffTypeArgs;
	private FunctionInvocation impl;
	private IMocksControl c;
	@Before
	public void init(){
		this.c = createControl();
		this.impl =  c.createMock(FunctionInvocation.class);
		FunctionSpecBuilder b = FunctionSpecBuilder.create("testFunc1"); 
		this.specSameTypeArgs = b.withParam(INTEGER).withParam(INTEGER).build(INTEGER, impl);
		b = FunctionSpecBuilder.create("testFunc2"); 
		this.specDiffTypeArgs = b.withParam(INTEGER).withParam(STRING).build(INTEGER, impl);
	}
	
	@Test
	public void testValidateSingleTypeParametersWithSameTypeArgs()
	{ 
		ImmutableList.Builder<Expression> b = ImmutableList.builder();
		c.replay();
		assertFalse(specSameTypeArgs.validateParameters(b.build()));
		assertFalse(specSameTypeArgs.validateParameters(b.add(INTEGER.create(10L)).build()));
		assertTrue(specSameTypeArgs.validateParameters(b.add(INTEGER.create(12L)).build()));
		assertFalse(specSameTypeArgs.validateParameters(b.add(INTEGER.create(13L)).build()));
		c.verify();
	}
	
	@Test
	public void testValidateSingeTypeParametersWithDiffTypesArgs()
	{
		ImmutableList.Builder<Expression> b = ImmutableList.builder();
		c.replay();
		assertFalse(specDiffTypeArgs.validateParameters(b.build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(INTEGER.create(10L)).build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(INTEGER.create(12L)).build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(INTEGER.create(13L)).build()));
		b = ImmutableList.builder();
		assertTrue(specDiffTypeArgs.validateParameters(b.add(INTEGER.create(10)).add(STRING.create("aaa")).build()));
		b = ImmutableList.builder();
		assertFalse(specDiffTypeArgs.validateParameters(b.add(STRING.create("a")).add(INTEGER.create(10L)).build()));
		c.verify();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testParameterAfterVaragParam()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.create("testFunc");
		c.replay();
		b.withParam(INTEGER, 1, 10).withParam(INTEGER);
		c.verify();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParameterVarArgMinAndMaxEquals()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.create("testFunc");
		c.replay();
		b.withParam(INTEGER, 3, 3).withParam(INTEGER);
		c.verify();
	}
}
