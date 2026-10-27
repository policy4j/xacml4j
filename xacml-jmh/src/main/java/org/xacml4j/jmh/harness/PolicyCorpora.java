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
import java.util.SortedSet;

import org.xacml4j.v30.SyntaxException;

/** Finds {@link PolicyCorpusProvider} implementations on the classpath by name. */
public final class PolicyCorpora
{
	private PolicyCorpora(){
	}

	/**
	 * @param value the {@code corpus} parameter: {@code name} or {@code name:argument}
	 * @return the loaded corpus
	 */
	public static PolicyCorpus load(String value) throws IOException, SyntaxException {
		Selection selection = Selection.parse(value);
		return find(selection.name()).load(selection.argument());
	}

	/**
	 * @param name a corpus name
	 * @return the provider with that name
	 * @throws IllegalArgumentException if no provider has that name
	 * @throws IllegalStateException if several providers have that name
	 */
	public static PolicyCorpusProvider find(String name){
		return Providers.find(PolicyCorpusProvider.class, PolicyCorpusProvider::name, "corpus", name);
	}

	/** @return the sorted names of the corpora on the classpath */
	public static SortedSet<String> names(){
		return Providers.names(PolicyCorpusProvider.class, PolicyCorpusProvider::name);
	}
}
