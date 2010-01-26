package com.artagon.xacml.policy.function.impl;


import com.artagon.xacml.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.policy.function.annotations.XacmlFuncParam;
import com.artagon.xacml.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.policy.function.annotations.XacmlFuncVarArgParam;

import com.artagon.xacml.policy.type.DataTypes;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.policy.type.IntegerType.IntegerValue;
import com.artagon.xacml.util.Preconditions;

public class ArithmeticFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-add")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue add(
			@XacmlFuncVarArgParam(type=DataTypes.INTEGER, min=2)IntegerValue ...values)
	{
		Long sum = 0L;
		for(IntegerValue v : values){
			sum += v.getValue();
		}
		return DataTypes.INTEGER.create(sum);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-multiply")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue multiply(
			@XacmlFuncVarArgParam(type=DataTypes.INTEGER, min=2)IntegerValue ...values)
	{
		Long value = 1L;
		for(IntegerValue v : values){
			value *= v.getValue();
		}
		return DataTypes.INTEGER.create(value);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-add")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue add(
			@XacmlFuncVarArgParam(type=DataTypes.DOUBLE, min=2)DoubleValue ...values)
	{
		Double sum = 0.0;
		for(DoubleValue v : values){
			sum += v.getValue();
		}
		return DataTypes.DOUBLE.create(sum);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-multiply")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static BooleanValue multiply(
			@XacmlFuncVarArgParam(type=DataTypes.DOUBLE, min=2)DoubleValue ...values)
	{
		Double value = 1.0;
		for(DoubleValue v : values){
			value *= v.getValue();
		}
		return DataTypes.DOUBLE.create(value);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue abs(
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerValue v)
	{
		return DataTypes.INTEGER.create(Math.abs(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue integerToDouble(
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerValue v)
	{
		return DataTypes.DOUBLE.create(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-to-integer")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue doubleToInteger(
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(v.getValue().intValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-abs")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue abs(
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(Math.abs(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:floor")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue floor(
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(Math.floor(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:round")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue round(
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(Math.round(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-subtract")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue subtract(
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue a,
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		return DataTypes.DOUBLE.create(a.getValue() - b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-divide")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue divide(
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue a,
			@XacmlFuncParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DataTypes.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-divide")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue divide(
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerValue a,
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerValue b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DataTypes.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-subtract")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue subtract(
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerValue a,
			@XacmlFuncParam(type=DataTypes.INTEGER)IntegerValue b)
	{
		return DataTypes.INTEGER.create(a.getValue() - b.getValue());
	}
}
