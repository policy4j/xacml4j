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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.pdp.PolicyDecisionPointContext;
import org.xacml4j.v30.spi.pdp.RequestContextHandler;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Bead policy4j-the.4.57: a request with repeated attribute categories
 * (Multiple Decision Profile, section 3.3) expands to the product of the
 * repeats. The expansion has the same limit on the number of individual
 * requests as the expansion of {@code multiple:content-selector}
 * (ruling D16, bead policy4j-the.4.50). Beyond the limit the result is one
 * Indeterminate with status processing-error, and no individual request
 * is evaluated.
 * <p>
 * Request references, repeated categories and content selectors multiply.
 * {@link MultipleResourcesHandler} has one limit for what they give
 * together: at most that many individual requests are evaluated for one
 * request.
 */
public class MultipleResourcesRepeatedCategoriesLimitTest
{
	private PolicyDecisionPointContext pdp;

	@BeforeEach
	public void init()
	{
		this.pdp = mock(PolicyDecisionPointContext.class);
	}

	@Test
	public void repeatsUpToTheLimitAreExpanded()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		RequestContextHandler handler = new MultipleResourcesViaRepeatingAttributesHandler();

		Collection<Result> results = handler.handle(request(repeated(CategoryId.RESOURCE, 100)), pdp);

		assertEquals(100, results.size());
		verify(pdp, times(100)).requestDecision(Mockito.any());
	}

	@Test
	public void moreRepeatsThanTheLimitAreIndeterminate()
	{
		RequestContextHandler handler = new MultipleResourcesViaRepeatingAttributesHandler();

		Collection<Result> results = handler.handle(request(repeated(CategoryId.RESOURCE, 101)), pdp);

		assertLimitExceeded(results);
		verifyNoInteractions(pdp);
	}

	@Test
	public void productUpToTheLimitIsExpanded()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		RequestContextHandler handler = new MultipleResourcesViaRepeatingAttributesHandler();

		Collection<Result> results = handler.handle(
				request(repeated(CategoryId.RESOURCE, 10), repeated(CategoryId.ACTION, 10)), pdp);

		assertEquals(100, results.size());
		verify(pdp, times(100)).requestDecision(Mockito.any());
	}

	@Test
	public void productAboveTheLimitIsIndeterminate()
	{
		RequestContextHandler handler = new MultipleResourcesViaRepeatingAttributesHandler();

		// 11 x 11 = 121 individual requests.
		Collection<Result> results = handler.handle(
				request(repeated(CategoryId.RESOURCE, 11), repeated(CategoryId.ACTION, 11)), pdp);

		assertLimitExceeded(results);
		verifyNoInteractions(pdp);
	}

	/** A category that occurs once does not count; it is part of every individual request. */
	@Test
	public void categoryThatOccursOnceDoesNotCount()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		RequestContextHandler handler = new MultipleResourcesViaRepeatingAttributesHandler();

		Collection<Result> results = handler.handle(request(repeated(CategoryId.RESOURCE, 100),
				repeated(CategoryId.ACTION, 1), repeated(CategoryId.SUBJECT_ACCESS, 1)), pdp);

		assertEquals(100, results.size());
		verify(pdp, times(100)).requestDecision(Mockito.any());
	}

	@Test
	public void productOfManyCategoriesDoesNotOverflow()
	{
		RequestContextHandler handler = new MultipleResourcesViaRepeatingAttributesHandler(Integer.MAX_VALUE);

		// 100^5 = 10^10 is above Integer.MAX_VALUE: the count must not wrap around to a
		// small number, and the product must not be built (it can not be).
		Collection<Result> results = handler.handle(request(
				repeated(CategoryId.RESOURCE, 100), repeated(CategoryId.ACTION, 100),
				repeated(CategoryId.ENVIRONMENT, 100), repeated(CategoryId.SUBJECT_ACCESS, 100),
				repeated(CategoryId.SUBJECT_RECIPIENT, 100)), pdp);

		assertLimitExceeded(results);
		verifyNoInteractions(pdp);
	}

	@Test
	public void limitIsConfigurable()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

		assertEquals(101, new MultipleResourcesViaRepeatingAttributesHandler(200)
				.handle(request(repeated(CategoryId.RESOURCE, 101)), pdp).size());
		assertLimitExceeded(new MultipleResourcesViaRepeatingAttributesHandler(2)
				.handle(request(repeated(CategoryId.RESOURCE, 3)), pdp));
		verify(pdp, times(101)).requestDecision(Mockito.any());
	}

	@Test
	public void limitMustBePositive()
	{
		assertThrows(IllegalArgumentException.class, () -> new MultipleResourcesViaRepeatingAttributesHandler(0));
		assertThrows(IllegalArgumentException.class, () -> new MultipleResourcesViaRepeatingAttributesHandler(-1));
	}

	@Test
	public void publicHandlerChainPassesTheLimit()
	{
		assertLimitExceeded(new MultipleResourcesHandler(100, 2)
				.handle(request(repeated(CategoryId.RESOURCE, 3)), pdp));
		// The chain that existing callers create has the default limit.
		assertLimitExceeded(new MultipleResourcesHandler()
				.handle(request(repeated(CategoryId.RESOURCE, 101)), pdp));
		verifyNoInteractions(pdp);
	}

	/**
	 * Repeated categories that each carry a {@code multiple:content-selector}:
	 * the repeats are expanded first, then every individual request is
	 * expanded by its selector. The chain counts what the two steps give
	 * together.
	 */
	@Test
	public void repeatsTimesSelectorsHaveOneLimit()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		RequestContextHandler chain = new MultipleResourcesHandler(100, 6);

		// 2 repeats x 3 selected nodes: 6 individual requests, the limit.
		assertEquals(6, chain.handle(request(repeatedWithSelector(CategoryId.RESOURCE, 2, 3)), pdp).size());
		verify(pdp, times(6)).requestDecision(Mockito.any());

		// 2 repeats x 5 selected nodes: each step is inside the limit, 10 together
		// are not. The seventh individual request is not evaluated.
		assertLimitExceeded(chain.handle(request(repeatedWithSelector(CategoryId.RESOURCE, 2, 5)), pdp));
		verify(pdp, times(12)).requestDecision(Mockito.any());

		// 7 repeats: above the limit before any selector is read.
		assertLimitExceeded(chain.handle(request(repeatedWithSelector(CategoryId.RESOURCE, 7, 3)), pdp));
		verify(pdp, times(12)).requestDecision(Mockito.any());
	}

	/**
	 * A selector step that is beyond the limit for one of the individual
	 * requests of the repeats: that individual request is Indeterminate,
	 * and none is evaluated.
	 */
	@Test
	public void selectorStepBeyondTheLimitIsIndeterminateForItsRequest()
	{
		RequestContextHandler chain = new MultipleResourcesHandler(100, 4);

		Collection<Result> results = chain.handle(request(repeatedWithSelector(CategoryId.RESOURCE, 2, 5)), pdp);

		assertEquals(2, results.size());
		for (Result result : results) {
			assertLimitExceeded(List.of(result));
		}
		verifyNoInteractions(pdp);
	}

	/** The budget is one per request: the next request has the whole limit again. */
	@Test
	public void everyRequestHasTheWholeLimit()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		RequestContextHandler chain = new MultipleResourcesHandler(100, 6);

		for (int i = 1; i <= 3; i++) {
			assertEquals(6, chain.handle(request(repeatedWithSelector(CategoryId.RESOURCE, 2, 3)), pdp).size());
			verify(pdp, times(6 * i)).requestDecision(Mockito.any());
		}
	}

	/** A request without repeats, selectors or references is one individual request under any limit. */
	@Test
	public void singleRequestPassesTheSmallestLimit()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

		Collection<Result> results = new MultipleResourcesHandler(1, 1)
				.handle(request(repeated(CategoryId.RESOURCE, 1), repeated(CategoryId.ACTION, 1)), pdp);

		assertEquals(1, results.size());
		assertEquals(Decision.PERMIT, results.iterator().next().getDecision());
		verify(pdp, times(1)).requestDecision(Mockito.any());
	}

	@Test
	public void requestReferencesUpToTheLimitAreEvaluated()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

		Collection<Result> results = new MultipleResourcesHandler().handle(referencesToOneResourceEach(100), pdp);

		assertEquals(100, results.size());
		verify(pdp, times(100)).requestDecision(Mockito.any());
	}

	@Test
	public void moreRequestReferencesThanTheLimitAreIndeterminate()
	{
		Collection<Result> results = new MultipleResourcesHandler().handle(referencesToOneResourceEach(101), pdp);

		assertLimitExceeded(results);
		verifyNoInteractions(pdp);
	}

	/** A referenced request can have repeated categories. The chain counts both steps together. */
	@Test
	public void requestReferencesTimesRepeatsHaveOneLimit()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		RequestContextHandler chain = new MultipleResourcesHandler(100, 6);

		// 2 references x 3 repeats: 6 individual requests, the limit.
		assertEquals(6, chain.handle(referencesToRepeats(2, 3, NO_SELECTOR), pdp).size());
		verify(pdp, times(6)).requestDecision(Mockito.any());

		// 3 references x 3 repeats: each step is inside the limit, 9 together are not.
		assertLimitExceeded(chain.handle(referencesToRepeats(3, 3, NO_SELECTOR), pdp));
		verify(pdp, times(12)).requestDecision(Mockito.any());
	}

	/**
	 * Individual requests that end Indeterminate without an evaluation
	 * count too: 3 references, each to 2 repeats whose selector is beyond
	 * the limit, are 6 individual requests for a limit of 4.
	 */
	@Test
	public void individualRequestsThatAreNotEvaluatedAreCounted()
	{
		RequestContextHandler chain = new MultipleResourcesHandler(100, 4);

		// 2 references x 2 repeats: 4 results, each Indeterminate by the selector step.
		assertEquals(4, chain.handle(referencesToRepeats(2, 2, 5), pdp).size());
		// 3 references x 2 repeats: 6 individual requests.
		assertLimitExceeded(chain.handle(referencesToRepeats(3, 2, 5), pdp));
		verifyNoInteractions(pdp);
	}

	/**
	 * An individual request whose selector selects no node gives no result
	 * and no evaluation. The chain still has to build it and to evaluate
	 * its selector, so it counts: without that, a request could make the
	 * chain do limit x limit selector steps and answer with no result.
	 */
	@Test
	public void individualRequestsThatSelectNothingAreCounted()
	{
		RequestContextHandler chain = new MultipleResourcesHandler(100, 4);

		// 2 references x 2 repeats: 4 individual requests, none selects a node.
		assertEquals(0, chain.handle(referencesToRepeats(2, 2, 0), pdp).size());
		// 3 references x 2 repeats: 6 individual requests.
		assertLimitExceeded(chain.handle(referencesToRepeats(3, 2, 0), pdp));
		// 5 repeats, no references: above the limit before any selector is read.
		assertLimitExceeded(chain.handle(request(repeatedWithSelector(CategoryId.RESOURCE, 5, 0)), pdp));
		// 4 repeats, no references: 4 individual requests, none selects a node.
		assertEquals(0, chain.handle(request(repeatedWithSelector(CategoryId.RESOURCE, 4, 0)), pdp).size());
		verifyNoInteractions(pdp);
	}

	/**
	 * An individual request that ends without an evaluation is a result
	 * too. 4 repeats under a limit of 4: three have a selector that is
	 * beyond the limit, which gives one Indeterminate each, and one has a
	 * selector that selects 4 nodes. That is 7 results.
	 */
	@Test
	public void responseHasNoMoreResultsThanTheLimit()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		RequestContextHandler chain = new MultipleResourcesHandler(100, 4);

		Collection<Result> results = chain.handle(request(
				repeatedWithSelector(CategoryId.RESOURCE, 0, 3, 5),
				repeatedWithSelector(CategoryId.RESOURCE, 3, 1, 4)), pdp);

		assertLimitExceeded(results);
		verify(pdp, times(4)).requestDecision(Mockito.any());
	}

	/**
	 * A request reference that does not resolve gives one Indeterminate
	 * result without an evaluation. It is a result of the response.
	 */
	@Test
	public void referenceThatDoesNotResolveCountsAsAResult()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

		// 2 references x 2 repeats and 1 reference to a category that the request does not have.
		Collection<Result> results = new MultipleResourcesHandler(100, 5)
				.handle(referencesToRepeats(2, 2, NO_SELECTOR, 1), pdp);
		assertEquals(5, results.size());
		assertEquals(1, results.stream().filter(r -> r.getDecision() == Decision.INDETERMINATE).count());
		verify(pdp, times(4)).requestDecision(Mockito.any());

		assertLimitExceeded(new MultipleResourcesHandler(100, 4)
				.handle(referencesToRepeats(2, 2, NO_SELECTOR, 1), pdp));
		verify(pdp, times(8)).requestDecision(Mockito.any());
	}

	/**
	 * Equal {@code <Attributes>} elements of one category are one
	 * category, as before the limit. 101 of them are not beyond it.
	 */
	@Test
	public void equalRepeatsCountOnce()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		List<Category> categories = new ArrayList<>();
		for (int i = 0; i < 101; i++) {
			categories.addAll(repeated(CategoryId.RESOURCE, 1));
		}

		Collection<Result> results = new MultipleResourcesHandler().handle(request(categories), pdp);

		assertEquals(1, results.size());
		assertEquals(Decision.PERMIT, results.iterator().next().getDecision());
		verify(pdp, times(1)).requestDecision(Mockito.any());
	}

	/**
	 * A handler that an application appends to the chain gets the
	 * counting context. What it sends to the PDP is counted.
	 */
	@Test
	public void handlerAppendedToTheChainIsInsideTheLimit()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());
		MultipleResourcesHandler chain = new MultipleResourcesHandler(100, 3);
		chain.setNext(new AbstractRequestContextHandler("urn:test:evaluates-twice") {
			@Override
			public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context)
			{
				List<Result> results = new ArrayList<>(handleNext(request, context));
				results.addAll(handleNext(request, context));
				return results;
			}
		});

		// 1 individual request, evaluated twice by the appended handler.
		assertEquals(2, chain.handle(request(repeated(CategoryId.RESOURCE, 1)), pdp).size());
		verify(pdp, times(2)).requestDecision(Mockito.any());

		// 2 repeats, each evaluated twice: the fourth evaluation is beyond the limit of 3.
		assertLimitExceeded(chain.handle(request(repeated(CategoryId.RESOURCE, 2)), pdp));
		verify(pdp, times(5)).requestDecision(Mockito.any());
	}

	/** A request for a combined decision that is beyond the limit gets the same single result. */
	@Test
	public void combinedDecisionBeyondTheLimitIsTheOneIndeterminate()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

		// Beyond the limit of the repeats step: nothing is evaluated.
		assertLimitExceeded(new MultipleResourcesHandler().handle(RequestContext.builder()
				.combineDecision(true).attributes(repeated(CategoryId.RESOURCE, 101)).build(), pdp));
		verifyNoInteractions(pdp);

		// Beyond the limit in total: 2 repeats x 5 selected nodes for a limit of 6.
		assertLimitExceeded(new MultipleResourcesHandler(100, 6).handle(RequestContext.builder()
				.combineDecision(true).attributes(repeatedWithSelector(CategoryId.RESOURCE, 2, 5)).build(), pdp));
		verify(pdp, times(6)).requestDecision(Mockito.any());
	}

	@Test
	public void limitOfTheChainMustBePositive()
	{
		assertThrows(IllegalArgumentException.class, () -> new MultipleResourcesHandler(100, 0));
		assertThrows(IllegalArgumentException.class, () -> new MultipleResourcesBudgetHandler(0));
		assertThrows(IllegalArgumentException.class, () -> new MultipleResourcesViaRequestReferencesHandler(-1));
	}

	/** The budget handler implements no profile feature; the chain reports the features of the other handlers. */
	@Test
	public void budgetHandlerAddsNoFeature()
	{
		assertEquals(List.of(), List.copyOf(new MultipleResourcesBudgetHandler(1).getFeatures()));
		assertEquals(4, new MultipleResourcesHandler().getFeatures().size());
	}

	/** Below the limits every repeat and every selected node gives one individual request. */
	@Test
	public void repeatsWithSelectorsInsideTheLimitsAreAllExpanded()
	{
		when(pdp.requestDecision(Mockito.any())).thenReturn(Result.ok(Decision.PERMIT).build());

		assertEquals(15, new MultipleResourcesHandler(100, 100)
				.handle(request(repeatedWithSelector(CategoryId.RESOURCE, 5, 3)), pdp).size());
		verify(pdp, times(15)).requestDecision(Mockito.any());
	}

	@Test
	public void indeterminateResultKeepsTheIncludeInResultAttributes()
	{
		List<Category> categories = new ArrayList<>();
		for (int i = 0; i < 101; i++) {
			categories.add(Category.builder(CategoryId.RESOURCE)
					.entity(Entity.builder()
							.attribute(Attribute.builder("urn:test:id").includeInResult(i == 0)
									.value(XacmlTypes.STRING.ofAny("resource-" + i)).build())
							.build())
					.build());
		}
		RequestContext request = RequestContext.builder().attributes(categories).build();

		Collection<Result> results = new MultipleResourcesViaRepeatingAttributesHandler().handle(request, pdp);

		assertLimitExceeded(results);
		assertEquals(1, request.getIncludeInResultAttributes().size());
		assertEquals(new ArrayList<>(request.getIncludeInResultAttributes()),
				new ArrayList<>(results.iterator().next().getIncludeInResultAttributes()));
		verifyNoInteractions(pdp);
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

	@SafeVarargs
	private static RequestContext request(List<Category>... categories)
	{
		List<Category> all = new ArrayList<>();
		for (List<Category> c : categories) {
			all.addAll(c);
		}
		return RequestContext.builder().attributes(all).build();
	}

	/** {@code count} request references, each to its own resource category and to the one action category. */
	private static RequestContext referencesToOneResourceEach(int count)
	{
		List<Category> categories = new ArrayList<>();
		List<RequestReference> references = new ArrayList<>();
		categories.add(Category.builder(CategoryId.ACTION).id("action")
				.entity(Entity.builder()
						.attribute(Attribute.builder("urn:test:id")
								.value(XacmlTypes.STRING.ofAny("read")).build())
						.build())
				.build());
		for (int i = 0; i < count; i++) {
			categories.add(Category.builder(CategoryId.RESOURCE).id("resource-" + i)
					.entity(Entity.builder()
							.attribute(Attribute.builder("urn:test:id")
									.value(XacmlTypes.STRING.ofAny("value-" + i)).build())
							.build())
					.build());
			references.add(RequestReference.builder().reference("resource-" + i, "action").build());
		}
		return RequestContext.builder().attributes(categories).reference(references).build();
	}

	/** For {@link #referencesToRepeats}: the resource categories have no content and no selector. */
	private static final int NO_SELECTOR = -1;

	/**
	 * {@code references} request references, each to its own {@code repeats}
	 * resource categories. Unless {@code nodes} is {@link #NO_SELECTOR},
	 * every resource category has a selector that matches {@code nodes}
	 * nodes of its content; that can be none.
	 */
	private static RequestContext referencesToRepeats(int references, int repeats, int nodes)
	{
		return referencesToRepeats(references, repeats, nodes, 0);
	}

	/**
	 * As {@link #referencesToRepeats(int, int, int)}, with {@code unresolved}
	 * more request references to a category that the request does not have.
	 */
	private static RequestContext referencesToRepeats(int references, int repeats, int nodes, int unresolved)
	{
		List<Category> categories = new ArrayList<>();
		List<RequestReference> requests = new ArrayList<>();
		for (int r = 0; r < references; r++) {
			String[] ids = new String[repeats];
			for (int i = 0; i < repeats; i++) {
				ids[i] = "resource-" + r + "-" + i;
				Entity.Builder entity = Entity.builder()
						.attribute(Attribute.builder("urn:test:id")
								.value(XacmlTypes.STRING.ofAny("value-" + r + "-" + i)).build());
				if (nodes != NO_SELECTOR) {
					StringBuilder xml = new StringBuilder("<record>");
					for (int n = 0; n < nodes; n++) {
						xml.append("<item>").append(n).append("</item>");
					}
					entity.content(XmlContent.of(xml.append("</record>").toString()))
							.attribute(Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
									.value(XacmlTypes.XPATH.ofAny("/record/item", CategoryId.RESOURCE)).build());
				}
				categories.add(Category.builder(CategoryId.RESOURCE).id(ids[i]).entity(entity.build()).build());
			}
			requests.add(RequestReference.builder().reference(ids).build());
		}
		for (int u = 0; u < unresolved; u++) {
			requests.add(RequestReference.builder().reference("missing-" + u).build());
		}
		return RequestContext.builder().attributes(categories).reference(requests).build();
	}

	/** {@code count} categories with the same id and different attribute values. */
	private static List<Category> repeated(CategoryId id, int count)
	{
		List<Category> categories = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			categories.add(Category.builder(id)
					.entity(Entity.builder()
							.attribute(Attribute.builder("urn:test:id")
									.value(XacmlTypes.STRING.ofAny("value-" + i)).build())
							.build())
					.build());
		}
		return categories;
	}

	/**
	 * {@code count} categories with the same id, each with a selector that
	 * matches {@code nodes} nodes of its content. The categories differ in
	 * an attribute value, not only in their content, so that these tests
	 * do not depend on the comparison of XML content (bug
	 * policy4j-the.4.60).
	 */
	private static List<Category> repeatedWithSelector(CategoryId id, int count, int nodes)
	{
		return repeatedWithSelector(id, 0, count, nodes);
	}

	/** As {@link #repeatedWithSelector(CategoryId, int, int)}; the attribute values are numbered from {@code first}. */
	private static List<Category> repeatedWithSelector(CategoryId id, int first, int count, int nodes)
	{
		List<Category> categories = new ArrayList<>();
		for (int i = first; i < first + count; i++) {
			StringBuilder xml = new StringBuilder("<record>");
			for (int n = 0; n < nodes; n++) {
				xml.append("<item>").append(n).append("</item>");
			}
			categories.add(Category.builder(id)
					.entity(Entity.builder()
							.content(XmlContent.of(xml.append("</record>").toString()))
							.attribute(Attribute.builder("urn:test:id")
											.value(XacmlTypes.STRING.ofAny("value-" + i)).build(),
									Attribute.builder(MultipleResourcesViaXPathExpressionHandler.MULTIPLE_CONTENT_SELECTOR)
											.value(XacmlTypes.XPATH.ofAny("/record/item", id)).build())
							.build())
					.build());
		}
		return categories;
	}
}
