package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.XacmlObject;

/**
 * The class denotes application of a function to its arguments, 
 * thus encoding a {@link FunctionReference} call. The {@link DefaultApply} can be 
 * applied to a given list of {@link Expression} instances.
 * 
 * @author Giedrius Trumpickas
 *
 */
public final class DefaultApply extends XacmlObject implements Apply
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
		Preconditions.checkArgument(spec.validateParameters(arguments));
		this.spec = spec;
		this.arguments = arguments;
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
