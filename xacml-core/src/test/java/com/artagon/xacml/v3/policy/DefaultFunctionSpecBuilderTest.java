package com.artagon.xacml.v3.policy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.function.DefaultFunctionSpecBuilder;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.IntegerType;
import com.artagon.xacml.v3.policy.type.StringType;

public class DefaultFunctionSpecBuilderTest extends XacmlPolicyTestCase
{
	private IntegerType type1;
	private StringType type2;
	
	private FunctionSpec specSameTypeArgs;
	private FunctionSpec specDiffTypeArgs;
	private MockFunctionImplementation<IntegerType.IntegerValue> impl;
	
	@Before
	public void init(){
		this.type1 = DataTypes.INTEGER.getType();
		this.type2 = DataTypes.STRING.getType();
		IntegerType.IntegerValue expectedResult = DataTypes.INTEGER.create(new Integer(10));
		this.impl =  new MockFunctionImplementation<IntegerType.IntegerValue>(expectedResult);
		
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder("testFunc1"); 
		
		this.specSameTypeArgs = b.withParam(type1).withParam(type1).build(DataTypes.INTEGER.getType(), impl);
		
		b = new DefaultFunctionSpecBuilder("testFunc2"); 
		this.specDiffTypeArgs = b.withParam(type1).withParam(type2).build(DataTypes.INTEGER.getType(), impl);
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
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder("testFunc");
		b.withParam(type1, 1, 10).withParam(type1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParameterVarArgMinAndMaxEquals()
	{
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder("testFunc");
		b.withParam(type1, 3, 3).withParam(type1);
	}
}
