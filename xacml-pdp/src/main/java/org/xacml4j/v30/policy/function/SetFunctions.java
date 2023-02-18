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

import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.types.BooleanValue;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * XACML set functions
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML Set Functions")
public final class SetFunctions
{
	/** Private constructor for utility class */
	private SetFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfValues integerIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfValues integerUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue integerSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue integerAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue integerSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfValues booleanIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfValues booleanUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanSubset(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue booleanSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfValues stringIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfValues stringUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue stringSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue stringAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue stringSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfValues doubleIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfValues doubleUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue doubleSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue doubleAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue doubleSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfValues dateIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfValues dateUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#date", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#date", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfValues timeIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfValues timeUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue timeSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#time", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#time", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue timeAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue timeSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfValues dateTimeIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfValues dateTimeUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateTimeSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#dateTime", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#dateTime", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateTimeAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dateTimeSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfValues anyURIIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfValues anyURITimeUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyURISubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#anyURI", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#anyURI", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyURIAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyURISetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfValues x500NameIntersection(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-union")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfValues x500NameUnion(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue x500NameSubset(
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue x500NameAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue x500NameSetEquals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfValues rfc822NameIntersection(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfValues rfc822NameUnion(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue rfc822NameSubset(
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue rfc822NameAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue rfc822NameSetEquals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfValues hexBinaryIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfValues hexBinaryUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue hexBinarySubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#hexBinary", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#hexBinary", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue hexBinaryAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue hexBinarySetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfValues base64BinaryIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfValues base64BinaryUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue base64BinarySubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#base64Binary", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#base64Binary", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue base64BinaryAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue base64BinarySetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfValues dayTimeDurationIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfValues dayTimeDurationUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dayTimeDurationSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dayTimeDurationAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue dayTimeDurationSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfValues yearMonthDurationIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfValues yearMonthDurationUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue yearMonthDurationSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue yearMonthDurationAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue yearMonthDurationSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-intersection")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfValues ipAddressIntersection(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true) BagOfValues b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-union")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfValues ipAddressDurationUnion(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true) BagOfValues b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue ipAddressDurationSubset(
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag = true) BagOfValues a,
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag = true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue ipAddressAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue ipAddressSetEquals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true) BagOfValues a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true) BagOfValues b)
	{
		return XacmlTypes.BOOLEAN.of(a.containsAll(b) && b.containsAll(a));
	}
}
