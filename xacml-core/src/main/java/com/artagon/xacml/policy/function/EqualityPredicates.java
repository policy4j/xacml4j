package com.artagon.xacml.policy.function;


import com.artagon.xacml.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.policy.function.annotations.XacmlFuncParam;
import com.artagon.xacml.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.AnyURIType.AnyURIValue;
import com.artagon.xacml.policy.type.Base64BinaryType.Base64BinaryValue;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.policy.type.DateTimeType.DateTimeValue;
import com.artagon.xacml.policy.type.DateType.DateValue;
import com.artagon.xacml.policy.type.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.policy.type.HexBinaryType.HexBinaryValue;
import com.artagon.xacml.policy.type.IntegerType.IntegerValue;
import com.artagon.xacml.policy.type.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.policy.type.StringType.StringValue;
import com.artagon.xacml.policy.type.TimeType.TimeValue;
import com.artagon.xacml.policy.type.X500NameType.X500NameValue;
import com.artagon.xacml.policy.type.YearMonthDurationType.YearMonthDurationValue;

/**
 * An implementation of XACML equality predicates
 * 
 * @author Giedrius Trumpickas
 */
public class EqualityPredicates 
{
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.ANYURI)AnyURIValue a, 
			@XacmlFuncParam(type=XacmlDataType.ANYURI)AnyURIValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue a, 
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.BOOLEAN)BooleanValue a, 
			@XacmlFuncParam(type=XacmlDataType.BOOLEAN)BooleanValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}

	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.X500NAME)X500NameValue a, 
			@XacmlFuncParam(type=XacmlDataType.X500NAME)X500NameValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.STRING)StringValue a, 
			@XacmlFuncParam(type=XacmlDataType.STRING)StringValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equalsIgnoreCase(
			@XacmlFuncParam(type=XacmlDataType.STRING)StringValue a, 
			@XacmlFuncParam(type=XacmlDataType.STRING)StringValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equalsIgnoreCase(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:date-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.DATE)DateValue a, 
			@XacmlFuncParam(type=XacmlDataType.DATE)DateValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.DATETIME)DateTimeValue a, 
			@XacmlFuncParam(type=XacmlDataType.DATETIME)DateTimeValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:time-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.DATETIME)TimeValue a, 
			@XacmlFuncParam(type=XacmlDataType.DATETIME)TimeValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.DAYTIMEDURATION)DayTimeDurationValue a, 
			@XacmlFuncParam(type=XacmlDataType.DAYTIMEDURATION)DayTimeDurationValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.YEARMONTHDURATION)YearMonthDurationValue a, 
			@XacmlFuncParam(type=XacmlDataType.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.RFC822NAME)RFC822NameValue a, 
			@XacmlFuncParam(type=XacmlDataType.RFC822NAME)RFC822NameValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.HEXBINARY)HexBinaryValue a, 
			@XacmlFuncParam(type=XacmlDataType.HEXBINARY)HexBinaryValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.BASE64BINARY)Base64BinaryValue a, 
			@XacmlFuncParam(type=XacmlDataType.BASE64BINARY)Base64BinaryValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
}
