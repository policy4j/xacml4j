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
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.jmh.store.InMemoryPolicyStore;
import org.xacml4j.jmh.testing.TestStores;
import org.xacml4j.jmh.testing.TestCorpora;
import org.xacml4j.v30.spi.repository.InMemoryPolicyRepository;

/** Spec requirement "Policy store selection". */
public class PolicyStoreTest
{
	@BeforeEach
	public void resetProbe()
	{
		TestStores.Probe.reset();
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"in-memory", "snapshot"})
	public void builtInStoreIsFoundByItsName(String name)
	{
		assertThat(PolicyStore.named(name).name()).isEqualTo(name);
	}

	@Test
	public void inMemoryStoreOpensTheProductionRepository() throws Exception
	{
		assertThat(PolicyStore.named("in-memory")).isInstanceOf(InMemoryPolicyStore.class);
		try(PdpFixture fixture = PdpFixture.open("in-memory", TestCorpora.documents("documents"))){
			assertThat(fixture.repository()).isInstanceOf(InMemoryPolicyRepository.class);
		}
	}

	@Test
	public void namesAreSortedAndHoldEveryStoreOnTheClasspath()
	{
		assertThat(PolicyStore.names()).containsAtLeast("in-memory", "snapshot", TestStores.Probe.NAME, TestStores.Counting.NAME,
				TestStores.FirstTwin.NAME);
		assertThat(PolicyStore.names()).isInOrder();
	}

	@Test
	public void unknownStoreNameFailsWithTheKnownNames()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> PolicyStore.named("missing"));

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

	static Stream<Arguments> parameters()
	{
		return Stream.of(
				Arguments.of("in-memory", "in-memory", null),
				Arguments.of("probe:a", "probe", "a"),
				Arguments.of("probe:a:b", "probe", "a:b"),
				Arguments.of("dir:/tmp/corpus:1", "dir", "/tmp/corpus:1"),
				Arguments.of("probe:", "probe", ""));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("parameters")
	public void textAfterTheFirstColonIsTheArgument(String parameter, String name, String argument)
	{
		assertThat(Services.split(parameter)).asList().containsExactly(name, argument).inOrder();
	}

	@ParameterizedTest(name = "\"{0}\"")
	@ValueSource(strings = {"", " ", ":", ":a", "  :a"})
	public void blankNameIsRejected(String parameter)
	{
		assertThrows(IllegalArgumentException.class, () -> Services.split(parameter));
	}

	@Test
	public void storeReceivesTheArgumentWhenTheHarnessOpensARepository() throws Exception
	{
		PolicyCorpus corpus = TestCorpora.documents("documents");

		try(PdpFixture first = PdpFixture.open("probe:a:b", corpus)){
			assertThat(first.store()).isInstanceOf(TestStores.Probe.class);
		}
		try(PdpFixture second = PdpFixture.open("probe", corpus)){
			assertThat(second.store()).isInstanceOf(TestStores.Probe.class);
		}

		assertThat(TestStores.Probe.arguments()).containsExactly(Optional.of("a:b"), Optional.empty()).inOrder();
	}

	@Test
	public void twoStoresWithOneNameFailWithBothClasses()
	{
		IllegalStateException e = assertThrows(IllegalStateException.class, () -> PolicyStore.named(TestStores.FirstTwin.NAME));

		assertThat(e).hasMessageThat().contains(TestStores.FirstTwin.class.getName());
		assertThat(e).hasMessageThat().contains(TestStores.SecondTwin.class.getName());
	}
}
