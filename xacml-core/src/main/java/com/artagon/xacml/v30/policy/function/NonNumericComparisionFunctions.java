package com.artagon.xacml.v30.policy.function;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.core.Time;
import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncParamEvaluationContext;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.BooleanValueExp;
import com.artagon.xacml.v30.types.DateTimeValueExp;
import com.artagon.xacml.v30.types.DateValueExp;
import com.artagon.xacml.v30.types.StringValueExp;
import com.artagon.xacml.v30.types.TimeValueExp;
import com.google.common.base.Preconditions;

@XacmlFunctionProvider(description="XACML non-numeric comparision functions")
public class NonNumericComparisionFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp greatherThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.getValue().compareTo(b.getValue()) > 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp greatherThanOrEqual(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		int r = a.getValue().compareTo(b.getValue());
		return BooleanType.BOOLEAN.create(r > 0 || r == 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		int r = a.getValue().compareTo(b.getValue());
		return BooleanType.BOOLEAN.create(r < 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp lessThanOrEqual(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValueExp b)
	{
		int r = a.getValue().compareTo(b.getValue());
		return BooleanType.BOOLEAN.create(r < 0 || r == 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp greatherThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp greatherThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp b)
	{
		int r = a.compareTo(b);
		return BooleanType.BOOLEAN.create(r  > 0 || r == 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.compareTo(b) < 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp lessThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp b)
	{
		int r = a.compareTo(b);
		return BooleanType.BOOLEAN.create(r  < 0 || r == 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:time-in-range")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp timeInRange(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp b,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#time")TimeValueExp c)
	{
		Time ac = a.getValue();
		Time bc = b.getValue();
		Time cc = c.getValue();
		Preconditions.checkArgument(b.compareTo(c) <= 0);
		return BooleanType.BOOLEAN.create(ac.compareTo(bc) >= 0 && ac.compareTo(cc) <= 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp greatherThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp greatherThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp b)
	{
		int r = a.compareTo(b);
		return BooleanType.BOOLEAN.create(r  > 0 || r == 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.compareTo(b) < 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp lessThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#date")DateValueExp b)
	{
		int r = a.compareTo(b);
		return BooleanType.BOOLEAN.create(r  < 0 || r == 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp greatherThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.compareTo(b) > 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp greatherThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp b)
	{
		int r = a.compareTo(b);
		return BooleanType.BOOLEAN.create(r  > 0 || r == 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp lessThan(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp b)
	{
		return BooleanType.BOOLEAN.create(a.compareTo(b) < 0);
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValueExp lessThanOrEquals(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#dateTime")DateTimeValueExp b)
	{
		int r = a.compareTo(b);
		return BooleanType.BOOLEAN.create(r  < 0 || r == 0);
	}
}
