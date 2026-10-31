package org.xacml4j.v20;

/*
 * #%L
 * Xacml4J Conformance Tests
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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.base.Strings;

public class Xacml20ConformanceTest
{
	static Stream<ConformanceManifest.Case> cases() throws Exception
	{
		return ConformanceManifest.cases("oasis20");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("cases")
	public void testConformance(ConformanceManifest.Case testCase) throws Exception
	{
		ConformanceCaseRunner.requireEnabled(testCase);
		ConformanceCaseRunner.verifyOasis(testCase);
	}

	private static String createTestAssetName(String prefix, int testCaseNum, String suffix)
	{
		return prefix + Strings.padStart(Integer.toString(testCaseNum), 3, '0') + suffix;
	}

	@Test
	public void testName()
	{
		assertEquals("AA003B", createTestAssetName("AA", 3, "B"));
	}
}
