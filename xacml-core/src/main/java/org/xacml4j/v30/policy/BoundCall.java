package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExp;

/**
 * A call of a two-argument function whose first argument, a literal, is bound at the call site
 * (change add-regex-engine-spi, design D6): the function prepares the literal once, on the first
 * invocation, and every invocation of the call site reuses it. A {@link Match} and an
 * {@link Apply} with a literal first argument hold one from {@link FunctionSpec#bind}.
 */
public interface BoundCall
{
	/**
	 * Invokes the function with the bound literal as its first argument
	 *
	 * @param <T> value expression type
	 * @param context evaluation context
	 * @param argument the second argument
	 * @return function invocation value
	 * @throws EvaluationException if the function fails, with the status of the failure
	 */
	<T extends ValueExp> T invoke(EvaluationContext context, Expression argument) throws EvaluationException;
}
