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

Module `xacml-jmh` (change `add-pdp-jmh-benchmarks`; usage in
`xacml-jmh/README.md`):

- `xacml-jmh/pom.xml` with `jmh-core` and `jmh-generator-annprocess` 1.37
  (appended to the parent's annotation processor path); `package` writes
  the executable `xacml-jmh/target/benchmarks.jar` (shade `outputFile`, main
  class `org.openjdk.jmh.Main`) next to the thin module jar.
- Benchmarks in `org.xacml4j.jmh.<area>`, one class per subject:
  `pdp.PdpDecideBenchmark`, `pdp.PolicyStoreLookupBenchmark`,
  `pdp.PolicyStoreLoadBenchmark`. Planned next: `FunctionInvocationBenchmark`,
  `ValueParsingBenchmark` (modernize tasks 6.8, 9.2).
- The harness (`org.xacml4j.jmh.harness`) selects the policy store
  (`-p store=<name>[:<argument>]`) and the corpus
  (`-p corpus=rsa2008|synthetic:<n>|dir:<path>`) by name through
  `ServiceLoader`, and checks every decision and store lookup before and
  after measurement. Stores and corpora from other jars run with
  `java -cp benchmarks.jar:<jar> org.openjdk.jmh.Main`.
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

JDK 25 (the project baseline; JDK 28 EA with `--enable-preview` once the build moves):

```sh
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -pl xacml-jmh -am -DskipTests package
"$(jenv prefix 25)/bin/java" -jar xacml-jmh/target/benchmarks.jar '<regex>' \
  -jvmArgs -XX:-UseJVMCICompiler -prof gc -foe true \
  -rf json -rff <scratch>/results.json -o <scratch>/jmh.log
```

Write results and logs outside the repository.

- One benchmark run on the machine at a time: no build, no other
  benchmark, no swarm lane compiling. Plug in the laptop and close heavy
  applications.
- Measure baseline and candidate on the same machine, the same JDK build,
  and the same flags, back to back. Record the baseline commit; when the
  tree has uncommitted work, build the baseline from the base sources in a
  scratch copy and record the hashes of the changed files.
- `jenv prefix 25` is Oracle GraalVM here: without
  `-XX:-UseJVMCICompiler` the Graal JIT compiles, and its numbers do not
  compare with C2 (OpenJDK). Record the JIT.
- `-foe true` fails the run on a benchmark error instead of dropping that
  result; check the JSON for every expected method and parameter set, a
  finite error, and forks x iterations samples.
- `-prof gc` gives allocation per operation (`gc.alloc.rate.norm`, bytes in
  the benchmark JVM); a result without it says "not reported", never 0.
  Report it for value-class work.
- Compact object headers (JEP 519; `-XX:+UseCompactObjectHeaders`, a product
  option of JDK 25, off by default) shrink every object header from 12 to
  8 bytes. A one-fork smoke of `PdpDecideBenchmark` on Oracle GraalVM 25.0.4
  with C2 (cycle AK of change optimize-pdp-evaluation, 2026-10-27) allocated
  10 to 12.5% less per decision with the flag: rsa2008 4,299 to 3,769 B/op,
  synthetic:1000 `all` 51,163 to 44,787 and `first-deny` 920 to 824, `pip:8`
  `independent` 10,105 to 8,937; the time per operation did not change
  beyond its error. The pairs of D12 keep the default headers, so their
  history stays comparable; measure the flag as a pair of its own, and pass
  the fork's heap arguments with it, because a command-line `-jvmArgsAppend`
  replaces the benchmark's own:
  `-jvmArgsAppend "-Xms2g -Xmx2g -XX:+UseG1GC -XX:+UseCompactObjectHeaders"`.

## Reporting

A run directory under the change's `evidence/benchmarks/jmh/<NNN>-<scope>/`
holds:

- `command.txt`: the build and run commands;
- `env.txt`: `java -version`, the CPU model, the OS version, and the JMH
  version;
- `summary.md`: each benchmark by repository-relative path and method
  (`xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java#decide`),
  and a table of parameters, baseline score ± error, candidate score ± error,
  the change in percent, and allocation per operation; the baseline run it
  compares against; and the conclusion.

The same numbers go in `benchmarks/jmh/LEDGER.md`. Cite benchmarks, not their
output: do not commit JMH logs or JSON (docs/ai/OPENSPEC.md "Evidence"). The
same holds for a JCStress test: cite its class and the outcome it pins.

Claim an improvement or regression only when the error intervals do not
overlap. Otherwise write "no significant difference". A second JMH engineer
from another model family reviews the method and the numbers
(docs/ai/SWARMS.md); the review is its own run under
`evidence/reviews/performance/`.
