package com.artagon.xacml.policy;

import com.artagon.xacml.util.Preconditions;

public class FunctionReferenceExpression implements Expression
{
	private FunctionSpec spec;
	
	/**
	 * Constructs function reference expression
	 * 
	 * @param spec a function specification
	 */
	public FunctionReferenceExpression(FunctionSpec spec){
		Preconditions.checkNotNull(spec);
		this.spec = spec;
	}
	
	/**
	 * Gets referenced function specification
	 * 
	 * @return {@link FunctionSpec} for a 
	 * referenced function
	 */
	public FunctionSpec getSpec(){
		return spec;
	}
	

	@Override
	public FunctionReferenceExpression evaluate(EvaluationContext context)
			throws PolicyEvaluationException {
		return this;
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
};
