package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.artagon.xacml.v3.policy.Apply;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.PolicyFactory;
import com.artagon.xacml.v3.policy.PolicySyntaxException;
import com.artagon.xacml.v3.policy.spi.DecisionCombiningAlgorithmProvider;
import com.artagon.xacml.v3.policy.spi.FunctionProvidersRegistry;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.google.common.collect.ImmutableList;

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
		AttributeValue v = f.createValue(DataTypes.STRING.getTypeId(), "test");
		assertEquals("test", v.toXacmlString());
	}
	
	@Test(expected=PolicySyntaxException.class)
	public void testCreateAttributeValueUnknownType() throws PolicySyntaxException
	{
		f.createValue("aaaaa", "test");
	}
	
	@Test(expected=PolicySyntaxException.class)
	public void testCreateAttributeValueInvalidValue() throws PolicySyntaxException
	{
		f.createValue(DataTypes.STRING.getTypeId(), null);
	}
	
	@Test
	public void testCreateApply() throws PolicySyntaxException
	{
		Collection<Expression> arguments = ImmutableList.<Expression>of(DataTypes.STRING.create("v0"), DataTypes.STRING.create("v1"));
		FunctionSpec spec = createStrictMock(FunctionSpec.class);
		expect(functionRegistry.getFunction("testId")).andReturn(spec);
		expect(spec.getNumberOfParams()).andReturn(2);
		replay(functionRegistry, spec);
		Apply apply = f.createApply("testId", arguments);
		verify(functionRegistry, spec);
	}
}
