package org.xacml4j.v30;

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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;

import com.google.common.collect.ImmutableList;

/** The results of a response keep the order they were added in, however they were added (design D32). */
class ResponseContextTest
{
	private final Result permit = Result.ok(Decision.PERMIT).build();
	private final Result deny = Result.ok(Decision.DENY).build();
	private final Result notApplicable = Result.ok(Decision.NOT_APPLICABLE).build();

	@Test
	void resultsKeepTheirOrderHoweverTheyAreAdded()
	{
		assertEquals(List.of(permit, deny),
				List.copyOf(ResponseContext.builder().results(List.of(permit, deny)).build().getResults()));
		assertEquals(List.of(permit, deny, notApplicable),
				List.copyOf(ResponseContext.builder().results(List.of(permit, deny)).result(notApplicable).build().getResults()));
		assertEquals(List.of(notApplicable, permit, deny),
				List.copyOf(ResponseContext.builder().result(notApplicable).results(List.of(permit, deny)).build().getResults()));
		assertEquals(List.of(permit, deny, notApplicable),
				List.copyOf(ResponseContext.builder().results(List.of(permit)).results(List.of(deny, notApplicable)).build().getResults()));
		assertEquals(List.of(), List.copyOf(ResponseContext.builder().build().getResults()));
	}

	@Test
	void responsesWithTheSameResultsAreEqual()
	{
		assertEquals(ResponseContext.builder().results(List.of(permit, deny)).build(),
				ResponseContext.builder().result(permit).result(deny).build());
	}

	/** Cycle AU: a plain result made without a builder shares the response of its decision; one with an obligation does not. */
	@Test
	void plainResultsMadeWithoutABuilderShareTheResponseOfTheirDecision()
	{
		Result plain = Result.of(Decision.DENY, ImmutableList.of(), ImmutableList.of(), ImmutableList.of(),
				ImmutableList.of(), ImmutableList.of());
		Result obliged = Result.of(Decision.DENY, ImmutableList.of(Obligation.builder("obligation", Effect.DENY).build()),
				ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of());

		assertSame(ResponseContext.of(deny), ResponseContext.of(plain));
		assertNotSame(ResponseContext.of(deny), ResponseContext.of(obliged));
		assertEquals(List.of(obliged), List.copyOf(ResponseContext.of(obliged).getResults()));
	}

	@Test
	void singleResponsesPreserveEveryResultFieldAndRemainImmutable()
	{
		Category included = Category.resource().entity(Entity.builder().attribute(Attribute.builder("item")
				.includeInResult(true).value(StringVal.of("document")).build()).build()).build();
		RuntimeException failure = new IllegalStateException("failed", new IllegalArgumentException("cause"));
		List<Result> inputs = List.of(permit, deny, notApplicable,
				Result.ok(Decision.PERMIT).includeInResult(included).build(),
				Result.ok(Decision.PERMIT).resolvedAttr(List.of(included)).build(),
				Result.ok(Decision.PERMIT).obligation(Obligation.builder("obligation", Effect.PERMIT).build()).build(),
				Result.ok(Decision.PERMIT).advice(Advice.builder("advice", Effect.PERMIT).build()).build(),
				Result.ok(Decision.PERMIT).referencedPolicy(PolicyIDReference.builder("urn:test:policy").build()).build(),
				Result.builder(Decision.PERMIT, Status.ok().message("detail").error(failure).build()).build(),
				Result.indeterminate(Status.processingError().message("failed").error(failure).build()).build());
		for(Result input : inputs){
			ResponseContext response = ResponseContext.of(input);
			assertEquals(ResponseContext.builder().result(input).build(), response);
			assertEquals(input.getStatus().getError(), response.getResults().iterator().next().getStatus().getError());
			assertThrows(UnsupportedOperationException.class, () -> response.getResults().clear());
		}
	}
}
