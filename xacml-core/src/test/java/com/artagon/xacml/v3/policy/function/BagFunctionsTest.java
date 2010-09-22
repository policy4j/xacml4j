package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.AnyURIType;
import com.artagon.xacml.v3.types.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.StringType.StringValue;

public class BagFunctionsTest 
{
	
	@Test
	public void testFunctionIfImplemented() throws Exception
	{
		FunctionProvider f = new AnnotiationBasedFunctionProvider(BagFunctions.class);
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:dnsName-bag"));
	}
	
	@Test
	public void testStringBagFunctions() throws EvaluationException
	{
		StringValue v0 = StringType.Factory.create("a");
		StringValue v1 = StringType.Factory.create("b");
		BagOfAttributeValues<StringValue> bag = StringType.Factory.bagOf(v0);
		assertEquals(v0, BagFunctions.stringOneAndOnly(bag));
		assertEquals(IntegerType.Factory.create(1), BagFunctions.stringBagSize(bag));
		assertEquals(BooleanType.Factory.create(true), BagFunctions.stringIsIn(v0, bag));
		assertEquals(BooleanType.Factory.create(false), BagFunctions.stringIsIn(v1, bag));
		assertEquals(StringType.Factory.bagOf(v0, v1), BagFunctions.stringBag(v0, v1));
	}
	
	@Test
	public void testBooleanBagFunctions() throws EvaluationException
	{
		BooleanValue v0 = BooleanType.Factory.create(true);
		BooleanValue v1 = BooleanType.Factory.create(false);
		BagOfAttributeValues<BooleanValue> bag = BooleanType.Factory.bagOf(v0);
		assertEquals(v0, BagFunctions.booleanOneAndOnly(bag));
		assertEquals(IntegerType.Factory.create(1), BagFunctions.booleanBagSize(bag));
		assertEquals(BooleanType.Factory.create(true), BagFunctions.booleanIsIn(v0, bag));
		assertEquals(BooleanType.Factory.create(false), BagFunctions.booleanIsIn(v1, bag));
		assertEquals(BooleanType.Factory.bagOf(v0, v1), BagFunctions.booleanBag(v0, v1));
	}
	
	@Test
	public void testIntegerBagFunctions() throws EvaluationException
	{
		IntegerValue v0 = IntegerType.Factory.create(1);
		IntegerValue v1 = IntegerType.Factory.create(2);
		BagOfAttributeValues<IntegerValue> bag = IntegerType.Factory.bagOf(v0);
		assertEquals(v0, BagFunctions.integerOneAndOnly(bag));
		assertEquals(IntegerType.Factory.create(1), BagFunctions.integerBagSize(bag));
		assertEquals(BooleanType.Factory.create(true), BagFunctions.integerIsIn(v0, bag));
		assertEquals(BooleanType.Factory.create(false), BagFunctions.integerIsIn(v1, bag));
		assertEquals(IntegerType.Factory.bagOf(v0, v1), BagFunctions.integerBag(v0, v1));
	}
	
	@Test
	public void testDoubleBagFunctions() throws EvaluationException
	{
		DoubleValue v0 = DoubleType.Factory.create(1);
		DoubleValue v1 = DoubleType.Factory.create(2);
		BagOfAttributeValues<DoubleValue> bag = DoubleType.Factory.bagOf(v0);
		assertEquals(v0, BagFunctions.doubleOneAndOnly(bag));
		assertEquals(IntegerType.Factory.create(1), BagFunctions.doubleBagSize(bag));
		assertEquals(BooleanType.Factory.create(true), BagFunctions.doubleIsIn(v0, bag));
		assertEquals(BooleanType.Factory.create(false), BagFunctions.doubleIsIn(v1, bag));
		assertEquals(DoubleType.Factory.bagOf(v0, v1), BagFunctions.doubleBag(v0, v1));
	}
	
	@Test
	public void testAnyURIBagFunctions() throws EvaluationException
	{
		AnyURIValue v0 = AnyURIType.Factory.create("http://www.test0.org");
		AnyURIValue v1 = AnyURIType.Factory.create("http://www.test1.org");
		AnyURIValue v2 = AnyURIType.Factory.create("http://www.test2.org");
		BagOfAttributeValues<AnyURIValue> bag = AnyURIType.Factory.bagOf(v0);
		assertEquals(v0, BagFunctions.anyURIOneAndOnly(bag));
		assertEquals(IntegerType.Factory.create(1), BagFunctions.anyURIBagSize(bag));
		assertEquals(BooleanType.Factory.create(true), BagFunctions.anyURIIsIn(v0, bag));
		assertEquals(BooleanType.Factory.create(false), BagFunctions.anyURIIsIn(v1, bag));
		assertEquals(AnyURIType.Factory.bagOf(v0, v1), BagFunctions.anyURIBag(v0, v1));
		
		assertEquals(BooleanType.Factory.create(true), BagFunctions.anyURIIsIn(v0, BagFunctions.anyURIBag(v0, v1)));
		assertEquals(BooleanType.Factory.create(false), BagFunctions.anyURIIsIn(v2, BagFunctions.anyURIBag(v0, v1)));
	}
}
