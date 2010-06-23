package com.artagon.xacml.v3.policy.function;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.spi.function.XacmlFunc;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.spi.function.XacmlParam;
import com.artagon.xacml.v3.spi.function.XacmlParamEvaluationContext;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.DateTimeType.DateTimeValue;
import com.artagon.xacml.v3.types.DateType.DateValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.TimeType.TimeValue;
import com.google.common.base.Preconditions;

@XacmlFunctionProvider
public class NonNumericComparisionFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue a, 
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue().compareTo(b.getValue()) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEqual(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue a, 
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		int r = a.getValue().compareTo(b.getValue());
		return XacmlDataTypes.BOOLEAN.create(r > 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-less-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue a, 
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		int r = a.getValue().compareTo(b.getValue());
		return XacmlDataTypes.BOOLEAN.create(r < 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEqual(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue a, 
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue b)
	{
		int r = a.getValue().compareTo(b.getValue());
		return XacmlDataTypes.BOOLEAN.create(r < 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue a, 
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEquals(
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue a, 
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue b)
	{
		int r = a.compareTo(b);
		return XacmlDataTypes.BOOLEAN.create(r  > 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue a, 
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.compareTo(b) < 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEquals(
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue a, 
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue b)
	{
		int r = a.compareTo(b);
		return XacmlDataTypes.BOOLEAN.create(r  < 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:time-in-range")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue timeInRange(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue a, 
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue b,
			@XacmlParam(type=XacmlDataTypes.TIME)TimeValue c)
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
		return XacmlDataTypes.BOOLEAN.create(ac.compare(bc) >= 0 && ac.compare(cc) <= 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue a, 
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEquals(
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue a, 
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue b)
	{
		int r = a.compareTo(b);
		return XacmlDataTypes.BOOLEAN.create(r  > 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue a, 
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.compareTo(b) < 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEquals(
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue a, 
			@XacmlParam(type=XacmlDataTypes.DATE)DateValue b)
	{
		int r = a.compareTo(b);
		return XacmlDataTypes.BOOLEAN.create(r  < 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEquals(
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue b)
	{
		int r = a.compareTo(b);
		return XacmlDataTypes.BOOLEAN.create(r  > 0 || r == 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.compareTo(b) < 0);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEquals(
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue a, 
			@XacmlParam(type=XacmlDataTypes.DATETIME)DateTimeValue b)
	{
		int r = a.compareTo(b);
		return XacmlDataTypes.BOOLEAN.create(r  < 0 || r == 0);
	}
}
