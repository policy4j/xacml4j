package com.artagon.xacml.v3.policy.impl.function;

import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.StringType.StringValue;

public class StringConversionFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-normalize-space")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringValue normalizeSpace(
			@XacmlParam(type=DataTypes.STRING)StringValue v)
	{
		return DataTypes.STRING.create(v.getValue().trim());
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:string-normalize-to-lower-case")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringValue normalizeToLowerCase(
			@XacmlParam(type=DataTypes.STRING)StringValue v)
	{
		return DataTypes.STRING.create(v.getValue().toLowerCase());
	}
	
	@XacmlFunc(id="urn:artagon:names:tc:xacml:1.0:function:string-normalize-to-upper-case")
	@XacmlFuncReturnType(type=DataTypes.STRING)
	public static StringValue normalizeToUpperCase(
			@XacmlParam(type=DataTypes.STRING)StringValue v)
	{
		return DataTypes.STRING.create(v.getValue().toUpperCase());
	}
}
