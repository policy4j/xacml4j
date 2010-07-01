package com.artagon.xacml.v3.policy.function;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.google.common.base.Preconditions;

@XacmlFunctionProvider(description="XACML higher order functions")
public class HigherOrderFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:any-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyOf(
			@XacmlParamEvaluationContext EvaluationContext context, 
			@XacmlParamFuncReference FunctionReference ref, 
			@XacmlParamAnyAttribute AttributeValue value,
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> bag) 
		throws EvaluationException
	{
		for(AttributeValue valueFromBag : bag.values()){
			BooleanValue r = ref.invoke(context, value, valueFromBag);
			if(r.getValue()){
				return XacmlDataTypes.BOOLEAN.create(true);
			}
		}
		return XacmlDataTypes.BOOLEAN.create(false);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:all-of")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue allOf(
			@XacmlParamEvaluationContext EvaluationContext context, 
			@XacmlParamFuncReference FunctionReference ref, 
			@XacmlParamAnyAttribute AttributeValue value,
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> bag) 
		throws EvaluationException
	{
		for(AttributeValue valueFromBag : bag.values()){
			BooleanValue r = ref.invoke(context, value, valueFromBag);
			if(!r.getValue()){
				return XacmlDataTypes.BOOLEAN.create(false);
			}
		}
		return XacmlDataTypes.BOOLEAN.create(true);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:any-of-any")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyOfAny(
			@XacmlParamEvaluationContext EvaluationContext context, 
			@XacmlParamFuncReference FunctionReference ref, 
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> a,
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> b) 
		throws EvaluationException
	{
		for(AttributeValue aValue : a.values()){
			for(AttributeValue bValue : b.values()){
				BooleanValue r = ref.invoke(context, aValue, bValue);
				if(r.getValue()){
					return XacmlDataTypes.BOOLEAN.create(true);
				}
			}
		}
		return XacmlDataTypes.BOOLEAN.create(false);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:all-of-any")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue allOfAny(
			@XacmlParamEvaluationContext EvaluationContext context, 
			@XacmlParamFuncReference FunctionReference ref, 
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> a,
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> b) 
		throws EvaluationException
	{
		boolean result = true;
		for(AttributeValue v : a.values()){
			result  &= anyOf(context, ref, v, b).getValue();
			if(!result){
				break;
			}
		}
		return XacmlDataTypes.BOOLEAN.create(result);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:any-of-all")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue anyOfAll(
			@XacmlParamEvaluationContext EvaluationContext context, 
			@XacmlParamFuncReference FunctionReference ref, 
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> a,
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> b) 
		throws EvaluationException
	{
		for(AttributeValue va : a.values())
		{
			boolean result = allOf(context, ref, va, b).getValue();
			if(result){
				return XacmlDataTypes.BOOLEAN.create(true);
			}
		}
		return XacmlDataTypes.BOOLEAN.create(false);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:all-of-all")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue allOfAll(
			@XacmlParamEvaluationContext EvaluationContext context, 
			@XacmlParamFuncReference FunctionReference ref, 
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> a,
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> b) 
		throws EvaluationException
	{
		for(AttributeValue aValue : a.values())
		{
			for(AttributeValue bValue : b.values()){
				BooleanValue r = ref.invoke(context, aValue, bValue);
				if(!r.getValue()){
					return XacmlDataTypes.BOOLEAN.create(false);
				}
			}
		}
		return XacmlDataTypes.BOOLEAN.create(true);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:map")
	@XacmlFuncReturnTypeResolver(resolverClass=MapFunctionResolverValidator.class)
	@XacmlFuncParamValidator(validatorClass=MapFunctionResolverValidator.class)
	public static  BagOfAttributeValues<? extends AttributeValue> map(
			@XacmlParamEvaluationContext EvaluationContext context, 
			@XacmlParamFuncReference FunctionReference ref, 
			@XacmlParamAnyBag BagOfAttributeValues<AttributeValue> bag) 
		throws EvaluationException
	{
		Collection<AttributeValue> values = new ArrayList<AttributeValue>(bag.size());
		for(AttributeValue v : bag.values()){
			values.add((AttributeValue)ref.invoke(context, v));
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
	public static class MapFunctionResolverValidator implements 
		FunctionParametersValidator, FunctionReturnTypeResolver
	{
		private final static Logger log = LoggerFactory.getLogger(MapFunctionResolverValidator.class);
		
		public MapFunctionResolverValidator(){
		}
		
		@Override
		public ValueType resolve(FunctionSpec spec, 
				Expression... arguments) 
		{
			Preconditions.checkArgument(arguments != null, 
					"Can't resolve function=\"%s\" return type " +
					"dynamically, arguments must be specified", spec.getId());
			Preconditions.checkArgument(arguments.length == spec.getNumberOfParams(), 
					"Can't resolve function=\"%s\" return type " +
					"dynamically, function requires 2 parameters to be specified", spec.getId());
			Preconditions.checkArgument(arguments[0] instanceof FunctionReference, 
					"First function argument must be function reference");
			AttributeValueType type = (AttributeValueType)((FunctionReference)arguments[0]).getEvaluatesTo();
			return type.bagOf();
		}

		@Override
		public boolean validate(FunctionSpec spec, Expression... arguments) 
		{
			if(log.isDebugEnabled()){
				log.debug("Validating function=\"{}\" parameters", spec.getId());
			}
			if(arguments == null || 
					arguments.length != 2){
				return false;
			}
			if(!(arguments[0] instanceof FunctionReference)){
				return false;
			}
			FunctionReference ref = (FunctionReference)arguments[0];
			if(ref.getNumberOfParams() != 1){
				return false;
			}
			return true;
		}
	}
}
