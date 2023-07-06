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


import org.xacml4j.v30.policy.function.*;
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
	public static BooleanVal eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:entity-equal")
	@XacmlFuncReturnType(typeId="boolean")
	public static BooleanVal eq(
			@XacmlFuncParam(typeId="entity") Entity a,
			@XacmlFuncParam(typeId="entity") Entity b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURI a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI") AnyURI b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal eq(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean") BooleanVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean") BooleanVal b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal eq(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name") X500Name b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equalsIgnoreCase(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:time-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration") ISO8601DayTimeDuration a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration") ISO8601DayTimeDuration b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-equal")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") ISO8601YearMonthDuration a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") ISO8601YearMonthDuration b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name") RFC822Name a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name") RFC822Name b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary") HexBinary a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary") HexBinary b)
	{
		return _eq(a, b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal equals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary") Base64Binary a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary") Base64Binary b)
	{
		return _eq(a, b);
	}

	private static BooleanVal _eq(Value<?> a, Value<?> b){
		return BooleanVal.of(a.equals(b));
	}
}
