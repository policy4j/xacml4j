package org.xacml4j.v30.policy.function;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamEvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.AnyURIType;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.BooleanType;
import org.xacml4j.v30.types.DNSNameExp;
import org.xacml4j.v30.types.DNSNameType;
import org.xacml4j.v30.types.DateExp;
import org.xacml4j.v30.types.DateTimeExp;
import org.xacml4j.v30.types.DateTimeType;
import org.xacml4j.v30.types.DateType;
import org.xacml4j.v30.types.DayTimeDurationExp;
import org.xacml4j.v30.types.DayTimeDurationType;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.DoubleType;
import org.xacml4j.v30.types.IPAddressExp;
import org.xacml4j.v30.types.IPAddressType;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.IntegerType;
import org.xacml4j.v30.types.RFC822NameExp;
import org.xacml4j.v30.types.RFC822NameType;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.TimeExp;
import org.xacml4j.v30.types.TimeType;
import org.xacml4j.v30.types.X500NameExp;
import org.xacml4j.v30.types.X500NameType;
import org.xacml4j.v30.types.YearMonthDurationExp;
import org.xacml4j.v30.types.YearMonthDurationType;


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
		return new StringExp(buf.append(a).append(b).toString());
	}

	@Deprecated
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:uri-string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp uriStringconcatenate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		StringBuilder buf = new StringBuilder();
		return new StringExp(buf.append(AnyURIType.ANYURI.toString(a)).append(b).toString());
	}



	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp startsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.create(a.getValue().startsWith(b.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp uriStartsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.create(AnyURIType.ANYURI.toString(a).startsWith(b.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp endsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.create(a.getValue().endsWith(b.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp uriEndsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.create(AnyURIType.ANYURI.toString(a).endsWith(b.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp contains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.create(a.getValue().contains(b.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp uriContains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.create(AnyURIType.ANYURI.toString(a).contains(b.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromBoolean(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanExp v)
	{
		return new StringExp(BooleanType.BOOLEAN.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp booleanFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return BooleanType.BOOLEAN.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-integer")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromInteger(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp v)
	{
		return new StringExp(IntegerType.INTEGER.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:integer-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp integerFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return IntegerType.INTEGER.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDouble(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return new StringExp(DoubleType.DOUBLE.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:double-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp doubleFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DoubleType.DOUBLE.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-time")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromTime(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeExp v)
	{
		return new StringExp(TimeType.TIME.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeExp timeFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return TimeType.TIME.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-date")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDate(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateExp v)
	{
		return new StringExp(DateType.DATE.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateExp dateFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DateType.DATE.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dateTime")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDateTime(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp v)
	{
		return new StringExp(DateTimeType.DATETIME.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeExp dateTimeFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DateTimeType.DATETIME.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-anyURI")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromAnyURI(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp v)
	{
		return new StringExp(AnyURIType.ANYURI.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIExp anyURIFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return AnyURIType.ANYURI.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDayTimeDuration(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationExp v)
	{
		return new StringExp(DayTimeDurationType.DAYTIMEDURATION.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationExp dayTimeDurationFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DayTimeDurationType.DAYTIMEDURATION.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromYearMonthDuration(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp v)
	{
		return new StringExp(YearMonthDurationType.YEARMONTHDURATION.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationExp yearMonthDurationFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return YearMonthDurationType.YEARMONTHDURATION.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-x500Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromX500Name(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp v)
	{
		return new StringExp(X500NameType.X500NAME.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameExp x500NameFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return X500NameType.X500NAME.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-rfc822Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromRfc822Name(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameExp v)
	{
		return new StringExp(RFC822NameType.RFC822NAME.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameExp rfc822NameFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return RFC822NameType.RFC822NAME.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-ipAddress")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromIpAddress(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressExp v)
	{
		return new StringExp(IPAddressType.IPADDRESS.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressExp ipAddressFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return IPAddressType.IPADDRESS.fromString(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dnsName")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDnsName(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameExp v)
	{
		return new StringExp(DNSNameType.DNSNAME.toString(v));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameExp dnsNameFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DNSNameType.DNSNAME.create(v.getValue());
	}
}
