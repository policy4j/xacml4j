package com.artagon.xacml.policy.function;


import com.artagon.xacml.policy.type.AnyURIType;
import com.artagon.xacml.policy.type.Base64BinaryType;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.DateTimeType;
import com.artagon.xacml.policy.type.DateType;
import com.artagon.xacml.policy.type.DayTimeDurationType;
import com.artagon.xacml.policy.type.DoubleType;
import com.artagon.xacml.policy.type.HexBinaryType;
import com.artagon.xacml.policy.type.IntegerType;
import com.artagon.xacml.policy.type.RFC822NameType;
import com.artagon.xacml.policy.type.StringType;
import com.artagon.xacml.policy.type.TimeType;
import com.artagon.xacml.policy.type.X500NameType;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.YearMonthDurationType;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;

import com.artagon.xacml.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.policy.function.annotations.XacmlFuncParam;
import com.artagon.xacml.policy.function.annotations.XacmlFuncReturnType;

public class EqualityPredicates 
{
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:1.0:function:integer-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerType.IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerType.IntegerValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.ANYURI)AnyURIType.AnyURIValue a, 
			@XacmlFuncParam(type=XacmlDataType.ANYURI)AnyURIType.AnyURIValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:1.0:function:double-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleType.DoubleValue a, 
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleType.DoubleValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.BOOLEAN)BooleanType.BooleanValue a, 
			@XacmlFuncParam(type=XacmlDataType.BOOLEAN)BooleanType.BooleanValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}

	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue eq(
			@XacmlFuncParam(type=XacmlDataType.X500NAME)X500NameType.X500NameValue a, 
			@XacmlFuncParam(type=XacmlDataType.X500NAME)X500NameType.X500NameValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:1.0:function:string-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.STRING)StringType.StringValue a, 
			@XacmlFuncParam(type=XacmlDataType.STRING)StringType.StringValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equalsIgnoreCase(
			@XacmlFuncParam(type=XacmlDataType.STRING)StringType.StringValue a, 
			@XacmlFuncParam(type=XacmlDataType.STRING)StringType.StringValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equalsIgnoreCase(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:3.0:function:date-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.DATE)DateType.DateValue a, 
			@XacmlFuncParam(type=XacmlDataType.DATE)DateType.DateValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:3.0:function:dateTime-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.DATETIME)DateTimeType.DateTimeValue a, 
			@XacmlFuncParam(type=XacmlDataType.DATETIME)DateTimeType.DateTimeValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:3.0:function:time-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.DATETIME)TimeType.TimeValue a, 
			@XacmlFuncParam(type=XacmlDataType.DATETIME)TimeType.TimeValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.DAYTIMEDURATION)DayTimeDurationType.DayTimeDurationValue a, 
			@XacmlFuncParam(type=XacmlDataType.DAYTIMEDURATION)DayTimeDurationType.DayTimeDurationValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.YEARMONTHDURATION)YearMonthDurationType.YearMonthDurationValue a, 
			@XacmlFuncParam(type=XacmlDataType.YEARMONTHDURATION)YearMonthDurationType.YearMonthDurationValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.RFC822NAME)RFC822NameType.RFC822NameValue a, 
			@XacmlFuncParam(type=XacmlDataType.RFC822NAME)RFC822NameType.RFC822NameValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.HEXBINARY)HexBinaryType.HexBinaryValue a, 
			@XacmlFuncParam(type=XacmlDataType.HEXBINARY)HexBinaryType.HexBinaryValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(functionId="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=XacmlDataType.BASE64BINARY)Base64BinaryType.Base64BinaryValue a, 
			@XacmlFuncParam(type=XacmlDataType.BASE64BINARY)Base64BinaryType.Base64BinaryValue b)
	{
		return XacmlDataType.BOOLEAN.create(a.equals(b));
	}
}
