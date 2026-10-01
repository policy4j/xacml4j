# JMH Benchmarks

Performance claims need JMH numbers from a controlled run. Every OpenSpec
task group records a JMH decision: run benchmarks, or
`NOT APPLICABLE: <reason>`.

## When JMH applies

Run JMH when a change touches a hot path or claims a speedup:

- PDP `decide` over realistic policies (the conformance policies are a
  good corpus);
- function invocation and argument conversion;
- bag and set operations;
- value parsing and equality (data types);
- XML and JSON (un)marshalling;
- caches and registries on the decision path;
- every JEP 401 value-class conversion (before and after).

It does not apply to test-only changes, docs, fixtures, or build plumbing
that does not change the produced code.

## Where benchmarks live

There is no JMH module yet. The first change that needs one adds
`xacml-jmh` (change task 9.2 plans it); adding a module to the reactor
needs the maintainer's approval. Shape:

- `xacml-jmh/pom.xml` with `org.openjdk.jmh:jmh-core` and
  `jmh-generator-annprocess`, depending on the modules under test, and
  building an executable `benchmarks.jar` (maven-shade-plugin, main class
  `org.openjdk.jmh.Main`).
- Benchmarks in `org.xacml4j.jmh.<area>`, one class per subject, for
  example `PdpDecideBenchmark`, `FunctionInvocationBenchmark`,
  `ValueParsingBenchmark`.
- The existing `xacml-pdp/src/test/java/org/xacml4j/util/FunctionInvocationBenchmark.java`
  is an ignored JUnit loop around `System.nanoTime()`. It is not valid
  evidence; port its cases to JMH.

## Writing a benchmark

- Inputs live in `@State` objects prepared in `@Setup`; never build them
  inside the measured method.
- Return the result or consume it with a `Blackhole`, so the JIT cannot
  remove the work. Avoid constant inputs the JIT can fold.
- `@BenchmarkMode(Mode.AverageTime)` with `@OutputTimeUnit` in ns or us for
  latency, `Mode.Throughput` for throughput claims.
- At least `@Fork(3)`, `@Warmup(iterations = 5)`,
  `@Measurement(iterations = 5)`; increase them when the error is large.
- Use `@Param` for input sizes (bag size, policy count) instead of copies of
  the same method.
- From JDK 28 on, add `--enable-preview` to the fork JVM:
  `@Fork(value = 3, jvmArgsAppend = "--enable-preview")`.

## Running

```sh
JAVA_HOME="$(jenv prefix 28-ea)" rtk mvn -q -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am package -DskipTests
"$(jenv prefix 28-ea)/bin/java" --enable-preview -jar xacml-jmh/target/benchmarks.jar \
  '<regex>' -prof gc -rf json -rff <run-dir>/results.json
```

- One benchmark run on the machine at a time: no build, no other
  benchmark, no swarm lane compiling. Plug in the laptop and close heavy
  applications.
- Measure baseline and candidate on the same machine, the same JDK build,
  and the same flags, back to back. Record the baseline commit.
- `-prof gc` gives allocation per operation (`gc.alloc.rate.norm`); report
  it for value-class work.

## Reporting

A run directory under the change's `evidence/benchmarks/jmh/<NNN>-<scope>/`
holds:

- `command.txt`: the build and run commands;
- `env.txt`: `java -version`, the CPU model, the OS version, and the JMH
  version;
- `results.json`: the JMH JSON output;
- `summary.md`: a table of benchmark, parameters, baseline score ± error,
  candidate score ± error, the change in percent, and allocation per
  operation; the baseline run it compares against; and the conclusion.

Claim an improvement or regression only when the error intervals do not
overlap. Otherwise write "no significant difference". A second JMH engineer
from another model family reviews the method and the numbers
(docs/ai/SWARMS.md); the review is its own run under
`evidence/reviews/performance/`.
