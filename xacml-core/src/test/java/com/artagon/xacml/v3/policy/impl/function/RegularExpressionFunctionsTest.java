package com.artagon.xacml.v3.policy.impl.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.regex.Pattern;

import org.junit.Test;

import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.policy.spi.FunctionProvider;
import com.artagon.xacml.v3.policy.spi.function.ReflectionBasedFunctionProvider;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.policy.type.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;

public class RegularExpressionFunctionsTest 
{
	
	@Test
	public void testFunctionIfImplemented()
	{
		FunctionProvider f = new ReflectionBasedFunctionProvider(RegularExpressionFunctions.class);
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match"));
		
	}
	
	// converts incorrectly to character class subtraction
    // current parsing is based on substituting -[ with the &&[^
    // without looking at the context where -[ is used
    // in order to handle character class subtraction, we
    // replace all instances of "-[" with "&&[^" in the reg exp
    // SEE: http://sourceforge.net/mailarchive/forum.php?thread_name=4C055316.3080101%40stanford.edu&forum_name=sunxacml-discuss
	@Test
	public void testXacmlRegExptoJERegExpWithCharacterSubstraction()
	{
		// FIXME:
		// converts incorrectly to class subtraction
		// current parsing is base on substituting -[ with the &&[^
		// without looking at the context where -[ is useds
		assertEquals("[0-9]{3}-[0-9]{3}-[0-9]{4}", RegularExpressionFunctions.covertXacmlToJavaSyntax("[0-9]{3}-[0-9]{3}-[0-9]{4}"));
	}
	
	@Test
	public void testStringRegExpMatch() throws EvaluationException
	{
		StringValue regexp = DataTypes.STRING.create("G*,Trumpickas");
		StringValue input = DataTypes.STRING.create("Giedrius,Trumpickas");
		assertEquals(DataTypes.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(regexp, input));
	}
	
	@Test
	public void testAnyURIRegExpMatch() throws EvaluationException
	{
		StringValue regexp = DataTypes.STRING.create("http://www.test.org/public/*");
		AnyURIValue input = DataTypes.ANYURI.create("http://www.test.org/public/test/a");
		assertEquals(DataTypes.BOOLEAN.create(true), RegularExpressionFunctions.anyURIRegexpMatch(regexp, input));
	}
	
	@Test
	public void testrfc822NameRegExpMatch() throws EvaluationException
	{
		StringValue regexp = DataTypes.STRING.create(".*@comcast.net");
		RFC822NameValue input = DataTypes.RFC822NAME.create("trumpyla@comcast.net");
		assertEquals(DataTypes.BOOLEAN.create(true), RegularExpressionFunctions.rfc822NameRegexpMatch(regexp, input));
	}
	
	
}
