package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.Apply;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionInvocationException;
import com.artagon.xacml.v3.FunctionReference;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.Value;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

/**
 * The class denotes application of a function to its arguments, 
 * thus encoding a {@link FunctionReference} call. The {@link Apply} can be 
 * applied to a given list of {@link Expression} instances.
 * 
 * @author Giedrius Trumpickas
 *
 */
final class DefaultApply extends XacmlObject implements Apply
{	
	private FunctionSpec spec;
	private Expression[] arguments;
	
	/**
	 * Constructs XACML apply expression with given function and list
	 * of arguments to given function.
	 * 
	 * @param spec a function to be invoked
	 * @param returnType a function return type
	 * @param arguments a function invocation arguments
	 */
	public DefaultApply(FunctionSpec spec, Expression ...arguments){
		Preconditions.checkNotNull(spec);
		Preconditions.checkNotNull(arguments == null || arguments.length > 0);
		Preconditions.checkArgument(spec.validateParameters(arguments));
		this.spec = spec;
		this.arguments = arguments;
	}
	
	public DefaultApply(FunctionSpec spec, Collection<Expression> arguments){
		this(spec, (Expression[])arguments.toArray());
	}

	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.Apply#getFunctionId()
	 */
	public String getFunctionId(){
		return spec.getId();
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return spec.resolveReturnType(arguments);
	}
	
	/* (non-Javadoc)
	 * @see com.artagon.xacml.v3.policy.Apply#evaluate(com.artagon.xacml.v3.policy.EvaluationContext)
	 */
	public Value evaluate(EvaluationContext context) 
		throws EvaluationException
	{
		try{
			return spec.invoke(context, arguments);
		}catch(EvaluationException e){
			throw e;
		}catch(Exception e){
			throw new FunctionInvocationException(context, spec, e);
		}
	}
	
	
	@Override
	public void accept(PolicyVisitor v)
	{
		v.visitEnter(this);
		for(Expression expression : arguments){
			expression.accept(v);
		}
		v.visitLeave(this);
	}	
}
