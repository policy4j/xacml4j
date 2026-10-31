package org.xacml4j.jmh.harness;

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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.jmh.testing.RecordingPolicyRepository.Read;
import org.xacml4j.jmh.testing.TestStores;
import org.xacml4j.jmh.testing.TestPdps;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.PolicyDecisionPoint;

/**
 * Characterizes the premise of design D7: the PDP wraps the repository in a
 * {@code DefaultPolicyReferenceResolver} whose reference cache has no bound,
 * so once a request has been decided, deciding it again asks the store
 * nothing, whatever the store and the corpus size. A {@code decide}
 * benchmark therefore does not measure the store. If this test fails, D7 is
 * revisited.
 */
public class ReferenceCacheCharacterizationTest
{
	@BeforeEach
	public void resetRecording()
	{
		TestStores.Recording.reset();
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"rsa2008", "synthetic:100"})
	public void fixturePdpAsksTheStoreNothingAfterTheChecksOfOpen(String corpus) throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open(TestStores.Recording.NAME, corpus)){
			assertWithMessage("store lookups of the checks of open").that(TestStores.Recording.count()).isGreaterThan(0);
			int before = TestStores.Recording.count();

			for(CorpusRequest request : fixture.corpus().requests()){
				fixture.pdp().decide(request.request());
			}

			List<Read> reads = TestStores.Recording.reads();
			assertThat(reads.subList(before, reads.size())).isEmpty();
		}
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"rsa2008", "synthetic:100"})
	public void firstDecideOfARequestAsksTheStoreAndTheSecondAsksNothing(String corpus) throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open(TestStores.Recording.NAME, corpus)){
			CompositeDecisionRule root = fixture.policies().get(fixture.corpus().rootIndex());
			for(CorpusRequest request : fixture.corpus().requests()){
				// a new PDP has a new reference resolver, so its cache starts empty
				try(PolicyDecisionPoint pdp = TestPdps.build(fixture.repository(), root)){
					int start = TestStores.Recording.count();
					pdp.decide(request.request());
					int afterFirst = TestStores.Recording.count();
					pdp.decide(request.request());
					List<Read> reads = TestStores.Recording.reads();

					assertWithMessage("store lookups of the first decide of %s", request.id())
							.that(afterFirst - start).isGreaterThan(0);
					assertWithMessage("store lookups of the second decide of %s", request.id())
							.that(reads.subList(afterFirst, reads.size())).isEmpty();
				}
			}
		}
	}
}
