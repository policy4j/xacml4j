package com.artagon.xacml.policy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.function.DefaultFunctionSpecBuilder;
import com.artagon.xacml.policy.function.StaticallyTypedFunction;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.StringType;
import com.artagon.xacml.policy.type.XacmlDataType;

public class DefaultFunctionSpecBuilderTest extends XacmlPolicyTestCase
{
	private IntegerType type1;
	private StringType type2;
	
	private FunctionSpec specSameTypeArgs;
	private FunctionSpec specDiffTypeArgs;
	private StaticallyTypedFunction impl;
	
	@Before
	public void init(){
		this.type1 = XacmlDataType.INTEGER.getType();
		this.type2 = XacmlDataType.STRING.getType();
		this.impl =  new MockFunctionImplementation(type1.create(new Integer(10)));
		
		DefaultFunctionSpecBuilder b = new DefaultFunctionSpecBuilder("testFunc1"); 
		
		this.specSameTypeArgs = b.withParam(type1).withParam(type1).build(impl);
		
		b = new DefaultFunctionSpecBuilder("testFunc2"); 
		this.specDiffTypeArgs = b.withParam(type1).withParam(type2).build(impl);
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
