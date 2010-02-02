package com.artagon.xacml.v3.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class MatchAnyOfTest extends XacmlPolicyTestCase
{
	private Collection<Matchable> matches;
	
	@Before
	public void init(){
		this.matches = new LinkedList<Matchable>();
	}
	
	@Test
	public void testAllMatch()
	{
		matches.add(new MockMatchable(MatchResult.MATCH));
		Matchable m = new DefaultMatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
		matches.add(new MockMatchable(MatchResult.MATCH));
		m = new DefaultMatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
		matches.add(new MockMatchable(MatchResult.MATCH));
		m = new DefaultMatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
	
	@Test
	public void testFirstMatchRestNoMatch()
	{
		matches.add(new MockMatchable(MatchResult.MATCH));
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		Matchable m = new DefaultMatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
	
	@Test
	public void testFirstNoMatchRestMatch()
	{
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		matches.add(new MockMatchable(MatchResult.MATCH));
		Matchable m = new DefaultMatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
	
	@Test
	public void testFirstNoMatchAllMatch()
	{
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		Matchable m = new DefaultMatchAnyOf(matches);
		assertEquals(MatchResult.NOMATCH, m.match(context));
		matches.add(new MockMatchable(MatchResult.MATCH));
		m = new DefaultMatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
	
	@Test
	public void testMixedMatchNoMatchAndOneIndeterminate()
	{
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		matches.add(new MockMatchable(MatchResult.INDETERMINATE));
		Matchable m = new DefaultMatchAnyOf(matches);
		matches.add(new MockMatchable(MatchResult.MATCH));
		m = new DefaultMatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
}
