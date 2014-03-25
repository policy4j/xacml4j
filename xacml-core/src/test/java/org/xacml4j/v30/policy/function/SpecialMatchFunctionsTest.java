package org.xacml4j.v30.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.RFC822NameExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.X500NameExp;


public class SpecialMatchFunctionsTest
{
	private FunctionProvider p;

	@Before
	public void init() throws Exception
	{
		this.p = new AnnotiationBasedFunctionProvider(
				SpecialMatchFunctions.class);
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
		StringExp p = StringExp.valueOf(".sun.com");
		RFC822NameExp n = RFC822NameExp.valueOf("test@east.sun.com");
		assertEquals(BooleanExp.valueOf(true), SpecialMatchFunctions.rfc822NameMatch(p, n));

		p = StringExp.valueOf("sun.com");
		n = RFC822NameExp.valueOf("test@sun.com");
		assertEquals(BooleanExp.valueOf(true), SpecialMatchFunctions.rfc822NameMatch(p, n));
	}

	@Test
	public void testX500NameMatch()
	{
		X500NameExp a = X500NameExp.valueOf("ou=org,o=com");
		X500NameExp b = X500NameExp.valueOf("cn=test, ou=org,o=com");

		assertEquals(BooleanExp.valueOf(true), SpecialMatchFunctions.x500NameMatch(a, b));

		a = X500NameExp.valueOf("ou=org,o=com");
		b = X500NameExp.valueOf("cn=test, ou=ORG,o=Com");

		assertEquals(BooleanExp.valueOf(true), SpecialMatchFunctions.x500NameMatch(a, b));

		a = X500NameExp.valueOf("ou=org1,o=com");
		b = X500NameExp.valueOf("cn=test, ou=ORG,o=com");

		assertEquals(BooleanExp.valueOf(false), SpecialMatchFunctions.x500NameMatch(a, b));

	}
}
