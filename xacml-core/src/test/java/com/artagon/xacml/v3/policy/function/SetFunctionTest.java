package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;

public class SetFunctionTest 
{
	private static FunctionProvider p;
	
	@BeforeClass
	public static void init(){
		p = new AnnotiationBasedFunctionProvider(SetFunctions.class);
	}
	
	@Test
	public void testIfImplemented()
	{
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals"));
		
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals"));
	}
	
	@Test
	public void testBooleanUnion()
	{
		BagOfAttributeValues<BooleanValue> a = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true));
		BagOfAttributeValues<BooleanValue> b = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false));
		BagOfAttributeValues<BooleanValue> c = SetFunctions.booleanUnion(a, b);
		assertEquals(2, c.size());
		assertTrue(c.contains(XacmlDataTypes.BOOLEAN.create(true)));
		assertTrue(c.contains(XacmlDataTypes.BOOLEAN.create(false)));
	}
	
	@Test
	public void testBooleanSetEquals()
	{
		BagOfAttributeValues<BooleanValue> a = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false));
		BagOfAttributeValues<BooleanValue> b = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(true));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), SetFunctions.booleanSetEquals(a, b));
	}
	
	@Test
	public void testBooleanIntersection()
	{
		BagOfAttributeValues<BooleanValue> a = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false));
		BagOfAttributeValues<BooleanValue> b = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true));
		assertEquals(XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true)), SetFunctions.booleanIntersection(a, b));
	}
	
	@Test
	public void testBooleanIntercetion()
	{
		BagOfAttributeValues<BooleanValue> a = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(true));
		BagOfAttributeValues<BooleanValue> b = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(false), XacmlDataTypes.BOOLEAN.create(false));
		BagOfAttributeValues<BooleanValue> c = SetFunctions.booleanIntersection(a, b);
		assertEquals(0, c.size());
		
		b = XacmlDataTypes.BOOLEAN.bag(XacmlDataTypes.BOOLEAN.create(true), XacmlDataTypes.BOOLEAN.create(false));
		c = SetFunctions.booleanIntersection(a, b);
		assertEquals(1, c.size());
		assertTrue(c.contains(XacmlDataTypes.BOOLEAN.create(true)));
	}
}
