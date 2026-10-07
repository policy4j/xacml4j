package org.xacml4j.v30.policy.function.impl;

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
import java.util.function.Predicate;

/**
 * Combines boolean operands that are evaluated one by one, from first to
 * last, and whose evaluation can fail (Indeterminate).
 * <p>
 * XACML 3.0 A.3.5: {@code or} is "True" if at least one argument evaluates
 * to "True", and {@code and} is "False" if one argument evaluates to
 * "False". So a deciding operand wins over an operand that failed before
 * it. When no operand decides and at least one failed, the first failure is
 * thrown again and the result is Indeterminate with the status of that
 * failure. The higher-order bag functions (A.3.12) combine the results of
 * their predicate with these two functions.
 */
final class DecidingOperands
{
	private DecidingOperands() {}

	/**
	 * The {@code or} combination.
	 *
	 * @param operands operands in evaluation order
	 * @param value evaluates one operand; it can throw
	 * @return {@code true} at the first operand that is true; {@code false}
	 * when every operand is false
	 * @throws RuntimeException the first failure, when no operand is true
	 * and at least one failed
	 */
	static <V> boolean anyTrue(Iterable<? extends V> operands, Predicate<? super V> value)
	{
		return firstDeciding(operands, value, true);
	}

	/**
	 * The {@code and} combination.
	 *
	 * @param operands operands in evaluation order
	 * @param value evaluates one operand; it can throw
	 * @return {@code false} at the first operand that is false; {@code true}
	 * when every operand is true
	 * @throws RuntimeException the first failure, when no operand is false
	 * and at least one failed
	 */
	static <V> boolean allTrue(Iterable<? extends V> operands, Predicate<? super V> value)
	{
		return firstDeciding(operands, value, false);
	}

	private static <V> boolean firstDeciding(Iterable<? extends V> operands,
			Predicate<? super V> value, boolean deciding)
	{
		RuntimeException failure = null;
		for(V operand : operands){
			try{
				if(value.test(operand) == deciding){
					return deciding;
				}
			}catch(RuntimeException e){
				if(failure == null){
					failure = e;
				}
			}
		}
		if(failure != null){
			throw failure;
		}
		return !deciding;
	}

	/**
	 * The {@code n-of} combination.
	 * Evaluation stops with {@code true} when {@code n} operands are true.
	 * It stops with {@code false} when {@code n} can not be reached even if
	 * every failed operand and every operand not yet evaluated were true.
	 *
	 * @param n the number of operands that must be true
	 * @param operands operands in evaluation order
	 * @param value evaluates one operand; it can throw
	 * @return whether at least {@code n} operands are true
	 * @throws RuntimeException the first failure, when the operands that
	 * are true and the operands that failed are together at least {@code n}
	 */
	static <V> boolean atLeast(int n, List<? extends V> operands, Predicate<? super V> value)
	{
		int trueCount = 0;
		int failureCount = 0;
		RuntimeException failure = null;
		for(int i = 0; i < operands.size(); i++){
			int notEvaluated = operands.size() - i;
			if(trueCount + failureCount + notEvaluated < n){
				return false;
			}
			try{
				if(value.test(operands.get(i)) && ++trueCount == n){
					return true;
				}
			}catch(RuntimeException e){
				failureCount++;
				if(failure == null){
					failure = e;
				}
			}
		}
		if(failure != null && trueCount + failureCount >= n){
			throw failure;
		}
		return false;
	}
}
