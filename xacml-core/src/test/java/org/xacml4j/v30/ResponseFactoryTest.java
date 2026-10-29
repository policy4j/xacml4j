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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

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
		for(List<AttributeAssignment> assignments : List.of(List.<AttributeAssignment>of(), List.of(a),
				List.of(a, b, otherValue, copy, otherCategory, otherIssuer))){
			for(Effect effect : new Effect[]{null, Effect.PERMIT, Effect.DENY}){
				List<AttributeAssignment> input = new ArrayList<>(assignments);
				List<DecisionRuleResponse> expected = List.of(
						Obligation.builder("id", effect).attributes(input).build(),
						Advice.builder("id", effect).attributes(input).build());
				List<DecisionRuleResponse> actual = List.of(
						Obligation.of("id", effect, input), Advice.of("id", effect, input));
				input.clear();
				for(int i = 0; i < actual.size(); i++){
					DecisionRuleResponse response = actual.get(i);
					assertEquals(expected.get(i), response);
					assertEquals(expected.get(i).hashCode(), response.hashCode());
					assertEquals(expected.get(i).toString(), response.toString());
					assertEquals(effect, response.getFulfillOn());
					assertEquals(List.copyOf(expected.get(i).getAttributes()), List.copyOf(response.getAttributes()));
					assertEquals(List.copyOf(expected.get(i).getAttribute("a")), List.copyOf(response.getAttribute("a")));
					assertEquals(List.of(), List.copyOf(response.getAttribute("missing")));
					assertThrows(UnsupportedOperationException.class, () -> response.getAttributes().add(a));
				}
			}
		}
		assertEquals(List.of(a, otherValue, otherCategory, otherIssuer, b), List.copyOf(
				Obligation.of("id", Effect.PERMIT, List.of(a, b, otherValue, copy, otherCategory, otherIssuer)).getAttributes()));
	}
}
