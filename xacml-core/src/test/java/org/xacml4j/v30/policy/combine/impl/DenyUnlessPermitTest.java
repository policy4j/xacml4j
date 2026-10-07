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
public class DenyUnlessPermitTest
{
	private List<DecisionRule> decisions;
	private DecisionCombiningAlgorithm<DecisionRule> algorithm;
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.decisions = new LinkedList<DecisionRule>();
		this.algorithm = new DenyUnlessPermit<DecisionRule>("aaaa");
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testCombineWithNoDecisions()
	{
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
	}

	@Test
	public void testCombineWithPermit() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		decisions.add(r1);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.PERMIT);
		InOrder order = inOrder(context, r1);
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1);
	}

	@Test
	public void testCombineWithNotApplicable() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		decisions.add(r1);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.NOT_APPLICABLE);
		InOrder order = inOrder(context, r1);
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1);
	}

	@Test
	public void testCombineWithIndeterminate() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		decisions.add(r1);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.INDETERMINATE);
		InOrder order = inOrder(context, r1);
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1);
	}


	@Test
	public void testCombineWithIndeterminateD() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		decisions.add(r1);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.INDETERMINATE_D);
		InOrder order = inOrder(context, r1);
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1);
	}

	@Test
	public void testCombineWithIndeterminateP() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		decisions.add(r1);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.INDETERMINATE_P);
		InOrder order = inOrder(context, r1);
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1);
	}

	@Test
	public void testCombineWithIndeterminateDP() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		decisions.add(r1);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.INDETERMINATE_DP);
		InOrder order = inOrder(context, r1);
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1);
	}

	@Test
	public void testCombineWithDenyAndPermit() throws EvaluationException
	{
		DecisionRule r1 = mock(DecisionRule.class);
		DecisionRule r2 = mock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.DENY);
		when(r2.createContext(context)).thenReturn(context);
		when(r2.evaluate(context)).thenReturn(Decision.PERMIT);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verify(r2).createContext(context);
		order.verify(r2).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		verify(r2, times(1)).createContext(context);
		verify(r2, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}
}
