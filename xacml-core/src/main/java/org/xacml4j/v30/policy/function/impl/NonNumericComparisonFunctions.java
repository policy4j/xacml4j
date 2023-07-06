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

@XacmlFunctionProvider(description="XACML non-numeric comparison functions")
public final class NonNumericComparisonFunctions
{
	/** Private constructor for utility class */
	private NonNumericComparisonFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal greaterThan(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringVal a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringVal b)
	{
		return XacmlTypes.BOOLEAN.ofAny(a.get().compareTo(b.get()) > 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal greaterThanOrEqual(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringVal a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringVal b)
	{
		int r = a.get().compareTo(b.get());
		return XacmlTypes.BOOLEAN.ofAny(r > 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal b)
	{
		int r = a.get().compareTo(b.get());
		return XacmlTypes.BOOLEAN.ofAny(r < 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal lessThanOrEqual(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal b)
	{
		int r = a.get().compareTo(b.get());
		return XacmlTypes.BOOLEAN.ofAny(r < 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal greaterThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time b)
	{
		return BooleanVal.of(a.compareTo(b) > 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal greaterThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time b)
	{
		int r = a.compareTo(b);
		return BooleanVal.of(r  > 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time b)
	{
		return XacmlTypes.BOOLEAN.ofAny(a.compareTo(b) < 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal lessThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.ofAny(r  < 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-in-range")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal timeInRange(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time b,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") ISO8601Time c)
	{
		return BooleanVal.of(a.compareTo(b) >= 0 && a.compareTo(c) <= 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal greaterThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date b)
	{
		return XacmlTypes.BOOLEAN.ofAny(a.compareTo(b) > 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal greaterThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.ofAny(r  > 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date b)
	{
		return XacmlTypes.BOOLEAN.ofAny(a.compareTo(b) < 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal lessThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.ofAny(r  < 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal greaterThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime b)
	{
		return XacmlTypes.BOOLEAN.ofAny(a.compareTo(b) > 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal greaterThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.ofAny(r  > 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime b)
	{
		return XacmlTypes.BOOLEAN.ofAny(a.compareTo(b) < 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal lessThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.ofAny(r  < 0 || r == 0);
	}
}
