package com.artagon.xacml.v3.policy.impl;

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
import com.artagon.xacml.v3.MatchAnyOf;
import com.artagon.xacml.v3.MatchResult;
import com.artagon.xacml.v3.Target;

public class TargetTest
{
	private Collection<MatchAnyOf> matches;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.matches = new LinkedList<MatchAnyOf>();
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testAllMatch(){
		MatchAnyOf m1 = createStrictMock(MatchAnyOf.class);
		MatchAnyOf m2 = createStrictMock(MatchAnyOf.class);
		matches.add(m1);
		matches.add(m2);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.MATCH);
		replay(m1, m2);
		Target t = new Target(matches);
		assertEquals(MatchResult.MATCH, t.match(context));
		verify(m1, m2);
	}
	
	@Test
	public void testEmptyTarget(){
		Target t = new Target(matches);
		assertEquals(MatchResult.MATCH, t.match(context));
		t = new Target();
		assertEquals(MatchResult.MATCH, t.match(context));
	}
	
		
	@Test
	public void testAtLeastOneNoMatch()
	{
		MatchAnyOf m1 = createStrictMock(MatchAnyOf.class);
		MatchAnyOf m2 = createStrictMock(MatchAnyOf.class);
		MatchAnyOf m3 = createStrictMock(MatchAnyOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		replay(m1, m2, m3);
		Target t = new Target(matches);
		assertEquals(MatchResult.INDETERMINATE, t.match(context));
		verify(m1, m2, m3);
	}
	
	@Test
	public void testAllMatchAndIndeterminate()
	{
		MatchAnyOf m1 = createStrictMock(MatchAnyOf.class);
		MatchAnyOf m2 = createStrictMock(MatchAnyOf.class);
		MatchAnyOf m3 = createStrictMock(MatchAnyOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		replay(m1, m2, m3);
		Target t = new Target(matches);
		assertEquals(MatchResult.INDETERMINATE, t.match(context));
		verify(m1, m2, m3);
	}
	
	@Test
	public void testFirstIsNoMatch()
	{
		MatchAnyOf m1 = createStrictMock(MatchAnyOf.class);
		MatchAnyOf m2 = createStrictMock(MatchAnyOf.class);
		MatchAnyOf m3 = createStrictMock(MatchAnyOf.class);
		matches.add(m1);
		matches.add(m2);
		matches.add(m3);
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);
		replay(m1, m2, m3);
		Target t = new Target(matches);
		assertEquals(MatchResult.NOMATCH, t.match(context));
		verify(m1, m2, m3);
	}
	
}
