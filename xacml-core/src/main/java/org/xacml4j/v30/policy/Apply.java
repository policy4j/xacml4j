package org.xacml4j.v30.policy;

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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.types.DynamicType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * The class denotes application of a function to its arguments,
 * thus encoding a {@link FunctionReference} call. The {@link Apply} can be
 * applied to a given list of {@link Expression} instances.
 *
 * @author Giedrius Trumpickas
 */
public final class Apply implements Expression
{
	private final static Logger LOG = LoggerFactory.getLogger(Apply.class);

	private final FunctionSpec spec;
	private final List<Expression> arguments;

	/**
	 * The function of the expression that is not type correct: of this
	 * expression, or of one that it holds. Empty when the check that is
	 * made when the expression is built found no type error.
	 */
	private final Optional<String> typeError;

	/**
	 * The arguments as the function gets them: {@link #arguments}, with a
	 * {@link TypeAssertion} around each argument of dynamic type.
	 */
	private final List<Expression> invocationArguments;

	/** The status message of an expression that is not type correct. It names the function only. */
	static final String NOT_TYPE_CORRECT = "Function=\"%s\" is applied to arguments that do not fit it";

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
		Optional<String> inside = typeErrorInside(arguments);
		this.typeError = inside.isPresent()?inside:typeErrorOf(spec, arguments);
		this.invocationArguments = typeError.isPresent()?arguments:withTypeAssertions(spec, arguments);
	}

	/**
	 * The check that is made when the expression is built accepts an
	 * argument of dynamic type where a parameter needs a data type of the
	 * same shape (see {@link DynamicType#isConsistent}). The value of such
	 * an argument is checked against its parameter when it is evaluated.
	 *
	 * @return the arguments, each one of dynamic type wrapped in a
	 * {@link TypeAssertion}
	 */
	private static List<Expression> withTypeAssertions(FunctionSpec spec, List<Expression> arguments)
	{
		boolean dynamic = false;
		for(Expression argument : arguments){
			dynamic |= DynamicType.isDynamic(argument.getEvaluatesTo());
		}
		if(!dynamic){
			return arguments;
		}
		// The expression is type correct, so every argument has a
		// parameter. Parameters come in the order mandatory, optional,
		// variadic, and arguments are bound by position: an argument
		// beyond the last parameter belongs to the variadic one.
		int last = spec.getNumberOfParams() - 1;
		ImmutableList.Builder<Expression> checked = ImmutableList.builder();
		for(int i = 0; i < arguments.size(); i++){
			Expression argument = arguments.get(i);
			checked.add(DynamicType.isDynamic(argument.getEvaluatesTo())
					?new TypeAssertion(argument, spec.getParamSpecAt(Math.min(i, last)), spec)
					:argument);
		}
		return checked.build();
	}

	/**
	 * Expressions compose greater expressions, and a valid expression is
	 * type correct (XACML 3.0 core, section 7.4). An expression that holds
	 * an expression with a type error is therefore not valid, also when
	 * its function would not evaluate that argument: {@code or(true, x)}
	 * with a type error in {@code x} is Indeterminate, not true. A
	 * reference to a variable stands for the expression of its definition.
	 *
	 * @return the function of the first argument expression that has a
	 * type error
	 */
	private static Optional<String> typeErrorInside(List<Expression> arguments)
	{
		for(Expression argument : arguments){
			Expression e = argument;
			// A definition can be a reference again. The chain ends: a
			// definition is built before a reference to it can be.
			while(e instanceof VariableReference){
				e = ((VariableReference)e).getDefinition().getExpression();
			}
			if(e instanceof Apply && ((Apply)e).typeError.isPresent()){
				return ((Apply)e).typeError;
			}
		}
		return Optional.empty();
	}

	/**
	 * Checks the arguments against the function, once, when the expression
	 * is built.
	 *
	 * @return the function, when its arguments are not shown to fit it
	 */
	private static Optional<String> typeErrorOf(FunctionSpec spec, List<Expression> arguments)
	{
		// Every argument has a type when the expression is built: a data
		// type, or the dynamic type. Where the type can not be told at all
		// (the data type of the attribute-designator function is a literal
		// that names no data type), the arguments are not shown to fit.
		for(int i = 0; i < arguments.size(); i++){
			try{
				arguments.get(i).getEvaluatesTo();
			}catch(RuntimeException e){
				LOG.warn("Function=\"{}\": the type of argument {} is not known when the policy is loaded. " +
						"The expression will evaluate to Indeterminate: {}", spec.getId(), i, e.toString());
				return Optional.of(spec.getId());
			}
		}
		try{
			spec.validateParametersAndThrow(arguments);
			return Optional.empty();
		}catch(SyntaxException e){
			LOG.warn("Function=\"{}\" is applied to arguments that do not fit it. " +
					"The expression will evaluate to Indeterminate: {}", spec.getId(), e.getMessage());
			return Optional.of(spec.getId());
		}catch(RuntimeException e){
			// The check itself failed, so the arguments are not shown to fit.
			LOG.warn("Function=\"{}\": the arguments can not be checked. " +
					"The expression will evaluate to Indeterminate: {}", spec.getId(), e.toString());
			return Optional.of(spec.getId());
		}
	}

	/**
	 * Tests if the number and the types of the arguments agree with the
	 * function (XACML 3.0 core, section 7.4: "Valid expressions SHALL be
	 * type correct"). An expression that holds an expression that is not
	 * type correct is not type correct either. Such an expression can be
	 * built. Its evaluation is Indeterminate with status processing-error
	 * (section 7.19.2), and its function is not invoked.
	 * <p>
	 * An argument of {@linkplain DynamicType dynamic type} is type correct
	 * where its shape fits the parameter. Its value is checked at every
	 * evaluation, with the same result when it does not fit.
	 *
	 * @return {@code false} if the arguments were not shown to fit when
	 * the expression was built
	 */
	public boolean isTypeCorrect(){
		return !typeError.isPresent();
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

	public static Builder builder(String id, Function<String, Optional<FunctionSpec>> provider){
		return new Builder(provider.apply(id)
		                           .orElseThrow(()->PolicySyntaxException.invalidFunction(id)));
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
	public ValueExpTypeInfo getEvaluatesTo(){
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
	 * @return expression evaluation result as {@link ValueExp}
	 * defaultProvider
	 */
	@Override
	public ValueExp evaluate(EvaluationContext context)
		throws EvaluationException
	{
		if(typeError.isPresent()){
			// The reason was logged when the expression was built. The
			// status message names the function only.
			throw new FunctionInvocationException(spec, NOT_TYPE_CORRECT, typeError.get());
		}
		try{
			return spec.invoke(context, invocationArguments.toArray(new Expression[]{}));
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
		default void visitEnter(Apply v) {v.accept(this);}
		default void visitLeave(Apply v) {v.accept(this);}
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
