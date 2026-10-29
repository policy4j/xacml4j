package org.xacml4j.v30.policy;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;

public final class IndexCustomChild extends BaseDecisionRule implements CompositeDecisionRule
{
	public IndexCustomChild(Target target){ super(new CustomBuilder(target)); }
	@Override public EvaluationContext createContext(EvaluationContext context){ return context; }
	@Override public MatchResult isMatch(EvaluationContext context){ return MatchResult.MATCH; }
	@Override Decision evaluateRule(EvaluationContext context){ return Decision.PERMIT; }
	@Override public CompositeDecisionRuleIDReference getReference(){ return PolicyIDReference.builder("custom").build(); }
	@Override public void accept(PolicyTreeVisitor visitor){ }
	@Override protected boolean isEvaluationContextValid(EvaluationContext context){ return true; }

	private static final class CustomBuilder extends BaseDecisionRule.Builder<CustomBuilder>
	{
		CustomBuilder(Target target){ super("custom"); target(target); }
		@Override protected CustomBuilder getThis(){ return this; }
	}
}
