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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.xacml4j.v30.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The class denotes application of a function to its arguments,
 * thus encoding a {@link FunctionReference} call. The {@link Apply} can be
 * applied to a given list of {@link Expression} instances.
 *
 * @author Giedrius Trumpickas
 */
public final class Apply implements Expression
{
	private final FunctionSpec spec;
	private final List<Expression> arguments;

	private int hashCode = 0;

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
	 * @return an immutable defaultProvider of {@link List}
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
	 * defaultProvider
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
		if(hashCode == 0){
			this.hashCode = Objects.hash(spec, arguments);
		}
		return hashCode;
	}

	@Override
	public String toString(){
		return MoreObjects
				.toStringHelper(this)
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

		public Builder params(Iterable<Expression> params){
			this.paramsBuilder.addAll(params);
			return this;
		}

		public Builder param(Expression ...params){
			return params(Arrays.asList(params));
		}

		public Apply build(){
			return new Apply(this);
		}
	}
}
