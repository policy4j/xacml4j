## Why

The PDP has no valid performance evidence. The only timing code is an ignored
JUnit loop (`xacml-pdp/src/test/java/org/xacml4j/util/FunctionInvocationBenchmark.java`)
and `RSA2008PerformanceTestCase`, which times 10,000 loops with
`System.currentTimeMillis()` and is excluded from Surefire. Planned work in
`modernize-core-for-jdk28-valhalla` (tasks 6.8, 9.2 to 9.4) needs JMH numbers
for time and allocation per decision. Policy stores are pluggable
(`PolicyRepository`), so one benchmark must run the same scenarios against
any store, including stores that live outside this repository.

## What Changes

- A new Maven module `xacml-jmh` (JMH 1.37) that builds an executable
  `benchmarks.jar`. It is in the reactor and is not deployed.
- A small harness API in `org.xacml4j.jmh.harness`:
  - `PolicyStore` and `StoreSession`: open an isolated `PolicyRepository`,
    load parsed policies into it, and always release it.
  - `PolicyCorpusProvider` and `PolicyCorpus`: policies, the root policy,
    and requests with their expected decisions.
  - Name-based selection (`name` or `name:argument`) through
    `ServiceLoader`, so a store or corpus in another jar is found without a
    change to this module.
  - `PdpFixture`: loads a corpus into a store, builds the PDP, and checks
    every expected decision and every store lookup before measurement
    starts.
- Stores: `in-memory` (the production `InMemoryPolicyRepository`) and
  `snapshot` (an immutable-map `AbstractPolicyRepository` in the module, which
  proves that a second implementation plugs in).
- Corpora: `rsa2008` (the RSA 2008 interop policies and 17 requests, read
  from the conformance fixtures without change), `synthetic:<n>` (generated
  XACML 3.0 policies with independently computed decisions), and
  `dir:<path>` (a user's own policies and requests, with a manifest).
- Benchmarks: `PdpDecideBenchmark` (time per decision),
  `PolicyStoreLookupBenchmark` (direct store lookups), and
  `PolicyStoreLoadBenchmark` (load parsed policies; import XML). Allocation
  per operation comes from the JMH GC profiler (`-prof gc`).
- Docs: `xacml-jmh/README.md`; `docs/ai/BENCHMARKS.md` and the module table
  in `AGENTS.md` are updated.
- A baseline JMH run on JDK 21 is recorded as evidence.

No public API of an existing module changes. No production code of an
existing module changes.

## Capabilities

### New Capabilities

- `pdp-benchmark-harness`: a reusable JMH harness for PDP evaluation: store
  and corpus selection, decision checks before measurement, allocation
  reporting, and the benchmark set.

### Modified Capabilities

None.

## Research Questions

- Q1. Which JMH release is current, and under which license?
- Q2. How does JMH report allocation per operation?
- Q3. Does a jar that is on the host classpath reach the forked benchmark
  JVMs, so that an external store can be added without a rebuild?
- Q4. How does a response carry the extended Indeterminate values, so that
  the decision check compares them correctly?

The decision check compares decisions with the vendored expected responses
and with decisions that the synthetic generator computes by itself.

## Verification

- `xacml-jmh` unit tests (JUnit 5): every store and corpus fixture opens and
  passes its decision check; unknown names fail and list the known names; a
  store that loses a policy fails the check; directory-manifest errors;
  synthetic determinism; an in-process JMH smoke run of every benchmark.
- Full reactor `clean test` on JDK 21 with no regression (8,009 tests, 0
  failures, 18 skipped before this change).
- `benchmarks.jar` builds; a baseline run with `-prof gc` on JDK 21 is
  recorded under `evidence/benchmarks/jmh/`.
- Code review and JMH method review from model families other than the
  authors'.

## Impact

- Modules: new `xacml-jmh`; one `<module>` entry in the root `pom.xml`; one
  `/xacml-jmh/target` line in `.gitignore`.
- Dependencies, in `xacml-jmh` only: `org.openjdk.jmh:jmh-core` and
  `jmh-generator-annprocess` 1.37 (GPL 2.0 with the Classpath Exception), and
  `maven-shade-plugin` 3.6.2 (the latest release, set by the change
  `upgrade-build-plugins`). The shaded `benchmarks.jar` is a separate file;
  the thin module jar is what an external store compiles against. No other
  module depends on `xacml-jmh`, and `maven.deploy.skip` keeps it out of
  releases. The maintainer approves the new module and these dependencies.
- Vendored files: `xacml-conformance-tests/src/test/resources/rsa2008-interop/**`
  is copied into the benchmark jar at build time and never changed.
- Found defects, not fixed here, recorded as one beads issue:
  `InMemoryPolicyRepository.getPolicies` and `getPolicySets` throw
  `NullPointerException` for an id that is absent from their map
  (`xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepository.java:81-87`,
  `:97-103`, `:198`), and for the XACML 1.0 and 2.0 policy-combining
  algorithms the exception escapes `decide`; the `size` argument of
  `DefaultPolicyReferenceResolver` is unused, so its cache is unbounded.
- Plan review: rv001 (Claude Fable 5.1), rv002 (Agy, Gemini 4 Argon), and
  rv003 (Codex GPT-6.1 Terra) under `evidence/reviews/plan/`; the design
  records how each finding was resolved.
- CI: unchanged. `.github/workflows/main.yml` still builds with JDK 8 (known
  gap).
- `modernize-core-for-jdk28-valhalla`: this change adds the module that its
  tasks 6.8 and 9.2 need. Those tasks keep their own benchmarks (function
  invocation, value parsing) and the JDK 28 baselines.
- Approval: the maintainer asked for this work on 2026-10-26 ("create a
  reusable jmh benchmark for testing pdp evaluation performance and
  allocations ... re-usable to run pdp with different policy stores", then
  "Implement").

## Tracking

Beads epic: see `tasks.md` header. Task X.Y maps to issue `<epic>.X.Y`.
