
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
import org.xacml4j.v30.policy.AttributeReference;
import org.xacml4j.v30.MatchResult;


@ExtendWith(MockitoExtension.class)
public class MatchAllOfTest
{
	private Collection<Match> matches;
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.matches = new LinkedList<Match>();
		this.context = ModelFixtures.context();
	}


	@Test
	public void testNoMatchStopsEvaluation()
	{
		AttributeReference m1Reference = ModelFixtures.reference();
		Match m1 = ModelFixtures.match(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		Match m2 = ModelFixtures.match(m2Reference);
		matches.add(m1);
		matches.add(m2);
		when(m1Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));

		InOrder order = inOrder(m1Reference, m2Reference);
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.NOMATCH, m.match(context));
		order.verify(m1Reference).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference);
	}

	@Test
	public void testAllMatch()
	{
		AttributeReference m1Reference = ModelFixtures.reference();
		Match m1 = ModelFixtures.match(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		Match m2 = ModelFixtures.match(m2Reference);
		AttributeReference m3Reference = ModelFixtures.reference();
		Match m3 = ModelFixtures.match(m3Reference);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		when(m1Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(m2Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(m3Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));

		InOrder order = inOrder(m1Reference, m2Reference, m3Reference);
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.MATCH, m.match(context));
		order.verify(m1Reference).evaluate(context);
		order.verify(m2Reference).evaluate(context);
		order.verify(m3Reference).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference, m3Reference);
	}

	@Test
	public void testAtLeastOneNoMatch1()
	{
		AttributeReference m1Reference = ModelFixtures.reference();
		Match m1 = ModelFixtures.match(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		Match m2 = ModelFixtures.match(m2Reference);
		AttributeReference m3Reference = ModelFixtures.reference();
		Match m3 = ModelFixtures.match(m3Reference);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		when(m1Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(m2Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));

		InOrder order = inOrder(m1Reference, m2Reference, m3Reference);
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.NOMATCH, m.match(context));
		order.verify(m1Reference).evaluate(context);
		order.verify(m2Reference).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference, m3Reference);
	}

	@Test
	public void testAtLeastOneNoMatchAndIndeterminate()
	{
		AttributeReference m1Reference = ModelFixtures.reference();
		Match m1 = ModelFixtures.match(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		Match m2 = ModelFixtures.match(m2Reference);
		AttributeReference m3Reference = ModelFixtures.reference();
		Match m3 = ModelFixtures.match(m3Reference);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		when(m1Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(m2Reference.evaluate(context)).thenThrow(ModelFixtures.evaluationError());
		when(m3Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));

		InOrder order = inOrder(m1Reference, m2Reference, m3Reference);
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.NOMATCH, m.match(context));
		order.verify(m1Reference).evaluate(context);
		order.verify(m2Reference).evaluate(context);
		order.verify(m3Reference).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference, m3Reference);
	}

	@Test
	public void testAllMatchAndAtLeastOneIndeterminate()
	{
		AttributeReference m1Reference = ModelFixtures.reference();
		Match m1 = ModelFixtures.match(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		Match m2 = ModelFixtures.match(m2Reference);
		AttributeReference m3Reference = ModelFixtures.reference();
		Match m3 = ModelFixtures.match(m3Reference);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		when(m1Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(m2Reference.evaluate(context)).thenThrow(ModelFixtures.evaluationError());
		when(m3Reference.evaluate(context)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));

		InOrder order = inOrder(m1Reference, m2Reference, m3Reference);
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		order.verify(m1Reference).evaluate(context);
		order.verify(m2Reference).evaluate(context);
		order.verify(m3Reference).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference, m3Reference);
	}

	@Test
	public void testObjectMethods() {
		AttributeReference m1Reference = ModelFixtures.reference();
		Match m1 = ModelFixtures.match(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		Match m2 = ModelFixtures.match(m2Reference);
		AttributeReference m3Reference = ModelFixtures.reference();
		Match m3 = ModelFixtures.match(m3Reference);

		Matchable allOf1 = MatchAllOf.builder().allOf(m1, m2).build();
		Matchable allOf2 = MatchAllOf.builder().allOf(m1, m2).build();
		Matchable allOf3 = MatchAllOf.builder().allOf(m3).build();

		assertTrue(allOf1.equals(allOf2));
		assertFalse(allOf1.equals(allOf3));
		assertFalse(allOf1.equals(m3));
		assertFalse(allOf1.equals(null));

		assertEquals(allOf1.hashCode(), allOf2.hashCode());
		assertEquals(allOf1.toString(), allOf2.toString());
		verifyNoMoreInteractions(m1Reference, m2Reference, m3Reference);
	}
}

