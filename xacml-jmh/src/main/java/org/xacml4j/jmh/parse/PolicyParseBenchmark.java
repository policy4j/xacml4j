package org.xacml4j.jmh.parse;

/*
 * #%L
 * Xacml4J JMH Benchmarks
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.xacml4j.jmh.harness.Marshalling;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.PolicyUnmarshaller;

/**
 * Measures parsing one policy document of a corpus, cycling through the corpus documents
 * (change improve-xacml-parsing, spec parse-benchmarks). The bytes are taken once in setup,
 * so one operation is one {@code unmarshal} of a byte array.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g", "-XX:+UseG1GC"})
public class PolicyParseBenchmark
{
	@Param({"rsa2008", "synthetic:100"})
	public String corpus;

	private byte[][] documents;
	private PolicyUnmarshaller[] unmarshallers;
	private List<CompositeDecisionRule> expected;

	/** The next document of one benchmark thread. */
	@State(Scope.Thread)
	public static class Cursor
	{
		int next;
	}

	@Setup(Level.Trial)
	public void setUp() throws IOException, SyntaxException
	{
		List<PolicySource> sources = PolicyCorpusProvider.corpus(corpus).policies();
		documents = new byte[sources.size()][];
		unmarshallers = new PolicyUnmarshaller[sources.size()];
		List<String> names = new ArrayList<>(sources.size());
		for(int i = 0; i < sources.size(); i++){
			documents[i] = sources.get(i).content();
			unmarshallers[i] = Marshalling.policies(sources.get(i).mediaType());
			names.add(sources.get(i).name());
		}
		expected = PdpFixture.parse(sources);
		List<CompositeDecisionRule> parsed = new ArrayList<>(documents.length);
		for(int i = 0; i < documents.length; i++){
			parsed.add(unmarshallers[i].unmarshal(documents[i]));
		}
		ParseChecks.requireSame("policy", names, expected, parsed);
	}

	@Benchmark
	public CompositeDecisionRule parse(Cursor cursor) throws IOException, SyntaxException
	{
		int i = cursor.next;
		cursor.next = i + 1 == documents.length ? 0 : i + 1;
		return unmarshallers[i].unmarshal(documents[i]);
	}

	/** @return the corpus models, in document order (for tests) */
	List<CompositeDecisionRule> expected()
	{
		return expected;
	}
}
