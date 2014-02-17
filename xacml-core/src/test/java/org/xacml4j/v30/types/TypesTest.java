package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.xacml4j.v30.types.AnyURIType.ANYURI;
import static org.xacml4j.v30.types.Base64BinaryType.BASE64BINARY;
import static org.xacml4j.v30.types.BooleanType.BOOLEAN;
import static org.xacml4j.v30.types.DNSNameType.DNSNAME;
import static org.xacml4j.v30.types.DateTimeType.DATETIME;
import static org.xacml4j.v30.types.DateType.DATE;
import static org.xacml4j.v30.types.DayTimeDurationType.DAYTIMEDURATION;
import static org.xacml4j.v30.types.DoubleType.DOUBLE;
import static org.xacml4j.v30.types.HexBinaryType.HEXBINARY;
import static org.xacml4j.v30.types.IPAddressType.IPADDRESS;
import static org.xacml4j.v30.types.IntegerType.INTEGER;
import static org.xacml4j.v30.types.RFC822NameType.RFC822NAME;
import static org.xacml4j.v30.types.StringType.STRING;
import static org.xacml4j.v30.types.TimeType.TIME;
import static org.xacml4j.v30.types.XPathExpType.XPATHEXPRESSION;
import static org.xacml4j.v30.types.YearMonthDurationType.YEARMONTHDURATION;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.XacmlSyntaxException;



public class TypesTest
{
	private Types types;

	@Before
	public void init(){
		this.types = Types.builder().defaultTypes().create();
	}

	@Test
	public void testXACML20DeprecatedTypeMapping()
	{
		assertNotNull(types.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#yearMonthDuration"));
		assertNotNull(types.getType("urn:oasis:names:tc:xacml:2.0:data-type:xpathExpression"));
		assertNotNull(types.getType("http://www.w3.org/TR/2002/WD-xquery-operators-20020816#dayTimeDuration"));
	}

	@Test(expected=XacmlSyntaxException.class)
	public void testUknownType()
	{
		types.getType("TypeDoesNotExist");
	}
	
	@Test
	public void testAddCapability()
	{
		TestCapability c0 = new TestCapability() {
		};
		TestCapability c1 = new TestCapability() {
		};
		types.addCapability(ANYURI, TestCapability.class, c0);
		assertSame(c0, types.getCapability(ANYURI, TestCapability.class));
		types.addCapability(ANYURI, TestCapability.class, c1);
		assertSame(c0, types.getCapability(ANYURI, TestCapability.class));
	}

	@Test
	public void testDefaultTypes()
	{
		assertEquals(ANYURI, types.getType("http://www.w3.org/2001/XMLSchema#anyURI"));
		assertEquals(BASE64BINARY, types.getType("http://www.w3.org/2001/XMLSchema#base64Binary"));
		assertEquals(BOOLEAN, types.getType(BOOLEAN.getDataTypeId()));
		assertEquals(DATE, types.getType(DATE.getDataTypeId()));
		assertEquals(DATETIME, types.getType(DATETIME.getDataTypeId()));
		assertEquals(DAYTIMEDURATION, types.getType(DAYTIMEDURATION.getDataTypeId()));
		assertEquals(DNSNAME, types.getType(DNSNAME.getDataTypeId()));
		assertEquals(DOUBLE, types.getType(DOUBLE.getDataTypeId()));
		assertEquals(HEXBINARY, types.getType(HEXBINARY.getDataTypeId()));
		assertEquals(INTEGER, types.getType(INTEGER.getDataTypeId()));
		assertEquals(IPADDRESS, types.getType(IPADDRESS.getDataTypeId()));
		assertEquals(RFC822NAME, types.getType(RFC822NAME.getDataTypeId()));
		assertEquals(STRING, types.getType(STRING.getDataTypeId()));
		assertEquals(TIME, types.getType(TIME.getDataTypeId()));
		assertEquals(XPATHEXPRESSION, types.getType(XPATHEXPRESSION.getDataTypeId()));
		assertEquals(YEARMONTHDURATION, types.getType(YEARMONTHDURATION.getDataTypeId()));
	}
	
	public interface TestCapability extends TypeCapability
	{
		
	}

}
