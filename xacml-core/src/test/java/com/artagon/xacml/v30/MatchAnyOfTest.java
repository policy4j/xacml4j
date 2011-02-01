package com.artagon.xacml.v30;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class MatchAnyOfTest
{
	private Collection<MatchAllOf> matches;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.matches = new LinkedList<MatchAllOf>();
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testAtLeastOneMatch1()
	{
		MatchAllOf m1 = createStrictMock(MatchAllOf.class);
		MatchAllOf m2 = createStrictMock(MatchAllOf.class);
		MatchAllOf m3 = createStrictMock(MatchAllOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);;
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		replay(m1, m2, m3);
		Matchable m = new MatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
		verify(m1, m2, m3);
	}
	
	@Test
	public void testAtLeastOneMatch2()
	{
		MatchAllOf m1 = createStrictMock(MatchAllOf.class);
		MatchAllOf m2 = createStrictMock(MatchAllOf.class);
		MatchAllOf m3 = createStrictMock(MatchAllOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);;
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		expect(m3.match(context)).andReturn(MatchResult.MATCH);
		replay(m1, m2, m3);
		Matchable m = new MatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
		verify(m1, m2, m3);
	}
	
	@Test
	public void testNoMatchAndAtLeastOneIndeterminate()
	{
		MatchAllOf m1 = createStrictMock(MatchAllOf.class);
		MatchAllOf m2 = createStrictMock(MatchAllOf.class);
		MatchAllOf m3 = createStrictMock(MatchAllOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);;
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);
		expect(m3.match(context)).andReturn(MatchResult.INDETERMINATE);
		expect(m2.match(context)).andReturn(MatchResult.NOMATCH);
		replay(m1, m2, m3);
		Matchable m = new MatchAnyOf(matches);
		assertEquals(MatchResult.INDETERMINATE, m.match(context));
		verify(m1, m2, m3);
	}
	
	@Test
	public void testAllNoMatch()
	{
		MatchAllOf m1 = createStrictMock(MatchAllOf.class);
		MatchAllOf m2 = createStrictMock(MatchAllOf.class);
		MatchAllOf m3 = createStrictMock(MatchAllOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);;
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);
		expect(m2.match(context)).andReturn(MatchResult.NOMATCH);
		expect(m3.match(context)).andReturn(MatchResult.NOMATCH);
		replay(m1, m2, m3);
		Matchable m = new MatchAnyOf(matches);
		assertEquals(MatchResult.NOMATCH, m.match(context));
		verify(m1, m2, m3);
	}
	
}
