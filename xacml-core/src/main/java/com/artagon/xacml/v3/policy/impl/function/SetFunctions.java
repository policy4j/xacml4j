package com.artagon.xacml.v3.policy.impl.function;

import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.spi.function.XacmlFunc;
import com.artagon.xacml.v3.policy.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.spi.function.XacmlParam;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;

public class SetFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-intersection")
	@XacmlFuncReturnType(type=DataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerValue> integerIntersection(
			@XacmlParam(type=DataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=DataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN, isBag=true)
	public static BagOfAttributeValues<BooleanValue> booleanIntersection(
			@XacmlParam(type=DataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlParam(type=DataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return a.intersection(b);
	}
}
