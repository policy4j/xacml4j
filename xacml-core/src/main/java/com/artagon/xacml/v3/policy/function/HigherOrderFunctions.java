package com.artagon.xacml.v3.policy.function;

import java.util.ArrayList;
import java.util.Collection;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionParametersValidator;
import com.artagon.xacml.v3.FunctionReference;
import com.artagon.xacml.v3.FunctionReturnTypeResolver;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.spi.function.XacmlFunc;
import com.artagon.xacml.v3.spi.function.XacmlFuncParamValidator;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnTypeResolver;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.spi.function.XacmlParamAnyAttribute;
import com.artagon.xacml.v3.spi.function.XacmlParamAnyBag;
import com.artagon.xacml.v3.spi.function.XacmlParamEvaluationContext;
import com.artagon.xacml.v3.spi.function.XacmlParamFuncReference;
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
			@XacmlParamAnyAttribute AttributeValue value,
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> bag) 
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
	@XacmlFuncReturnTypeResolver(resolverClass=MapFunctionResolverValidator.class)
	@XacmlFuncParamValidator(validatorClass=MapFunctionResolverValidator.class)
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
		AttributeValueType type = (AttributeValueType)ref.getEvaluatesTo();
		return type.bagOf().create(values);
	}

	/**
	 * An implementation of {@link FunctionReturnTypeResolver} and
	 * {@link FunctionParametersValidator} for XACML map higher
	 * order function
	 * 
	 * @author Giedrius Trumpickas
	 */
	class MapFunctionResolverValidator implements 
		FunctionParametersValidator, FunctionReturnTypeResolver
	{
		@Override
		public ValueType resolve(FunctionSpec spec, 
				Expression... arguments) {
			AttributeValueType type = (AttributeValueType)((FunctionReference)arguments[0]).getEvaluatesTo();
			return type.bagOf();
		}

		@Override
		public boolean validate(FunctionSpec spec, Expression... arguments) 
		{
			FunctionReference ref = (FunctionReference)arguments[0];
			if(ref.getSpec().getNumberOfParams() != 1){
				return false;
			}
			BagOfAttributeValues<AttributeValue> bag = (BagOfAttributeValues<AttributeValue>)arguments[1];
			return true;
		}
		
	}
}
