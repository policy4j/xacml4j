package com.artagon.xacml.v30.policy.function;

import com.artagon.xacml.v30.spi.function.XacmlFuncParam;
import com.artagon.xacml.v30.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v30.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v30.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v30.types.StringExp;
import com.artagon.xacml.v30.types.StringType;

@XacmlFunctionProvider(description="XACML string conversion functions")
public class StringConversionFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-normalize-space")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp normalizeSpace(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return StringType.STRING.create(v.getValue().trim());
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp normalizeToLowerCase(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return StringType.STRING.create(v.getValue().toLowerCase());
	}
	
	@XacmlFuncSpec(id="urn:artagon:names:tc:xacml:1.0:function:string-normalize-to-upper-case")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#string")
	public static StringExp normalizeToUpperCase(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringExp v)
	{
		return StringType.STRING.create(v.getValue().toUpperCase());
	}
}
