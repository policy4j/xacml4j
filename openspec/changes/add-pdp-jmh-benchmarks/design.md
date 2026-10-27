## Context

- No JMH code exists. `docs/ai/BENCHMARKS.md` names the planned module
  `xacml-jmh`; `modernize-core-for-jdk28-valhalla` tasks 6.8 and 9.2 wait for
  it. `RSA2008PerformanceTestCase` times a loop with wall-clock millis and
  prints with `System.err`
  (`xacml-conformance-tests/src/test/java/org/xacml4j/v20/RSA2008PerformanceTestCase.java:122-144`).
- The PDP is built with `PolicyDecisionPointBuilder`
  (`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointBuilder.java:141-160`).
  Its defaults are `NoCachePolicyDecisionCache` and
  `NoAuditPolicyDecisionPointAuditor` (`:66-72`).
- The extension point for policy stores is `PolicyRepository`
  (`xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyRepository.java:42-230`).
  It has no `close`. The only implementation is `InMemoryPolicyRepository`, on
  top of `AbstractPolicyRepository`.
- The context factory always wraps the repository in a
  `DefaultPolicyReferenceResolver` with its reference cache on
  (`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java:99`;
  `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java:55-58`, `:80-108`).
  The cache has no maximum size: the `size` argument is unused (`:60-77`).
  After the first resolution of a reference, `decide` does not call the store
  for that reference again, at any corpus size.
- `AbstractPolicyRepository` makes one single-thread executor per instance
  for listener notices
  (`xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java:66-72`);
  a four-argument constructor takes an `Executor` (`:74-84`). The thread
  starts on the first notice (`:167-193`). The inherited `get(id, Version)`
  builds a `VersionMatch` and filters (`:204-210`); `InMemoryPolicyRepository`
  overrides it with a direct map lookup
  (`xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepository.java:106-114`).
- `InMemoryPolicyRepository.getPolicies` and `getPolicySets` pass `null` to
  `find` for an id that is absent from their map, and `find` throws
  `NullPointerException` (`InMemoryPolicyRepository.java:81-87`, `:97-103`,
  `:198`). So `getPolicy` with a policy-set id, `getPolicySet` with a policy
  id, and every unknown id throw. For the XACML 1.0 and 2.0 policy-combining
  algorithms the exception escapes `decide`: `PolicySetIDReference.createContext`
  catches only `PolicyResolutionException`, and
  `LegacyDenyOverridesPolicyCombineAlgorithm.combine` has no catch (rv001 F2,
  checked in source).
- The resolver calls `getPolicy` for a `PolicyIDReference` and `getPolicySet`
  for a `PolicySetIDReference`, with the version, earliest, and latest
  constraints of the reference (`DefaultPolicyReferenceResolver.java:92-96`,
  `:123-127`).
- The XACML 3.0 `deny-overrides` policy-combining algorithm returns at the
  first `Deny` (`xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java:73-78`).
- The conformance runner shows the reference way to load a corpus and build a
  PDP (`xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java:101-140`).
  All 17 RSA 2008 cases are `executed` and pass
  (`xacml-conformance-tests/src/test/manifest/conformance-manifest.csv:863-879`).
  All nine RSA files are `PolicySet` documents; the root
  (`XacmlPolicySet-01-top-level.xml`) refers to six of them and uses the 1.0
  `deny-overrides` policy-combining algorithm.
- The parent POM sets `annotationProcessorPaths` to AutoService only
  (`pom.xml:275-281`). With that list set, `javac` does not discover other
  processors on the classpath. `.gitignore` lists each module's `target`
  directory by name.
- Each `decide` draws from shared static `java.util.Random` instances
  (`PolicyDecisionPointContextFactory.java:68,111`, `MDCSupport.java:49,56`).
  The change `optimize-pdp-evaluation` group 1 removes them; multi-thread
  numbers taken before that change include the contention on them.

## Goals / Non-Goals

**Goals:**

- One JMH module that measures PDP decisions, store lookups, and store loads,
  with time and allocation per operation.
- Any `PolicyRepository` implementation can be measured by name, also from a
  jar outside this repository, without a change to the benchmark code, and is
  always released after use.
- Any policy set with requests can be measured: built-in corpora and a user
  directory.
- A wrong store or a wrong corpus can never give numbers: decisions and
  lookups are checked before measurement, and a run with a failed benchmark
  is marked failed.

**Non-Goals:**

- Changes to the PDP, the repository SPI, or other existing modules. The
  `InMemoryPolicyRepository` null defect and the unused cache `size` are
  recorded as one beads issue.
- A PDP builder option to turn off the reference cache.
- Lookups of absent ids in the lookup benchmark (they throw in the in-memory
  store; see Context).
- Function-invocation and value-parsing benchmarks, and JDK 28 baselines
  (modernize tasks 6.8 and 9.2).
- Performance claims or tuning (change `optimize-pdp-evaluation`). This change
  records a baseline only.
- Multiple-decision requests (one request yields more than one result).
- CI changes.

## Research Basis

`research/README.md`: JMH 1.37 is current (Q1); `-prof gc` reports
`gc.alloc.rate.norm` in B/op, and JMH 1.37 leaves the metric out when it
cannot measure it (Q2, rv003 F5); forks use the host `java.class.path`, so an
extra jar works with `java -cp ... org.openjdk.jmh.Main` and not with
`java -jar` (Q3); an expected `Indeterminate` matches all four Indeterminate
values (Q4). The rest of this design is inference from the live source and
from the plan reviews rv001 to rv003 (`evidence/reviews/plan/`).

## Decisions

### D1. A new module `xacml-jmh` in the default reactor, not deployed

Benchmarks live in `xacml-jmh`, listed in the root `<modules>`, with
`maven.deploy.skip=true`. Alternatives: a module in a Maven profile only (it
breaks without notice because normal builds skip it); JMH classes in the test
sources of `xacml-pdp` (no executable jar, and the JMH processor would run on
every test compile). In the default reactor, every build compiles the
benchmarks and runs the harness tests. `.gitignore` gets `/xacml-jmh/target`.

### D2. Build: JMH 1.37, the processor path, and a separate shaded jar

- Dependencies: `jmh-core` 1.37 (compile), `xacml-pdp`, and `xacml-xml`
  (marshalling providers, found by `ServiceLoader`). `jmh-generator-annprocess`
  1.37 goes into `annotationProcessorPaths` with
  `combine.children="append"`, so AutoService stays.
- `maven-shade-plugin` 3.6.2 (latest release; `upgrade-build-plugins` sets
  the versions of the other plugins) at `package` with
  `<outputFile>${project.build.directory}/benchmarks.jar</outputFile>`. With
  `outputFile` the plugin neither replaces nor attaches the main artifact and
  writes no `dependency-reduced-pom.xml` (maven-shade-plugin `shade-mojo`
  documentation, rv001 F3). The thin `xacml-jmh-<version>.jar` stays the
  artifact that an external store compiles against. Transformers:
  `ServicesResourceTransformer` (it merges the `META-INF/services` files of
  `MarshallingProvider`, `FunctionProvider`,
  `DecisionCombiningAlgorithmProvider`, `PolicyStore`, and
  `PolicyCorpusProvider`) and `ManifestResourceTransformer` with main class
  `org.openjdk.jmh.Main` and `Multi-Release: true` (log4j jars are
  multi-release). `META-INF/*.SF`, `*.DSA`, `*.RSA` are excluded.
- The RSA 2008 fixtures enter both jars through a second `<resource>` entry:
  directory `${project.basedir}/../xacml-conformance-tests/src/test/resources`,
  include `rsa2008-interop/**`. Alternatives: a test-jar of the conformance
  module (changes its POM and ships its test classes) or a copy of the files
  (duplicates vendored data). The source files stay read-only.

### D3. Stores are found by name and opened as sessions

A store is a factory for the existing extension point, `PolicyRepository`.
`ServiceLoader` finds the implementations; built-in stores register with
`@AutoService(PolicyStore.class)` (project convention, AGENTS.md); external
stores add a `META-INF/services/org.xacml4j.jmh.harness.PolicyStore` file.
Each use of a store is a `StoreSession` that the harness always closes, so a
store that holds connections, files, or threads is released (rv001 F1, rv003
F1). Each session gets its own repository id, so a store that uses the id as a
namespace never sees two sessions in one namespace. Two implementations with
the same name fail with `IllegalStateException` that names both classes
(rv001 F7). Alternatives: a `switch` in the benchmark (closed to extension); a
class name in the parameter with reflection (no common constructor contract,
no release hook).

### D4. Harness API

All types are in `org.xacml4j.jmh.harness`. New code returns `Optional` or
throws; it does not return `null` (the `PolicyRepository` contract of the
snapshot store is the only `null` source, D10). `MediaType` is
`org.xacml4j.v30.marshal.MediaType`.

```java
public enum StoreCapability { LOAD, CONCURRENT_READS }

public interface PolicyStore {
    String name();                                        // the parameter name, e.g. "in-memory"
    default Set<StoreCapability> capabilities() { return EnumSet.allOf(StoreCapability.class); }
    StoreSession open(PolicyStoreContext context) throws Exception;   // a new, isolated session
}

public interface StoreSession extends AutoCloseable {
    PolicyRepository repository();
    default void load(List<CompositeDecisionRule> policies) { /* repository.add(p) each; IllegalStateException when add is false */ }
    @Override default void close() throws Exception {}
}
// A read-only store without LOAD attaches to its existing data in open() and checks it in load().

public record PolicyStoreContext(String repositoryId, Optional<String> argument,
        FunctionProvider functions, DecisionCombiningAlgorithmProvider algorithms) {}

public record Selection(String name, Optional<String> argument) {
    public static Selection parse(String value);          // "a:b:c" -> name "a", argument "b:c"; "a" -> empty; blank name -> IllegalArgumentException
}

public record SelectedStore(PolicyStore store, Optional<String> argument) {}

public final class PolicyStores {
    public static SelectedStore select(String value);     // Selection.parse + find
    public static PolicyStore find(String name);           // unknown: IllegalArgumentException with the sorted known names; duplicate: IllegalStateException
    public static SortedSet<String> names();
}

public interface PolicyCorpusProvider {
    String name();                                        // e.g. "rsa2008", "synthetic", "dir"
    PolicyCorpus load(Optional<String> argument) throws IOException, SyntaxException;
}

public final class PolicySource {                         // a final class, not a record: byte[] equality
    public PolicySource(String name, MediaType mediaType, byte[] content);   // copies content
    public String name(); public MediaType mediaType();
    public byte[] content();                              // a copy
    public InputStream open();                            // a new stream on the held bytes
    // equals and hashCode use name, mediaType, and Arrays.equals of content
}
public record CorpusRequest(String id, RequestContext request, Decision expected) {}
public record PolicyCorpus(String name, List<PolicySource> policies, String root,
        List<CorpusRequest> requests) {}                  // root: the name of one PolicySource;
                                                         // checks: non-empty lists, unique names and ids, root present

public final class PolicyCorpora {
    public static PolicyCorpus load(String value) throws IOException, SyntaxException;   // Selection.parse, then provider.load
    public static PolicyCorpusProvider find(String name); // unknown / duplicate as for stores
    public static SortedSet<String> names();
}

public record LookupQuery(String label, Kind kind, String id, Optional<VersionMatch> version,
        Optional<VersionMatch> earliest, Optional<VersionMatch> latest, Optional<Version> exact,
        CompositeDecisionRule expected) {
    public enum Kind { EXACT, POLICY_REFERENCE, POLICY_SET_REFERENCE }
}

public final class PdpFixture implements AutoCloseable {
    public static PdpFixture open(String store, String corpus) throws IOException, SyntaxException;
    public static PdpFixture open(SelectedStore store, PolicyCorpus corpus) throws IOException, SyntaxException;
    public static List<CompositeDecisionRule> parse(List<PolicySource> sources) throws IOException, SyntaxException;
    public PolicyStore store();
    public PolicyStoreContext newContext();               // a fresh repository id for each further session
    public PolicyRepository repository();
    public PolicyCorpus corpus();
    public List<CompositeDecisionRule> policies();        // parsed, corpus order
    public List<LookupQuery> lookups();                   // D7, checked in open
    public List<CorpusRequest> requests(String selector); // "all" or one id; unknown id -> IllegalArgumentException
    public PolicyDecisionPoint pdp();
    public void verifyConcurrent(int threads);            // D8; IllegalStateException on any mismatch
    public void close();                                  // closes the PDP, then the session; never throws past the first failure it records
}
```

`PdpFixture.open` does, in order: parse every source with the policy
unmarshaller of its media type, from a provider map discovered once
(`MarshallingProvider.discoverProviders()`, held in a static field, rv002
F7); `store.open` with a context whose repository id is
`jmh-<store>-<corpus>-<sequence>`; `session.load`; get the root back with
`get(id, version)` of the parsed root; build the PDP; run the lookup check and
the decision check (D5). The function and algorithm providers are the
defaults. Any failure closes what was opened and is thrown as
`IllegalStateException` with the corpus, store, and every failing item.

### D5. Decisions and lookups are checked before measurement

`open` decides every corpus request once. A request fails when deciding it
throws, when it yields other than exactly one result, or when its decision
differs from the expected one; an expected `Indeterminate` matches any
`Decision.isIndeterminate()` value (research Q4). A failure line names the
request id, the expected decision, and the actual decision or the exception
class and message (rv001 F2). The lookup check runs every `LookupQuery` of D7
once and compares the result with the parsed rule by type, id, and version
(rv003 F2). Any failure fails `open`. A store that loses a policy that no
request reaches still fails, through its lookup.

### D6. The PDP setup

`PolicyDecisionPointBuilder.builder("jmh")` with the repository, the root
from the repository, `defaultRequestHandlers()`, and
`PolicyInformationPoint.builder("jmh").withDefaultRegistry().noCacheProvider()`.
The decision cache stays `NoCachePolicyDecisionCache` and the auditor stays
`NoAuditPolicyDecisionPointAuditor`, so each operation evaluates the
policies. The policies are loaded before the PDP is built, so no listener
notice is sent and no notification thread starts.

### D7. Three benchmarks, because the reference cache hides the store

The unbounded reference cache (Context) makes `decide` store-independent
after the first decision of each reference. The set separates the costs:

| Benchmark | Methods | One operation |
| --- | --- | --- |
| `PdpDecideBenchmark` | `decide` | one `decide` of the next selected request |
| `PolicyStoreLookupBenchmark` | `lookup`, `cursor` | `lookup`: the next `LookupQuery`, dispatched by kind to `get(id, version)`, `getPolicy`, or `getPolicySet` with the query's constraints; `cursor`: only the cursor step and the query read, as a control (rv003 F9) |
| `PolicyStoreLoadBenchmark` | `loadParsed`, `importXml` | one store lifecycle for the whole corpus: `open`, load, `close`. `loadParsed` loads the parsed rules; `importXml` also parses every source from its held bytes through the production `PolicyImportTool` (`repository.newImportTool()`), so the cost of its `MarshallingProvider` discovery is part of the number |

The lookup queries come from the corpus (rv003 F4, rv002 F1): one `EXACT`
query per parsed rule (`get(id, version)`), and one reference query per
`PolicyIDReference` or `PolicySetIDReference` in the corpus, with the
constraints of that reference and its kind deciding between `getPolicy` and
`getPolicySet`. Queries keep corpus order; the summary of a run reports how
many queries of each kind it had. Absent ids are not queried (Non-Goals).

`PolicyStoreLoadBenchmark` puts `close` inside the operation, so the score is
the whole lifecycle of one corpus. A per-invocation teardown would leave the
close out, but JMH warns against `Level.Invocation` for operations shorter
than a millisecond, and the built-in loads are shorter. The store capability
`LOAD` is required; the setup fails with a message for a store without it.
For `AbstractPolicyRepository` stores the operation includes the executor
that each repository allocates; the baseline summary says so (rv001, rv002).

Alternative: turn the reference cache off. That needs a new PDP builder
option (Non-Goals).

### D8. JMH settings, state, and threads

- `@BenchmarkMode(Mode.AverageTime)`; `@OutputTimeUnit` microseconds for
  decide and load, nanoseconds for lookup.
- `@Fork(3)`, `@Warmup(iterations = 5, time = 1s)`,
  `@Measurement(iterations = 5, time = 1s)` (docs/ai/BENCHMARKS.md). The
  command line can override them.
- Parameters: `store` `{"in-memory", "snapshot"}`, `corpus`
  `{"rsa2008", "synthetic:100"}`; `PdpDecideBenchmark` also `request`
  `{"all"}`. `request=all` is an equal-weight periodic mix of the corpus
  requests in corpus order; a single id measures one request.
- The cursor is `@State(Scope.Thread)`. It holds an array and an index that
  wraps with a compare, not a modulo. Its start index is
  `threadIndex * length / threads` (from JMH `ThreadParams`), so threads do
  not walk the same items at the same time (rv003 F9).
- Threads: the default and the baseline use `-t 1`. With more threads, a
  `@Setup(Level.Trial)` method that takes `BenchmarkParams` fails unless the
  store has `CONCURRENT_READS`, and runs `PdpFixture.verifyConcurrent(threads)`
  first, which decides every request from that many threads at once and
  checks every decision (rv003 F6). A multi-thread score is the shared-PDP
  workload, including the metric counters and, before
  `optimize-pdp-evaluation` group 1, the shared random seeds.
- Each method returns its result (`ResponseContext`, the found rule, the
  repository of the closed session), so JMH consumes it.

### D9. Corpora

- `rsa2008`: the nine policy sets and 17 requests of the conformance suite
  (D2 resources), XACML 2.0, root `XacmlPolicySet-01-top-level.xml`. The
  expected decision of each request is the single result of its vendored
  response, parsed with the XACML 2.0 response unmarshaller.
- `synthetic:<n>` (default 100, range 1 to 100,000): XACML 3.0 text from
  fixed templates. The root policy set uses the 3.0 `deny-overrides` policy
  combining algorithm and holds `n` `PolicyIdReference` elements. Policy `i`
  targets resource id `resource-<i>`; its rules permit action `read` and deny
  action `delete`. The generator computes the expected decision from the same
  rule table it writes, without a PDP. Target slots are the distinct values of
  `0`, `n/2`, and `n-1` in that order (one slot for `n = 1`, two for
  `n = 2`). Request ids name the slot and the outcome: `first-permit`,
  `first-deny`, `middle-permit`, `middle-deny`, `last-permit`, `last-deny`
  (for the slots that exist), and `unknown-notapplicable` (rv002 F4). Because
  `deny-overrides` stops at the first `Deny`, a Permit request and the
  NotApplicable request check all `n` policies, while a Deny request checks
  the policies up to its slot (rv002 F8, rv003 F3). Run summaries report
  scores per request id for scaling claims.
- `dir:<path>`: the manifest format of the spec. The path is used as given.
  Files are read once, at load, and sorted by file name (rv002 F9). File names
  in `requests/` may hold only letters, digits, `.`, `_`, and `-`, so that the
  `expect.<file name>` keys need no `Properties` escaping (rv001 F8).

### D10. The `snapshot` store

`SnapshotPolicyRepository extends AbstractPolicyRepository` with the
constructor `public SnapshotPolicyRepository(String id, FunctionProvider
functions, DecisionCombiningAlgorithmProvider algorithms)`. It calls the
four-argument super constructor with a direct executor (`Runnable::run`), so
it allocates no executor service (rv002 F6). It holds immutable maps
(`id -> NavigableMap<Version, Policy>` and the same for policy sets) in a
`volatile` field. `addPolicy` and `removePolicy` build a new map under a lock
and publish it; lookups read the current map without a lock. It overrides
`get(id, Version)`, `getPolicy`, and `getPolicySet` with direct map lookups
(`get`, `lastEntry` when no constraint is given), so both built-in stores
answer `get` by a map lookup (rv001 F5). It returns empty collections for an
unknown id, and `null` only where the `PolicyRepository` contract requires it
(`getPolicy`, `getPolicySet`, `get`).

### D11. Logging, allocation, and run records

- `xacml-jmh/src/main/resources/log4j2.xml` sets the root level to `WARN`,
  so debug logging does not run while measuring.
- Allocation numbers come from `-prof gc` (`gc.alloc.rate.norm`): bytes
  allocated in the benchmark JVM per operation, not by a remote store. A run
  that lacks the metric for a benchmark says "not reported" for it, never 0
  (rv003 F5). Unit tests do not assert allocation budgets.
- Every recorded run uses `-foe true`, writes one JSON file per command, and
  is checked for completeness: each expected benchmark method and parameter
  set is present with a finite score (rv003 F7). `scripts/check-jmh-results.py`
  is out of scope; the check is a small script kept in the run directory.
- `env.txt` of a run records the base commit, `git status --porcelain`, the
  sha256 of `git diff` and of each new file, the sha256 of `benchmarks.jar`,
  the exact JDK build, the JVM flags, the thread count, and the parameters
  (rv003 F8).

### D12. Documentation

`xacml-jmh/README.md`: build, run, parameters, the request mix, how to add a
store or a corpus in another jar (compile against the thin
`org.xacml4j:xacml-jmh` jar from a local `mvn install`, `provided` scope; run
with `java -cp xacml-jmh/target/benchmarks.jar:<jar> org.openjdk.jmh.Main`),
store capabilities and the session lifecycle, the directory manifest, and
the allocation output. `docs/ai/BENCHMARKS.md`: the module exists; JDK 21
commands; the store and corpus parameters; `-foe true`. `AGENTS.md`: the
module table and dependency graph.

### D13. Test-visible surface of the parallel lanes

Fixed before group 3 so the lanes write against the same names (rv001 F4,
rv002 F2):

- Each benchmark class in `org.xacml4j.jmh.pdp` is itself
  `@State(Scope.Benchmark)` with public `@Param` fields `store` and `corpus`
  (and `request` on `PdpDecideBenchmark`), `public void setUp() throws
  Exception` under `@Setup(Level.Trial)`, `public void checkThreads(BenchmarkParams params)`
  under a second `@Setup(Level.Trial)`, and `public void tearDown()` under
  `@TearDown(Level.Trial)`. Each has a nested
  `@State(Scope.Thread) public static class Cursor` with
  `public void setUp(<benchmark class> benchmark, ThreadParams thread)`.
- `PdpDecideBenchmark` exposes `public List<CorpusRequest> selected()` after
  `setUp()`.
- `SnapshotPolicyRepository` has the D10 constructor.
- Corpus tests go through `PolicyCorpora.load(...)` only.

### D14. Changes from the review gate (rv004 to rv007)

The code review gate of task 3.7 (`evidence/reviews/code/`,
`evidence/reviews/performance/rv005`) changed these decisions; each change has
a test in `ReviewGateFixesTest` or the named test class.

| Finding | Change |
| --- | --- |
| rv006 F1: the lookup oracle used the engine's `VersionMatch`, which reads `*` as one digit (bead policy4j-w842) | `VersionPatterns` matches by XACML 3.0 §5.13; a reference lookup expects the highest matching corpus version. `EarliestVersion` and `LatestVersion` are applied as patterns, as the engine does (a range reading of §5.11 is not modeled; no built-in corpus uses them) |
| rv006 F2: the concurrent check only decided requests, which the reference cache serves | `verifyConcurrent` also runs every lookup on the store from every thread |
| rv006 F3, rv004: repository ids repeat across forks; a `dir:` id held a path | ids carry the process id and start time in hexadecimal; names are reduced to letters, digits, `.`, `_`, `-` |
| rv005 F4: nothing checked the store after setup | `PdpFixture.recheck()` in every benchmark's trial tear-down; a failed check fails the run, and the fixture is closed anyway |
| rv007 F1: `importXml` adds through the import tool, which ignores a refused `add` | the load benchmark's setup loads and imports the corpus once each into fresh sessions and runs `verifyLookups` on both |
| rv007 F2: a store without `LOAD` must know what `load` is for | `StoreSession.load` documents the no-op option; the fixture keeps calling it |
| rv004 F1, rv005 F1: one import tool per document | one tool per session (`importAll`) |
| rv004 F2, rv005 F5: `lookup` returned an `Optional` (16 B/op in the harness) | `lookup` dispatches on the query kind and returns the store's answer |
| rv004 nit 5 | the load benchmark asks the capability before it opens a fixture |
| rv004 nit 6 | the snapshot store's latest lookup walks keys with `lowerKey`, without an entry, view, or `Optional`; `matching` uses a loop, not a stream |
| rv004 F3, nit 4; rv007 F3 | the `dir` corpus skips hidden files, names a missing `root` key, and rejects unknown manifest keys |
| rv005 nit 7 | the load benchmark iterates 2 s per warmup and measurement iteration |
| rv005 F3, F6 | heap and collector are fixed in the benchmark annotations (`-Xms2g -Xmx2g -XX:+UseG1GC`); the JIT and the log are fixed in the Benchmark Plan |
| rv009 (re-review): the load preflight closed the fixture only on unchecked failures; `importAll` and `StoreSession.close` throw checked ones | the preflight catches `Throwable` and rethrows it; test `loadSetUpClosesTheFixtureWhenAPreflightSessionThrowsACheckedException` (red on the old catch, run 012) |

Not changed: the unused `Cursor` of the load benchmark stays for the D13
surface (rv007 nit 5, rv004 nit 7); the duplicated `checkThreads` blocks stay
per class (rv004 nit 7).

## Test Strategy

JUnit 5 in `xacml-jmh/src/test/java`, Truth for assertions (TESTING.md).

| Requirement | Tests |
| --- | --- |
| Policy store selection | `PolicyStoresTest`; test-only stores registered in `src/test/resources/META-INF/services`: one that records its argument, one that counts `open` and `close`, one duplicate name |
| Policy corpus selection | `PolicyCorporaTest`, with a test-only duplicate provider |
| Store sessions are released | `PdpFixtureTest` and `PolicyStoreLoadBenchmarkTest` with the counting store, including a failing `open` check |
| Decisions and lookups are checked before measurement | `PdpFixtureTest`, parameterized over both stores and the corpora `rsa2008`, `synthetic:1`, `synthetic:2`, `synthetic:100`, and a test `dir` corpus; test stores that drop a referenced policy set (returning empty, and throwing), drop the root, drop an unreferenced policy, or return the wrong latest version; a `dir` corpus with a wrong expectation |
| Snapshot store answers like the in-memory store | `SnapshotPolicyRepositoryTest` (type-dispatched queries only, because of the in-memory defect) |
| RSA 2008 corpus | `Rsa2008CorpusTest` |
| Synthetic corpus | `SyntheticCorpusTest` |
| Directory corpus | `DirectoryCorpusTest` (`@TempDir`) |
| Benchmark set | `BenchmarkSmokeTest` (JMH `Runner`, `forks(0)`, `shouldFailOnError(true)`, no warmup, one 100 ms iteration, every benchmark and default parameter set, the result set checked for completeness; one run with `GCProfiler`); `PdpDecideBenchmarkTest` (request selection, cursor wrap and offsets); `checkThreads` with two threads |

`ReferenceCacheCharacterizationTest` uses a counting store wrapper to show
that a second `decide` of the same request makes no store lookup. It documents
the D7 premise; if it fails, D7 is revisited.

`BenchmarkSmokeTest` takes the JMH lock in `java.io.tmpdir`, so test runs of
`xacml-jmh` run one at a time and never during a recorded benchmark run
(rv003 F11). It is a functional test only; the forked smoke run of task 3.7
and the baseline are the timing runs.

Group gates also run the full reactor `clean test` on JDK 21 and compare with
the counts before (8,009 tests, 0 failures, 18 skipped, plus new tests of
other changes) plus the new `xacml-jmh` tests. Conformance results must not
change.

## Benchmark Plan

JMH applies: this change is the benchmark module.

- Baseline: the source state when the run starts (D11 `env.txt`), JDK 21
  (Oracle GraalVM 21.0.6, the only JDK 21 on the machine) with the C2 JIT
  (`-jvmArgs -XX:-UseJVMCICompiler`, so the numbers compare with OpenJDK
  builds; rv005 F3), the heap and collector of the annotations, the shaded
  jar, `-t 1`, `-foe true`, one run on a quiet machine with no agent session
  alive.
- Commands, each with its own JSON file and console log in the run directory:
  1. `java -jar xacml-jmh/target/benchmarks.jar -jvmArgs -XX:-UseJVMCICompiler -prof gc -foe true -rf json -rff <run>/results-default.json -o <run>/jmh-default.log`
     (default parameters; `request=all` is the equal-weight mix);
  2. `java -jar xacml-jmh/target/benchmarks.jar PdpDecideBenchmark -jvmArgs -XX:-UseJVMCICompiler -p store=in-memory -p corpus=synthetic:10,synthetic:1000 -p request=first-permit,first-deny,middle-permit,middle-deny,last-permit,last-deny,unknown-notapplicable -prof gc -foe true -rf json -rff <run>/results-scaling.json -o <run>/jmh-scaling.log`
     (one score per request id, so scaling claims use `last-permit` and
     `unknown-notapplicable` for the full scan and `first-deny` for the short
     circuit; rv005 F2).
- Completeness (D11 script): every expected method and parameter set present,
  a finite score and a finite error, and 15 raw samples (3 forks x 5
  iterations) per result.
- Report per BENCHMARKS.md: `command.txt`, `env.txt` (with the JIT flags from
  `-XX:+PrintFlagsFinal`, the CPU with its performance and efficiency core
  counts, and the load), the JSON files and logs, `summary.md` with score ±
  error, `gc.alloc.rate.norm`, `gc.count`, and `gc.time` per benchmark and
  parameter set, the lookup query counts per kind, the per-fork spread, and
  what one operation includes (rv005 "What one operation includes"). No
  comparison claim: this is the first baseline; no store difference from
  `decide` (D7); no per-policy cost from the `request=all` mix; no
  multi-thread claim. `optimize-pdp-evaluation` keeps its own before/after
  pairs; its "before" is built from the base commit's sources in a scratch
  copy.
- A JMH engineer from another model family than the run's author reviews the
  method and numbers (`evidence/reviews/performance/`).

## Code Intelligence Plan

New code only; no rename or API change in existing modules. Checks:
`ripwire . --edit-check=<symbol>` on the harness entry points before the
gate; `ast-grep scan` (the `java-no-console-output` rule) on
`xacml-jmh/src/main`; `rtk git status --porcelain -- xacml-conformance-tests`
is empty; a `clean` build of the reactor.

## Execution Plan

NTM swarm in the main checkout (session base `xacml4j`, docs/ai/SWARMS.md).
The conductor is this Claude Code session (Claude Opus 5.5). At most four
agents run at a time; each phase's session is shut down before the next.

| Phase | Lane | CLI / model | Persona or role | Owns (writes) |
| --- | --- | --- | --- | --- |
| Plan review | rv001 arch | Claude Fable 5.1 | `policy4j_arch_review_claude_fable` | nothing |
| Plan review | rv002 arch | Agy, Gemini 4 Argon | `--agyx=1:gemini-4-argon-high` + `.ntm/roles/policy4j_arch_review.md` | nothing |
| Plan review | rv003 JMH method | Codex GPT-6.1 Terra | `--cod=1:gpt-6.1-terra:max` + `.ntm/roles/policy4j_jmh.md` | nothing |
| Spine (group 2) | conductor | Claude Opus 5.5 | this session | `pom.xml` (one line), `.gitignore` (one line), `xacml-jmh/pom.xml`, `xacml-jmh/src/main/resources/**`, `.../jmh/harness/**`, `.../jmh/store/InMemoryPolicyStore.java`, `.../jmh/corpus/Rsa2008Corpus.java`, the first `PdpFixtureTest` |
| Lanes (group 3) | snapshot store | Codex GPT-6 Astra | `policy4j_implementer_codex_astra` | `.../jmh/store/SnapshotPolicyStore.java`, `.../jmh/store/SnapshotPolicyRepository.java` |
| Lanes (group 3) | corpora | Codex GPT-6.1 Sol | `policy4j_implementer_codex_sol` | `.../jmh/corpus/SyntheticCorpus.java`, `.../jmh/corpus/DirectoryCorpus.java`, helpers in `.../jmh/corpus/` named `Synthetic*` or `Directory*` |
| Lanes (group 3) | benchmarks | Codex GPT-6.1 Terra | `--cod=1:gpt-6.1-terra:max` + `.ntm/roles/policy4j_jmh.md` | `.../jmh/pdp/**` |
| Lanes (group 3) | tests | Claude Opus 5.5 | `policy4j_test_writer` | `xacml-jmh/src/test/**` (it takes over `PdpFixtureTest`) |
| Review gate | rv004 code (Codex-authored files) | Claude Fable 5.1 | `policy4j_code_review_claude_fable` | nothing |
| Review gate | rv005 JMH method | Claude Fable 5.1 | `policy4j_jmh_claude_fable` (same pane, second packet) | nothing |
| Review gate | rv006 code (Claude-authored files) | Codex GPT-6.1 Sol | `policy4j_code_review_codex_sol` | nothing |
| Review gate | rv007 code, design, and semantics, all files | Agy, Gemini 4 Argon | `--agyx` + `.ntm/roles/policy4j_code_review.md` | nothing |
| Baseline review | rv008 performance | Agy, Gemini 4 Argon | `--agyx` + `.ntm/roles/policy4j_jmh.md` | nothing |

`...` is `xacml-jmh/src/main/java/org/xacml4j/jmh`. Rules for the group 3
lanes:

- Disjoint files as listed. The harness API (D4) and the test surface (D13)
  are fixed; a lane that needs a change reports `BLOCKER:` to the conductor.
- No Maven run in the shared tree, because parallel builds overwrite
  `xacml-jmh/target`. A lane compiles in its own copy:
  `rsync -a --delete --exclude=.git --exclude=target --exclude=openspec --exclude=.beads ./ "$TMPDIR/xacml4j-<lane>/"`,
  with the other lanes' files excluded, then an offline Maven build (`-o`,
  JDK 21, the Maven command that the packet gives) of `test-compile` with
  `-f "$TMPDIR/xacml4j-<lane>/pom.xml" -pl xacml-jmh -am`. Lanes do not run
  `xacml-jmh` tests (the JMH lock, D8); the conductor runs them. If its
  sandbox blocks this, the lane says so and the conductor builds.
- No commits, no branch changes, no edits outside the owned files.
- Each lane ends with a report between the markers of its packet; the
  conductor stores it as a run under `evidence/`.

Semantics lane: SWARMS.md asks for a Grok or Kimi adversarial lane in a code
review gate. The maintainer's lane list for this work (2026-10-26) names
Claude Opus 5.5, Claude Fable 5.1, Codex Sol 6.1, Astra, Terra, and Agy
Argon, and leaves Kimi out. This change does not change engine semantics; the
two places with XACML content (the synthetic decision table, the 2.0 response
mapping) are cross-checked by `PdpFixtureTest` against the PDP, and rv007
(Agy) carries the semantics focus. This is recorded as a waiver in gate 3.7
(rv001 F6).

## Risks / Trade-offs

- [The reference cache hides store cost in `decide`] → D7: separate lookup
  and load benchmarks; a characterization test pins the premise.
- [A wrong store gives fast, wrong numbers] → D5 decision and lookup checks,
  tested with stores that lose or misreport policies.
- [A store leaks per operation] → D3 sessions, closed in every path, tested
  with a counting store.
- [A failed benchmark disappears from the results] → `-foe true` and the
  completeness check of D11.
- [Parallel lanes break each other's builds] → private build copies; the
  conductor owns the shared build and the tests.
- [Memory pressure with several agent CLIs] → at most four agents per phase;
  sessions are shut down between phases; no benchmark runs while a session
  is alive.
- [JMH needs the network] → JMH 1.37, its dependencies, and shade 3.6.0 are
  in the local Maven repository; builds run offline. A missing artifact is a
  blocker for the maintainer.
- [The synthetic expected decisions copy a PDP bug] → the generator computes
  decisions from its own table, and `PdpFixtureTest` compares them with the
  PDP.
- [JMH license] → GPL 2.0 with the Classpath Exception, in a module that is
  not deployed and that no other module uses.
- [Vendored fixtures change by mistake] → they are only read through a Maven
  resource entry; the gate checks `git status` for that path.

## Migration and Rollback

Additive. Rollback: remove the `<module>` line, the `.gitignore` line, and the
`xacml-jmh` directory.

## Open Questions

None. The maintainer asked for the module (proposal, Approval).
