package com.artagon.xacml.v3.policy.function;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.ValueExpression;
import com.artagon.xacml.v3.spi.function.XacmlFuncParam;
import com.artagon.xacml.v3.spi.function.XacmlFuncParamEvaluationContext;
import com.artagon.xacml.v3.spi.function.XacmlFuncParamVarArg;
import com.artagon.xacml.v3.spi.function.XacmlFuncReturnType;
import com.artagon.xacml.v3.spi.function.XacmlFuncSpec;
import com.artagon.xacml.v3.spi.function.XacmlFunctionProvider;
import com.artagon.xacml.v3.types.BooleanType;
import com.artagon.xacml.v3.types.BooleanValue;
import com.artagon.xacml.v3.types.IntegerType.IntegerValue;

/**
 * This class contains the implementation for XACML logical functions 
 * that operate on arguments of data-type “http://www.w3.org/2001/XMLSchema#boolean”.
 * 
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML logical fucntions")
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
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:and", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue and(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)Expression ...values) 
		throws EvaluationException
	{
		Boolean r = Boolean.TRUE;
		for(Expression e : values){
			r = r & ((BooleanValue)e.evaluate(context)).getValue(); 
			if(!r){
				break;
			}
		}
		return BooleanType.BOOLEAN.create(r);
	}

	
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:not")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue not(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanValue v)
	{
		return BooleanType.BOOLEAN.create(!v.getValue());
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
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:or", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue or(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)Expression...values) 
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
		return BooleanType.BOOLEAN.create(r);
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
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:n-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue nof(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerValue n,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)Expression...values) 
		throws EvaluationException
	{
		if(values.length < n.getValue()){
			throw new IllegalArgumentException(String.format(
					"Number of arguments=\"%s\" is less " +
					"than minimum required number=\"%s\"", 
					values.length, n.getValue()));
		}
		if(n.getValue() > Integer.MAX_VALUE){
			throw new IllegalArgumentException(String.format(
					"First parameter=\"%s\" is bigger than=\"%d\"", 
					n, Integer.MAX_VALUE));
		}
		BooleanValue TRUE = BooleanType.BOOLEAN.create(true);
		if(n.getValue() == 0){
			return TRUE;
		}
		int count = 0;
		int num = n.getValue().intValue();
		for(int i = 0; i < values.length; i++ ){
			ValueExpression v = (ValueExpression)values[i].evaluate(context);
			 if(v.equals(TRUE)){
				 count++;
				 if(num == count){
					 return TRUE;
				 }
			 }
		}
		return BooleanType.BOOLEAN.create(false);
	}
}
