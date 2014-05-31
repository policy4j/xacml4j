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

import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamVarArg;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;

import com.google.common.base.Preconditions;


/**
 * All of the following functions SHALL take two arguments of the specified data-type,
 * integer, or double, and SHALL return an element of integer or double data-type,
 * respectively. However, the "add" functions MAY take more than two arguments.
 * Each function evaluation operating on doubles SHALL proceed as specified by their
 * logical counterparts in IEEE 754 [IEEE754]. For all of these functions,
 * if any argument is "Indeterminate", then the function SHALL evaluate to
 * "Indeterminate". In the case of the divide functions, if the divisor is zero,
 *  then the function SHALL evaluate to "Indeterminate".
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML arithmetic functions")
public class ArithmeticFunctions
{
	/** Private constructor for utility class */
	private ArithmeticFunctions() {}

	/**
	 * Adds two or more integer data-type values
	 *
	 * @param values a two or more integer data-type value
	 * @return an integer data-type value representing
	 * arithmetic sum of a given values
	 */
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-add")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp addInteger(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)
			IntegerExp ...values)
	{
		Long sum = 0L;
		for(IntegerExp v : values){
			sum += v.getValue();
		}
		return IntegerExp.valueOf(sum);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-multiply")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp multiplyInteger(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)
			IntegerExp ...values)
	{
		Long value = 1L;
		for(IntegerExp v : values){
			value *= v.getValue();
		}
		return IntegerExp.valueOf(value);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-add")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp addDouble(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#double", min=2)
			DoubleExp ...values)
	{
		Double sum = 0.0;
		for(DoubleExp v : values){
			sum += v.getValue();
		}
		return DoubleExp.valueOf(sum);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-multiply")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp multiplyDouble(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#double", min=2)
			DoubleExp ...values)
	{
		Double value = 1.0;
		for(DoubleExp v : values){
			value *= v.getValue();
		}
		return DoubleExp.valueOf(value);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp abs(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp v)
	{
		return IntegerExp.valueOf(Math.abs(v.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-abs")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp abs(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return DoubleExp.valueOf(Math.abs(v.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:floor")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp floor(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return DoubleExp.valueOf(Math.floor(v.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:round")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp round(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return DoubleExp.valueOf(Math.round(v.getValue()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-subtract")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		return a.subtract(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-divide")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp divideDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp b)
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return a.divide(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-divide")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleExp divideInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return a.divide(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-mod")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp modInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return a.mod(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-subtract")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerExp subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp b)
	{
		return a.subtract(b);
	}
}
