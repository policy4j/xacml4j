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
import org.xacml4j.v30.spi.observe.DecisionObservation;

final class PinnedReference implements CompositeDecisionRule
{
	private final CompositeDecisionRuleIDReference ref;
	private final CompositeDecisionRule target;

	PinnedReference(CompositeDecisionRuleIDReference ref, CompositeDecisionRule target){
		this.ref = ref;
		this.target = target;
	}

	CompositeDecisionRule target(){ return target; }

	boolean canCheckTarget(EvaluationContext context){
		if(missed(context)) return false;
		if(ref.getClass() == PolicyIDReference.class) return ((PolicyIDReference)ref).canCheckTarget(context, target);
		if(ref.getClass() == PolicySetIDReference.class) return ((PolicySetIDReference)ref).canCheckTarget(context, target);
		return false;
	}

	@Override
	public String getId(){ return ref.getId(); }

	@Override
	public CompositeDecisionRuleIDReference getReference(){ return ref.getReference(); }

	@Override
	public EvaluationContext createContext(EvaluationContext context){
		if(missed(context)) return ref.createContext(context);
		if(ref instanceof PolicyIDReference policy) return policy.createContext(context, target);
		return ((PolicySetIDReference)ref).createContext(context, target);
	}

	/** Reported as the evaluation of the reference, whether the position is missed or not (design D34) */
	@Override
	public Decision evaluate(EvaluationContext context){
		if(failed(context)) return ref.evaluate(context);
		DecisionObservation o = RootEvaluationContext.observationOf(context);
		if(!o.isEnabled()) return evaluateTarget(context);
		Object parent = o.evaluationStarted(context, ref);
		Decision d;
		try{
			d = evaluateTarget(context);
		}catch(Throwable t){
			o.evaluationFailed(context, ref, parent, t);
			throw t;
		}
		o.evaluationFinished(context, ref, parent, d);
		return d;
	}

	private Decision evaluateTarget(EvaluationContext context){
		setEvaluating(context, true);
		try{
			return target.evaluate(context);
		}finally{
			setEvaluating(context, false);
		}
	}

	private void setEvaluating(EvaluationContext context, boolean evaluating){
		if(ref instanceof PolicyIDReference policy) policy.setEvaluating(context, evaluating);
		else ((PolicySetIDReference)ref).setEvaluating(context, evaluating);
	}

	@Override
	public MatchResult isMatch(EvaluationContext context){
		return failed(context) ? ref.isMatch(context) : target.isMatch(context);
	}

	private boolean failed(EvaluationContext context){
		return missed(context) || (ref instanceof PolicyIDReference policy && policy.isFailed(context))
				|| (ref instanceof PolicySetIDReference set && set.isCyclic(context));
	}

	private boolean missed(EvaluationContext context){
		EvaluationContext top = DescendantEvaluationContext.topOf(context);
		return top instanceof RootEvaluationContext root && root.isMissed(ref);
	}

	@Override
	public void accept(PolicyTreeVisitor visitor){ ref.accept(visitor); }
}
