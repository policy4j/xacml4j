
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


public class MatchAllOfTest
{
	private Collection<Match> matches;
	private EvaluationContext context;
	private IMocksControl ctl;

	@Before
	public void init(){
		this.matches = new LinkedList<Match>();
		this.ctl = createStrictControl();
		this.context = ctl.createMock(EvaluationContext.class);
	}


	@Test
	public void testNoMatchStopsEvaluation()
	{
		Match m1 = ctl.createMock(Match.class);
		Match m2 = ctl.createMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);

		ctl.replay();
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.NOMATCH, m.match(context));
		ctl.verify();
	}

	@Test
	public void testAllMatch()
	{
		Match m1 = ctl.createMock(Match.class);
		Match m2 = ctl.createMock(Match.class);
		Match m3 = ctl.createMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.MATCH);
		expect(m3.match(context)).andReturn(MatchResult.MATCH);

		ctl.replay();
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.MATCH, m.match(context));
		ctl.verify();
	}

	@Test
	public void testAtLeastOneNoMatch1()
	{
		Match m1 = ctl.createMock(Match.class);
		Match m2 = ctl.createMock(Match.class);
		Match m3 = ctl.createMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.NOMATCH);

		ctl.replay();
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.NOMATCH, m.match(context));
		ctl.verify();
	}

	@Test
	public void testAtLeastOneNoMatchAndIndeterminate()
	{
		Match m1 = ctl.createMock(Match.class);
		Match m2 = ctl.createMock(Match.class);
		Match m3 = ctl.createMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		expect(m3.match(context)).andReturn(MatchResult.NOMATCH);

		ctl.replay();
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.NOMATCH, m.match(context));
		ctl.verify();
	}

	@Test
	public void testAllMatchAndAtLeastOneIndeterminate()
	{
		Match m1 = ctl.createMock(Match.class);
		Match m2 = ctl.createMock(Match.class);
		Match m3 = ctl.createMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		expect(m3.match(context)).andReturn(MatchResult.MATCH);

		ctl.replay();
		Matchable m = MatchAllOf.builder().allOf(matches).build();
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		ctl.verify();
	}

	@Test
	public void testObjectMethods() {
		Match m1 = ctl.createMock(Match.class);
		Match m2 = ctl.createMock(Match.class);
		Match m3 = ctl.createMock(Match.class);

		Matchable allOf1 = MatchAllOf.builder().allOf(m1, m2).build();
		Matchable allOf2 = MatchAllOf.builder().allOf(m1, m2).build();
		Matchable allOf3 = MatchAllOf.builder().allOf(m3).build();

		ctl.replay();
		assertTrue(allOf1.equals(allOf2));
		assertFalse(allOf1.equals(allOf3));
		assertFalse(allOf1.equals(m3));
		assertFalse(allOf1.equals(null));

		assertEquals(allOf1.hashCode(), allOf2.hashCode());
		assertEquals(allOf1.toString(), allOf2.toString());
		ctl.verify();
	}
}

