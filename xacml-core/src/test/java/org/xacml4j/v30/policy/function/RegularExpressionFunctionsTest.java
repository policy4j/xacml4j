package org.xacml4j.v30.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.AnyURIType;
import org.xacml4j.v30.types.BooleanType;
import org.xacml4j.v30.types.RFC822NameExp;
import org.xacml4j.v30.types.RFC822NameType;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.StringType;


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
		StringExp regexp1 = StringType.STRING.create("   This  is n*o*t* *IT!  ");
		StringExp regexp2 = StringType.STRING.create("  *This .*is not IT! *");
		StringExp input1 = StringType.STRING.create("   This  is not IT!  ");
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp1, input1));
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp2, input1));
	}

	@Test
	public void testRegExpMatchFromIIC168ConformanceTest()
	{
		StringExp regexp1 = StringType.STRING.create("   This  is n*o*t* *IT!  ");
		StringExp input1 = StringType.STRING.create("   This  is IT!  ");
		StringExp input2 = StringType.STRING.create("   This  is not IT!  ");
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp1, input1));
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp1, input2));
	}

	@Test
	public void testStringRegExpMatch() throws EvaluationException
	{
		StringExp regexp = StringType.STRING.create("G*,Trumpickas");
		StringExp input = StringType.STRING.create("Giedrius,Trumpickas");
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp, input));
	}

	@Test
	public void testAnyURIRegExpMatch() throws EvaluationException
	{
		StringExp regexp = StringType.STRING.create("http://www.test.org/public/*");
		AnyURIExp input = AnyURIType.ANYURI.create("http://www.test.org/public/test/a");
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.anyURIRegexpMatch(regexp, input));
	}

	@Test
	public void testrfc822NameRegExpMatch() throws EvaluationException
	{
		StringExp regexp = StringType.STRING.create(".*@comcast.net");
		RFC822NameExp input = RFC822NameType.RFC822NAME.create("trumpyla@comcast.net");
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.rfc822NameRegexpMatch(regexp, input));
	}



}
