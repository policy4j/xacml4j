package org.xacml4j.v30.policy.combine;

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

import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.policy.TargetBeforeContext;

import com.google.common.collect.ImmutableList;

/**
 * An utility class for evaluating {@link DecisionRule}
 *
 * @author Giedrius Trumpickas
 */
public final class DecisionCombiningAlgorithms
{
	private DecisionCombiningAlgorithms(){
	}

	/**
	 * Tests whether a list supports indexed traversal without changing its iterator semantics.
	 * Guava immutable lists provide constant-time indexed access; other lists keep their
	 * iterators to preserve snapshot, fail-fast, and lazy traversal behavior.
	 *
	 * @param values the list to traverse
	 * @return {@code true} for Guava immutable lists
	 */
	public static boolean supportsIndexedTraversal(List<?> values){
		return values instanceof ImmutableList<?>;
	}

	/**
	 * Evaluates a child in a context of its own, except that a child whose built-in target the
	 * request entity decides gets no context when it cannot match, and is not matched again when it
	 * does (design D41).
	 *
	 * @param <D> {@link DecisionRule} subclass
	 * @param context a parent evaluation context
	 * @param decision a decision rule to be evaluated
	 * @return evaluation result as {@link Decision} defaultProvider
	 */
	public static <D extends DecisionRule> Decision evaluateIfMatch(EvaluationContext context, D decision) {
		return TargetBeforeContext.evaluateIfMatch(context, decision);
	}

	/**
	 * Tells whether a child's target cannot match, for an algorithm that matches every child before
	 * it evaluates one: such a child answers its match call from the parent context and is skipped,
	 * and a matching child answers its next match call, in the context the algorithm creates.
	 *
	 * @param <D> {@link DecisionRule} subclass
	 * @param context a parent evaluation context
	 * @param decision a decision rule about to be matched
	 * @return {@code true} when the child cannot match and is skipped
	 */
	public static <D extends DecisionRule> boolean isDefinitelyNoMatch(EvaluationContext context, D decision) {
		return TargetBeforeContext.isDefinitelyNoMatch(context, decision);
	}

	/**
	 * A helper method which invokes {@link DecisionRule#createContext(EvaluationContext)}
	 * then sub-sequentially invokes {@link DecisionRule#evaluate(EvaluationContext)}
	 * with the just created {@link EvaluationContext} defaultProvider as an argument
	 *
	 * @param <D> {@link DecisionRule} subclass
	 * @param context a parent evaluation context
	 * @param decision a decision rule to be evaluated
	 * @return evaluation result as {@link Decision} defaultProvider
	 */
	public static <D extends DecisionRule> Decision evaluate(EvaluationContext context, D decision) {
		EvaluationContext decisionContext = decision.createContext(context);
		return decision.evaluate(decisionContext);
	}
}
