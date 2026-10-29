package org.xacml4j.v30.policy.function.regex;

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
 * A pattern compiled once by a {@link RegexEngine} (change add-regex-engine-spi, design D1). It
 * is immutable and safe to share between threads and call sites; the state of a match stays
 * inside the call.
 */
public interface CompiledRegex
{
	/**
	 * Searches the input from its start for any substring that matches the pattern, as XF 7.6.2
	 * {@code fn:matches} does. The engine checks the budget while it reads the input and after
	 * the search, so that a result that comes after the deadline is not used.
	 *
	 * @param input the input of the match
	 * @param budget the time of this call
	 * @return whether a substring of the input matches
	 * @throws RegexLimitException when the input is longer than the limit of the engine, the
	 * deadline of the budget passed, or the engine threw an {@link Error} while matching
	 */
	boolean find(CharSequence input, RegexBudget budget);
}
