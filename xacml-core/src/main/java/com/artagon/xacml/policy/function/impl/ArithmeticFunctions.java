package com.artagon.xacml.policy.function.impl;


import com.artagon.xacml.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.policy.function.annotations.XacmlFuncParam;
import com.artagon.xacml.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.policy.function.annotations.XacmlFuncVarArgParam;

import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.policy.type.IntegerType.IntegerValue;
import com.artagon.xacml.util.Preconditions;

public class ArithmeticFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-add")
	@XacmlFuncReturnType(type=XacmlDataType.INTEGER)
	public static IntegerValue add(
			@XacmlFuncVarArgParam(type=XacmlDataType.INTEGER, min=2)IntegerValue ...values)
	{
		Long sum = 0L;
		for(IntegerValue v : values){
			sum += v.getValue();
		}
		return XacmlDataType.INTEGER.create(sum);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-multiply")
	@XacmlFuncReturnType(type=XacmlDataType.INTEGER)
	public static IntegerValue multiply(
			@XacmlFuncVarArgParam(type=XacmlDataType.INTEGER, min=2)IntegerValue ...values)
	{
		Long value = 1L;
		for(IntegerValue v : values){
			value *= v.getValue();
		}
		return XacmlDataType.INTEGER.create(value);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-add")
	@XacmlFuncReturnType(type=XacmlDataType.DOUBLE)
	public static DoubleValue add(
			@XacmlFuncVarArgParam(type=XacmlDataType.DOUBLE, min=2)DoubleValue ...values)
	{
		Double sum = 0.0;
		for(DoubleValue v : values){
			sum += v.getValue();
		}
		return XacmlDataType.DOUBLE.create(sum);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-multiply")
	@XacmlFuncReturnType(type=XacmlDataType.DOUBLE)
	public static BooleanValue multiply(
			@XacmlFuncVarArgParam(type=XacmlDataType.DOUBLE, min=2)DoubleValue ...values)
	{
		Double value = 1.0;
		for(DoubleValue v : values){
			value *= v.getValue();
		}
		return XacmlDataType.DOUBLE.create(value);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(type=XacmlDataType.INTEGER)
	public static IntegerValue abs(
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerValue v)
	{
		return XacmlDataType.INTEGER.create(Math.abs(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(type=XacmlDataType.INTEGER)
	public static IntegerValue integerToDouble(
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerValue v)
	{
		return XacmlDataType.DOUBLE.create(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-to-integer")
	@XacmlFuncReturnType(type=XacmlDataType.DOUBLE)
	public static DoubleValue doubleToInteger(
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue v)
	{
		return XacmlDataType.DOUBLE.create(v.getValue().intValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-abs")
	@XacmlFuncReturnType(type=XacmlDataType.DOUBLE)
	public static DoubleValue abs(
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue v)
	{
		return XacmlDataType.DOUBLE.create(Math.abs(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:floor")
	@XacmlFuncReturnType(type=XacmlDataType.DOUBLE)
	public static DoubleValue floor(
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue v)
	{
		return XacmlDataType.DOUBLE.create(Math.floor(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:round")
	@XacmlFuncReturnType(type=XacmlDataType.DOUBLE)
	public static DoubleValue round(
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue v)
	{
		return XacmlDataType.DOUBLE.create(Math.round(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-subtract")
	@XacmlFuncReturnType(type=XacmlDataType.DOUBLE)
	public static DoubleValue subtract(
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue a,
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue b)
	{
		return XacmlDataType.DOUBLE.create(a.getValue() - b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-divide")
	@XacmlFuncReturnType(type=XacmlDataType.DOUBLE)
	public static DoubleValue divide(
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue a,
			@XacmlFuncParam(type=XacmlDataType.DOUBLE)DoubleValue b)
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return XacmlDataType.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-divide")
	@XacmlFuncReturnType(type=XacmlDataType.DOUBLE)
	public static DoubleValue divide(
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerValue a,
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerValue b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return XacmlDataType.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-subtract")
	@XacmlFuncReturnType(type=XacmlDataType.INTEGER)
	public static IntegerValue subtract(
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerValue a,
			@XacmlFuncParam(type=XacmlDataType.INTEGER)IntegerValue b)
	{
		return XacmlDataType.INTEGER.create(a.getValue() - b.getValue());
	}
}
