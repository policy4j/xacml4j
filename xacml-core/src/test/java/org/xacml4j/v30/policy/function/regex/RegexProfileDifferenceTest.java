package org.xacml4j.v30.policy.function.regex;

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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.policy.function.regex.RegexEngineContractTest.Expect;

/**
 * The difference matrix between the {@code legacy} profile and the {@code xf} profile on the
 * {@code jdk} engine (change add-regex-engine-spi, design D2): the drifts of the translation
 * before the SPI (research RX1, D1 to D8) and the Java-only syntax it accepts.
 */
public class RegexProfileDifferenceTest
{
	private static final RegexEngine JDK = RegexEngines.engine("jdk");

	static Stream<Arguments> differences()
	{
		return RegexEngineContractTest.rows().filter(row -> row.legacy() != row.xf())
				.map(row -> Arguments.of(row.name(), row.pattern(), row.input(), row.legacy(), row.xf()));
	}

	@ParameterizedTest(name = "{0}: legacy {3}, xf {4}")
	@MethodSource("differences")
	public void legacyKeepsTodaysResultAndXfFollowsTheStandard(String name, String pattern, String input,
			Expect legacy, Expect xf)
	{
		assertEquals(legacy, RegexEngineContractTest.outcome(JDK, RegexProfile.LEGACY, pattern, input), name + " legacy");
		assertEquals(xf, RegexEngineContractTest.outcome(JDK, RegexProfile.XF, pattern, input), name + " xf");
	}
}
