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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

final class ConformanceManifest
{
	static final Path MANIFEST_PATH = Path.of("src/test/manifest/conformance-manifest.csv");
	private static final String HEADER = "suite,case_id,request_file,policy_files,"
			+ "expected_response_file,disposition,enabled,reason,bug_id";
	private static final Set<String> SUITES = Set.of("oasis20", "oasis30", "rsa2008", "example");
	private static final Set<String> DISPOSITIONS = Set.of("executed", "special-harness", "skipped");

	private ConformanceManifest() {}

	record Case(String suite, String caseId, String requestFile, List<String> policyFiles,
			String expectedResponseFile, String disposition, boolean enabled, String reason, String bugId)
	{
		Case
		{
			policyFiles = List.copyOf(policyFiles);
		}

		@Override
		public String toString()
		{
			return suite + "/" + caseId;
		}
	}

	static Stream<Case> cases(String suite) throws IOException
	{
		if (!SUITES.contains(suite)) {
			throw new IllegalArgumentException("Unknown conformance suite: " + suite);
		}
		return read(MANIFEST_PATH).stream().filter(testCase -> suite.equals(testCase.suite()));
	}

	static List<Case> read(Path manifest) throws IOException
	{
		List<String> lines = Files.readAllLines(manifest);
		if (lines.isEmpty() || !HEADER.equals(lines.get(0))) {
			throw new IllegalArgumentException("Unexpected conformance manifest header: " + manifest);
		}
		List<Case> cases = new ArrayList<>();
		Set<String> keys = new HashSet<>();
		for (int line = 1; line < lines.size(); line++) {
			String[] fields = lines.get(line).split(",", -1);
			if (fields.length != 9) {
				throw new IllegalArgumentException("Expected nine unquoted CSV fields at " + manifest + ":" + (line + 1));
			}
			String key = fields[0] + "/" + fields[1];
			if (!keys.add(key)) {
				throw new IllegalArgumentException("duplicate " + key);
			}
			if (!SUITES.contains(fields[0]) || fields[1].isBlank()) {
				throw new IllegalArgumentException("Unknown suite or empty case id: " + key);
			}
			if (!DISPOSITIONS.contains(fields[5])) {
				throw new IllegalArgumentException("Unknown disposition for " + key + ": " + fields[5]);
			}
			if (!Set.of("true", "false").contains(fields[6])) {
				throw new IllegalArgumentException("Invalid enabled flag for " + key);
			}
			boolean enabled = Boolean.parseBoolean(fields[6]);
			if ("executed".equals(fields[5]) && !enabled) {
				throw new IllegalArgumentException("executed case must be enabled: " + key);
			}
			if ("skipped".equals(fields[5]) && enabled) {
				throw new IllegalArgumentException("skipped case must be disabled: " + key);
			}
			if (fields[7].isBlank()) {
				throw new IllegalArgumentException("Missing reason for " + key);
			}
			if (!enabled && fields[8].isBlank()) {
				throw new IllegalArgumentException("Missing bug id for disabled case " + key);
			}
			List<String> policies = List.of(fields[3].split(";", -1));
			for (String resource : Stream.concat(Stream.of(fields[2], fields[4]), policies.stream()).toList()) {
				Path path = Path.of(resource);
				if (resource.isBlank() || path.isAbsolute() || path.startsWith("..")
						|| !path.normalize().equals(path) || resource.contains("\\")) {
					throw new IllegalArgumentException("Invalid resource path for " + key + ": " + resource);
				}
			}
			cases.add(new Case(fields[0], fields[1], fields[2], policies, fields[4],
					fields[5], enabled, fields[7], fields[8]));
		}
		return List.copyOf(cases);
	}

	static void validateFixtureInventory(Path resources, List<Case> cases) throws IOException
	{
		Map<String, String> fixtures = new LinkedHashMap<>();
		collect(resources, "oasis20", "oasis-xacml20-compat-test", "", "Request.xml", fixtures);
		collect(resources, "oasis30", "xacml3.0-ct-v.0.4", "", "Request.xml", fixtures);
		collect(resources, "rsa2008", "rsa2008-interop", "XacmlRequest-", ".xml", fixtures);
		if (Files.isDirectory(resources)) {
			try (Stream<Path> paths = Files.list(resources)) {
				for (Path directory : paths.filter(Files::isDirectory)
						.filter(path -> path.getFileName().toString().startsWith("Example_")).sorted().toList()) {
					Path request = directory.resolve("Request.xml");
					if (Files.isRegularFile(request)) {
						fixtures.put("example/" + directory.getFileName(), resources.relativize(request).toString());
					}
				}
			}
		}
		List<String> differences = new ArrayList<>();
		Set<String> keys = new HashSet<>();
		for (Case testCase : cases) {
			String key = testCase.toString();
			if (!keys.add(key)) {
				differences.add("duplicate " + key);
			}
			String fixture = fixtures.get(key);
			if (fixture == null) {
				differences.add(key + ": missing request fixture");
			} else if (!fixture.equals(testCase.requestFile())) {
				differences.add(key + ": request fixture path mismatch: " + fixture);
			}
			for (String resource : Stream.concat(Stream.of(testCase.requestFile(), testCase.expectedResponseFile()),
					testCase.policyFiles().stream()).toList()) {
				if (!Files.isRegularFile(resources.resolve(resource))) {
					differences.add(key + ": missing resource " + resource);
				}
			}
		}
		for (String key : fixtures.keySet()) {
			if (!keys.contains(key)) {
				differences.add(key + ": missing manifest row");
			}
		}
		if (!differences.isEmpty()) {
			throw new IllegalArgumentException("Conformance manifest does not match fixtures:\n" + String.join("\n", differences));
		}
	}

	private static void collect(Path resources, String suite, String directory, String prefix,
			String suffix, Map<String, String> fixtures) throws IOException
	{
		Path folder = resources.resolve(directory);
		if (!Files.isDirectory(folder)) {
			return;
		}
		// The 3.0 suite's xacml3.0-deprecated directory holds 72 superseded requests.
		// Other directories remain in the inventory so newly added cases cannot disappear.
		try (Stream<Path> paths = Files.walk(folder)) {
			for (Path path : paths.filter(Files::isRegularFile)
					.filter(path -> !"oasis30".equals(suite)
							|| !folder.relativize(path).startsWith("xacml3.0-deprecated")).sorted().toList()) {
				String name = path.getFileName().toString();
				if (name.startsWith(prefix) && name.endsWith(suffix)) {
					String id = name.substring(prefix.length(), name.length() - suffix.length());
					String key = suite + "/" + id;
					String previous = fixtures.putIfAbsent(key, resources.relativize(path).toString());
					if (previous != null) {
						throw new IllegalArgumentException("duplicate request fixture " + key + ": "
								+ previous + " and " + resources.relativize(path));
					}
				}
			}
		}
	}
}
