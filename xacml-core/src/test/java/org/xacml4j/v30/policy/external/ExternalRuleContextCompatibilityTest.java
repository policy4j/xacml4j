package org.xacml4j.v30.policy.external;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyReferenceResolver;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * A subclass of the public {@link Rule.RuleEvaluationContext} declared outside its package
 * (review RCK1 of change optimize-pdp-evaluation, cycle K, design D17): its overrides keep
 * answering for the contexts below it, the methods it could override at HEAD stay
 * overridable (binary compatibility, JLS 13.4.17), and staging still follows
 * {@code getParentContext()}.
 */
public class ExternalRuleContextCompatibilityTest
{
	private static final Clock FIXED = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
	private static final AttributeDesignatorKey SUBJECT_ID = AttributeDesignatorKey.builder()
			.category(CategoryId.SUBJECT_ACCESS).attributeId("urn:test:subject-id").dataType(XacmlTypes.STRING).build();

	@SuppressWarnings("unchecked")
	private static RootEvaluationContext root()
	{
		return new RootEvaluationContext(false, Duration.ZERO,
				mock(PolicyReferenceResolver.class), mock(EvaluationContextHandler.class));
	}

	@SuppressWarnings("unchecked")
	private static Policy policy(String id)
	{
		return Policy.builder(id).combiningAlgorithm(mock(DecisionCombiningAlgorithm.class)).build();
	}

	@Test
	public void methodsOverridableAtHeadStayOverridable() throws Exception
	{
		List<String> finalMethods = new ArrayList<>();
		for (Method m : EvaluationContext.class.getMethods()) {
			Method inherited = Rule.RuleEvaluationContext.class.getMethod(m.getName(), m.getParameterTypes());
			if (Modifier.isFinal(inherited.getModifiers())) {
				finalMethods.add(m.getName() + List.of(m.getParameterTypes()));
			}
		}
		// The only final one at HEAD (commit 21858796)
		assertEquals(List.of("resolve" + List.of(AttributeReferenceKey.class)), finalMethods);
	}

	@Test
	public void overridesOfAnExternalRuleContextAnswerForTheContextsBelowIt()
	{
		Rule rule = Rule.builder("rule", Effect.PERMIT).build();
		Policy declared = policy("urn:test:declared");
		Policy overridden = policy("urn:test:overridden");
		RootEvaluationContext root = root();
		EvaluationContext policyContext = declared.createContext(root);
		Rule.RuleEvaluationContext custom = rule.new RuleEvaluationContext(policyContext){
			@Override
			public Clock getClock() {
				return FIXED;
			}

			@Override
			public boolean isValidateFuncParamsAtRuntime() {
				return true;
			}

			@Override
			public CompositeDecisionRule getCurrentPolicy() {
				return overridden;
			}

			@Override
			public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators() {
				return Map.of(SUBJECT_ID, XacmlTypes.STRING.ofAny("alice").toBag());
			}
		};

		EvaluationContext below = custom.createExtIndeterminateEvalContext();

		assertTrue(below.isExtendedIndeterminateEval());
		assertSame(FIXED, below.getClock());
		assertTrue(below.isValidateFuncParamsAtRuntime());
		assertSame(overridden, below.getCurrentPolicy());
		assertSame(rule, below.getCurrentRule());
		assertSame(root.getClock(), policyContext.getClock());
		assertFalse(policyContext.isValidateFuncParamsAtRuntime());
		// The presence answer follows the overridden resolved designators, on the context and below it (review R-YZ)
		assertTrue(custom.hasResolvedDesignators());
		assertTrue(below.hasResolvedDesignators());
		assertEquals(custom.getResolvedDesignators(), below.getResolvedDesignators());
		assertFalse(policyContext.hasResolvedDesignators());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void stagingFollowsTheParentContextOfAnExternalRuleContext()
	{
		Rule rule = Rule.builder("rule", Effect.PERMIT).build();
		RootEvaluationContext root = root();
		PolicySet set = PolicySet.builder("urn:test:set")
				.withCombiningAlgorithm((DecisionCombiningAlgorithm<CompositeDecisionRule>) mock(DecisionCombiningAlgorithm.class)).build();
		EvaluationContext setContext = set.createContext(root);
		// Its parent field is the policy set context; its parent context is the root
		Rule.RuleEvaluationContext custom = rule.new RuleEvaluationContext(setContext){
			@Override
			public EvaluationContext getParentContext() {
				return root;
			}
		};
		List<EvaluationContext> combinedIn = new ArrayList<>();
		Policy policy = Policy.builder("urn:test:policy").rule(rule)
				.combiningAlgorithm(new Capturing(combinedIn)).build();
		EvaluationContext policyContext = policy.createContext(custom);

		assertEquals(Decision.PERMIT, policy.evaluate(policyContext));

		// Not enclosed by a policy set through getParentContext(), so not staged (as at HEAD)
		assertEquals(1, combinedIn.size());
		assertSame(policyContext, combinedIn.get(0));
	}

	/** A rule combining algorithm that records its context and is Permit. */
	private static final class Capturing implements DecisionCombiningAlgorithm<Rule>
	{
		private final List<EvaluationContext> contexts;

		Capturing(List<EvaluationContext> contexts) {
			this.contexts = contexts;
		}

		@Override
		public String getId() {
			return "urn:test:capturing";
		}

		@Override
		public Decision combine(EvaluationContext context, List<Rule> decisions) {
			contexts.add(context);
			return Decision.PERMIT;
		}
	}
}
