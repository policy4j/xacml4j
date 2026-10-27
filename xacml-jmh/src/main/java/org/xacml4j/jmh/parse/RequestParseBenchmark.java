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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.Marshalling;
import org.xacml4j.jmh.harness.PolicyCorpora;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.marshal.MediaType;
import org.xacml4j.v30.marshal.Marshaller;
import org.xacml4j.v30.marshal.RequestUnmarshaller;
import org.xacml4j.v30.marshal.json.JsonRequestContextMarshaller;
import org.xacml4j.v30.marshal.json.JsonRequestContextUnmarshaller;

/**
 * Measures parsing one request document of a corpus, cycling through the corpus requests
 * (change improve-xacml-parsing, spec parse-benchmarks). Setup writes the corpus requests as
 * XACML 3.0 XML ({@code format=xml30}) or XACML JSON ({@code format=json}) with the project's
 * own marshallers, parses each document once, and requires the corpus request back. XACML 2.0
 * requests are not measured: the project has no XACML 2.0 request marshaller.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g", "-XX:+UseG1GC"})
public class RequestParseBenchmark
{
	@Param({"rsa2008", "synthetic:100"})
	public String corpus;

	@Param({"xml30", "json"})
	public String format;

	private byte[][] documents;
	private RequestUnmarshaller unmarshaller;
	private boolean reader;

	/** The next document of one benchmark thread. */
	@State(Scope.Thread)
	public static class Cursor
	{
		int next;
	}

	@Setup(Level.Trial)
	public void setUp() throws IOException, SyntaxException
	{
		List<CorpusRequest> requests = PolicyCorpora.load(corpus).requests();
		Marshaller<RequestContext> marshaller;
		switch(format){
			case "xml30" -> {
				marshaller = Marshalling.requestMarshaller(MediaType.Type.XACML30_XML);
				unmarshaller = Marshalling.requests(MediaType.Type.XACML30_XML);
			}
			case "json" -> {
				// xacml-json registers no MarshallingProvider, and its unmarshaller reads only a
				// Reader or a JsonElement (it returns null for bytes); its classes are used directly.
				reader = true;
				marshaller = new JsonRequestContextMarshaller();
				unmarshaller = new JsonRequestContextUnmarshaller();
			}
			default -> throw new IllegalArgumentException("Unknown request format \"" + format + "\"; known: xml30, json");
		}
		documents = new byte[requests.size()][];
		List<String> names = new ArrayList<>(requests.size());
		List<RequestContext> expected = new ArrayList<>(requests.size());
		List<RequestContext> parsed = new ArrayList<>(requests.size());
		for(int i = 0; i < requests.size(); i++){
			// A Writer target: the JSON marshaller does not flush an OutputStream target.
			StringWriter text = new StringWriter();
			marshaller.marshal(requests.get(i).request(), text);
			documents[i] = text.toString().getBytes(UTF_8);
			names.add(requests.get(i).id() + "." + format);
			expected.add(requests.get(i).request());
			parsed.add(unmarshaller.unmarshal(input(documents[i])));
		}
		ParseChecks.requireSame("request", names, expected, parsed);
	}

	@Benchmark
	public RequestContext parse(Cursor cursor) throws IOException, SyntaxException
	{
		int i = cursor.next;
		cursor.next = i + 1 == documents.length ? 0 : i + 1;
		return unmarshaller.unmarshal(input(documents[i]));
	}

	/** @return the document as the unmarshaller reads it: bytes for XML, a UTF-8 reader for JSON */
	private Object input(byte[] document)
	{
		return reader ? new InputStreamReader(new ByteArrayInputStream(document), UTF_8) : document;
	}
}
