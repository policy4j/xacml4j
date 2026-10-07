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

import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.DoubleVal;
import org.xacml4j.v30.types.IntegerVal;


/**
 * A.3.4 Numeric data-type conversion functions
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML numeric conversion functions")
public final class NumericConversionFunctions
{
	/** Private constructor for utility class */
	private NumericConversionFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-to-integer")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerVal doubleToInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleVal v)
	{
		double value = v.get();
		// C5: NaN and infinity keep the historical intValue saturation.
		if(Double.isNaN(value) || Double.isInfinite(value)){
			return IntegerVal.of((int) value);
		}
		// Signed long is [-2^63, 2^63). 2^63 is not a long.
		if(value >= 9223372036854775808.0d || value < -9223372036854775808.0d){
			throw new EvaluationException(Status.processingError().build(),
					"double-to-integer value is outside the xs:integer range");
		}
		return IntegerVal.of((long) value);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-to-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static DoubleVal integerToDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal v)
	{
		long number = v.get();
		double converted = (double) number;
		// 2^63 is not a long. A double outside [-2^63, 2^63) saturates
		// when cast back (JLS 5.1.3), so Long.MAX_VALUE would look exact.
		// -2^63 is an exact double and stays on the low side of that test.
		if(converted >= 9223372036854775808.0d
				|| converted < -9223372036854775808.0d
				|| (long) converted != number){
			throw new EvaluationException(Status.processingError().build(),
					"integer-to-double value is not exactly representable");
		}
		return v.toDoubleValue();
	}
}
