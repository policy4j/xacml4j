package org.xacml4j.v30.policy.function;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.spi.function.XacmlFuncParam;
import org.xacml4j.v30.spi.function.XacmlFuncParamEvaluationContext;
import org.xacml4j.v30.spi.function.XacmlFuncParamVarArg;
import org.xacml4j.v30.spi.function.XacmlFuncReturnType;
import org.xacml4j.v30.spi.function.XacmlFuncSpec;
import org.xacml4j.v30.spi.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.BooleanType;
import org.xacml4j.v30.types.IntegerExp;


/**
 * This class contains the implementation for XACML logical functions
 * that operate on arguments of data-type "http://www.w3.org/2001/XMLSchema#boolean".
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
	 * @return {@link BooleanExp} representing function evaluation result
	 * @throws EvaluationException if an evaluation error occurs
	 */
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:and", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp and(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)Expression ...values)
		throws EvaluationException
	{
		Boolean r = Boolean.TRUE;
		for(Expression e : values){
			r = r & ((BooleanExp)e.evaluate(context)).getValue();
			if(!r){
				break;
			}
		}
		return BooleanType.BOOLEAN.create(r);
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:not")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanExp not(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean")BooleanExp v)
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
	public static BooleanExp or(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)Expression...values)
		throws EvaluationException
	{
		Boolean r = Boolean.FALSE;
		for(Expression e : values){
			Boolean v = ((BooleanExp)e.evaluate(context)).getValue();
			r = r | v;
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
	public static BooleanExp nof(
			@XacmlFuncParamEvaluationContext EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer")IntegerExp n,
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
		BooleanExp TRUE = BooleanType.BOOLEAN.create(true);
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
