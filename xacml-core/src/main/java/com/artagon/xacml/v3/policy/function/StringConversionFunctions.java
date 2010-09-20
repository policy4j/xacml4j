package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.XacmlDataTypes;

@XacmlFunctionProvider(description="XACML string conversion functions")
public class StringConversionFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-normalize-space")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringValue normalizeSpace(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue v)
	{
		return StringType.Factory.create(v.getValue().trim());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringValue normalizeToLowerCase(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue v)
	{
		return StringType.Factory.create(v.getValue().toLowerCase());
	}
	
	@XacmlFuncSpec(id="urn:artagon:names:tc:xacml:1.0:function:string-normalize-to-upper-case")
	@XacmlFuncReturnType(type=XacmlDataTypes.STRING)
	public static StringValue normalizeToUpperCase(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue v)
	{
		return StringType.Factory.create(v.getValue().toUpperCase());
	}
}
