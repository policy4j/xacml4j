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

import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;


@XacmlFunctionProvider(description="XACML numeric comparison functions")
public class NumericComparisonFunctions
{
	/** Private constructor for utility class */
	private NumericComparisonFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp greaterThanInteger(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerExp b)
	{
		return BooleanExp.valueOf(a.getValue() > b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp lessThanInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return BooleanExp.valueOf(a.getValue() < b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp greaterThanOrEqualInteger(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerExp b)
	{
		return BooleanExp.valueOf(a.getValue().equals(b.getValue()) || a.getValue() > b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp lessThanOrEqualBoolean(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return BooleanExp.valueOf(a.getValue().equals(b.getValue()) || a.getValue() < b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp greaterThanDouble(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double") DoubleExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double") DoubleExp b)
	{
		return BooleanExp.valueOf(a.getValue() > b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp lessThanDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		return BooleanExp.valueOf(a.getValue() < b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp greaterThanOrEqualDouble(
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double") DoubleExp a,
			@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#double") DoubleExp b)
	{
		return BooleanExp.valueOf(a.getValue().equals(b.getValue()) || a.getValue() > b.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp lessThanOrEqualDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		return BooleanExp.valueOf(a.getValue().equals(b.getValue()) || a.getValue() < b.getValue());
	}
}
