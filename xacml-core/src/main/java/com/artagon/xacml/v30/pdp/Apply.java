package com.artagon.xacml.v30.pdp;

import java.util.LinkedList;
import java.util.List;

import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.ExpressionVisitor;
import com.artagon.xacml.v30.ValueExpression;
import com.artagon.xacml.v30.ValueType;
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
	 * @param params a function invocation arguments
	 */
	private Apply(Builder b)
	{
		if(!b.func.validateParameters(b.params)){
			throw new IllegalArgumentException(
					String.format(
							"Given arguments=\"%s\" are " +
							"not valid for function=\"%s\"",
							b.params, b.func));
		}
		this.spec = b.func;
		this.arguments = ImmutableList.copyOf(b.params);
		this.hashCode = Objects.hashCode(spec, arguments);
	}

	/**
	 * Creates {@link Apply} builder with
	 * a given function
	 * @param func a function
	 * @return {@link Builder}
	 */
	public static Builder builder(FunctionSpec func){
		return new Builder(func);
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
	public void accept(ExpressionVisitor expv){
		ApplyVistor v = (ApplyVistor)expv;
		v.visitEnter(this);
		for(Expression arg : arguments){
			arg.accept(v);
		}
		v.visitLeave(this);
	}

	public interface ApplyVistor extends ExpressionVisitor
	{
		void visitEnter(Apply v);
		void visitLeave(Apply v);
	}

	public final static class Builder
	{
		private FunctionSpec func;
		private List<Expression> params = new LinkedList<Expression>();

		private Builder(FunctionSpec spec){
			Preconditions.checkNotNull(spec);
			this.func = spec;
		}

		public Builder param(Iterable<Expression> params){
			Preconditions.checkNotNull(params);
			for(Expression p : params){
				Preconditions.checkNotNull(p);
				this.params.add(p);
			}
			return this;
		}

		public Builder param(Expression ...params){
			Preconditions.checkNotNull(params);
			for(Expression p : params){
				Preconditions.checkNotNull(p);
				this.params.add(p);
			}
			return this;
		}

		public Apply build(){
			return new Apply(this);
		}
	}
}
