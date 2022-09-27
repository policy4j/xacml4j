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

import com.google.common.base.Preconditions;
import org.xacml4j.v30.spi.function.*;
import org.xacml4j.v30.types.*;

@XacmlFunctionProvider(description="XACML non-numeric comparison functions")
public final class NonNumericComparisonFunctions
{
	/** Private constructor for utility class */
	private NonNumericComparisonFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThan(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.value().compareTo(b.value()) > 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThanOrEqual(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		int r = a.value().compareTo(b.value());
		return XacmlTypes.BOOLEAN.of(r > 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		int r = a.value().compareTo(b.value());
		return XacmlTypes.BOOLEAN.of(r < 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanOrEqual(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringValue b)
	{
		int r = a.value().compareTo(b.value());
		return XacmlTypes.BOOLEAN.of(r < 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.compareTo(b) > 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.of(r  > 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.compareTo(b) < 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.of(r  < 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-in-range")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue timeInRange(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue b,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time") TimeValue c)
	{
		org.xacml4j.v30.Time ac = a.value();
		org.xacml4j.v30.Time bc = b.value();
		org.xacml4j.v30.Time cc = c.value();
		Preconditions.checkArgument(b.compareTo(c) <= 0);
		return XacmlTypes.BOOLEAN.of(ac.compareTo(bc) >= 0 && ac.compareTo(cc) <= 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.compareTo(b) > 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.of(r  > 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.compareTo(b) < 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") DateValue b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.of(r  < 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.compareTo(b) > 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.of(r  > 0 || r == 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.compareTo(b) < 0);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") DateTimeValue b)
	{
		int r = a.compareTo(b);
		return XacmlTypes.BOOLEAN.of(r  < 0 || r == 0);
	}
}
