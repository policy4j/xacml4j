package org.xacml4j.v30.policy.combine;

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;


public class PermitUnlessDenyTest {

	private IMocksControl mockCtl;

	private List<DecisionRule> decisions;
	private DecisionCombiningAlgorithm<DecisionRule> algorithm;
	private EvaluationContext context;

	@Before
	public void init(){
		this.decisions = new LinkedList<DecisionRule>();
		this.algorithm = new PermitUnlessDeny<DecisionRule>("aaaa");
		
		mockCtl = createStrictControl();
		this.context = mockCtl.createMock(EvaluationContext.class);
	}
	
	@Test
	public void testCombineWithNoDecisions()
	{
		mockCtl.replay();
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		mockCtl.verify();
	}
	
	@Test
	public void testCombineWithDeny() throws EvaluationException
	{
		DecisionRule r1 = mockCtl.createMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.DENY);
		mockCtl.replay();
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		mockCtl.verify();
	}
	
	@Test
	public void testCombineWithNotApplicable() throws EvaluationException
	{
		DecisionRule r1 = mockCtl.createMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.NOT_APPLICABLE);
		mockCtl.replay();
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		mockCtl.verify();
	}
	
	@Test
	public void testCombineWithIndeterminate() throws EvaluationException
	{
		DecisionRule r1 = mockCtl.createMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.INDETERMINATE);
		mockCtl.replay();
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		mockCtl.verify();
	}
	
	
	@Test
	public void testCombineWithDenyAndPermit() throws EvaluationException
	{
		DecisionRule r1 = mockCtl.createMock(DecisionRule.class);
		DecisionRule r2 = mockCtl.createMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.DENY);
		mockCtl.replay();
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		mockCtl.verify();
	}
}
