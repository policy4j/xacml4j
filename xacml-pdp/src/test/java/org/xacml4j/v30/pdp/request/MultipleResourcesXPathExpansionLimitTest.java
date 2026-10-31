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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Maintainer ruling 2026-10-07 (run 061 D16, bead policy4j-the.4.50): the
 * expansion of a request with {@code multiple:content-selector} attributes
 * has a limit on the node paths per category and on the number of
 * individual requests. Beyond a limit the result is one Indeterminate with
 * status processing-error, and no individual request is evaluated.
 */
public class MultipleResourcesXPathExpansionLimitTest
{
	private static final String SELECTOR = MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR;

	private PolicyDecisionPointContext pdp;

	@BeforeEach
	public void init()
	{
		this.pdp = mock(PolicyDecisionPointContext.class);
	}

	@Test
	public void defaultLimitsAreOneHundred()
	{
		assertEquals(100, MultipleResourcesViaXPathExpressionHandler.DEFAULT_MAX_PATHS_PER_CATEGORY);
		assertEquals(100, MultipleResourcesViaXPathExpressionHandler.DEFAULT_MAX_EXPANDED_REQUESTS);
	}

	@Test
	public void pathsUpToTheLimitAreExpanded()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		RequestContextHandler handler = new MultipleResourcesViaXPathExpressionHandler();

		Collection<Result> results = handler.handle(request(category(CategoryId.RESOURCE, 100)), pdp);

		assertEquals(100, results.size());
		verify(pdp, times(100)).requestDecision(Mockito.any());
	}

	@Test
	public void morePathsThanTheLimitInOneCategoryAreIndeterminate()
	{
		RequestContextHandler handler = new MultipleResourcesViaXPathExpressionHandler();

		Collection<Result> results = handler.handle(request(category(CategoryId.RESOURCE, 101)), pdp);

		assertLimitExceeded(results);
		verifyNoInteractions(pdp);
	}

	@Test
	public void productUpToTheLimitIsExpanded()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		RequestContextHandler handler = new MultipleResourcesViaXPathExpressionHandler();

		Collection<Result> results = handler.handle(
				request(category(CategoryId.RESOURCE, 10), category(CategoryId.ACTION, 10)), pdp);

		assertEquals(100, results.size());
		verify(pdp, times(100)).requestDecision(Mockito.any());
	}

	@Test
	public void productAboveTheLimitIsIndeterminate()
	{
		RequestContextHandler handler = new MultipleResourcesViaXPathExpressionHandler();

		// 11 x 11 = 121 individual requests; each category is below the limit of paths.
		Collection<Result> results = handler.handle(
				request(category(CategoryId.RESOURCE, 11), category(CategoryId.ACTION, 11)), pdp);

		assertLimitExceeded(results);
		verifyNoInteractions(pdp);
	}

	@Test
	public void productOfManyCategoriesDoesNotOverflow()
	{
		RequestContextHandler handler = new MultipleResourcesViaXPathExpressionHandler(100, Integer.MAX_VALUE);

		// 100^5 = 10^10 is above Integer.MAX_VALUE; the count must not wrap around to a small number.
		Collection<Result> results = handler.handle(request(
				category(CategoryId.RESOURCE, 100), category(CategoryId.ACTION, 100),
				category(CategoryId.ENVIRONMENT, 100), category(CategoryId.SUBJECT_ACCESS, 100),
				category(CategoryId.SUBJECT_RECIPIENT, 100)), pdp);

		assertLimitExceeded(results);
		verifyNoInteractions(pdp);
	}

	/**
	 * rv085 round 1: a selector that selects no node gives no individual
	 * request. That is not a limit violation, in either order of the
	 * categories, and it does not fail when the other products are large.
	 */
	@Test
	public void emptySelectionIsNotALimitViolation()
	{
		RequestContextHandler handler = new MultipleResourcesViaXPathExpressionHandler();

		assertEquals(0, handler.handle(request(category(CategoryId.RESOURCE, 10),
				category(CategoryId.ACTION, 11), category(CategoryId.ENVIRONMENT, 0)), pdp).size());
		assertEquals(0, handler.handle(request(category(CategoryId.ENVIRONMENT, 0),
				category(CategoryId.RESOURCE, 10), category(CategoryId.ACTION, 11)), pdp).size());
		// 100^5 with one empty category: no individual request and no exception.
		assertEquals(0, handler.handle(request(
				category(CategoryId.RESOURCE, 100), category(CategoryId.ACTION, 100),
				category(CategoryId.ENVIRONMENT, 100), category(CategoryId.SUBJECT_ACCESS, 100),
				category(CategoryId.SUBJECT_RECIPIENT, 100), category(CategoryId.SUBJECT_INTERMEDIARY, 0)),
				pdp).size());
		verifyNoInteractions(pdp);
	}

	/** A selector above the path limit is rejected even when another selector selects nothing. */
	@Test
	public void pathLimitIsCheckedBeforeAnEmptySelectionEndsTheExpansion()
	{
		RequestContextHandler handler = new MultipleResourcesViaXPathExpressionHandler();

		assertLimitExceeded(handler.handle(request(category(CategoryId.ENVIRONMENT, 0),
				category(CategoryId.RESOURCE, 101)), pdp));
		verifyNoInteractions(pdp);
	}

	@Test
	public void limitsAreConfigurable()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

		assertEquals(101, new MultipleResourcesViaXPathExpressionHandler(200, 200)
				.handle(request(category(CategoryId.RESOURCE, 101)), pdp).size());
		assertLimitExceeded(new MultipleResourcesViaXPathExpressionHandler(2, 100)
				.handle(request(category(CategoryId.RESOURCE, 3)), pdp));
		assertLimitExceeded(new MultipleResourcesViaXPathExpressionHandler(100, 2)
				.handle(request(category(CategoryId.RESOURCE, 3)), pdp));
		verify(pdp, times(101)).requestDecision(Mockito.any());
	}

	@Test
	public void limitsMustBePositive()
	{
		assertThrows(IllegalArgumentException.class, () -> new MultipleResourcesViaXPathExpressionHandler(0, 100));
		assertThrows(IllegalArgumentException.class, () -> new MultipleResourcesViaXPathExpressionHandler(100, 0));
		assertThrows(IllegalArgumentException.class, () -> new MultipleResourcesHandler(0, 100));
	}

	@Test
	public void publicHandlerChainTakesTheLimits()
	{
		RequestContextHandler chain = new MultipleResourcesHandler(2, 100);

		assertLimitExceeded(chain.handle(request(category(CategoryId.RESOURCE, 3)), pdp));
		// rv085 round 2: the second argument reaches the handler too.
		assertLimitExceeded(new MultipleResourcesHandler(100, 2)
				.handle(request(category(CategoryId.RESOURCE, 3)), pdp));
		verifyNoInteractions(pdp);
	}

	/** rv086: the chain that existing callers create has the default limits. */
	@Test
	public void defaultHandlerChainHasTheDefaultLimits()
	{
		assertLimitExceeded(new MultipleResourcesHandler()
				.handle(request(category(CategoryId.RESOURCE, 101)), pdp));
		verifyNoInteractions(pdp);
	}

	@Test
	public void indeterminateResultKeepsTheIncludeInResultAttributes()
	{
		Attribute included = Attribute.builder("urn:test:included").includeInResult(true)
				.value(XacmlTypes.STRING.ofAny("kept")).build();
		Category resource = Category.builder(CategoryId.RESOURCE)
				.entity(Entity.builder().content(content(101))
						.attribute(included, selector(CategoryId.RESOURCE)).build())
				.build();
		RequestContext request = RequestContext.builder().attributes(resource).build();

		Collection<Result> results = new MultipleResourcesViaXPathExpressionHandler().handle(request, pdp);

		assertLimitExceeded(results);
		assertEquals(1, request.getIncludeInResultAttributes().size());
		assertEquals(new java.util.ArrayList<>(request.getIncludeInResultAttributes()),
				new java.util.ArrayList<>(results.iterator().next().getIncludeInResultAttributes()));
	}

	/** One Indeterminate with processing-error and the fixed message, which names no request data. */
	private static void assertLimitExceeded(Collection<Result> results)
	{
		assertEquals(1, results.size());
		Result result = results.iterator().next();
		assertEquals(Decision.INDETERMINATE, result.getDecision());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(MultipleResourcesViaXPathExpressionHandler.LIMIT_EXCEEDED_MESSAGE),
				result.getStatus().getMessage());
	}

	private static RequestContext request(Category... categories)
	{
		return RequestContext.builder().attributes(categories).build();
	}

	/** A category whose selector matches {@code nodes} nodes of its content. */
	private static Category category(CategoryId id, int nodes)
	{
		return Category.builder(id)
				.entity(Entity.builder().content(content(nodes)).attribute(selector(id)).build())
				.build();
	}

	private static Attribute selector(CategoryId id)
	{
		return Attribute.builder(SELECTOR)
				.value(XacmlTypes.XPATH.ofAny("/record/item", id))
				.build();
	}

	private static XmlContent content(int nodes)
	{
		StringBuilder xml = new StringBuilder("<record>");
		for (int i = 0; i < nodes; i++) {
			xml.append("<item>").append(i).append("</item>");
		}
		return XmlContent.of(xml.append("</record>").toString());
	}
}
