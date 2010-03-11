package com.artagon.xacml.v3.policy.impl.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.spi.FunctionFactory;
import com.artagon.xacml.v3.policy.spi.function.AnnotationBasedFunctionFactory;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.policy.type.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;

public class RegularExpressionFunctionsTest 
{
	
	@Test
	public void testFunctionIfImplemented()
	{
		FunctionFactory f = new AnnotationBasedFunctionFactory(RegularExpressionFunctions.class);
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:ipAddress-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dnsName-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-regexp-match"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-regexp-match"));
		
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
		StringValue regexp = DataTypes.STRING.create("^*@comcast.net");
		RFC822NameValue input = DataTypes.RFC822NAME.create("trumpyla@comcast.net");
		assertEquals(DataTypes.BOOLEAN.create(true), RegularExpressionFunctions.rfc822NameRegexpMatch(regexp, input));
	}
	
	
}
