package com.artagon.xacml.policy;

import java.util.List;

public class BaseRegularFunctionImplementation extends BaseFunctionImplementation
{

	@Override
	protected Value doInvoke(EvaluationContext context,
			List<Expression> arguments) throws PolicyEvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
		Preconditions.checkNotNull(spec);
	}
}
