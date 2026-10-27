# xacml-jmh: PDP benchmarks

JMH benchmarks of PDP decisions, policy store lookups, and policy store loads,
with time and allocation per operation. The policy store and the policy corpus
are parameters, so the same benchmarks measure any `PolicyRepository`
implementation with any policies, also ones that live outside this repository.
(OpenSpec change `add-pdp-jmh-benchmarks`.)

## Build

```sh
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -pl xacml-jmh -am -DskipTests package
```

This writes `xacml-jmh/target/benchmarks.jar` (everything, with
`org.openjdk.jmh.Main` as main class) and the thin
`xacml-jmh/target/xacml-jmh-<version>.jar` (the harness API, for external
stores to compile against).

## Run

```sh
java -jar xacml-jmh/target/benchmarks.jar -prof gc -foe true -rf json -rff results.json
```

| Benchmark | Methods | One operation | Unit |
| --- | --- | --- | --- |
| `PdpDecideBenchmark` | `decide` | one `decide` of the next selected request | us/op |
| `PolicyStoreLookupBenchmark` | `lookup`, `cursor` | one store lookup that the corpus implies (by id and version, or a reference with its constraints); `cursor` is the control without the lookup | ns/op |
| `PolicyStoreLoadBenchmark` | `loadParsed`, `importXml` | one store session for the whole corpus: open, load, close; `importXml` also parses every policy through `PolicyImportTool` | us/op |

Parameters (`-p name=v1,v2`):

| Parameter | Default | Meaning |
| --- | --- | --- |
| `store` | `in-memory`, `snapshot` | the policy store, `name` or `name:argument` |
| `corpus` | `rsa2008`, `synthetic:100` | the policies and requests, `name` or `name:argument` |
| `request` (decide only) | `all` | `all` decides the corpus requests in turn, one per operation (an equal-weight mix); or one request id |

Defaults: 3 forks with `-Xms2g -Xmx2g -XX:+UseG1GC`, 5 warmup and 5
measurement iterations of 1 s (2 s for the load benchmark), average time.
Override with `-f`, `-wi`, `-i`, `-w`, `-r`, `-jvmArgs`.

On Oracle GraalVM the JIT is Graal unless you pass
`-jvmArgs -XX:-UseJVMCICompiler`; the project baselines use C2 that way, so
their numbers compare with OpenJDK builds. Say which JIT a number came from.

- `-prof gc` adds `gc.alloc.rate.norm`: bytes allocated per operation in the
  benchmark JVM. JMH leaves it out when it cannot measure it; read a missing
  value as "not reported", not as 0.
- `-foe true` stops on the first benchmark error instead of leaving the failed
  combination out of the results.
- `-t <n>` runs `n` threads on one shared PDP. Setup then fails unless the
  store declares concurrent reads, and first decides every request from `n`
  threads at once.

Before any measurement each benchmark opens a fixture that loads the corpus
into the store, takes the root policy back from the store, builds the PDP, and
checks every expected decision and every corpus lookup (references are matched
by XACML 3.0 §5.13, so `1.*` expects `1.10` over `1.9`). With `-t <n>` it also
runs every lookup and decision from `n` threads at once. The load benchmark
also loads and imports the corpus once each and checks the result. When a
trial ends, the checks run again. A store or corpus that gives a wrong answer,
before or during the trial, fails the run, so it can never produce numbers.

Note: the built-in stores fail a corpus whose references use a `*` version
pattern over a multi-digit number, because the engine's `VersionMatch` reads
`*` as one digit (bead policy4j-w842). That is the check working.

The PDP caches resolved policy references, so after the first decision of a
request `decide` does not call the store again. Store differences therefore
show in `PolicyStoreLookupBenchmark` and `PolicyStoreLoadBenchmark`, not in
`decide`.

## Stores

| Name | Store |
| --- | --- |
| `in-memory` | `InMemoryPolicyRepository`, the production store |
| `snapshot` | `SnapshotPolicyRepository`: immutable maps, copy-on-write updates, lock-free reads (a second implementation; loads are slower, lookups faster) |

## Corpora

| Name | Corpus |
| --- | --- |
| `rsa2008` | the RSA 2008 interop suite: 9 XACML 2.0 policy sets, 17 requests, expected decisions from the vendored responses |
| `synthetic:<n>` | `n` generated XACML 3.0 policies (1 to 100,000, default 100) under a `deny-overrides` root policy set; requests for the first, middle, and last policy (Permit and Deny each) and one NotApplicable request. A Deny request stops at its policy; Permit and NotApplicable requests check all `n` targets |
| `dir:<path>` | your own policies and requests in a directory (below) |

### Your own policies: `dir:<path>`

```text
<path>/corpus.properties
<path>/policies/*.xml
<path>/requests/*.xml
```

`corpus.properties`:

```properties
format=xacml30              # or xacml20
root=root-policy-set.xml    # a file in policies/
expect.read-1.xml=Permit    # one line per file in requests/
expect.delete-1.xml=Deny    # Permit, Deny, NotApplicable, or Indeterminate
```

Request file names use only letters, digits, `.`, `_`, and `-`. Files are read
once, sorted by name; hidden files (`.DS_Store`) are skipped. Keys other than
`format`, `root`, and `expect.*` are rejected, so a misspelled key fails the
load. Each request must yield exactly one result.

```sh
java -jar xacml-jmh/target/benchmarks.jar PdpDecideBenchmark -p corpus=dir:/path/to/corpus -p store=in-memory -prof gc
```

## Your own store

1. Depend on the thin jar (`mvn install` this reactor; the module is not
   deployed) with `provided` scope, and on `xacml-pdp`:
   `org.xacml4j:xacml-jmh:<version>`.
2. Implement `org.xacml4j.jmh.harness.PolicyStore`:
   - `name()`: the value of `-p store=<name>`;
   - `open(id, argument, functions, algorithms)`: a new, empty
     `PolicyRepository`. The id is unique for each repository and each JVM
     (letters, digits, `.`, `_`, `-` only, so it can name a schema or a
     directory); the argument is the text after the first colon of the
     parameter (`-p store=<name>:<argument>`, for a URL or a configuration
     file). The harness adds the corpus with `PolicyRepository.add` and
     checks every lookup before it measures; `importXml` adds through the
     production import tool. Run more than one thread (`-t`) only if a
     repository can be read by several threads; the harness checks every
     lookup and decision from that many threads first.
   - `close(repository)` (optional): release connections, files, or threads.
     The harness closes every repository it opens, also when a check fails.
3. Register it in `META-INF/services/org.xacml4j.jmh.harness.PolicyStore` (or
   with `@AutoService(PolicyStore.class)`).
4. Run with your jar on the class path. `java -jar` ignores `-cp`, so name the
   main class:

```sh
java -cp xacml-jmh/target/benchmarks.jar:my-store.jar org.openjdk.jmh.Main \
  -p store=my-store:jdbc:postgresql://localhost/policies -p corpus=rsa2008 -prof gc -foe true
```

JMH starts each fork with the same class path, so the store is found in every
benchmark JVM. Two stores with the same name fail with a message that names
both classes.

A corpus works the same way: implement
`org.xacml4j.jmh.harness.PolicyCorpusProvider` and register it in
`META-INF/services/org.xacml4j.jmh.harness.PolicyCorpusProvider`.

## Recording results

See `docs/ai/BENCHMARKS.md`: one directory per run with `command.txt`,
`env.txt`, the JSON results, and `summary.md`; baseline and candidate back to
back on one quiet machine; claim a difference only when the error intervals do
not overlap.
