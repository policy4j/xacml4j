package com.artagon.xacml.v3.policy.function;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
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
	private FunctionProvider numericComparisionFunctions;
	private EvaluationContext context;
	
	private FunctionSpec intToString;
	private FunctionSpec intEq;
	private FunctionSpec intGreaterThan;
	
	private FunctionSpec map;
	private FunctionSpec anyOf;
	private FunctionSpec allOfAny;
	private FunctionSpec anyOfAll;
	
	@Before
	public void init()
	{
		this.higherOrderFunctions = new AnnotiationBasedFunctionProvider(HigherOrderFunctions.class);
		this.stringFunctions = new AnnotiationBasedFunctionProvider(StringFunctions.class);
		this.equalityFunctions = new AnnotiationBasedFunctionProvider(EqualityPredicates.class);
		this.numericComparisionFunctions = new AnnotiationBasedFunctionProvider(NumericComparisionFunctions.class);
		this.context = createStrictMock(EvaluationContext.class);
		this.intToString = stringFunctions.getFunction("urn:oasis:names:tc:xacml:3.0:function:string-from-integer");
		this.intEq = equalityFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-equal");
		this.intGreaterThan = numericComparisionFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than");
		
		this.map = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:map");
		this.anyOf = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:any-of");
		this.allOfAny = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:all-of-any");
		this.anyOfAll = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:any-of-all");
		assertNotNull(map);
		assertNotNull(intToString);
		assertNotNull(intEq);
	}
	
	@Test
	public void testMap() throws EvaluationException
	{
		Collection<AttributeValue> v = new LinkedList<AttributeValue>();
		v.add(XacmlDataTypes.INTEGER.create(10));
		v.add(XacmlDataTypes.INTEGER.create(20));
		v.add(XacmlDataTypes.INTEGER.create(30));
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		context.setValidateFuncParamsAtRuntime(true);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		context.setValidateFuncParamsAtRuntime(false);
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		context.setValidateFuncParamsAtRuntime(true);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		context.setValidateFuncParamsAtRuntime(false);
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		context.setValidateFuncParamsAtRuntime(true);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		context.setValidateFuncParamsAtRuntime(false);
		
		
		replay(context);
		BagOfAttributeValues<StringValue> bag =  map.invoke(context, new FunctionReference(intToString), XacmlDataTypes.INTEGER.bag(v));
		verify(context);	
		assertTrue(bag.contains(XacmlDataTypes.STRING.create("10")));
		assertTrue(bag.contains(XacmlDataTypes.STRING.create("20")));
		assertTrue(bag.contains(XacmlDataTypes.STRING.create("30")));
	}
	
	@Test
	public void testAnyOf() throws EvaluationException
	{
		Collection<AttributeValue> v = new LinkedList<AttributeValue>();
		v.add(XacmlDataTypes.INTEGER.create(10));
		v.add(XacmlDataTypes.INTEGER.create(20));
		v.add(XacmlDataTypes.INTEGER.create(30));
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		context.setValidateFuncParamsAtRuntime(true);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		context.setValidateFuncParamsAtRuntime(false);
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		context.setValidateFuncParamsAtRuntime(true);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		context.setValidateFuncParamsAtRuntime(false);
		
		replay(context);
		BooleanValue r = anyOf.invoke(context, new FunctionReference(intEq), XacmlDataTypes.INTEGER.create(20), XacmlDataTypes.INTEGER.bag(v));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), r);
		verify(context);
	}
	
	@Test
	public void testAllOfAny() throws EvaluationException
	{
		Collection<AttributeValue> a = new LinkedList<AttributeValue>();
		a.add(XacmlDataTypes.INTEGER.create(10));
		a.add(XacmlDataTypes.INTEGER.create(20));
		
		Collection<AttributeValue> b = new LinkedList<AttributeValue>();
		b.add(XacmlDataTypes.INTEGER.create(1));
		b.add(XacmlDataTypes.INTEGER.create(3));
		b.add(XacmlDataTypes.INTEGER.create(5));
		b.add(XacmlDataTypes.INTEGER.create(19));
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		context.setValidateFuncParamsAtRuntime(true);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		context.setValidateFuncParamsAtRuntime(false);
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		context.setValidateFuncParamsAtRuntime(true);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		context.setValidateFuncParamsAtRuntime(false);
		
		replay(context);
		BooleanValue r = allOfAny.invoke(context, new FunctionReference(intGreaterThan), 
				XacmlDataTypes.INTEGER.bag(a), XacmlDataTypes.INTEGER.bag(b));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), r);
		verify(context);
	}
	
	@Test
	public void testAnyOfAll() throws EvaluationException
	{
		Collection<AttributeValue> a = new LinkedList<AttributeValue>();
		a.add(XacmlDataTypes.INTEGER.create(3));
		a.add(XacmlDataTypes.INTEGER.create(5));
		
		Collection<AttributeValue> b = new LinkedList<AttributeValue>();
		b.add(XacmlDataTypes.INTEGER.create(1));
		b.add(XacmlDataTypes.INTEGER.create(2));
		b.add(XacmlDataTypes.INTEGER.create(3));
		b.add(XacmlDataTypes.INTEGER.create(4));
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		context.setValidateFuncParamsAtRuntime(true);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		context.setValidateFuncParamsAtRuntime(false);
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		context.setValidateFuncParamsAtRuntime(true);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		context.setValidateFuncParamsAtRuntime(false);
		
		replay(context);
		BooleanValue r = anyOfAll.invoke(context, new FunctionReference(intGreaterThan), 
				XacmlDataTypes.INTEGER.bag(a), XacmlDataTypes.INTEGER.bag(b));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), r);
		verify(context);
	}
}
