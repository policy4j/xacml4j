package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.MatchAnyOf;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.Target;

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
		Capture<EvaluationContext> c1 = new Capture<EvaluationContext>();
		Capture<EvaluationContext> c2 = new Capture<EvaluationContext>();
		expect(m1.match(capture(c1))).andReturn(MatchResult.MATCH);
		expect(m2.match(capture(c2))).andReturn(MatchResult.MATCH);
		replay(m1, m2, context);
		Target t = new Target(matches);
		assertEquals(MatchResult.MATCH, t.match(context));
		verify(m1, m2, context);
	}
	
	@Test
	public void testEmptyTarget()
	{
		replay(context);
		Target t = new Target(matches);
		assertEquals(MatchResult.MATCH, t.match(context));
		verify(context);
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
		Capture<EvaluationContext> c1 = new Capture<EvaluationContext>();
		Capture<EvaluationContext> c2 = new Capture<EvaluationContext>();
		expect(m1.match(capture(c1))).andReturn(MatchResult.MATCH);
		expect(m2.match(capture(c2))).andReturn(MatchResult.NOMATCH
				);
		replay(m1, m2, m3, context);
		Target t = new Target(matches);
		assertEquals(MatchResult.NOMATCH, t.match(context));
		verify(m1, m2, m3, context);
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
		Capture<EvaluationContext> c1 = new Capture<EvaluationContext>();
		Capture<EvaluationContext> c2 = new Capture<EvaluationContext>();
		expect(m1.match(capture(c1))).andReturn(MatchResult.MATCH);
		expect(m2.match(capture(c2))).andReturn(MatchResult.NOMATCH);
		replay(m1, m2, m3, context);
		Target t = new Target(matches);
		assertEquals(MatchResult.NOMATCH, t.match(context));
		verify(m1, m2, m3, context);
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
		Capture<EvaluationContext> c1 = new Capture<EvaluationContext>();
		expect(m1.match(capture(c1))).andReturn(MatchResult.NOMATCH);
		replay(m1, m2, m3, context);
		Target t = new Target(matches);
		assertEquals(MatchResult.NOMATCH, t.match(context));
		verify(m1, m2, m3, context);
	}
	
}
