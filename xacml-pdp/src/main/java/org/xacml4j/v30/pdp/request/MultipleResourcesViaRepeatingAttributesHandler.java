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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.RequestContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

final class MultipleResourcesViaRepeatingAttributesHandler extends AbstractRequestContextHandler
{
	private final static String FEATURE_ID = "urn:oasis:names:tc:xacml:3.0:profile:multiple:repeated-attribute-categories";

	private final static Logger LOG = LoggerFactory.getLogger(MultipleResourcesViaRepeatingAttributesHandler.class);

	private final int maxExpandedRequests;

	public MultipleResourcesViaRepeatingAttributesHandler(){
		this(MultipleResourcesViaXPathExpressionHandler.DEFAULT_MAX_EXPANDED_REQUESTS);
	}

	/**
	 * @param maxExpandedRequests the largest number of individual requests
	 * that the repeated categories of one request may expand to
	 */
	MultipleResourcesViaRepeatingAttributesHandler(int maxExpandedRequests){
		super(FEATURE_ID);
		Preconditions.checkArgument(maxExpandedRequests > 0,
				"maxExpandedRequests=\"%s\" must be positive", maxExpandedRequests);
		this.maxExpandedRequests = maxExpandedRequests;
	}

	@Override
	public Collection<Result> handle(RequestContext request,
			PolicyDecisionPointContext context)
	{
		if(!request.containsRepeatingCategories()){
			return handleNext(request, context);
		}
		List<Set<Category>> byCategory = new LinkedList<>();
		// The number of individual requests is the product of the repeats. It is
		// checked after every category, before the product is built, so it
		// stays below maxExpandedRequests * Integer.MAX_VALUE and does not
		// overflow.
		long expandedRequests = 1;
		for(CategoryId categoryId : request.getCategoriesByCategoryId().keySet()){
			Collection<Category> attributes = request.getCategory(categoryId);
			if(attributes == null ||
					attributes.isEmpty()) {
				continue;
			}
			Set<Category> repeats = ImmutableSet.copyOf(attributes);
			expandedRequests *= repeats.size();
			if(expandedRequests > maxExpandedRequests){
				// The request is well-formed; the PDP declines to process it. The
				// status message is a fixed text that names no request data.
				LOG.warn("Multiple decision request rejected: repeated categories give " +
						"more than maxExpandedRequests=\"{}\" individual requests", maxExpandedRequests);
				return MultipleResourcesBudgetHandler.limitExceeded(request);
			}
			byCategory.add(repeats);
		}
		Collection<Result> results = new LinkedList<>();
		Set<List<Category>> cartesian = Sets.cartesianProduct(byCategory);
		for(List<Category> requestAttr : cartesian){
			results.addAll(handleNext(RequestContext
					.builder()
					.copyOf(request, requestAttr)
					.build(), context));
		}
		return results;
	}
}
