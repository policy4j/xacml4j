package org.xacml4j.v30.policy;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Which context answers each query of an evaluation, which cycle K of change
 * optimize-pdp-evaluation must keep (design D17): the nearest level that evaluates a rule,
 * policy, policy set, or reference answers the query about it; every other query reaches the
 * first context that is not one of the engine's own, called once. Real policies, policy sets,
 * references, combining algorithms, and contexts; the resolvers are test doubles, and probes
 * in the conditions record what each context answers during the evaluation.
 */
public class ContextChainQueryCharacterizationTest
{
	private static final String RULES = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
	private static final String POLICIES = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final AttributeDesignatorKey KEY = AttributeDesignatorKey.builder("urn:test:attribute")
			.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).build();
	private static final ValueExp VARIABLE_VALUE = XacmlTypes.STRING.ofAny("variable");

	private final Map<String, Map<String, Object>> answers = new LinkedHashMap<>();
	private final Optional<BagOfValues> rootBag = Optional.of(XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("root")));

	private Rule rule;
	private Policy policy;
	private PolicySet inner;
	private PolicySet outer;
	private PolicyIDReference policyRef;
	private PolicySetIDReference innerRef;
	private PolicyReferenceResolver resolver;
	private EvaluationContextHandler handler;

	@BeforeEach
	public void init() throws Exception
	{
		this.rule = Rule.builder("rule", Effect.PERMIT).condition(new Condition(new Probe("rule"))).build();
		this.policy = Policy.builder("policy").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(RULES))
				.var(new VariableDefinition("v", XacmlTypes.STRING.ofAny("unused")))
				.condition(new Condition(new Probe("policy")))
				.rule(rule)
				.build();
		this.policyRef = PolicyIDReference.builder("policy").versionAsString("1.+").build();
		this.inner = PolicySet.builder("inner").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(POLICIES))
				.condition(new Condition(new Probe("inner")))
				.compositeDecisionRules(policyRef)
				.build();
		this.innerRef = PolicySetIDReference.builder("inner").versionAsString("1.+").build();
		this.outer = PolicySet.builder("outer").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(POLICIES))
				.condition(new Condition(new Probe("outer")))
				.compositeDecisionRules(innerRef)
				.build();
		this.resolver = mock(PolicyReferenceResolver.class);
		when(resolver.resolve(policyRef)).thenReturn(policy);
		when(resolver.resolve(innerRef)).thenReturn(inner);
		this.handler = mock(EvaluationContextHandler.class);
	}

	private RootEvaluationContext root()
	{
		RootEvaluationContext root = new RootEvaluationContext(false, Duration.ZERO, resolver, handler);
		when(handler.resolve(root, KEY)).thenReturn(rootBag);
		return root;
	}

	@Test
	public void nearestLevelAnswersQueriesAboutWhatItEvaluates()
	{
		RootEvaluationContext root = root();

		assertEquals(Decision.PERMIT, outer.evaluate(outer.createContext(root)));

		assertEquals(List.of("outer", "inner", "policy", "rule"), new ArrayList<>(answers.keySet()));
		assertLevel("outer", null, null, outer, null, null);
		assertLevel("inner", null, null, inner, null, innerRef);
		assertLevel("policy", null, policy, inner, policyRef, innerRef);
		assertLevel("rule", rule, policy, inner, policyRef, innerRef);
		// The variable that the policy probe set is read by the rule through the staging context
		assertSame(VARIABLE_VALUE, answers.get("rule").get("variable"));
		for (Map<String, Object> level : answers.values()) {
			assertSame(rootBag, level.get("resolve"));
			assertSame(root.getClock(), level.get("clock"));
			assertEquals(false, level.get("validate"));
			assertEquals(root.getXPathVersion(), level.get("xpath"));
			assertEquals(root.getDecisionCacheTTL(), level.get("ttl"));
		}
	}

	@Test
	public void foreignContextAboveTheTreeAnswersWhatNoLevelAnswers()
	{
		RootEvaluationContext root = root();
		Map<String, AtomicInteger> calls = new HashMap<>();
		Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
		Optional<BagOfValues> foreignBag = Optional.of(XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("foreign")));
		Rule foreignRule = Rule.builder("foreign-rule", Effect.DENY).build();
		EvaluationContext foreign = foreign(root, calls, Map.of(
				"isValidateFuncParamsAtRuntime", true,
				"getClock", clock,
				"resolve", foreignBag,
				"getCurrentRule", foreignRule));

		assertEquals(Decision.PERMIT, outer.evaluate(outer.createContext(foreign)));

		assertLevel("outer", foreignRule, null, outer, null, null);
		assertLevel("inner", foreignRule, null, inner, null, innerRef);
		assertLevel("policy", foreignRule, policy, inner, policyRef, innerRef);
		assertLevel("rule", rule, policy, inner, policyRef, innerRef);
		for (Map<String, Object> level : answers.values()) {
			assertSame(foreignBag, level.get("resolve"));
			assertSame(clock, level.get("clock"));
			assertEquals(true, level.get("validate"));
			assertEquals(root.getDecisionCacheTTL(), level.get("ttl"));
		}
	}

	@Test
	public void foreignContextInsideTheChainIsCalledOncePerForwardedQuery()
	{
		RootEvaluationContext root = root();
		Map<String, AtomicInteger> calls = new HashMap<>();
		Optional<BagOfValues> foreignBag = Optional.of(XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("foreign")));
		EvaluationContext outerContext = outer.createContext(root);
		EvaluationContext foreign = foreign(outerContext, calls, Map.of(
				"isValidateFuncParamsAtRuntime", true,
				"resolve", foreignBag));
		EvaluationContext innerContext = inner.createContext(foreign);
		EvaluationContext policyContext = policy.createContext(innerContext);
		EvaluationContext ruleContext = rule.createContext(policyContext);
		calls.clear();

		assertEquals(true, ruleContext.isValidateFuncParamsAtRuntime());
		assertSame(foreignBag, ruleContext.resolve(KEY));
		assertSame(root.getClock(), ruleContext.getClock());
		assertSame(rule, ruleContext.getCurrentRule());
		assertSame(policy, ruleContext.getCurrentPolicy());
		assertSame(inner, ruleContext.getCurrentPolicySet());
		assertNull(ruleContext.getCurrentPolicyIDReference());
		assertSame(outer, foreign.getCurrentPolicySet());

		assertEquals(1, calls.get("isValidateFuncParamsAtRuntime").get());
		assertEquals(1, calls.get("resolve").get());
		assertEquals(1, calls.get("getClock").get());
		assertEquals(1, calls.get("getCurrentPolicyIDReference").get());
		assertNull(calls.get("getCurrentRule"));
		assertNull(calls.get("getCurrentPolicy"));
		assertEquals(1, calls.get("getCurrentPolicySet").get());
	}

	@Test
	public void validationSettingIsReadAndWrittenAtTheRoot()
	{
		RootEvaluationContext root = root();
		EvaluationContext ruleContext = rule.createContext(policy.createContext(inner.createContext(root)));

		ruleContext.setValidateFuncParamsAtRuntime(true);

		assertEquals(true, root.isValidateFuncParamsAtRuntime());
		assertEquals(true, ruleContext.isValidateFuncParamsAtRuntime());
		RootEvaluationContext reference = new RootEvaluationContext(false, Duration.ofSeconds(30), resolver, handler);
		root = new RootEvaluationContext(false, Duration.ofSeconds(30), resolver, handler);
		ruleContext = rule.createContext(policy.createContext(inner.createContext(root)));
		reference.setDecisionCacheTTL(Duration.ofSeconds(5));
		ruleContext.setDecisionCacheTTL(Duration.ofSeconds(5));
		assertEquals(reference.getDecisionCacheTTL(), root.getDecisionCacheTTL());
		assertEquals(reference.getDecisionCacheTTL(), ruleContext.getDecisionCacheTTL());
	}

	private void assertLevel(String level, Rule currentRule, Policy currentPolicy, PolicySet currentPolicySet,
			PolicyIDReference currentPolicyRef, PolicySetIDReference currentPolicySetRef)
	{
		Map<String, Object> a = answers.get(level);
		assertSame(currentRule, a.get("rule"), level);
		assertSame(currentPolicy, a.get("policy"), level);
		assertSame(currentPolicySet, a.get("policySet"), level);
		assertSame(currentPolicyRef, a.get("policyRef"), level);
		assertSame(currentPolicySetRef, a.get("policySetRef"), level);
	}

	/**
	 * A context of another implementation: it answers the given queries and passes the others on.
	 * {@code resolve} names the attribute overload; the policy reference overload is
	 * {@code resolveReference}.
	 */
	private static EvaluationContext foreign(EvaluationContext delegate, Map<String, AtomicInteger> calls,
			Map<String, Object> overrides)
	{
		return (EvaluationContext) Proxy.newProxyInstance(EvaluationContext.class.getClassLoader(),
				new Class<?>[]{EvaluationContext.class}, (proxy, method, args) -> {
					String name = method.getName();
					if (name.equals("resolve") && method.getParameterTypes()[0] != AttributeReferenceKey.class) {
						name = "resolveReference";
					}
					calls.computeIfAbsent(name, k -> new AtomicInteger()).incrementAndGet();
					Object answer = overrides.get(name);
					if (answer != null) {
						return answer;
					}
					try {
						return method.invoke(delegate, args);
					} catch (InvocationTargetException e) {
						throw e.getCause();
					}
				});
	}

	/** A boolean condition that records what its evaluation context answers. */
	private final class Probe implements Expression
	{
		private final String level;

		Probe(String level) {
			this.level = level;
		}

		@Override
		public ValueExpTypeInfo getEvaluatesTo() {
			return XacmlTypes.BOOLEAN;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T extends Expression> T evaluate(EvaluationContext context) {
			if (level.equals("policy")) {
				context.setVariableEvaluationResult("v", VARIABLE_VALUE);
			}
			Map<String, Object> a = new HashMap<>();
			a.put("rule", context.getCurrentRule());
			a.put("policy", context.getCurrentPolicy());
			a.put("policySet", context.getCurrentPolicySet());
			a.put("policyRef", context.getCurrentPolicyIDReference());
			a.put("policySetRef", context.getCurrentPolicySetIDReference());
			a.put("resolve", context.resolve(KEY));
			a.put("clock", context.getClock());
			a.put("validate", context.isValidateFuncParamsAtRuntime());
			a.put("xpath", context.getXPathVersion());
			a.put("ttl", context.getDecisionCacheTTL());
			if (level.equals("rule")) {
				a.put("variable", context.getVariableEvaluationResult("v"));
			}
			answers.put(level, a);
			return (T) BooleanVal.of(true);
		}

		@Override
		public void accept(ExpressionVisitor visitor) {
		}
	}
}
