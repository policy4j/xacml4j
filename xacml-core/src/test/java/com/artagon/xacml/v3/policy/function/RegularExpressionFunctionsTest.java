package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.AnyURIType;
import com.artagon.xacml.v3.types.AnyURIValue;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.RFC822NameType;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.StringValue;

public class RegularExpressionFunctionsTest 
{
	
	@Test
	public void testFunctionIfImplemented() throws Exception
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
		StringValue regexp1 = StringType.STRING.create("   This  is n*o*t* *IT!  ");
		StringValue regexp2 = StringType.STRING.create("  *This .*is not IT! *");
		StringValue input1 = StringType.STRING.create("   This  is not IT!  ");
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp1, input1));
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp2, input1));
	}
	
	@Test
	public void testRegExpMatchFromIIC168ConformanceTest()
	{
		StringValue regexp1 = StringType.STRING.create("   This  is n*o*t* *IT!  ");
		StringValue input1 = StringType.STRING.create("   This  is IT!  ");
		StringValue input2 = StringType.STRING.create("   This  is not IT!  ");  
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp1, input1));
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp1, input2));
	}
	
	@Test
	public void testStringRegExpMatch() throws EvaluationException
	{
		StringValue regexp = StringType.STRING.create("G*,Trumpickas");
		StringValue input = StringType.STRING.create("Giedrius,Trumpickas");
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp, input));     
	}
	
	@Test
	public void testAnyURIRegExpMatch() throws EvaluationException
	{
		StringValue regexp = StringType.STRING.create("http://www.test.org/public/*");
		AnyURIValue input = AnyURIType.ANYURI.create("http://www.test.org/public/test/a");
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.anyURIRegexpMatch(regexp, input));
	}
	
	@Test
	public void testrfc822NameRegExpMatch() throws EvaluationException
	{
		StringValue regexp = StringType.STRING.create(".*@comcast.net");
		RFC822NameValue input = RFC822NameType.Factory.create("trumpyla@comcast.net");
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.rfc822NameRegexpMatch(regexp, input));
	}
	
	
	
}
