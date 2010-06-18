package com.artagon.xacml.v3;

import com.google.common.base.Preconditions;

/**
 * A function reference expression, used
 * to pass function reference to higher order
 * functions as an argument
 * 
 * @author Giedrius Trumpickas
 */
public class FunctionReference extends XacmlObject implements Expression
{
	private FunctionSpec spec;
	private ValueType returnType;
	
	/**
	 * Constructs function reference expression
	 * 
	 * @param spec a function specification
	 * @param returnType a function return type
	 */
	public FunctionReference(FunctionSpec spec){
		Preconditions.checkNotNull(spec);
		this.spec = spec;
		this.returnType = spec.resolveReturnType();
		Preconditions.checkState(returnType != null);
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return returnType;
	}
	
	/**
	 * Invokes a function with a given parameters
	 * 
	 * @param <T>
	 * @param context
	 * @param params
	 * @return
	 * @throws EvaluationException
	 */
	public <T extends Value> T invoke(EvaluationContext context, 
			Expression ...params) throws EvaluationException
	{
		try{
			return spec.invoke(context, params);
		}finally{
			
		}
	}
	
	/**
	 * Implementation returns itself
	 */
	@Override
	public FunctionReference evaluate(EvaluationContext context)
			throws EvaluationException {
		return this;
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
};
