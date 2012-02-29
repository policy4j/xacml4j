package com.artagon.xacml.v30.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.artagon.xacml.v30.spi.repository.PolicyReferenceResolver;
import com.artagon.xacml.v30.types.StringType;

public class PolicyTest 
{
	private EvaluationContext context;
	private Policy policy;
	private Target target;
	
	
	private DecisionCombiningAlgorithm<Rule> combingingAlg;
	
	private Expression denyObligationAttributeExp;
	private Expression permitObligationAttributeExp;
	
	private Expression denyAdviceAttributeExp;
	private Expression permitAdviceAttributeExp;

	
	
	private PolicyReferenceResolver referenceResolver;
	private EvaluationContextHandler handler;
	
	private IMocksControl c;
	
	@SuppressWarnings("unchecked")
	@Before
	public void init() throws XacmlSyntaxException
	{		
		this.c = createStrictControl();
		
		this.target = c.createMock(Target.class);
		this.combingingAlg = c.createMock(DecisionCombiningAlgorithm.class);
		
		this.permitAdviceAttributeExp = c.createMock(Expression.class);
		this.denyAdviceAttributeExp = c.createMock(Expression.class);
		
		this.denyObligationAttributeExp = c.createMock(Expression.class);
		this.permitObligationAttributeExp = c.createMock(Expression.class);
		
		this.policy = Policy.builder()
				.withVersion("1.0")
				.withId("TestPolicy")
				.withTarget(target)
				.withRule(c.createMock(Rule.class))
				.withCombiningAlgorithm(combingingAlg)
				.withObligation(ObligationExpression
						.builder("denyObligation", Effect.DENY)
							.attributeAssigment("testId", denyObligationAttributeExp))
				.withObligation(ObligationExpression
						.builder("permitObligation", Effect.PERMIT)
						.attributeAssigment("testId", permitObligationAttributeExp))
				.withAdvice(AdviceExpression
						.builder("denyAdvice", Effect.DENY)
						.withAttributeAssigment("testId", denyAdviceAttributeExp))
				.withAdvice(AdviceExpression
					.builder("permitAdvice", Effect.PERMIT)
					.withAttributeAssigment("testId", permitAdviceAttributeExp))
				.create();
			
		this.referenceResolver = c.createMock(PolicyReferenceResolver.class);
		this.handler = c.createMock(EvaluationContextHandler.class);
		this.context = new RootEvaluationContext(true, 0, referenceResolver, handler);
	}
	
	@Test
	@Ignore
	public void testPolicyCreate() throws XacmlSyntaxException
	{
//		Policy p = new Policy("testId", 
//				Version.valueOf(1),
//				"Test",
//				new PolicyDefaults(XPathVersion.XPATH1),
//				target, 
//				Collections.<VariableDefinition>emptyList(), 
//				combingingAlg, rules, adviceExpressions, obligationExpressions);
//		assertEquals("testId", p.getId());
//		assertEquals(Version.valueOf(1), p.getVersion());
//		assertEquals("Test", p.getDescription());
//		assertSame(target, p.getTarget());
//		assertSame(combingingAlg, p.getRuleCombiningAlgorithm());
	}
	
	@Test
	public void testCreateContext() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		assertSame(policy, policyContext.getCurrentPolicy());
		assertSame(context, policyContext.getParentContext());
		EvaluationContext policyContext1 = policy.createContext(policyContext);
		assertSame(policyContext, policyContext1);
	}
	
	@Test
	public void testIsApplicableTargetMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.MATCH);
		c.replay();
		assertEquals(MatchResult.MATCH, policy.isMatch(policyContext));
		c.verify();
	}
	
	@Test
	public void testIsApplicableTargetNoMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.NOMATCH);
		c.replay();
		assertEquals(MatchResult.NOMATCH, policy.isMatch(policyContext));
		c.verify();
	}
	
	@Test
	public void testIsApplicableTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, policy.isMatch(policyContext));
		c.verify();
	}
	
	@Test
	public void testEvaluateCombiningAlgorithmResultIsDeny() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		Capture<List<Rule>> ruleCapture = new Capture<List<Rule>>();
		
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.DENY);
		expect(combingingAlg.getId()).andReturn("id");
		
		expect(denyAdviceAttributeExp.evaluate(policyContext)).andReturn(StringType.STRING.create("testValue1"));
		expect(denyObligationAttributeExp.evaluate(policyContext)).andReturn(StringType.STRING.create("testValue1"));
		
		c.replay();
		assertEquals(Decision.DENY, policy.evaluate(policyContext));
		c.verify();
		assertSame(policy, policyContext.getCurrentPolicy());
		assertSame(policyContext, contextCapture.getValue());
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testEvaluateCombiningAlgorithResultIsPermit() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		Capture<List<Rule>> ruleCapture = new Capture<List<Rule>>();
		
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.PERMIT);
		expect(combingingAlg.getId()).andReturn("id");
		expect(permitAdviceAttributeExp.evaluate(policyContext)).andReturn(StringType.STRING.create("testValue1"));
		expect(permitObligationAttributeExp.evaluate(policyContext)).andReturn(StringType.STRING.create("testValue1"));
		
		c.replay();
		assertEquals(Decision.PERMIT, policy.evaluate(policyContext));
		c.verify();
		assertSame(policy, policyContext.getCurrentPolicy());
		assertSame(policyContext, contextCapture.getValue());
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testEvaluateCombiningAlgorithResultIsIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		Capture<List<Rule>> ruleCapture = new Capture<List<Rule>>();
		
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.INDETERMINATE);
		expect(combingingAlg.getId()).andReturn("id");
		
		c.replay();
		assertEquals(Decision.INDETERMINATE, policy.evaluate(policyContext));
		c.verify();
		assertSame(policy, policyContext.getCurrentPolicy());
		assertSame(policyContext, contextCapture.getValue());
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	
}
