package com.artagon.xacml.policy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.Functions;
import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.StringType;

public class DefaultFunctionSpecBuilderTest extends XacmlPolicyTestCase
{
	private IntegerType type1;
	private StringType type2;
	
	private FunctionSpec specSameTypeArgs;
	private FunctionSpec specDiffTypeArgs;
	private RegularFunction impl;
	
	@Before
	public void init(){
		this.type1 = DataTypes.INTEGER.getType();
		this.type2 = DataTypes.STRING.getType();
		this.impl =  new MockFunctionImplementation(type1.create(new Integer(10)));
		
		ExplicitFunctionSpecBuilder b = new ExplicitFunctionSpecBuilder(Functions.INTEGER_ADD); 
		
		this.specSameTypeArgs = b.withParam(type1).withParam(type1).build(impl);
		
		b = new ExplicitFunctionSpecBuilder(Functions.INTEGER_ADD); 
		this.specDiffTypeArgs = b.withParam(type1).withParam(type2).build(impl);
	}
	
	@Test
	public void testValidateSingleTypeParametersWithSameTypeArgs()
	{ 
		List<Expression> params = new LinkedList<Expression>();
		assertFalse(specSameTypeArgs.validateParameters(params));
		params = new LinkedList<Expression>();
		params.add(type1.create(10L));
		assertFalse(specSameTypeArgs.validateParameters(params));
		params = new LinkedList<Expression>();
		params.add(type1.create(10L));
		params.add(type1.create(12L));
		assertTrue(specSameTypeArgs.validateParameters(params));
		
		params = new LinkedList<Expression>();
		params.add(type1.create(10L));
		params.add(type1.create(12L));
		params.add(type1.create(13L));
		assertFalse(specSameTypeArgs.validateParameters(params));
	}
	
	@Test
	public void testValidateSingeTypeParametersWithDiffTypesArgs()
	{
		List<Expression> params = new LinkedList<Expression>();
		assertFalse(specDiffTypeArgs.validateParameters(params));
		params = new LinkedList<Expression>();
		params.add(type1.create(10L));
		assertFalse(specDiffTypeArgs.validateParameters(params));
		params = new LinkedList<Expression>();
		params.add(type1.create(10L));
		params.add(type1.create(12L));
		assertFalse(specDiffTypeArgs.validateParameters(params));
		
		params = new LinkedList<Expression>();
		params.add(type1.create(10L));
		params.add(type1.create(12L));
		params.add(type1.create(13L));
		assertFalse(specDiffTypeArgs.validateParameters(params));
		
		params = new LinkedList<Expression>();
		params.add(type1.create(10L));
		params.add(type2.create("b"));
		assertTrue(specDiffTypeArgs.validateParameters(params));
		
		params = new LinkedList<Expression>();
		params.add(type2.create("a"));
		params.add(type1.create(12L));
		assertFalse(specDiffTypeArgs.validateParameters(params));
	}
	
	@Test(expected=IllegalStateException.class)
	public void testParameterAfterVaragParam()
	{
		ExplicitFunctionSpecBuilder b = new ExplicitFunctionSpecBuilder(Functions.INTEGER_ADD);
		b.withParam(type1, 1, 10).withParam(type1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testParameterVarArgMinAndMaxEquals()
	{
		ExplicitFunctionSpecBuilder b = new ExplicitFunctionSpecBuilder(Functions.INTEGER_ADD);
		b.withParam(type1, 3, 3).withParam(type1);
	}
}
