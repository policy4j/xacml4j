package com.artagon.xacml.v3.policy.function;

import java.util.ArrayList;
import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.BagOfAttributeValuesType;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.FunctionReference;
import com.artagon.xacml.v3.FunctionSpec;

import com.artagon.xacml.v3.spi.function.annotiations.XacmlFunc;
import com.artagon.xacml.v3.spi.function.annotiations.XacmlFuncDynamicReturnType;
import com.artagon.xacml.v3.spi.function.annotiations.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.annotiations.XacmlFunctionProvider;
import com.artagon.xacml.v3.spi.function.annotiations.XacmlParamAnyBag;
import com.artagon.xacml.v3.spi.function.annotiations.XacmlParamEvaluationContext;
import com.artagon.xacml.v3.spi.function.annotiations.XacmlParamFuncReference;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;

@XacmlFunctionProvider
public class HigherOrderFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:any-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public BooleanValue anyOf(
			@XacmlParamEvaluationContext EvaluationContext context, 
			@XacmlParamFuncReference FunctionReference ref, 
			AttributeValue value,
			BagOfAttributeValues<AttributeValue> bag) 
		throws EvaluationException
	{
		FunctionSpec p =  ref.getSpec();
		for(AttributeValue valueFromBag : bag.values()){
			BooleanValue r = p.invoke(context, value, valueFromBag);
			if(r.getValue()){
				return XacmlDataTypes.BOOLEAN.create(true);
			}
		}
		return XacmlDataTypes.BOOLEAN.create(false);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:map")
	@XacmlFuncDynamicReturnType(resolver="resolveMapType")
	public BagOfAttributeValues<? extends AttributeValue> map(
			@XacmlParamEvaluationContext EvaluationContext context, 
			@XacmlParamFuncReference FunctionReference ref, 
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> bag) 
		throws EvaluationException
	{
		FunctionSpec p =  ref.getSpec();
		Collection<AttributeValue> values = new ArrayList<AttributeValue>(bag.size());
		for(AttributeValue v : bag.values()){
			values.add((AttributeValue)p.invoke(context, v));
		}
		return resolveMapType(ref, bag).create(values);
	}

	private static BagOfAttributeValuesType<? extends AttributeValue> resolveMapType(
			FunctionReference ref, 
			BagOfAttributeValues<AttributeValue> values)
	{
		AttributeValueType type = (AttributeValueType)ref.getSpec().resolveReturnType();
		return type.bagOf();
	}
}
