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
| `RegexFunctionBenchmark` | `compile`, `match`, `call`, `callMiss` | one compilation, one search with a compiled pattern, or one whole call of the regexp functions ([below](#regular-expression-functions)) | ns/op |

Parameters (`-p name=v1,v2`):

| Parameter | Default | Meaning |
| --- | --- | --- |
| `store` | `in-memory`, `snapshot` | the policy store, `name` or `name:argument` |
| `corpus` | `rsa2008`, `synthetic:100` | the policies and requests, `name` or `name:argument` |
| `request` (decide only) | `all` | `all` decides the corpus requests in turn, one per operation (an equal-weight mix); or one request id |
| `targetIndex` (decide only) | `false` | enable the restricted request-attribute target index |
| `pipLatencyMicros` (decide, `pip` corpus) | `0` | simulated latency of each backend read, in microseconds |
| `pipMode` (decide, `pip` corpus) | `serial` | `serial` or `parallel` reads within a demanded resolver |
| `pipCache` (decide, `pip` corpus) | `off` | `off` or `on` for the fixture-local PIP cache |
| `pipMaxInFlight` (decide, `pip` corpus) | `32` | maximum simultaneous backend reads per fixture |
| `patterns` (regex) | `conformance`, `adversarial` | the pattern set of `RegexFunctionBenchmark` |
| `inputLength` (regex `match`, `call`, `callMiss`) | `16`, `256`, `4096` | the length of every input, in characters |

Defaults: 3 forks with `-Xms2g -Xmx2g -XX:+UseG1GC`, 5 warmup and 5
measurement iterations of 1 s (2 s for the load benchmark), average time.
Override with `-f`, `-wi`, `-i`, `-w`, `-r`, `-jvmArgs`.

On Oracle GraalVM the JIT is Graal unless you pass
`-jvmArgs -XX:-UseJVMCICompiler`; the project baselines use C2 that way, so
their numbers compare with OpenJDK builds. Say which JIT a number came from.

- `-prof gc` adds `gc.alloc.rate.norm`: bytes allocated per operation in the
  benchmark JVM. JMH leaves it out when it cannot measure it; read a missing
  value as "not reported", not as 0.
- `-jvmArgsAppend "<args>"` replaces the fork arguments above (the heap and
  the collector), so repeat them. Compact object headers (JEP 519,
  `-XX:+UseCompactObjectHeaders`, a JDK 25 product option, off by default)
  shrink every object header from 12 to 8 bytes: a one-fork smoke on Oracle
  GraalVM 25.0.4 with C2 (2026-10-27) allocated 10 to 12.5% less per decision
  (rsa2008 4,299 to 3,769 B/op, synthetic:1000 `first-deny` 920 to 824,
  `pip:8` `independent` 10,105 to 8,937) with no time change beyond the error.
  The project's recorded pairs keep the default headers, so their history
  stays comparable; measure the flag as a pair of its own:
  `-jvmArgsAppend "-Xms2g -Xmx2g -XX:+UseG1GC -XX:+UseCompactObjectHeaders"`.
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
| `pip:<n>` | PIP-supplied attributes and XML selector content, with `n` independent backend reads (1 to 64, default 8); cases are described below |
| `rules:<n>` | eleven XACML 3.0 cases for the next optimizations, each with its whole expected result: integer and date ranges, a repeated condition, multi-valued bags, a wide request and its narrow control, obligations and advice, and regular expressions; `n` rules or policies per case (1 to 10,000, default 100); cases are described below |
| `dir:<path>` | your own policies and requests in a directory (below) |

### PIP corpus: `pip:<n>`

Requests carry only an identity. The PIP supplies the policy attributes and
selector content. Select one case with `-p request=<id>`: `independent`,
`parallel-attributes`, `dependent`, `shared`, `shared-static`,
`missing-optional`, `missing-required`, `failure`, `skipped-failure`,
`fallback`, or `selector`.
The independent case reads `n` values inside one demanded resolver; `parallel`
uses the fixture's virtual threads and the same in-flight limit for all backend
reads. The parallel-attributes case demands one of `n` attributes of a resolver
built with `buildParallel()`: the information point runs the `n` reads through
its own runner on the fixture's executor (design D26), whatever `pipMode`. The
dependent case resolves a PIP context reference. The shared case demands two
attributes that one resolver with a context key reads together: with the cache
off, the second call cannot know its key before the resolver asks for the
context key (design D27 keeps that timing), so the peer is read again; with the
cache on, the cache serves it. The shared-static case does the same with a
resolver without context keys, so the peer comes from the set the decision
already has (`PolicyInformationPoint.forDecision()`, task 30), cache off or on.
The selector uses the PIP entity fallback. Cache-off uses no PIP cache. Cache-on
uses bounded, fixture-local Guava caches keyed by `ResolverCacheKey`; setup
checks that the warm cache answers the cases with only cacheable resolvers
(`independent`, `parallel-attributes`, `dependent`, `shared`, `shared-static`,
`selector`) while the backend is unavailable.

The fixture compares the full expected response, including status, advice,
obligations, resolved attributes, and policy references, at setup and recheck.
For an external PIP, `PdpFixture.open(store, corpus, pip)` leaves the PIP under
the caller's ownership.

```sh
java -jar xacml-jmh/target/benchmarks.jar PdpDecideBenchmark \
  -p store=in-memory -p corpus=pip:8 -p request=independent \
  -p pipLatencyMicros=1000 -p pipMode=serial,parallel -p pipCache=off -p pipMaxInFlight=32 -t 1
```

Use `-t 1 -bm avgt` to compare serial and parallel reads, including zero
latency. Use `-t N -bm avgt` to compare concurrent decisions with cache off
and on, `-bm sample` for latency percentiles, and `-bm thrpt` for saturation.
The effects remain unmeasured until a benchmark run is made.

### Rules corpus: `rules:<n>`

Workloads that the other corpora do not have: ranges, a condition that many
rules repeat, multi-valued bags, attributes that no policy reads, obligations
and advice, and regular expressions. One root policy set (`first-applicable`)
holds one policy or policy set per case. It selects the case by the
environment attribute `urn:xacml4j:jmh:rules:case`, through a `string-equal`
target that the target index can use. Select one case with
`-p request=<id>`:

| Request | Policies | Expected result |
| --- | --- | --- |
| `integer-ranges` | `n` rules (`first-applicable`) with targets `10i <= size < 10i + 15`: `integer-less-than-or-equal` and `integer-greater-than`. Ranges overlap, and the request is in the last two | the effect of rule `n-2`, or rule 0 for `n` = 1 (even rules permit, odd rules deny) |
| `date-ranges` | the same with days after 2024-01-01 (`date-less-than-or-equal`, `date-greater-than`), under `deny-overrides` | Deny (Permit for `n` = 1) |
| `repeated-conditions` | `n` rules (`first-applicable`). Each condition matches `string-regexp-match` over `string-normalize-to-lower-case(string-normalize-space(...))` of the same e-mail attribute, then compares a department. Only the last rule's department matches | Permit |
| `multi-valued` | `n` policies (`deny-overrides`) with a `string-equal` target on a role. Each rule tests a bag of 8 groups with `all-of` (`string-starts-with`) and `any-of` (`string-equal`). The request has the last 8 roles and an unknown one, and the last role's policy denies | Deny |
| `narrow` | one policy and rule that read one resource attribute; 4 IncludeInResult attributes, one per category | Permit with the 4 IncludeInResult attributes |
| `wide` | as `narrow`, plus 40 attributes that no policy reads (10 per category, strings and integers). The difference from `narrow` is the cost of attributes that are not read | as `narrow` |
| `responses` | under `permit-overrides`: `n` policies that do not apply; a policy that denies with responses for Deny; and a policy that permits with an obligation (one designator assignment of two values, one literal), advice, and a policy obligation. The policy set has its own obligation and advice. `ReturnPolicyIdList` is true | Permit with 3 obligations and 2 advice in order, and 4 policy identifiers |
| `regex-targets` | `n` rules (`first-applicable`) whose targets match an anyURI attribute with `anyURI-regexp-match` and a literal pattern of their own, `^https://records\.example\.com/<i>/[a-z]+\.pdf$`. Only the last rule's pattern finds the request's URI | the effect of rule `n-1` |
| `regex-conditions` | `n` rules (`first-applicable`) whose conditions match a user with `string-regexp-match` and a literal pattern of their own, `^(user\|admin)-<i>@[a-z]{2,16}\.example\.(com\|org)$`. Only the last rule's pattern finds the request's user | the effect of rule `n-1` |
| `regex-any-of-all` | `n` Permit rules (`deny-overrides`, so every rule is evaluated) with the condition of conformance case IIC168: `any-of-all` with `string-regexp-match` over its bag of two patterns and the request's two values | Permit |
| `regex-malformed` | `n` Permit rules (`deny-overrides`, so every rule is evaluated) whose condition has a pattern with a character class that is not closed. Every rule ends Indeterminate | Indeterminate{P}, status syntax-error with the message `Regular expression pattern is invalid` |

The regex corpus of change `add-regex-engine-spi` is `repeated-conditions`
and the four `regex-` requests. Its results are warmed decisions: the fixture
decides every request before the measurement. The expected status of
`regex-malformed` is the one design D5 of that change gives a malformed pattern:
syntax-error with the fixed message of `RegExpFunctions.INVALID_PATTERN_MESSAGE`.

The fixture compares the whole expected result at setup and recheck: status,
obligations and advice in order with their assignments in order, policy
identifiers, and the values of the IncludeInResult and resolved attributes.
An oracle in the corpus gives that result; it does not run a PDP. With
`-p targetIndex=true`, the index selects the case at the root, the
`multi-valued` policies by role, and the `responses` policies by resource
type. It does not index the range targets or the regex targets.

```sh
java -jar xacml-jmh/target/benchmarks.jar PdpDecideBenchmark.decide \
  -p store=in-memory -p corpus=rules:100 -p request=integer-ranges,wide,narrow \
  -p targetIndex=false,true -prof gc
```

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

## Regular-expression functions

`RegexFunctionBenchmark` calls the regexp functions only through the entry
points that change `add-regex-engine-spi` keeps (design D7), so it runs the
same before and after the engine SPI:

| Method | One operation |
| --- | --- |
| `compile` | `Pattern.compile(RegExpFunctions.covertXacmlToJavaSyntax(p))` of the next pattern of the set |
| `match` | `find()` of the next input with its compiled pattern, without the deadline |
| `call` | `RegExpFunctions.matches(p, input)`: translation, limits, compilation, and the search with its deadline. The set's patterns repeat, as a pattern cache would find them |
| `callMiss` | the same call, with at least 1,024 distinct patterns before one repeats, more than a cache of 256 holds. Each is a pattern of the set with a suffix that matches only the empty string |

`-p patterns=conformance` takes the 12 patterns of the OASIS cases that use
`string-regexp-match` (IIB008 to IIC169). `adversarial` takes `\s+$`,
`.*\.pdf$`, and `[a-z0-9._]+@example\.com`, which a backtracking search misses
in time quadratic in the input. Every pattern has two inputs of `inputLength`
characters: a filler and then one of the pattern's shortest inputs, which it
finds; and the same input with `~` as its last character, which it misses. A
shortest input longer than `inputLength` loses its start and is not found.
Setup checks every pattern, also every pattern of `callMiss`, against these
results, and a wrong result fails the trial.

Compiled patterns are reused (design D6): an engine instance keeps the 256 most
recently used, and the `cacheRegExps` property is ignored with a warning. So
`call` finds its pattern in the cache and measures the search with its deadline,
while `callMiss` compiles on every call: its patterns are more than the cache
holds, and each is longer than its source pattern by its suffix. Compare
`callMiss` with `callMiss` of another run, not with `call` of the same run.
Before design D6 the functions compiled on every call, so the two did the same
work.

```sh
java -jar xacml-jmh/target/benchmarks.jar RegexFunctionBenchmark -prof gc
java -jar xacml-jmh/target/benchmarks.jar PdpDecideBenchmark.decide -p store=in-memory -p corpus=rules:100 \
  -p request=repeated-conditions,regex-targets,regex-conditions,regex-any-of-all,regex-malformed -prof gc
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
