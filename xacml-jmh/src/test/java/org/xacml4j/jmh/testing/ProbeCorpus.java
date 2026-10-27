package org.xacml4j.jmh.testing;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.v30.SyntaxException;

/**
 * Corpus {@code probe}: the {@link TestCorpora#documents} corpus, with a
 * static record of the argument of every load, as in {@link ProbeStore}.
 */
public final class ProbeCorpus implements PolicyCorpusProvider
{
	public static final String NAME = "probe";

	private static final List<Optional<String>> ARGUMENTS = Collections.synchronizedList(new ArrayList<>());

	@Override
	public String name(){
		return NAME;
	}

	@Override
	public PolicyCorpus load(Optional<String> argument) throws IOException, SyntaxException {
		ARGUMENTS.add(argument);
		return TestCorpora.documents(NAME + argument.map(a -> ":" + a).orElse(""));
	}

	/** @return the arguments of the loads since the last reset, in order */
	public static List<Optional<String>> arguments(){
		synchronized(ARGUMENTS){
			return List.copyOf(ARGUMENTS);
		}
	}

	public static void reset(){
		ARGUMENTS.clear();
	}
}
