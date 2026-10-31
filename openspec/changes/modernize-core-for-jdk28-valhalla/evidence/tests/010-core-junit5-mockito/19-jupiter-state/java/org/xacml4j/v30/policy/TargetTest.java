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

import org.mockito.ArgumentCaptor;
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
public class TargetTest
{
	private Collection<MatchAnyOf> matches;
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.matches = new LinkedList<MatchAnyOf>();
		this.context = ModelFixtures.context();
	}

	@Test
	public void testAllMatch(){
		AttributeReference m1Reference = ModelFixtures.reference();
		MatchAnyOf m1 = ModelFixtures.anyOf(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		MatchAnyOf m2 = ModelFixtures.anyOf(m2Reference);
		matches.add(m1);
		matches.add(m2);
		ArgumentCaptor<EvaluationContext> c1 = ArgumentCaptor.forClass(EvaluationContext.class);
		ArgumentCaptor<EvaluationContext> c2 = ArgumentCaptor.forClass(EvaluationContext.class);
		when(m1Reference.evaluate(c1.capture())).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(m2Reference.evaluate(c2.capture())).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		InOrder order = inOrder(m1Reference, m2Reference);
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.MATCH, t.match(context));
		order.verify(m1Reference).evaluate(c1.capture());
		order.verify(m2Reference).evaluate(c2.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference);
	}

	@Test
	public void testEmptyTarget()
	{
		
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.MATCH, t.match(context));
		
	}


	@Test
	public void testFirstMatchSecondIsNoMatch()
	{
		AttributeReference m1Reference = ModelFixtures.reference();
		MatchAnyOf m1 = ModelFixtures.anyOf(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		MatchAnyOf m2 = ModelFixtures.anyOf(m2Reference);
		AttributeReference m3Reference = ModelFixtures.reference();
		MatchAnyOf m3 = ModelFixtures.anyOf(m3Reference);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		ArgumentCaptor<EvaluationContext> c1 = ArgumentCaptor.forClass(EvaluationContext.class);
		ArgumentCaptor<EvaluationContext> c2 = ArgumentCaptor.forClass(EvaluationContext.class);
		when(m1Reference.evaluate(c1.capture())).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(m2Reference.evaluate(c2.capture())).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));
		InOrder order = inOrder(m1Reference, m2Reference, m3Reference);
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.NOMATCH, t.match(context));
		order.verify(m1Reference).evaluate(c1.capture());
		order.verify(m2Reference).evaluate(c2.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference, m3Reference);
	}

	@Test
	public void testFirstMatchAndSecondIsIndeterminate()
	{
		AttributeReference m1Reference = ModelFixtures.reference();
		MatchAnyOf m1 = ModelFixtures.anyOf(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		MatchAnyOf m2 = ModelFixtures.anyOf(m2Reference);
		AttributeReference m3Reference = ModelFixtures.reference();
		MatchAnyOf m3 = ModelFixtures.anyOf(m3Reference);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		ArgumentCaptor<EvaluationContext> c1 = ArgumentCaptor.forClass(EvaluationContext.class);
		ArgumentCaptor<EvaluationContext> c2 = ArgumentCaptor.forClass(EvaluationContext.class);
		when(m1Reference.evaluate(c1.capture())).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(m2Reference.evaluate(c2.capture())).thenThrow(ModelFixtures.evaluationError());
		InOrder order = inOrder(m1Reference, m2Reference, m3Reference);
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.INDETERMINATE, t.match(context));
		order.verify(m1Reference).evaluate(c1.capture());
		order.verify(m2Reference).evaluate(c2.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference, m3Reference);
	}

	@Test
	public void testFirstIsNoMatch()
	{
		AttributeReference m1Reference = ModelFixtures.reference();
		MatchAnyOf m1 = ModelFixtures.anyOf(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		MatchAnyOf m2 = ModelFixtures.anyOf(m2Reference);
		AttributeReference m3Reference = ModelFixtures.reference();
		MatchAnyOf m3 = ModelFixtures.anyOf(m3Reference);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		ArgumentCaptor<EvaluationContext> c1 = ArgumentCaptor.forClass(EvaluationContext.class);
		when(m1Reference.evaluate(c1.capture())).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));
		InOrder order = inOrder(m1Reference, m2Reference, m3Reference);
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.NOMATCH, t.match(context));
		order.verify(m1Reference).evaluate(c1.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference, m3Reference);
	}

	@Test
	public void testFirstIsIndeterminate()
	{
		AttributeReference m1Reference = ModelFixtures.reference();
		MatchAnyOf m1 = ModelFixtures.anyOf(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		MatchAnyOf m2 = ModelFixtures.anyOf(m2Reference);
		matches.add(m1);
		matches.add(m2);
		ArgumentCaptor<EvaluationContext> c1 = ArgumentCaptor.forClass(EvaluationContext.class);
		when(m1Reference.evaluate(c1.capture())).thenThrow(ModelFixtures.evaluationError());
		InOrder order = inOrder(m1Reference, m2Reference);
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.INDETERMINATE, t.match(context));
		order.verify(m1Reference).evaluate(c1.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(m1Reference, m2Reference);
	}

	@Test
	public void testObjectMethods() {
		AttributeReference m1Reference = ModelFixtures.reference();
		MatchAnyOf m1 = ModelFixtures.anyOf(m1Reference);
		AttributeReference m2Reference = ModelFixtures.reference();
		MatchAnyOf m2 = ModelFixtures.anyOf(m2Reference);
		AttributeReference m3Reference = ModelFixtures.reference();
		MatchAnyOf m3 = ModelFixtures.anyOf(m3Reference);

		Target t1 = Target.builder().anyOf(m1, m2).build();
		Target t2 = Target.builder().anyOf(m1, m2).build();
		Target t3 = Target.builder().anyOf(m3, m2).build();

		assertTrue(t1.equals(t2));
		assertFalse(t1.equals(t3));
		assertFalse(t1.equals(m3));
		assertFalse(t1.equals(null));

		assertEquals(t1.hashCode(), t2.hashCode());
		assertEquals(t1.toString(), t2.toString());
		verifyNoMoreInteractions(m1Reference, m2Reference, m3Reference);
	}
}
