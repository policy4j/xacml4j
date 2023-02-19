package org.xacml4j.v30.policy.function.impl;

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

import org.xacml4j.v30.policy.function.XacmlEvaluationContextParam;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.AnyURIValue;
import org.xacml4j.v30.types.BooleanValue;
import org.xacml4j.v30.types.DNSNameValue;
import org.xacml4j.v30.types.DateTimeValue;
import org.xacml4j.v30.types.DateValue;
import org.xacml4j.v30.types.DayTimeDurationValue;
import org.xacml4j.v30.types.DoubleValue;
import org.xacml4j.v30.types.IPAddressValue;
import org.xacml4j.v30.types.IntegerValue;
import org.xacml4j.v30.types.RFC822NameValue;
import org.xacml4j.v30.types.StringValue;
import org.xacml4j.v30.types.TimeValue;
import org.xacml4j.v30.types.X500NameValue;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.types.YearMonthDurationValue;


@XacmlFunctionProvider(description="XACML string functions")
public final class StringFunctions
{
	/** Private constructor for utility class */
	private StringFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue concatenate(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return a.concat(b);
	}

	@Deprecated
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:2.0:function:uri-string-concatenate")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue uriStringConcatenate(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return a.toStringExp().concat(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue startsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.startsWith(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-starts-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue uriStartsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.toStringExp().startsWith(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue endsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.endsWith(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-ends-with")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue uriEndsWith(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.toStringExp().endsWith(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue contains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.contains(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:uri-contains")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue uriContains(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.toStringExp().contains(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromBoolean(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean") BooleanValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.BOOLEAN.of(v.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-from-integer")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromInteger(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue v)
	{
		return v.toStringValue();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:integer-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue integerFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.INTEGER.of(v.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromDouble(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:double-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleValue doubleFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.DOUBLE.of(v.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-time")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromTime(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time")
	public static TimeValue timeFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.TIME.of(v.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-date")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromDate(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateValue dateFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.DATE.of(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dateTime")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromDateTime(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeValue dateTimeFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.DATETIME.of(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-anyURI")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromAnyURI(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI")
	public static AnyURIValue anyURIFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.ANYURI.of(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromDayTimeDuration(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration") DayTimeDurationValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")
	public static DayTimeDurationValue dayTimeDurationFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.DAYTIMEDURATION.of(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromYearMonthDuration(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") YearMonthDurationValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")
	public static YearMonthDurationValue yearMonthDurationFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return YearMonthDurationValue.of(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-x500Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromX500Name(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500NameValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")
	public static X500NameValue x500NameFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.X500NAME.of(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-rfc822Name")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromRfc822Name(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name") RFC822NameValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")
	public static RFC822NameValue rfc822NameFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.RFC822NAME.of(v);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-ipAddress")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromIpAddress(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress") IPAddressValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress")
	public static IPAddressValue ipAddressFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.IPADDRESS.of(v.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:string-from-dnsName")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringValue stringFromDnsName(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName") DNSNameValue v)
	{
		return v.toStringExp();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:dnsName")
	public static DNSNameValue dnsNameFromString(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue v)
	{
		return XacmlTypes.DNSNAME.of(v);
	}
}
