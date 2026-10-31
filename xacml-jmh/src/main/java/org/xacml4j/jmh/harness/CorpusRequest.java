package org.xacml4j.jmh.harness;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.types.Value;

/**
 * A parsed request of a corpus, its expected decision, and its whole expected result where the
 * corpus knows it.
 *
 * @param id the request id, unique within its corpus
 * @param request the parsed request
 * @param expected the expected decision; {@code INDETERMINATE} matches every Indeterminate value
 * @param response the expected result, if any
 */
public record CorpusRequest(String id, RequestContext request, Decision expected, Optional<Result> response)
{
	public CorpusRequest {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(request, "request");
		Objects.requireNonNull(expected, "expected");
		Objects.requireNonNull(response, "response");
	}

	/** A request whose decision is expected */
	public CorpusRequest(String id, RequestContext request, Decision expected){
		this(id, request, expected, Optional.empty());
	}

	/** A request whose whole result is expected */
	public CorpusRequest(String id, RequestContext request, Result response){
		this(id, request, response.getDecision(), Optional.of(response));
	}

	/**
	 * @param actual a decision of the PDP
	 * @return whether it is the expected decision; an expected Indeterminate
	 * matches every Indeterminate value (XACML 3.0 §5.53, §7.10)
	 */
	public boolean matches(Decision actual){
		return expected.isIndeterminate() ? actual.isIndeterminate() : expected == actual;
	}

	/**
	 * Compares a result of the PDP with the expected decision or, where there is one, with the
	 * expected result: decision, status, obligations and advice in order with their attribute
	 * assignments in order, policy identifiers in order, and the values of the IncludeInResult and
	 * resolved attributes in order.
	 *
	 * @param actual a result of the PDP
	 * @return how it differs, if it does
	 */
	public Optional<String> mismatch(Result actual){
		if(response.isEmpty()){
			return matches(actual.getDecision()) ? Optional.empty()
					: Optional.of("expected " + expected + ", got " + actual.getDecision());
		}
		Result result = response.orElseThrow();
		boolean same = actual.equals(result)
				&& sameInOrder(actual.getObligations(), result.getObligations())
				&& sameInOrder(actual.getAssociatedAdvice(), result.getAssociatedAdvice())
				&& values(actual.getIncludeInResultAttributes()).equals(values(result.getIncludeInResultAttributes()))
				&& values(actual.getResolvedAttributes()).equals(values(result.getResolvedAttributes()));
		return same ? Optional.empty() : Optional.of("expected " + result + ", got " + actual);
	}

	private static boolean sameInOrder(Collection<? extends DecisionRuleResponse> actual,
			Collection<? extends DecisionRuleResponse> expected){
		List<? extends DecisionRuleResponse> actualList = List.copyOf(actual);
		List<? extends DecisionRuleResponse> expectedList = List.copyOf(expected);
		if(actualList.size() != expectedList.size()) return false;
		for(int i = 0; i < actualList.size(); i++){
			DecisionRuleResponse a = actualList.get(i);
			DecisionRuleResponse e = expectedList.get(i);
			if(!a.equals(e) || a.getFulfillOn() != e.getFulfillOn()
					|| !List.copyOf(a.getAttributes()).equals(List.copyOf(e.getAttributes()))) return false;
		}
		return true;
	}

	private static Map<CategoryId, Map<String, List<Value<?>>>> values(Collection<Category> categories){
		Map<CategoryId, Map<String, List<Value<?>>>> result = new LinkedHashMap<>();
		for(Category category : categories){
			Map<String, List<Value<?>>> values = new LinkedHashMap<>();
			for(Attribute attribute : category.getEntity().attributes()){
				values.put(attribute.getAttributeId(), List.copyOf(attribute.values()));
			}
			result.put(category.getCategoryId(), values);
		}
		return result;
	}
}
