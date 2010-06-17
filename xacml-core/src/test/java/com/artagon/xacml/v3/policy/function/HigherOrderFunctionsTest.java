package com.artagon.xacml.v3.policy.function;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.FunctionInvocationException;
import com.artagon.xacml.v3.FunctionReference;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.StringType.StringValue;

public class HigherOrderFunctionsTest 
{
	private FunctionProvider higherOrderFunctions;
	private FunctionProvider stringFunctions;
	private FunctionProvider equalityFunctions;
	private EvaluationContext context;
	
	private FunctionSpec intToString;
	private FunctionSpec intEq;
	
	private FunctionSpec map;
	private FunctionSpec anyOf;
	
	@Before
	public void init()
	{
		this.higherOrderFunctions = new AnnotiationBasedFunctionProvider(HigherOrderFunctions.class);
		this.stringFunctions = new AnnotiationBasedFunctionProvider(StringFunctions.class);
		this.equalityFunctions = new AnnotiationBasedFunctionProvider(EqualityPredicates.class);
		this.context = createStrictMock(EvaluationContext.class);
		this.map = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:map");
		this.intToString = stringFunctions.getFunction("urn:oasis:names:tc:xacml:3.0:function:string-from-integer");
		this.intEq = equalityFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-equal");
		this.anyOf = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:any-of");
		assertNotNull(map);
		assertNotNull(intToString);
		assertNotNull(intEq);
	}
	
	@Test
	public void testMap() throws FunctionInvocationException
	{
		Collection<AttributeValue> v = new LinkedList<AttributeValue>();
		v.add(XacmlDataTypes.INTEGER.create(10));
		v.add(XacmlDataTypes.INTEGER.create(20));
		v.add(XacmlDataTypes.INTEGER.create(30));
		expect(context.isValidateFuncParamAtRuntime()).andReturn(false).times(4);
		replay(context);
		BagOfAttributeValues<StringValue> bag =  map.invoke(context, new FunctionReference(intToString), XacmlDataTypes.INTEGER.bag(v));
		verify(context);	
		assertTrue(bag.contains(XacmlDataTypes.STRING.create("10")));
		assertTrue(bag.contains(XacmlDataTypes.STRING.create("20")));
		assertTrue(bag.contains(XacmlDataTypes.STRING.create("30")));
	}
	
	@Test
	public void testAnyOf() throws FunctionInvocationException
	{
		Collection<AttributeValue> v = new LinkedList<AttributeValue>();
		v.add(XacmlDataTypes.INTEGER.create(10));
		v.add(XacmlDataTypes.INTEGER.create(20));
		v.add(XacmlDataTypes.INTEGER.create(30));
		expect(context.isValidateFuncParamAtRuntime()).andReturn(false).times(7);
		replay(context);
		BooleanValue r = anyOf.invoke(context, new FunctionReference(intEq), XacmlDataTypes.INTEGER.create(20), XacmlDataTypes.INTEGER.bag(v));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), r);
		r = anyOf.invoke(context, new FunctionReference(intEq), XacmlDataTypes.INTEGER.create(40), XacmlDataTypes.INTEGER.bag(v));
		assertEquals(XacmlDataTypes.BOOLEAN.create(false), r);
		verify(context);

	}
}
