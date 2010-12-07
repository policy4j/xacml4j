package com.artagon.xacml.v3.policy.function;

import static com.artagon.xacml.v3.types.BooleanType.BOOLEAN;
import static com.artagon.xacml.v3.types.IntegerType.INTEGER;
import static com.artagon.xacml.v3.types.StringType.STRING;
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
import com.artagon.xacml.v3.policy.FunctionReference;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.BooleanValue;


public class HigherOrderFunctionsTest 
{
	private FunctionProvider higherOrderFunctions;
	private FunctionProvider stringFunctions;
	private FunctionProvider equalityFunctions;
	private FunctionProvider numericComparisionFunctions;
	private FunctionProvider regExpFunctions;
	private EvaluationContext context;
	
	private FunctionSpec intToString;
	private FunctionSpec intEq;
	private FunctionSpec intGreaterThan;
	private FunctionSpec stringRegExpMatch;
	
	private FunctionSpec map;
	private FunctionSpec anyOf;
	private FunctionSpec allOfAny;
	private FunctionSpec anyOfAll;
	private FunctionSpec allOfAll;
	
	@Before
	public void init() throws Exception
	{
		this.higherOrderFunctions = new AnnotiationBasedFunctionProvider(HigherOrderFunctions.class);
		this.stringFunctions = new AnnotiationBasedFunctionProvider(StringFunctions.class);
		this.equalityFunctions = new AnnotiationBasedFunctionProvider(EqualityPredicates.class);
		this.numericComparisionFunctions = new AnnotiationBasedFunctionProvider(NumericComparisionFunctions.class);
		this.regExpFunctions = new AnnotiationBasedFunctionProvider(RegularExpressionFunctions.class);
		this.context = createStrictMock(EvaluationContext.class);
		this.intToString = stringFunctions.getFunction("urn:oasis:names:tc:xacml:3.0:function:string-from-integer");
		this.intEq = equalityFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-equal");
		this.intGreaterThan = numericComparisionFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than");
		this.stringRegExpMatch = regExpFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match");
		this.map = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:map");
		this.anyOf = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:any-of");
		this.allOfAny = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:all-of-any");
		this.anyOfAll = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:any-of-all");
		this.allOfAll = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:all-of-all");
		assertNotNull(map);
		assertNotNull(intToString);
		assertNotNull(intEq);
	}
	
	@Test
	public void testMapWithValidArguments() throws EvaluationException
	{
		Collection<AttributeValue> v = new LinkedList<AttributeValue>();
		v.add(INTEGER.create(10));
		v.add(INTEGER.create(20));
		v.add(INTEGER.create(30));
		
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
		BagOfAttributeValues bag =  map.invoke(context, new FunctionReference(intToString), INTEGER.bagOf(v));
		verify(context);	
		assertTrue(bag.contains(STRING.create("10")));
		assertTrue(bag.contains(STRING.create("20")));
		assertTrue(bag.contains(STRING.create("30")));
	}
	
	@Test
	public void testAnyOf() throws EvaluationException
	{
		Collection<AttributeValue> v = new LinkedList<AttributeValue>();
		v.add(INTEGER.create(10));
		v.add(INTEGER.create(20));
		v.add(INTEGER.create(30));
		
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
		BooleanValue r = anyOf.invoke(context, new FunctionReference(intEq), INTEGER.create(20), INTEGER.bagOf(v));
		assertEquals(BOOLEAN.create(true), r);
		verify(context);
	}
	
	@Test
	public void testAllOfAny() throws EvaluationException
	{
		Collection<AttributeValue> a = new LinkedList<AttributeValue>();
		a.add(INTEGER.create(10));
		a.add(INTEGER.create(20));
		
		Collection<AttributeValue> b = new LinkedList<AttributeValue>();
		b.add(INTEGER.create(1));
		b.add(INTEGER.create(3));
		b.add(INTEGER.create(5));
		b.add(INTEGER.create(19));
		
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
				INTEGER.bagOf(a), INTEGER.bagOf(b));
		assertEquals(BOOLEAN.create(true), r);
		verify(context);
	}
	
	@Test
	public void testAnyOfAll() throws EvaluationException
	{
		Collection<AttributeValue> a = new LinkedList<AttributeValue>();
		a.add(INTEGER.create(3));
		a.add(INTEGER.create(5));
		
		Collection<AttributeValue> b = new LinkedList<AttributeValue>();
		b.add(INTEGER.create(1));
		b.add(INTEGER.create(2));
		b.add(INTEGER.create(3));
		b.add(INTEGER.create(4));
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		
		for(int i = 0; i < 7; i++){
			expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
			context.setValidateFuncParamsAtRuntime(true);
			expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
			context.setValidateFuncParamsAtRuntime(false);
		}
		
		
		replay(context);
		BooleanValue r = anyOfAll.invoke(context, new FunctionReference(intGreaterThan), 
				INTEGER.bagOf(a), INTEGER.bagOf(b));
		assertEquals(BOOLEAN.create(true), r);
		verify(context);
	}
	
	@Test
	public void testAnyOfAllIIC168() throws EvaluationException
	{
				
		Collection<AttributeValue> a = new LinkedList<AttributeValue>();
		a.add(STRING.create("   This  is n*o*t* *IT!  "));
		a.add(STRING.create("   This is not a match to IT!  "));
		
		Collection<AttributeValue> b = new LinkedList<AttributeValue>();
		b.add(STRING.create("   This  is IT!  "));
		b.add(STRING.create("   This  is not IT!  "));

		
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
		BooleanValue r = anyOfAll.invoke(context, new FunctionReference(stringRegExpMatch), 
				STRING.bagOf(a), STRING.bagOf(b));
		assertEquals(BOOLEAN.create(true), r);
		verify(context);
	}
	
	@Test
	public void testAllOfAll() throws EvaluationException
	{
		Collection<AttributeValue> a = new LinkedList<AttributeValue>();
		a.add(INTEGER.create(5));
		a.add(INTEGER.create(6));
		
		Collection<AttributeValue> b = new LinkedList<AttributeValue>();
		b.add(INTEGER.create(1));
		b.add(INTEGER.create(2));
		b.add(INTEGER.create(3));
		b.add(INTEGER.create(4));
		
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		
		for(int i = 0; i < 8; i ++){
			expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
			context.setValidateFuncParamsAtRuntime(true);
			expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
			context.setValidateFuncParamsAtRuntime(false);
		}
		
		replay(context);
		BooleanValue r = allOfAll.invoke(context, new FunctionReference(intGreaterThan), 
				INTEGER.bagOf(a), INTEGER.bagOf(b));
		assertEquals(BOOLEAN.create(true), r);
		verify(context);
	}
}
