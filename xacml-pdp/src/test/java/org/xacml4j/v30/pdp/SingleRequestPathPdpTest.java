package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Policy Decision Point (PDP) Implementation
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.RequestReference;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.content.XmlContent;
import org.xacml4j.v30.pdp.request.AbstractRequestContextHandler;
import org.xacml4j.v30.pdp.request.MultipleResourcesHandler;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

class SingleRequestPathPdpTest
{
	private static final String MULTIPLE = "urn:oasis:names:tc:xacml:3.0:profile:multiple:content-selector";
	private static final String CONTENT_SELECTOR = "urn:oasis:names:tc:xacml:3.0:content-selector";
	private static final Policy POLICY = Policy.builder("single-request-path").version("1.0")
			.combiningAlgorithm(DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build()
					.getRuleAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
			.rule(Rule.builder("permit", Effect.PERMIT)).build();

	@Test
	void requestReferencesKeepTheirResultOrder() throws Exception {
		Category first = resource("first");
		Category second = resource("second");
		RequestContext request = RequestContext.builder().attributes(first, second)
				.reference(RequestReference.builder().reference("second").build(),
						RequestReference.builder().reference("first").build()).build();
		assertResults(request, List.of(result(second), result(first)));
	}

	@Test
	void repeatedCategoriesKeepTheirResultOrder() throws Exception {
		Category first = resource("first");
		Category second = resource("second");
		assertResults(RequestContext.builder().attributes(first, second).build(),
				List.of(result(first), result(second)));
	}

	@ParameterizedTest
	@ValueSource(strings = {MULTIPLE, "urn:oasis:names:tc:xacml:3.0:multiple:content-selector",
			"urn:oasis:names:tc:xacml:1.0:resource:resource-id"})
	void contentSelectorsKeepTheirIndividualResultsInNodeOrder(String id) throws Exception {
		assertResults(selector(id, "//items/item"),
				List.of(result(selected("//items/item[1]")), result(selected("//items/item[2]"))));
	}

	@Test
	void anEmptySelectionReturnsNoResults() throws Exception {
		assertResults(selector(MULTIPLE, "//items/missing"), List.of());
	}

	@Test
	void aSingleRequestKeepsItsIncludedAttributes() throws Exception {
		Category category = resource("single");
		assertResults(RequestContext.builder().attributes(category).build(), List.of(result(category)));
	}

	@Test
	void aCustomHandlerStillHandlesAnOrdinaryRequest() throws Exception {
		try(PolicyDecisionPoint pdp = builder().defaultRequestHandlers().requestHandler(deny()).build()){
			assertEquals(List.of(Result.ok(Decision.DENY).build()),
					List.copyOf(pdp.decide(RequestContext.builder().build()).getResults()));
		}
	}

	@Test
	void aHandlerAppendedAfterBuildingStillHandlesAnOrdinaryRequest() throws Exception {
		MultipleResourcesHandler handlers = new MultipleResourcesHandler();
		try(PolicyDecisionPoint pdp = builder().requestHandler(handlers).build()){
			handlers.setNext(deny());
			assertEquals(List.of(Result.ok(Decision.DENY).build()),
					List.copyOf(pdp.decide(RequestContext.builder().build()).getResults()));
		}
	}

	@Test
	void concurrentSingleRequestsKeepTheirOwnIncludedAttributes() throws Exception {
		try(PolicyDecisionPoint pdp = builder().defaultRequestHandlers().build();
				var executor = Executors.newFixedThreadPool(4)){
			List<Future<List<Result>>> results = new ArrayList<>();
			for(int i = 0; i < 20; i++){
				RequestContext request = RequestContext.builder().attributes(resource("item-" + i)).build();
				results.add(executor.submit(() -> List.copyOf(pdp.decide(request).getResults())));
			}
			for(int i = 0; i < results.size(); i++){
				assertEquals(List.of(result(resource("item-" + i))), results.get(i).get());
			}
		}
	}

	private static void assertResults(RequestContext request, List<Result> expected) throws Exception {
		try(PolicyDecisionPoint pdp = builder().defaultRequestHandlers().build()){
			assertEquals(expected, List.copyOf(pdp.decide(request).getResults()));
		}
	}

	private static PolicyDecisionPointBuilder builder(){
		return PolicyDecisionPointBuilder.builder("single-request-path")
				.rootPolicy(POLICY).policyRepository(PolicyRepository.build("single-request-path").build())
				.pip(mock(PolicyInformationPoint.class));
	}

	private static AbstractRequestContextHandler deny(){
		return new AbstractRequestContextHandler("test:deny") {
			@Override
			public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context){
				return List.of(Result.ok(Decision.DENY).build());
			}
		};
	}

	private static Category resource(String id){
		return Category.resource().id(id).entity(Entity.builder().attribute(Attribute.builder("item")
				.includeInResult(true).value(XacmlTypes.STRING.ofAny(id)).build()).build()).build();
	}

	private static RequestContext selector(String id, String path) throws Exception {
		return RequestContext.builder().attributes(Category.resource().entity(Entity.builder()
				.content(XmlContent.of(XmlContent.fromString("<items><item>first</item><item>second</item></items>")))
				.attribute(Attribute.builder(id).includeInResult(true)
						.value(XacmlTypes.XPATH.ofAny(path, CategoryId.RESOURCE)).build()).build()).build()).build();
	}

	private static Category selected(String path){
		return Category.resource().entity(Entity.builder().attribute(Attribute.builder(CONTENT_SELECTOR)
				.includeInResult(true).value(XacmlTypes.XPATH.ofAny(path, CategoryId.RESOURCE)).build()).build()).build();
	}

	private static Result result(Category category){
		return Result.ok(Decision.PERMIT).includeInResult(category).build();
	}
}
