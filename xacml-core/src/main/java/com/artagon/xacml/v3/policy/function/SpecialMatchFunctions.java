package com.artagon.xacml.v3.policy.function;

import javax.security.auth.x500.X500Principal;

import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.X500NameType.X500NameValue;
import com.artagon.xacml.v3.types.XacmlDataTypes;

@XacmlFunctionProvider(description="XACML special match functions")
public class SpecialMatchFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue rfc822NameMatch(
			@XacmlFuncParam(type=XacmlDataTypes.STRING)StringValue pattern, 
			@XacmlFuncParam(type=XacmlDataTypes.RFC822NAME)RFC822NameValue rfc822Name)
	{
		 return XacmlDataTypes.BOOLEAN.create(rfc822Name.getValue().matches(pattern.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue x500NameMatch(
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME)X500NameValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.X500NAME)X500NameValue b)
	{
		 String n0 = a.getValue().getName(X500Principal.CANONICAL);
		 String n1 = b.getValue().getName(X500Principal.CANONICAL);
		 return XacmlDataTypes.BOOLEAN.create(n1.endsWith(n0));
	}
}
