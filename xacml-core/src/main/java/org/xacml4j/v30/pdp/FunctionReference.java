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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * A function reference expression, used
 * to pass function reference to higher order
 * functions as an argument
 *
 * @author Giedrius Trumpickas
 */
public class FunctionReference implements Expression
{
	private final static Logger log = LoggerFactory.getLogger(FunctionReference.class);

	private final FunctionSpec spec;
	private final ValueType returnType;

	/**
	 * Constructs function reference expression
	 *
	 * @param spec a function specification
	 */
	public FunctionReference(FunctionSpec spec)
	{
		Preconditions.checkNotNull(spec);
		this.spec = spec;
		this.returnType = spec.resolveReturnType(ImmutableList.<Expression>of());
		Preconditions.checkState(returnType != null);
	}

	/**
	 * Gets function identifier URI
	 *
	 * @return function identifier
	 */
	public String getFunctionId(){
		return spec.getId();
	}

	@Override
	public ValueType getEvaluatesTo(){
		return returnType;
	}

	/**
	 * Invokes a function with a given parameters
	 *
	 * @param <T> value expression type
	 * @param context evaluation context
	 * @param params function parameters
	 * @return function invocation value
	 * @throws EvaluationException if function information fails
	 */
	public <T extends ValueExpression> T invoke(EvaluationContext context,
			Expression ...params) throws EvaluationException
	{
		if(log.isDebugEnabled()){
				log.debug("Invoking function reference=\"{}\"",
						spec.getId());
		}
		return spec.invoke(context, params);
	}

	/**
	 * Gets number format function parameters
	 *
	 * @return number of formal function parameters
	 */
	public int getNumberOfParams(){
		return spec.getNumberOfParams();
	}

	/**
	 * Gets function parameter specification
	 * at the given index
	 *
	 * @param index a parameter index
	 * @return {@link FunctionParamSpec} instance
	 * @exception IndexOutOfBoundsException if index
	 * is outside of bounds
	 */
	public FunctionParamSpec getParamSpecAt(int index){
		return spec.getParamSpecAt(index);
	}

	/**
	 * Implementation returns itself
	 */
	@Override
	public FunctionReference evaluate(EvaluationContext context)
			throws EvaluationException {
		return this;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("function", spec)
				.toString();
	}

	@Override
	public int hashCode(){
		return spec.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof FunctionReference)){
			return false;
		}
		FunctionReference r = (FunctionReference)o;
		return spec.equals(r.spec);
	}

	@Override
	public void accept(ExpressionVisitor fv) {
		FunctionReferenceVisitor v = (FunctionReferenceVisitor)fv;
		v.visitEnter(this);
		v.visitLeave(this);
	}

	public interface FunctionReferenceVisitor extends ExpressionVisitor
	{
		void visitEnter(FunctionReference v);
		void visitLeave(FunctionReference v);
	}
}
