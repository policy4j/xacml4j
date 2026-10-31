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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandlerChain;

import com.google.common.collect.ImmutableList;

public class MultipleResourcesHandler extends RequestContextHandlerChain
{
	private final static Logger log = LoggerFactory.getLogger(MultipleResourcesHandler.class);

	public MultipleResourcesHandler()
	{
		this(MultipleResourcesViaXPathExpressionHandler.DEFAULT_MAX_PATHS_PER_CATEGORY,
				MultipleResourcesViaXPathExpressionHandler.DEFAULT_MAX_EXPANDED_REQUESTS);
	}

	/**
	 * Creates the handler chain with limits for the expansion of requests
	 * with request references, with repeated categories and with
	 * {@code multiple:content-selector} attributes. Beyond a limit the
	 * request gets one Indeterminate result with status processing-error.
	 * <p>
	 * At most {@code maxExpandedRequests} individual requests are evaluated
	 * for one request, whatever combination of the three it uses, and its
	 * response has at most {@code maxExpandedRequests} results. Each
	 * step checks its own count before it builds its individual requests:
	 * the number of request references, the product of the repeated
	 * categories, the product of the selected nodes. What the steps give
	 * together is counted while the request is handled: the individual
	 * requests that reach the selector step (also those that select no
	 * node), and the individual requests that are evaluated. A request
	 * that passes each step and is beyond the limit in total is rejected
	 * after at most {@code maxExpandedRequests} evaluations.
	 *
	 * @param maxPathsPerCategory the largest number of node paths that one
	 * selector may select in its category
	 * @param maxExpandedRequests the largest number of individual requests
	 * that are evaluated for one request, and of results in its response
	 */
	public MultipleResourcesHandler(int maxPathsPerCategory, int maxExpandedRequests)
	{
		super(new MultipleResourcesBudgetHandler(maxExpandedRequests),
				new MultipleResourcesViaRequestReferencesHandler(maxExpandedRequests),
				new MultipleResourcesViaRepeatingAttributesHandler(maxExpandedRequests),
				new MultipleResourcesBudgetHandler.Checkpoint(),
				new MultipleResourcesViaXPathExpressionLegacyHandler(),
				new MultipleResourcesViaXPathExpressionHandler(maxPathsPerCategory, maxExpandedRequests));
	}

	@Override
	protected Collection<Result> postProcessResults(
			RequestContext req,
			Collection<Result> results) {
		if(!req.isCombinedDecision()){
			return results;
		}
		if(results.size() == 1){
			return results;
		}
		Result prev = null;
		for(Result r : results)
		{
			Collection<Advice> advice = r.getAssociatedAdvice();
			if(!advice.isEmpty()){
				return ImmutableList.of(
						Result.indeterminate(
								Status.processingError().build())
						.build());
			}
			Collection<Obligation> obligation = r.getObligations();
			if(!obligation.isEmpty()){
				return ImmutableList.of(
						Result.indeterminate(
								Status.processingError().build())
						.build());
			}
			if(prev != null){
				if(r.isIndeterminate()){
					if(!prev.isIndeterminate()){
						return ImmutableList.of(
								Result.indeterminate(
										Status.processingError().build())
								.build());
					}
				}
				if(!r.getDecision().equals(prev.getDecision())){
					if(log.isDebugEnabled()){
						log.debug("Previous decision result=\"{}\" " +
								"can not be combined with the current result=\"{}\"", prev, r);
					}
					return ImmutableList.of(
							Result.indeterminate(
									Status.processingError().build())
							.build());
				}
			}
			prev = r;
		}
		if(prev == null){
			return ImmutableList.of(
					Result.indeterminate(
							Status.processingError().build())
					.build());
		}
		return ImmutableList.of(Result.ok(prev.getDecision()).build());
	}
}
