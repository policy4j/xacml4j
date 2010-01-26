package com.artagon.xacml.v3.policy.function.impl;


import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamVarArg;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;

public class ArithmeticFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-add")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue add(
			@XacmlParamVarArg(type=DataTypes.INTEGER, min=2)IntegerValue ...values)
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
			@XacmlParamVarArg(type=DataTypes.INTEGER, min=2)IntegerValue ...values)
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
			@XacmlParamVarArg(type=DataTypes.DOUBLE, min=2)DoubleValue ...values)
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
			@XacmlParamVarArg(type=DataTypes.DOUBLE, min=2)DoubleValue ...values)
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
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue v)
	{
		return DataTypes.INTEGER.create(Math.abs(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-abs")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue integerToDouble(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue v)
	{
		return DataTypes.DOUBLE.create(v.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-to-integer")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue doubleToInteger(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(v.getValue().intValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-abs")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue abs(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(Math.abs(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:floor")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue floor(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(Math.floor(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:round")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue round(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.DOUBLE.create(Math.round(v.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-subtract")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue subtract(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue a,
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		return DataTypes.DOUBLE.create(a.getValue() - b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-divide")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue divide(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue a,
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DataTypes.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-divide")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static DoubleValue divide(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a,
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b) 
	{
		Preconditions.checkArgument(b.getValue() != 0);
		return DataTypes.DOUBLE.create(a.getValue()/b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-subtract")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerValue subtract(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a,
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b)
	{
		return DataTypes.INTEGER.create(a.getValue() - b.getValue());
	}
}
