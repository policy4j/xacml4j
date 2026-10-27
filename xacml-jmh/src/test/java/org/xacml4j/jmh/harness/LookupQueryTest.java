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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xacml4j.jmh.testing.TestCorpora;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * The lookups that a corpus implies (design D7) and how a lookup calls the
 * repository: {@code get(id, version)} for an exact query, {@code getPolicy}
 * or {@code getPolicySet} with the constraints of a reference.
 */
@ExtendWith(MockitoExtension.class)
public class LookupQueryTest
{
	@Mock
	private PolicyRepository repository;

	@Test
	public void rsa2008HasAnExactQueryPerPolicySetAndAQueryPerPolicySetReference() throws Exception
	{
		List<CompositeDecisionRule> rules = PdpFixture.parse(PolicyCorpora.load("rsa2008").policies());

		List<LookupQuery> queries = LookupQuery.of(rules);

		// PolicySetIdReference elements of the vendored files: 01-top-level six,
		// 02b-N one, 02c-N-PermCollections two, 03-N-RPS-med-rec-vrole one
		assertThat(kinds(queries, LookupQuery.Kind.EXACT)).hasSize(9);
		assertThat(kinds(queries, LookupQuery.Kind.POLICY_SET_REFERENCE)).hasSize(10);
		assertThat(kinds(queries, LookupQuery.Kind.POLICY_REFERENCE)).isEmpty();
		for(int i = 0; i < rules.size(); i++){
			assertThat(queries.get(i).kind()).isEqualTo(LookupQuery.Kind.EXACT);
			assertThat(queries.get(i).expected()).isSameInstanceAs(rules.get(i));
			assertThat(queries.get(i).exact().isPresent()).isTrue();
			assertThat(queries.get(i).exact().get()).isEqualTo(Rules.version(rules.get(i)));
		}
	}

	@Test
	public void referenceWithoutConstraintsExpectsTheLatestVersion() throws Exception
	{
		List<CompositeDecisionRule> rules = PdpFixture.parse(TestCorpora.versioned("versioned").policies());

		List<LookupQuery> references = kinds(LookupQuery.of(rules), LookupQuery.Kind.POLICY_REFERENCE);

		assertThat(references).hasSize(1);
		LookupQuery reference = references.get(0);
		assertThat(reference.id()).isEqualTo(TestCorpora.VERSIONED_ID);
		assertThat(reference.version().isPresent()).isFalse();
		assertThat(reference.earliest().isPresent()).isFalse();
		assertThat(reference.latest().isPresent()).isFalse();
		assertThat(Rules.version(reference.expected())).isEqualTo(Version.parse("2.0"));
	}

	@Test
	public void referenceWithAVersionConstraintExpectsTheMatchingVersion() throws Exception
	{
		List<CompositeDecisionRule> rules = List.of(
				TestCorpora.rule(TestCorpora.rootPolicySet(TestCorpora.VERSIONS_ROOT_ID,
						TestCorpora.policyReference(TestCorpora.VERSIONED_ID, "1.0"))),
				TestCorpora.rule(TestCorpora.readPolicy(TestCorpora.VERSIONED_ID, "1.0", "first version")),
				TestCorpora.rule(TestCorpora.readPolicy(TestCorpora.VERSIONED_ID, "2.0", "second version")));

		List<LookupQuery> references = kinds(LookupQuery.of(rules), LookupQuery.Kind.POLICY_REFERENCE);

		assertThat(references).hasSize(1);
		assertThat(references.get(0).version().isPresent()).isTrue();
		assertThat(references.get(0).version().get()).isEqualTo(VersionMatch.parse("1.0"));
		assertThat(Rules.version(references.get(0).expected())).isEqualTo(Version.parse("1.0"));
	}

	@Test
	public void referenceToARuleOutsideTheCorpusGivesNoQuery() throws Exception
	{
		List<CompositeDecisionRule> rules = List.of(
				TestCorpora.rule(TestCorpora.rootPolicySet(TestCorpora.ROOT_ID,
						TestCorpora.policyReference("urn:xacml4j:jmh:test:absent"))),
				TestCorpora.rule(TestCorpora.documentsPolicy()));

		List<LookupQuery> queries = LookupQuery.of(rules);

		assertThat(queries).hasSize(2);
		assertThat(kinds(queries, LookupQuery.Kind.EXACT)).hasSize(2);
	}

	@Test
	public void exactQueryCallsGetWithItsVersion() throws Exception
	{
		CompositeDecisionRule rule = TestCorpora.rule(TestCorpora.documentsPolicy());
		Version version = Version.parse("1.0");
		LookupQuery query = new LookupQuery("exact", LookupQuery.Kind.EXACT, TestCorpora.DOCUMENTS_ID,
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(version), rule);
		when(repository.get(TestCorpora.DOCUMENTS_ID, version)).thenReturn(rule);

		Optional<CompositeDecisionRule> found = query.find(repository);

		assertThat(found.isPresent()).isTrue();
		assertThat(found.get()).isSameInstanceAs(rule);
		verify(repository).get(TestCorpora.DOCUMENTS_ID, version);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void policyReferenceQueryCallsGetPolicyWithItsConstraints() throws Exception
	{
		Policy policy = (Policy)TestCorpora.rule(TestCorpora.readPolicy(TestCorpora.VERSIONED_ID, "1.1", "one"));
		VersionMatch version = VersionMatch.parse("1.+");
		VersionMatch earliest = VersionMatch.parse("1.*");
		VersionMatch latest = VersionMatch.parse("1.1");
		LookupQuery query = new LookupQuery("reference", LookupQuery.Kind.POLICY_REFERENCE, TestCorpora.VERSIONED_ID,
				Optional.of(version), Optional.of(earliest), Optional.of(latest), Optional.empty(), policy);
		when(repository.getPolicy(TestCorpora.VERSIONED_ID, version, earliest, latest)).thenReturn(policy);

		Optional<CompositeDecisionRule> found = query.find(repository);

		assertThat(found.isPresent()).isTrue();
		assertThat(found.get()).isSameInstanceAs(policy);
		verify(repository).getPolicy(TestCorpora.VERSIONED_ID, version, earliest, latest);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void policySetReferenceQueryWithoutConstraintsCallsGetPolicySetWithNulls() throws Exception
	{
		PolicySet policySet = (PolicySet)TestCorpora.rule(
				TestCorpora.rootPolicySet(TestCorpora.ROOT_ID, TestCorpora.policyReference(TestCorpora.DOCUMENTS_ID)));
		LookupQuery query = new LookupQuery("reference", LookupQuery.Kind.POLICY_SET_REFERENCE, TestCorpora.ROOT_ID,
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), policySet);
		when(repository.getPolicySet(TestCorpora.ROOT_ID, null, null, null)).thenReturn(policySet);

		Optional<CompositeDecisionRule> found = query.find(repository);

		assertThat(found.isPresent()).isTrue();
		assertThat(found.get()).isSameInstanceAs(policySet);
		verify(repository).getPolicySet(TestCorpora.ROOT_ID, null, null, null);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void lookupThatFindsNothingIsEmpty() throws Exception
	{
		CompositeDecisionRule rule = TestCorpora.rule(TestCorpora.documentsPolicy());
		Version version = Version.parse("1.0");
		LookupQuery query = new LookupQuery("exact", LookupQuery.Kind.EXACT, TestCorpora.DOCUMENTS_ID,
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(version), rule);
		when(repository.get(TestCorpora.DOCUMENTS_ID, version)).thenReturn(null);

		Optional<CompositeDecisionRule> found = query.find(repository);

		assertThat(found.isPresent()).isFalse();
		verify(repository).get(TestCorpora.DOCUMENTS_ID, version);
		verifyNoMoreInteractions(repository);
	}

	@Test
	public void exactQueryWithoutAVersionIsRejected() throws Exception
	{
		CompositeDecisionRule rule = TestCorpora.rule(TestCorpora.documentsPolicy());

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> new LookupQuery("exact without version", LookupQuery.Kind.EXACT, TestCorpora.DOCUMENTS_ID,
						Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), rule));
		assertThat(e).hasMessageThat().contains("exact without version");
	}

	private static List<LookupQuery> kinds(List<LookupQuery> queries, LookupQuery.Kind kind)
	{
		List<LookupQuery> found = new ArrayList<>();
		for(LookupQuery query : queries){
			if(query.kind() == kind){
				found.add(query);
			}
		}
		return found;
	}
}
