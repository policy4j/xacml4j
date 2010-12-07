
package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.Match;
import com.artagon.xacml.v3.policy.MatchAllOf;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.Matchable;

public class MatchAllOfTest
{
	private Collection<Match> matches;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.matches = new LinkedList<Match>();
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	
	@Test
	public void testNoMatchStopsEvaluation()
	{
		Match m1 = createStrictMock(Match.class);
		Match m2 = createStrictMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);
		replay(m1, m2);
		Matchable m = new MatchAllOf(matches);
		assertEquals(MatchResult.NOMATCH, m.match(context));
		verify(m1, m2);
	}
	
	@Test
	public void testAllMatch()
	{
		Match m1 = createStrictMock(Match.class);
		Match m2 = createStrictMock(Match.class);
		Match m3 = createStrictMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.MATCH);
		expect(m3.match(context)).andReturn(MatchResult.MATCH);
		replay(m1, m2, m3);
		Matchable m = new MatchAllOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
		verify(m1, m2, m3);
	}
	
	@Test
	public void testAtLeastOneNoMatch1()
	{
		Match m1 = createStrictMock(Match.class);
		Match m2 = createStrictMock(Match.class);
		Match m3 = createStrictMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.NOMATCH);
		replay(m1, m2, m3);
		Matchable m = new MatchAllOf(matches);
		assertEquals(MatchResult.NOMATCH, m.match(context));
		verify(m1, m2, m3);
	}
	
	@Test
	public void testAtLeastOneNoMatchAndIndeterminate()
	{
		Match m1 = createStrictMock(Match.class);
		Match m2 = createStrictMock(Match.class);
		Match m3 = createStrictMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);;
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		expect(m3.match(context)).andReturn(MatchResult.NOMATCH);
		replay(m1, m2, m3);
		Matchable m = new MatchAllOf(matches);
		assertEquals(MatchResult.NOMATCH, m.match(context));
		verify(m1, m2, m3);
	}
	
	@Test
	public void testAllMatchAndAtLeastOneIndeterminate()
	{
		Match m1 = createStrictMock(Match.class);
		Match m2 = createStrictMock(Match.class);
		Match m3 = createStrictMock(Match.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		expect(m3.match(context)).andReturn(MatchResult.MATCH);
		replay(m1, m2, m3);
		Matchable m = new MatchAllOf(matches);
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		verify(m1, m2, m3);
	}

}

