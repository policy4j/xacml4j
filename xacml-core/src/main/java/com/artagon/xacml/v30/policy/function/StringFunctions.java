package com.artagon.xacml.v30.policy.function;

import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.types.AnyURIType;
import com.artagon.xacml.v30.types.AnyURIValueExp;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.BooleanValueExp;
import com.artagon.xacml.v30.types.DNSNameType;
import com.artagon.xacml.v30.types.DNSNameValueExp;
import com.artagon.xacml.v30.types.DateTimeType;
import com.artagon.xacml.v30.types.DateTimeValueExp;
import com.artagon.xacml.v30.types.DateType;
import com.artagon.xacml.v30.types.DateValueExp;
import com.artagon.xacml.v30.types.DayTimeDurationType;
import com.artagon.xacml.v30.types.DayTimeDurationValueExp;
import com.artagon.xacml.v30.types.DoubleType;
import com.artagon.xacml.v30.types.DoubleValueExp;
import com.artagon.xacml.v30.types.IPAddressType;
import com.artagon.xacml.v30.types.IPAddressValueExp;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.IntegerValueExp;
import com.artagon.xacml.v30.types.RFC822NameType;
import com.artagon.xacml.v30.types.RFC822NameValueExp;
import com.artagon.xacml.v30.types.StringType;
import com.artagon.xacml.v30.types.StringValueExp;
import com.artagon.xacml.v30.types.TimeType;
import com.artagon.xacml.v30.types.TimeValueExp;
import com.artagon.xacml.v30.types.X500NameType;
import com.artagon.xacml.v30.types.X500NameValueExp;
import com.artagon.xacml.v30.types.YearMonthDurationType;
import com.artagon.xacml.v30.types.YearMonthDurationValueExp;

@XacmlFunctionProvider(description="XACML string functions")
public class StringFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp concatenate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		StringBuilder buf = new StringBuilder();
		return StringType.STRING.create(buf.append(a).append(b).toString());
	}
	
	@Deprecated
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:uri-string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp uriStringconcatenate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValueExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		StringBuilder buf = new StringBuilder();
		return StringType.STRING.create(buf.append(a.toXacmlString()).append(b).toString());
	}
	
	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp startsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().startsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp uriStartsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValueExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.toXacmlString().startsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp endsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().endsWith(b.getValue()));
	}
		
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp uriEndsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValueExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.toXacmlString().endsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp contains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().contains(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp uriContains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValueExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.toXacmlString().contains(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromBoolean(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp booleanFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return BooleanType.BOOLEAN.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-integer")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:integer-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValueExp integerFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return IntegerType.INTEGER.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:double-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValueExp doubleFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return DoubleType.DOUBLE.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-time")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromTime(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeValueExp timeFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return TimeType.TIME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-date")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromDate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateValueExp dateFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return DateType.DATE.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dateTime")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromDateTime(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeValueExp dateTimeFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return DateTimeType.DATETIME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-anyURI")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromAnyURI(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIValueExp anyURIFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return AnyURIType.ANYURI.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromDayTimeDuration(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationValueExp dayTimeDurationFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return DayTimeDurationType.DAYTIMEDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromYearMonthDuration(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationValueExp yearMonthDurationFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return YearMonthDurationType.YEARMONTHDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-x500Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromX500Name(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameValueExp x500NameFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return X500NameType.X500NAME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-rfc822Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromRfc822Name(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameValueExp rfc822NameFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return RFC822NameType.RFC822NAME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-ipAddress")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromIpAddress(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressValueExp ipAddressFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return IPAddressType.IPADDRESS.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dnsName")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValueExp stringFromDnsName(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameValueExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameValueExp dnsNameFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp v)
	{
		return DNSNameType.DNSNAME.fromXacmlString(v.getValue());
	}
}
