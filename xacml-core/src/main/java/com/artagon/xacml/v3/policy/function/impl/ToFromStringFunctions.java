package com.artagon.xacml.v3.policy.function.impl;

import com.artagon.xacml.v3.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.type.BooleanType;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.StringType;

public class ToFromStringFunctions
{
	/*

urn:oasis:names:tc:xacml:3.0:function:integer-from-string 
urn:oasis:names:tc:xacml:3.0:function:string-from-integer 
urn:oasis:names:tc:xacml:3.0:function:double-from-string
urn:oasis:names:tc:xacml:3.0:function:string-from-double 
urn:oasis:names:tc:xacml:3.0:function:time-from-string 
urn:oasis:names:tc:xacml:3.0:function:string-from-time 
urn:oasis:names:tc:xacml:3.0:function:date-from-string 
urn:oasis:names:tc:xacml:3.0:function:string-from-date
urn:oasis:names:tc:xacml:3.0:function:dateTime-from-string 
urn:oasis:names:tc:xacml:3.0:function:string-from-dateTime 
urn:oasis:names:tc:xacml:3.0:function:anyURI-from-string
urn:oasis:names:tc:xacml:3.0:function:string-from-anyURI 
urn:oasis:names:tc:xacml:3.0:function:dayTimeDuration-from-string 
urn:oasis:names:tc:xacml:3.0:function:string-from-dayTimeDuration 
urn:oasis:names:tc:xacml:3.0:function:yearMonthDuration-from-string 
urn:oasis:names:tc:xacml:3.0:function:string-from-yearMonthDuration 
urn:oasis:names:tc:xacml:3.0:function:x500Name-from-string
urn:oasis:names:tc:xacml:3.0:function:string-from-x500Name
urn:oasis:names:tc:xacml:3.0:function:rfc822Name-from-string 
urn:oasis:names:tc:xacml:3.0:function:string-from-rfc822Name 
urn:oasis:names:tc:xacml:3.0:function:ipAddress-from-string 
urn:oasis:names:tc:xacml:3.0:function:string-from-ipAddress 
urn:oasis:names:tc:xacml:3.0:function:dnsName-from-string 
urn:oasis:names:tc:xacml:3.0:function:string-from-dnsName
	 */
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:string-from-boolean")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringType.StringValue stringFromBoolean(
			@XacmlParam(type=DataTypes.BOOLEAN)BooleanType.BooleanValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanType.BooleanValue booleanFromString(
			@XacmlParam(type=DataTypes.STRING)StringType.StringValue v)
	{
		return DataTypes.STRING.create(v.toXacmlString());
	}
}
