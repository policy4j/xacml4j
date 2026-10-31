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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import org.xacml4j.v30.SyntaxException;

/**
 * Maintainer ruling 2026-10-07 (run 061 D12, bead policy4j-the.4.17): the
 * PDP API takes a parsed request, so the adapter in front of it maps a
 * request that cannot be parsed to an Indeterminate response with status
 * syntax-error. The conformance harness is that adapter for IIA005, whose
 * request has an Attribute without AttributeId.
 */
public class ConformanceRequestSyntaxErrorTest
{
	@ParameterizedTest
	@ValueSource(strings = {"oasis20", "oasis30"})
	public void mapsTheRequestSyntaxErrorOfIIA005ToTheExpectedResponse(String suite) throws Exception
	{
		assertDoesNotThrow(() -> ConformanceCaseRunner.verifyOasis(
				testCase(suite, "IIA005", "IIA005", "IIA005", "special-harness")));
	}

	@ParameterizedTest
	@ValueSource(strings = {"oasis20", "oasis30"})
	public void theMappedResponseMustStillMatchTheExpectedResponse(String suite) throws Exception
	{
		// The response fixture of IIA001 expects Permit; the mapped response is Indeterminate.
		ConformanceManifest.Case testCase = testCase(suite, "IIA005", "IIA005", "IIA001", "special-harness");
		AssertionError failure = assertThrows(AssertionError.class, () -> ConformanceCaseRunner.verifyOasis(testCase));
		AssertionFailedError comparison = assertInstanceOf(AssertionFailedError.class, failure);
		assertEquals("PERMIT", comparison.getExpected().getValue().toString());
		assertEquals("INDETERMINATE", comparison.getActual().getValue().toString());
	}

	@ParameterizedTest
	@ValueSource(strings = {"oasis20", "oasis30"})
	public void doesNotMapTheSyntaxErrorForAnotherCase(String suite) throws Exception
	{
		// The same invalid request under another case id: the mapping is for the named case only.
		ConformanceManifest.Case testCase = testCase(suite, "IIA001", "IIA005", "IIA005", "special-harness");
		SyntaxException failure = assertThrows(SyntaxException.class, () -> ConformanceCaseRunner.verifyOasis(testCase));
		assertTrue(failure.getStatus().getStatusCode().isSyntaxError());
	}

	@ParameterizedTest
	@ValueSource(strings = {"oasis20", "oasis30"})
	public void doesNotMapTheSyntaxErrorForAnExecutedCase(String suite) throws Exception
	{
		ConformanceManifest.Case testCase = testCase(suite, "IIA005", "IIA005", "IIA005", "executed");
		assertThrows(SyntaxException.class, () -> ConformanceCaseRunner.verifyOasis(testCase));
	}

	/**
	 * rv083: the guard accepts only the named defect. Another request syntax
	 * error in the same case is not mapped.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"oasis20", "oasis30"})
	public void acceptsOnlyTheMessageOfTheMissingAttributeId(String suite) throws Exception
	{
		ConformanceManifest.Case named = testCase(suite, "IIA005", "IIA005", "IIA005", "special-harness");
		assertTrue(ConformanceCaseRunner.expectedRequestSyntaxError(named,
				new SyntaxException("Request Attribute is missing AttributeId")));
		assertFalse(ConformanceCaseRunner.expectedRequestSyntaxError(named,
				new SyntaxException("Request Attribute is missing DataType")));
		assertFalse(ConformanceCaseRunner.expectedRequestSyntaxError(
				testCase(suite, "IIA005", "IIA005", "IIA005", "executed"),
				new SyntaxException("Request Attribute is missing AttributeId")));
		assertFalse(ConformanceCaseRunner.expectedRequestSyntaxError(
				testCase(suite, "IIA001", "IIA005", "IIA005", "special-harness"),
				new SyntaxException("Request Attribute is missing AttributeId")));
	}

	private static ConformanceManifest.Case testCase(String suite, String caseId, String requestCase,
			String responseCase, String disposition) throws Exception
	{
		ConformanceManifest.Case original = ConformanceManifest.cases(suite)
				.filter(testCase -> "IIA005".equals(testCase.caseId())).findFirst().orElseThrow();
		String directory = original.requestFile().substring(0, original.requestFile().lastIndexOf('/') + 1);
		return new ConformanceManifest.Case(suite, caseId, directory + requestCase + "Request.xml",
				List.of(directory + requestCase + "Policy.xml"), directory + responseCase + "Response.xml",
				disposition, true, "Map the request syntax error or verify the response", "");
	}
}
