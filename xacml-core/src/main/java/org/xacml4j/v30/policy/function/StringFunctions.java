package org.xacml4j.v30.policy.function;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamEvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.DNSNameExp;
import org.xacml4j.v30.types.DateExp;
import org.xacml4j.v30.types.DateTimeExp;
import org.xacml4j.v30.types.DayTimeDurationExp;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IPAddressExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.RFC822NameExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.TimeExp;
import org.xacml4j.v30.types.X500NameExp;
import org.xacml4j.v30.types.YearMonthDurationExp;


@XacmlFunctionProvider(description="XACML string functions")
public class StringFunctions
{
	/** Private constructor for utility class */
	private StringFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp concatenate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return a.concat(b);
	}

	@Deprecated
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:uri-string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp uriStringConcatenate(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#anyURI") AnyURIExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringExp b)
	{
		return a.toStringExp().concat(b);
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
		return BooleanExp.valueOf(a.toStringExp().startsWith(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp endsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.valueOf(a.endsWith(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp uriEndsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.valueOf(a.toStringExp().endsWith(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp contains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.valueOf(a.contains(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp uriContains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp b)
	{
		return BooleanExp.create(a.toStringExp().contains(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromBoolean(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp booleanFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return BooleanExp.valueOf(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-integer")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromInteger(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:integer-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp integerFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return IntegerExp.valueOf(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDouble(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:double-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp doubleFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DoubleExp.valueOf(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-time")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromTime(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeExp timeFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return TimeExp.valueOf(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-date")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDate(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateExp dateFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DateExp.valueOf(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dateTime")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDateTime(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeExp dateTimeFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DateTimeExp.valueOf(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-anyURI")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromAnyURI(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI")AnyURIExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIExp anyURIFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return AnyURIExp.valueOf(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDayTimeDuration(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationExp dayTimeDurationFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DayTimeDurationExp.valueOf(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromYearMonthDuration(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationExp yearMonthDurationFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return YearMonthDurationExp.valueOf(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-x500Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromX500Name(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameExp x500NameFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return X500NameExp.valueOf(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-rfc822Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromRfc822Name(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameExp rfc822NameFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return RFC822NameExp.valueOf(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-ipAddress")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromIpAddress(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")IPAddressExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressExp ipAddressFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return IPAddressExp.valueOf(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dnsName")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp stringFromDnsName(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")DNSNameExp v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameExp dnsNameFromString(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return DNSNameExp.valueOf(v);
	}
}
