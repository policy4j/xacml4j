package com.artagon.xacml.v3.policy.function;

import javax.security.auth.x500.X500Principal;

import com.artagon.xacml.v3.spi.function.XacmlFunc;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.spi.function.XacmlParam;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.types.StringType.StringValue;
import com.artagon.xacml.v3.types.X500NameType.X500NameValue;

@XacmlFunctionProvider
public class SpecialMatchFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue rfc822NameMatch(
			@XacmlParam(type=XacmlDataTypes.STRING)StringValue pattern, 
			@XacmlParam(type=XacmlDataTypes.RFC822NAME)RFC822NameValue rfc822Name)
	{
		 return XacmlDataTypes.BOOLEAN.create(rfc822Name.getValue().matches(pattern.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-match")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static 
			BooleanValue x500NameMatch(
			@XacmlParam(type=XacmlDataTypes.X500NAME)X500NameValue a, 
			@XacmlParam(type=XacmlDataTypes.X500NAME)X500NameValue b)
	{
		 String n0 = a.getValue().getName(X500Principal.CANONICAL);
		 String n1 = b.getValue().getName(X500Principal.CANONICAL);
		 return XacmlDataTypes.BOOLEAN.create(n1.endsWith(n0));
	}
}
