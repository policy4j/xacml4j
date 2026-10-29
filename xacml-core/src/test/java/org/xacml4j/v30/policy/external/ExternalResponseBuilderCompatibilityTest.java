package org.xacml4j.v30.policy.external;

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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.types.StringVal;

/**
 * A response and its builder declared outside the package of {@link DecisionRuleResponse}: the
 * builder fills the protected {@code attributes} field directly, as it could at HEAD (review R-AN
 * of change optimize-pdp-evaluation, Codex Sol, P2-2).
 */
public class ExternalResponseBuilderCompatibilityTest
{
	@Test
	public void aBuilderOfAnotherPackageFillsTheAttributesFieldDirectly()
	{
		Receipt receipt = new Receipt.Builder()
				.stamp(assignment("issued", "2026-10-28"))
				.stamp(assignment("by", "pdp"))
				.stamp(assignment("by", "pep"))
				.build();

		assertEquals("receipt", receipt.getId());
		assertEquals(Effect.PERMIT, receipt.getFulfillOn());
		assertEquals(List.of("issued", "by", "by"),
				receipt.getAttributes().stream().map(AttributeAssignment::getAttributeId).toList());
		assertEquals(List.<Object>of(StringVal.of("2026-10-28"), StringVal.of("pdp"), StringVal.of("pep")),
				receipt.getAttributes().stream().map(a -> (Object)a.getAttribute()).toList());
		assertEquals(2, receipt.getAttribute("by").size());
	}

	private static AttributeAssignment assignment(String id, String value)
	{
		return AttributeAssignment.builder(id).value(StringVal.of(value)).build();
	}

	/** A response of another package */
	private static final class Receipt extends DecisionRuleResponse
	{
		private Receipt(Builder b) {
			super(b);
		}

		@Override
		public <T extends DecisionRuleResponse> T merge(T a) {
			return a;
		}

		/** Writes the protected field of the base builder directly */
		private static final class Builder extends DecisionRuleResponse.Builder<Builder>
		{
			private Builder() {
				super("receipt", Effect.PERMIT);
			}

			private Builder stamp(AttributeAssignment assignment) {
				attributes.put(assignment.getAttributeId(), assignment);
				return this;
			}

			@Override
			protected Builder getThis() {
				return this;
			}

			private Receipt build() {
				return new Receipt(this);
			}
		}
	}
}
