package com.artagon.xacml.v30;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * The class denotes application of a function to its arguments, 
 * thus encoding a {@link FunctionReference} call. The {@link Apply} can be 
 * applied to a given list of {@link Expression} instances.
 * 
 * @author Giedrius Trumpickas
 *
 */
public class Apply implements Expression
{	
	private FunctionSpec spec;
	private List<Expression> arguments;
	
	private int hashCode;
	
	/**
	 * Constructs XACML apply expression with given function and list
	 * of arguments to given function.
	 * 
	 * @param spec a function to be invoked
	 * @param returnType a function return type
	 * @param arguments a function invocation arguments
	 */
	public Apply(FunctionSpec spec, 
			List<Expression> arguments) 
		throws XacmlSyntaxException
	{
		Preconditions.checkNotNull(spec != null, 
				"Can't create Apply without function, function can't be null");
		Preconditions.checkNotNull(arguments);
		if(!spec.validateParameters(arguments)){
			throw new IllegalArgumentException(
					String.format(
							"Given arguments=\"%s\" are not valid for function=\"%s\"", 
							arguments, spec));
		}
		this.spec = spec;
		this.arguments = ImmutableList.copyOf(arguments);
		this.hashCode = Objects.hashCode(spec, arguments);
	}
	
	public Apply(FunctionSpec spec, 
			Expression ...arguments) 
		throws XacmlSyntaxException
	{
		this(spec, (arguments == null)?
				Collections.<Expression>emptyList():
					Arrays.asList(arguments));
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
	 * Gets function invocation arguments
	 * 
	 * @return an immutable instance of {@link List}
	 */
	public List<Expression> getArguments(){
		return arguments;
	}
	
	/**
	 * Evaluates given expression by invoking function
	 * with a given parameters
	 * 
	 * @param context an evaluation context
	 * @return expression evaluation result as {@link ValueExpression}
	 * instance
	 */
	@Override
	public ValueExpression evaluate(EvaluationContext context) 
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
	public int hashCode(){
		return hashCode;
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("function", spec)
		.add("arguments", arguments)
		.toString();
	}
	
	
	@Override
	public void accept(ExpressionVisitor v){
		v.visit(this);
	}	
}
