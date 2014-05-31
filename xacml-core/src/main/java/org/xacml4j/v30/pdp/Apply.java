package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.List;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.ValueType;

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
	private final FunctionSpec spec;
	private final List<Expression> arguments;

	private final int hashCode;

	/**
	 * Constructs XACML apply expression with given function and list
	 * of arguments to given function.
	 *
	 * @param b Apply expression builder
	 */
	private Apply(Builder b)
	{
		this.spec = b.func;
		this.arguments = b.paramsBuilder.build();
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
			throw new FunctionInvocationException(spec, e);
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

	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof Apply)){
			return false;
		}
		Apply a = (Apply)o;
		return spec.equals(a.spec) &&
				arguments.equals(a.arguments);
	}

	@Override
	public void accept(ExpressionVisitor expv){
		ApplyVisitor v = (ApplyVisitor)expv;
		v.visitEnter(this);
		for(Expression arg : arguments){
			arg.accept(v);
		}
		v.visitLeave(this);
	}

	public interface ApplyVisitor
		extends ExpressionVisitor
	{
		void visitEnter(Apply v);
		void visitLeave(Apply v);
	}

	public final static class Builder
	{
		private FunctionSpec func;
		private ImmutableList.Builder<Expression> paramsBuilder = ImmutableList.builder();

		private Builder(FunctionSpec spec){
			Preconditions.checkNotNull(spec);
			this.func = spec;
		}

		public Builder param(Iterable<Expression> params){
			this.paramsBuilder.addAll(params);
			return this;
		}

		public Builder param(Expression ...params){
			this.paramsBuilder.add(params);
			return this;
		}

		public Apply build(){
			return new Apply(this);
		}
	}
}
