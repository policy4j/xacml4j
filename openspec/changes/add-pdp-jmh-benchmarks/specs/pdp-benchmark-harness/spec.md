## ADDED Requirements

### Requirement: Policy store selection
The benchmark harness SHALL select the policy store from the value of the
`store` benchmark parameter, written as `name` or `name:argument`, among the
`PolicyStore` implementations that `java.util.ServiceLoader` finds on the
classpath. The text after the first colon SHALL be passed to the store as its
argument. The harness SHALL provide the stores `in-memory` (the production
`InMemoryPolicyRepository`) and `snapshot`. An unknown name SHALL fail before
measurement with a message that lists the known store names. Two
implementations with the same name SHALL fail with a message that names both
implementation classes.

#### Scenario: Built-in stores are found
- **GIVEN** the `xacml-jmh` classpath
- **WHEN** the harness looks up `in-memory` and `snapshot`
- **THEN** each lookup returns a store with that name (`PolicyStoresTest`)

#### Scenario: Unknown store name
- **GIVEN** no store named `missing` on the classpath
- **WHEN** the harness looks up `missing`
- **THEN** it throws `IllegalArgumentException` whose message contains `in-memory` and `snapshot` (`PolicyStoresTest`)

#### Scenario: Store argument
- **GIVEN** a store named `probe` that records its argument
- **WHEN** the harness selects `probe:a:b` and opens a session, then selects `probe` and opens a session
- **THEN** the store receives the argument `a:b` the first time and no argument the second time (`PolicyStoresTest`)

#### Scenario: Duplicate store name
- **GIVEN** two test stores that both call themselves `twin`
- **WHEN** the harness looks up `twin`
- **THEN** it throws `IllegalStateException` that names both classes (`PolicyStoresTest`)

#### Scenario: Store from another jar
- **GIVEN** a `PolicyStore` that is registered in `META-INF/services` of a classpath entry outside the main code of `xacml-jmh`
- **WHEN** a fixture is opened with that store name
- **THEN** the fixture uses that store and passes its checks (`PdpFixtureTest`; the forked-JVM case is the run of task 3.6)

### Requirement: Store sessions are released
Every use of a store SHALL be a session with its own repository id, and the
harness SHALL close every session that it opened: when a fixture closes, when
opening a fixture fails after the session was opened, and after each
operation of the load benchmark. A repository id SHALL differ from the ids of
other JVMs, including other JMH forks, and SHALL hold only letters, digits,
`.`, `_`, and `-`.

#### Scenario: Ids across processes
- **GIVEN** a fixture of a `dir` corpus, whose name holds a path
- **WHEN** its repository id and a further context id are read
- **THEN** both hold the process id of the JVM and only letters, digits, `.`, `_`, and `-` (`ReviewGateFixesTest`)

#### Scenario: Fixture lifecycle
- **GIVEN** a test store that counts opened and closed sessions
- **WHEN** a fixture is opened and closed, and another fixture fails its decision check
- **THEN** the store counts two sessions opened and two closed, with two different repository ids (`PdpFixtureTest`)

#### Scenario: Load operations
- **GIVEN** the counting store
- **WHEN** the load benchmark runs 50 operations in process
- **THEN** the store counts as many closed sessions as opened ones, and no repository id twice (`PolicyStoreLoadBenchmarkTest`)

### Requirement: Policy corpus selection
The harness SHALL select the policy corpus from the value of the `corpus`
benchmark parameter, written as `name` or `name:argument`, among the
`PolicyCorpusProvider` implementations that `ServiceLoader` finds. The harness
SHALL provide the corpora `rsa2008`, `synthetic` (argument: the policy count,
default 100), and `dir` (argument: a directory path). An unknown name SHALL
fail before measurement with a message that lists the known corpus names, and
a duplicate name SHALL fail with a message that names both classes.

#### Scenario: Built-in corpora are found
- **GIVEN** the `xacml-jmh` classpath
- **WHEN** the harness loads `rsa2008` and `synthetic:10`
- **THEN** it returns corpora named `rsa2008` and `synthetic:10` (`PolicyCorporaTest`)

#### Scenario: Unknown corpus name
- **GIVEN** no corpus named `missing`
- **WHEN** the harness loads `missing`
- **THEN** it throws `IllegalArgumentException` whose message contains `rsa2008`, `synthetic`, and `dir` (`PolicyCorporaTest`)

### Requirement: Decisions and lookups are checked before measurement
A fixture SHALL load every corpus policy into the selected store, take the
root policy from the store by its identifier and version, build the PDP
without a decision cache, and before any measurement decide every corpus
request once and run every lookup query of the corpus once. The fixture SHALL
fail when the store does not return the root policy, when deciding a request
throws, when a request yields other than exactly one result, when a decision
differs from the expected decision, or when a lookup returns no rule or a rule
of another type, identifier, or version than the parsed corpus. The failure
message SHALL name each failing request or lookup. An expected
`Indeterminate` SHALL match every Indeterminate decision (`Indeterminate`,
`Indeterminate{D}`, `Indeterminate{P}`, `Indeterminate{DP}`), because a
response carries only `Indeterminate` (XACML 3.0 §5.53, §7.10). The expected
target of a reference lookup SHALL be the highest corpus version that matches
the reference's `Version` by XACML 3.0 §5.13, where `*` matches one whole
version number. The benchmarks SHALL run the same checks again when a trial
ends, and SHALL fail the trial when a check fails.

#### Scenario: Wildcard version reference
- **GIVEN** a corpus with versions 1.9 and 1.10 of a policy and a reference with `Version="1.*"`
- **WHEN** its lookup queries are built, and a fixture is opened with the `in-memory` store
- **THEN** the reference query expects 1.10, and the fixture fails naming the reference lookup, because the engine's matcher returns 1.9 (bead policy4j-w842) (`ReviewGateFixesTest`)

#### Scenario: A store that changes during a trial
- **GIVEN** a decide benchmark set up with a store that later loses its rules
- **WHEN** the store changes and the trial tears down
- **THEN** tear-down throws `IllegalStateException` that names a lookup (`ReviewGateFixesTest`)

#### Scenario: Every built-in store and corpus passes
- **GIVEN** the store `in-memory` or `snapshot` and the corpus `rsa2008`, `synthetic:1`, `synthetic:2`, `synthetic:100`, or a valid `dir` corpus
- **WHEN** a fixture is opened
- **THEN** it opens, every request has its expected decision, and every lookup returns its rule (`PdpFixtureTest`)

#### Scenario: A store loses a referenced policy
- **GIVEN** a store that does not keep one policy set that the root policy set refers to, once returning nothing for it and once throwing on its lookup
- **WHEN** a fixture is opened with the `rsa2008` corpus
- **THEN** it throws `IllegalStateException` that names each request whose decision changed or whose evaluation threw, with the exception class (`PdpFixtureTest`)

#### Scenario: A store loses an unreferenced policy or misreports a version
- **GIVEN** a store that drops a policy that no request reaches, and a store that returns an older version as the latest
- **WHEN** a fixture is opened
- **THEN** it throws `IllegalStateException` that names the failing lookup (`PdpFixtureTest`)

#### Scenario: A store loses the root policy
- **GIVEN** a store that does not keep the root policy
- **WHEN** a fixture is opened
- **THEN** it throws `IllegalStateException` that names the root policy (`PdpFixtureTest`)

#### Scenario: A wrong expectation
- **GIVEN** a `dir` corpus whose manifest expects `Deny` for a request that yields `Permit`
- **WHEN** a fixture is opened
- **THEN** it throws `IllegalStateException` that names that request, the expected decision, and the actual decision (`PdpFixtureTest`)

### Requirement: Snapshot store answers like the in-memory store
The `snapshot` store SHALL return the same policy or policy set as the
`in-memory` store for every `get(id, version)` lookup of a corpus rule, every
`getPolicy` lookup of a policy identifier, and every `getPolicySet` lookup of
a policy-set identifier. `getPolicies` and `getPolicySets` SHALL return the
matching versions sorted from the earliest to the latest version. Adding a
policy whose identifier and version are already present SHALL return `false`
and keep the first policy. For an identifier that it does not hold, the
`snapshot` store SHALL return an empty collection from `getPolicies` and
`getPolicySets` and `null` from `getPolicy`, `getPolicySet`, and `get`, as the
`PolicyRepository` contract states.

#### Scenario: Same answers for every corpus identifier
- **GIVEN** the `rsa2008` and `synthetic:100` corpora loaded into both stores
- **WHEN** every rule is looked up by exact version, and every policy or policy-set identifier by latest version with the lookup of its type
- **THEN** both stores return equal results (`SnapshotPolicyRepositoryTest`)

#### Scenario: Versions in order and duplicates kept once
- **GIVEN** versions 1.0, 1.2, and 1.1 of one policy added in that order, then 1.1 added again
- **WHEN** `getPolicies(id)` and `getPolicy(id, null, null, null)` are called
- **THEN** the versions come back as 1.0, 1.1, 1.2; the latest is 1.2; the second add of 1.1 returns `false` (`SnapshotPolicyRepositoryTest`)

#### Scenario: Unknown identifier
- **GIVEN** a `snapshot` store without the identifier `missing`
- **WHEN** `getPolicies`, `getPolicySets`, `getPolicy`, `getPolicySet`, and `get` are called for `missing`
- **THEN** the collections are empty and the single lookups return `null` (`SnapshotPolicyRepositoryTest`)

### Requirement: RSA 2008 corpus
The `rsa2008` corpus SHALL hold the nine XACML 2.0 policy sets and the 17
requests of the RSA 2008 interop suite, with policy set 01 as the root. The
expected decision of each request SHALL come from its vendored response file.
The vendored files SHALL NOT be changed.

#### Scenario: Corpus contents
- **GIVEN** the `rsa2008` corpus
- **WHEN** it is loaded
- **THEN** it has 9 policies, 17 requests with the ids `01-01` to `05-02`, and the root `XacmlPolicySet-01-top-level.xml` (`Rsa2008CorpusTest`)

### Requirement: Synthetic corpus
The `synthetic:<n>` corpus SHALL generate `n` XACML 3.0 policies and one root
policy set that refers to them with `PolicyIdReference`. The same `n` SHALL
give equal policy sources and equal requests. The generator SHALL compute
each expected decision from its own rules, without a PDP. The requests SHALL
target the distinct slots of policies `0`, `n/2`, and `n-1` with one `Permit`
and one `Deny` request each, plus one `NotApplicable` request, with ids that
name the slot and the outcome. A count that is not an integer from 1 to
100,000 SHALL be rejected.

#### Scenario: Deterministic output
- **GIVEN** two loads of `synthetic:50`
- **WHEN** their policy sources and requests are compared
- **THEN** they are equal (`SyntheticCorpusTest`)

#### Scenario: Slots and outcomes
- **GIVEN** `synthetic:1`, `synthetic:2`, and `synthetic:100`
- **WHEN** they are loaded
- **THEN** they have 3, 5, and 7 requests with unique ids; their expected decisions include `Permit`, `Deny`, and `NotApplicable`; and the requests of `synthetic:100` target policies 0, 50, and 99 (`SyntheticCorpusTest`)

#### Scenario: Count out of range
- **GIVEN** the arguments `0`, `100001`, `-3`, and `ten`
- **WHEN** `synthetic` is loaded with each of them
- **THEN** each load throws `IllegalArgumentException` (`SyntheticCorpusTest`)

### Requirement: Directory corpus
The `dir:<path>` corpus SHALL read a manifest `corpus.properties` in the
directory, the policies from its `policies/` subdirectory, and the requests
from its `requests/` subdirectory, each sorted by file name. The manifest SHALL
give `format` (`xacml30` or `xacml20`), `root` (the file name of the root
policy in `policies/`), and one `expect.<request file name>` entry per request
with the value `Permit`, `Deny`, `NotApplicable`, or `Indeterminate`. Request
file names SHALL use only letters, digits, `.`, `_`, and `-`. The corpus SHALL
fail with a message that names the problem when the manifest is missing, the
format is unknown, the root file is missing, a request file has no
expectation or an invalid name, an expectation has no request file, or a
decision value is unknown.

#### Scenario: Valid directory
- **GIVEN** a directory with a manifest, two policies, and three requests
- **WHEN** `dir:<that directory>` is loaded and a fixture is opened with it
- **THEN** the corpus has two policies and three requests in file-name order, and the fixture passes its checks (`DirectoryCorpusTest`)

#### Scenario: Manifest errors
- **GIVEN** a directory with one of these problems: no manifest, format `xacml40`, a missing root file, a request without expectation, an expectation without request, the decision `Maybe`, a request file named `a b.xml`
- **WHEN** it is loaded
- **THEN** the load throws `IllegalArgumentException` whose message names the problem (`DirectoryCorpusTest`)

### Requirement: Benchmark set
The module SHALL provide `PdpDecideBenchmark`, `PolicyStoreLookupBenchmark`,
and `PolicyStoreLoadBenchmark`. Each SHALL take the `store` and `corpus`
parameters, prepare its inputs only in setup methods, and return or consume
every result. `PdpDecideBenchmark` SHALL take a `request` parameter: `all`
decides the corpus requests in turn, one per operation; a request id decides
only that request; an unknown id SHALL fail in setup. With more than one
benchmark thread, setup SHALL fail unless the store declares concurrent reads,
and SHALL first run every corpus lookup on the store and decide every request
from that many threads at once and check every answer. The load benchmark's
setup SHALL load and import the corpus once each into fresh sessions and check
every lookup of both before measuring, and its import operation SHALL use one
import tool per session. A run with the JMH GC profiler SHALL report
`gc.alloc.rate.norm` for a benchmark, or the run record SHALL say that it was
not reported.

#### Scenario: Concurrent lookups are checked
- **GIVEN** a store that declares concurrent reads but finds nothing off the thread that opened it
- **WHEN** the concurrent check runs with two threads
- **THEN** it throws `IllegalStateException` that names a lookup (`ReviewGateFixesTest`)

#### Scenario: Import through a store that refuses adds
- **GIVEN** a store whose `load` fills it but whose repository refuses `add`
- **WHEN** the load benchmark is set up
- **THEN** setup throws `IllegalStateException` that names the import session (`ReviewGateFixesTest`)

#### Scenario: One import tool per operation
- **GIVEN** a store that counts the import tools of its repositories
- **WHEN** one `importXml` operation runs
- **THEN** the store counts one import tool (`ReviewGateFixesTest`)

#### Scenario: Smoke run of every benchmark
- **GIVEN** the default stores and corpora
- **WHEN** every benchmark method runs in process with one short measurement iteration and fail-on-error
- **THEN** each run ends without error, and the results hold every benchmark method and parameter set with a finite score (`BenchmarkSmokeTest`)

#### Scenario: Allocation metric
- **GIVEN** a smoke run of `PdpDecideBenchmark` with the GC profiler
- **WHEN** the run ends
- **THEN** its result has the secondary metric `gc.alloc.rate.norm` (`BenchmarkSmokeTest`; the baseline run of task 4.2 for every benchmark)

#### Scenario: Request selection
- **GIVEN** the `rsa2008` corpus
- **WHEN** `PdpDecideBenchmark` is set up with `request=01-01`, then with `request=99-99`
- **THEN** the first selects only request `01-01`, and the second fails in setup with a message that names `99-99` (`PdpDecideBenchmarkTest`)

#### Scenario: Several threads
- **GIVEN** a test store without concurrent reads, and the `in-memory` store
- **WHEN** the thread check runs for two threads
- **THEN** it fails for the test store and passes for `in-memory` after the concurrent decision check (`PdpDecideBenchmarkTest`)
