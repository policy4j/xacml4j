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


import org.xacml4j.v30.spi.function.*;
import org.xacml4j.v30.types.*;


/**
 * An implementation of XACML equality predicates
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML equality predicates")
public final class EqualityPredicates
{
	/** Private constructor for utility class */
	private EqualityPredicates() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURIValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean") BooleanValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean") BooleanValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue eq(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500NameValue a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500NameValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equalsIgnoreCase(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equalsIgnoreCase(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration") DayTimeDurationValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration") DayTimeDurationValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") YearMonthDurationValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") YearMonthDurationValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name") RFC822NameValue a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name") RFC822NameValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary") HexBinaryValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary") HexBinaryValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary") Base64BinaryValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary") Base64BinaryValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.equals(b));
	}
}
