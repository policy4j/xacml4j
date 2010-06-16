package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.spi.function.XacmlFunc;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.spi.function.XacmlParam;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;

@XacmlFunctionProvider
public class SetFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerValue> integerIntersection(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerValue> integerUnion(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return a.union(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:integer-subset")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue integerSubest(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> a,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerValue> b) 
	{
		return XacmlDataTypes.BOOLEAN.create(b.containsAll(a));
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-intersection")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN, isBag=true)
	public static BagOfAttributeValues<BooleanValue> booleanIntersection(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return a.intersection(b);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:boolean-union")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN, isBag=true)
	public static BagOfAttributeValues<BooleanValue> booleanUnion(
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> a,
			@XacmlParam(type=XacmlDataTypes.BOOLEAN, isBag=true)BagOfAttributeValues<BooleanValue> b) 
	{
		return a.union(b);
	}
}
