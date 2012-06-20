package com.artagon.xacml.v30.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

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
		Target t = new Target(matches);
		assertEquals(MatchResult.MATCH, t.match(context));
		c.verify();
	}
	
	@Test
	public void testEmptyTarget()
	{
		c.replay();
		Target t = new Target(matches);
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
		Target t = new Target(matches);
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
		Target t = new Target(matches);
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
		Target t = new Target(matches);
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
		Target t = new Target(matches);
		assertEquals(MatchResult.INDETERMINATE, t.match(context));
		c.verify();
	}
}
