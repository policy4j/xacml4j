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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyCorpora;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.policy.PolicySet;

/** Spec requirement "RSA 2008 corpus". */
public class Rsa2008CorpusTest
{
	private static final String ROOT = "XacmlPolicySet-01-top-level.xml";
	private static final List<String> POLICIES = List.of(
			"XacmlPolicySet-01-top-level.xml",
			"XacmlPolicySet-02a-CDA.xml",
			"XacmlPolicySet-02b-N.xml",
			"XacmlPolicySet-02c-N-PermCollections.xml",
			"XacmlPolicySet-02d-prog-note.xml",
			"XacmlPolicySet-02e-MA.xml",
			"XacmlPolicySet-02f-emergency.xml",
			"XacmlPolicySet-03-N-RPS-med-rec-vrole.xml",
			"XacmlPolicySet-04-N-PPS-PRD-004.xml");

	/** The {@code <Decision>} of each vendored {@code rsa2008-interop/XacmlResponse-<id>.xml}. */
	private static final Map<String, Decision> RESPONSES = new LinkedHashMap<>();

	static {
		RESPONSES.put("01-01", Decision.PERMIT);
		RESPONSES.put("01-02", Decision.DENY);
		RESPONSES.put("01-03", Decision.PERMIT);
		RESPONSES.put("01-04", Decision.DENY);
		RESPONSES.put("02-01", Decision.DENY);
		RESPONSES.put("02-02", Decision.DENY);
		RESPONSES.put("02-03", Decision.PERMIT);
		RESPONSES.put("02-04", Decision.DENY);
		RESPONSES.put("03-01", Decision.DENY);
		RESPONSES.put("03-02", Decision.PERMIT);
		RESPONSES.put("03-03", Decision.PERMIT);
		RESPONSES.put("04-01", Decision.DENY);
		RESPONSES.put("04-02", Decision.PERMIT);
		RESPONSES.put("04-03", Decision.PERMIT);
		RESPONSES.put("04-04", Decision.DENY);
		RESPONSES.put("05-01", Decision.PERMIT);
		RESPONSES.put("05-02", Decision.PERMIT);
	}

	@Test
	public void corpusHoldsNinePolicySetsSeventeenRequestsAndTheTopLevelRoot() throws Exception
	{
		PolicyCorpus corpus = PolicyCorpora.load("rsa2008");

		assertThat(corpus.name()).isEqualTo("rsa2008");
		assertThat(names(corpus.policies())).containsExactlyElementsIn(POLICIES);
		assertThat(corpus.root()).isEqualTo(ROOT);
		assertThat(ids(corpus.requests())).containsExactlyElementsIn(RESPONSES.keySet()).inOrder();
	}

	@Test
	public void expectedDecisionsAreTheDecisionsOfTheVendoredResponses() throws Exception
	{
		for(CorpusRequest request : PolicyCorpora.load("rsa2008").requests()){
			assertWithMessage("expected decision of %s", request.id())
					.that(request.expected()).isEqualTo(RESPONSES.get(request.id()));
		}
	}

	@Test
	public void policiesAreXacml20PolicySets() throws Exception
	{
		PolicyCorpus corpus = PolicyCorpora.load("rsa2008");

		for(PolicySource source : corpus.policies()){
			assertWithMessage("media type of %s", source.name())
					.that(source.mediaType()).isEqualTo(MediaType.Type.XACML20_XML);
		}
		for(CompositeDecisionRule rule : PdpFixture.parse(corpus.policies())){
			assertWithMessage("rule %s", rule.getId()).that(rule).isInstanceOf(PolicySet.class);
		}
	}

	@Test
	public void policiesAreTheBytesOfTheVendoredFiles() throws Exception
	{
		Path vendored = vendoredDirectory();

		for(PolicySource source : PolicyCorpora.load("rsa2008").policies()){
			assertWithMessage("bytes of %s", source.name())
					.that(source.content()).isEqualTo(Files.readAllBytes(vendored.resolve(source.name())));
		}
	}

	@Test
	public void argumentIsRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> PolicyCorpora.load("rsa2008:x"));
	}

	/**
	 * @return {@code xacml-conformance-tests/src/test/resources/rsa2008-interop},
	 * found from the copy that the module build puts in {@code target/classes}
	 */
	private static Path vendoredDirectory() throws Exception
	{
		URL copy = Rsa2008CorpusTest.class.getClassLoader().getResource("rsa2008-interop/" + ROOT);
		assertWithMessage("build copy of %s", ROOT).that(copy).isNotNull();
		// <repo>/xacml-jmh/target/classes/rsa2008-interop/<file>
		Path repository = Path.of(copy.toURI()).getParent().getParent().getParent().getParent().getParent();
		Path vendored = repository.resolve("xacml-conformance-tests/src/test/resources/rsa2008-interop");
		assertWithMessage("vendored directory %s", vendored).that(Files.isDirectory(vendored)).isTrue();
		return vendored;
	}

	private static List<String> names(List<PolicySource> sources)
	{
		List<String> names = new ArrayList<>();
		for(PolicySource source : sources){
			names.add(source.name());
		}
		return names;
	}

	private static List<String> ids(List<CorpusRequest> requests)
	{
		List<String> ids = new ArrayList<>();
		for(CorpusRequest request : requests){
			ids.add(request.id());
		}
		return ids;
	}
}
