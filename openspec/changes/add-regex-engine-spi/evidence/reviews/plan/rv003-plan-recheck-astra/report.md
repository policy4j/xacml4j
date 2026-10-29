FINAL REPORT (RXP1R plan reviewer, GPT-6)

Five prior findings are resolved at plan level. One benchmark detail remains unresolved, and the revision introduces a contradiction about the repetition limit.

| Prior finding | Disposition | Evidence |
|---|---|---|
| **1 — P1: Incompatible legacy and strict semantics** | **RESOLVED** | Separate profiles preserve legacy translation, bypass strict parsing for legacy, and restrict cross-engine equality to `xf`. Explicit difference tests cover the identified cases. [design.md:87](openspec/changes/add-regex-engine-spi/design.md:87), [spec.md:54](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md:54). |
| **2 — P1: Compilation bound applied too late** | **RESOLVED** | The plan now requires an overflow-safe expansion estimate and emitted-length bound before native compilation, with a counting-compiler test. [design.md:134](openspec/changes/add-regex-engine-spi/design.md:134), [spec.md:69](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md:69). |
| **3 — P1: Transient failures retained permanently** | **RESOLVED** | Only deterministic failures are retained; deadline/resource failures permit retry. The timeout-then-success scenario is observable. [design.md:163](openspec/changes/add-regex-engine-spi/design.md:163), [spec.md:104](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md:104). |
| **4 — P2: Grammar editions mixed silently** | **RESOLVED** | The proposed baseline explicitly pins XF 1.0 (2007); later behavior is a named maintainer decision. Difference tests cover dot and noncapturing groups. [design.md:96](openspec/changes/add-regex-engine-spi/design.md:96), [design.md:264](openspec/changes/add-regex-engine-spi/design.md:264). |
| **5 — P2: Status migration contradicts characterization** | **RESOLVED** | Historical characterization and acceptance are distinguished, approved exceptions are explicit, and status/message changes are marked BREAKING. [design.md:142](openspec/changes/add-regex-engine-spi/design.md:142), [spec.md:35](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md:35). |
| **6 — P2: Benchmark controls and first-use claims** | **NOT RESOLVED** | Wrapper controls, full function calls, and warmed-decision labels are fixed. The optional first-use case still needs the cold-state guarantee described below. [design.md:209](openspec/changes/add-regex-engine-spi/design.md:209). |

**Remaining P2 — A fresh fixture is already warmed when opened.**  
The proposed first-use benchmark requires `SingleShotTime` and a fresh fixture per invocation, but `PdpFixture.open()` calls `fixture.check()` before returning. Recreating that fixture in benchmark setup therefore compiles the literal before the measured decision. [design.md:216](openspec/changes/add-regex-engine-spi/design.md:216), [PdpFixture.java:124](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java:124).

**Smallest fix:** if first-use cost will be claimed, require an unevaluated fixture and perform correctness checks on separate state. Assert zero compilations immediately before the measured decision and one afterward. Alternatively, retain only the explicitly warmed measurements.

**New P2 — The repetition cap has contradictory profile scope and an unmarked compatibility effect.**  
The disposition says counts above 1000 become structural limits on **both profiles**, while the normative requirement scopes the new bound to `xf`. D5 describes the limit row as unchanged. [summary.md:17](openspec/changes/add-regex-engine-spi/evidence/reviews/plan/rv001-plan-astra/summary.md:17), [spec.md:70](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md:70), [design.md:148](openspec/changes/add-regex-engine-spi/design.md:148).

Applying that cap to legacy would change existing successful matches into Indeterminate. The current test explicitly expects `(?:){1000}{1001}` to match empty input. [RegExpLimitsTest.java:264](xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegExpLimitsTest.java:264).

**Smallest fix:** make Open Question 4 explicitly identify the engines/profiles affected, then align the disposition, D5, and spec. If legacy changes are proposed, mark them BREAKING and add them to the compatibility exceptions. Add boundary cases at 1000/1001 and preserve or explicitly supersede the existing legacy case.

Open Questions 1, 2, 3, and 5 clearly identify maintainer choices. Question 4 needs the scope clarification above; this review chooses none of their answers. [design.md:258](openspec/changes/add-regex-engine-spi/design.md:258).

Verification: read the disposition, prior report, revised proposal/design/spec/tasks, and relevant limit tests and benchmark source. No files changed; no builds, tests, benchmarks, validation, Git writes, or beads writes ran. Implementation correctness and performance remain **UNVERIFIED**.

**Verdict: APPROVE WITH CHANGES.**
