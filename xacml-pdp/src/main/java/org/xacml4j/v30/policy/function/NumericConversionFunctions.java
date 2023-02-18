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

import org.xacml4j.v30.types.DoubleValue;
import org.xacml4j.v30.types.IntegerValue;


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
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static IntegerValue doubleToInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double") DoubleValue v)
	{
		return v.toIntegerValue();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-to-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static DoubleValue integerToDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerValue v)
	{
		return v.toDoubleValue();
	}
}
