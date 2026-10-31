package org.xacml4j.jmh.harness;

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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.jmh.testing.TestCorpora;
import org.xacml4j.v30.Decision;

/**
 * The checks of a corpus: policies and requests, unique names and ids, and
 * a root among the policies (design D4).
 */
public class PolicyCorpusTest
{
	private PolicyCorpus documents;

	@BeforeEach
	public void loadCorpus() throws Exception
	{
		documents = TestCorpora.documents("documents");
	}

	@Test
	public void rootIndexIsThePositionOfTheRootDocument()
	{
		assertThat(documents.policies().get(documents.rootIndex()).name()).isEqualTo("root.xml");
		assertThat(documents.rootIndex()).isEqualTo(1);
	}

	@Test
	public void corpusKeepsItsOwnListsOfPoliciesAndRequests()
	{
		List<PolicySource> policies = new ArrayList<>(documents.policies());
		List<CorpusRequest> requests = new ArrayList<>(documents.requests());
		PolicyCorpus corpus = new PolicyCorpus("copy", policies, "root.xml", requests);

		policies.clear();
		requests.clear();

		assertThat(corpus.policies()).hasSize(2);
		assertThat(corpus.requests()).hasSize(4);
	}

	@Test
	public void corpusWithoutPoliciesIsRejected()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new PolicyCorpus("corpus-without-policies", List.of(), "root.xml", documents.requests()));
		assertThat(e).hasMessageThat().contains("corpus-without-policies");
	}

	@Test
	public void corpusWithoutRequestsIsRejected()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new PolicyCorpus("corpus-without-requests", documents.policies(), "root.xml", List.of()));
		assertThat(e).hasMessageThat().contains("corpus-without-requests");
	}

	@Test
	public void corpusWithoutItsRootDocumentIsRejected()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new PolicyCorpus("rootless", documents.policies(), "missing.xml", documents.requests()));
		assertThat(e).hasMessageThat().contains("missing.xml");
	}

	@Test
	public void twoDocumentsWithOneNameAreRejected()
	{
		List<PolicySource> policies = new ArrayList<>(documents.policies());
		policies.add(TestCorpora.source("root.xml", TestCorpora.documentsPolicy()));

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new PolicyCorpus("twice", policies, "root.xml", documents.requests()));
		assertThat(e).hasMessageThat().contains("root.xml");
	}

	@Test
	public void twoRequestsWithOneIdAreRejected() throws Exception
	{
		List<CorpusRequest> requests = new ArrayList<>(documents.requests());
		requests.add(TestCorpora.request("read", TestCorpora.actionRequest("write"), Decision.NOT_APPLICABLE));

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new PolicyCorpus("twice", documents.policies(), "root.xml", requests));
		assertThat(e).hasMessageThat().contains("read");
	}
}
