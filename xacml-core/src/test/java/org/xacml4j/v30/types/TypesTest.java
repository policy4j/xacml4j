package org.xacml4j.v30.types;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import static org.xacml4j.v30.types.AnyURIType.ANYURI;
import static org.xacml4j.v30.types.Base64BinaryType.BASE64BINARY;
import static org.xacml4j.v30.types.BooleanType.BOOLEAN;
import static org.xacml4j.v30.types.DateType.DATE;
import static org.xacml4j.v30.types.DateTimeType.DATETIME;
import static org.xacml4j.v30.types.DayTimeDurationType.DAYTIMEDURATION;
import static org.xacml4j.v30.types.DNSNameType.DNSNAME;
import static org.xacml4j.v30.types.DoubleType.DOUBLE;
import static org.xacml4j.v30.types.HexBinaryType.HEXBINARY;
import static org.xacml4j.v30.types.IntegerType.INTEGER;
import static org.xacml4j.v30.types.IPAddressType.IPADDRESS;
import static org.xacml4j.v30.types.RFC822NameType.RFC822NAME;
import static org.xacml4j.v30.types.StringType.STRING;


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
	
	@Test
	public void testDefaultTypes()
	{
		assertEquals(ANYURI, types.getType(ANYURI.getDataTypeId()));
		assertEquals(BASE64BINARY, types.getType(BASE64BINARY.getDataTypeId()));
		assertEquals(BOOLEAN, types.getType(BOOLEAN.getDataTypeId()));
		assertEquals(DATE, types.getType(DATE.getDataTypeId()));
		assertEquals(DATETIME, types.getType(DATETIME.getDataTypeId()));
		assertEquals(DAYTIMEDURATION, types.getType(DAYTIMEDURATION.getDataTypeId()));
		assertEquals(DNSNAME, types.getType(DNSNAME.getDataTypeId()));
		assertEquals(DOUBLE, types.getType(DOUBLE.getDataTypeId()));
		assertEquals(INTEGER, types.getType(INTEGER.getDataTypeId()));
		assertEquals(IPADDRESS, types.getType(IPADDRESS.getDataTypeId()));
		assertEquals(RFC822NAME, types.getType(RFC822NAME.getDataTypeId()));
		assertEquals(STRING, types.getType(STRING.getDataTypeId()));
	}
	
}
