package org.xacml4j.jmh.pdp;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.jmh.harness.LookupQuery;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyCorpora;
import org.xacml4j.jmh.harness.Rules;
import org.xacml4j.jmh.testing.JmhParams;
import org.xacml4j.jmh.testing.RecordingPolicyRepository.Read;
import org.xacml4j.jmh.testing.RecordingStore;
import org.xacml4j.v30.CompositeDecisionRule;

/**
 * {@code PolicyStoreLookupBenchmark} outside a JMH run (design D7): each
 * {@code lookup} runs the next query of the corpus, in corpus order, through
 * the repository method of its kind; the {@code cursor} control makes no
 * lookup.
 */
public class PolicyStoreLookupBenchmarkTest
{
	@BeforeEach
	public void resetRecording()
	{
		RecordingStore.reset();
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"rsa2008", "synthetic:100"})
	public void lookupRunsTheCorpusQueriesInOrderWithTheMethodOfTheirKind(String corpus) throws Exception
	{
		List<LookupQuery> queries = LookupQuery.of(PdpFixture.parse(PolicyCorpora.load(corpus).policies()));
		PolicyStoreLookupBenchmark benchmark = new PolicyStoreLookupBenchmark();
		benchmark.store = RecordingStore.NAME;
		benchmark.corpus = corpus;
		benchmark.setUp();
		try{
			PolicyStoreLookupBenchmark.Cursor cursor = new PolicyStoreLookupBenchmark.Cursor();
			cursor.setUp(benchmark, JmhParams.thread(0, 1));
			int start = RecordingStore.count();
			int operations = queries.size() + 1;
			List<Object> found = new ArrayList<>();
			for(int k = 0; k < operations; k++){
				found.add(benchmark.lookup(cursor));
			}

			List<Read> expected = new ArrayList<>();
			for(int k = 0; k < operations; k++){
				LookupQuery query = queries.get(k % queries.size());
				expected.add(new Read(method(query.kind()), query.id()));
			}
			List<Read> reads = RecordingStore.reads();
			assertThat(reads.subList(start, reads.size())).containsExactlyElementsIn(expected).inOrder();
			for(int k = 0; k < operations; k++){
				LookupQuery query = queries.get(k % queries.size());
				CompositeDecisionRule rule = rule(found.get(k));
				assertWithMessage("lookup %s: %s", k, query.label())
						.that(Rules.sameIdentity(query.expected(), rule)).isTrue();
			}
		}finally{
			benchmark.tearDown();
		}
	}

	@Test
	public void cursorControlMakesNoLookup() throws Exception
	{
		PolicyStoreLookupBenchmark benchmark = new PolicyStoreLookupBenchmark();
		benchmark.store = RecordingStore.NAME;
		benchmark.corpus = "rsa2008";
		benchmark.setUp();
		try{
			PolicyStoreLookupBenchmark.Cursor cursor = new PolicyStoreLookupBenchmark.Cursor();
			cursor.setUp(benchmark, JmhParams.thread(0, 1));
			int start = RecordingStore.count();

			for(int k = 0; k < 50; k++){
				benchmark.cursor(cursor);
			}

			List<Read> reads = RecordingStore.reads();
			assertThat(reads.subList(start, reads.size())).isEmpty();
		}finally{
			benchmark.tearDown();
		}
	}

	private static String method(LookupQuery.Kind kind)
	{
		return switch(kind){
			case EXACT -> "get";
			case POLICY_REFERENCE -> "getPolicy";
			case POLICY_SET_REFERENCE -> "getPolicySet";
		};
	}

	/**
	 * D8 fixes that {@code lookup} returns the found rule, not its type: a
	 * rule, or an {@code Optional} of it as {@link LookupQuery#find} gives.
	 */
	private static CompositeDecisionRule rule(Object found)
	{
		Object value = found instanceof Optional<?> optional ? optional.orElse(null) : found;
		assertWithMessage("result of lookup").that(value).isInstanceOf(CompositeDecisionRule.class);
		return (CompositeDecisionRule)value;
	}
}
