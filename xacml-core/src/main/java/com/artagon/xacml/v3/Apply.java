package com.artagon.xacml.v3;

import java.util.Collection;



/**
 * The class denotes application of a function to its arguments, 
 * thus encoding a {@link FunctionReference} call. The {@link Apply} can be 
 * applied to a given list of {@link Expression} instances.
 * 
 * @author Giedrius Trumpickas
 *
 */
public class Apply extends XacmlObject implements Expression, PolicyElement
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
	public Apply(FunctionSpec spec, Expression ...arguments) 
		throws PolicySyntaxException
	{
		checkNotNull(spec != null, 
				"Can't create Apply without function, function can't be null");
		checkArgument((arguments == null || arguments.length > 0), 
				"At least one argument must be specified");
		spec.validateParametersAndThrow(arguments);
		this.spec = spec;
		this.arguments = arguments;
	}
	
	public Apply(FunctionSpec spec, Collection<Expression> arguments) 
		throws PolicySyntaxException
	{
		this(spec, arguments.toArray(new Expression[0]));
	}

	
	/**
	 * Gets XACML function identifier
	 * 
	 * @return XACML function identifier
	 */
	public String getFunctionId(){
		return spec.getId();
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return spec.resolveReturnType(arguments);
	}
	
	/**
	 * Evaluates given expression by invoking function
	 * with a given parameters
	 * 
	 * @param context an evaluation context
	 * @return expression evaluation result as {@link Value}
	 * instance
	 */
	@Override
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
