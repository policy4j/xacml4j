package org.xacml4j.v30;

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

/**
 * A XACML policy expression
 *
 * @author Giedrius Trumpickas
 */
public interface Expression
{
	/**
	 * Gets type to which this expression
	 * evaluates to
	 *
	 * @return {@link ValueExpTypeInfo}
	 */
	<VT extends ValueExpTypeInfo> VT getEvaluatesTo();

	/**
	 * Evaluates this expression
	 *
	 * @param context an evaluation context
	 * @return {@link Expression} an expression
	 * representing evaluation result, usually evaluation result
	 * is an defaultProvider {@link ValueExp} but in some cases
	 * expression evaluates to itself
	 * @throws EvaluationException if an evaluation error
	 * occurs
	 */
	<T extends Expression> T evaluate(
			EvaluationContext context) throws EvaluationException;

	/**
	 * Accepts {@link ExpressionVisitor} implementation
	 *
	 * @param v expression visitor
	 * @exception ClassCastException if given implementation
	 * is not supported by this node
	 */
	void accept(ExpressionVisitor v);
}
