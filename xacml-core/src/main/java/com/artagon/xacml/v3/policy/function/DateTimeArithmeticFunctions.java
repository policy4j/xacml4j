package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.spi.function.XacmlLegacyFunc;
import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.types.DateType.DateValue;
import com.artagon.xacml.v3.types.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.types.YearMonthDurationType.YearMonthDurationValue;

@XacmlFunctionProvider(description="XACML date time arithmetic functions")
public class DateTimeArithmeticFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-add-dayTimeDuration")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME)
	public static DateTimeValue add(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME)DateTimeValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION)DayTimeDurationValue b)
	{
		return a.add(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-dayTimeDuration")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME)
	public static DateTimeValue subtract(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME)DateTimeValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.DAYTIMEDURATION)DayTimeDurationValue b)
	{
		return a.subtract(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-add-yearMonthDuration")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME)
	public static DateTimeValue add(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME)DateTimeValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return a.add(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-subtract-yearMonthDuration")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATETIME)
	public static DateTimeValue subtract(
			@XacmlFuncParam(type=XacmlDataTypes.DATETIME)DateTimeValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return a.subtract(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-add-yearMonthDuration")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATE)
	public static DateValue add(
			@XacmlFuncParam(type=XacmlDataTypes.DATE)DateValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return a.add(b);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-subtract-yearMonthDuration")
	@XacmlFuncReturnType(type=XacmlDataTypes.DATE)
	public static DateValue subtract(
			@XacmlFuncParam(type=XacmlDataTypes.DATE)DateValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return a.subtract(b);
	}
}
