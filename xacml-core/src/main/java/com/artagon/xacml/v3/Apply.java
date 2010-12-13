package com.artagon.xacml.v3;

import java.util.Arrays;
import java.util.Collection;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;



/**
 * The class denotes application of a function to its arguments, 
 * thus encoding a {@link FunctionReference} call. The {@link Apply} can be 
 * applied to a given list of {@link Expression} instances.
 * 
 * @author Giedrius Trumpickas
 *
 */
public class Apply implements Expression, PolicyElement
{	
	private FunctionSpec spec;
	private Expression[] arguments;
	
	private int hashCode;
	
	/**
	 * Constructs XACML apply expression with given function and list
	 * of arguments to given function.
	 * 
	 * @param spec a function to be invoked
	 * @param returnType a function return type
	 * @param arguments a function invocation arguments
	 */
	public Apply(FunctionSpec spec, Expression ...arguments) 
		throws XacmlSyntaxException
	{
		Preconditions.checkNotNull(spec != null, 
				"Can't create Apply without function, function can't be null");
		Preconditions.checkArgument((arguments == null || arguments.length > 0), 
				"At least one argument must be specified");
		if(!spec.validateParameters(arguments)){
			throw new IllegalArgumentException(
					String.format(
							"Given arguments=\"%s\" are not valid for function=\"%s\"", 
							Arrays.deepToString(arguments), 
							spec.getId()));
		}
		this.spec = spec;
		this.arguments = Arrays.copyOf(arguments, arguments.length);
		this.hashCode = 31 * spec.hashCode() +  Arrays.hashCode(arguments);
	}
	
	public Apply(FunctionSpec spec, 
			Collection<Expression> arguments) 
		throws XacmlSyntaxException
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
		.add("arguments", Arrays.deepToString(arguments))
		.toString();
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
