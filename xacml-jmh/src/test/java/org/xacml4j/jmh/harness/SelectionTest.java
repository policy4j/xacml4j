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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Parameter values {@code name} and {@code name:argument}: the text after
 * the first colon is the argument (spec "Policy store selection", design D4).
 */
public class SelectionTest
{
	static Stream<Arguments> values()
	{
		return Stream.of(
				Arguments.of("in-memory", "in-memory", Optional.empty()),
				Arguments.of("probe:a", "probe", Optional.of("a")),
				Arguments.of("probe:a:b", "probe", Optional.of("a:b")),
				Arguments.of("dir:/tmp/corpus:1", "dir", Optional.of("/tmp/corpus:1")),
				Arguments.of("probe:", "probe", Optional.of("")));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("values")
	public void textAfterTheFirstColonIsTheArgument(String value, String name, Optional<String> argument)
	{
		Selection selection = Selection.parse(value);

		assertThat(selection.name()).isEqualTo(name);
		assertThat(selection.argument()).isEqualTo(argument);
	}

	@ParameterizedTest(name = "\"{0}\"")
	@ValueSource(strings = {"", " ", ":", ":a", "  :a"})
	public void blankNameIsRejected(String value)
	{
		assertThrows(IllegalArgumentException.class, () -> Selection.parse(value));
	}
}
