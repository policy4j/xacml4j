package com.artagon.xacml.v30.policy.function;

import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.types.AnyURIType;
import com.artagon.xacml.v30.types.AnyURIExp;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.BooleanExp;
import com.artagon.xacml.v30.types.DNSNameType;
import com.artagon.xacml.v30.types.DNSNameExp;
import com.artagon.xacml.v30.types.DateTimeType;
import com.artagon.xacml.v30.types.DateTimeExp;
import com.artagon.xacml.v30.types.DateType;
import com.artagon.xacml.v30.types.DateExp;
import com.artagon.xacml.v30.types.DayTimeDurationType;
import com.artagon.xacml.v30.types.DayTimeDurationExp;
import com.artagon.xacml.v30.types.DoubleType;
import com.artagon.xacml.v30.types.DoubleExp;
import com.artagon.xacml.v30.types.IPAddressType;
import com.artagon.xacml.v30.types.IPAddressExp;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.IntegerExp;
import com.artagon.xacml.v30.types.RFC822NameType;
import com.artagon.xacml.v30.types.RFC822NameExp;
import com.artagon.xacml.v30.types.StringType;
import com.artagon.xacml.v30.types.StringExp;
import com.artagon.xacml.v30.types.TimeType;
import com.artagon.xacml.v30.types.TimeExp;
import com.artagon.xacml.v30.types.X500NameType;
import com.artagon.xacml.v30.types.X500NameExp;
import com.artagon.xacml.v30.types.YearMonthDurationType;
import com.artagon.xacml.v30.types.YearMonthDurationExp;

@XacmlFunctionProvider(description="XACML string functions")
public class StringFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp concatenate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		StringBuilder buf = new StringBuilder();
		return StringType.STRING.create(buf.append(a).append(b).toString());
	}
	
	@Deprecated
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:uri-string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp uriStringconcatenate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		StringBuilder buf = new StringBuilder();
		return StringType.STRING.create(buf.append(a.toXacmlString()).append(b).toString());
	}
	
	
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp startsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().startsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp uriStartsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanType.BOOLEAN.create(a.toXacmlString().startsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp endsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().endsWith(b.getValue()));
	}
		
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp uriEndsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanType.BOOLEAN.create(a.toXacmlString().endsWith(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp contains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().contains(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp uriContains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanType.BOOLEAN.create(a.toXacmlString().contains(b.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromBoolean(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp booleanFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return BooleanType.BOOLEAN.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-integer")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:integer-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp integerFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return IntegerType.INTEGER.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:double-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp doubleFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DoubleType.DOUBLE.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-time")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromTime(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeExp timeFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return TimeType.TIME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-date")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateExp dateFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DateType.DATE.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dateTime")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDateTime(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeExp dateTimeFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DateTimeType.DATETIME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-anyURI")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromAnyURI(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIExp anyURIFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return AnyURIType.ANYURI.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDayTimeDuration(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationExp dayTimeDurationFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DayTimeDurationType.DAYTIMEDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromYearMonthDuration(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationExp yearMonthDurationFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return YearMonthDurationType.YEARMONTHDURATION.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-x500Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromX500Name(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameExp x500NameFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return X500NameType.X500NAME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-rfc822Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromRfc822Name(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameExp rfc822NameFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return RFC822NameType.RFC822NAME.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-ipAddress")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromIpAddress(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressExp ipAddressFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return IPAddressType.IPADDRESS.fromXacmlString(v.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dnsName")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDnsName(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameExp v)
	{
		return StringType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameExp dnsNameFromString(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DNSNameType.DNSNAME.fromXacmlString(v.getValue());
	}
}
