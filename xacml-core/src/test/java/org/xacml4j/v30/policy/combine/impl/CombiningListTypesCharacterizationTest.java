package org.xacml4j.v30.policy.combine.impl;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.Rule;

/**
 * Cycle J of change optimize-pdp-evaluation (design D16): every combining algorithm gives the same
 * decision and evaluates the same elements in the same order whether its list is an {@code ArrayList},
 * a {@code LinkedList}, or an immutable {@code List.of}, for every sequence of up to three decisions.
 */
public class CombiningListTypesCharacterizationTest
{
	private static final Decision[] DECISIONS = Decision.values();
	private static final EvaluationContext CONTEXT = mock(EvaluationContext.class, withSettings().stubOnly());

	/** One combination run: the decision and the order in which elements were asked. */
	private record Run(Decision decision, List<String> trace)
	{
	}

	private static List<List<Decision>> sequences()
	{
		List<List<Decision>> all = new ArrayList<>();
		all.add(List.of());
		for(Decision a : DECISIONS){
			all.add(List.of(a));
			for(Decision b : DECISIONS){
				all.add(List.of(a, b));
				for(Decision c : DECISIONS){
					all.add(List.of(a, b, c));
				}
			}
		}
		return all;
	}

	private static <D extends DecisionRule> void assertSameOnEveryListType(String name, DecisionCombiningAlgorithm<D> algorithm,
			Function<Element, D> factory)
	{
		for(List<Decision> sequence : sequences()){
			Run array = run(algorithm, sequence, factory, ArrayList::new);
			Run linked = run(algorithm, sequence, factory, LinkedList::new);
			Run immutable = run(algorithm, sequence, factory, List::copyOf);
			Run guava = run(algorithm, sequence, factory, ImmutableList::copyOf);
			assertWithMessage("%s over %s, LinkedList", name, sequence).that(linked).isEqualTo(array);
			assertWithMessage("%s over %s, List.of", name, sequence).that(immutable).isEqualTo(array);
			assertWithMessage("%s over %s, ImmutableList", name, sequence).that(guava).isEqualTo(array);
		}
	}

	private static <D extends DecisionRule> Run run(DecisionCombiningAlgorithm<D> algorithm, List<Decision> sequence,
			Function<Element, D> factory, Function<List<D>, List<D>> listType)
	{
		List<String> trace = new ArrayList<>();
		List<D> elements = new ArrayList<>();
		for(int i = 0; i < sequence.size(); i++){
			elements.add(factory.apply(new Element("r" + i, sequence.get(i), trace)));
		}
		Decision decision = algorithm.combine(CONTEXT, listType.apply(elements));
		return new Run(decision, trace);
	}

	/** What an element answers, and where it records the calls it gets. */
	private record Element(String id, Decision decision, List<String> trace)
	{
		Effect effect()
		{
			return switch(decision){
				case DENY, INDETERMINATE_D -> Effect.DENY;
				default -> Effect.PERMIT;
			};
		}

		MatchResult match()
		{
			return switch(decision){
				case NOT_APPLICABLE -> MatchResult.NOMATCH;
				case INDETERMINATE, INDETERMINATE_D, INDETERMINATE_P, INDETERMINATE_DP -> MatchResult.INDETERMINATE;
				default -> MatchResult.MATCH;
			};
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<T> type, Element e)
	{
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (p, method, args) -> {
			switch(method.getName()){
				case "createContext": e.trace().add(e.id() + ".createContext"); return args[0];
				case "evaluate": e.trace().add(e.id() + ".evaluate"); return e.decision();
				case "isMatch": e.trace().add(e.id() + ".isMatch"); return e.match();
				case "getId": return e.id();
				case "toString": return e.id();
				case "hashCode": return System.identityHashCode(p);
				case "equals": return p == args[0];
				default: throw new UnsupportedOperationException(method.getName());
			}
		});
	}

	private static Rule rule(Element e)
	{
		Rule rule = mock(Rule.class, withSettings().stubOnly());
		when(rule.createContext(CONTEXT)).thenAnswer(i -> { e.trace().add(e.id() + ".createContext"); return CONTEXT; });
		when(rule.evaluate(CONTEXT)).thenAnswer(i -> { e.trace().add(e.id() + ".evaluate"); return e.decision(); });
		when(rule.getEffect()).thenReturn(e.effect());
		when(rule.getId()).thenReturn(e.id());
		return rule;
	}

	@Test
	public void genericAlgorithmsAgreeOnEveryListType()
	{
		Function<Element, DecisionRule> rules = e -> proxy(DecisionRule.class, e);
		assertSameOnEveryListType("deny-overrides", new DenyOverrides<>("test:deny-overrides"), rules);
		assertSameOnEveryListType("permit-overrides", new PermitOverrides<>("test:permit-overrides"), rules);
		assertSameOnEveryListType("deny-unless-permit", new DenyUnlessPermit<>("test:deny-unless-permit"), rules);
		assertSameOnEveryListType("permit-unless-deny", new PermitUnlessDeny<>("test:permit-unless-deny"), rules);
		assertSameOnEveryListType("first-applicable", new FirstApplicable<>("test:first-applicable"), rules);
	}

	@Test
	public void policyAlgorithmsAgreeOnEveryListType()
	{
		Function<Element, CompositeDecisionRule> policies = e -> proxy(CompositeDecisionRule.class, e);
		assertSameOnEveryListType("only-one-applicable", new OnlyOneApplicablePolicyCombiningAlgorithm(), policies);
		assertSameOnEveryListType("legacy deny-overrides policy", new LegacyDenyOverridesPolicyCombineAlgorithm(), policies);
		assertSameOnEveryListType("legacy permit-overrides policy", new LegacyPermitOverridesPolicyCombineAlgorithm(), policies);
		assertSameOnEveryListType("legacy ordered deny-overrides policy", new LegacyOrderedDenyOverridesPolicyCombineAlgorithm(), policies);
		assertSameOnEveryListType("legacy ordered permit-overrides policy", new LegacyOrderedPermitOverridesPolicyCombineAlgorithm(), policies);
	}

	/**
	 * Review RCJ1: the combining API takes the caller's list; a {@code CopyOnWriteArrayList} that a rule
	 * changes during combination is still combined as it was when combination started (its iterator's
	 * snapshot).
	 */
	@Test
	public void copyOnWriteListIsCombinedAsItsSnapshot()
	{
		List<String> trace = new ArrayList<>();
		java.util.concurrent.CopyOnWriteArrayList<DecisionRule> rules = new java.util.concurrent.CopyOnWriteArrayList<>();
		DecisionRule permit = proxy(DecisionRule.class, new Element("replacement", Decision.PERMIT, trace));
		DecisionRule deny = proxy(DecisionRule.class, new Element("r1", Decision.DENY, trace));
		DecisionRule replacing = (DecisionRule) Proxy.newProxyInstance(DecisionRule.class.getClassLoader(),
				new Class<?>[]{DecisionRule.class}, (p, method, args) -> {
					switch(method.getName()){
						case "createContext": trace.add("r0.createContext"); return args[0];
						case "evaluate": trace.add("r0.evaluate"); rules.set(1, permit); return Decision.NOT_APPLICABLE;
						default: return null;
					}
				});
		rules.add(replacing);
		rules.add(deny);

		Decision decision = new FirstApplicable<DecisionRule>("test:first-applicable").combine(CONTEXT, rules);

		assertWithMessage("decision").that(decision).isEqualTo(Decision.DENY);
		assertWithMessage("trace").that(trace)
				.containsExactly("r0.createContext", "r0.evaluate", "r1.createContext", "r1.evaluate").inOrder();
	}

	/**
	 * Review RCJ1: a lazy list without random access is read only as far as the combination goes; a
	 * terminal decision of its first element does not read the rest.
	 */
	@Test
	public void lazyListIsReadOnlyAsFarAsTheCombinationGoes()
	{
		List<String> trace = new ArrayList<>();
		List<Decision> source = new LinkedList<>(List.of(Decision.DENY, Decision.PERMIT));
		List<DecisionRule> lazy = com.google.common.collect.Lists.transform(source, d -> {
			if(d == Decision.PERMIT){
				throw new IllegalStateException("the second element must not be read");
			}
			return proxy(DecisionRule.class, new Element("r0", d, trace));
		});

		Decision decision = new FirstApplicable<DecisionRule>("test:first-applicable").combine(CONTEXT, lazy);

		assertWithMessage("decision").that(decision).isEqualTo(Decision.DENY);
		assertWithMessage("trace").that(trace).containsExactly("r0.createContext", "r0.evaluate").inOrder();
	}

	@Test
	public void legacyRuleAlgorithmsAgreeOnEveryListType()
	{
		Function<Element, Rule> rules = CombiningListTypesCharacterizationTest::rule;
		assertSameOnEveryListType("legacy deny-overrides rule", new LegacyDenyOverridesRuleCombineAlgorithm(), rules);
		assertSameOnEveryListType("legacy permit-overrides rule", new LegacyPermitOverridesRuleCombineAlgorithm(), rules);
		assertSameOnEveryListType("legacy ordered deny-overrides rule", new LegacyOrderedDenyOverridesRuleCombineAlgorithm(), rules);
		assertSameOnEveryListType("legacy ordered permit-overrides rule", new LegacyOrderedPermitOverridesRuleCombineAlgorithm(), rules);
	}
}
