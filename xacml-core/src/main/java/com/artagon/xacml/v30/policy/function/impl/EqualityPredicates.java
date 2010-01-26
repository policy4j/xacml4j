package com.artagon.xacml.v30.policy.function.impl;


import com.artagon.xacml.v30.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.v30.policy.function.annotations.XacmlFuncParam;
import com.artagon.xacml.v30.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v30.policy.type.DataTypes;
import com.artagon.xacml.v30.policy.type.AnyURIType.AnyURIValue;
import com.artagon.xacml.v30.policy.type.Base64BinaryType.Base64BinaryValue;
import com.artagon.xacml.v30.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v30.policy.type.DateTimeType.DateTimeValue;
import com.artagon.xacml.v30.policy.type.DateType.DateValue;
import com.artagon.xacml.v30.policy.type.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v30.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.v30.policy.type.HexBinaryType.HexBinaryValue;
import com.artagon.xacml.v30.policy.type.IntegerType.IntegerValue;
import com.artagon.xacml.v30.policy.type.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v30.policy.type.StringType.StringValue;
import com.artagon.xacml.v30.policy.type.TimeType.TimeValue;
import com.artagon.xacml.v30.policy.type.X500NameType.X500NameValue;
import com.artagon.xacml.v30.policy.type.YearMonthDurationType.YearMonthDurationValue;

/**
 * An implementation of XACML equality predicates
 * 
 * @author Giedrius Trumpickas
 */
public class EqualityPredicates 
{
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerValue a, 
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=DataTypes.ANYURI)AnyURIValue a, 
			@XacmlFuncParam(type=DataTypes.ANYURI)AnyURIValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue a, 
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlFuncParam(type=DataTypes.BOOLEAN)BooleanValue a, 
			@XacmlFuncParam(type=DataTypes.BOOLEAN)BooleanValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}

	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue eq(
			@XacmlFuncParam(type=DataTypes.X500NAME)X500NameValue a, 
			@XacmlFuncParam(type=DataTypes.X500NAME)X500NameValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=DataTypes.STRING)StringValue a, 
			@XacmlFuncParam(type=DataTypes.STRING)StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equalsIgnoreCase(
			@XacmlFuncParam(type=DataTypes.STRING)StringValue a, 
			@XacmlFuncParam(type=DataTypes.STRING)StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.equalsIgnoreCase(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:date-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=DataTypes.DATE)DateValue a, 
			@XacmlFuncParam(type=DataTypes.DATE)DateValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlFuncParam(type=DataTypes.DATETIME)DateTimeValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:time-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=DataTypes.DATETIME)TimeValue a, 
			@XacmlFuncParam(type=DataTypes.DATETIME)TimeValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=DataTypes.DAYTIMEDURATION)DayTimeDurationValue a, 
			@XacmlFuncParam(type=DataTypes.DAYTIMEDURATION)DayTimeDurationValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationValue a, 
			@XacmlFuncParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=DataTypes.RFC822NAME)RFC822NameValue a, 
			@XacmlFuncParam(type=DataTypes.RFC822NAME)RFC822NameValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=DataTypes.HEXBINARY)HexBinaryValue a, 
			@XacmlFuncParam(type=DataTypes.HEXBINARY)HexBinaryValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlFuncParam(type=DataTypes.BASE64BINARY)Base64BinaryValue a, 
			@XacmlFuncParam(type=DataTypes.BASE64BINARY)Base64BinaryValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
}
