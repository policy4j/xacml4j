package org.xacml4j.v30.pdp;

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

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;

import java.util.Collection;
import java.util.LinkedList;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;


public class TargetTest
{
	private Collection<MatchAnyOf> matches;
	private EvaluationContext context;
	private IMocksControl c;

	@Before
	public void init(){
		this.matches = new LinkedList<MatchAnyOf>();
		this.c = createStrictControl();
		this.context = c.createMock(EvaluationContext.class);
	}

	@Test
	public void testAllMatch(){
		MatchAnyOf m1 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m2 = c.createMock(MatchAnyOf.class);
		matches.add(m1);
		matches.add(m2);
		Capture<EvaluationContext> c1 = new Capture<EvaluationContext>();
		Capture<EvaluationContext> c2 = new Capture<EvaluationContext>();
		expect(m1.match(capture(c1))).andReturn(MatchResult.MATCH);
		expect(m2.match(capture(c2))).andReturn(MatchResult.MATCH);
		c.replay();
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.MATCH, t.match(context));
		c.verify();
	}

	@Test
	public void testEmptyTarget()
	{
		c.replay();
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.MATCH, t.match(context));
		c.verify();
	}


	@Test
	public void testFirstMatchSecondIsNoMatch()
	{
		MatchAnyOf m1 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m2 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m3 = c.createMock(MatchAnyOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		Capture<EvaluationContext> c1 = new Capture<EvaluationContext>();
		Capture<EvaluationContext> c2 = new Capture<EvaluationContext>();
		expect(m1.match(capture(c1))).andReturn(MatchResult.MATCH);
		expect(m2.match(capture(c2))).andReturn(MatchResult.NOMATCH);
		c.replay();
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.NOMATCH, t.match(context));
		c.verify();
	}

	@Test
	public void testFirstMatchAndSecondIsIndeterminate()
	{
		MatchAnyOf m1 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m2 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m3 = c.createMock(MatchAnyOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		Capture<EvaluationContext> c1 = new Capture<EvaluationContext>();
		Capture<EvaluationContext> c2 = new Capture<EvaluationContext>();
		expect(m1.match(capture(c1))).andReturn(MatchResult.MATCH);
		expect(m2.match(capture(c2))).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.INDETERMINATE, t.match(context));
		c.verify();
	}

	@Test
	public void testFirstIsNoMatch()
	{
		MatchAnyOf m1 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m2 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m3 = c.createMock(MatchAnyOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		Capture<EvaluationContext> c1 = new Capture<EvaluationContext>();
		expect(m1.match(capture(c1))).andReturn(MatchResult.NOMATCH);
		c.replay();
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.NOMATCH, t.match(context));
		c.verify();
	}

	@Test
	public void testFirstIsIndeterminate()
	{
		MatchAnyOf m1 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m2 = c.createMock(MatchAnyOf.class);
		matches.add(m1);
		matches.add(m2);
		Capture<EvaluationContext> c1 = new Capture<EvaluationContext>();
		expect(m1.match(capture(c1))).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		Target t = Target.builder().anyOf(matches).build();
		assertEquals(MatchResult.INDETERMINATE, t.match(context));
		c.verify();
	}

	@Test
	public void testObjectMethods() {
		MatchAnyOf m1 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m2 = c.createMock(MatchAnyOf.class);
		MatchAnyOf m3 = c.createMock(MatchAnyOf.class);

		Target t1 = Target.builder().anyOf(m1, m2).build();
		Target t2 = Target.builder().anyOf(m1, m2).build();
		Target t3 = Target.builder().anyOf(m3, m2).build();

		c.replay();
		assertTrue(t1.equals(t2));
		assertFalse(t1.equals(t3));
		assertFalse(t1.equals(m3));
		assertFalse(t1.equals(null));

		assertEquals(t1.hashCode(), t2.hashCode());
		assertEquals(t1.toString(), t2.toString());
		c.verify();
	}
}
