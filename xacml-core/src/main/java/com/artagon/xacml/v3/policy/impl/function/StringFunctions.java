package com.artagon.xacml.v3.policy.impl.function;

import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.policy.type.AnyURIType;
import com.artagon.xacml.v3.policy.type.BooleanType;
import com.artagon.xacml.v3.policy.type.DNSNameType;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.DateTimeType;
import com.artagon.xacml.v3.policy.type.DateType;
import com.artagon.xacml.v3.policy.type.DayTimeDurationType;
import com.artagon.xacml.v3.policy.type.DoubleType;
import com.artagon.xacml.v3.policy.type.IPAddressType;
import com.artagon.xacml.v3.policy.type.IntegerType;
import com.artagon.xacml.v3.policy.type.RFC822NameType;
import com.artagon.xacml.v3.policy.type.StringType;
import com.artagon.xacml.v3.policy.type.TimeType;
import com.artagon.xacml.v3.policy.type.X500NameType;
import com.artagon.xacml.v3.policy.type.YearMonthDurationType;
import com.artagon.xacml.v3.policy.type.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;

@XacmlFunctionProvider
public class StringFunctions 
{
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:2.0:function:string-concatenate")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringValue concatenate(
			@XacmlParam(type=DataTypes.STRING)StringValue a,
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		StringBuilder buf = new StringBuilder();
		return DataTypes.STRING.create(buf.append(a).append(b).toString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-starts-with")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue startsWith(
			@XacmlParam(type=DataTypes.STRING)StringValue a,
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue().startsWith(b.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:uri-starts-with")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue uriStartsWith(
			@XacmlParam(type=DataTypes.ANYURI)AnyURIValue a,
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.toXacmlString().startsWith(b.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-ends-with")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue endsWith(
			@XacmlParam(type=DataTypes.STRING)StringValue a,
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue().endsWith(b.getValue()));
	}
		
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:uri-ends-with")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue uriEndsWith(
			@XacmlParam(type=DataTypes.ANYURI)AnyURIValue a,
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.toXacmlString().endsWith(b.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-contains")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue contains(
			@XacmlParam(type=DataTypes.STRING)StringValue a,
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue().contains(b.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:uri-contains")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue uriContains(
			@XacmlParam(type=DataTypes.ANYURI)AnyURIValue a,
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.toXacmlString().contains(b.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromBoolean(
			@XacmlParam(type=DataTypes.BOOLEAN)BooleanType.BooleanValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanType.BooleanValue booleanFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.BOOLEAN.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-from-integer")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromInteger(
			@XacmlParam(type=DataTypes.INTEGER)IntegerType.IntegerValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:integer-from-string")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerType.IntegerValue integerFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.INTEGER.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-double")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromDouble(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleType.DoubleValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:double-from-string")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleType.DoubleValue doubleFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.DOUBLE.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-time")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromTime(
			@XacmlParam(type=DataTypes.TIME)TimeType.TimeValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:time-from-string")
	@XacmlFuncReturnType(type=DataTypes.TIME)
	public static TimeType.TimeValue timeFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.TIME.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-date")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromDate(
			@XacmlParam(type=DataTypes.DATE)DateType.DateValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:date-from-string")
	@XacmlFuncReturnType(type=DataTypes.DATE)
	public static DateType.DateValue dateFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.DATE.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-dateTime")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromDateTime(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeType.DateTimeValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string")
	@XacmlFuncReturnType(type=DataTypes.DATETIME)
	public static DateTimeType.DateTimeValue dateTimeFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.DATETIME.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-anyURI")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromAnyURI(
			@XacmlParam(type=DataTypes.ANYURI)AnyURIType.AnyURIValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string")
	@XacmlFuncReturnType(type=DataTypes.ANYURI)
	public static AnyURIType.AnyURIValue anyURIFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.ANYURI.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-dayTimeDuration")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromDayTimeDuration(
			@XacmlParam(type=DataTypes.DAYTIMEDURATION)DayTimeDurationType.DayTimeDurationValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string")
	@XacmlFuncReturnType(type=DataTypes.DAYTIMEDURATION)
	public static DayTimeDurationType.DayTimeDurationValue dayTimeDurationFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.DAYTIMEDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-yearMonthDuration")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromYearMonthDuration(
			@XacmlParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationType.YearMonthDurationValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string")
	@XacmlFuncReturnType(type=DataTypes.YEARMONTHDURATION)
	public static DayTimeDurationType.DayTimeDurationValue yearMonthDurationFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.YEARMONTHDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-x500Name")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromX500Name(
			@XacmlParam(type=DataTypes.X500NAME)X500NameType.X500NameValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string")
	@XacmlFuncReturnType(type=DataTypes.X500NAME)
	public static X500NameType.X500NameValue x500NameFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.X500NAME.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-rfc822Name")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromRfc822Name(
			@XacmlParam(type=DataTypes.RFC822NAME)RFC822NameType.RFC822NameValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string")
	@XacmlFuncReturnType(type=DataTypes.RFC822NAME)
	public static RFC822NameType.RFC822NameValue rfc822NameFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.RFC822NAME.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-ipAddress")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromIpAddress(
			@XacmlParam(type=DataTypes.IPADDRESS)IPAddressType.IPAddressValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string")
	@XacmlFuncReturnType(type=DataTypes.IPADDRESS)
	public static IPAddressType.IPAddressValue ipAddressFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.IPADDRESS.fromXacmlString(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:string-from-dnsName")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromDnsName(
			@XacmlParam(type=DataTypes.DNSNAME)DNSNameType.DNSNameValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string")
	@XacmlFuncReturnType(type=DataTypes.DNSNAME)
	public static DNSNameType.DNSNameValue dnsNameFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.DNSNAME.fromXacmlString(v.getValue());
	}

}
