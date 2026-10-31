package org.xacml4j.v30.policy.function.impl;

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

import java.util.Optional;

import org.xacml4j.v30.policy.BoundCall;
import org.xacml4j.v30.types.Value;

/**
 * A function provider instance whose functions bind a literal first argument at its call site
 * (change add-regex-engine-spi, design D6), as the regexp-match functions compile a literal
 * pattern once. The binder gets the literal of each two-argument call site of one of its
 * functions when the policy is built, and prepares it on the first invocation of the call, not
 * before: the outcome of the preparation belongs to the evaluation. A provider that is a class
 * of static methods binds nothing. A bound call receives an already evaluated operand and may be
 * invoked concurrently.
 */
interface LiteralBinder
{
	/**
	 * Binds a literal first argument of a call of a function of this provider
	 *
	 * @param functionId the identifier of the function
	 * @param literal the literal first argument of the call
	 * @return the bound call, which is invoked with the evaluated second argument as the
	 * function's own method would be; or nothing when the function does not bind this literal,
	 * so that the call is invoked as any other
	 */
	Optional<BoundCall> bind(String functionId, Value<?> literal);
}
