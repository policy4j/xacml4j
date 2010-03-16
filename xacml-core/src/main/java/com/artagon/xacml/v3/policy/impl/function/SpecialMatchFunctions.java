package com.artagon.xacml.v3.policy.impl.function;

import javax.security.auth.x500.X500Principal;

import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.RFC822NameType.RFC822NameValue;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;
import com.artagon.xacml.v3.policy.type.X500NameType.X500NameValue;

public class SpecialMatchFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue rfc822NameMatch(
			@XacmlParam(type=DataTypes.STRING)StringValue pattern, 
			@XacmlParam(type=DataTypes.RFC822NAME)RFC822NameValue rfc822Name)
	{
		 return DataTypes.BOOLEAN.create(rfc822Name.getValue().matches(pattern.getValue()));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:x500Name-match")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static 
			BooleanValue x500NameMatch(
			@XacmlParam(type=DataTypes.X500NAME)X500NameValue a, 
			@XacmlParam(type=DataTypes.X500NAME)X500NameValue b)
	{
		 String n0 = a.getValue().getName(X500Principal.CANONICAL);
		 String n1 = b.getValue().getName(X500Principal.CANONICAL);
		 return DataTypes.BOOLEAN.create(n1.endsWith(n0));
	}
}
