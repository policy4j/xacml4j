package org.xacml4j.v30.pdp.request;

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

import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.spi.audit.PolicyDecisionAuditor;
import org.xacml4j.v30.spi.observe.DecisionObservation;
import org.xacml4j.v30.spi.pdp.PolicyDecisionCache;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;

import com.google.common.base.Preconditions;

/**
 * The first handler of {@link MultipleResourcesHandler}. The other
 * handlers check their own step before they build it. This handler bounds
 * what the steps give together: request references, repeated categories
 * and content selectors multiply. For one request it counts
 * <ul>
 * <li>the individual requests that reach the content-selector step, after
 * the request references and the repeated categories (counted by the
 * {@link Checkpoint} in the chain). Such a request costs a selector
 * evaluation also when it selects no node and gives no result;</li>
 * <li>the individual requests that the last handler sends to the PDP;</li>
 * <li>the results of the response.</li>
 * </ul>
 * Each count has the same limit.
 * <p>
 * Beyond the limit the request gets one Indeterminate result. The
 * individual requests that were evaluated before the limit was reached
 * were evaluated in full (with their audit records and cache entries);
 * their results are not returned.
 */
final class MultipleResourcesBudgetHandler extends AbstractRequestContextHandler
{
	private final static Logger LOG = LoggerFactory.getLogger(MultipleResourcesBudgetHandler.class);

	private final int maxExpandedRequests;

	/**
	 * @param maxExpandedRequests the limit of each of the three counts of
	 * one request
	 */
	MultipleResourcesBudgetHandler(int maxExpandedRequests){
		Preconditions.checkArgument(maxExpandedRequests > 0,
				"maxExpandedRequests=\"%s\" must be positive", maxExpandedRequests);
		this.maxExpandedRequests = maxExpandedRequests;
	}

	@Override
	public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context)
	{
		try{
			Collection<Result> results = handleNext(request, new Budget(context, maxExpandedRequests));
			// An individual request that ends without an evaluation (a request
			// reference that does not resolve, a selector step beyond its own
			// limit) is a result too, and was not counted by the budget.
			if(results.size() > maxExpandedRequests){
				LOG.warn("Multiple decision request rejected: it gives " +
						"more than maxExpandedRequests=\"{}\" results", maxExpandedRequests);
				return limitExceeded(request);
			}
			return results;
		}catch(BudgetExceededException e){
			// The request is well-formed; the PDP declines to process it. The
			// results of the individual requests that were evaluated before
			// the limit are not returned. The status message is a fixed text
			// that names no request data.
			LOG.warn("Multiple decision request rejected: it expands to " +
					"more than maxExpandedRequests=\"{}\" individual requests", maxExpandedRequests);
			return limitExceeded(request);
		}
	}

	/**
	 * The result of a request that is beyond a limit of the expansion: one
	 * Indeterminate with status processing-error, the fixed message and
	 * the IncludeInResult attributes of the request.
	 */
	static Collection<Result> limitExceeded(RequestContext request)
	{
		return Collections.singleton(
				Result.indeterminate(Status.processingError()
						.message(MultipleResourcesViaXPathExpressionHandler.LIMIT_EXCEEDED_MESSAGE)
						.build())
				.includeInResultAttr(request.getIncludeInResultAttributes())
				.build());
	}

	/**
	 * Stands in the chain after the handlers of request references and of
	 * repeated categories, before the handlers of content selectors. It
	 * counts the individual requests that pass. Without the context of a
	 * {@link MultipleResourcesBudgetHandler} before it, it counts nothing.
	 */
	static final class Checkpoint extends AbstractRequestContextHandler
	{
		@Override
		public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context)
		{
			if(context instanceof Budget){
				((Budget)context).countSelectorStep();
			}
			return handleNext(request, context);
		}
	}

	/** One more individual request than the limit reached the selector step or the PDP. */
	@SuppressWarnings("serial")
	private static final class BudgetExceededException extends RuntimeException
	{
		BudgetExceededException(){
			super(null, null, false, false);
		}
	}

	/**
	 * The context of one request. It lets at most {@code limit} individual
	 * requests reach the selector step and passes at most {@code limit}
	 * individual requests on to the PDP. It is used by the thread that
	 * handles the request only.
	 */
	private static final class Budget implements PolicyDecisionPointContext
	{
		private final PolicyDecisionPointContext context;
		private int selectorStepsLeft;
		private int left;

		Budget(PolicyDecisionPointContext context, int limit){
			this.context = context;
			this.selectorStepsLeft = limit;
			this.left = limit;
		}

		void countSelectorStep(){
			if(selectorStepsLeft == 0){
				throw new BudgetExceededException();
			}
			selectorStepsLeft--;
		}

		@Override
		public Result requestDecision(RequestContext req){
			if(left == 0){
				throw new BudgetExceededException();
			}
			left--;
			return context.requestDecision(req);
		}

		@Override
		public String getCorrelationId(){
			return context.getCorrelationId();
		}

		@Override
		public long getCorrelation(){
			return context.getCorrelation();
		}

		@Override
		public EvaluationContext createEvaluationContext(RequestContext req){
			return context.createEvaluationContext(req);
		}

		@Override
		public EvaluationContext createEvaluationContext(RequestContext req, DecisionObservation observation){
			return context.createEvaluationContext(req, observation);
		}

		@Override
		public CompositeDecisionRule getDomainPolicy(){
			return context.getDomainPolicy();
		}

		@Override
		public PolicyDecisionCache getDecisionCache(){
			return context.getDecisionCache();
		}

		@Override
		public PolicyDecisionAuditor getDecisionAuditor(){
			return context.getDecisionAuditor();
		}

		/**
		 * The handler chain of the PDP. A caller that handles a request
		 * through it starts another budget; no code of this project does.
		 */
		@Override
		public RequestContextHandler getRequestHandlers(){
			return context.getRequestHandlers();
		}

		@Override
		public boolean isDecisionCacheEnabled(){
			return context.isDecisionCacheEnabled();
		}

		@Override
		public boolean isDecisionAuditEnabled(){
			return context.isDecisionAuditEnabled();
		}

		@Override
		public boolean isValidateFuncParamsAtRuntime(){
			return context.isValidateFuncParamsAtRuntime();
		}
	}
}
