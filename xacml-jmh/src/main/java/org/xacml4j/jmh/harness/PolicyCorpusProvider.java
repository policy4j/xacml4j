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

import java.io.IOException;
import java.util.Optional;

import org.xacml4j.v30.SyntaxException;

/**
 * A source of policy corpora. Implementations are found with
 * {@link java.util.ServiceLoader} and selected with the benchmark parameter
 * {@code corpus=<name>} or {@code corpus=<name>:<argument>}.
 */
public interface PolicyCorpusProvider
{
	/**
	 * @return the name that the benchmark parameter {@code corpus} selects
	 */
	String name();

	/**
	 * @param argument the text after the first colon of the parameter
	 * @return the corpus
	 * @throws IOException if a corpus file cannot be read
	 * @throws SyntaxException if a request or response cannot be parsed
	 * @throws IllegalArgumentException if the argument is not valid for this corpus
	 */
	PolicyCorpus load(Optional<String> argument) throws IOException, SyntaxException;
}
