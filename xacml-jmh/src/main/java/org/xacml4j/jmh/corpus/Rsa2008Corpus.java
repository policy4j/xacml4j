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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.Marshalling;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.MediaType;

import com.google.auto.service.AutoService;

/**
 * The RSA 2008 interop suite: nine XACML 2.0 policy sets and 17 requests,
 * read from the vendored conformance fixtures that the module's build copies
 * into its jars. The expected decision of each request is the single result
 * of its vendored response. It takes no argument.
 */
@AutoService(PolicyCorpusProvider.class)
public final class Rsa2008Corpus implements PolicyCorpusProvider
{
	public static final String NAME = "rsa2008";

	private static final String DIRECTORY = "rsa2008-interop/";
	private static final MediaType XACML20 = MediaType.Type.XACML20_XML;
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
	private static final List<String> REQUESTS = List.of(
			"01-01", "01-02", "01-03", "01-04",
			"02-01", "02-02", "02-03", "02-04",
			"03-01", "03-02", "03-03",
			"04-01", "04-02", "04-03", "04-04",
			"05-01", "05-02");

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public PolicyCorpus load(Optional<String> argument) throws IOException, SyntaxException {
		if(argument.isPresent()){
			throw new IllegalArgumentException("Corpus \"" + NAME + "\" takes no argument, got \"" + argument.get() + "\"");
		}
		List<PolicySource> policies = new ArrayList<>(POLICIES.size());
		for(String file : POLICIES){
			policies.add(new PolicySource(file, XACML20, read(file)));
		}
		List<CorpusRequest> requests = new ArrayList<>(REQUESTS.size());
		for(String id : REQUESTS){
			RequestContext request = Marshalling.requests(XACML20)
					.unmarshal(new ByteArrayInputStream(read("XacmlRequest-" + id + ".xml")));
			Collection<Result> expected = Marshalling.responses(XACML20)
					.unmarshal(new ByteArrayInputStream(read("XacmlResponse-" + id + ".xml"))).getResults();
			if(expected.size() != 1){
				throw new IllegalStateException("Response " + id + " of corpus \"" + NAME + "\" has " + expected.size() + " results");
			}
			requests.add(new CorpusRequest(id, request, expected.iterator().next().getDecision()));
		}
		return new PolicyCorpus(NAME, policies, POLICIES.get(0), requests);
	}

	private static byte[] read(String file) throws IOException {
		try(InputStream in = Rsa2008Corpus.class.getClassLoader().getResourceAsStream(DIRECTORY + file)){
			if(in == null){
				throw new IOException("Missing corpus resource " + DIRECTORY + file);
			}
			return in.readAllBytes();
		}
	}
}
