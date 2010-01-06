package com.artagon.xacml.policy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.util.Preconditions;

public abstract class BaseFunctionInvocation implements FunctionInvocation
{
	private List<Expression> arguments;
	private FunctionSpec spec;
	
	protected BaseFunctionInvocation(FunctionSpec spec, List<Expression> arguments){
		Preconditions.checkNotNull(spec);
		Preconditions.checkNotNull(arguments);
		this.spec = spec;
		this.arguments = arguments;
	}
	
	@Override
	public final List<Expression> getArguments() {
		return Collections.unmodifiableList(arguments);
	}

	@Override
	public final FunctionSpec getSpec() {
		return spec;
	}

	protected final List<Expression> evaluate(EvaluationContext context, List<Expression> params) 
		throws PolicyEvaluationException
	{
		List<Expression> evaluated = new LinkedList<Expression>();
		for(Expression exp : params){
			if(exp instanceof ValueExpression){
				evaluated.add(((ValueExpression)exp).evaluate(context));
				continue;
			}
			evaluated.add(exp);
		}
		return evaluated;
	}

	@Override
	public Value invoke(EvaluationContext context) throws PolicyEvaluationException {
		return doInvoke(context, evaluate(context, arguments));
	}

	protected abstract  Value doInvoke(EvaluationContext context, List<Expression> arguments) 
		throws PolicyEvaluationException;
}
