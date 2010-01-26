package com.artagon.xacml.v3.policy;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class DefaultTargetTest extends XacmlPolicyTestCase
{
	private Collection<Matchable> matches;
	
	@Before
	public void init(){
		this.matches = new LinkedList<Matchable>();
	}
	
	@Test
	public void testAllMatch(){
		matches.add(new MockMatchable(MatchResult.MATCH));
		matches.add(new MockMatchable(MatchResult.MATCH));
		DefaultTarget t = new DefaultTarget(matches);
		assertEquals(MatchResult.MATCH, t.match(context));
	}
	
	@Test
	public void testEmptyTarget(){
		DefaultTarget t = new DefaultTarget(matches);
		assertEquals(MatchResult.MATCH, t.match(context));
		t = new DefaultTarget();
		assertEquals(MatchResult.MATCH, t.match(context));
	}
	
	@Test
	public void testAllNoMatch(){
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		DefaultTarget t = new DefaultTarget(matches);
		assertEquals(MatchResult.NOMATCH, t.match(context));
	}
	
	@Test
	public void testAtLeastOneNoMatch(){
		matches.add(new MockMatchable(MatchResult.MATCH));
		matches.add(new MockMatchable(MatchResult.INDETERMINATE));
		matches.add(new MockMatchable(MatchResult.NOMATCH));
		DefaultTarget t = new DefaultTarget(matches);
		assertEquals(MatchResult.NOMATCH, t.match(context));
	}
	
	@Test
	public void testAllMatchAndIndeterminate(){
		matches.add(new MockMatchable(MatchResult.MATCH));
		matches.add(new MockMatchable(MatchResult.INDETERMINATE));
		matches.add(new MockMatchable(MatchResult.MATCH));
		DefaultTarget t = new DefaultTarget(matches);
		assertEquals(MatchResult.INDETERMINATE, t.match(context));
	}
	
}
