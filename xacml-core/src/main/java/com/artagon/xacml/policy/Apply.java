package com.artagon.xacml.policy;

import java.util.Arrays;
import java.util.List;

import com.artagon.xacml.FunctionId;
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
	private List<Expression> arguments;
	private FunctionImplementation function;
	
	/**
	 * Constructs XACML apply expression with given function and list
	 * and list
	 * 
	 * @param function
	 * @param arguments
	 */
	public Apply(FunctionSpec spec, List<Expression> arguments){
		Preconditions.checkNotNull(spec);
		Preconditions.checkNotNull(arguments);
		Preconditions.checkArgument(spec.validateParameters(arguments), 
				"Given list of parameters can't be used for a given function spec=\"%s\"", spec);
		this.spec = spec;
		this.arguments = arguments;
		this.function = spec.getImplementation();
	}
	
	/**
	 * Constructs XACML apply expression with given function and list
	 * of arguments to given function.
	 * 
	 * @param function
	 * @param arguments
	 */
	public Apply(FunctionSpec spec, Expression ... arguments){
		this(spec, Arrays.asList(arguments));
	}
	
	/**
	 * Gets function identifier
	 * 
	 * @return XACML function identifier
	 */
	public FunctionId getFunctionId(){
		return spec.getId();
	}
	
	@Override
	public ValueType getEvaluatesTo(){
		return function.resolveReturnType(arguments);
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
		return function.invoke(context, arguments);
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
