package com.artagon.xacml.v3.policy.impl.function;

import com.artagon.xacml.v3.policy.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;
import com.artagon.xacml.v3.policy.type.DoubleType.DoubleValue;

public class NumericComparisionFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue() > b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue() < b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEqual(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue() == b.getValue() || a.getValue() > b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEqual(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue() == b.getValue() || a.getValue() < b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue() > b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue() < b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEqual(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue() == b.getValue() || a.getValue() > b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEqual(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue b)
	{
		return DataTypes.BOOLEAN.create(a.getValue() == b.getValue() || a.getValue() < b.getValue());
	}
}
