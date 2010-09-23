package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.AnyURIType;
import com.artagon.xacml.v3.types.AnyURIValue;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanValue;
import com.artagon.xacml.v3.types.DNSNameType;
import com.artagon.xacml.v3.types.DNSNameValue;
import com.artagon.xacml.v3.types.DateTimeType;
import com.artagon.xacml.v3.types.DateTimeValue;
import com.artagon.xacml.v3.types.DateType;
import com.artagon.xacml.v3.types.DateValue;
import com.artagon.xacml.v3.types.DayTimeDurationType;
import com.artagon.xacml.v3.types.DayTimeDurationValue;
import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.IPAddressType;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.RFC822NameType;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.TimeType;
import com.artagon.xacml.v3.types.X500NameType;
import com.artagon.xacml.v3.types.YearMonthDurationType;
import com.artagon.xacml.v3.types.YearMonthDurationValue;

@XacmlFunctionProvider(description="XACML string functions")
public class StringFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue concatenate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue b)
	{
		StringBuilder buf = new StringBuilder();
		return StringType.Factory.create(buf.append(a).append(b).toString());
	}
	
	@Deprecated
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:uri-string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue uriStringconcatenate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue b)
	{
		StringBuilder buf = new StringBuilder();
		return StringType.Factory.create(buf.append(a.toXacmlString()).append(b).toString());
	}
	
	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue startsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().startsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue uriStartsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue b)
	{
		return BooleanType.BOOLEAN.create(a.toXacmlString().startsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue endsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().endsWith(b.getValue()));
	}
		
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue uriEndsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue b)
	{
		return BooleanType.BOOLEAN.create(a.toXacmlString().endsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue contains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().contains(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue uriContains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue b)
	{
		return BooleanType.BOOLEAN.create(a.toXacmlString().contains(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromBoolean(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return BooleanType.BOOLEAN.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-integer")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerType.IntegerValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:integer-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerType.IntegerValue integerFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return IntegerType.Factory.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleType.DoubleValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:double-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleType.DoubleValue doubleFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return DoubleType.Factory.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-time")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromTime(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeType.TimeValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeType.TimeValue timeFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return TimeType.Factory.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-date")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromDate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateValue dateFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return DateType.DATE.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dateTime")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromDateTime(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeValue dateTimeFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return DateTimeType.DATETIME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-anyURI")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromAnyURI(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIValue anyURIFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return AnyURIType.ANYURI.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromDayTimeDuration(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationValue dayTimeDurationFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return DayTimeDurationType.DAYTIMEDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromYearMonthDuration(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationValue yearMonthDurationFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return YearMonthDurationType.YEARMONTHDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-x500Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromX500Name(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameType.X500NameValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameType.X500NameValue x500NameFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return X500NameType.Factory.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-rfc822Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromRfc822Name(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameType.RFC822NameValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameType.RFC822NameValue rfc822NameFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return RFC822NameType.Factory.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-ipAddress")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromIpAddress(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressType.IPAddressValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressType.IPAddressValue ipAddressFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return IPAddressType.Factory.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dnsName")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringType.StringValue stringFromDnsName(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameValue v)
	{
		return StringType.Factory.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameValue dnsNameFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringType.StringValue v)
	{
		return DNSNameType.DNSNAME.fromXacmlString(v.getValue());
	}
}
