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
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;


@ExtendWith(MockitoExtension.class)
public class FirstApplicableTest
{

	private List<DecisionRule> decisions;
	private DecisionCombiningAlgorithm<DecisionRule> algorithm;
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.decisions = new LinkedList<DecisionRule>();
		this.algorithm = new FirstApplicable<DecisionRule>("test");
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testNoDecisions()
	{
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(context, decisions));
	}


	@Test
	public void testNoApplicableDecisions() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		DecisionRule r2 = mock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.NOT_APPLICABLE);
		when(r2.createContext(context)).thenReturn(context);
		when(r2.evaluate(context)).thenReturn(Decision.NOT_APPLICABLE);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verify(r2).createContext(context);
		order.verify(r2).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}

	@Test
	public void testPermit() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		DecisionRule r2 = mock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.PERMIT);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}

	@Test
	public void testDeny() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		DecisionRule r2 = mock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.DENY);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}


	@Test
	public void testIndeterminate() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		DecisionRule r2 = mock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.INDETERMINATE);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.INDETERMINATE, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}

	@Test
	public void testIndeterminateD() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		DecisionRule r2 = mock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.INDETERMINATE_D);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.INDETERMINATE_D, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}

	@Test
	public void testIndeterminateP() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		DecisionRule r2 = mock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.INDETERMINATE_P);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.INDETERMINATE_P, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}

	@Test
	public void testIndeterminateDP() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		DecisionRule r2 = mock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.INDETERMINATE_DP);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}
}
