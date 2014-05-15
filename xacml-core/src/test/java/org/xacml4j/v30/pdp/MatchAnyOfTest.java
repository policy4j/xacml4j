package org.xacml4j.v30.pdp;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;


public class MatchAnyOfTest
{
	private Collection<MatchAllOf> matches;
	private EvaluationContext context;
	private IMocksControl ctl;

	@Before
	public void init(){
		this.matches = new LinkedList<MatchAllOf>();
		this.ctl = createStrictControl();
		this.context = ctl.createMock(EvaluationContext.class);
	}

	@Test
	public void testAtLeastOneMatch1()
	{
		MatchAllOf m1 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m2 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m3 = ctl.createMock(MatchAllOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);

		ctl.replay();
		Matchable m = MatchAnyOf.builder().anyOf(matches).build();
		assertEquals(MatchResult.MATCH, m.match(context));
		ctl.verify();
	}

	@Test
	public void testAtLeastOneMatch2()
	{
		MatchAllOf m1 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m2 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m3 = ctl.createMock(MatchAllOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		expect(m3.match(context)).andReturn(MatchResult.MATCH);

		ctl.replay();
		Matchable m = MatchAnyOf.builder().anyOf(matches).build();
		assertEquals(MatchResult.MATCH, m.match(context));
		ctl.verify();
	}

	@Test
	public void testNoMatchAndAtLeastOneIndeterminate()
	{
		MatchAllOf m1 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m2 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m3 = ctl.createMock(MatchAllOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);
		expect(m2.match(context)).andReturn(MatchResult.NOMATCH);
		expect(m3.match(context)).andReturn(MatchResult.INDETERMINATE);

		ctl.replay();
		Matchable m = MatchAnyOf.builder().anyOf(matches).build();
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		ctl.verify();
	}

	@Test
	public void testAllNoMatch()
	{
		MatchAllOf m1 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m2 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m3 = ctl.createMock(MatchAllOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);
		expect(m2.match(context)).andReturn(MatchResult.NOMATCH);
		expect(m3.match(context)).andReturn(MatchResult.NOMATCH);

		ctl.replay();
		Matchable m = MatchAnyOf.builder().anyOf(matches).build();
		assertEquals(MatchResult.NOMATCH, m.match(context));
		ctl.verify();
	}

	@Test
	public void testObjectMethods() {
		MatchAllOf m1 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m2 = ctl.createMock(MatchAllOf.class);
		MatchAllOf m3 = ctl.createMock(MatchAllOf.class);

		Matchable anyOf1 = MatchAnyOf.builder().anyOf(m1, m2).build();
		Matchable anyOf2 = MatchAnyOf.builder().anyOf(m1, m2).build();
		Matchable anyOf3 = MatchAnyOf.builder().anyOf(m3).build();

		ctl.replay();
		assertTrue(anyOf1.equals(anyOf2));
		assertFalse(anyOf1.equals(anyOf3));
		assertFalse(anyOf1.equals(m3));
		assertFalse(anyOf1.equals(null));

		assertEquals(anyOf1.hashCode(), anyOf2.hashCode());
		assertEquals(anyOf1.toString(), anyOf2.toString());
		ctl.verify();
	}
}
