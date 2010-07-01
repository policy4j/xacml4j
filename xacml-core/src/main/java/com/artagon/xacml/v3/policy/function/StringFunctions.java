package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.AnyURIType;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.DNSNameType;
import com.artagon.xacml.v3.types.DateTimeType;
import com.artagon.xacml.v3.types.DateType;
import com.artagon.xacml.v3.types.DayTimeDurationType;
import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.IPAddressType;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.RFC822NameType;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.TimeType;
import com.artagon.xacml.v3.types.X500NameType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.YearMonthDurationType;
import com.artagon.xacml.v3.types.AnyURIType.AnyURIValue;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.StringType.StringValue;

@XacmlFunctionProvider(description="XACML string functions")
public class StringFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:string-concatenate")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringValue concatenate(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		StringBuilder buf = new StringBuilder();
		return XacmlDataTypes.STRING.create(buf.append(a).append(b).toString());
	}
	
	@Deprecated
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:uri-string-concatenate")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringValue uriStringconcatenate(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI)AnyURIValue a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		StringBuilder buf = new StringBuilder();
		return XacmlDataTypes.STRING.create(buf.append(a.toXacmlString()).append(b).toString());
	}
	
	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-starts-with")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue startsWith(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue().startsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-starts-with")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue uriStartsWith(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI)AnyURIValue a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.toXacmlString().startsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-ends-with")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue endsWith(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue().endsWith(b.getValue()));
	}
		
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-ends-with")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue uriEndsWith(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI)AnyURIValue a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.toXacmlString().endsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-contains")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue contains(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue().contains(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-contains")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue uriContains(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI)AnyURIValue a,
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.toXacmlString().contains(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromBoolean(
			@XacmlFuncParam(type=XacmlDataTypes.BOOLEAN)BooleanType.BooleanValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanType.BooleanValue booleanFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.BOOLEAN.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-integer")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromInteger(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:integer-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerType.IntegerValue integerFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.INTEGER.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-double")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromDouble(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleType.DoubleValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:double-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static DoubleType.DoubleValue doubleFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.DOUBLE.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-time")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromTime(
			@XacmlFuncParam(type=XacmlDataTypes.TIME)TimeType.TimeValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.TIME)
	public static TimeType.TimeValue timeFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.TIME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-date")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromDate(
			@XacmlFuncParam(type=XacmlDataTypes.DATE)DateType.DateValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATE)
	public static DateType.DateValue dateFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.DATE.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dateTime")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromDateTime(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME)DateTimeType.DateTimeValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME)
	public static DateTimeType.DateTimeValue dateTimeFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.DATETIME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-anyURI")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromAnyURI(
			@XacmlFuncParam(type=XacmlDataTypes.ANYURI)AnyURIType.AnyURIValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.ANYURI)
	public static AnyURIType.AnyURIValue anyURIFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.ANYURI.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dayTimeDuration")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromDayTimeDuration(
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION)DayTimeDurationType.DayTimeDurationValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.DAYTIMEDURATION)
	public static DayTimeDurationType.DayTimeDurationValue dayTimeDurationFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.DAYTIMEDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-yearMonthDuration")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromYearMonthDuration(
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION)YearMonthDurationType.YearMonthDurationValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.YEARMONTHDURATION)
	public static DayTimeDurationType.DayTimeDurationValue yearMonthDurationFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.YEARMONTHDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-x500Name")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromX500Name(
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME)X500NameType.X500NameValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.X500NAME)
	public static X500NameType.X500NameValue x500NameFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.X500NAME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-rfc822Name")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromRfc822Name(
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME)RFC822NameType.RFC822NameValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.RFC822NAME)
	public static RFC822NameType.RFC822NameValue rfc822NameFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.RFC822NAME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-ipAddress")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromIpAddress(
			@XacmlFuncParam(type=XacmlDataTypes.IPADDRESS)IPAddressType.IPAddressValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.IPADDRESS)
	public static IPAddressType.IPAddressValue ipAddressFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.IPADDRESS.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dnsName")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringType.StringValue stringFromDnsName(
			@XacmlFuncParam(type=XacmlDataTypes.DNSNAME)DNSNameType.DNSNameValue v)
	{
		return XacmlDataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string")
	@XacmlFuncReturnType(type=XacmlDataTypes.DNSNAME)
	public static DNSNameType.DNSNameValue dnsNameFromString(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringType.StringValue v)
	{
		return XacmlDataTypes.DNSNAME.fromXacmlString(v.getValue());
	}

}
