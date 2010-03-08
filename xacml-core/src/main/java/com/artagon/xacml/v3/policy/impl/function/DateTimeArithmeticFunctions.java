package com.artagon.xacml.v3.policy.impl.function;

import com.artagon.xacml.v3.policy.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.DateType.DateValue;
import com.artagon.xacml.v3.policy.type.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.policy.type.DayTimeDurationType.DayTimeDurationValue;
import com.artagon.xacml.v3.policy.type.YearMonthDurationType.YearMonthDurationValue;

public class DateTimeArithmeticFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-dayTimeDuration")
	@XacmlFuncReturnType(type=DataTypes.DATETIME)
	public static DateTimeValue add(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=DataTypes.DAYTIMEDURATION)DayTimeDurationValue b)
	{
		return DataTypes.DATETIME.create(a.add(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-dayTimeDuration")
	@XacmlFuncReturnType(type=DataTypes.DATETIME)
	public static DateTimeValue subtract(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=DataTypes.DAYTIMEDURATION)DayTimeDurationValue b)
	{
		return DataTypes.DATETIME.create(a.subtract(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-add-yearMonthDuration")
	@XacmlFuncReturnType(type=DataTypes.DATETIME)
	public static DateTimeValue add(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return DataTypes.DATETIME.create(a.add(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:dateTime-subtract-yearMonthDuration")
	@XacmlFuncReturnType(type=DataTypes.DATETIME)
	public static DateTimeValue subtract(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return DataTypes.DATETIME.create(a.subtract(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:date-add-yearMonthDuration")
	@XacmlFuncReturnType(type=DataTypes.DATETIME)
	public static DateValue add(
			@XacmlParam(type=DataTypes.DATE)DateValue a, 
			@XacmlParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return DataTypes.DATE.create(a.add(b));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:date-subtract-yearMonthDuration")
	@XacmlFuncReturnType(type=DataTypes.DATE)
	public static DateValue subtract(
			@XacmlParam(type=DataTypes.DATE)DateValue a, 
			@XacmlParam(type=DataTypes.YEARMONTHDURATION)YearMonthDurationValue b)
	{
		return DataTypes.DATE.create(a.subtract(b));
	}
}
