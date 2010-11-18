package com.artagon.xacml.v3.policy.function;

import javax.security.auth.x500.X500Principal;

import com.artagon.xacml.v3.sdk.function.XacmlFuncParam;
import com.artagon.xacml.v3.sdk.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.sdk.function.XacmlFuncSpec;
import com.artagon.xacml.v3.sdk.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanValue;
import com.artagon.xacml.v3.types.RFC822NameValue;
import com.artagon.xacml.v3.types.StringValue;
import com.artagon.xacml.v3.types.X500NameValue;

@XacmlFunctionProvider(description="XACML special match functions")
public class SpecialMatchFunctions 
{
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue rfc822NameMatch(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string")StringValue pattern, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:rfc822Name")RFC822NameValue rfc822Name)
	{
		 return BooleanType.BOOLEAN.create(rfc822Name.getValue().matches(pattern.getValue()));
	}
	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-match")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static 
			BooleanValue x500NameMatch(
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameValue a, 
			@XacmlFuncParam(typeId="urn:oasis:names:tc:xacml:1.0:data-type:x500Name")X500NameValue b)
	{
		 String n0 = a.getValue().getName(X500Principal.CANONICAL);
		 String n1 = b.getValue().getName(X500Principal.CANONICAL);
		 return BooleanType.BOOLEAN.create(n1.endsWith(n0));
	}
}
