package com.artagon.xacml.v3.policy.impl.function;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.policy.spi.function.XacmlParamEvaluationContext;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.policy.type.DateType.DateValue;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;
import com.artagon.xacml.v3.policy.type.TimeType.TimeValue;

public class NonNumericComparisionFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=DataTypes.STRING)StringValue a, 
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue().compareTo(b.getValue()) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEqual(
			@XacmlParam(type=DataTypes.STRING)StringValue a, 
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		int r = a.getValue().compareTo(b.getValue());
		return DataTypes.BOOLEAN.create(r > 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-less-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=DataTypes.STRING)StringValue a, 
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		int r = a.getValue().compareTo(b.getValue());
		return DataTypes.BOOLEAN.create(r < 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEqual(
			@XacmlParam(type=DataTypes.STRING)StringValue a, 
			@XacmlParam(type=DataTypes.STRING)StringValue b)
	{
		int r = a.getValue().compareTo(b.getValue());
		return DataTypes.BOOLEAN.create(r < 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=DataTypes.TIME)TimeValue a, 
			@XacmlParam(type=DataTypes.TIME)TimeValue b)
	{
		return DataTypes.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equals")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEquals(
			@XacmlParam(type=DataTypes.TIME)TimeValue a, 
			@XacmlParam(type=DataTypes.TIME)TimeValue b)
	{
		int r = a.compareTo(b);
		return DataTypes.BOOLEAN.create(r  > 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=DataTypes.TIME)TimeValue a, 
			@XacmlParam(type=DataTypes.TIME)TimeValue b)
	{
		return DataTypes.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equals")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEquals(
			@XacmlParam(type=DataTypes.TIME)TimeValue a, 
			@XacmlParam(type=DataTypes.TIME)TimeValue b)
	{
		int r = a.compareTo(b);
		return DataTypes.BOOLEAN.create(r  < 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-in-range")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue timeInRange(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParam(type=DataTypes.TIME)TimeValue a, 
			@XacmlParam(type=DataTypes.TIME)TimeValue b,
			@XacmlParam(type=DataTypes.TIME)TimeValue c)
	{
		XMLGregorianCalendar ac = (XMLGregorianCalendar)a.getValue().clone();
		XMLGregorianCalendar bc = (XMLGregorianCalendar)b.getValue().clone();
		XMLGregorianCalendar cc = (XMLGregorianCalendar)c.getValue().clone();
		if(ac.getTimezone() == DatatypeConstants.FIELD_UNDEFINED){
			ac.setTimezone(context.getTimeZone().getRawOffset() / (1000 * 60));
		}
		if(bc.getTimezone() == DatatypeConstants.FIELD_UNDEFINED){
			bc.setTimezone(ac.getTimezone());
		}
		if(cc.getTimezone() == DatatypeConstants.FIELD_UNDEFINED){
			cc.setTimezone(ac.getTimezone());
		}
		Preconditions.checkArgument(b.compareTo(c) <= 0);
		return DataTypes.BOOLEAN.create(ac.compare(bc) >= 0 && ac.compare(cc) <= 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=DataTypes.DATE)DateValue a, 
			@XacmlParam(type=DataTypes.DATE)DateValue b)
	{
		return DataTypes.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equals")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEquals(
			@XacmlParam(type=DataTypes.DATE)DateValue a, 
			@XacmlParam(type=DataTypes.DATE)DateValue b)
	{
		int r = a.compareTo(b);
		return DataTypes.BOOLEAN.create(r  > 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=DataTypes.DATE)DateValue a, 
			@XacmlParam(type=DataTypes.DATE)DateValue b)
	{
		return DataTypes.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equals")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEquals(
			@XacmlParam(type=DataTypes.DATE)DateValue a, 
			@XacmlParam(type=DataTypes.DATE)DateValue b)
	{
		int r = a.compareTo(b);
		return DataTypes.BOOLEAN.create(r  < 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue b)
	{
		return DataTypes.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equals")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEquals(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue b)
	{
		int r = a.compareTo(b);
		return DataTypes.BOOLEAN.create(r  > 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue b)
	{
		return DataTypes.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equals")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEquals(
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=DataTypes.DATETIME)DateTimeValue b)
	{
		int r = a.compareTo(b);
		return DataTypes.BOOLEAN.create(r  < 0 || r == 0);
	}
}
