package com.artagon.xacml.v30.policy;

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
	private FunctionSpec spec;
	private ValueType returnType;
	
	/**
	 * Constructs function reference expression
	 * 
	 * @param spec a function specification
	 * @param returnType a function return type
	 */
	FunctionReferenceExpression(FunctionSpec spec, ValueType returnType){
		Preconditions.checkNotNull(spec);
		Preconditions.checkNotNull(returnType);
		this.spec = spec;
		this.returnType = returnType;
	}
	
	/**
	 * Gets referenced function specification
	 * 
	 * @return {@link BaseFunctionSpec} for a 
	 * referenced function
	 */
	public FunctionSpec getSpec(){
		return spec;
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return returnType;
	}

	@Override
	public FunctionReferenceExpression evaluate(EvaluationContext context)
			throws EvaluationException {
		return this;
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
};
