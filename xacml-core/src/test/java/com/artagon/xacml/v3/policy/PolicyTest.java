package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.policy.AdviceExpression;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.ObligationExpression;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyDefaults;
import com.artagon.xacml.v3.policy.RootEvaluationContext;
import com.artagon.xacml.v3.policy.Rule;
import com.artagon.xacml.v3.policy.Target;
import com.artagon.xacml.v3.policy.VariableDefinition;
import com.artagon.xacml.v3.spi.PolicyReferenceResolver;

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
	
	private List<Rule> rules;
	
	private PolicyReferenceResolver referenceResolver;
	private EvaluationContextHandler handler;
	
	private IMocksControl control;
	
	@SuppressWarnings("unchecked")
	@Before
	public void init() throws XacmlSyntaxException
	{
		this.rules = new LinkedList<Rule>();
		
		this.control = createControl();
		this.rule1 = control.createMock(Rule.class);
		rules.add(rule1);
		this.rule2 = control.createMock(Rule.class);
		rules.add(rule2);
		
		this.target = control.createMock(Target.class);
		this.combingingAlg = control.createMock(DecisionCombiningAlgorithm.class);
		
		this.obligationExpressions = new LinkedList<ObligationExpression>();
		this.adviceExpressions = new LinkedList<AdviceExpression>();
		
		this.denyObligationExp = control.createMock(ObligationExpression.class);
		this.permitObligationExp = control.createMock(ObligationExpression.class);
		obligationExpressions.add(denyObligationExp);
		obligationExpressions.add(permitObligationExp);
		
		this.denyAdviceExp = control.createMock(AdviceExpression.class);
		this.permitAdviceExp = control.createMock(AdviceExpression.class);
		
		adviceExpressions.add(denyAdviceExp);
		adviceExpressions.add(permitAdviceExp);
		
		this.policy = new Policy("test", 
				Version.valueOf(1),
				"Test",
				new PolicyDefaults(XPathVersion.XPATH1),
				target, 
				Collections.<VariableDefinition>emptyList(), 
				combingingAlg, rules, adviceExpressions, obligationExpressions);
		this.referenceResolver = control.createMock(PolicyReferenceResolver.class);
		this.handler = control.createMock(EvaluationContextHandler.class);
		this.context = new RootEvaluationContext(true, referenceResolver, handler);
	}
	
	@Test
	public void testPolicyCreate() throws XacmlSyntaxException
	{
		Policy p = new Policy("testId", 
				Version.valueOf(1),
				"Test",
				new PolicyDefaults(XPathVersion.XPATH1),
				target, 
				Collections.<VariableDefinition>emptyList(), 
				combingingAlg, rules, adviceExpressions, obligationExpressions);
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
		control.replay();
		assertEquals(MatchResult.MATCH, policy.isApplicable(policyContext));
		control.verify();
	}
	
	@Test
	public void testPolicyIsApplicableTargetNoMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.NOMATCH);
		control.replay();
		assertEquals(MatchResult.NOMATCH, policy.isApplicable(policyContext));
		control.verify();
	}
	
	@Test
	public void testPolicyIsApplicableTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.INDETERMINATE);
		control.replay();
		assertEquals(MatchResult.INDETERMINATE, policy.isApplicable(policyContext));
		control.verify();
	}
	
	@Test
	public void testPolicyEvaluationCombiningAlgorithmResultIsDeny() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(combingingAlg.combine(rules, policyContext)).andReturn(Decision.DENY);
		
		Advice advice = control.createMock(Advice.class);
		Obligation obligation = control.createMock(Obligation.class);
		
		expect(denyAdviceExp.isApplicable(Decision.DENY)).andReturn(true);
		expect(denyAdviceExp.getId()).andReturn("denyAdviceExp").times(0, 1);
		expect(denyAdviceExp.evaluate(policyContext)).andReturn(advice);
		expect(permitAdviceExp.isApplicable(Decision.DENY)).andReturn(false);
		
		expect(denyObligationExp.isApplicable(Decision.DENY)).andReturn(true);
		expect(denyObligationExp.getId()).andReturn("denyObligationExp").times(0, 1);
		expect(denyObligationExp.evaluate(policyContext)).andReturn(obligation);
		expect(permitObligationExp.isApplicable(Decision.DENY)).andReturn(false);
		
		
		context.addAdvices(Collections.singletonList(advice));
		context.addObligations(Collections.singletonList(obligation));
		
		control.replay();
		assertEquals(Decision.DENY, policy.evaluate(policyContext));
		control.verify();
	}
	
	@Test
	public void testPolicyEvaluationCombiningAlgorithResultIsPermit() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(combingingAlg.combine(rules, policyContext)).andReturn(Decision.PERMIT);
		
		Advice advice = control.createMock(Advice.class);
		Obligation obligation = control.createMock(Obligation.class);
		
		expect(denyAdviceExp.isApplicable(Decision.PERMIT)).andReturn(false);
		expect(permitAdviceExp.isApplicable(Decision.PERMIT)).andReturn(true);
		expect(permitAdviceExp.getId()).andReturn("permitAdviceExp").times(0, 1);
		expect(permitAdviceExp.evaluate(policyContext)).andReturn(advice);
		
		expect(denyObligationExp.isApplicable(Decision.PERMIT)).andReturn(false);
		expect(permitObligationExp.isApplicable(Decision.PERMIT)).andReturn(true);
		expect(permitObligationExp.getId()).andReturn("permitObligationExp").times(0, 1);
		expect(permitObligationExp.evaluate(policyContext)).andReturn(obligation);
		
		context.addAdvices(Collections.singletonList(advice));
		context.addObligations(Collections.singletonList(obligation));
		
		control.replay();
		assertEquals(Decision.PERMIT, policy.evaluate(policyContext));
		control.verify();
	}
	
	@Test
	public void testPolicyEvaluationCombiningAlgorithResultIsIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(combingingAlg.combine(rules, policyContext)).andReturn(Decision.INDETERMINATE);
		control.replay();
		assertEquals(Decision.INDETERMINATE, policy.evaluate(policyContext));
		control.verify();
	}
	
	
}
