package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Apply;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.PolicyFactory;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.policy.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.policy.spi.FunctionProvidersRegistry;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultPolicyFactoryTest 
{
	private PolicyFactory f;
	private FunctionProvidersRegistry functionRegistry;
	private DecisionCombiningAlgorithmProvider dectionCombiningAlgoProvider;
	
	@Before
	public void init()
	{
		this.dectionCombiningAlgoProvider = createStrictMock(DecisionCombiningAlgorithmProvider.class);
		this.functionRegistry = createStrictMock(FunctionProvidersRegistry.class);
		this.f = new DefaultPolicyFactory(functionRegistry, dectionCombiningAlgoProvider);
	}
	
	@Test
	public void testCreateAttributeValue() throws PolicySyntaxException
	{
		AttributeValue v = f.createAttributeValue(DataTypes.STRING.getTypeId(), "test");
		assertEquals("test", v.toXacmlString());
	}
	
	@Test(expected=PolicySyntaxException.class)
	public void testCreateAttributeValueUnknownType() throws PolicySyntaxException
	{
		f.createAttributeValue("aaaaa", "test");
	}
	
	@Test(expected=PolicySyntaxException.class)
	public void testCreateAttributeValueInvalidValue() throws PolicySyntaxException
	{
		f.createAttributeValue(DataTypes.STRING.getTypeId(), null);
	}
	
	@Test
	public void testCreateApply() throws PolicySyntaxException
	{
		Expression[] arguments = {DataTypes.STRING.create("v0"), DataTypes.STRING.create("v1")};
		FunctionSpec spec = createStrictMock(FunctionSpec.class);
		expect(functionRegistry.getFunction("testId")).andReturn(spec);
		spec.validateParametersAndThrow(arguments);
		replay(functionRegistry, spec);
		Apply apply = f.createApply("testId", arguments);
		assertNotNull(apply);
		verify(functionRegistry, spec);
	}
}
