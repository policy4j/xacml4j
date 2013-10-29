package org.xacml4j.v30.policy.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.spi.function.AnnotiationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.BooleanType;
import org.xacml4j.v30.types.RFC822NameExp;
import org.xacml4j.v30.types.RFC822NameType;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.StringType;
import org.xacml4j.v30.types.X500NameExp;
import org.xacml4j.v30.types.X500NameType;


public class SpecialMatchFunctionsTest
{
	private FunctionProvider p;

	@Before
	public void init() throws Exception
	{
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
		StringExp p = StringType.STRING.create(".sun.com");
		RFC822NameExp n = RFC822NameType.RFC822NAME.create("test@east.sun.com");
		assertEquals(BooleanType.BOOLEAN.create(true), SpecialMatchFunctions.rfc822NameMatch(p, n));

		p = StringType.STRING.create("sun.com");
		n = RFC822NameType.RFC822NAME.create("test@sun.com");
		assertEquals(BooleanType.BOOLEAN.create(true), SpecialMatchFunctions.rfc822NameMatch(p, n));
	}

	@Test
	public void testX500NameMatch()
	{
		X500NameExp a = X500NameType.X500NAME.create("ou=org,o=com");
		X500NameExp b = X500NameType.X500NAME.create("cn=test, ou=org,o=com");

		assertEquals(BooleanType.BOOLEAN.create(true), SpecialMatchFunctions.x500NameMatch(a, b));

		a = X500NameType.X500NAME.create("ou=org,o=com");
		b = X500NameType.X500NAME.create("cn=test, ou=ORG,o=Com");

		assertEquals(BooleanType.BOOLEAN.create(true), SpecialMatchFunctions.x500NameMatch(a, b));

		a = X500NameType.X500NAME.create("ou=org1,o=com");
		b = X500NameType.X500NAME.create("cn=test, ou=ORG,o=com");

		assertEquals(BooleanType.BOOLEAN.create(false), SpecialMatchFunctions.x500NameMatch(a, b));

	}
}
