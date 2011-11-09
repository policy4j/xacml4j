package com.artagon.xacml.v30.policy.function;

import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.types.DoubleType;
import com.artagon.xacml.v30.types.DoubleExp;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.IntegerExp;

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
	public static IntegerExp doubleToInteger(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#double")DoubleExp v)
	{
		return IntegerType.INTEGER.create(v.getValue().intValue());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-to-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static DoubleExp integerToDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp v)
	{
		return DoubleType.DOUBLE.create(v.getValue().doubleValue());
	}
}
