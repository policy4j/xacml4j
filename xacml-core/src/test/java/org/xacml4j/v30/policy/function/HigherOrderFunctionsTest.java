package org.xacml4j.v30.policy.function;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.xacml4j.v30.types.BooleanType.BOOLEAN;
import static org.xacml4j.v30.types.IntegerType.INTEGER;
import static org.xacml4j.v30.types.StringType.STRING;

import java.util.Collection;
import java.util.LinkedList;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.pdp.FunctionReference;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.Types;



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
	
	private IMocksControl c;
	
	private Types types;

	@Before
	public void init() throws Exception
	{
		this.types = Types.builder().defaultTypes().create();
		this.c  = createControl();
		this.higherOrderFunctions = new AnnotiationBasedFunctionProvider(HigherOrderFunctions.class);
		this.stringFunctions = new AnnotiationBasedFunctionProvider(StringFunctions.class);
		this.equalityFunctions = new AnnotiationBasedFunctionProvider(EqualityPredicates.class);
		this.numericComparisionFunctions = new AnnotiationBasedFunctionProvider(NumericComparisionFunctions.class);
		this.regExpFunctions = new AnnotiationBasedFunctionProvider(RegularExpressionFunctions.class);
		this.context = c.createMock(EvaluationContext.class);
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
		Collection<AttributeExp> v = new LinkedList<AttributeExp>();
		v.add(INTEGER.create(10));
		v.add(INTEGER.create(20));
		v.add(INTEGER.create(30));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(4);

		c.replay();
		BagOfAttributeExp bag =  map.invoke(context, new FunctionReference(intToString), INTEGER.bagOf(v));
		c.verify();
		assertTrue(bag.contains(STRING.create("10")));
		assertTrue(bag.contains(STRING.create("20")));
		assertTrue(bag.contains(STRING.create("30")));
	}

	@Test
	public void testAnyOf() throws EvaluationException
	{
		Collection<AttributeExp> v = new LinkedList<AttributeExp>();
		v.add(INTEGER.create(10));
		v.add(INTEGER.create(20));
		v.add(INTEGER.create(30));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);

		c.replay();
		BooleanExp r = anyOf.invoke(context, new FunctionReference(intEq), INTEGER.create(20), INTEGER.bagOf(v));
		assertEquals(BOOLEAN.create(true), r);
		c.verify();
	}

	@Test
	public void testAllOfAny() throws EvaluationException
	{
		Collection<AttributeExp> a = new LinkedList<AttributeExp>();
		a.add(INTEGER.create(10));
		a.add(INTEGER.create(20));

		Collection<AttributeExp> b = new LinkedList<AttributeExp>();
		b.add(INTEGER.create(1));
		b.add(INTEGER.create(3));
		b.add(INTEGER.create(5));
		b.add(INTEGER.create(19));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);

		c.replay();
		BooleanExp r = allOfAny.invoke(context, new FunctionReference(intGreaterThan),
				INTEGER.bagOf(a), INTEGER.bagOf(b));
		assertEquals(BOOLEAN.create(true), r);
		c.verify();
	}

	@Test
	public void testAnyOfAll() throws EvaluationException
	{
		Collection<AttributeExp> a = new LinkedList<AttributeExp>();
		a.add(INTEGER.create(3));
		a.add(INTEGER.create(5));

		Collection<AttributeExp> b = new LinkedList<AttributeExp>();
		b.add(INTEGER.create(1));
		b.add(INTEGER.create(2));
		b.add(INTEGER.create(3));
		b.add(INTEGER.create(4));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(8);


		c.replay();
		BooleanExp r = anyOfAll.invoke(context, new FunctionReference(intGreaterThan),
				INTEGER.bagOf(a), INTEGER.bagOf(b));
		assertEquals(BOOLEAN.create(true), r);
		c.verify();
	}

	@Test
	public void testAnyOfAllIIC168() throws EvaluationException
	{

		Collection<AttributeExp> a = new LinkedList<AttributeExp>();
		a.add(STRING.create("   This  is n*o*t* *IT!  "));
		a.add(STRING.create("   This is not a match to IT!  "));

		Collection<AttributeExp> b = new LinkedList<AttributeExp>();
		b.add(STRING.create("   This  is IT!  "));
		b.add(STRING.create("   This  is not IT!  "));


		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);
		expect(context.getTypes()).andReturn(types).times(2);
		c.replay();
		BooleanExp r = anyOfAll.invoke(context, new FunctionReference(stringRegExpMatch),
				STRING.bagOf(a), STRING.bagOf(b));
		assertEquals(BOOLEAN.create(true), r);
		c.verify();
	}

	@Test
	public void testAllOfAll() throws EvaluationException
	{
		Collection<AttributeExp> a = new LinkedList<AttributeExp>();
		a.add(INTEGER.create(5));
		a.add(INTEGER.create(6));

		Collection<AttributeExp> b = new LinkedList<AttributeExp>();
		b.add(INTEGER.create(1));
		b.add(INTEGER.create(2));
		b.add(INTEGER.create(3));
		b.add(INTEGER.create(4));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(9);
		
		c.replay();
		BooleanExp r = allOfAll.invoke(context, new FunctionReference(intGreaterThan),
				INTEGER.bagOf(a), INTEGER.bagOf(b));
		assertEquals(BOOLEAN.create(true), r);
		c.verify();
	}
}
