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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

/** Regenerates the recorded Git blob ids for the vendored standards files. */
public final class GenerateVendoredFixtureManifest
{
	static final String MANIFEST_PATH = "xacml-conformance-tests/src/test/integrity/vendored-fixtures.sha1";
	static final String FIXTURE_ROOT_PROPERTY = "vendored.fixture.root";

	private static final String[] FIXTURE_DIRECTORIES = {
			"xacml-conformance-tests/src/test/resources",
			"xacml-xml-jaxb/src/main/resources/xsd"
	};

	private GenerateVendoredFixtureManifest()
	{
	}

	public static void main(String[] args) throws IOException
	{
		if (args.length != 1 || !"--write".equals(args[0])) {
			throw new IllegalArgumentException("Usage: GenerateVendoredFixtureManifest --write");
		}
		Path repositoryRoot = findRepositoryRoot(Paths.get("").toAbsolutePath());
		Map<String, String> blobs = collectBlobs(repositoryRoot);
		Path manifest = repositoryRoot.resolve(MANIFEST_PATH);
		Files.createDirectories(manifest.getParent());
		StringBuilder contents = new StringBuilder();
		blobs.forEach((path, blob) -> contents.append(blob).append('\t').append(path).append('\n'));
		Files.writeString(manifest, contents.toString(), StandardCharsets.UTF_8);
		System.out.println("Wrote " + blobs.size() + " vendored fixture blobs to " + MANIFEST_PATH);
	}

	static Path findRepositoryRoot(Path start)
	{
		Path candidate = start.toAbsolutePath().normalize();
		while (candidate != null) {
			boolean hasFixtureDirectories = true;
			for (String directory : FIXTURE_DIRECTORIES) {
				hasFixtureDirectories &= Files.isDirectory(candidate.resolve(directory));
			}
			if (hasFixtureDirectories) {
				return candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException("Could not find repository root containing both vendored fixture sets from " + start);
	}

	static Map<String, String> collectBlobs(Path fixtureRoot) throws IOException
	{
		Map<String, String> blobs = new TreeMap<>();
		for (String directory : FIXTURE_DIRECTORIES) {
			Path root = fixtureRoot.resolve(directory);
			if (!Files.isDirectory(root)) {
				throw new IOException("Vendored fixture directory is missing: " + root);
			}
			try (Stream<Path> paths = Files.walk(root)) {
				for (Path path : paths.filter(candidate -> !candidate.equals(root)
						&& (Files.isRegularFile(candidate, LinkOption.NOFOLLOW_LINKS)
								|| Files.isSymbolicLink(candidate))).toList()) {
					String relativePath = fixtureRoot.relativize(path).toString().replace(path.getFileSystem().getSeparator(), "/");
					if (relativePath.indexOf('\t') >= 0 || relativePath.indexOf('\n') >= 0 || relativePath.indexOf('\r') >= 0) {
						throw new IOException("Vendored path cannot be represented in the tab-separated manifest: " + relativePath);
					}
					blobs.put(relativePath, gitBlobId(blobContent(path)));
				}
			}
		}
		return blobs;
	}

	static Map<String, String> readManifest(Path manifest) throws IOException
	{
		Map<String, String> blobs = new TreeMap<>();
		int lineNumber = 0;
		for (String line : Files.readAllLines(manifest, StandardCharsets.UTF_8)) {
			lineNumber++;
			if (line.isBlank()) {
				continue;
			}
			int separator = line.indexOf('\t');
			if (separator != 40 || line.indexOf('\t', separator + 1) >= 0) {
				throw new IOException("Invalid manifest row " + lineNumber + ": expected <40-char blob id><TAB><repo-relative path>");
			}
			String blob = line.substring(0, separator);
			String path = line.substring(separator + 1);
			if (!blob.matches("[0-9a-f]{40}") || path.isBlank() || path.startsWith("/")) {
				throw new IOException("Invalid manifest row " + lineNumber + ": " + line);
			}
			if (blobs.put(path, blob) != null) {
				throw new IOException("Duplicate manifest path on row " + lineNumber + ": " + path);
			}
		}
		return blobs;
	}

	static List<String> differences(Map<String, String> expected, Map<String, String> actual)
	{
		List<String> differences = new ArrayList<>();
		for (String path : new TreeSet<>(union(expected, actual))) {
			String expectedBlob = expected.get(path);
			String actualBlob = actual.get(path);
			if (expectedBlob == null) {
				differences.add("ADDED " + path + " expected=<absent> actual=" + actualBlob);
			} else if (actualBlob == null) {
				differences.add("MISSING " + path + " expected=" + expectedBlob + " actual=<missing>");
			} else if (!expectedBlob.equals(actualBlob)) {
				differences.add("CHANGED " + path + " expected=" + expectedBlob + " actual=" + actualBlob);
			}
		}
		return differences;
	}

	private static TreeSet<String> union(Map<String, String> expected, Map<String, String> actual)
	{
		TreeSet<String> paths = new TreeSet<>(expected.keySet());
		paths.addAll(actual.keySet());
		return paths;
	}

	private static byte[] blobContent(Path path) throws IOException
	{
		if (Files.isSymbolicLink(path)) {
			String target = Files.readSymbolicLink(path).toString().replace(path.getFileSystem().getSeparator(), "/");
			return target.getBytes(StandardCharsets.UTF_8);
		}
		return Files.readAllBytes(path);
	}

	static String gitBlobId(byte[] content)
	{
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			sha1.update(("blob " + content.length + '\0').getBytes(StandardCharsets.US_ASCII));
			return HexFormat.of().formatHex(sha1.digest(content));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("The JDK does not provide SHA-1", e);
		}
	}
}
