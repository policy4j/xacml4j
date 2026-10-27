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
import org.xacml4j.jmh.testing.FirstTwinCorpus;
import org.xacml4j.jmh.testing.ProbeCorpus;
import org.xacml4j.jmh.testing.SecondTwinCorpus;

/** Spec requirement "Policy corpus selection". */
public class PolicyCorporaTest
{
	@BeforeEach
	public void resetProbe()
	{
		ProbeCorpus.reset();
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"rsa2008", "synthetic:10"})
	public void builtInCorpusIsNamedAsSelected(String value) throws Exception
	{
		assertThat(PolicyCorpora.load(value).name()).isEqualTo(value);
	}

	@Test
	public void namesAreSortedAndHoldEveryCorpusOnTheClasspath()
	{
		assertThat(PolicyCorpora.names()).containsAtLeast("rsa2008", "synthetic", "dir", ProbeCorpus.NAME,
				FirstTwinCorpus.NAME);
		assertThat(PolicyCorpora.names()).isInOrder();
	}

	@Test
	public void unknownCorpusNameFailsWithTheKnownNames()
	{
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> PolicyCorpora.load("missing"));

		assertThat(e).hasMessageThat().contains("missing");
		assertThat(e).hasMessageThat().contains("rsa2008");
		assertThat(e).hasMessageThat().contains("synthetic");
		assertThat(e).hasMessageThat().contains("dir");
	}

	@Test
	public void providerReceivesTheTextAfterTheFirstColonAsItsArgument() throws Exception
	{
		assertThat(PolicyCorpora.load("probe:a:b").name()).isEqualTo("probe:a:b");
		assertThat(PolicyCorpora.load("probe").name()).isEqualTo("probe");

		assertThat(ProbeCorpus.arguments()).containsExactly(Optional.of("a:b"), Optional.empty()).inOrder();
	}

	@Test
	public void twoCorporaWithOneNameFailWithBothClasses()
	{
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PolicyCorpora.load(FirstTwinCorpus.NAME + ":x"));

		assertThat(e).hasMessageThat().contains(FirstTwinCorpus.class.getName());
		assertThat(e).hasMessageThat().contains(SecondTwinCorpus.class.getName());
	}

	@Test
	public void blankCorpusNameIsRejected()
	{
		assertThrows(IllegalArgumentException.class, () -> PolicyCorpora.load(":x"));
	}
}
