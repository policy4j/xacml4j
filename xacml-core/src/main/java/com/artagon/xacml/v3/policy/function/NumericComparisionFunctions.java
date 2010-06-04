package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;

@XacmlFunctionProvider
public class NumericComparisionFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() > b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() < b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEqual(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() == b.getValue() || a.getValue() > b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEqual(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue a, 
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() == b.getValue() || a.getValue() < b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThan(
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() > b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThan(
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() < b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEqual(
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() == b.getValue() || a.getValue() > b.getValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEqual(
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue a, 
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() == b.getValue() || a.getValue() < b.getValue());
	}
}
