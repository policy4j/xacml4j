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
import com.artagon.xacml.v3.sdk.function.XacmlFuncParam;
import com.artagon.xacml.v3.sdk.function.XacmlFuncParamEvaluationContext;
import com.artagon.xacml.v3.sdk.function.XacmlFuncParamFunctionReference;
import com.artagon.xacml.v3.sdk.function.XacmlFuncParamVarArg;
import com.artagon.xacml.v3.sdk.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.sdk.function.XacmlFuncSpec;
import com.artagon.xacml.v3.sdk.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanValue;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.IntegerValue;

@XacmlFunctionProvider(description="TestFunctions")
@Ignore
public class TestFunctions 
{
	@XacmlFuncSpec(id="test1")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue test1(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a, 
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b) 
	{
		return BooleanType.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFuncSpec(id="test2")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue test2(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues bag)
	{
		return IntegerType.INTEGER.create(bag.size());
	}
	
	@XacmlFuncSpec(id="test3", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue and(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)IntegerValue ...values) 
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerValue)e.evaluate(context)).getValue(); 
			
		}
		return IntegerType.INTEGER.create(v);
	}
	
	@XacmlFuncSpec(id="test4", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static IntegerValue and(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#integer", min=2)Expression ...values) 
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerValue)e.evaluate(context)).getValue(); 
			
		}
		return IntegerType.INTEGER.create(v);
	}
	
	@XacmlFuncSpec(id="test5")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)
	public static BagOfAttributeValues test5(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionSpec function,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer", isBag=true)BagOfAttributeValues bag) 
		throws EvaluationException
	{
		Collection<AttributeValue> attributes = new LinkedList<AttributeValue>();
		for(AttributeValue attr : bag.values()){
			AttributeValue v = function.invoke(context, attr);
			attributes.add(v); 
			
		}
		return IntegerType.INTEGER.bagOf(attributes);
	}
	
	@XacmlFuncSpec(id="test5VarArg")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static BooleanValue test5VarArg(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue value,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)BooleanValue ...values) 
		throws EvaluationException
	{	
		return BooleanType.BOOLEAN.create(false);
	}
	
	@XacmlFuncSpec(id="test6VarArg")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#integer")
	public static BooleanValue test6(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue a,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue b,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)BooleanValue ...values) 
		throws EvaluationException
	{	
		return BooleanType.BOOLEAN.create(false);
	}
}
