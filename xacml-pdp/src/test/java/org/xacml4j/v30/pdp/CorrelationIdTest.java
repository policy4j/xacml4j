package org.xacml4j.v30.pdp;

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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.pdp.request.AbstractRequestContextHandler;
import org.xacml4j.v30.spi.observe.DecisionObserver;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * The names of a {@code decide} call and of its individual decisions come
 * from sequences, not from random numbers (change optimize-pdp-evaluation,
 * group 1). The name of a decision is the correlation id of its call and
 * its number, as an observer forms it (design D34).
 */
public class CorrelationIdTest
{
	/** Records the name of each decision */
	private static final class DecisionIds implements DecisionObserver<PolicyDecisionPoint>
	{
		private final List<String> ids;

		DecisionIds(List<String> ids){
			this.ids = ids;
		}

		@Override
		public PolicyDecisionPoint requestStarted(PolicyDecisionPoint pdp, RequestContext request){
			return pdp;
		}

		@Override
		public PolicyDecisionPoint decisionStarted(PolicyDecisionPoint pdp, RequestContext request, long correlation, int number){
			ids.add(pdp.getCorrelationId(correlation) + "-" + number);
			return pdp;
		}
	}

	/** Records the correlation id of each call, then asks for the given number of individual decisions. */
	private static final class RecordingHandler extends AbstractRequestContextHandler
	{
		private final List<String> correlationIds = new ArrayList<>();
		private final int decisionsPerCall;

		RecordingHandler(int decisionsPerCall){
			super("urn:xacml4j:test:correlation");
			this.decisionsPerCall = decisionsPerCall;
		}

		@Override
		public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context){
			correlationIds.add(context.getCorrelationId());
			List<Result> results = new ArrayList<>();
			for(int i = 0; i < decisionsPerCall; i++){
				results.add(context.requestDecision(request));
			}
			return results;
		}
	}

	private static PolicyDecisionPoint newPdp(AbstractRequestContextHandler handler, List<String> decisionIds)
	{
		CompositeDecisionRule root = Mockito.mock(CompositeDecisionRule.class);
		when(root.createContext(Mockito.any())).thenReturn(Mockito.mock(EvaluationContext.class));
		when(root.evaluate(Mockito.any())).thenReturn(Decision.PERMIT);
		return PolicyDecisionPointBuilder.builder("correlation-test")
				.policyRepository(Mockito.mock(PolicyRepository.class))
				.pip(Mockito.mock(PolicyInformationPoint.class))
				.rootPolicy(root)
				.requestHandler(handler)
				.observer(new DecisionIds(decisionIds))
				.build();
	}

	private static RequestContext request(){
		return RequestContext.builder().returnPolicyIdList(false).build();
	}

	private static String prefixOf(String correlationId){
		return correlationId.substring(0, correlationId.lastIndexOf('-'));
	}

	@Test
	public void correlationIdsOfOnePdpFollowOneSequence()
	{
		RecordingHandler handler = new RecordingHandler(1);
		PolicyDecisionPoint pdp = newPdp(handler, new ArrayList<>());

		pdp.decide(request());
		pdp.decide(request());
		pdp.decide(request());

		String prefix = prefixOf(handler.correlationIds.get(0));
		assertThat(handler.correlationIds)
				.containsExactly(prefix + "-1", prefix + "-2", prefix + "-3")
				.inOrder();
	}

	@Test
	public void sequenceIsHexadecimal()
	{
		RecordingHandler handler = new RecordingHandler(1);
		PolicyDecisionPoint pdp = newPdp(handler, new ArrayList<>());

		for(int i = 0; i < 17; i++){
			pdp.decide(request());
		}

		String prefix = prefixOf(handler.correlationIds.get(0));
		// call n (from 1) has the sequence n in hexadecimal
		assertThat(handler.correlationIds.get(9)).isEqualTo(prefix + "-a");
		assertThat(handler.correlationIds.get(14)).isEqualTo(prefix + "-f");
		assertThat(handler.correlationIds.get(15)).isEqualTo(prefix + "-10");
		assertThat(handler.correlationIds.get(16)).isEqualTo(prefix + "-11");
	}

	@Test
	public void concurrentCallsGetDistinctCorrelationIds() throws Exception
	{
		int threads = 8;
		int callsPerThread = 250;
		java.util.Queue<String> ids = new java.util.concurrent.ConcurrentLinkedQueue<>();
		AbstractRequestContextHandler collecting = new AbstractRequestContextHandler("urn:xacml4j:test:concurrent"){
			@Override
			public Collection<Result> handle(RequestContext request, PolicyDecisionPointContext context){
				ids.add(context.getCorrelationId());
				return List.of(context.requestDecision(request));
			}
		};
		// The observer runs on every worker thread, so its list must be thread-safe (review rv010 N2).
		List<String> decisionIds = java.util.Collections.synchronizedList(new ArrayList<>());
		PolicyDecisionPoint pdp = newPdp(collecting, decisionIds);
		java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
		try{
			java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
			List<java.util.concurrent.Future<?>> done = new ArrayList<>();
			for(int t = 0; t < threads; t++){
				done.add(pool.submit(() -> {
					start.await();
					for(int i = 0; i < callsPerThread; i++){
						pdp.decide(request());
					}
					return null;
				}));
			}
			start.countDown();
			for(java.util.concurrent.Future<?> f : done){
				f.get(30, java.util.concurrent.TimeUnit.SECONDS);
			}
		}finally{
			pool.shutdownNow();
		}

		assertThat(ids).hasSize(threads * callsPerThread);
		assertThat(new java.util.HashSet<>(ids)).hasSize(threads * callsPerThread);
		assertThat(decisionIds).hasSize(threads * callsPerThread);
		assertThat(new java.util.HashSet<>(decisionIds)).hasSize(threads * callsPerThread);
	}

	@Test
	public void twoPdpsNumberTheirCallsAlike()
	{
		RecordingHandler first = new RecordingHandler(1);
		RecordingHandler second = new RecordingHandler(1);
		PolicyDecisionPoint a = newPdp(first, new ArrayList<>());
		PolicyDecisionPoint b = newPdp(second, new ArrayList<>());

		a.decide(request());
		b.decide(request());
		a.decide(request());
		b.decide(request());

		for(RecordingHandler handler : List.of(first, second)){
			String prefix = prefixOf(handler.correlationIds.get(0));
			assertThat(handler.correlationIds)
					.containsExactly(prefix + "-1", prefix + "-2")
					.inOrder();
		}
	}

	@Test
	public void theDecisionOfOneRequestIsNumberOneOfTheContextsCorrelationId()
	{
		RecordingHandler handler = new RecordingHandler(1);
		List<String> decisionIds = new ArrayList<>();
		PolicyDecisionPoint pdp = newPdp(handler, decisionIds);

		pdp.decide(request());

		assertThat(decisionIds).containsExactly(handler.correlationIds.get(0) + "-1");
	}

	@Test
	public void individualDecisionsOfOneCallAreNumberedFromOne()
	{
		RecordingHandler handler = new RecordingHandler(2);
		List<String> decisionIds = new ArrayList<>();
		PolicyDecisionPoint pdp = newPdp(handler, decisionIds);

		pdp.decide(request());
		pdp.decide(request());

		String firstCall = handler.correlationIds.get(0);
		String secondCall = handler.correlationIds.get(1);
		assertThat(decisionIds)
				.containsExactly(firstCall + "-1", firstCall + "-2", secondCall + "-1", secondCall + "-2")
				.inOrder();
	}
}
