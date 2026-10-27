FINAL REPORT (policy4j_code_review_codex_sol, GPT-6.1 Sol)

Lane: rv006-spine-code. CLI: Codex. Model designation supplied by packet; runtime identity UNVERIFIED.

**Verdict: REJECT — three major findings.**

Reviewed the uncommitted working tree on `feature_xacml_20`, HEAD `5fbbd6bcd473de56384dcdab0a912a1e1f74e88b`. “Checked” below means source inspection. Runtime reproductions of the findings were not run.

1. **MAJOR — the lookup oracle reproduces an existing wildcard defect and can approve the wrong version.**

   Checked in [LookupQuery.java:141](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/LookupQuery.java:141): expected reference targets use `VersionMatch.match`. That matcher expands `*` to a single digit, checked in [VersionMatch.java:35](xacml-core/src/main/java/org/xacml4j/v30/VersionMatch.java:35) and `:94`. XACML defines `*` as a complete numeric component. [XACML 3.0 §5.13](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html)

   For policies `P` versions `1.9` and `1.10`, both permitting the request, and a reference with `Version="1.*"`, the oracle expects `1.9`. The in-memory store uses the same defective matcher and also selects `1.9`, so both checks pass; a correct external store returning `1.10` fails the lookup check. Store filtering and latest selection checked in `xacml-pdp/.../InMemoryPolicyRepository.java:200` and `AbstractPolicyRepository.java:99`; identity comparison checked in `PdpFixture.java:275`.

   **Fix:** correct wildcard matching, through a separately scoped core fix if necessary, and pin the oracle’s expected result independently. **Test:** a versioned corpus containing `1.9` and `1.10` with `Version="1.*"` must expect `1.10`; a store returning `1.9` must fail. The underlying matcher defect predates this change; the new guard masks it.

2. **MAJOR — concurrent validation does not exercise the lookup benchmark’s concurrent store reads.**

   Checked in [PdpFixture.java:219](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java:219): verifier threads only decide requests through the already checked PDP. Cached references bypass repository reads, checked in `xacml-pdp/.../DefaultPolicyReferenceResolver.java:82`; this behavior is expressly asserted in `ReferenceCacheCharacterizationTest.java:57`.

   The lookup benchmark calls this verifier during its thread check, then measures direct repository lookups without checking returned identities. Checked in `xacml-jmh/.../pdp/PolicyStoreLookupBenchmark.java:90` and `:106`. A store declaring concurrent reads but returning wrong rules during overlapping reads can therefore pass setup and produce finite scores for incorrect lookups. D8’s decision-only check leaves this D7 workload unvalidated.

   **Fix:** concurrently run and validate the fixture’s lookup queries before multithreaded lookup measurement. **Test:** add a store that answers correctly serially but returns a wrong version during coordinated overlapping reads; setup must reject it.

3. **MAJOR — repository IDs repeat across JVM forks.**

   Checked in [PdpFixture.java:308](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java:308): IDs contain only store name, corpus name, and a counter initialized to zero at `:59`. Each fresh fork repeats that sequence. Three forks are configured in `PdpDecideBenchmark.java:52`; separate processes are created by JMH 1.37’s `Runner.java:622`, `:657`, and `:717`, inspected from its local source archive.

   An external store using the supplied ID as its persistent namespace can reuse an earlier fork’s data or reject duplicate loads. This violates D3’s explicit namespace promise, checked in `design.md:146`. Existing uniqueness assertions stay within one JVM, checked in `PdpFixtureTest.java:194` and `PolicyStoreLoadBenchmarkTest.java:63`.

   **Fix:** include a process-unique prefix generated once, followed by the session counter. **Test:** two fresh JVMs selecting the same store and corpus must produce disjoint repository IDs.

The scenario coverage is present, with the gaps above. Test paths below are relative to `xacml-jmh/src/test/java/org/xacml4j/jmh/`.

| Spec scenarios | Source inspection |
| --- | --- |
| Store selection: built-ins, unknown name, argument, duplicates, external provider | Checked in `harness/PolicyStoresTest.java:53`, `:76`, `:106`, `:121`; `harness/PdpFixtureTest.java:147`. Assertions check concrete providers, arguments, and both duplicate classes. |
| Session release: fixture success/failure and load operations | Checked in `harness/PdpFixtureTest.java:158`, `:178`; `pdp/PolicyStoreLoadBenchmarkTest.java:63`. Open/close records are compared; uniqueness coverage is JVM-local. |
| Corpus selection: built-ins and unknown name | Checked in `harness/PolicyCorporaTest.java:49`, `:63`; argument and duplicate assertions at `:74`, `:83`. |
| Decision/lookup checks: all combinations, lost references, lost unreferenced policy, stale version, lost root, wrong expectation | Checked in `harness/PdpFixtureTest.java:125`, `:136`, `:208`, `:223`, `:245`, `:265`, `:278`, `:308`. Tests distinguish lookup-only failures and inspect request diagnostics. |
| Snapshot equivalence, ordered versions/duplicates, unknown identifiers | Checked in `store/SnapshotPolicyRepositoryTest.java:71`, `:107`, `:146`. |
| RSA contents and expected decisions | Checked in `corpus/Rsa2008CorpusTest.java:87`, `:98`, `:121`. All 17 hard-coded decisions match the vendored response files’ `<Decision>` at line 27, independently searched during this review. |
| Synthetic determinism, slots/outcomes, invalid counts | Checked in `corpus/SyntheticCorpusTest.java:63`, `:75`, `:93`, `:146`. |
| Directory contents/order and manifest errors | Checked in `corpus/DirectoryCorpusTest.java:64`, `:87`, `:157`. Fixtures are deliberately written out of order; all seven specified error cases are represented. |
| Benchmark completeness, allocation, request selection, thread guards | Checked in `pdp/BenchmarkSmokeTest.java:68`, `:88`; `pdp/PdpDecideBenchmarkTest.java:72`, `:89`, `:159`, `:171`. Completeness compares exact method/parameter keys and checks finite scores; concurrent faulty-store coverage needs finding 2’s test. |

Other checks found correct:

- Failed fixture opening attempts cleanup through `finally`; ordinary closing attempts PDP closure before session closure and retains secondary failures. Checked in `PdpFixture.java:121`, `:247`, `:326`.
- Build configuration appends the JMH processor to the parent processor path, copies RSA resources, explicitly disables dependency-reduced POM creation, merges services, sets the executable manifest and multi-release flag, excludes signatures, and skips deployment. Checked in `xacml-jmh/pom.xml:19`, `:49`, `:63`, `:85`, `:87`, `:89`, `:91`, `:93`, `:101`; parent processor checked in `pom.xml:352`. Reactor and ignore entries checked in `pom.xml:28` and `.gitignore:59`; WARN logging checked in `log4j2.xml:13`.
- The smoke test obtains locking through `Runner.run()`. Checked in `BenchmarkSmokeTest.java:75` and JMH 1.37 source-archive `Runner.java:182`, `:203`.

Verification performed: scoped source reads and `rtk rg`; Git diff/status/show; cache-disabled ripwire impact; read-only ripwire quality delta; scoped `ast-grep scan` (exit 0); root POM/ignore diff check (exit 0). Ripwire results were PARTIAL because of name-based resolution, reflective entry points, and unrelated shared-tree changes. Final fingerprints matched for 64 reviewed source/resource files.

No files were changed. No Maven, tests, benchmarks, forks, beads writes, or Git writes were run. Retained functional evidence in runs 008 and 010 was read, not rerun. Runtime counterexamples and validation after fixes remain UNVERIFIED.
