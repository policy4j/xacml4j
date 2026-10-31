package org.xacml4j.v30.policy.combine.impl;

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

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;


@ExtendWith(MockitoExtension.class)
public class OnlyOneApplicablePolicyCombiningAlgorithmTest
{
	private OnlyOneApplicablePolicyCombiningAlgorithm c;
	private List<CompositeDecisionRule> d;
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.c = new OnlyOneApplicablePolicyCombiningAlgorithm();
		this.d = new LinkedList<CompositeDecisionRule>();
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testDecisionIsNoMatchContinueEvaluation() throws EvaluationException
	{
		CompositeDecisionRule d1 = mock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = mock(CompositeDecisionRule.class);
		EvaluationContext c1 = mock(EvaluationContext.class);
		EvaluationContext c2 = mock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		when(d1.createContext(context)).thenReturn(c1);
		when(d1.isMatch(c1)).thenReturn(MatchResult.NOMATCH);
		when(d2.createContext(context)).thenReturn(c2);
		when(d2.isMatch(c2)).thenReturn(MatchResult.NOMATCH);
		InOrder order = inOrder(context, d1, d2, c1, c2);
		assertEquals(Decision.NOT_APPLICABLE, c.combine(context, d));
		order.verify(d1).createContext(context);
		order.verify(d1).isMatch(c1);
		order.verify(d2).createContext(context);
		order.verify(d2).isMatch(c2);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, d1, d2, c1, c2);
	}

	@Test
	public void testDecisionIndeterminateStopsEvaluation() throws EvaluationException
	{
		CompositeDecisionRule d1 = mock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = mock(CompositeDecisionRule.class);
		EvaluationContext c1 = mock(EvaluationContext.class);
		EvaluationContext c2 = mock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		when(d1.createContext(context)).thenReturn(c1);
		when(d1.isMatch(c1)).thenReturn(MatchResult.INDETERMINATE);
		InOrder order = inOrder(context, d1, d2, c1, c2);
		assertEquals(Decision.INDETERMINATE, c.combine(context, d));
		order.verify(d1).createContext(context);
		order.verify(d1).isMatch(c1);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, d1, d2, c1, c2);
	}

	@Test
	public void testMoreThanOneIsApplicable() throws EvaluationException
	{
		CompositeDecisionRule d1 = mock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = mock(CompositeDecisionRule.class);
		EvaluationContext c1 = mock(EvaluationContext.class);
		EvaluationContext c2 = mock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		when(d1.createContext(context)).thenReturn(c1);
		when(d1.isMatch(c1)).thenReturn(MatchResult.MATCH);
		when(d2.createContext(context)).thenReturn(c2);
		when(d2.isMatch(c2)).thenReturn(MatchResult.MATCH);
		InOrder order = inOrder(context, d1, d2, c1, c2);
		assertEquals(Decision.INDETERMINATE, c.combine(context, d));
		order.verify(d1).createContext(context);
		order.verify(d1).isMatch(c1);
		order.verify(d2).createContext(context);
		order.verify(d2).isMatch(c2);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, d1, d2, c1, c2);
	}

	@Test
	public void testOnlyOneIsApplicableAndDecisionIsPermit() throws EvaluationException
	{
		CompositeDecisionRule d1 = mock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = mock(CompositeDecisionRule.class);
		EvaluationContext c1 = mock(EvaluationContext.class);
		EvaluationContext c2 = mock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		when(d1.createContext(context)).thenReturn(c1);
		when(d1.isMatch(c1)).thenReturn(MatchResult.MATCH);
		when(d2.createContext(context)).thenReturn(c2);
		when(d2.isMatch(c2)).thenReturn(MatchResult.NOMATCH);
		when(d1.evaluate(c1)).thenReturn(Decision.PERMIT);
		InOrder order = inOrder(context, d1, d2, c1, c2);
		assertEquals(Decision.PERMIT, c.combine(context, d));
		order.verify(d1).createContext(context);
		order.verify(d1).isMatch(c1);
		order.verify(d2).createContext(context);
		order.verify(d2).isMatch(c2);
		order.verify(d1).evaluate(c1);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, d1, d2, c1, c2);
	}

	@Test
	public void testOnlyOneIsApplicableAndDecisionIsDeny() throws EvaluationException
	{
		CompositeDecisionRule d1 = mock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = mock(CompositeDecisionRule.class);
		EvaluationContext c1 = mock(EvaluationContext.class);
		EvaluationContext c2 = mock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		when(d1.createContext(context)).thenReturn(c1);
		when(d1.isMatch(c1)).thenReturn(MatchResult.MATCH);
		when(d2.createContext(context)).thenReturn(c2);
		when(d2.isMatch(c2)).thenReturn(MatchResult.NOMATCH);
		when(d1.evaluate(c1)).thenReturn(Decision.DENY);
		InOrder order = inOrder(context, d1, d2, c1, c2);
		assertEquals(Decision.DENY, c.combine(context, d));
		order.verify(d1).createContext(context);
		order.verify(d1).isMatch(c1);
		order.verify(d2).createContext(context);
		order.verify(d2).isMatch(c2);
		order.verify(d1).evaluate(c1);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, d1, d2, c1, c2);
	}

	@Test
	public void testOnlyOneIsApplicableAndDecisionIsIndeterminate() throws EvaluationException
	{
		CompositeDecisionRule d1 = mock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = mock(CompositeDecisionRule.class);
		EvaluationContext c1 = mock(EvaluationContext.class);
		EvaluationContext c2 = mock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		when(d1.createContext(context)).thenReturn(c1);
		when(d1.isMatch(c1)).thenReturn(MatchResult.MATCH);
		when(d2.createContext(context)).thenReturn(c2);
		when(d2.isMatch(c2)).thenReturn(MatchResult.NOMATCH);
		when(d1.evaluate(c1)).thenReturn(Decision.INDETERMINATE);
		InOrder order = inOrder(context, d1, d2, c1, c2);
		assertEquals(Decision.INDETERMINATE, c.combine(context, d));
		order.verify(d1).createContext(context);
		order.verify(d1).isMatch(c1);
		order.verify(d2).createContext(context);
		order.verify(d2).isMatch(c2);
		order.verify(d1).evaluate(c1);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, d1, d2, c1, c2);
	}
}
