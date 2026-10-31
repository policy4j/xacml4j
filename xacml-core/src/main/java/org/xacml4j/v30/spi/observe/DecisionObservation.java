package org.xacml4j.v30.spi.observe;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.policy.ConditionResult;
import org.xacml4j.v30.policy.FunctionSpec;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * The engine's side of a {@link DecisionObserver}: the observation of one decision (design D34). Observers
 * do not use it.
 * <p>
 * A start returns the token of the parent, which the caller keeps on its stack and gives back to the matching
 * end; the end makes the parent the innermost open token again before it calls the observer. Only the
 * deciding thread uses an observation, and one observation is the only object that an observed decision
 * allocates for its observer.
 * <p>
 * Starts and point events are made only while the observation {@link #isEnabled() is enabled}; ends are made
 * for every span that started. An observer exception is kept, the first one, and disables the observation.
 * The failure callbacks and {@link #functionFailed} add their exception to the failure that propagates, as
 * suppressed, and do not throw; the other callbacks throw it.
 */
public final class DecisionObservation
{
	private static final DecisionObservation NONE = new DecisionObservation(NoObserver.INSTANCE, NoObserver.INSTANCE, null);

	private final DecisionObserver<Object> observer;
	/** The deciding thread; none for {@link #none()} */
	private final Thread thread;
	/** The innermost open token, first the token of the decision */
	private Object current;
	private Throwable observerFailure;
	/** The source named for the attribute resolution that ends next */
	private AttributeSource source;

	private DecisionObservation(DecisionObserver<Object> observer, Object decision, Thread thread){
		this.observer = observer;
		this.current = decision;
		this.thread = thread;
	}

	/**
	 * The observation of a decision that is not observed: it is never enabled
	 *
	 * @return the observation that observes nothing
	 */
	public static DecisionObservation none(){
		return NONE;
	}

	/**
	 * Starts the observation of a decision on the calling thread with {@link DecisionObserver#decisionStarted}
	 *
	 * @param observer the observer of the decision point
	 * @param request the token of the request
	 * @param individualRequest the individual request
	 * @param correlation the correlation sequence of the decide call
	 * @param number the number of the decision in the decide call, from 1
	 * @return the observation of the decision
	 */
	public static <S> DecisionObservation start(DecisionObserver<S> observer, S request,
			RequestContext individualRequest, long correlation, int number){
		// Safe: the observation gives the observer only tokens that the observer returned
		@SuppressWarnings("unchecked")
		DecisionObserver<Object> o = (DecisionObserver<Object>)observer;
		return new DecisionObservation(o, token(o.decisionStarted(request, individualRequest, correlation, number)),
				Thread.currentThread());
	}

	/**
	 * Tells whether starts and point events are made: on the deciding thread of an observed decision, before
	 * the first observer exception
	 */
	public boolean isEnabled(){
		return thread == Thread.currentThread() && observerFailure == null;
	}

	public void decisionCacheHit(Result result){
		if(isEnabled()){
			try{
				observer.decisionCacheHit(current, result);
			}catch(Throwable t){
				throw kept(t);
			}
		}
	}

	public void decisionFinished(Result result){
		try{
			observer.decisionFinished(current, result);
		}catch(Throwable t){
			throw kept(t);
		}
	}

	/**
	 * Reports the failure of the decision. A kept observer exception is added to it as suppressed, unless it is
	 * that failure or is there already.
	 */
	public void decisionFailed(Throwable failure){
		if(observerFailure != null && observerFailure != failure
				&& !Arrays.asList(failure.getSuppressed()).contains(observerFailure)){
			failure.addSuppressed(observerFailure);
		}
		try{
			observer.decisionFailed(current, failure);
		}catch(Throwable t){
			suppressed(failure, t);
		}
	}

	/** Throws the kept observer exception, if there is one; the decision point calls it after the root evaluation */
	public void throwObserverFailure(){
		if(observerFailure != null){
			Throwables.throwIfUnchecked(observerFailure);
			throw new IllegalStateException(observerFailure);
		}
	}

	public Object evaluationStarted(EvaluationContext context, DecisionRule rule){
		Object parent = current;
		try{
			current = token(observer.evaluationStarted(parent, context, rule));
		}catch(Throwable t){
			throw kept(t);
		}
		return parent;
	}

	public void evaluationFinished(EvaluationContext context, DecisionRule rule, Object parent, Decision decision){
		Object evaluation = end(parent);
		try{
			observer.evaluationFinished(evaluation, context, rule, decision);
		}catch(Throwable t){
			throw kept(t);
		}
	}

	public void evaluationFailed(EvaluationContext context, DecisionRule rule, Object parent, Throwable failure){
		Object evaluation = end(parent);
		try{
			observer.evaluationFailed(evaluation, context, rule, failure);
		}catch(Throwable t){
			suppressed(failure, t);
		}
	}

	public Object extendedIndeterminateStarted(EvaluationContext context, CompositeDecisionRule rule){
		Object parent = current;
		try{
			current = token(observer.extendedIndeterminateStarted(parent, context, rule));
		}catch(Throwable t){
			throw kept(t);
		}
		return parent;
	}

	public void extendedIndeterminateFinished(EvaluationContext context, CompositeDecisionRule rule, Object parent,
			Decision decision){
		Object pass = end(parent);
		try{
			observer.extendedIndeterminateFinished(pass, context, rule, decision);
		}catch(Throwable t){
			throw kept(t);
		}
	}

	public void extendedIndeterminateFailed(EvaluationContext context, CompositeDecisionRule rule, Object parent,
			Throwable failure){
		Object pass = end(parent);
		try{
			observer.extendedIndeterminateFailed(pass, context, rule, failure);
		}catch(Throwable t){
			suppressed(failure, t);
		}
	}

	public Object attributeRequested(EvaluationContext context, AttributeReferenceKey key){
		Object parent = current;
		try{
			current = token(observer.attributeRequested(parent, context, key));
		}catch(Throwable t){
			throw kept(t);
		}
		return parent;
	}

	/**
	 * Names the source of the attribute resolution that ends next; each return path of the context handler
	 * names its source, after the resolutions nested in it have ended
	 */
	public void resolvedFrom(AttributeSource source){
		if(thread == Thread.currentThread()){
			this.source = source;
		}
	}

	public void attributeResolved(EvaluationContext context, AttributeReferenceKey key, Object parent,
			Optional<BagOfValues> value){
		Object attribute = end(parent);
		AttributeSource s = source;
		source = null;
		Preconditions.checkState(s != null, "No source for key=\"%s\"", key);
		try{
			observer.attributeResolved(attribute, context, key, s, value);
		}catch(Throwable t){
			throw kept(t);
		}
	}

	public void attributeFailed(EvaluationContext context, AttributeReferenceKey key, Object parent, Throwable failure){
		Object attribute = end(parent);
		source = null;
		try{
			observer.attributeFailed(attribute, context, key, failure);
		}catch(Throwable t){
			suppressed(failure, t);
		}
	}

	public Object resolverStarted(EvaluationContext context, String resolverId, AttributeReferenceKey key){
		Object parent = current;
		try{
			current = token(observer.resolverStarted(parent, context, resolverId, key));
		}catch(Throwable t){
			throw kept(t);
		}
		return parent;
	}

	public void resolverCacheHit(EvaluationContext context, String resolverId, AttributeReferenceKey key){
		if(isEnabled()){
			try{
				observer.resolverCacheHit(current, context, resolverId, key);
			}catch(Throwable t){
				throw kept(t);
			}
		}
	}

	public void resolverFinished(EvaluationContext context, String resolverId, AttributeReferenceKey key, Object parent,
			Optional<BagOfValues> value){
		Object resolver = end(parent);
		try{
			observer.resolverFinished(resolver, context, resolverId, key, value);
		}catch(Throwable t){
			throw kept(t);
		}
	}

	public void resolverFailed(EvaluationContext context, String resolverId, AttributeReferenceKey key, Object parent,
			Throwable failure){
		Object resolver = end(parent);
		try{
			observer.resolverFailed(resolver, context, resolverId, key, failure);
		}catch(Throwable t){
			suppressed(failure, t);
		}
	}

	public void targetMatched(EvaluationContext context, DecisionRule rule, MatchResult result){
		if(isEnabled()){
			try{
				observer.targetMatched(current, context, rule, result);
			}catch(Throwable t){
				throw kept(t);
			}
		}
	}

	public void conditionEvaluated(EvaluationContext context, DecisionRule rule, ConditionResult result){
		if(isEnabled()){
			try{
				observer.conditionEvaluated(current, context, rule, result);
			}catch(Throwable t){
				throw kept(t);
			}
		}
	}

	public void childrenSelected(EvaluationContext context, CompositeDecisionRule rule, List<? extends DecisionRule> selected){
		if(isEnabled()){
			try{
				observer.childrenSelected(current, context, rule, selected);
			}catch(Throwable t){
				throw kept(t);
			}
		}
	}

	/** Reports the failure of a function call, which the caller then throws */
	public void functionFailed(EvaluationContext context, FunctionSpec function, Throwable failure){
		if(isEnabled()){
			try{
				observer.functionFailed(current, context, function, failure);
			}catch(Throwable t){
				suppressed(failure, t);
			}
		}
	}

	public void responsesStaged(EvaluationContext context, DecisionRule rule, Decision decision, List<Advice> advice,
			List<Obligation> obligations){
		if(isEnabled()){
			try{
				observer.responsesStaged(current, context, rule, decision, advice, obligations);
			}catch(Throwable t){
				throw kept(t);
			}
		}
	}

	private static Object token(Object token){
		return Preconditions.checkNotNull(token, "token");
	}

	/** Ends the innermost span: its parent is the innermost open token again */
	private Object end(Object parent){
		Object span = current;
		current = parent;
		return span;
	}

	/** Keeps the first observer exception and throws the given one */
	private RuntimeException kept(Throwable t){
		if(observerFailure == null){
			observerFailure = t;
		}
		Throwables.throwIfUnchecked(t);
		throw new IllegalStateException(t);
	}

	/** Keeps the first observer exception and adds the given one to the failure that propagates */
	private void suppressed(Throwable failure, Throwable t){
		if(observerFailure == null){
			observerFailure = t;
		}
		if(t != failure){
			failure.addSuppressed(t);
		}
	}

	/** The observer of a decision point that has none */
	enum NoObserver implements DecisionObserver<Object>
	{
		INSTANCE;

		@Override
		public Object requestStarted(PolicyDecisionPoint pdp, RequestContext request){
			return INSTANCE;
		}
	}
}
