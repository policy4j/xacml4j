package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.FunctionReference;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

/**
 * A function reference expression, used
 * to pass function reference to higher order
 * functions as an argument
 * 
 * @author Giedrius Trumpickas
 */
final class DefaultFunctionReference extends XacmlObject implements FunctionReference
{
	private FunctionSpec spec;
	private ValueType returnType;
	
	/**
	 * Constructs function reference expression
	 * 
	 * @param spec a function specification
	 * @param returnType a function return type
	 */
	public DefaultFunctionReference(FunctionSpec spec){
		Preconditions.checkNotNull(spec);
		this.spec = spec;
		this.returnType = spec.resolveReturnType();
		Preconditions.checkState(returnType != null);
	}
	
	@Override
	public FunctionSpec getSpec(){
		return spec;
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return returnType;
	}

	@Override
	public DefaultFunctionReference evaluate(EvaluationContext context)
			throws EvaluationException {
		return this;
	}
	
	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
};
