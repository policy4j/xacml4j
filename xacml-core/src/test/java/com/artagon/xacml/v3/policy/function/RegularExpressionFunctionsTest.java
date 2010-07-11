package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class RegularExpressionFunctionsTest 
{
	
	@Test
	public void testFunctionIfImplemented()
	{
		FunctionProvider f = new AnnotiationBasedFunctionProvider(RegularExpressionFunctions.class);
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match"));
		
	}
	
	@Test
	public void testXacmlRegExptoJERegExpWithCharacterSubstraction()
	{
		assertEquals(".*[0-9]{3}-[0-9]{3}-[0-9]{4}.*", RegularExpressionFunctions.covertXacmlToJavaSyntax("[0-9]{3}-[0-9]{3}-[0-9]{4}"));
	}
	
	@Test
	public void testXacmlRegExpWithSpaceBugTrimming()
	{
		StringValue regexp1 = XacmlDataTypes.STRING.create("   This  is n*o*t* *IT!  ");
		StringValue regexp2 = XacmlDataTypes.STRING.create("  *This .*is not IT! *");
		StringValue input1 = XacmlDataTypes.STRING.create("   This  is not IT!  ");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp1, input1));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp2, input1));
	}
	
	@Test
	public void testRegExpMatchFromIIC168ConformanceTest()
	{
		StringValue regexp1 = XacmlDataTypes.STRING.create("   This  is n*o*t* *IT!  ");
		StringValue input1 = XacmlDataTypes.STRING.create("   This  is IT!  ");
		StringValue input2 = XacmlDataTypes.STRING.create("   This  is not IT!  ");  
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp1, input1));
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp1, input2));
	}
	
	@Test
	public void testStringRegExpMatch() throws EvaluationException
	{
		StringValue regexp = XacmlDataTypes.STRING.create("G*,Trumpickas");
		StringValue input = XacmlDataTypes.STRING.create("Giedrius,Trumpickas");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp, input));     
	}
	
	@Test
	public void testAnyURIRegExpMatch() throws EvaluationException
	{
		StringValue regexp = XacmlDataTypes.STRING.create("http://www.test.org/public/*");
		AnyURIValue input = XacmlDataTypes.ANYURI.create("http://www.test.org/public/test/a");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), RegularExpressionFunctions.anyURIRegexpMatch(regexp, input));
	}
	
	@Test
	public void testrfc822NameRegExpMatch() throws EvaluationException
	{
		StringValue regexp = XacmlDataTypes.STRING.create(".*@comcast.net");
		RFC822NameValue input = XacmlDataTypes.RFC822NAME.create("trumpyla@comcast.net");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), RegularExpressionFunctions.rfc822NameRegexpMatch(regexp, input));
	}
	
	
	
}
