package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.spi.function.XacmlFunc;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.spi.function.XacmlParam;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.DoubleType.DoubleValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;

/**
 * A.3.4 Numeric data-type conversion functions
 * 
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider
public class NumericConversionFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:double-to-integer")
	@XacmlFuncReturnType(type=XacmlDataTypes.DOUBLE)
	public static IntegerValue doubleToInteger(
			@XacmlParam(type=XacmlDataTypes.DOUBLE)DoubleValue v)
	{
		return XacmlDataTypes.INTEGER.create(v.getValue().intValue());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-to-double")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static DoubleValue integerToDouble(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerValue v)
	{
		return XacmlDataTypes.DOUBLE.create(v.getValue().doubleValue());
	}
}
