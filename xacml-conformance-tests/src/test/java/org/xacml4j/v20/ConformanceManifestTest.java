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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ConformanceManifestTest
{
	private static final String HEADER = "suite,case_id,request_file,policy_files,"
			+ "expected_response_file,disposition,enabled,reason,bug_id\n";

	@TempDir
	Path temporaryDirectory;

	@Test
	public void rejectsFixtureWithoutManifestRow() throws Exception
	{
		fixture("oasis-xacml20-compat-test/IIA999Request.xml");
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> ConformanceManifest.validateFixtureInventory(temporaryDirectory, List.of()));
		assertTrue(failure.getMessage().contains("oasis20/IIA999"));
		assertTrue(failure.getMessage().contains("missing manifest row"));
	}

	@Test
	public void rejectsManifestRowWithoutFixture() throws Exception
	{
		List<ConformanceManifest.Case> cases = read(row("IIA999", "executed", "true", "Compare response", ""));
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> ConformanceManifest.validateFixtureInventory(temporaryDirectory, cases));
		assertTrue(failure.getMessage().contains("oasis20/IIA999"));
		assertTrue(failure.getMessage().contains("missing request fixture"));
	}

	@Test
	public void rejectsNestedActiveFixtureWithoutManifestRow() throws Exception
	{
		fixture("oasis-xacml20-compat-test/new-group/IIA999Request.xml");
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> ConformanceManifest.validateFixtureInventory(temporaryDirectory, List.of()));
		assertTrue(failure.getMessage().contains("oasis20/IIA999"));
		assertTrue(failure.getMessage().contains("missing manifest row"));
	}

	@Test
	public void rejectsDuplicateRequestFixtureId() throws Exception
	{
		fixture("oasis-xacml20-compat-test/IIA999Request.xml");
		fixture("oasis-xacml20-compat-test/new-group/IIA999Request.xml");
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> ConformanceManifest.validateFixtureInventory(temporaryDirectory, List.of()));
		assertTrue(failure.getMessage().contains("duplicate request fixture oasis20/IIA999"));
	}

	@Test
	public void rejectsMissingPolicyAndResponseFixtures() throws Exception
	{
		fixture("oasis-xacml20-compat-test/IIA999Request.xml");
		List<ConformanceManifest.Case> cases = read(row("IIA999", "executed", "true", "Compare response", ""));
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> ConformanceManifest.validateFixtureInventory(temporaryDirectory, cases));
		assertTrue(failure.getMessage().contains("IIA999Policy.xml"));
		assertTrue(failure.getMessage().contains("IIA999Response.xml"));
	}

	@Test
	public void rejectsDuplicateSuiteAndCase() throws Exception
	{
		String row = row("IIA999", "executed", "true", "Compare response", "");
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> read(row + row));
		assertTrue(failure.getMessage().contains("duplicate oasis20/IIA999"));
	}

	@Test
	public void rejectsSkippedCaseWithoutReason() throws Exception
	{
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> read(row("IIA999", "skipped", "false", "", "BUG-TBD-IIA999")));
		assertTrue(failure.getMessage().contains("reason"));
	}

	@Test
	public void rejectsDisabledCaseWithoutBugId() throws Exception
	{
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> read(row("IIA999", "skipped", "false", "Engine returns the wrong decision", "")));
		assertTrue(failure.getMessage().contains("bug id"));
	}

	@Test
	public void rejectsDisabledExecutedDisposition() throws Exception
	{
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> read(row("IIA999", "executed", "false", "Engine error", "BUG-TBD-IIA999")));
		assertTrue(failure.getMessage().contains("executed case must be enabled"));
	}

	@Test
	public void enumeratesEveryManifestRowIncludingLastCase() throws Exception
	{
		List<ConformanceManifest.Case> cases = read(row("IIA001", "executed", "true", "Compare response", "")
				+ row("IIA024", "executed", "true", "Compare response", ""));
		assertEquals(List.of("IIA001", "IIA024"),
				cases.stream().map(ConformanceManifest.Case::caseId).toList());
	}

	@Test
	public void manifestExactlyCoversActiveVendoredRequests() throws Exception
	{
		List<ConformanceManifest.Case> cases = ConformanceManifest.read(ConformanceManifest.MANIFEST_PATH);
		ConformanceManifest.validateFixtureInventory(Path.of("src/test/resources"), cases);
		assertEquals(Map.of("oasis20", 374L, "oasis30", 487L, "rsa2008", 17L, "example", 2L),
				cases.stream().collect(Collectors.groupingBy(ConformanceManifest.Case::suite, Collectors.counting())));
	}

	@Test
	public void referenceCasesListTheirRequiredRepositoryPolicies() throws Exception
	{
		for (String id : List.of("IIE001", "IIE002")) {
			ConformanceManifest.Case testCase = ConformanceManifest.cases("oasis30")
					.filter(entry -> id.equals(entry.caseId())).findFirst().orElseThrow();
			assertEquals(3, testCase.policyFiles().size(), id + " needs the root and two referenced policies");
		}
	}

	@Test
	public void rejectsResourcePathOutsideFixtureDirectory() throws Exception
	{
		IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
				() -> read(row("IIA999", "executed", "true", "Compare response", "")
						.replace("oasis-xacml20-compat-test/IIA999Policy.xml", "../IIA999Policy.xml")));
		assertTrue(failure.getMessage().contains("Invalid resource path"));
	}

	private List<ConformanceManifest.Case> read(String rows) throws Exception
	{
		Path manifest = temporaryDirectory.resolve("manifest.csv");
		Files.writeString(manifest, HEADER + rows);
		return ConformanceManifest.read(manifest);
	}

	private void fixture(String resource) throws Exception
	{
		Path fixture = temporaryDirectory.resolve(resource);
		Files.createDirectories(fixture.getParent());
		Files.writeString(fixture, "<fixture/>");
	}

	private static String row(String id, String disposition, String enabled, String reason, String bug)
	{
		return "oasis20," + id + ",oasis-xacml20-compat-test/" + id + "Request.xml,"
				+ "oasis-xacml20-compat-test/" + id + "Policy.xml,"
				+ "oasis-xacml20-compat-test/" + id + "Response.xml,"
				+ disposition + "," + enabled + "," + reason + "," + bug + "\n";
	}
}
