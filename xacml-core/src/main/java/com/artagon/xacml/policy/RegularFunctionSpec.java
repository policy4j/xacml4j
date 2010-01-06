package com.artagon.xacml.policy;

import java.util.List;

import com.artagon.xacml.FunctionId;
import com.artagon.xacml.util.Preconditions;

public class RegularFunctionSpec extends BaseFunctionSpec
{
	private ValueType returnType;
	
	public RegularFunctionSpec(FunctionId id, 
			ValueType returnType, List<ParamSpec> paramSpec) {
		super(id, paramSpec);
		Preconditions.checkNotNull(returnType);
		this.returnType = returnType;
	}

	@Override
	public FunctionInvocation createInvocation(List<Expression> parameters) {
		return new BaseFunctionInvocation(this, parameters) {
			
			@Override
			public ValueType getReturnType() {
				return returnType;
			}
			
			@Override
			protected Value doInvoke(EvaluationContext context,
					List<Expression> arguments) throws PolicyEvaluationException {
				return null;
			}
		};
	}
}
