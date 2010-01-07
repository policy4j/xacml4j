package com.artagon.xacml.policy;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class BaseFunctionImplementation implements FunctionImplementation
{
	
	/**
	 * Evaluates function argument expressions
	 * 
	 * @param context an evaluation context
	 * @param arguments a function arguments
	 * @return list of evaluated arguments
	 * @throws PolicyEvaluationException if an evaluation
	 * error occurs
	 */
	protected final List<Expression> evaluate(EvaluationContext context, List<Expression> params) 
		throws PolicyEvaluationException
	{
		List<Expression> evaluated = new LinkedList<Expression>();
		for(Expression exp : params){
			evaluated.add(exp.evaluate(context));
		}
		return evaluated;
	}
	
	@Override
	public Value invoke(EvaluationContext context, Expression... expressions)
			throws PolicyEvaluationException {
		return doInvoke(context, Arrays.asList(expressions));
	}
	
	@Override
	public Value invoke(EvaluationContext context, List<Expression> arguments) throws PolicyEvaluationException {
		return doInvoke(context, evaluate(context, arguments));
	}

	protected abstract  Value doInvoke(EvaluationContext context, List<Expression> arguments) 
		throws PolicyEvaluationException;
}
