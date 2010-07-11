package com.artagon.xacml.v3.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.spi.FunctionProvider;
import com.artagon.xacml.v3.spi.function.AnnotiationBasedFunctionProvider;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class SpecialMatchFunctionsTest 
{
	private FunctionProvider p;
	
	@Before
	public void init(){
		this.p = new AnnotiationBasedFunctionProvider(SpecialMatchFunctions.class);
	}
	
	@Test
	public void testIfFunctionImplemented()
	{
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-match"));
	}
	
	@Test
	public void testRFC822NameMatch()
	{
		StringValue p = XacmlDataTypes.STRING.create(".sun.com");
		RFC822NameValue n = XacmlDataTypes.RFC822NAME.create("test@east.sun.com");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), SpecialMatchFunctions.rfc822NameMatch(p, n));
		
		p = XacmlDataTypes.STRING.create("sun.com");
		n = XacmlDataTypes.RFC822NAME.create("test@sun.com");
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), SpecialMatchFunctions.rfc822NameMatch(p, n));
	}
}
