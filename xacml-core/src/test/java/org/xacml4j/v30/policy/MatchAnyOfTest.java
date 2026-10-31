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

import static org.mockito.Mockito.*;

import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;


@ExtendWith(MockitoExtension.class)
public class MatchAnyOfTest
{
	private Collection<MatchAllOf> matches;
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.matches = new LinkedList<MatchAllOf>();
		this.context = ModelFixtures.context();
	}

	@Test
	public void testAtLeastOneMatch1()
	{
		Expression m1Expression = ModelFixtures.reference();
		MatchAllOf m1 = ModelFixtures.allOf(m1Expression);
		Expression m2Expression = ModelFixtures.reference();
		MatchAllOf m2 = ModelFixtures.allOf(m2Expression);
		Expression m3Expression = ModelFixtures.reference();
		MatchAllOf m3 = ModelFixtures.allOf(m3Expression);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		when(m1Expression.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));

		InOrder order = inOrder(m1Expression, m2Expression, m3Expression);
		Matchable m = MatchAnyOf.builder().anyOf(matches).build();
		assertEquals(MatchResult.MATCH, m.match(context));
		order.verify(m1Expression).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(m1Expression, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Expression, m2Expression, m3Expression);
	}

	@Test
	public void testAtLeastOneMatch2()
	{
		Expression m1Expression = ModelFixtures.reference();
		MatchAllOf m1 = ModelFixtures.allOf(m1Expression);
		Expression m2Expression = ModelFixtures.reference();
		MatchAllOf m2 = ModelFixtures.allOf(m2Expression);
		Expression m3Expression = ModelFixtures.reference();
		MatchAllOf m3 = ModelFixtures.allOf(m3Expression);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		when(m1Expression.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));
		when(m2Expression.evaluate(context)).thenThrow(ModelFixtures.evaluationError());
		when(m3Expression.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));

		InOrder order = inOrder(m1Expression, m2Expression, m3Expression);
		Matchable m = MatchAnyOf.builder().anyOf(matches).build();
		assertEquals(MatchResult.MATCH, m.match(context));
		order.verify(m1Expression).evaluate(context);
		order.verify(m2Expression).evaluate(context);
		order.verify(m3Expression).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(m1Expression, times(1)).evaluate(context);
		verify(m2Expression, times(1)).evaluate(context);
		verify(m3Expression, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Expression, m2Expression, m3Expression);
	}

	@Test
	public void testNoMatchAndAtLeastOneIndeterminate()
	{
		Expression m1Expression = ModelFixtures.reference();
		MatchAllOf m1 = ModelFixtures.allOf(m1Expression);
		Expression m2Expression = ModelFixtures.reference();
		MatchAllOf m2 = ModelFixtures.allOf(m2Expression);
		Expression m3Expression = ModelFixtures.reference();
		MatchAllOf m3 = ModelFixtures.allOf(m3Expression);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		when(m1Expression.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));
		when(m2Expression.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));
		when(m3Expression.evaluate(context)).thenThrow(ModelFixtures.evaluationError());

		InOrder order = inOrder(m1Expression, m2Expression, m3Expression);
		Matchable m = MatchAnyOf.builder().anyOf(matches).build();
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		order.verify(m1Expression).evaluate(context);
		order.verify(m2Expression).evaluate(context);
		order.verify(m3Expression).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(m1Expression, times(1)).evaluate(context);
		verify(m2Expression, times(1)).evaluate(context);
		verify(m3Expression, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Expression, m2Expression, m3Expression);
	}

	@Test
	public void testAllNoMatch()
	{
		Expression m1Expression = ModelFixtures.reference();
		MatchAllOf m1 = ModelFixtures.allOf(m1Expression);
		Expression m2Expression = ModelFixtures.reference();
		MatchAllOf m2 = ModelFixtures.allOf(m2Expression);
		Expression m3Expression = ModelFixtures.reference();
		MatchAllOf m3 = ModelFixtures.allOf(m3Expression);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		when(m1Expression.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));
		when(m2Expression.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));
		when(m3Expression.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));

		InOrder order = inOrder(m1Expression, m2Expression, m3Expression);
		Matchable m = MatchAnyOf.builder().anyOf(matches).build();
		assertEquals(MatchResult.NOMATCH, m.match(context));
		order.verify(m1Expression).evaluate(context);
		order.verify(m2Expression).evaluate(context);
		order.verify(m3Expression).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(m1Expression, times(1)).evaluate(context);
		verify(m2Expression, times(1)).evaluate(context);
		verify(m3Expression, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Expression, m2Expression, m3Expression);
	}

	@Test
	public void testObjectMethods() {
		Expression m1Expression = ModelFixtures.reference();
		MatchAllOf m1 = ModelFixtures.allOf(m1Expression);
		Expression m2Expression = ModelFixtures.reference();
		MatchAllOf m2 = ModelFixtures.allOf(m2Expression);
		Expression m3Expression = ModelFixtures.reference();
		MatchAllOf m3 = ModelFixtures.allOf(m3Expression);

		Matchable anyOf1 = MatchAnyOf.builder().anyOf(m1, m2).build();
		Matchable anyOf2 = MatchAnyOf.builder().anyOf(m1, m2).build();
		Matchable anyOf3 = MatchAnyOf.builder().anyOf(m3).build();

		assertTrue(anyOf1.equals(anyOf2));
		assertFalse(anyOf1.equals(anyOf3));
		assertFalse(anyOf1.equals(m3));
		assertFalse(anyOf1.equals(null));

		assertEquals(anyOf1.hashCode(), anyOf2.hashCode());
		assertEquals(anyOf1.toString(), anyOf2.toString());
		verifyNoMoreInteractions(m1Expression, m2Expression, m3Expression);
	}
}
