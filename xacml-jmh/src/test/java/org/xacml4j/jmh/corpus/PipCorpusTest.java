package org.xacml4j.jmh.corpus;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.types.XacmlTypes;

public class PipCorpusTest
{
	@ParameterizedTest
	@ValueSource(strings = {"serial", "parallel"})
	public void decisionsAndStatusesMatchForEachBackendMode(String mode) throws Exception {
		try(PipWorkload workload = PipWorkload.open(3, 0, mode, "off", 2);
				PdpFixture fixture = PdpFixture.open("in-memory", "pip:3", workload.pip())){
			assertEquals(Decision.PERMIT, result(fixture, "independent").getDecision());
			assertEquals(Decision.PERMIT, result(fixture, "parallel-attributes").getDecision());
			assertEquals(Decision.DENY, result(fixture, "dependent").getDecision());
			assertEquals(Decision.PERMIT, result(fixture, "shared").getDecision());
			assertEquals(Decision.PERMIT, result(fixture, "shared-static").getDecision());
			assertEquals(Decision.NOT_APPLICABLE, result(fixture, "missing-optional").getDecision());
			assertEquals(StatusCodeId.MISSING_ATTRIBUTE, result(fixture, "missing-required").getStatus().getStatusCode().getValue());
			assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, result(fixture, "failure").getStatus().getStatusCode().getValue());
			assertEquals(Decision.PERMIT, result(fixture, "skipped-failure").getDecision());
			assertEquals(Decision.PERMIT, result(fixture, "fallback").getDecision());
			assertEquals(Decision.PERMIT, result(fixture, "selector").getDecision());
			fixture.verifyConcurrent(2);
			fixture.recheck();
		}
	}

	@Test
	public void corpusNameAndFanOutAreValidated() throws Exception {
		assertEquals("pip:8", PolicyCorpusProvider.corpus("pip").name());
		assertEquals("pip:1", PolicyCorpusProvider.corpus("pip:1").name());
		assertThrows(IllegalArgumentException.class, () -> PolicyCorpusProvider.corpus("pip:0"));
		assertThrows(IllegalArgumentException.class, () -> PolicyCorpusProvider.corpus("pip:65"));
		assertThrows(IllegalArgumentException.class, () -> PolicyCorpusProvider.corpus("pip:pip"));
		assertEquals(8, PipCorpus.count("pip"));
		assertEquals(3, PipCorpus.count("pip:3"));
		assertThrows(IllegalArgumentException.class, () -> PipCorpus.count("pip:pip"));
		assertThrows(IllegalArgumentException.class, () -> PipCorpus.count("synthetic:8"));
	}

	@Test
	public void parsedPipCorpusCanBeOpenedDirectly() throws Exception {
		try(PdpFixture fixture = PdpFixture.open("in-memory", PolicyCorpusProvider.corpus("pip:1"))){
			assertEquals(Decision.PERMIT, result(fixture, "independent").getDecision());
		}
	}

	@Test
	public void warmCacheAnswersTheCacheableCasesWithoutTheBackend() throws Exception {
		try(PipWorkload workload = PipWorkload.open(3, 0, "serial", "on", 2);
				PdpFixture fixture = PdpFixture.open("in-memory", "pip:3", workload.pip())){
			workload.verifyCache(fixture);
			assertEquals(Decision.PERMIT, result(fixture, "shared").getDecision());
			assertEquals(Decision.PERMIT, result(fixture, "shared-static").getDecision());
			assertEquals(Decision.PERMIT, result(fixture, "selector").getDecision());
			fixture.recheck();
		}
	}

	@Test
	public void fixtureRejectsWrongResolvedValuesEvenWhenDecisionMatches() throws Exception {
		try(PipWorkload workload = PipWorkload.open(2, 0, "serial", "off", 2)){
			PolicyInformationPoint changed = new DelegatingPip(workload.pip()) {
				@Override
				public Optional<BagOfValues> resolve(EvaluationContext context, AttributeDesignatorKey key){
					Optional<BagOfValues> values = super.resolve(context, key);
					if(key.getAttributeId().equals(PipCorpus.BULK) && values.isPresent()){
						return Optional.of(XacmlTypes.STRING.bagBuilder().attributes(values.get())
								.attribute(XacmlTypes.STRING.ofAny("unexpected")).build());
					}
					return values;
				}
			};
			IllegalStateException error = assertThrows(IllegalStateException.class,
					() -> PdpFixture.open("in-memory", "pip:2", changed));
			assertTrue(error.getMessage().contains("independent"));
		}
	}

	@Test
	public void callerSuppliedPipRemainsCallerOwned() throws Exception {
		try(PipWorkload workload = PipWorkload.open(1, 0, "serial", "off", 1)){
			try(PdpFixture fixture = PdpFixture.open("in-memory", "pip:1", new CallerOwnedPip(workload.pip()))){
				assertEquals(Decision.PERMIT, result(fixture, "independent").getDecision());
			}
		}
	}

	private static Result result(PdpFixture fixture, String id){
		CorpusRequest request = fixture.requests(id).get(0);
		return fixture.pdp().decide(request.request()).getResults().iterator().next();
	}

	private static class DelegatingPip implements PolicyInformationPoint
	{
		private final PolicyInformationPoint delegate;

		private DelegatingPip(PolicyInformationPoint delegate){
			this.delegate = delegate;
		}

		@Override
		public String getId(){ return delegate.getId(); }

		@Override
		public Optional<BagOfValues> resolve(EvaluationContext context, AttributeDesignatorKey key){
			return delegate.resolve(context, key);
		}

		@Override
		public Optional<BagOfValues> resolve(EvaluationContext context, AttributeSelectorKey key){
			return delegate.resolve(context, key);
		}
	}

	private static final class CallerOwnedPip extends DelegatingPip implements AutoCloseable
	{
		private CallerOwnedPip(PolicyInformationPoint delegate){ super(delegate); }

		@Override
		public void close(){ throw new AssertionError("the fixture must not close the caller's PIP"); }
	}
}
