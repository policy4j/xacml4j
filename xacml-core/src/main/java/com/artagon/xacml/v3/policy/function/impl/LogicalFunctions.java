package com.artagon.xacml.v3.policy.function.impl;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionInvocationException;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamEvaluationContext;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamVarArg;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;

/**
 * This class contains the implementation for XACML logical functions 
 * that operate on arguments of data-type “http://www.w3.org/2001/XMLSchema#boolean”.
 * 
 * @author Giedrius Trumpickas
 */
public class LogicalFunctions 
{
	
	/**
	 * This function SHALL return "True" if it has no arguments and SHALL return "False" 
	 * if one of its arguments evaluates to "False". 
	 * The order of evaluation SHALL be from first argument to last. 
	 * The evaluation SHALL stop with a result of "False" if any argument 
	 * evaluates to "False", leaving the rest of the arguments unevaluated.
	 * 
	 * @param context
	 * @param values
	 * @return
	 * @throws EvaluationException
	 */
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:and", evaluateArguments=false)
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue and(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamVarArg(type=DataTypes.BOOLEAN, min=0)Expression ...values) 
		throws EvaluationException
	{
		Boolean r = Boolean.TRUE;
		for(Expression e : values){
			r = r & ((BooleanValue)e.evaluate(context)).getValue(); 
			if(!r){
				break;
			}
		}
		return DataTypes.BOOLEAN.create(r);
	}

	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:not")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue not(
			@XacmlParam(type=DataTypes.BOOLEAN)BooleanValue v)
	{
		return DataTypes.BOOLEAN.create(!v.getValue());
	}
	
	/**
	 * This function SHALL return "False" if it has no arguments and SHALL return "True" 
	 * if at least one of its arguments evaluates to "True". 
	 * The order of evaluation SHALL be from first argument to last. 
	 * The evaluation SHALL stop with a result of "True" if any argument 
	 * evaluates to "True", leaving the rest of the arguments unevaluated.
	 * 
	 * @param context
	 * @param values
	 * @return
	 * @throws EvaluationException
	 */
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:or", evaluateArguments=false)
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue or(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamVarArg(type=DataTypes.BOOLEAN, min=0)Expression...values) 
		throws EvaluationException
	{
		Boolean r = Boolean.TRUE;
		for(Expression e : values){
			Boolean v = ((BooleanValue)e.evaluate(context)).getValue();
			r = r & v; 
			if(r){
				break;
			}
		}
		return DataTypes.BOOLEAN.create(r);
	}
	
	
	/**
	 * The first argument to this function SHALL be of data-type http://www.w3.org/2001/XMLSchema#integer. 
	 * The remaining arguments SHALL be of data-type http://www.w3.org/2001/XMLSchema#boolean. 
	 * The first argument specifies the minimum number of the remaining arguments 
	 * that MUST evaluate to "True" for the expression to be considered "True". 
	 * If the first argument is 0, the result SHALL be "True". 
	 * If the number of arguments after the first one is less than the 
	 * value of the first argument, then the expression SHALL result in "Indeterminate". 
	 * The order of evaluation SHALL be: first evaluate the integer value, and 
	 * then evaluate each subsequent argument. The evaluation SHALL stop and return "True" 
	 * if the specified number of arguments evaluate to "True". 
	 * The evaluation of arguments SHALL stop if it is determined that evaluating 
	 * the remaining arguments will not satisfy the requirement.
	 * 
	 * @param context
	 * @param n
	 * @param values
	 * @return
	 * @throws EvaluationException
	 */
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:n-of")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue nof(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue n,
			@XacmlParamVarArg(type=DataTypes.BOOLEAN, min=0)Expression...values) 
		throws EvaluationException
	{
		Boolean r = Boolean.TRUE;
		if(values.length < n.getValue()){
			throw new FunctionInvocationException("Number of arguments=\"%s\" is " +
					"less than minimum required number=\"%s\"", values.length, n.getValue());
		}
		for(int i = 0; i < n.getValue(); i++ ){
			r &= ((BooleanValue)values[i].evaluate(context)).getValue();
		}
		return DataTypes.BOOLEAN.create(r);
	}
}
