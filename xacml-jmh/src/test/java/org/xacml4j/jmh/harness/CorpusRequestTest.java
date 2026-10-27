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

import static com.google.common.truth.Truth.assertWithMessage;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.RequestContext;

/**
 * The decision check of a fixture: an expected decision from a response
 * matches the PDP decision (spec "Decisions and lookups are checked before
 * measurement").
 */
public class CorpusRequestTest
{
	/**
	 * A serialized response carries only Permit, Deny, Indeterminate, or
	 * NotApplicable (XACML 3.0 core §5.53, §7.17), so a corpus expectation is
	 * one of those four; the PDP's in-memory {@code Result} can still hold an
	 * extended Indeterminate{D|P|DP} (§7.10), which an expected Indeterminate
	 * must match (review rv007).
	 */
	private static final Set<Decision> RESPONSE_DECISIONS =
			EnumSet.of(Decision.PERMIT, Decision.DENY, Decision.INDETERMINATE, Decision.NOT_APPLICABLE);
	private static final Set<Decision> INDETERMINATE = EnumSet.of(
			Decision.INDETERMINATE, Decision.INDETERMINATE_D, Decision.INDETERMINATE_P, Decision.INDETERMINATE_DP);

	static Stream<Arguments> truthTable()
	{
		Stream.Builder<Arguments> rows = Stream.builder();
		for(Decision expected : RESPONSE_DECISIONS){
			for(Decision actual : Decision.values()){
				boolean matches = expected == Decision.INDETERMINATE ? INDETERMINATE.contains(actual) : expected == actual;
				rows.add(Arguments.of(expected, actual, matches));
			}
		}
		return rows.build();
	}

	@ParameterizedTest(name = "expected {0}, actual {1}: {2}")
	@MethodSource("truthTable")
	public void expectedIndeterminateMatchesEveryIndeterminateAndOtherDecisionsMatchOnlyThemselves(
			Decision expected, Decision actual, boolean matches)
	{
		CorpusRequest request = new CorpusRequest("r", RequestContext.builder().build(), expected);

		assertWithMessage("expected %s, actual %s", expected, actual)
				.that(request.matches(actual)).isEqualTo(matches);
	}
}
