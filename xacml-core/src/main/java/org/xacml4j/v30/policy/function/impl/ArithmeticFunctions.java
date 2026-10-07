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

import java.math.BigInteger;

import com.google.common.base.Preconditions;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.policy.function.*;
import org.xacml4j.v30.types.DoubleVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.XacmlTypes;


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
public final class ArithmeticFunctions
{
	/** Private constructor for utility class */
	private ArithmeticFunctions(){}

	/**
	 * Adds two or more integer data-type values
	 *
	 * @param values a two or more integer data-type value
	 * @return an integer data-type value representing
	 * arithmetic sum of a given values
	 */
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-add", shortId = "integer-add")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal addInteger(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)
			IntegerVal...values)
	{
		try{
			return XacmlTypes.INTEGER.ofAny(sum(values));
		}catch(ArithmeticException e){
			throw outsideIntegerRange("integer-add", e);
		}
	}

	/**
	 * The sum of the values. A sum of the first values can leave the 64-bit
	 * range while the sum of all values is inside it, so the sum is then
	 * taken without a bound and checked at the end.
	 *
	 * @throws ArithmeticException if the sum is outside the 64-bit range
	 */
	private static long sum(IntegerVal[] values)
	{
		try{
			long sum = 0L;
			for(IntegerVal v : values){
				sum = Math.addExact(sum, v.get());
			}
			return sum;
		}catch(ArithmeticException e){
			BigInteger sum = BigInteger.ZERO;
			for(IntegerVal v : values){
				sum = sum.add(BigInteger.valueOf(v.get()));
			}
			return sum.longValueExact();
		}
	}

	/**
	 * The product of the values, as {@link #sum(IntegerVal[])} for a sum.
	 *
	 * @throws ArithmeticException if the product is outside the 64-bit range
	 */
	private static long product(IntegerVal[] values)
	{
		try{
			long product = 1L;
			for(IntegerVal v : values){
				product = Math.multiplyExact(product, v.get());
			}
			return product;
		}catch(ArithmeticException e){
			BigInteger product = BigInteger.ONE;
			for(IntegerVal v : values){
				product = product.multiply(BigInteger.valueOf(v.get()));
			}
			return product.longValueExact();
		}
	}

	/**
	 * XACML integers are 64-bit signed integers here. A result outside that
	 * range is Indeterminate with status processing-error, never a wrapped
	 * value (maintainer ruling 2026-10-07, run 061 D4). The message names
	 * the function, not the operands.
	 */
	private static EvaluationException outsideIntegerRange(String function, ArithmeticException cause)
	{
		String message = function + " result is outside the xs:integer range";
		return new EvaluationException(Status.processingError().message(message).build(), message, cause);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-multiply", shortId = "integer-multiply")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal multiplyInteger(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)
			IntegerVal...values)
	{
		try{
			return IntegerVal.of(product(values));
		}catch(ArithmeticException e){
			throw outsideIntegerRange("integer-multiply", e);
		}
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-add", shortId = "double-add")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleVal addDouble(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#double", min=2)
			DoubleVal...values)
	{
		java.lang.Double sum = 0.0;
		for(DoubleVal v : values){
			sum += v.get();
		}
		return DoubleVal.of(sum);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-multiply", shortId = "double-multiply")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleVal multiplyDouble(
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#double", min=2)
			DoubleVal...values)
	{
		java.lang.Double value = 1.0;
		for(DoubleVal v : values){
			value *= v.get();
		}
		return DoubleVal.of(value);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs", shortId = "integer-abs")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal abs(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal v)
	{
		try{
			return v.abs();
		}catch(ArithmeticException e){
			throw outsideIntegerRange("integer-abs", e);
		}
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-abs", shortId = "double-abs")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleVal abs(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal v)
	{
		return DoubleVal.of(Math.abs(v.get()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:floor", shortId = "floor")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleVal floor(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal v)
	{
		return DoubleVal.of(Math.floor(v.get()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:round", shortId = "round")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleVal round(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal v)
	{
		// IEEE 754 round-to-nearest: NaN and the infinities stay, a tie goes to
		// the even integer. Math.round would return a long (NaN -> 0).
		return DoubleVal.of(Math.rint(v.get()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-subtract", shortId = "double-subtract")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleVal subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal b)
	{
		return a.subtract(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-divide", shortId = "double-divide")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleVal divideDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal b)
	{
		Preconditions.checkArgument(b.get() != 0);
		return a.divide(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-divide", shortId = "integer-divide")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal divideInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal b)
	{
		Preconditions.checkArgument(b.get() != 0);
		// A.3.2 requires xs:integer; quotient toward zero is the approved project interpretation.
		try{
			// The smallest integer divided by -1 is outside the range.
			return IntegerVal.of(Math.divideExact(a.get(), b.get()));
		}catch(ArithmeticException e){
			throw outsideIntegerRange("integer-divide", e);
		}
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-mod", shortId = "integer-mod")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal modInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal b)
	{
		return a.mod(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-subtract", shortId = "integer-subtract")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal b)
	{
		try{
			return a.subtract(b);
		}catch(ArithmeticException e){
			throw outsideIntegerRange("integer-subtract", e);
		}
	}
}
