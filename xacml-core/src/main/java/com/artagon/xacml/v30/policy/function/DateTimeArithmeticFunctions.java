package com.artagon.xacml.v30.policy.function;

import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.spi.function.XacmlLegacyFunc;
import com.artagon.xacml.v30.types.DateExp;
import com.artagon.xacml.v30.types.DateTimeExp;
import com.artagon.xacml.v30.types.DayTimeDurationExp;
import com.artagon.xacml.v30.types.YearMonthDurationExp;

@XacmlFunctionProvider(description="XACML date time arithmetic functions")
public class DateTimeArithmeticFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeExp add(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationExp b)
	{
		return a.add(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-dayTimeDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeExp subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dayTimeDuration")DayTimeDurationExp b)
	{
		return a.subtract(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeExp add(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp b)
	{
		return a.add(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#dateTime")
	public static DateTimeExp subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp b)
	{
		return a.subtract(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateExp add(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp b)
	{
		return a.add(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-subtract-yearMonthDuration")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#date")
	public static DateExp subtract(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#yearMonthDuration")YearMonthDurationExp b)
	{
		return a.subtract(b);
	}
}
