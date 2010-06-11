package com.artagon.xacml.v3;

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
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_AND_LOCATORS);
		context.setAttributeResolutionScope(AttributeResolutionScope.REQUEST_ONLY);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.MATCH);
		context.setAttributeResolutionScope(AttributeResolutionScope.REQUEST_AND_LOCATORS);
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
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_AND_LOCATORS);
		context.setAttributeResolutionScope(AttributeResolutionScope.REQUEST_ONLY);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		context.setAttributeResolutionScope(AttributeResolutionScope.REQUEST_AND_LOCATORS);
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
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_AND_LOCATORS);
		context.setAttributeResolutionScope(AttributeResolutionScope.REQUEST_ONLY);
		expect(m1.match(context)).andReturn(MatchResult.MATCH);
		expect(m2.match(context)).andReturn(MatchResult.INDETERMINATE);
		context.setAttributeResolutionScope(AttributeResolutionScope.REQUEST_AND_LOCATORS);
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
		expect(context.getAttributeResolutionScope()).andReturn(AttributeResolutionScope.REQUEST_AND_LOCATORS);
		context.setAttributeResolutionScope(AttributeResolutionScope.REQUEST_ONLY);
		expect(m1.match(context)).andReturn(MatchResult.NOMATCH);
		context.setAttributeResolutionScope(AttributeResolutionScope.REQUEST_AND_LOCATORS);
		replay(m1, m2, m3);
		Target t = new Target(matches);
		assertEquals(MatchResult.NOMATCH, t.match(context));
		verify(m1, m2, m3);
	}
	
}
