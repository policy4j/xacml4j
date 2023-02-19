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

import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.BooleanValue;
import org.xacml4j.v30.types.DoubleValue;
import org.xacml4j.v30.types.IntegerValue;
import org.xacml4j.v30.types.XacmlTypes;


@XacmlFunctionProvider(description="XACML numeric comparison functions")
public final class NumericComparisonFunctions
{
	/** Private constructor for utility class */
	private NumericComparisonFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThanInteger(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerValue a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.value() > b.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.value() < b.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThanOrEqualInteger(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerValue a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.value().equals(b.value()) || a.value() > b.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanOrEqualBoolean(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.value().equals(b.value()) || a.value() < b.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThanDouble(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double") DoubleValue a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double") DoubleValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.value() > b.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.value() < b.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue greaterThanOrEqualDouble(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double") DoubleValue a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double") DoubleValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.value().equals(b.value()) || a.value() > b.value());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue lessThanOrEqualDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleValue b)
	{
		return XacmlTypes.BOOLEAN.of(a.value().equals(b.value()) || a.value() < b.value());
	}
}
