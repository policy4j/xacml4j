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
import org.junit.Test;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.Effect;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.MatchResult;
import com.artagon.xacml.v30.spi.repository.PolicyReferenceResolver;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.Iterables;

public class PolicySetTest 
{
	private EvaluationContext context;
	private PolicySet policySet;
	private Target target;
	
	
	private DecisionCombiningAlgorithm<CompositeDecisionRule> combingingAlg;
	
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
		
		this.policySet = PolicySet.builder("TestPolicy")
				.withVersion("1.0")
				.withTarget(target)
				.withPolicy(c.createMock(Policy.class))
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
	public void testCreateContext() throws EvaluationException
	{
		EvaluationContext policyContext = policySet.createContext(context);
		assertSame(policySet, policyContext.getCurrentPolicySet());
		assertSame(context, policyContext.getParentContext());
		EvaluationContext policyContext1 = policySet.createContext(policyContext);
		assertSame(policyContext, policyContext1);
	}
	
	@Test
	public void testIsApplicableTargetMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policySet.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.MATCH);
		c.replay();
		assertEquals(MatchResult.MATCH, policySet.isMatch(policyContext));
		c.verify();
	}
	
	@Test
	public void testIsApplicableTargetNoMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policySet.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.NOMATCH);
		c.replay();
		assertEquals(MatchResult.NOMATCH, policySet.isMatch(policyContext));
		c.verify();
	}
	
	@Test
	public void testIsApplicableTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policySet.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, policySet.isMatch(policyContext));
		c.verify();
	}
	
	@Test
	public void testEvaluateCombiningAlgorithmResultIsDeny() throws EvaluationException
	{
		EvaluationContext policyContext = policySet.createContext(context);
		
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();
		
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.DENY);
		
		expect(denyAdviceAttributeExp.evaluate(policyContext)).andReturn(StringType.STRING.create("testValue1"));
		expect(denyObligationAttributeExp.evaluate(policyContext)).andReturn(StringType.STRING.create("testValue1"));
		
		c.replay();
		assertEquals(Decision.DENY, policySet.evaluate(policyContext));
		c.verify();
		assertSame(policySet, policyContext.getCurrentPolicySet());
		assertSame(policyContext, contextCapture.getValue());
		
		assertEquals(1, Iterables.size(context.getMatchingAdvices(Decision.DENY)));
		assertEquals(1, Iterables.size(context.getMatchingObligations(Decision.DENY)));
	}
	
	@Test
	public void testEvaluateCombiningAlgorithResultIsPermit() throws EvaluationException
	{
		EvaluationContext policySetContext = policySet.createContext(context);
		
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();
		
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.PERMIT);
		expect(permitAdviceAttributeExp.evaluate(policySetContext)).andReturn(StringType.STRING.create("testValue1"));
		expect(permitObligationAttributeExp.evaluate(policySetContext)).andReturn(StringType.STRING.create("testValue1"));
		
		c.replay();
		assertEquals(Decision.PERMIT, policySet.evaluate(policySetContext));
		c.verify();
		assertSame(policySet, policySetContext.getCurrentPolicySet());
		assertSame(policySetContext, contextCapture.getValue());
		assertEquals(1, Iterables.size(context.getMatchingAdvices(Decision.PERMIT)));
		assertEquals(1, Iterables.size(context.getMatchingObligations(Decision.PERMIT)));
	}
	
	@Test
	public void testEvaluateCombiningAlgorithResultIsIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policySet.createContext(context);
		
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();
		
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.INDETERMINATE);
		
		c.replay();
		assertEquals(Decision.INDETERMINATE, policySet.evaluate(policyContext));
		c.verify();
		assertSame(policySet, policyContext.getCurrentPolicySet());
		assertSame(policyContext, contextCapture.getValue());
		assertEquals(0, Iterables.size(context.getMatchingAdvices(Decision.DENY)));
		assertEquals(0, Iterables.size(context.getMatchingObligations(Decision.DENY)));
		assertEquals(0, Iterables.size(context.getMatchingAdvices(Decision.PERMIT)));
		assertEquals(0, Iterables.size(context.getMatchingObligations(Decision.PERMIT)));
	}
	
	
}
