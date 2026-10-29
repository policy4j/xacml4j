Beads: epic `policy4j-eufz`; task X.Y is issue `<epic>.X.Y`.
Gate (default for code tasks): `PATH="<maven-3.9.16>/bin:$PATH" JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -Dlicense.skipUpdateLicense=true clean test`
JMH rule: every implementation group records a before/after pair; lanes stay idle during JMH runs.

## 1. Plan

- [x] 1.1 Discovery swarm (six NTM lanes, read-only) answers research Q1 to Q5; reports in `research/`, `research/synthesis.md`; done: `research/rx1-*.md` to `rx6-*.md`, `research/synthesis.md`
- [x] 1.2 Plan review by lanes from other families than the author; resolve findings in the artifacts; check: `openspec validate add-regex-engine-spi --strict`; evidence: `evidence/reviews/plan/`; done: `evidence/reviews/plan/rv001`-`rv004` (REJECT, REJECT, then APPROVE WITH CHANGES applied, APPROVE)
- [x] 1.3 Maintainer decisions on design Open Questions 1 to 5 and the **BREAKING** items (D5 statuses and messages, D6 cache property); record them in the proposal; done: proposal "Maintainer Decisions (2026-10-28)"

## 2. Characterization and baseline

- [x] 2.1 `RegExpCharacterizationTest`: today's results, statuses, and messages for the corpus (conformance patterns, RX1 table rows, the drift cases, `RegExpLimitsTest` limit cases, an injected engine `Error`); check: passes on today's code; done (Claude Fable): `RegExpCharacterizationTest`, 116 rows, passed on the code before the change; `evidence/implementation/rx-spi-fable.md`
- [x] 2.2 `RegexFunctionBenchmark` for today's functions (compile, match, and full calls) and the regex corpus of `PdpDecideBenchmark`, with `RegexBenchmarkTest`; check: `RegexBenchmarkTest`, `BenchmarkSmokeTest`; done (Claude Opus): `xacml-jmh` `RegexFunctionBenchmark` (compile, match, call, callMiss over 1,024 patterns) and `rules:<n>` requests regex-targets, regex-conditions, regex-any-of-all, regex-malformed; JMH 225; `evidence/implementation/rx-bench-opus.md`; review `evidence/reviews/code/r-RXBENCH-argon/report.md` (part 2) APPROVE
- [ ] 2.3 Baseline JMH run (`evidence/benchmarks/jmh/001-regex-baseline`, ledger rows)

## 3. SPI, profiles, and the jdk engine

- [x] 3.1 Failing tests: `RegexEngineContractTest` (`jdk`/`xf` rows), `RegexProfileDifferenceTest`, `RegexStatusMatrixTest`, `XacmlPatternTest` (grammar, flat classes, size estimate); done (Claude Fable): `RegexEngineContractTest` (69 rows x profiles), `RegexProfileDifferenceTest` (17), `RegexStatusMatrixTest` (15 D5 rows listed), `XacmlPatternTest` (147), `LegacySyntaxTest`
- [x] 3.2 SPI (D1), `XacmlPattern`, `jdk` engine with `legacy` (today's translation and limits) and `xf`, compatible entry points and properties (D7); check: the tests of 3.1 and 2.1; done (Claude Fable): the D1 SPI in `org.xacml4j.v30.policy.function.regex`, `XacmlPattern` (strict XF 1.0), the `jdk` engine with `legacy` and `xf` (count bound 1000, program-size cap 100,000), the D5 statuses, the D7 entry points; merged into the main tree; reactor green on main (core 7,194, PDP 700, conformance module 1,818, JMH 225); review R-RXSPI-sol REJECT (`evidence/reviews/code/r-RXSPI-sol/report.md`): six P2 (recursive `computeIfAbsent` in `UnicodeSets`, empty groups pass the size estimate, back-references to a group that did not take part, legacy input-limit order, a compilation stack overflow reported as malformed, Unicode name and block-name deviations from strict XF) and one P3 (constant-only tests); fixes FIX-RXSPI (Claude Fable) in progress
- [ ] 3.3 Gate: review, reactor and conformance (`legacy` and `xf`), JMH pair against 2.3

## 4. Provider wiring and selection

- [x] 4.1 Failing `RegexEngineSelectionTest` (unknown engine and profile, duplicate names, two repositories, `PolicyImportTool` path); done (Claude Fable, RX-WIRE): 9 tests, written before the code
- [x] 4.2 Instance-based regexp provider and selection as decided (D3); the repository's `FunctionProvider` reaches `PolicyImportTool`, the conformance runner, and `PdpFixture`; check: the tests of 4.1 and 2.1; done (Claude Fable, RX-WIRE): `RegExpFunctions.Matching` per provider, `withRegexEngine` on `FunctionProviderBuilder` and `PolicyRepository.Builder`, the properties read when a provider is built, `PolicyImportTool` imports with the repository's functions; merged into the main tree (the conductor merged `Match.java` by hand over cycle AV); reactor green on main (core 7,220, PDP 731, conformance 1,818, JMH 225); review R-RXWIRE-sol (groups 4 and 5) REJECT (`evidence/reviews/code/r-RXWIRE-sol/report.md`): six P2 (the binder bypasses custom factories and the trust check, a bound call accepts types the method rejects, runtime validation skips the call-site outcome, two cold callers can both compile after eviction, provider limits are a class-initialization snapshot, the `jdk` default skips the duplicate-name check) and one P3 (eager defaults, `ExceptionInInitializerError` in `MarshallingProvider`); Claude Fable ran out of usage, so Codex Terra fixed both reviews in FIX-RX: lazy word-set holder, XML 1.0 Second Edition name ranges and canonical Appendix F block names, group and repetition overhead in the estimate, `UNSUPPORTED` for a back-reference to a group that may not take part and for class back-references, lengths checked before compilation, the JDK's wrapped compilation stack overflow a transient limit, XF tables prepared outside the deadline, binding only for trusted invocations with the operand type checked, the call-site outcome kept under runtime validation and synchronized cold retention, provider limits read when built, the default resolved through `RegexEngines`, defaults selected at build and `MarshallingProvider`'s system functions lazy, `LiteralBinder` documented; the constant-only test removed and the difference matrix derived from the contract table; merged; lane reactor green (core 7,263, PDP 749), conformance with `xf` green; review R-FIXRX-argon APPROVE (all fourteen findings fixed; two P3: a back-reference in a character class is checked for grammar before it is reported unsupported, and `LiteralBinder` and `XacmlPattern.prepare()` can be narrower), the P3 fixes FIX-RXP3 (Codex Astra: class back-references validated before unsupported, `LiteralBinder` package-private in `function.impl`, `XacmlPattern.prepare()` replaced by static initialization) merged; review APPROVE in `../optimize-pdp-evaluation/evidence/reviews/code/r-D48-argon/report.md`, section 2
- [ ] 4.3 Gate: review, reactor and conformance; JMH: not applicable unless the decide path changes (record the reason)

## 5. Compile once

- [x] 5.1 Failing `RegexCompileOnceTest` (literal target, `Apply`, IIC168 higher-order path, deterministic failure kept, transient failure retried, concurrent misses, eviction); done (Claude Fable, RX-WIRE): 7 tests
- [x] 5.2 Call-site binding and bounded cache (D6); check: the tests of 5.1 and 2.1; done (Claude Fable, RX-WIRE): `FunctionSpec.bind`, `BoundCall`, `LiteralBinder`, a pattern bound at its `Match` or `Apply` call site and compiled on first evaluation, a Guava cache of 256 per `Matching`, deterministic failures kept, `cacheRegExps` ignored with a warning; one-fork smoke under load: `rules:100` regex requests -54 to -93% time and -75 to -90% B/op, `call` hits 1,361 to 208 B/op, `callMiss` +35% time at 16 characters (accepted by R-RXWIRE-sol); review R-RXWIRE-sol REJECT, see 4.2
- [ ] 5.3 Gate: review, reactor and conformance, JMH pair (function benchmark and regex corpus; PDP results are warmed decisions, compilation cost from the function benchmark)

## 6. The re2j engine

- [ ] 6.1 Failing `Re2jEngineTest` and the `re2j` rows of `RegexEngineContractTest` (class edge cases, rejections, Unicode after 6.0, size bound before native compilation)
- [ ] 6.2 Module `xacml-regex-re2j` with RE2/J 1.8 (D8) and the emitter (D4); check: the tests of 6.1; blocked 2026-10-28: `com.google.re2j:re2j` is in no local Maven repository (neither 1.7 nor 1.8), and the corporate mirror is unreachable off VPN; a direct Maven Central download was refused earlier as a mirror bypass, so the artifact needs the maintainer
- [ ] 6.3 Conformance suites with `re2j`/`xf` selected; every regexp case passes or is listed with its reason
- [ ] 6.4 Gate: review, reactor and conformance, JMH pair per engine, including the wrapped and unwrapped deadline cases of `re2j`

## 7. Close out

- [ ] 7.1 Docs: engines, profiles, selection, limits, statuses, the `cacheRegExps` property
- [ ] 7.2 `openspec validate add-regex-engine-spi --strict`
- [ ] 7.3 Audit requirement → task → test → evidence in `evidence/README.md`
- [ ] 7.4 Close the beads epic and run `br sync --flush-only`
