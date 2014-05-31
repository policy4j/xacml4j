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

import org.xacml4j.v30.pdp.PolicyElement;

/**
 * A XACML decision rule, core interface
 * for all rules in the XACML specification
 *
 * @author Giedrius Trumpickas
 */
public interface DecisionRule extends PolicyElement
{
	/**
	 * Gets decision rule identifier
	 *
	 * @return decision rule identifier
	 */
	String getId();

	/**
	 * Creates an evaluation context to match
	 * or evaluate this decision rule
	 *
	 * @param context a parent evaluation context
	 * @return {@link EvaluationContext} an evaluation
	 * context to be used to match or evaluate this decision
	 */
	EvaluationContext createContext(EvaluationContext context);

	/**
	 * Tests if this decision rule is applicable to a given
	 * evaluation context. An evaluation context
	 * must be created by invoking
	 * {@link DecisionRule#createContext(EvaluationContext)} first
	 *
	 * @param context an evaluation context
	 * @return {@link MatchResult} indicating applicability
	 * of this decision rule to the given evaluation context
	 */
	MatchResult isMatch(EvaluationContext context);

	/**
	 * Evaluates this decision in the given evaluation context.
	 * An evaluation context must be created by invoking
	 * {@link DecisionRule#createContext(EvaluationContext)} first.
	 * Evaluation should be performed if prior call
	 * to {@link DecisionRule#isMatch(EvaluationContext)} returns
	 * {@link MatchResult#MATCH}
	 *
	 * @param context an evaluation context
	 * @return {@link Decision}
	 */
	Decision evaluate(EvaluationContext context);
}
