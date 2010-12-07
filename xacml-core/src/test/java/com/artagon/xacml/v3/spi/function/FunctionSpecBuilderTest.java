package com.artagon.xacml.v3.spi.function;


import static com.artagon.xacml.v3.types.IntegerType.INTEGER;
import static com.artagon.xacml.v3.types.StringType.STRING;
import static org.easymock.EasyMock.createStrictMock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;

public class FunctionSpecBuilderTest
{
	private IntegerType type1;
	private StringType type2;
	
	private FunctionSpec specSameTypeArgs;
	private FunctionSpec specDiffTypeArgs;
	private FunctionInvocation impl;
	
	@Before
	public void init(){
		this.type1 = INTEGER;
		this.type2 = STRING;
		this.impl =  createStrictMock(FunctionInvocation.class);
		
		FunctionSpecBuilder b = FunctionSpecBuilder.create("testFunc1"); 
		
		this.specSameTypeArgs = b.withParam(type1).withParam(type1).build(INTEGER, impl);
		
		b = FunctionSpecBuilder.create("testFunc2"); 
		this.specDiffTypeArgs = b.withParam(type1).withParam(type2).build(INTEGER, impl);
	}
	
	@Test
	public void testValidateSingleTypeParametersWithSameTypeArgs()
	{ 
		assertFalse(specSameTypeArgs.validateParameters());
		assertFalse(specSameTypeArgs.validateParameters(type1.create(10L)));
		assertTrue(specSameTypeArgs.validateParameters(type1.create(10L), type1.create(12L)));
		assertFalse(specSameTypeArgs.validateParameters(type1.create(10L), type1.create(12L), type1.create(13L)));
	}
	
	@Test
	public void testValidateSingeTypeParametersWithDiffTypesArgs()
	{
		assertFalse(specDiffTypeArgs.validateParameters());
		assertFalse(specDiffTypeArgs.validateParameters(type1.create(10L)));
		assertFalse(specDiffTypeArgs.validateParameters(type1.create(10L), type1.create(12L)));
		assertFalse(specDiffTypeArgs.validateParameters(type1.create(10L), type1.create(12L), type1.create(13L)));
		assertTrue(specDiffTypeArgs.validateParameters(type1.create(10L), type2.create("b")));

		assertFalse(specDiffTypeArgs.validateParameters(type2.create("a"), type1.create(10L)));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testParameterAfterVaragParam()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.create("testFunc");
		b.withParam(type1, 1, 10).withParam(type1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParameterVarArgMinAndMaxEquals()
	{
		FunctionSpecBuilder b = FunctionSpecBuilder.create("testFunc");
		b.withParam(type1, 3, 3).withParam(type1);
	}
}
