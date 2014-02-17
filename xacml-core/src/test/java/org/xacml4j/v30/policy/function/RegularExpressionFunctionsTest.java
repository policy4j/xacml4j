package org.xacml4j.v30.policy.function;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
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
import org.xacml4j.v30.types.Types;


public class RegularExpressionFunctionsTest
{
	private IMocksControl c;
	private EvaluationContext context;
	private Types types;
	
	@Before
	public void init(){
		c = createControl();
		this.context = c.createMock(EvaluationContext.class);
		this.types = Types.builder().defaultTypes().create();
	}
	
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
		expect(context.getTypes()).andReturn(types);
		c.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(context, regexp1, input1));
		c.verify();
		c.reset();
		expect(context.getTypes()).andReturn(types);
		c.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(context, regexp2, input1));
		c.verify();
	}

	@Test
	public void testRegExpMatchFromIIC168ConformanceTest()
	{
		StringExp regexp1 = StringType.STRING.create("   This  is n*o*t* *IT!  ");
		StringExp input1 = StringType.STRING.create("   This  is IT!  ");
		StringExp input2 = StringType.STRING.create("   This  is not IT!  ");
		expect(context.getTypes()).andReturn(types);
		c.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(context, regexp1, input1));
		c.verify();
		c.reset();
		expect(context.getTypes()).andReturn(types);
		c.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(context, regexp1, input2));
		c.verify();
	}

	@Test
	public void testStringRegExpMatch() throws EvaluationException
	{
		StringExp regexp = StringType.STRING.create("G*,Trumpickas");
		StringExp input = StringType.STRING.create("Giedrius,Trumpickas");
		expect(context.getTypes()).andReturn(types);
		c.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.stringRegexpMatch(context, regexp, input));
		c.verify();
	}

	@Test
	public void testAnyURIRegExpMatch() throws EvaluationException
	{
		StringExp regexp = StringType.STRING.create("http://www.test.org/public/*");
		AnyURIExp input = AnyURIType.ANYURI.fromAny("http://www.test.org/public/test/a");
		expect(context.getTypes()).andReturn(types);
		c.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.anyURIRegexpMatch(context, regexp, input));
		c.verify();
		
	}

	@Test
	public void testrfc822NameRegExpMatch() throws EvaluationException
	{
		StringExp regexp = StringType.STRING.create(".*@comcast.net");
		RFC822NameExp input = RFC822NameType.RFC822NAME.create("trumpyla@comcast.net");
		expect(context.getTypes()).andReturn(types);
		c.replay();
		assertEquals(BooleanType.BOOLEAN.create(true), RegularExpressionFunctions.rfc822NameRegexpMatch(context, regexp, input));
		c.verify();
		
	}



}
