package org.xacml4j.v30.spi.combine;

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


import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;

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
	 * A helper method which invokes {@link DecisionRule#createContext(EvaluationContext)}
	 * then sub-sequentially invokes {@link DecisionRule#evaluate(org.xacml4j.v30.EvaluationContext)}
	 * with the just created {@link EvaluationContext} instance as an argument
	 *
	 * @param context a parent evaluation context
	 * @param decision a decision rule to be evaluated
	 * @return evaluation result as {@link Decision} instance
	 */
	public static <D extends DecisionRule> Decision evaluateIfMatch(EvaluationContext context, D decision) {
		EvaluationContext decisionContext = decision.createContext(context);
		return decision.evaluate(decisionContext);
	}

	/**
	 * A helper method which invokes {@link DecisionRule#createContext(EvaluationContext)}
	 * then sub-sequentially invokes {@link DecisionRule#evaluate(EvaluationContext)}
	 * with the just created {@link EvaluationContext} instance as an argument
	 *
	 * @param context a parent evaluation context
	 * @param decision a decision rule to be evaluated
	 * @return evaluation result as {@link Decision} instance
	 */
	public static <D extends DecisionRule> Decision evaluate(EvaluationContext context, D decision) {
		EvaluationContext decisionContext = decision.createContext(context);
		return decision.evaluate(decisionContext);
	}
}
