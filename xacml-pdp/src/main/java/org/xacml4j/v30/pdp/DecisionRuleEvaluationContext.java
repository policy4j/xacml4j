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

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;

/**
 * Specialization of {@link org.xacml4j.v30.EvaluationContext}, that is
 * used to evaluate policy decisions.
 *
 * @author Valdas Sevelis
 */
public interface DecisionRuleEvaluationContext extends EvaluationContext {
	/**
	 * Adds evaluated policy or policy set
	 * evaluation result to the context
	 *
	 * @param policy an evaluated policy or policy set
	 * @param result a policy or policy set evaluation result
	 */
	void addEvaluationResult(CompositeDecisionRule policy, Decision result);

	@Override
	DecisionRuleEvaluationContext getParentContext();

	@Override
	DecisionRuleEvaluationContext createExtIndeterminateEvalContext();

	/**
	 * Gets currently evaluated policy.
	 * If invocation returns
	 * {@code null}, {@link #getCurrentPolicySet()}
	 * will return NOT {@code null} references
	 * to the currently evaluated policy set
	 *
	 *
	 * @return {@link CompositeDecisionRule} or {@code null}
	 */
	CompositeDecisionRule getCurrentPolicy();

	/**
	 * Gets currently evaluated policy set
	 *
	 * @return {@link CompositeDecisionRule} or {@code null}
	 */
	CompositeDecisionRule getCurrentPolicySet();

	/**
	 * Gets current rule
	 *
	 * @return {@link org.xacml4j.v30.DecisionRule} or {@code null}
	 */
	DecisionRule getCurrentRule();

	/**
	 * Gets current {@link CompositeDecisionRuleIDReference}
	 *
	 * @return current {@link CompositeDecisionRuleIDReference} or
	 * {@code null}
	 */
	CompositeDecisionRuleIDReference getCurrentPolicyIDReference();

	/**
	 * Gets currently evaluated {@link CompositeDecisionRuleIDReference}
	 *
	 * @return {@link CompositeDecisionRuleIDReference} or {@code null}
	 */
	CompositeDecisionRuleIDReference getCurrentPolicySetIDReference();
	/**
	 * Resolves given {@link org.xacml4j.v30.CompositeDecisionRuleIDReference}
	 * references
	 *
	 * @param ref a policy references
	 * @return resolved {@link org.xacml4j.v30.CompositeDecisionRule} instance
	 * @throws PolicyResolutionException if
	 * policy references can not be resolved
	 */
	CompositeDecisionRule resolve(CompositeDecisionRuleIDReference ref)
			throws PolicyResolutionException;

}
