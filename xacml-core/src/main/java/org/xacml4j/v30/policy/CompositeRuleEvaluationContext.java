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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Obligation;

/**
 * The context of one composite decision rule. While the rule stages its responses in the root of
 * the decision (XACML 3.0, section 7.18, design D36), the responses that pass through this context
 * on their way up, from the rule's children, its combining algorithm and its own expressions, go
 * into the frame of the rule; a response written to the root directly, as by an attribute
 * resolver, never does (review R-AN, Codex Sol). The root records the contexts that stage, the
 * innermost and those it is in, as each opens its staging, so this context holds no state of its
 * own (review R-AN2).
 */
abstract class CompositeRuleEvaluationContext extends DescendantEvaluationContext
{
	protected CompositeRuleEvaluationContext(EvaluationContext context){
		super(context);
	}

	/** Whether the rule of this context stages its responses in the root now */
	final boolean isStaging(){
		return topOf(this) instanceof RootEvaluationContext root && root.isStaging(this);
	}

	/**
	 * Starts staging the responses that pass through this context, which is then the innermost
	 * staging one; false when the root cannot record it, and nothing changed
	 */
	final boolean openStaging(){
		return stagingRoot().openStaging(this);
	}

	/** Ends the staging; the staging context recorded before this one, if any, is the innermost again. A second call does nothing */
	final void closeStaging(){
		stagingRoot().closeStaging(this);
	}

	/** The root of the decision, which this context stages in */
	final RootEvaluationContext stagingRoot(){
		return (RootEvaluationContext)topOf(this);
	}

	@Override
	public void addAdvices(Decision d, Iterable<Advice> advices){
		if(isStaging()){
			stagingRoot().stageAdvices(this, d, advices);
			return;
		}
		super.addAdvices(d, advices);
	}

	@Override
	public void addObligations(Decision d, Iterable<Obligation> obligations){
		if(isStaging()){
			stagingRoot().stageObligations(this, d, obligations);
			return;
		}
		super.addObligations(d, obligations);
	}
}
