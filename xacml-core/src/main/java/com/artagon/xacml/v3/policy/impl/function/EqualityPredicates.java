package com.artagon.xacml.v3.policy.impl.function;


import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.policy.type.Base64BinaryType.Base64BinaryValue;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.policy.type.DateType.DateValue;
import com.artagon.xacml.v3.policy.type.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.v3.policy.type.HexBinaryType.HexBinaryValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;
import com.artagon.xacml.v3.policy.type.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;
import com.artagon.xacml.v3.policy.type.TimeType.TimeValue;
import com.artagon.xacml.v3.policy.type.X500NameType.X500NameValue;
import com.artagon.xacml.v3.policy.type.YearMonthDurationType.YearMonthDurationValue;

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
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlParam(type=DataTypes.ANYURI)AnyURIValue a, 
			@XacmlParam(type=DataTypes.ANYURI)AnyURIValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue eq(
			@XacmlParam(type=DataTypes.BOOLEAN)BooleanValue a, 
			@XacmlParam(type=DataTypes.BOOLEAN)BooleanValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}

	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue eq(
			@XacmlParam(type=DataTypes.X500NAME)X500NameValue a, 
			@XacmlParam(type=DataTypes.X500NAME)X500NameValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=DataTypes.STRING)StringValue a, 
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equalsIgnoreCase(
			@XacmlParam(type=DataTypes.STRING)StringValue a, 
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.equalsIgnoreCase(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:date-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=DataTypes.DATE)DateValue a, 
			@XacmlParam(type=DataTypes.DATE)DateValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:time-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=DataTypes.DATETIME)TimeValue a, 
			@XacmlParam(type=DataTypes.DATETIME)TimeValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=DataTypes.DAYTIMEDURATION)DayTimeDurationValue a, 
			@XacmlParam(type=DataTypes.DAYTIMEDURATION)DayTimeDurationValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationValue a, 
			@XacmlParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=DataTypes.RFC822NAME)RFC822NameValue a, 
			@XacmlParam(type=DataTypes.RFC822NAME)RFC822NameValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=DataTypes.HEXBINARY)HexBinaryValue a, 
			@XacmlParam(type=DataTypes.HEXBINARY)HexBinaryValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue equals(
			@XacmlParam(type=DataTypes.BASE64BINARY)Base64BinaryValue a, 
			@XacmlParam(type=DataTypes.BASE64BINARY)Base64BinaryValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
}
