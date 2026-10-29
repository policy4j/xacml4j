# 002: parse benchmarks green (task 2.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

New in `xacml-jmh`: `org.xacml4j.jmh.parse.PolicyParseBenchmark` (`corpus` = rsa2008,
synthetic:100), `RequestParseBenchmark` (`corpus` x `format` = xml30, json), `ParseChecks`
(setup check), and `Marshalling.requestMarshaller(MediaType)`; `xacml-jmh` now depends on
`xacml-json`.

Result: `ParseBenchmarkTest` 9 tests, 0 failures: every policy of rsa2008 and synthetic:100
and every request of rsa2008 and synthetic:10 in both formats parses back to the corpus
model; an unknown format fails setup; the check names the first differing document; a short
in-process run of all six method and parameter sets gives finite scores.

Defects found on the way (recorded in design Context, fixed in group 7):
1. `xacml-json` has no `MarshallingProvider`, so `MediaType.Type.XACML30_JSON` cannot be
   discovered through the SPI (only XACML 2.0 and 3.0 XML are).
2. `JsonRequestContextUnmarshaller.unmarshal` reads only a `Reader` or a `JsonElement` and
   returns `null` for any other source (bytes, stream, string) instead of failing.
3. `JsonRequestContextMarshaller.marshal(source, OutputStream)` wraps the stream in an
   `OutputStreamWriter` that it never flushes or closes, so output can be lost.

Not covered: XACML 2.0 request parsing (no 2.0 request marshaller to write the corpus
requests); XSD validation parameters (group 6 adds the option).
