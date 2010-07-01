package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;

@XacmlFunctionProvider(description="XACML numeric comparision functions")
public class NumericComparisionFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanInteger(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() > b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanInteger(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() < b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEqualInteger(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue().equals(b.getValue()) || a.getValue() > b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEqualBoolean(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue().equals(b.getValue()) || a.getValue() < b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanDouble(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() > b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanDouble(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue() < b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue greatherThanOrEqualDouble(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue().equals(b.getValue()) || a.getValue() > b.getValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue lessThanOrEqualDouble(
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.DOUBLE)DoubleValue b)
	{
		return XacmlDataTypes.BOOLEAN.create(a.getValue().equals(b.getValue()) || a.getValue() < b.getValue());
	}
}
