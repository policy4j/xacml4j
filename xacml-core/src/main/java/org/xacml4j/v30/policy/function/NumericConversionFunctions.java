package org.xacml4j.v30.policy.function;

import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;


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
		return  IntegerExp.valueOf(v.getValue());
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:integer-to-double")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static DoubleExp integerToDouble(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp v)
	{
		return DoubleExp.valueOf(v.getValue());
	}
}
