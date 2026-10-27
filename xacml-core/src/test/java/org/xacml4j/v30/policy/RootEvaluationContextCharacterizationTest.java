package org.xacml4j.v30.policy;

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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Today's bookkeeping of {@link RootEvaluationContext}, which cycle E of change
 * optimize-pdp-evaluation must keep (design D9, review RVP): the policy id list in id order
 * with one entry per id, advice and obligations kept per effect and merged by id, rule
 * statuses, empty results when nothing was recorded, and no lost write when threads write
 * first at the same time.
 */
public class RootEvaluationContextCharacterizationTest
{
	private EvaluationContextHandler handler;
	private PolicyReferenceResolver resolver;

	@BeforeEach
	public void init()
	{
		handler = mock(EvaluationContextHandler.class);
		resolver = mock(PolicyReferenceResolver.class);
	}

	private RootEvaluationContext context()
	{
		return new RootEvaluationContext(false, Duration.ZERO, resolver, handler);
	}

	private static CompositeDecisionRule policy(String id, CompositeDecisionRuleIDReference reference)
	{
		CompositeDecisionRule p = mock(CompositeDecisionRule.class);
		when(p.getId()).thenReturn(id);
		when(p.getReference()).thenReturn(reference);
		return p;
	}

	@Test
	public void nothingRecordedGivesEmptyResults()
	{
		RootEvaluationContext context = context();
		DecisionRule rule = mock(DecisionRule.class);

		assertThat(context.getEvaluatedPolicies()).isEmpty();
		assertThat(context.getMatchingAdvices(Decision.PERMIT)).isEmpty();
		assertThat(context.getMatchingAdvices(Decision.DENY)).isEmpty();
		assertThat(context.getMatchingObligations(Decision.PERMIT)).isEmpty();
		assertThat(context.getMatchingObligations(Decision.DENY)).isEmpty();
		assertThat(context.getEvaluationStatus(rule).isPresent()).isFalse();
		assertThat(context.getEvaluationStatus().isPresent()).isFalse();
		assertThat(context).isEqualTo(context);
		assertThat(context.toString()).contains("evaluatedPolicies={}");
	}

	@Test
	public void evaluatedPoliciesAreInIdOrderWithOneEntryPerId()
	{
		RootEvaluationContext context = context();
		CompositeDecisionRuleIDReference a1 = mock(CompositeDecisionRuleIDReference.class);
		CompositeDecisionRuleIDReference a2 = mock(CompositeDecisionRuleIDReference.class);
		CompositeDecisionRuleIDReference b = mock(CompositeDecisionRuleIDReference.class);
		CompositeDecisionRuleIDReference c = mock(CompositeDecisionRuleIDReference.class);

		context.addEvaluationResult(policy("urn:b", b), Decision.DENY);
		context.addEvaluationResult(policy("urn:a", a1), Decision.PERMIT);
		context.addEvaluationResult(policy("urn:c", c), Decision.PERMIT);
		context.addEvaluationResult(policy("urn:a", a2), Decision.DENY);

		assertThat(context.getEvaluatedPolicies()).containsExactly(a2, b, c).inOrder();
		context.clear();
		assertThat(context.getEvaluatedPolicies()).isEmpty();
	}

	@Test
	public void adviceAndObligationsAreKeptPerEffectAndMergedById()
	{
		RootEvaluationContext context = context();
		Advice first = Advice.builder("advice-1", Effect.PERMIT).attribute("a", XacmlTypes.STRING.ofAny("x")).build();
		Advice second = Advice.builder("advice-1", Effect.PERMIT).attribute("a", XacmlTypes.STRING.ofAny("y")).build();
		Obligation deny = Obligation.builder("obligation-1", Effect.DENY).attribute("o", XacmlTypes.STRING.ofAny("z")).build();

		context.addAdvices(Decision.PERMIT, List.of(first));
		context.addAdvices(Decision.PERMIT, List.of(second));
		context.addObligations(Decision.DENY, List.of(deny));
		context.addAdvices(Decision.NOT_APPLICABLE, List.of(Advice.builder("ignored", Effect.PERMIT).build()));
		context.addObligations(Decision.INDETERMINATE, List.of(Obligation.builder("ignored", Effect.DENY).build()));

		assertThat(context.getMatchingAdvices(Decision.PERMIT)).containsExactly(first.merge(second));
		assertThat(context.getMatchingAdvices(Decision.DENY)).isEmpty();
		assertThat(context.getMatchingObligations(Decision.DENY)).containsExactly(deny);
		assertThat(context.getMatchingObligations(Decision.PERMIT)).isEmpty();
	}

	@Test
	public void ruleStatusesAreKeptPerRuleAndANullRuleSetsTheDecisionStatus()
	{
		RootEvaluationContext context = context();
		DecisionRule rule = mock(DecisionRule.class);
		DecisionRule other = mock(DecisionRule.class);
		Status ruleStatus = Status.processingError().build();
		Status decisionStatus = Status.syntaxError().build();

		context.setEvaluationStatus(rule, ruleStatus);
		context.setEvaluationStatus(null, decisionStatus);

		assertThat(context.getEvaluationStatus(rule).get()).isSameInstanceAs(ruleStatus);
		assertThat(context.getEvaluationStatus(other).isPresent()).isFalse();
		assertThat(context.getEvaluationStatus().get()).isSameInstanceAs(decisionStatus);
	}

	@Test
	public void concurrentFirstWritesAreAllKept() throws Exception
	{
		int threads = 8;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try{
			for(int round = 0; round < 200; round++){
				RootEvaluationContext context = context();
				CyclicBarrier start = new CyclicBarrier(threads);
				List<Future<?>> done = new ArrayList<>();
				List<DecisionRule> rules = new ArrayList<>();
				List<Status> statuses = new ArrayList<>();
				for(int t = 0; t < threads; t++){
					String id = "urn:policy:" + t;
					CompositeDecisionRule p = policy(id, mock(CompositeDecisionRuleIDReference.class));
					Advice advice = Advice.builder("advice-" + t, Effect.PERMIT).build();
					DecisionRule rule = mock(DecisionRule.class);
					Status status = Status.processingError().message("rule " + t).build();
					rules.add(rule);
					statuses.add(status);
					done.add(pool.submit(() -> {
						start.await(10, TimeUnit.SECONDS);
						context.addEvaluationResult(p, Decision.PERMIT);
						context.addAdvices(Decision.PERMIT, List.of(advice));
						context.setEvaluationStatus(rule, status);
						return null;
					}));
				}
				for(Future<?> f : done){
					f.get(20, TimeUnit.SECONDS);
				}
				assertThat(context.getEvaluatedPolicies()).hasSize(threads);
				assertThat(context.getMatchingAdvices(Decision.PERMIT)).hasSize(threads);
				for(int t = 0; t < threads; t++){
					assertThat(context.getEvaluationStatus(rules.get(t)).get()).isSameInstanceAs(statuses.get(t));
				}
			}
		}finally{
			pool.shutdownNow();
		}
	}

	/** Reviews RVC1 and RVC2: a view kept before the first advice or obligation sees later ones. */
	@Test
	public void responseViewsKeptBeforeTheFirstWriteSeeLaterWrites()
	{
		RootEvaluationContext context = context();
		java.util.Collection<Advice> permitAdvice = context.getMatchingAdvices(Decision.PERMIT);
		java.util.Collection<Obligation> denyObligations = context.getMatchingObligations(Decision.DENY);
		Advice advice = Advice.builder("advice-1", Effect.PERMIT).build();
		Obligation obligation = Obligation.builder("obligation-1", Effect.DENY).build();

		assertThat(permitAdvice).isEmpty();
		// The concurrent values view rejects null, before and after the first write (review rv009)
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> permitAdvice.contains(null));
		context.addAdvices(Decision.PERMIT, List.of(advice));
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> permitAdvice.contains(null));
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> denyObligations.contains(null));
		context.addObligations(Decision.DENY, List.of(obligation));

		assertThat(permitAdvice).containsExactly(advice);
		assertThat(denyObligations).containsExactly(obligation);
		assertThat(permitAdvice.remove(advice)).isTrue();
		assertThat(context.getMatchingAdvices(Decision.PERMIT)).isEmpty();
	}

	/** Review RVC2: the empty policy id list is a new modifiable list, as the collector gave. */
	@Test
	public void emptyPolicyListIsModifiable()
	{
		RootEvaluationContext context = context();

		context.getEvaluatedPolicies().add(mock(CompositeDecisionRuleIDReference.class));

		assertThat(context.getEvaluatedPolicies()).isEmpty();
	}

	/** Review RVC2: a null rule is rejected before and after the first status write. */
	@Test
	public void nullRuleStatusLookupIsRejected()
	{
		RootEvaluationContext context = context();

		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> context.getEvaluationStatus(null));
		context.setEvaluationStatus(mock(DecisionRule.class), Status.processingError().build());
		org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> context.getEvaluationStatus(null));
	}

	/**
	 * Review rv010: a kept obligation view removes conditionally, as the concurrent map's values
	 * view does; an obligation merged while removeIf tests it stays.
	 */
	@Test
	public void keptViewRemovesOnlyTheTestedResponse() throws Exception
	{
		RootEvaluationContext context = context();
		java.util.Collection<Obligation> kept = context.getMatchingObligations(Decision.DENY);
		context.addObligations(Decision.DENY, List.of(Obligation.builder("obligation-1", Effect.DENY)
				.attribute("a", XacmlTypes.STRING.ofAny("x")).build()));
		java.util.concurrent.CountDownLatch inPredicate = new java.util.concurrent.CountDownLatch(1);
		java.util.concurrent.CountDownLatch resume = new java.util.concurrent.CountDownLatch(1);
		ExecutorService pool = Executors.newSingleThreadExecutor();
		try{
			Future<Boolean> removal = pool.submit(() -> kept.removeIf(o -> {
				inPredicate.countDown();
				try{
					// A missed release fails the test instead of skipping the removal (review rv011)
					if(!resume.await(10, TimeUnit.SECONDS)){
						throw new AssertionError("removal not resumed");
					}
					return true;
				}catch(InterruptedException e){
					throw new IllegalStateException(e);
				}
			}));
			org.junit.jupiter.api.Assertions.assertTrue(inPredicate.await(10, TimeUnit.SECONDS));
			context.addObligations(Decision.DENY, List.of(Obligation.builder("obligation-1", Effect.DENY)
					.attribute("a", XacmlTypes.STRING.ofAny("y")).build()));
			resume.countDown();
			removal.get(20, TimeUnit.SECONDS);

			assertThat(context.getMatchingObligations(Decision.DENY)).hasSize(1);
			assertThat(context.getMatchingObligations(Decision.DENY).iterator().next().getAttributes()).hasSize(2);
		}finally{
			pool.shutdownNow();
		}
	}
}
