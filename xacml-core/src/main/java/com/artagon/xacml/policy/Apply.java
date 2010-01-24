package com.artagon.xacml.policy;

import com.artagon.xacml.util.Preconditions;

/**
 * The class denotes application of a function to its arguments, 
 * thus encoding a {@link FunctionReferenceExpression} call. The {@link Apply} can be 
 * applied to a given list of {@link Expression} instances.
 * 
 * @author Giedrius Trumpickas
 *
 */
public final class Apply implements Expression
{	
	private FunctionSpec spec;
	private Expression[] arguments;
	private ValueType returnType;
	
	
	/**
	 * Constructs XACML apply expression with given function and list
	 * of arguments to given function.
	 * 
	 * @param spec a function to be invoked
	 * @param returnType a function return type
	 * @param arguments a function invocation arguments
	 */
	Apply(FunctionSpec spec, ValueType returnType, Expression ...arguments){
		Preconditions.checkNotNull(spec);
		Preconditions.checkNotNull(returnType);
		Preconditions.checkArgument(spec.validateParameters(arguments));
		this.spec = spec;
		this.arguments = arguments;
		this.returnType = returnType;
	}

	
	/**
	 * Gets XACML function identifier
	 * 
	 * @return XACML function identifier
	 */
	public String getFunctionId(){
		return spec.getXacmlId();
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return returnType;
	}
	
	/**
	 * Evaluates given expression by invoking function
	 * with a given parameters
	 * 
	 * @param context an evaluation context
	 * @return expression evaluation result as {@link Value}
	 * instance
	 */
	public Value evaluate(EvaluationContext context) 
		throws PolicyEvaluationException
	{
		return spec.invoke(context, arguments);
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
