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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;

/** Every case of the corpus gives its whole expected result, with and without the target index. */
public class RulesCorpusTest
{
	@ParameterizedTest(name = "rules:{0}, targetIndex={1}")
	@CsvSource({"1, false", "2, true", "3, false", "8, true", "100, false", "100, true"})
	public void everyCaseGivesItsWholeExpectedResult(int count, boolean targetIndex) throws Exception {
		try(PdpFixture fixture = PdpFixture.open("in-memory", "rules:" + count, targetIndex)){
			assertThat(fixture.requests("all").stream().map(CorpusRequest::id).toList()).containsExactly("integer-ranges",
					"date-ranges", "repeated-conditions", "multi-valued", "narrow", "wide", "responses", "regex-targets",
					"regex-conditions", "regex-any-of-all", "regex-malformed").inOrder();
			fixture.verifyConcurrent(2);
		}
	}

	@Test
	public void countIsFromOneToTenThousand() throws Exception {
		assertThat(PolicyCorpusProvider.corpus("rules").name()).isEqualTo("rules:100");
		assertThrows(IllegalArgumentException.class, () -> PolicyCorpusProvider.corpus("rules:0"));
		assertThrows(IllegalArgumentException.class, () -> PolicyCorpusProvider.corpus("rules:10001"));
		assertThrows(IllegalArgumentException.class, () -> PolicyCorpusProvider.corpus("rules:x"));
	}
}
