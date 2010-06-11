package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.spi.XPathProvider;

public class PolicyTest 
{
	private EvaluationContext context;
	private Policy policy;
	private Rule rule1;
	private Rule rule2;
	private Target target;
	private Collection<ObligationExpression> obligationExpressions;
	private Collection<AdviceExpression> adviceExpressions;
	
	private AdviceExpression permitAdviceExp;
	private AdviceExpression denyAdviceExp;
	
	private ObligationExpression permitObligationExp;
	private ObligationExpression denyObligationExp;
	
	private DecisionCombiningAlgorithm<Rule> combingingAlg;
	
	private Request request;
	
	private List<Rule> rules;
	
	@SuppressWarnings("unchecked")
	@Before
	public void init() throws PolicySyntaxException
	{
		this.rules = new LinkedList<Rule>();
		this.rule1 = createStrictMock(Rule.class);
		rules.add(rule1);
		this.rule2 = createStrictMock(Rule.class);
		rules.add(rule2);
		
		this.target = createStrictMock(Target.class);
		this.combingingAlg = createStrictMock(DecisionCombiningAlgorithm.class);
		
		this.obligationExpressions = new LinkedList<ObligationExpression>();
		this.adviceExpressions = new LinkedList<AdviceExpression>();
		
		this.denyObligationExp = createStrictMock(ObligationExpression.class);
		this.permitObligationExp = createStrictMock(ObligationExpression.class);
		obligationExpressions.add(denyObligationExp);
		obligationExpressions.add(permitObligationExp);
		
		this.denyAdviceExp = createStrictMock(AdviceExpression.class);
		this.permitAdviceExp = createStrictMock(AdviceExpression.class);
		
		adviceExpressions.add(denyAdviceExp);
		adviceExpressions.add(permitAdviceExp);
		
		this.policy = new Policy("test", 
				Version.valueOf(1),
				"Test",
				new PolicyDefaults(XPathVersion.XPATH1),
				target, 
				Collections.<VariableDefinition>emptyList(), 
				combingingAlg, rules, adviceExpressions, obligationExpressions);
		
		this.request = createStrictMock(Request.class);
		this.context = new DefaultEvaluationContextFactory( 
				createStrictMock(PolicyReferenceResolver.class), 
				createStrictMock(XPathProvider.class)).createContext(policy, request);
	}
	
	@Test
	public void testPolicyCreate() throws PolicySyntaxException
	{
		Policy p = new Policy("testId", 
				Version.valueOf(1),
				"Test",
				new PolicyDefaults(XPathVersion.XPATH1),
				target, 
				Collections.<VariableDefinition>emptyList(), 
				combingingAlg, rules, adviceExpressions, obligationExpressions);
		assertEquals(new PolicyIdentifier("testId", Version.parse("1.0")), p.getPolicyIdentifier());
		assertEquals("testId", p.getId());
		assertEquals(Version.valueOf(1), p.getVersion());
		assertEquals("Test", p.getDescription());
		assertSame(target, p.getTarget());
		assertSame(combingingAlg, p.getRuleCombiningAlgorithm());
	}
	
	@Test
	public void testPolicyIsApplicableTargetMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.MATCH);
		replay(target);
		assertEquals(MatchResult.MATCH, policy.isApplicable(policyContext));
		verify(target);
	}
	
	@Test
	public void testPolicyIsApplicableTargetNoMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.NOMATCH);
		replay(target);
		assertEquals(MatchResult.NOMATCH, policy.isApplicable(policyContext));
		verify(target);
	}
	
	@Test
	public void testPolicyIsApplicableTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.INDETERMINATE);
		replay(target);
		assertEquals(MatchResult.INDETERMINATE, policy.isApplicable(policyContext));
		verify(target);
	}
	
	@Test
	public void testPolicyEvaluationCombiningAlgorithmResultIsDeny() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(combingingAlg.combine(rules, policyContext)).andReturn(Decision.DENY);
		
		Advice advice = createMock(Advice.class);
		Obligation obligation = createMock(Obligation.class);
		
		expect(denyAdviceExp.isApplicable(Decision.DENY)).andReturn(true);
		expect(denyAdviceExp.getId()).andReturn("denyAdviceExp").times(0, 1);
		expect(denyAdviceExp.evaluate(policyContext)).andReturn(advice);
		
		expect(denyObligationExp.isApplicable(Decision.DENY)).andReturn(true);
		expect(denyObligationExp.getId()).andReturn("denyObligationExp").times(0, 1);
		expect(denyObligationExp.evaluate(policyContext)).andReturn(obligation);
		context.addAdvices(Collections.singletonList(advice));
		context.addObligations(Collections.singletonList(obligation));
		
		replay(combingingAlg, rule1, rule2, denyAdviceExp, denyObligationExp);
		assertEquals(Decision.DENY, policy.evaluate(policyContext));
		verify(combingingAlg, rule1, rule2, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testPolicyEvaluationCombiningAlgorithResultIsPermit() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(combingingAlg.combine(rules, policyContext)).andReturn(Decision.PERMIT);
		
		Advice advice = createMock(Advice.class);
		Obligation obligation = createMock(Obligation.class);
		
		expect(permitAdviceExp.isApplicable(Decision.PERMIT)).andReturn(true);
		expect(permitAdviceExp.getId()).andReturn("permitAdviceExp").times(0, 1);
		expect(permitAdviceExp.evaluate(policyContext)).andReturn(advice);
		
		expect(permitObligationExp.isApplicable(Decision.PERMIT)).andReturn(true);
		expect(permitObligationExp.getId()).andReturn("permitObligationExp").times(0, 1);
		expect(permitObligationExp.evaluate(policyContext)).andReturn(obligation);
		context.addAdvices(Collections.singletonList(advice));
		context.addObligations(Collections.singletonList(obligation));
		
		replay(combingingAlg, rule1, rule2, permitAdviceExp, permitObligationExp);
		assertEquals(Decision.PERMIT, policy.evaluate(policyContext));
		verify(combingingAlg, rule1, rule2, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testPolicyEvaluationCombiningAlgorithResultIsIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(combingingAlg.combine(rules, policyContext)).andReturn(Decision.INDETERMINATE);
		replay(combingingAlg, rule1, rule2, permitAdviceExp, permitObligationExp);
		assertEquals(Decision.INDETERMINATE, policy.evaluate(policyContext));
		verify(combingingAlg, rule1, rule2, permitAdviceExp, permitObligationExp);
	}
	
	
}
