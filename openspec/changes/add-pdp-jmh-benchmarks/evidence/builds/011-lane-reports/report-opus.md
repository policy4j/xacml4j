FINAL REPORT (policy4j_test_writer, Claude Opus 5.5)

**Lane:** tests, task 3.1 (policy4j-ba4s.3.1). CLI: Claude Code. Model: Claude Opus 5.5 (claude-opus-5-5).

**Result:** 18 test classes with 99 test methods (165 JUnit invocations when parameter cases are counted). They cover every requirement and scenario of `specs/pdp-benchmark-harness/spec.md`, plus `ReferenceCacheCharacterizationTest`. There are also 23 test-only support classes and 2 service files. No main code was edited, no Maven ran in the shared tree, and no xacml-jmh test was run. The pass/fail counts are UNVERIFIED until the conductor's gate run.

**Files** (all new, under `xacml-jmh/src/test/`):
- `java/org/xacml4j/jmh/harness/`: `PdpFixtureTest` (taken over; both spine tests kept unchanged), `PolicyStoresTest`, `PolicyCorporaTest`, `ReferenceCacheCharacterizationTest`, `SelectionTest`, `CorpusRequestTest`, `PolicySourceTest`, `PolicyCorpusTest`, `RulesTest`, `LookupQueryTest`
- `java/org/xacml4j/jmh/corpus/`: `Rsa2008CorpusTest`, `SyntheticCorpusTest`, `DirectoryCorpusTest`
- `java/org/xacml4j/jmh/store/`: `SnapshotPolicyRepositoryTest`
- `java/org/xacml4j/jmh/pdp/`: `PdpDecideBenchmarkTest`, `PolicyStoreLoadBenchmarkTest`, `PolicyStoreLookupBenchmarkTest`, `BenchmarkSmokeTest`
- `java/org/xacml4j/jmh/testing/`:
  - stores: `ProbeStore` (`probe`), `CountingStore` (`counting`), `FirstTwinStore` and `SecondTwinStore` (`twin`), `LosingStore` (`losing:<id>`, finds nothing for that id), `ThrowingStore` (`throwing:<id>`, throws `LostPolicyException`), `StaleLatestStore` (`stale-latest`), `SingleReaderStore` (`single-reader`, no CONCURRENT_READS), `ReadOnlyStore` (`read-only`, no LOAD), `RecordingStore` (`recording`, logs every lookup)
  - repositories: `ForwardingPolicyRepository`, `LosingPolicyRepository`, `StaleLatestPolicyRepository`, `RecordingPolicyRepository`
  - corpora: `ProbeCorpus` (`probe`), `FirstTwinCorpus` and `SecondTwinCorpus` (`twin`)
  - helpers: `TestStores`, `TestCorpora`, `TestPdps`, `FailureLines`, `JmhParams`, `LostPolicyException`
- `resources/META-INF/services/org.xacml4j.jmh.harness.PolicyStore` and `…PolicyCorpusProvider`

**Scenario → test**
- Store selection (built-in, unknown, argument, duplicate) → `PolicyStoresTest`. Store from another jar → `PdpFixtureTest.storeRegisteredOutsideTheMainCodeIsUsedByName`.
- Sessions released:
  - Fixture lifecycle → `PdpFixtureTest.fixtureClosesItsSessionWhenItClosesAndWhenItsDecisionCheckFails`, plus a case where the store rejects a policy.
  - 50 load operations → `PolicyStoreLoadBenchmarkTest` (both `loadParsed` and `importXml`).
- Corpus selection (built-in, unknown, duplicate, argument) → `PolicyCorporaTest`.
- Checks before measurement, all in `PdpFixtureTest`:
  - Matrix: {in-memory, snapshot} × {rsa2008, synthetic:1, synthetic:2, synthetic:100}, plus a `dir` corpus written in `@TempDir`.
  - Failure cases: a lost referenced policy set (once returning nothing, once throwing), a lost root, a lost unreferenced policy, a stale latest version, a wrong `dir` expectation, and a request with two results.
  - Indeterminate matching is also covered by the `CorpusRequestTest` truth table.
- Snapshot store answers like in-memory, version order, duplicate add, unknown id → `SnapshotPolicyRepositoryTest`. It uses only lookups that match each id's type, because of the in-memory defect policy4j-dofm.
- RSA 2008, synthetic, and directory corpora → the three corpus tests. They load only through `PolicyCorpora.load` (D13).
- Benchmark set:
  - Smoke run and `gc.alloc.rate.norm` → `BenchmarkSmokeTest`.
  - Request selection, cursor wrap and thread offsets, two-thread check → `PdpDecideBenchmarkTest`.
  - Lookup dispatch by query kind → `PolicyStoreLookupBenchmarkTest`.
- D7 premise (reference cache hides the store) → `ReferenceCacheCharacterizationTest`.

**Decisions inside the fixed contract**
1. **Configurable losing stores.** `LosingStore` and `ThrowingStore` take the id to lose as the store argument (`losing:<id>`). One class covers the root, a referenced set, and an unreferenced policy.
2. **Static records.** ServiceLoader makes a new store instance on every lookup, so the probe, counting, and recording stores keep static records that each test resets first. Tests must run sequentially, which is the JUnit default.
3. **Oracle for the lost policy set.** The lost set is `…policysetid:emergency`, the target of the root's first child, which has an empty Target (`XacmlPolicySet-01-top-level.xml:14-21`), so every request reaches it. A separate PDP with the D6 settings, over its own session of the same store (`TestPdps`), shows which requests change or throw. The fixture message must name exactly those requests, with the exception class for the ones that throw.
4. **Test corpus for the unreferenced and stale-version cases.** rsa2008 has no unreferenced policy set: the root refers to 6, 02b to 03, 02c to 03 twice, 03 to 04. So these cases use a small XACML 3.0 corpus (`TestCorpora.versioned`). Its expected decisions cite XACML 3.0 core §7.3.5, §7.10, §7.11 Table 4, and C.2; I checked the § numbers against the OASIS 3.0 OS spec (docs.oasis-open.org, read 2026-10-26).
5. **Cursor test.** The test reads the cursor position from the rsa2008 decision sequence. It has 17 requests (a prime) and is not constant, so each start index gives a different sequence; the test asserts this first. It checks 2n+1 operations against start = t·n/T for six thread cases.
6. **Load test through the JMH Runner.** It runs in process with forks 0, single-shot mode, and batch size 50, which makes exactly 50 calls (JMH 1.37 `BenchmarkGenerator.java:1060-1065`). It needs no load-method signature and takes the JMH lock.
7. **Members beyond D13 that the tests call.** `decide(Cursor)` returning `ResponseContext`; `lookup(Cursor)`, whose result is taken as `Object` and an `Optional` is unwrapped; `cursor(Cursor)`, result unused. The lane's current code matches.
8. **Expected exception types.** `IllegalArgumentException` for an unknown request id or store. For a missing capability, a `RuntimeException` whose message names the store and matches `(?i)concurrent` or `(?i)load`.

**Verification**
All compiles used Maven 3.9.16 (IntelliJ bundled), JDK 21.0.6, offline:
`rtk mvn -B -o -q -Dlicense.skipUpdateLicense=true -f "$TMPDIR/xacml4j-tests…/pom.xml" -pl xacml-jmh -am test-compile`
- **Lane copy, packet excludes, 5 lane-dependent tests excluded:** OK. `test-classes/META-INF` holds only my 2 service files, so test compilation wrote no JMH BenchmarkList.
- **Lane copy plus D13 compile stubs (copy only), all tests:** OK.
- **Second copy `$TMPDIR/xacml4j-tests-integration` with the other lanes' current files:** OK. Its BenchmarkList has decide, lookup, cursor, loadParsed, importXml.

Two scratch programs (not tests, no JMH lock) confirmed:
- The `TestCorpora` decisions: read Permit, delete Deny, audit Indeterminate{P}, write NotApplicable.
- `stale-latest` and `losing:spare` fail only their lookup.
- `losing:emergency` changes the 9 Permit requests to Deny.
- `throwing:emergency` makes all 17 requests throw `LostPolicyException`.
- rsa2008 has 9 exact lookups and 10 policy-set reference lookups.
- After open, the fixture's PDP makes 0 store reads. A new PDP makes 1–6 reads on the first decide of a request and 0 on the second.
- Two loads of `synthetic:50` give equal policies and equal requests.

I read the lane sources (benchmarks, snapshot store, corpora); every assumption in my tests holds. `rtk git status`: nothing changed outside `xacml-jmh/`, nothing under `xacml-conformance-tests`.

**Findings** (nothing fixed; no production edits)
- **LOW, spine:** `Rules.describe` prints versions as `Version{version=1.0}`, not `1.0` (`Rules.java:67-69`; seen in the scratch output). My assertions check the substrings "1.0" and "2.0", so they also hold after a fix.
- **INFO, benchmarks lane:** `importXml` creates one import tool per source (`PolicyStoreLoadBenchmark.java:122-124`), and each one discovers the marshalling providers (`PolicyImportTool.java:54`). One operation on synthetic:100 therefore includes 101 discoveries. D7 does not say whether discovery should count once per operation or once per source; this is for rv004/rv005.

**Could not do:** run the tests (packet rule).

**Residual risks**
- `BenchmarkSmokeTest` runs 20 parameter sets in process and may be slow because of `importXml` on synthetic:100. It fails if another JMH run holds the lock.
- `gc.alloc.rate.norm` is reported only when allocation is non-zero (`GCProfiler.java:162`); decide allocates, so the test expects it.
- The lanes' files may change after my read.
- `PdpDecideBenchmarkTest.setUpThatFailsReleasesEverySessionItOpened` goes beyond the literal spec scenarios. It follows from "close every session that it opened", because JMH runs no trial teardown after a failed setup.

No BLOCKER.
