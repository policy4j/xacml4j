### Resolution of Prior RXP2 Findings

- **P1-1 (Engine selection via `PolicyRepository.Builder` disconnected from policy unmarshalling, conformance runner, and JMH `PdpFixture`) — RESOLVED**
  - Documented in Context ([design.md](openspec/changes/add-regex-engine-spi/design.md#L38-L42)) and specified in D3 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L114-L120)): `AbstractPolicyRepository` and `PolicyImportTool` pass the repository's `FunctionProvider` to `MarshallingProvider.newPolicyUnmarshaller(FunctionProvider, ...)`, and the conformance runner and `PdpFixture` use that path.
  - Covered by `Requirement: Selection reaches policy loading` and `Scenario: Two repositories` ([spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L25-L34)) and Task Group 4 ([tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L23-L28)).

- **P1-2 (Contradiction between `XacmlPattern` strict XF 1.0 validation / D5 status changes and `Requirement: The jdk engine keeps today's results`) — RESOLVED**
  - D1 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L87-L88)) and D2 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L90-L107)) separate `legacy` (today's translation, limits, and Java-only syntax; offered by `jdk` only; default; does not use `XacmlPattern`) from `xf` (strict XF 1.0 §7.6.1, offered by both `jdk` and `re2j` with cross-engine equality on the contract corpus).
  - D5 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L151-L156)) and [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L35-L46) separate the historical baseline (`RegExpCharacterizationTest`) from the post-change status acceptance test (`RegexStatusMatrixTest`), explicitly listing any changed rows.

- **P1-3 (Silent-misreading and translation defects in D3: `^`/`$` inside `[...]`, nested brackets from class escapes, and unbounded RE2 range expansion) — RESOLVED**
  - D4 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L128-L140)) specifies that every character class is computed as one code-point set and emitted as a single flat class (no nested brackets), `^` and `$` map to `\A` and `\z` only outside classes, and `XacmlPattern` enforces an overflow-safe program-size estimate and emitted-length bound before native compilation allocates (`Matcher.programSize()` remains a second check).
  - Covered by `Scenario: Classes are not misread` (`[a-z-[aeiou]]`, `[$]`, `[a^]`, `[\d_]`, `[\i-[:]]`, [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L59-L63)) and `Requirement: Compilation is bounded before it allocates` ([spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L69-L78)).

- **P2-1 (Benchmark claims in `design.md` and `tasks.md` cannot be measured as written) — RESOLVED**
  - Benchmark Plan ([design.md](openspec/changes/add-regex-engine-spi/design.md#L209-L220)), [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L114-L124), and [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L14-L40) (`2.2`, `3.3`, `5.3`, `6.4`) stages `RegexFunctionBenchmark` across groups, adds wrapped vs. unwrapped deadline cases for `re2j` on the same compiled patterns and inputs, adds counting hooks for cache hits/misses, and labels `PdpDecideBenchmark` results as warmed decisions (with `SingleShotTime` required for any first-use claim).

- **P2-2 (Missing observable scenarios in `spec.md` and inconsistent status for `a{1001}` vs repetition limits) — RESOLVED**
  - D4 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L139-L140)) and D5 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L148)) classify counts above 1000 as a structural limit (`processing-error` with the fixed limit message).
  - [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L20-L108) adds scenarios for `Duplicate engine name`, `Two repositories`, `Profile differences`, `Classes are not misread`, `Many allowed repetitions`, `Matrix` (statuses and messages), and `Transient failure is retried`.

- **P2-3 (Unmarked public API compatibility and system-property compatibility risks) — RESOLVED**
  - D7 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L174-L180)) and [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L38-L40) preserve `RegExpFunctions.matches(String, String)`, `covertXacmlToJavaSyntax(String)`, the six static function methods, and the existing `org.xacml4j.v30.policy.function.impl.RegExpFunctions.*` limit properties as defaults for `jdk`/`legacy`.
  - D5 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L151-L152)), D6 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L170-L172)), and task `1.3` ([tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L9)) explicitly mark the status-matrix and `cacheRegExps` changes as **BREAKING** items gated on maintainer approval.

- **P3-1 (Unstated XF 1.0 §7.6.1 grammar extensions in `XacmlPattern`: `.` and `(?:...)`) — RESOLVED**
  - D2 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L96-L101)), Open Question 5 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L264-L265)), and [spec.md](openspec/changes/add-regex-engine-spi/specs/regex-engine-spi/spec.md#L48-L50) pin `xf` to strict XF 1.0 §7.6.1 (23 January 2007: `.` excludes only LF, no `(?:...)`) and place later-edition deviations in Open Question 5 for maintainer decision.

- **P3-2 (Stale JDK 21 reference in `proposal.md` and task-number typo in `design.md`) — RESOLVED**
  - Fixed in [proposal.md](openspec/changes/add-regex-engine-spi/proposal.md#L53) (`JDK 25 support`) and [design.md](openspec/changes/add-regex-engine-spi/design.md#L209-L215) / [tasks.md](openspec/changes/add-regex-engine-spi/tasks.md#L40) (task `6.4`).

---

### New Defects Introduced by the Revision

None.

---

### Clarity of Open Questions 1 to 5

Open Questions 1 to 5 ([design.md](openspec/changes/add-regex-engine-spi/design.md#L256-L265)) are stated clearly, with concrete options, trade-offs, and cross-references to decisions D2 through D6:
1. **Engine selection scope** (`FunctionProviderBuilder`/`PolicyRepository.Builder` in D3 vs. per-PDP via `EvaluationContext`).
2. **Default profile** (`legacy` now vs. `xf` in a separate follow-up change).
3. **Statuses and messages** (D5 matrix vs. today's statuses).
4. **Numeric defaults** (compile-size estimate/emitted-length cap, 1000 count bound, 256-entry cache per engine instance).
5. **`xf` grammar edition** (strict XF 1.0 2007 vs. approving `.` excluding CR and `(?:...)`).

---

### Verdict

**APPROVE**
