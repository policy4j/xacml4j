package com.artagon.xacml.v3.spi.function;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Ignore;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;

@Ignore
public class TestFunctions 
{
	@XacmlFuncSpec(id="test1")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue test1(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue a, 
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="test2")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerType.IntegerValue test2(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerType.IntegerValue> bag)
	{
		return XacmlDataTypes.INTEGER.create(bag.size());
	}
	
	@XacmlFuncSpec(id="test3", evaluateArguments=false)
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerType.IntegerValue and(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(type=XacmlDataTypes.INTEGER, min=2)IntegerType.IntegerValue ...values) 
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerType.IntegerValue)e.evaluate(context)).getValue(); 
			
		}
		return XacmlDataTypes.INTEGER.create(v);
	}
	
	@XacmlFuncSpec(id="test4", evaluateArguments=false)
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerType.IntegerValue and(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(type=XacmlDataTypes.INTEGER, min=2)Expression ...values) 
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerType.IntegerValue)e.evaluate(context)).getValue(); 
			
		}
		return XacmlDataTypes.INTEGER.create(v);
	}
	
	@XacmlFuncSpec(id="test5")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerType.IntegerValue> test5(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionSpec function,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerType.IntegerValue> bag) 
		throws EvaluationException
	{
		Collection<AttributeValue> attributes = new LinkedList<AttributeValue>();
		for(AttributeValue attr : bag.values()){
			AttributeValue v = function.invoke(context, attr);
			attributes.add(v); 
			
		}
		return XacmlDataTypes.INTEGER.bag(attributes);
	}
	
	@XacmlFuncSpec(id="test5VarArg")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static BooleanValue test5VarArg(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue value,
			@XacmlFuncParamVarArg(type=XacmlDataTypes.BOOLEAN, min=0)BooleanValue ...values) 
		throws EvaluationException
	{	
		return XacmlDataTypes.BOOLEAN.create(false);
	}
	
	@XacmlFuncSpec(id="test6VarArg")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static BooleanValue test6(
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue a,
			@XacmlFuncParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue b,
			@XacmlFuncParamVarArg(type=XacmlDataTypes.BOOLEAN, min=0)BooleanValue ...values) 
		throws EvaluationException
	{	
		return XacmlDataTypes.BOOLEAN.create(false);
	}
}
