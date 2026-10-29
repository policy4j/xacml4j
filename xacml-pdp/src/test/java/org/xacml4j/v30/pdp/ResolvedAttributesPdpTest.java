package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J PDP
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * The attributes that a decision resolves from the information point come in its result by category,
 * equal attributes once, with their issuers and values, and in the order of a hash multimap of the
 * categories, as before the result of one category was built without the multimap (cycle AZ)
 */
class ResolvedAttributesPdpTest
{
	private static final String ISSUER = "urn:example:issuer";

	@Test
	void theAttributesOfOneCategoryComeWithTheirIssuersAndValues(){
		AttributeDesignatorKey role = key(CategoryId.SUBJECT_ACCESS, "urn:example:role", null, XacmlTypes.STRING);
		AttributeDesignatorKey issued = key(CategoryId.SUBJECT_ACCESS, "urn:example:role", ISSUER, XacmlTypes.STRING);
		AttributeDesignatorKey level = key(CategoryId.SUBJECT_ACCESS, "urn:example:level", null, XacmlTypes.INTEGER);
		Map<AttributeDesignatorKey, BagOfValues> bags = new LinkedHashMap<>();
		bags.put(role, XacmlTypes.STRING.bagOf(StringVal.of("reader"), StringVal.of("writer")));
		bags.put(issued, XacmlTypes.STRING.bagOf(StringVal.of("auditor")));
		bags.put(level, XacmlTypes.INTEGER.bagOf(XacmlTypes.INTEGER.ofAny(3)));

		Result result = decide(List.copyOf(bags.keySet()), bags, false).result();

		assertEquals(Map.of(CategoryId.SUBJECT_ACCESS, Set.of(attribute(role, bags), attribute(issued, bags), attribute(level, bags))),
				byCategory(result));
		Attribute auditor = result.getResolvedAttributes().iterator().next().getEntity().attributes().stream()
				.filter(a -> ISSUER.equals(a.getIssuer())).findFirst().orElseThrow();
		assertEquals("urn:example:role", auditor.getAttributeId());
	}

	/** Two types of one attribute resolve to no values: the two attributes are equal, and come once */
	@Test
	void equalAttributesOfACategoryComeOnce(){
		AttributeDesignatorKey text = key(CategoryId.RESOURCE, "urn:example:tag", ISSUER, XacmlTypes.STRING);
		AttributeDesignatorKey number = key(CategoryId.RESOURCE, "urn:example:tag", ISSUER, XacmlTypes.INTEGER);
		AttributeDesignatorKey other = key(CategoryId.RESOURCE, "urn:example:owner", null, XacmlTypes.STRING);
		Map<AttributeDesignatorKey, BagOfValues> bags = Map.of(text, XacmlTypes.STRING.emptyBag(),
				number, XacmlTypes.INTEGER.emptyBag(), other, XacmlTypes.STRING.bagOf(StringVal.of("alice")));

		Result result = decide(List.of(text, number, other), bags, false).result();

		List<Attribute> attributes = List.copyOf(result.getResolvedAttributes().iterator().next().getEntity().attributes());
		assertEquals(2, attributes.size());
		assertEquals(Set.of(attribute(text, bags), attribute(other, bags)), Set.copyOf(attributes));
	}

	@Test
	void theAttributesOfTwoCategoriesComeInTheirCategories(){
		AttributeDesignatorKey role = key(CategoryId.SUBJECT_ACCESS, "urn:example:role", ISSUER, XacmlTypes.STRING);
		AttributeDesignatorKey owner = key(CategoryId.RESOURCE, "urn:example:owner", null, XacmlTypes.STRING);
		AttributeDesignatorKey group = key(CategoryId.RESOURCE, "urn:example:group", null, XacmlTypes.STRING);
		Map<AttributeDesignatorKey, BagOfValues> bags = Map.of(role, XacmlTypes.STRING.bagOf(StringVal.of("reader")),
				owner, XacmlTypes.STRING.bagOf(StringVal.of("alice")), group, XacmlTypes.STRING.bagOf(StringVal.of("staff")));

		Result result = decide(List.of(role, owner, group), bags, false).result();

		assertEquals(Map.of(CategoryId.SUBJECT_ACCESS, Set.of(attribute(role, bags)),
				CategoryId.RESOURCE, Set.of(attribute(owner, bags), attribute(group, bags))), byCategory(result));
	}

	/** Eight attributes of one category, then of two, resolved on four threads of one decision */
	@Test
	void attributesResolvedOnSeveralThreadsAllCome(){
		for(int categories = 1; categories <= 2; categories++){
			List<AttributeDesignatorKey> keys = new ArrayList<>();
			Map<AttributeDesignatorKey, BagOfValues> bags = new HashMap<>();
			Map<CategoryId, Set<Attribute>> expected = new HashMap<>();
			for(int i = 0; i < 8; i++){
				CategoryId category = categories == 2 && i % 2 == 1 ? CategoryId.RESOURCE : CategoryId.SUBJECT_ACCESS;
				AttributeDesignatorKey key = key(category, "urn:example:attribute-" + i, i % 3 == 0 ? ISSUER : null, XacmlTypes.STRING);
				keys.add(key);
				bags.put(key, XacmlTypes.STRING.bagOf(StringVal.of("value-" + i)));
			}
			for(AttributeDesignatorKey key : keys){
				expected.computeIfAbsent(key.getCategory(), c -> new HashSet<>()).add(attribute(key, bags));
			}
			assertEquals(expected, byCategory(decide(keys, bags, true).result()));
		}
	}

	/** One to seven attributes of one category, then attributes of three categories with two issuers */
	@Test
	void categoriesAndAttributesComeInTheOrderOfAHashMultimapOfThem(){
		for(int n = 1; n <= 7; n++){
			List<AttributeDesignatorKey> keys = new ArrayList<>();
			for(int i = 0; i < n; i++){
				keys.add(key(CategoryId.SUBJECT_ACCESS, "urn:example:attribute-" + i, null, XacmlTypes.STRING));
			}
			assertInTheOrderOfAHashMultimap(keys);
		}
		List<AttributeDesignatorKey> keys = new ArrayList<>();
		CategoryId[] categories = { CategoryId.SUBJECT_ACCESS, CategoryId.RESOURCE, CategoryId.ACTION };
		for(int i = 0; i < 9; i++){
			keys.add(key(categories[i % 3], "urn:example:attribute-" + i / 2, i % 2 == 0 ? ISSUER : null, XacmlTypes.STRING));
		}
		assertInTheOrderOfAHashMultimap(keys);
	}

	private static void assertInTheOrderOfAHashMultimap(List<AttributeDesignatorKey> keys){
		Map<AttributeDesignatorKey, BagOfValues> bags = new HashMap<>();
		for(int i = 0; i < keys.size(); i++){
			bags.put(keys.get(i), XacmlTypes.STRING.bagOf(StringVal.of("value-" + i)));
		}
		Decided decided = decide(keys, bags, false);
		Multimap<CategoryId, Attribute> model = HashMultimap.create();
		for(AttributeDesignatorKey k : decided.resolved().keySet()){
			model.put(k.getCategory(), attribute(k, decided.resolved()));
		}
		List<CategoryId> order = new ArrayList<>();
		for(Category category : decided.result().getResolvedAttributes()){
			order.add(category.getCategoryId());
			assertEquals(List.copyOf(Entity.builder().attributes(model.get(category.getCategoryId())).build().attributes()),
					List.copyOf(category.getEntity().attributes()), keys::toString);
		}
		assertEquals(List.copyOf(model.keySet()), order, keys::toString);
	}

	/** The result, and the designators that the decision resolved */
	private record Decided(Result result, Map<AttributeDesignatorKey, BagOfValues> resolved) { }

	/** A decision whose policy resolves the keys in order, or each on a thread of a pool of four */
	private static Decided decide(List<AttributeDesignatorKey> keys, Map<AttributeDesignatorKey, BagOfValues> bags,
			boolean threads){
		AtomicReference<Map<AttributeDesignatorKey, BagOfValues>> resolved = new AtomicReference<>();
		ExecutorService pool = Executors.newFixedThreadPool(4);
		try{
			CompositeDecisionRule root = mock(CompositeDecisionRule.class);
			when(root.createContext(any())).thenAnswer(call -> call.getArgument(0));
			when(root.evaluate(any())).thenAnswer(call -> {
				EvaluationContext context = call.getArgument(0);
				List<Future<Optional<BagOfValues>>> resolutions = new ArrayList<>();
				for(AttributeDesignatorKey key : keys){
					if(threads){
						resolutions.add(pool.submit(() -> context.resolve(key)));
					}else{
						context.resolve(key);
					}
				}
				for(Future<Optional<BagOfValues>> resolution : resolutions){
					resolution.get();
				}
				resolved.set(context.getResolvedDesignators());
				return Decision.PERMIT;
			});
			PolicyInformationPoint pip = mock(PolicyInformationPoint.class);
			when(pip.resolve(any(), any(AttributeDesignatorKey.class)))
					.thenAnswer(call -> Optional.ofNullable(bags.get(call.<AttributeDesignatorKey>getArgument(1))));
			Result result = PolicyDecisionPointBuilder.builder("resolved-attributes").policyRepository(mock(PolicyRepository.class))
					.pip(pip).rootPolicy(root).build().decide(RequestContext.builder().build()).getResults().iterator().next();
			assertEquals(Decision.PERMIT, result.getDecision());
			return new Decided(result, resolved.get());
		}finally{
			pool.shutdownNow();
		}
	}

	private static Map<CategoryId, Set<Attribute>> byCategory(Result result){
		Map<CategoryId, Set<Attribute>> categories = new HashMap<>();
		for(Category category : result.getResolvedAttributes()){
			assertNull(categories.put(category.getCategoryId(), Set.copyOf(category.getEntity().attributes())));
		}
		return categories;
	}

	private static Attribute attribute(AttributeDesignatorKey key, Map<AttributeDesignatorKey, BagOfValues> bags){
		return Attribute.builder(key.getAttributeId()).issuer(key.getIssuer()).values(bags.get(key)).build();
	}

	private static AttributeDesignatorKey key(CategoryId category, String attributeId, String issuer, ValueType type){
		return AttributeDesignatorKey.builder(attributeId).category(category).issuer(issuer).dataType(type).build();
	}
}
