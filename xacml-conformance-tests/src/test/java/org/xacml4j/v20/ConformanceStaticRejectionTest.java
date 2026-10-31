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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentest4j.AssertionFailedError;

public class ConformanceStaticRejectionTest
{
	// IIA004Special.txt and IIC012Special.txt allow the named static defect to be rejected at load time.
	@ParameterizedTest
	@CsvSource({"oasis20,IIA004", "oasis20,IIC012", "oasis30,IIA004", "oasis30,IIC012"})
	public void acceptsRejectionForTheNamedStaticDefect(String suite, String caseId) throws Exception
	{
		assertDoesNotThrow(() -> ConformanceCaseRunner.verifyOasis(testCase(suite, caseId, caseId, caseId)));
	}

	@ParameterizedTest
	@CsvSource({"oasis20,IIA004", "oasis20,IIC012", "oasis30,IIA004", "oasis30,IIC012"})
	public void loadedPolicyMustStillProduceTheExpectedResponse(String suite, String caseId) throws Exception
	{
		// IIA001 loads successfully and returns Permit, unlike these cases' expected Indeterminate.
		ConformanceManifest.Case testCase = testCase(suite, caseId, "IIA001", caseId);
		AssertionError failure = assertThrows(AssertionError.class, () -> ConformanceCaseRunner.verifyOasis(testCase));
		AssertionFailedError comparison = assertInstanceOf(AssertionFailedError.class, failure);
		assertEquals("INDETERMINATE", comparison.getExpected().getValue().toString());
		assertEquals("PERMIT", comparison.getActual().getValue().toString());
	}

	@ParameterizedTest
	@CsvSource({"oasis20,IIA004", "oasis20,IIC012", "oasis30,IIA004", "oasis30,IIC012"})
	public void acceptsTheResponseWhenPolicyLoadingSucceeds(String suite, String caseId) throws Exception
	{
		// A loadable control exercises the response branch using the same unmodified request and response.
		assertDoesNotThrow(() -> ConformanceCaseRunner.verifyOasis(testCase(suite, caseId, "IIA001", "IIA001")));
	}

	@ParameterizedTest
	@CsvSource({"oasis20,IIA004,IIC012", "oasis20,IIC012,IIA004",
			"oasis30,IIA004,IIC012", "oasis30,IIC012,IIA004"})
	public void rejectsAnUnrelatedPolicyLoadingFailure(String suite, String caseId, String unrelated) throws Exception
	{
		ConformanceManifest.Case testCase = testCase(suite, caseId, unrelated, caseId);
		AssertionError failure = assertThrows(AssertionError.class, () -> ConformanceCaseRunner.verifyOasis(testCase));
		assertTrue(failure.getMessage().contains("Cannot import " + suite + "/" + caseId));
		assertTrue(failure.getMessage().contains(unrelated + "Policy.xml"));
	}

	private static ConformanceManifest.Case testCase(String suite, String caseId, String policyCase,
			String responseCase) throws Exception
	{
		ConformanceManifest.Case original = ConformanceManifest.cases(suite)
				.filter(testCase -> caseId.equals(testCase.caseId())).findFirst().orElseThrow();
		String directory = original.requestFile().substring(0, original.requestFile().lastIndexOf('/') + 1);
		return new ConformanceManifest.Case(suite, caseId, directory + policyCase + "Request.xml",
				List.of(directory + policyCase + "Policy.xml"), directory + responseCase + "Response.xml",
				"special-harness", true, "Verify the named static rejection or the response", "");
	}
}
