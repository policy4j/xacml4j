package com.artagon.xacml.v3.policy.impl.function;

import com.artagon.xacml.v3.policy.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;

/**
 * A.3.4 Numeric data-type conversion functions
 * 
 * @author Giedrius Trumpickas
 */
public class NumericConversionFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-to-integer")
	@XacmlFuncReturnType(type=DataTypes.DOUBLE)
	public static IntegerValue doubleToInteger(
			@XacmlParam(type=DataTypes.DOUBLE)DoubleValue v)
	{
		return DataTypes.INTEGER.create(v.getValue().intValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-to-double")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static DoubleValue integerToDouble(
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue v)
	{
		return DataTypes.DOUBLE.create(v.getValue().doubleValue());
	}
}
