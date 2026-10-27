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
import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.jmh.testing.TestCorpora;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.marshal.MediaType;

/** Spec requirement "Directory corpus" (design D9), on directories written in {@code @TempDir}. */
public class DirectoryCorpusTest
{
	/** Changes a valid directory corpus so that it has one problem. */
	@FunctionalInterface
	interface Problem
	{
		void introduce(Path dir) throws IOException;
	}

	@Test
	public void validDirectoryGivesItsPoliciesAndRequestsInFileNameOrder(@TempDir Path dir) throws Exception
	{
		TestCorpora.writeDirectory(dir);

		PolicyCorpus corpus = PolicyCorpusProvider.corpus("dir:" + dir);

		assertThat(names(corpus.policies())).containsExactly("documents.xml", "root.xml").inOrder();
		assertThat(corpus.root()).isEqualTo("root.xml");
		for(PolicySource source : corpus.policies()){
			assertWithMessage("media type of %s", source.name())
					.that(source.mediaType()).isEqualTo(MediaType.Type.XACML30_XML);
			assertWithMessage("bytes of %s", source.name())
					.that(source.content()).isEqualTo(Files.readAllBytes(dir.resolve("policies").resolve(source.name())));
		}
		assertThat(stems(corpus.requests())).containsExactly("a-read", "b-delete", "c-audit").inOrder();
		assertThat(decisions(corpus.requests()))
				.containsExactly(Decision.PERMIT, Decision.DENY, Decision.INDETERMINATE).inOrder();
		try(PdpFixture fixture = PdpFixture.open("in-memory", corpus)){
			assertThat(fixture.corpus().requests()).hasSize(3);
		}
	}

	@Test
	public void xacml20DirectoryReadsXacml20PoliciesAndRequests(@TempDir Path dir) throws Exception
	{
		Path policies = Files.createDirectories(dir.resolve("policies"));
		Path requests = Files.createDirectories(dir.resolve("requests"));
		for(String file : List.of(
				"XacmlPolicySet-01-top-level.xml", "XacmlPolicySet-02a-CDA.xml", "XacmlPolicySet-02b-N.xml",
				"XacmlPolicySet-02c-N-PermCollections.xml", "XacmlPolicySet-02d-prog-note.xml",
				"XacmlPolicySet-02e-MA.xml", "XacmlPolicySet-02f-emergency.xml",
				"XacmlPolicySet-03-N-RPS-med-rec-vrole.xml", "XacmlPolicySet-04-N-PPS-PRD-004.xml")){
			Files.write(policies.resolve(file), rsa2008(file));
		}
		for(String file : List.of("XacmlRequest-01-01.xml", "XacmlRequest-01-02.xml", "XacmlRequest-02-03.xml")){
			Files.write(requests.resolve(file), rsa2008(file));
		}
		// the decisions of the vendored XacmlResponse-01-01, -01-02, and -02-03
		TestCorpora.writeManifest(dir, """
				format=xacml20
				root=XacmlPolicySet-01-top-level.xml
				expect.XacmlRequest-01-01.xml=Permit
				expect.XacmlRequest-01-02.xml=Deny
				expect.XacmlRequest-02-03.xml=Permit
				""");

		PolicyCorpus corpus = PolicyCorpusProvider.corpus("dir:" + dir);

		assertThat(corpus.policies()).hasSize(9);
		for(PolicySource source : corpus.policies()){
			assertWithMessage("media type of %s", source.name())
					.that(source.mediaType()).isEqualTo(MediaType.Type.XACML20_XML);
		}
		assertThat(decisions(corpus.requests())).containsExactly(Decision.PERMIT, Decision.DENY, Decision.PERMIT).inOrder();
		try(PdpFixture fixture = PdpFixture.open("in-memory", corpus)){
			assertThat(fixture.corpus().requests()).hasSize(3);
		}
	}

	@Test
	public void hiddenFilesAreNotCorpusFiles(@TempDir Path dir) throws Exception
	{
		TestCorpora.writeDirectory(dir);
		int policies = PolicyCorpusProvider.corpus("dir:" + dir).policies().size();
		int requests = PolicyCorpusProvider.corpus("dir:" + dir).requests().size();
		// what Finder writes into a browsed directory
		Files.writeString(dir.resolve("policies").resolve(".DS_Store"), "not xml", UTF_8);
		Files.writeString(dir.resolve("requests").resolve(".DS_Store"), "not xml", UTF_8);

		PolicyCorpus corpus = PolicyCorpusProvider.corpus("dir:" + dir);

		assertThat(corpus.policies()).hasSize(policies);
		assertThat(corpus.requests()).hasSize(requests);
	}

	static Stream<Arguments> manifestProblems()
	{
		return Stream.of(
				Arguments.of("no manifest",
						(Problem)dir -> Files.delete(dir.resolve("corpus.properties")),
						"corpus.properties"),
				Arguments.of("format xacml40",
						(Problem)dir -> TestCorpora.writeManifest(dir, TestCorpora.MANIFEST.replace("format=xacml30", "format=xacml40")),
						"xacml40"),
				Arguments.of("missing root file",
						(Problem)dir -> TestCorpora.writeManifest(dir, TestCorpora.MANIFEST.replace("root=root.xml", "root=missing.xml")),
						"missing.xml"),
				Arguments.of("request without expectation",
						(Problem)dir -> Files.writeString(dir.resolve("requests").resolve("d-orphan.xml"),
								TestCorpora.actionRequest("read"), UTF_8),
						"d-orphan"),
				Arguments.of("expectation without request",
						(Problem)dir -> TestCorpora.writeManifest(dir, TestCorpora.MANIFEST + "expect.e-ghost.xml=Permit\n"),
						"e-ghost"),
				Arguments.of("decision Maybe",
						(Problem)dir -> TestCorpora.writeManifest(dir,
								TestCorpora.MANIFEST.replace("expect.a-read.xml=Permit", "expect.a-read.xml=Maybe")),
						"Maybe"),
				Arguments.of("misspelled key",
						(Problem)dir -> TestCorpora.writeManifest(dir, TestCorpora.MANIFEST + "expected.a-read.xml=Permit\n"),
						"expected.a-read.xml"),
				Arguments.of("no root key",
						(Problem)dir -> TestCorpora.writeManifest(dir, TestCorpora.MANIFEST.replaceAll("(?m)^root=.*\\R", "")),
						"root"),
				Arguments.of("request file named a b.xml",
						(Problem)dir -> {
							Files.writeString(dir.resolve("requests").resolve("a b.xml"), TestCorpora.actionRequest("read"), UTF_8);
							// the key needs Properties escaping, so the expectation exists and the name is the problem
							TestCorpora.writeManifest(dir, TestCorpora.MANIFEST + "expect.a\\ b.xml=Permit\n");
						},
						"a b.xml"));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("manifestProblems")
	public void manifestProblemFailsTheLoadWithAMessageThatNamesIt(String problem, Problem change, String named,
			@TempDir Path dir) throws Exception
	{
		TestCorpora.writeDirectory(dir);
		change.introduce(dir);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> PolicyCorpusProvider.corpus("dir:" + dir));

		assertWithMessage("message for: %s", problem).that(e).hasMessageThat().contains(named);
	}

	private static byte[] rsa2008(String file) throws IOException
	{
		try(InputStream in = DirectoryCorpusTest.class.getClassLoader().getResourceAsStream("rsa2008-interop/" + file)){
			assertWithMessage("build copy of %s", file).that(in).isNotNull();
			return in.readAllBytes();
		}
	}

	private static List<String> names(List<PolicySource> sources)
	{
		List<String> names = new ArrayList<>();
		for(PolicySource source : sources){
			names.add(source.name());
		}
		return names;
	}

	/** @return the request ids without a {@code .xml} ending, which the manifest format leaves open */
	private static List<String> stems(List<CorpusRequest> requests)
	{
		List<String> stems = new ArrayList<>();
		for(CorpusRequest request : requests){
			stems.add(request.id().endsWith(".xml") ? request.id().substring(0, request.id().length() - 4) : request.id());
		}
		return stems;
	}

	private static List<Decision> decisions(List<CorpusRequest> requests)
	{
		List<Decision> decisions = new ArrayList<>();
		for(CorpusRequest request : requests){
			decisions.add(request.expected());
		}
		return decisions;
	}
}
