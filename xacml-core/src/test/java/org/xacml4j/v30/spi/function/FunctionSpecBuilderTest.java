package org.xacml4j.v30.spi.function;


import static org.easymock.EasyMock.createControl;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;

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
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc1");
		this.specSameTypeArgs = b.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.INTEGER, impl);
		b = FunctionSpecBuilder.builder("testFunc2");
		this.specDiffTypeArgs = b.param(XacmlTypes.INTEGER).param(XacmlTypes.STRING).build(XacmlTypes.INTEGER, impl);
	}

	@Test
	public void testValidateSingleTypeParametersWithSameTypeArgs()
	{
		ImmutableList.Builder<Expression> b = ImmutableList.builder();
		c.replay();
		assertFalse(specSameTypeArgs.validateParameters(b.build()));
		assertFalse(specSameTypeArgs.validateParameters(b.add(IntegerExp.valueOf(10L)).build()));
		assertTrue(specSameTypeArgs.validateParameters(b.add(IntegerExp.valueOf(12L)).build()));
		assertFalse(specSameTypeArgs.validateParameters(b.add(IntegerExp.valueOf(13L)).build()));
		c.verify();
	}

	@Test
	public void testValidateSingeTypeParametersWithDiffTypesArgs()
	{
		ImmutableList.Builder<Expression> b = ImmutableList.builder();
		c.replay();
		assertFalse(specDiffTypeArgs.validateParameters(b.build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(IntegerExp.valueOf(10L)).build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(IntegerExp.valueOf(12L)).build()));
		assertFalse(specDiffTypeArgs.validateParameters(b.add(IntegerExp.valueOf(13L)).build()));
		b = ImmutableList.builder();
		assertTrue(specDiffTypeArgs.validateParameters(b.add(IntegerExp.valueOf(10)).add(StringExp.valueOf("aaa")).build()));
		b = ImmutableList.builder();
		assertFalse(specDiffTypeArgs.validateParameters(b.add(StringExp.valueOf("a")).add(IntegerExp.valueOf(10L)).build()));
		c.verify();
	}

	@Test(expected=IllegalStateException.class)
	public void testParameterAfterVaragParam()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc");
		c.replay();
		b.param(XacmlTypes.INTEGER, 1, 10).param(XacmlTypes.INTEGER);
		c.verify();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testParameterVarArgMinAndMaxEquals()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.builder("testFunc");
		c.replay();
		b.param(XacmlTypes.INTEGER, 3, 3).param(XacmlTypes.INTEGER);
		c.verify();
	}
}
