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
import org.xacml4j.v30.types.ISO8601Date;
import org.xacml4j.v30.types.ISO8601DateTime;
import org.xacml4j.v30.types.ISO8601DayTimeDuration;
import org.xacml4j.v30.types.ISO8601YearMonthDuration;


/**
 * XACML date/time arithmetic functions
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML date time arithmetic functions")
public final class DateTimeArithmeticFunctions
{
	/** Private constructor for utility class */
	private DateTimeArithmeticFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static ISO8601DateTime add(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration") ISO8601DayTimeDuration b)
	{
		return a.plus(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static ISO8601DateTime subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration") ISO8601DayTimeDuration b)
	{
		return a.minus(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static ISO8601DateTime add(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") ISO8601YearMonthDuration b)
	{
		return a.plus(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static ISO8601DateTime subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime") ISO8601DateTime a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") ISO8601YearMonthDuration b)
	{
		return a.minus(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static ISO8601Date add(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") ISO8601YearMonthDuration b)
	{
		return a.plus(b);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-subtract-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static ISO8601Date subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date") ISO8601Date a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration") ISO8601YearMonthDuration b)
	{
		return a.minus(b);
	}
}
