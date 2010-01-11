package com.artagon.xacml.policy;

import com.artagon.xacml.util.Preconditions;

/**
 * A function reference expression, used
 * to pass function reference to higher order
 * functions as an argument
 * 
 * @author Giedrius Trumpickas
 */
public class FunctionReferenceExpression implements Expression
{
	private RegularFunctionSpec spec;
	
	/**
	 * Constructs function reference expression
	 * 
	 * @param spec a function specification
	 */
	public FunctionReferenceExpression(RegularFunctionSpec spec){
		Preconditions.checkNotNull(spec);
		this.spec = spec;
	}
	
	/**
	 * Gets referenced function specification
	 * 
	 * @return {@link FunctionSpec} for a 
	 * referenced function
	 */
	public RegularFunctionSpec getSpec(){
		return spec;
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return spec.getReturnType();
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
