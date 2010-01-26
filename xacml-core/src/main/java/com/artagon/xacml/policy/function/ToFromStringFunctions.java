package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.policy.function.annotations.XacmlFuncParam;
import com.artagon.xacml.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.policy.type.BooleanType;
import com.artagon.xacml.policy.type.StringType;
import com.artagon.xacml.policy.type.XacmlDataType;

public class ToFromStringFunctions
{
	/*
	 * urn:oasis:names:tc:xacml:3.0:function:boolean-from-string

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
	@XacmlFuncReturnType(type=XacmlDataType.STRING)
	public static StringType.StringValue toString(
			@XacmlFuncParam(type=XacmlDataType.BOOLEAN)BooleanType.BooleanValue v)
	{
		return XacmlDataType.STRING.create(v.toXacmlString());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:3.0:function:boolean-from-string")
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanType.BooleanValue fromString(
			@XacmlFuncParam(type=XacmlDataType.STRING)StringType.StringValue v)
	{
		return XacmlDataType.STRING.create(v.toXacmlString());
	}
}
