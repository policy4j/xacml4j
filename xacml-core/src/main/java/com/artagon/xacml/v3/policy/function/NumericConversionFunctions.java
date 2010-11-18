package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.sdk.function.XacmlFuncParam;
import com.artagon.xacml.v3.sdk.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.sdk.function.XacmlFuncSpec;
import com.artagon.xacml.v3.sdk.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.DoubleType;
import com.artagon.xacml.v3.types.DoubleValue;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.IntegerValue;

/**
 * A.3.4 Numeric data-type conversion functions
 * 
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML numeric conversion functions")
public class NumericConversionFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:double-to-integer")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#double")
	public static IntegerValue doubleToInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleValue v)
	{
		return IntegerType.INTEGER.create(v.getValue().intValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-to-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static DoubleValue integerToDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue v)
	{
		return DoubleType.DOUBLE.create(v.getValue().doubleValue());
	}
}
