package com.artagon.xacml.v3.policy.function;


import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.policy.spi.function.XacmlLegacyFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.types.Base64BinaryType.Base64BinaryValue;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.types.DateType.DateValue;
import com.artagon.xacml.v3.types.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.HexBinaryType.HexBinaryValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.TimeType.TimeValue;
import com.artagon.xacml.v3.types.X500NameType.X500NameValue;
import com.artagon.xacml.v3.types.YearMonthDurationType.YearMonthDurationValue;

/**
 * An implementation of XACML equality predicates
 * 
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider
public class EqualityPredicates 
{
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlParam(type=XacmlDataTypes.ANYURI)AnyURIValue a, 
			@XacmlParam(type=XacmlDataTypes.ANYURI)AnyURIValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN)BooleanValue a, 
			@XacmlParam(type=XacmlDataTypes.BOOLEAN)BooleanValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}

	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue eq(
			@XacmlParam(type=XacmlDataTypes.X500NAME)X500NameValue a, 
			@XacmlParam(type=XacmlDataTypes.X500NAME)X500NameValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue a, 
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equalsIgnoreCase(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue a, 
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equalsIgnoreCase(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:date-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue a, 
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:time-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=XacmlDataTypes.DATETIME)TimeValue a, 
			@XacmlParam(type=XacmlDataTypes.DATETIME)TimeValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION)DayTimeDurationValue a, 
			@XacmlParam(type=XacmlDataTypes.DAYTIMEDURATION)DayTimeDurationValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION)YearMonthDurationValue a, 
			@XacmlParam(type=XacmlDataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=XacmlDataTypes.RFC822NAME)RFC822NameValue a, 
			@XacmlParam(type=XacmlDataTypes.RFC822NAME)RFC822NameValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=XacmlDataTypes.HEXBINARY)HexBinaryValue a, 
			@XacmlParam(type=XacmlDataTypes.HEXBINARY)HexBinaryValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY)Base64BinaryValue a, 
			@XacmlParam(type=XacmlDataTypes.BASE64BINARY)Base64BinaryValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
}
