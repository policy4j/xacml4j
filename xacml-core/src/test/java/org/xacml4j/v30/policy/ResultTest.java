package org.xacml4j.v30.policy;

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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


public class ResultTest
{
	private Obligation denyObligationWithId1;
	private Obligation denyObligationWithId2;
	private Obligation denyObligationWithSameId1;
	private Obligation mergedDenyObligationWithId1;

	private Advice denyAdviceWithId1;
	private Advice denyAdviceWithId2;
	private Advice denyAdviceWithSameId1;
	private Advice mergedDenyAdviceWithId1;

	@BeforeEach
	public void init(){
		this.denyObligationWithId1 = Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v1"))
				.build();
		this.denyObligationWithId2 = Obligation
				.builder("id2", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v1"))
				.build();
		this.denyObligationWithSameId1 = Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v2"))
				.build();
		this.mergedDenyObligationWithId1 = Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v1"))
				.attribute("test1", XacmlTypes.STRING.ofAny("v2"))
				.build();
		this.denyAdviceWithId1 = Advice
				.builder("id1", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v1"))
				.build();
		this.denyAdviceWithId2 = Advice
				.builder("id2", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v1"))
				.build();
		this.denyAdviceWithSameId1 = Advice
				.builder("id1", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v2"))
				.build();
		this.mergedDenyAdviceWithId1 = Advice
				.builder("id1", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v1"))
				.attribute("test1", XacmlTypes.STRING.ofAny("v2"))
				.build();
	}

	@Test
	public void testMergeObligationValues()
	{
		Result.Builder b = Result.ok(Decision.DENY);
		b.obligation(denyObligationWithId1);
		b.obligation(ImmutableList.of(denyObligationWithId2, denyObligationWithSameId1));
		Result r = b.build();
		assertEquals(Obligation
				.builder("id1", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v1"))
				.attribute("test1", XacmlTypes.STRING.ofAny("v2"))
				.build(), r.getObligation("id1"));
		assertEquals(Obligation
				.builder("id2", Effect.DENY)
				.attribute("test1", XacmlTypes.STRING.ofAny("v1"))
				.build(), r.getObligation("id2"));

	}

	@Test
	public void obligationsAndAdviceKeepTheOrderOfTheirFirstIdAndMergeLaterOnes()
	{
		Result r = Result.ok(Decision.DENY)
				.obligation(denyObligationWithId2, denyObligationWithId1)
				.obligation(ImmutableList.of(denyObligationWithSameId1))
				.advice(denyAdviceWithId2, denyAdviceWithId1)
				.advice(ImmutableList.of(denyAdviceWithSameId1))
				.build();
		assertEquals(ImmutableList.of(denyObligationWithId2, mergedDenyObligationWithId1),
				ImmutableList.copyOf(r.getObligations()));
		assertEquals(ImmutableList.of(denyAdviceWithId2, mergedDenyAdviceWithId1),
				ImmutableList.copyOf(r.getAssociatedAdvice()));
	}

	@Test
	public void aReusedBuilderLeavesEarlierResultsUnchanged()
	{
		Result.Builder b = Result.ok(Decision.DENY)
				.obligation(ImmutableList.of())
				.advice(ImmutableList.of());
		Result empty = b.build();
		Result first = b.obligation(denyObligationWithId1).advice(denyAdviceWithId1).build();
		Result second = b.obligation(denyObligationWithSameId1).advice(denyAdviceWithId2).build();

		assertEquals(Result.ok(Decision.DENY).build(), empty);
		assertTrue(empty.getObligations().isEmpty());
		assertTrue(empty.getAssociatedAdvice().isEmpty());
		assertEquals(ImmutableList.of(denyObligationWithId1), ImmutableList.copyOf(first.getObligations()));
		assertEquals(ImmutableList.of(denyAdviceWithId1), ImmutableList.copyOf(first.getAssociatedAdvice()));
		assertEquals(ImmutableList.of(mergedDenyObligationWithId1), ImmutableList.copyOf(second.getObligations()));
		assertEquals(ImmutableList.of(denyAdviceWithId1, denyAdviceWithId2),
				ImmutableList.copyOf(second.getAssociatedAdvice()));
	}

	/** Design D32: the collections of a result are empty unless something was added, and keep their order. */
	@Test
	public void collectionsOfAResultAreEmptyUnlessAddedAndKeepTheirOrder()
	{
		PolicyIDReference first = PolicyIDReference.builder("policy-b").build();
		PolicyIDReference second = PolicyIDReference.builder("policy-a").build();
		Result bare = Result.ok(Decision.PERMIT).build();
		Result fromEmpty = Result.ok(Decision.PERMIT)
				.includeInResultAttr(ImmutableList.of())
				.resolvedAttr(ImmutableList.of())
				.evaluatedPolicies(ImmutableList.of())
				.build();
		Result withPolicies = Result.ok(Decision.PERMIT).evaluatedPolicies(ImmutableList.of(first, second)).build();

		assertTrue(bare.getIncludeInResultAttributes().isEmpty());
		assertTrue(bare.getResolvedAttributes().isEmpty());
		assertTrue(bare.getPolicyIdentifiers().isEmpty());
		assertEquals(bare, fromEmpty);
		assertEquals(bare.hashCode(), fromEmpty.hashCode());
		assertEquals(ImmutableList.of(first, second), ImmutableList.copyOf(withPolicies.getPolicyIdentifiers()));
	}

	@Test
	public void hashCodeIsTheHashOfTheFieldArray()
	{
		PolicyIDReference policy = PolicyIDReference.builder("policy1").build();
		Result r = Result.ok(Decision.DENY)
				.obligation(denyObligationWithId1)
				.advice(denyAdviceWithId1)
				.referencedPolicy(policy)
				.build();
		assertEquals(Objects.hashCode(Decision.DENY, r.getStatus(),
				ImmutableMap.of("id1", denyAdviceWithId1),
				ImmutableMap.of("id1", denyObligationWithId1),
				ImmutableMap.of(),
				ImmutableList.of(policy),
				ImmutableMap.of()), r.hashCode());
	}

	/** Cycle AM: included categories given or added in any order are equal and hash as the map by category id they were. */
	@Test
	public void includedCategoriesEqualAndHashAsTheMapByCategoryId()
	{
		Category subject = category(CategoryId.SUBJECT_ACCESS, "alice");
		Category resource = category(CategoryId.RESOURCE, "doc-1");
		Result given = Result.ok(Decision.PERMIT).includeInResultAttr(ImmutableList.of(subject, resource)).build();
		Result added = Result.ok(Decision.PERMIT).includeInResult(resource).includeInResult(subject).build();

		assertEquals(given, added);
		assertEquals(given.hashCode(), added.hashCode());
		assertEquals(Objects.hashCode(Decision.PERMIT, given.getStatus(), ImmutableMap.of(), ImmutableMap.of(),
				ImmutableMap.of(CategoryId.SUBJECT_ACCESS, subject, CategoryId.RESOURCE, resource),
				ImmutableList.of(), ImmutableMap.of()), given.hashCode());
		assertEquals(ImmutableList.of(subject, resource), ImmutableList.copyOf(given.getIncludeInResultAttributes()));
		assertEquals(resource, given.getIncludeInResultByCategory(CategoryId.RESOURCE));
		assertThrows(IllegalArgumentException.class, () -> Result.ok(Decision.PERMIT)
				.includeInResultAttr(ImmutableList.of(subject, category(CategoryId.SUBJECT_ACCESS, "bob"))).build());
	}

	/** Cycle AM: responses given together keep their order and merge a repeated id; a reused builder keeps earlier results. */
	@Test
	public void responsesGivenTogetherKeepTheirOrderAndMergeARepeatedId()
	{
		Result together = Result.ok(Decision.DENY)
				.obligation(ImmutableList.of(denyObligationWithId2, denyObligationWithId1, denyObligationWithSameId1))
				.advice(ImmutableList.of(denyAdviceWithId2, denyAdviceWithId1))
				.build();
		Result oneByOne = Result.ok(Decision.DENY)
				.obligation(denyObligationWithId2, denyObligationWithId1, denyObligationWithSameId1)
				.advice(denyAdviceWithId2, denyAdviceWithId1)
				.build();
		Result.Builder reused = Result.ok(Decision.DENY).obligation(ImmutableList.of(denyObligationWithId1));
		Result first = reused.build();
		Result second = reused.obligation(ImmutableList.of(denyObligationWithSameId1)).build();

		assertEquals(ImmutableList.of(denyObligationWithId2, mergedDenyObligationWithId1), ImmutableList.copyOf(together.getObligations()));
		assertEquals(ImmutableList.of(denyAdviceWithId2, denyAdviceWithId1), ImmutableList.copyOf(together.getAssociatedAdvice()));
		assertEquals(oneByOne, together);
		assertEquals(oneByOne.hashCode(), together.hashCode());
		assertEquals(ImmutableList.of(denyObligationWithId1), ImmutableList.copyOf(first.getObligations()));
		assertEquals(ImmutableList.of(mergedDenyObligationWithId1), ImmutableList.copyOf(second.getObligations()));
	}

	/** Design D22: results with the same obligations and advice in another order are equal and hash alike, as maps by id. */
	@Test
	public void responsesInAnotherOrderGiveEqualResults()
	{
		Result one = Result.ok(Decision.DENY)
				.obligation(denyObligationWithId1, denyObligationWithId2)
				.advice(denyAdviceWithId1, denyAdviceWithId2)
				.build();
		Result other = Result.ok(Decision.DENY)
				.obligation(ImmutableList.of(denyObligationWithId2, denyObligationWithId1))
				.advice(denyAdviceWithId2, denyAdviceWithId1)
				.build();

		assertEquals(one, other);
		assertEquals(one.hashCode(), other.hashCode());
		assertNotEquals(one, Result.ok(Decision.DENY).obligation(denyObligationWithId1, denyObligationWithSameId1)
				.advice(denyAdviceWithId1, denyAdviceWithId2).build());
	}

	/** Cycle AU: a result made without a builder equals the result of a builder with the same content and shares its OK status. */
	@Test
	public void resultsMadeWithoutABuilderEqualTheResultsOfABuilder()
	{
		PolicyIDReference policy = PolicyIDReference.builder("policy1").build();
		Category subject = category(CategoryId.SUBJECT_ACCESS, "alice");
		Category resolved = category(CategoryId.RESOURCE, "doc-1");
		Result built = Result.ok(Decision.DENY)
				.obligation(denyObligationWithId2, denyObligationWithId1)
				.advice(denyAdviceWithId1)
				.includeInResultAttr(ImmutableList.of(subject))
				.resolvedAttr(ImmutableList.of(resolved))
				.evaluatedPolicies(ImmutableList.of(policy))
				.build();

		Result made = Result.of(Decision.DENY, ImmutableList.of(denyObligationWithId2, denyObligationWithId1),
				ImmutableList.of(denyAdviceWithId1), ImmutableList.of(subject), ImmutableList.of(resolved),
				ImmutableList.of(policy));

		assertEquals(built, made);
		assertEquals(built.hashCode(), made.hashCode());
		assertSame(Result.ok(Decision.PERMIT).build().getStatus(), made.getStatus());
		assertEquals(ImmutableList.of(denyObligationWithId2, denyObligationWithId1), ImmutableList.copyOf(made.getObligations()));
		assertEquals(denyObligationWithId1, made.getObligation("id1"));
		assertEquals(denyAdviceWithId1, made.getAssociatedAdvice("id1"));
		assertEquals(ImmutableList.of(resolved), ImmutableList.copyOf(made.getResolvedAttributes()));
		Status failed = Status.processingError().message("failed").build();
		assertEquals(Result.builder(Decision.INDETERMINATE_P, failed).includeInResultAttr(ImmutableList.of(subject)).build(),
				Result.of(Decision.INDETERMINATE_P, failed, ImmutableList.of(subject), ImmutableList.of()));
	}

	@Test
	public void plainResultsEqualBuiltResultsAndKeepExplicitStatuses()
	{
		for(Decision decision : ImmutableList.of(Decision.PERMIT, Decision.DENY, Decision.NOT_APPLICABLE)){
			Result built = Result.ok(decision).build();
			Result made = Result.of(decision, ImmutableList.of(), ImmutableList.of(),
					ImmutableList.of(), ImmutableList.of(), ImmutableList.of());
			assertEquals(built, made);
			assertEquals(built.hashCode(), made.hashCode());
			Status status = Status.processingError().message("unavailable")
					.error(new IllegalStateException("backend failed")).build();
			Result failed = Result.of(decision, status, ImmutableList.of(), ImmutableList.of());
			assertSame(status, failed.getStatus());
			assertEquals(Result.builder(decision, status).build(), failed);
		}
	}

	/** Cycle AU: the responses of a result made without a builder have distinct ids, as the map that held them required. */
	@Test
	public void aResultMadeWithoutABuilderRejectsARepeatedId()
	{
		assertThrows(IllegalArgumentException.class, () -> Result.of(Decision.DENY,
				ImmutableList.of(denyObligationWithId1, denyObligationWithSameId1), ImmutableList.of(),
				ImmutableList.of(), ImmutableList.of(), ImmutableList.of()));
		assertThrows(IllegalArgumentException.class, () -> Result.of(Decision.DENY,
				ImmutableList.of(), ImmutableList.of(denyAdviceWithId1, denyAdviceWithSameId1),
				ImmutableList.of(), ImmutableList.of(), ImmutableList.of()));
		assertThrows(IllegalArgumentException.class, () -> Result.of(Decision.INDETERMINATE,
				ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), ImmutableList.of()));
	}

	private static Category category(CategoryId id, String value)
	{
		return Category.builder(id).entity(Entity.builder()
				.attribute(Attribute.builder("urn:xacml4j:test:id").includeInResult(true).value(XacmlTypes.STRING.ofAny(value)).build())
				.build()).build();
	}

	@Test
	public void statusHashCodesAreTheHashesOfTheFieldArrays()
	{
		StatusCode code = StatusCode.builder(StatusCodeId.STATUS_PROCESSING_ERROR)
				.minorStatus(StatusCode.missingAttributeError())
				.build();
		Status status = Status.builder(code)
				.message("test")
				.detail(Status.ok().build())
				.build();
		assertEquals(Objects.hashCode(StatusCodeId.MISSING_ATTRIBUTE, null), code.getMinorStatus().hashCode());
		assertEquals(Objects.hashCode(StatusCodeId.STATUS_PROCESSING_ERROR, code.getMinorStatus()), code.hashCode());
		assertEquals(Objects.hashCode(code, "test", status.getDetail().get()), status.hashCode());
	}
}
