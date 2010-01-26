package com.artagon.xacml.v30.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.policy.MatchAnyOf;
import com.artagon.xacml.v30.policy.MatchResult;
import com.artagon.xacml.v30.policy.Matchable;

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
		Matchable m = new MatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
		matches.add(new MockMatchable(MatchResult.MATCH));
		m = new MatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
		matches.add(new MockMatchable(MatchResult.MATCH));
		m = new MatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
	
	@Test
	public void testFirstMatchRestNoMatch()
	{
		matches.add(new MockMatchable(MatchResult.MATCH));
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		Matchable m = new MatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
	
	@Test
	public void testFirstNoMatchRestMatch()
	{
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		matches.add(new MockMatchable(MatchResult.MATCH));
		Matchable m = new MatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
	
	@Test
	public void testFirstNoMatchAllMatch()
	{
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		Matchable m = new MatchAnyOf(matches);
		assertEquals(MatchResult.NOMATCH, m.match(context));
		matches.add(new MockMatchable(MatchResult.MATCH));
		m = new MatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
	
	@Test
	public void testMixedMatchNoMatchAndOneIndeterminate()
	{
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		matches.add(new MockMatchable(MatchResult.INDETERMINATE));
		Matchable m = new MatchAnyOf(matches);
		matches.add(new MockMatchable(MatchResult.MATCH));
		m = new MatchAnyOf(matches);
		assertEquals(MatchResult.MATCH, m.match(context));
	}
}
