package org.xacml4j.v30.policy.function;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.BooleanExp;


@XacmlFunctionProvider(description="XACML Set Functions")
public class SetFunctions
{
	/** Private constructor for utility class */
	private SetFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfAttributeExp integerIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfAttributeExp integerUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp integerSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp integerAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp integerSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfAttributeExp booleanIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)
	public static BagOfAttributeExp booleanUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp booleanSubset(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp booleanAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp booleanSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfAttributeExp stringIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)
	public static BagOfAttributeExp stringUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp stringSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp stringAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp stringSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfAttributeExp doubleIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)
	public static BagOfAttributeExp doubleUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp doubleSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp doubleAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp doubleSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfAttributeExp dateIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)
	public static BagOfAttributeExp dateUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dateSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#date", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#date", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dateAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dateSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfAttributeExp timeIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)
	public static BagOfAttributeExp timeUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp timeSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#time", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#time", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp timeAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp timeSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfAttributeExp dateTimeIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)
	public static BagOfAttributeExp dateTimeUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dateTimeSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#dateTime", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#dateTime", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dateTimeAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dateTimeSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfAttributeExp anyURIIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)
	public static BagOfAttributeExp anyURITimeUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp anyURISubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#anyURI", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#anyURI", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp anyURIAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp anyURISetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#anyURI", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfAttributeExp x500NameIntersection(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-union")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)
	public static BagOfAttributeExp x500NameUnion(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp x500NameSubset(
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp x500NameAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp x500NameSetEquals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfAttributeExp rfc822NameIntersection(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)
	public static BagOfAttributeExp rfc822NameUnion(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp rfc822NameSubset(
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp rfc822NameAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp rfc822NameSetEquals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfAttributeExp hexBinaryIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)
	public static BagOfAttributeExp hexBinaryUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp hexBinarySubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#hexBinary", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#hexBinary", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp hexBinaryAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp hexBinarySetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#hexBinary", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfAttributeExp base64BinaryIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)
	public static BagOfAttributeExp base64BinaryUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp base64BinarySubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#base64Binary", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#base64Binary", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp base64BinaryAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp base64BinarySetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#base64Binary", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfAttributeExp dayTimeDurationIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)
	public static BagOfAttributeExp dayTimeDurationUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dayTimeDurationSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dayTimeDurationAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp dayTimeDurationSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfAttributeExp yearMonthDurationIntersection(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)
	public static BagOfAttributeExp yearMonthDurationUnion(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp yearMonthDurationSubset(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp yearMonthDurationAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp yearMonthDurationSetEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-intersection")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfAttributeExp ipAddressIntersection(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeExp b)
	{
		return a.intersection(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-union")
	@XacmlFuncReturnType(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)
	public static BagOfAttributeExp ipAddressDurationUnion(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeExp b)
	{
		return a.union(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-subset")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp ipAddressDurationSubset(
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag = true) BagOfAttributeExp a,
			@XacmlFuncParam(typeId = "urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag = true) BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAll(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-at-least-one-member-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp ipAddressAtLeastOneMemberOf(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(b.containsAtLeastOneOf(a));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:ipAddress-set-equals")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp ipAddressSetEquals(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeExp a,
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:2.0:data-type:ipAddress", isBag=true)BagOfAttributeExp b)
	{
		return BooleanExp.valueOf(a.containsAll(b) && b.containsAll(a));
	}
}
