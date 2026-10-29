package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableSetMultimap;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.types.StringVal;

public class ResponseFactoryTest
{
	@Test
	public void factoriesKeepBuilderContentOrderEqualityAndOwnership()
	{
		AttributeAssignment a = AttributeAssignment.builder("a").value(StringVal.of("one")).build();
		AttributeAssignment b = AttributeAssignment.builder("b").value(StringVal.of("two")).build();
		AttributeAssignment otherValue = AttributeAssignment.builder("a").value(StringVal.of("three")).build();
		AttributeAssignment otherCategory = AttributeAssignment.builder("a").category(CategoryId.RESOURCE)
				.value(StringVal.of("one")).build();
		AttributeAssignment otherIssuer = AttributeAssignment.builder("a").issuer("issuer")
				.value(StringVal.of("one")).build();
		AttributeAssignment copy = AttributeAssignment.builder("a").value(StringVal.of("one")).build();
		List<AttributeAssignment> many = new ArrayList<>();
		for(int i = 0; i < 9; i++){
			many.add(AttributeAssignment.builder("id-" + i).value(StringVal.of("value")).build());
		}
		for(List<AttributeAssignment> assignments : List.of(List.<AttributeAssignment>of(), List.of(a),
				List.of(a, b), List.of(a, otherValue), List.of(a, copy), List.of(a, otherValue, b),
				List.of(a, b, otherValue, copy, otherCategory, otherIssuer), many)){
			for(Effect effect : new Effect[]{null, Effect.PERMIT, Effect.DENY}){
				List<AttributeAssignment> input = new ArrayList<>(assignments);
				ImmutableSetMultimap.Builder<String, AttributeAssignment> attributes = ImmutableSetMultimap.builder();
				for(AttributeAssignment assignment : assignments){
					attributes.put(assignment.getAttributeId(), assignment);
				}
				int originalHash = 31 * (31 + "id".hashCode()) + attributes.build().hashCode();
				List<DecisionRuleResponse> expected = List.of(
						Obligation.builder("id", effect).attributes(input).build(),
						Advice.builder("id", effect).attributes(input).build());
				List<DecisionRuleResponse> actual = List.of(
						Obligation.of("id", effect, input), Advice.of("id", effect, input));
				input.clear();
				for(int i = 0; i < actual.size(); i++){
					DecisionRuleResponse response = actual.get(i);
					assertEquals(expected.get(i), response);
					assertEquals(response, expected.get(i));
					assertEquals(expected.get(i).hashCode(), response.hashCode());
					assertEquals(originalHash, response.hashCode());
					assertEquals(expected.get(i).toString(), response.toString());
					assertEquals(effect, response.getFulfillOn());
					assertEquals(List.copyOf(expected.get(i).getAttributes()), List.copyOf(response.getAttributes()));
					assertEquals(List.copyOf(expected.get(i).getAttribute("a")), List.copyOf(response.getAttribute("a")));
					assertEquals(List.of(), List.copyOf(response.getAttribute("missing")));
					assertThrows(UnsupportedOperationException.class, () -> response.getAttributes().add(a));
					assertThrows(UnsupportedOperationException.class, () -> response.getAttribute("missing").add(a));
				}
			}
		}
		assertEquals(List.of(a, otherValue, otherCategory, otherIssuer, b), List.copyOf(
				Obligation.of("id", Effect.PERMIT, List.of(a, b, otherValue, copy, otherCategory, otherIssuer)).getAttributes()));
	}

	@Test
	public void equalAssignmentsInDifferentOrdersKeepSetEqualityAndHash()
	{
		AttributeAssignment a = AttributeAssignment.builder("a").value(StringVal.of("one")).build();
		AttributeAssignment other = AttributeAssignment.builder("a").value(StringVal.of("two")).build();
		AttributeAssignment b = AttributeAssignment.builder("b").value(StringVal.of("three")).build();
		List<DecisionRuleResponse> first = List.of(
				Obligation.of("id", Effect.PERMIT, List.of(a, other, b)),
				Advice.of("id", Effect.PERMIT, List.of(a, other, b)));
		List<DecisionRuleResponse> reordered = List.of(
				Obligation.builder("id", Effect.PERMIT).attributes(List.of(b, other, a)).build(),
				Advice.builder("id", Effect.PERMIT).attributes(List.of(b, other, a)).build());
		for(int i = 0; i < first.size(); i++){
			assertEquals(first.get(i), reordered.get(i));
			assertEquals(reordered.get(i), first.get(i));
			assertEquals(first.get(i).hashCode(), reordered.get(i).hashCode());
		}
	}

	@Test
	public void mergeAndBuilderFromKeepFactoryAndBuilderAssignments()
	{
		AttributeAssignment a = AttributeAssignment.builder("a").value(StringVal.of("one")).build();
		AttributeAssignment b = AttributeAssignment.builder("b").value(StringVal.of("two")).build();
		AttributeAssignment other = AttributeAssignment.builder("a").value(StringVal.of("three")).build();
		Obligation obligation = Obligation.of("id", Effect.PERMIT, List.of(a, b));
		Obligation obligationTail = Obligation.builder("id", Effect.PERMIT).attributes(List.of(other, a)).build();
		Advice advice = Advice.of("id", Effect.PERMIT, List.of(a, b));
		Advice adviceTail = Advice.builder("id", Effect.PERMIT).attributes(List.of(other, a)).build();
		assertEquals(List.of(other, a, b), List.copyOf(obligation.merge(obligationTail).getAttributes()));
		assertEquals(List.of(a, other, b), List.copyOf(advice.merge(adviceTail).getAttributes()));
		assertEquals(List.of(a, other, b), List.copyOf(obligationTail.merge(obligation).getAttributes()));
		assertEquals(List.of(other, a, b), List.copyOf(adviceTail.merge(advice).getAttributes()));
		assertEquals(obligation, Obligation.builder("other").from(obligation).build());
		assertEquals(obligationTail, Obligation.builder("other").from(obligationTail).build());
		assertEquals(advice, Advice.builder("other").from(advice).build());
		assertEquals(adviceTail, Advice.builder("other").from(adviceTail).build());
	}

	@Test
	public void subclassBuilderKeepsItsSuppliedMultimapKeys()
	{
		AttributeAssignment assignment = AttributeAssignment.builder("attribute").value(StringVal.of("value")).build();
		CustomResponse first = new CustomResponse.Builder().entry("first", assignment).build();
		CustomResponse second = new CustomResponse.Builder().entry("second", assignment).build();
		assertNotEquals(first, second);
		assertNotEquals(second, first);
		assertEquals(31 * (31 + "custom".hashCode()) + ImmutableSetMultimap.of("first", assignment).hashCode(),
				first.hashCode());
		CustomResponse copy = new CustomResponse.Builder().from(first).build();
		assertEquals(first, copy);
		assertEquals(first.hashCode(), copy.hashCode());
		assertEquals(List.of(assignment), List.copyOf(copy.getAttribute("first")));
		assertEquals(List.of(), List.copyOf(copy.getAttribute("attribute")));
	}

	private static final class CustomResponse extends DecisionRuleResponse
	{
		private CustomResponse(Builder builder){
			super(builder);
		}

		@Override
		public boolean equals(Object other){
			return other instanceof CustomResponse response && equalsTo(response);
		}

		@Override
		public <T extends DecisionRuleResponse> T merge(T response){
			return response;
		}

		private static final class Builder extends DecisionRuleResponse.Builder<Builder>
		{
			private Builder(){
				super("custom", Effect.PERMIT);
			}

			private Builder entry(String key, AttributeAssignment value){
				attributes.put(key, value);
				return this;
			}

			private CustomResponse build(){
				return new CustomResponse(this);
			}

			@Override
			protected Builder getThis(){
				return this;
			}
		}
	}
}
