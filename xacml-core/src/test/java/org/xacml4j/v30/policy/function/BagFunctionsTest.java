package org.xacml4j.v30.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.AnyURIType;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.BooleanType;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.DoubleType;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.IntegerType;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.StringType;


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
		StringExp v0 = StringType.STRING.create("a");
		StringExp v1 = StringType.STRING.create("b");
		BagOfAttributeExp bag = StringType.STRING.bagOf(v0);
		assertEquals(v0, BagFunctions.stringOneAndOnly(bag));
		assertEquals(IntegerType.INTEGER.create(1), BagFunctions.stringBagSize(bag));
		assertEquals(BooleanType.BOOLEAN.create(true), BagFunctions.stringIsIn(v0, bag));
		assertEquals(BooleanType.BOOLEAN.create(false), BagFunctions.stringIsIn(v1, bag));
		assertEquals(StringType.STRING.bagOf(v0, v1), BagFunctions.stringBag(v0, v1));
	}
	
	@Test
	public void testBooleanBagFunctions() throws EvaluationException
	{
		BooleanExp v0 = BooleanType.BOOLEAN.create(true);
		BooleanExp v1 = BooleanType.BOOLEAN.create(false);
		BagOfAttributeExp bag = BooleanType.BOOLEAN.bagOf(v0);
		assertEquals(v0, BagFunctions.booleanOneAndOnly(bag));
		assertEquals(IntegerType.INTEGER.create(1), BagFunctions.booleanBagSize(bag));
		assertEquals(BooleanType.BOOLEAN.create(true), BagFunctions.booleanIsIn(v0, bag));
		assertEquals(BooleanType.BOOLEAN.create(false), BagFunctions.booleanIsIn(v1, bag));
		assertEquals(BooleanType.BOOLEAN.bagOf(v0, v1), BagFunctions.booleanBag(v0, v1));
	}
	
	@Test
	public void testIntegerBagFunctions() throws EvaluationException
	{
		IntegerExp v0 = IntegerType.INTEGER.create(1);
		IntegerExp v1 = IntegerType.INTEGER.create(2);
		BagOfAttributeExp bag = IntegerType.INTEGER.bagOf(v0);
		assertEquals(v0, BagFunctions.integerOneAndOnly(bag));
		assertEquals(IntegerType.INTEGER.create(1), BagFunctions.integerBagSize(bag));
		assertEquals(BooleanType.BOOLEAN.create(true), BagFunctions.integerIsIn(v0, bag));
		assertEquals(BooleanType.BOOLEAN.create(false), BagFunctions.integerIsIn(v1, bag));
		assertEquals(IntegerType.INTEGER.bagOf(v0, v1), BagFunctions.integerBag(v0, v1));
	}
	
	@Test
	public void testDoubleBagFunctions() throws EvaluationException
	{
		DoubleExp v0 = DoubleType.DOUBLE.create(1);
		DoubleExp v1 = DoubleType.DOUBLE.create(2);
		BagOfAttributeExp bag = DoubleType.DOUBLE.bagOf(v0);
		assertEquals(v0, BagFunctions.doubleOneAndOnly(bag));
		assertEquals(IntegerType.INTEGER.create(1), BagFunctions.doubleBagSize(bag));
		assertEquals(BooleanType.BOOLEAN.create(true), BagFunctions.doubleIsIn(v0, bag));
		assertEquals(BooleanType.BOOLEAN.create(false), BagFunctions.doubleIsIn(v1, bag));
		assertEquals(DoubleType.DOUBLE.bagOf(v0, v1), BagFunctions.doubleBag(v0, v1));
	}
	
	@Test
	public void testAnyURIBagFunctions() throws EvaluationException
	{
		AnyURIExp v0 = AnyURIType.ANYURI.create("http://www.test0.org");
		AnyURIExp v1 = AnyURIType.ANYURI.create("http://www.test1.org");
		AnyURIExp v2 = AnyURIType.ANYURI.create("http://www.test2.org");
		BagOfAttributeExp bag = AnyURIType.ANYURI.bagOf(v0);
		assertEquals(v0, BagFunctions.anyURIOneAndOnly(bag));
		assertEquals(IntegerType.INTEGER.create(1), BagFunctions.anyURIBagSize(bag));
		assertEquals(BooleanType.BOOLEAN.create(true), BagFunctions.anyURIIsIn(v0, bag));
		assertEquals(BooleanType.BOOLEAN.create(false), BagFunctions.anyURIIsIn(v1, bag));
		assertEquals(AnyURIType.ANYURI.bagOf(v0, v1), BagFunctions.anyURIBag(v0, v1));
		
		assertEquals(BooleanType.BOOLEAN.create(true), BagFunctions.anyURIIsIn(v0, BagFunctions.anyURIBag(v0, v1)));
		assertEquals(BooleanType.BOOLEAN.create(false), BagFunctions.anyURIIsIn(v2, BagFunctions.anyURIBag(v0, v1)));
	}
}
