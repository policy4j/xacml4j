package org.xacml4j.jmh.store;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xacml4j.jmh.harness.LookupQuery;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.testing.TestCorpora;
import org.xacml4j.jmh.testing.TestStores;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.spi.repository.PolicyRepositoryListener;
import org.xacml4j.v30.Versionable;

/**
 * Spec requirement "Snapshot store answers like the in-memory store"
 * (design D10). Comparisons use only lookups of the type of each id:
 * {@code InMemoryPolicyRepository} throws {@code NullPointerException} for
 * an id it does not hold as that type (beads policy4j-dofm).
 */
@ExtendWith(MockitoExtension.class)
public class SnapshotPolicyRepositoryTest
{
	private static final String ID = TestCorpora.VERSIONED_ID;

	@Mock
	private PolicyRepositoryListener listener;

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"rsa2008", "synthetic:100"})
	public void answersEveryCorpusLookupOfItsTypeLikeTheInMemoryStore(String corpus) throws Exception
	{
		List<CompositeDecisionRule> rules = PdpFixture.parse(PolicyCorpusProvider.corpus(corpus).policies());
		PolicyRepository memory = new InMemoryPolicyRepository("memory", TestStores.FUNCTIONS, TestStores.ALGORITHMS);
		PolicyRepository snapshot = snapshot();
		for(CompositeDecisionRule rule : rules){
			assertThat(memory.add(rule)).isTrue();
			assertThat(snapshot.add(rule)).isTrue();
		}

		for(CompositeDecisionRule rule : rules){
			String id = rule.getId();
			Version version = ((Versionable) rule).getVersion();
			CompositeDecisionRule exact = memory.get(id, version);
			assertWithMessage("in-memory get %s", id).that(exact).isNotNull();
			assertWithMessage("get %s", id).that(snapshot.get(id, version)).isEqualTo(exact);
			if(rule instanceof Policy){
				Policy latest = memory.getPolicy(id, null, null, null);
				assertWithMessage("in-memory getPolicy %s", id).that(latest).isNotNull();
				assertWithMessage("getPolicy %s", id).that(snapshot.getPolicy(id, null, null, null)).isEqualTo(latest);
				assertWithMessage("getPolicies %s", id).that(snapshot.getPolicies(id))
						.containsExactlyElementsIn(memory.getPolicies(id)).inOrder();
			}else{
				PolicySet latest = memory.getPolicySet(id, null, null, null);
				assertWithMessage("in-memory getPolicySet %s", id).that(latest).isNotNull();
				assertWithMessage("getPolicySet %s", id).that(snapshot.getPolicySet(id, null, null, null)).isEqualTo(latest);
				assertWithMessage("getPolicySets %s", id).that(snapshot.getPolicySets(id, null))
						.containsExactlyElementsIn(memory.getPolicySets(id, null)).inOrder();
			}
		}
		for(LookupQuery query : LookupQuery.of(rules)){
			assertWithMessage(query.label()).that(query.find(snapshot)).isEqualTo(query.find(memory));
		}
	}

	@Test
	public void versionsComeBackFromTheEarliestToTheLatestAndTheFirstAddOfAVersionWins() throws Exception
	{
		PolicyRepository snapshot = snapshot();
		Policy v10 = policy("1.0", "first");
		Policy v12 = policy("1.2", "first");
		Policy v11 = policy("1.1", "first");
		Policy v11Again = policy("1.1", "second");

		assertThat(snapshot.add(v10)).isTrue();
		assertThat(snapshot.add(v12)).isTrue();
		assertThat(snapshot.add(v11)).isTrue();
		assertThat(snapshot.add(v11Again)).isFalse();

		assertThat(versions(snapshot.getPolicies(ID))).containsExactly("1.0", "1.1", "1.2").inOrder();
		assertThat(snapshot.getPolicy(ID, null, null, null)).isSameInstanceAs(v12);
		assertThat(snapshot.get(ID, Version.parse("1.1"))).isSameInstanceAs(v11);
	}

	@Test
	public void versionConstraintSelectsTheVersionsThatTheInMemoryStoreSelects() throws Exception
	{
		PolicyRepository memory = new InMemoryPolicyRepository("memory", TestStores.FUNCTIONS, TestStores.ALGORITHMS);
		PolicyRepository snapshot = snapshot();
		for(String version : List.of("1.0", "1.1", "1.2", "2.0")){
			Policy policy = policy(version, "first");
			memory.add(policy);
			snapshot.add(policy);
		}

		for(String pattern : List.of("1.1", "1.*", "1.+", "2.+", "3.*")){
			VersionMatch match = VersionMatch.parse(pattern);
			assertWithMessage("getPolicies %s", pattern).that(versions(snapshot.getPolicies(ID, match)))
					.containsExactlyElementsIn(versions(memory.getPolicies(ID, match))).inOrder();
			assertWithMessage("getPolicy %s", pattern).that(snapshot.getPolicy(ID, match, null, null))
					.isEqualTo(memory.getPolicy(ID, match, null, null));
		}
	}

	@Test
	public void unknownIdGivesEmptyCollectionsAndNulls() throws Exception
	{
		PolicyRepository snapshot = snapshot();
		snapshot.add(policy("1.0", "first"));

		assertThat(snapshot.getId()).isEqualTo("snapshot");
		assertThat(snapshot.getPolicies("missing")).isEmpty();
		assertThat(snapshot.getPolicySets("missing", null)).isEmpty();
		assertThat(snapshot.getPolicy("missing", null, null, null)).isNull();
		assertThat(snapshot.getPolicySet("missing", null, null, null)).isNull();
		assertThat(snapshot.get("missing", Version.parse("1.0"))).isNull();
	}

	@Test
	public void idOfTheOtherTypeGivesEmptyCollectionsAndNulls() throws Exception
	{
		PolicyRepository snapshot = snapshot();
		Policy policy = policy("1.0", "first");
		PolicySet policySet = (PolicySet)TestCorpora.rule(
				TestCorpora.rootPolicySet(TestCorpora.ROOT_ID, TestCorpora.policyReference(ID)));
		snapshot.add(policy);
		snapshot.add(policySet);

		assertThat(snapshot.getPolicySets(ID, null)).isEmpty();
		assertThat(snapshot.getPolicySet(ID, null, null, null)).isNull();
		assertThat(snapshot.getPolicies(TestCorpora.ROOT_ID)).isEmpty();
		assertThat(snapshot.getPolicy(TestCorpora.ROOT_ID, null, null, null)).isNull();
		assertThat(snapshot.get(ID, Version.parse("1.0"))).isSameInstanceAs(policy);
		assertThat(snapshot.get(TestCorpora.ROOT_ID, Version.parse("1.0"))).isSameInstanceAs(policySet);
	}

	@Test
	public void removeDropsOneVersion() throws Exception
	{
		PolicyRepository snapshot = snapshot();
		Policy v10 = policy("1.0", "first");
		Policy v11 = policy("1.1", "first");
		snapshot.add(v10);
		snapshot.add(v11);

		assertThat(snapshot.remove(v11)).isTrue();
		assertThat(snapshot.remove(v11)).isFalse();
		assertThat(versions(snapshot.getPolicies(ID))).containsExactly("1.0");
		assertThat(snapshot.getPolicy(ID, null, null, null)).isSameInstanceAs(v10);
		assertThat(snapshot.get(ID, Version.parse("1.1"))).isNull();

		assertThat(snapshot.remove(v10)).isTrue();
		assertThat(snapshot.getPolicies(ID)).isEmpty();
		assertThat(snapshot.getPolicy(ID, null, null, null)).isNull();
	}

	@Test
	public void listenersHearOfAnAddBeforeTheAddReturns() throws Exception
	{
		// D10: notices run on a direct executor, so no thread is involved
		PolicyRepository snapshot = snapshot();
		Policy v10 = policy("1.0", "first");
		snapshot.addPolicyRepositoryListener(listener);

		assertThat(snapshot.add(v10)).isTrue();

		verify(listener).policyAdded(v10);
		verifyNoMoreInteractions(listener);
	}

	private static PolicyRepository snapshot()
	{
		return new SnapshotPolicyRepository("snapshot", TestStores.FUNCTIONS, TestStores.ALGORITHMS);
	}

	private static Policy policy(String version, String description) throws Exception
	{
		return (Policy)TestCorpora.rule(TestCorpora.readPolicy(ID, version, description));
	}

	private static List<String> versions(Collection<? extends Policy> policies)
	{
		List<String> versions = new ArrayList<>();
		for(Policy policy : policies){
			versions.add(policy.getVersion().getValue());
		}
		return versions;
	}
}
