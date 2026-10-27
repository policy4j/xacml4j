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
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.StatusCodeId;
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
