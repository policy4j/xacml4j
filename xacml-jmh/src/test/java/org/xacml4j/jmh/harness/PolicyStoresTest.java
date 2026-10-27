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

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.jmh.store.InMemoryPolicyStore;
import org.xacml4j.jmh.testing.CountingStore;
import org.xacml4j.jmh.testing.FirstTwinStore;
import org.xacml4j.jmh.testing.ProbeStore;
import org.xacml4j.jmh.testing.SecondTwinStore;
import org.xacml4j.jmh.testing.TestCorpora;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;

/** Spec requirement "Policy store selection". */
public class PolicyStoresTest
{
	@BeforeEach
	public void resetProbe()
	{
		ProbeStore.reset();
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"in-memory", "snapshot"})
	public void builtInStoreIsFoundByItsName(String name)
	{
		assertThat(PolicyStores.find(name).name()).isEqualTo(name);
	}

	@Test
	public void inMemoryStoreOpensTheProductionRepository() throws Exception
	{
		assertThat(PolicyStores.find("in-memory")).isInstanceOf(InMemoryPolicyStore.class);
		try(PdpFixture fixture = PdpFixture.open(PolicyStores.select("in-memory"), TestCorpora.documents("documents"))){
			assertThat(fixture.repository()).isInstanceOf(InMemoryPolicyRepository.class);
		}
	}

	@Test
	public void namesAreSortedAndHoldEveryStoreOnTheClasspath()
	{
		assertThat(PolicyStores.names()).containsAtLeast("in-memory", "snapshot", ProbeStore.NAME, CountingStore.NAME,
				FirstTwinStore.NAME);
		assertThat(PolicyStores.names()).isInOrder();
	}

	@Test
	public void unknownStoreNameFailsWithTheKnownNames()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> PolicyStores.find("missing"));

		assertThat(e).hasMessageThat().contains("missing");
		assertThat(e).hasMessageThat().contains("in-memory");
		assertThat(e).hasMessageThat().contains("snapshot");
	}

	@Test
	public void unknownStoreNameFailsWhenAFixtureOpens()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> PdpFixture.open("missing:x", "rsa2008"));

		assertThat(e).hasMessageThat().contains("in-memory");
		assertThat(e).hasMessageThat().contains("snapshot");
	}

	@Test
	public void selectionKeepsTheTextAfterTheFirstColonAsTheArgument()
	{
		SelectedStore selected = PolicyStores.select("probe:a:b");

		assertThat(selected.store()).isInstanceOf(ProbeStore.class);
		assertThat(selected.argument()).isEqualTo(Optional.of("a:b"));
		assertThat(PolicyStores.select("probe").argument().isPresent()).isFalse();
	}

	@Test
	public void storeReceivesTheArgumentWhenTheHarnessOpensASession() throws Exception
	{
		PolicyCorpus corpus = TestCorpora.documents("documents");

		try(PdpFixture first = PdpFixture.open(PolicyStores.select("probe:a:b"), corpus)){
			assertThat(first.store()).isInstanceOf(ProbeStore.class);
		}
		try(PdpFixture second = PdpFixture.open(PolicyStores.select("probe"), corpus)){
			assertThat(second.store()).isInstanceOf(ProbeStore.class);
		}

		assertThat(ProbeStore.arguments()).containsExactly(Optional.of("a:b"), Optional.empty()).inOrder();
	}

	@Test
	public void twoStoresWithOneNameFailWithBothClasses()
	{
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> PolicyStores.find(FirstTwinStore.NAME));

		assertThat(e).hasMessageThat().contains(FirstTwinStore.class.getName());
		assertThat(e).hasMessageThat().contains(SecondTwinStore.class.getName());
	}
}
