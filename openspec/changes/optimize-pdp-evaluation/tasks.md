Beads: epic `policy4j-cwtu`; task X.Y is issue `<epic>.X.Y`.
Gate (default for code tasks): `JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true clean test`
(the evidence names the Maven binary that ran it).

## 1. No random numbers on the decide path

- [x] 1.1 Write the failing tests: `CorrelationIdTest` in `xacml-pdp` and the rule test of `java-no-random-on-decide-path`; check: both fail before 1.2 (the rule test passes; `ast-grep scan` reports the two `Random` fields); evidence: `evidence/tests/001-correlation-red`
- [x] 1.2 Implement D1 to D4 in `PolicyDecisionPointContextFactory`, `DefaultPolicyDecisionPoint`, `MDCSupport`, and `tools/ast-grep/rules/`; check: `CorrelationIdTest`, `ast-grep test`, `ast-grep scan`
- [x] 1.3 Gate: code review by two lanes from other families (`evidence/reviews/code/`); full reactor `clean test` and conformance counts unchanged (`evidence/tests/`); JMH before/after of the Benchmark Plan, after `add-pdp-jmh-benchmarks` task 3.5 (`evidence/benchmarks/jmh/`); docs and spec consistency; done: reviews rv001 to rv004, `evidence/tests/003-reactor-after-group1`, add-pdp-jmh-benchmarks `evidence/tests/016-gate-reactor`, JMH pair `evidence/benchmarks/jmh/001-group1-before`, `002-group1-after` (no measurable change)
- [x] 1.4 JMH pair with `-t 4` (design Benchmark Plan; the gate 1.3 pair ran `-t 1` only): before jar from the base sources of the three group 1 files, after jar from the tree, JDK 25, back to back; evidence `evidence/benchmarks/jmh/`; done: `evidence/benchmarks/jmh/007-group1-t4-before`, `008-group1-t4-after` (no claim)

## 2. First profiling round

- [ ] 2.1 Profile `PdpDecideBenchmark` on `rsa2008` and `synthetic:1000` (`in-memory` store, `-t 1`) with `-prof gc` and async-profiler CPU and allocation flame graphs; record the runs and add them to the ledger; evidence: `evidence/benchmarks/jmh/`
- [x] 2.2 Write the hot spots and allocation sites with their share of time and bytes into `research/README.md`; add one task group per hot spot to this file and one decision per group to `design.md` (hypothesis, files, expected gain, test first); `openspec validate optimize-pdp-evaluation --strict`; done: `research/synthesis.md`, design D5 to D12, groups 4 to 11
- [ ] 2.3 Gate: plan review of the new groups by a lane from another family (`evidence/reviews/plan/`)

## 4. Cycle A: one bag per request attribute and decision (D5)

Close out (group 3) keeps its number for the beads ids and runs last.

- [x] 4.1 Write failing tests: two resolves of one request designator in one decision call `RequestContextCallback.getEntity` once and return the same bag; PIP results and `getResolvedDesignators()` unchanged; MustBePresent and missing-attribute behavior unchanged
- [x] 4.4 Review RVP additions: same-key race with a barrier, an information point calling back, and a multiple-decision request through the context factory (spec scenarios added after review); done: `evidence/tests/004-cycleA-red-green` (red: one failure); done: `evidence/tests/006-cycleA-review-tests`
- [x] 4.2 Implement D5 in `DefaultEvaluationContextHandler`; check: the tests of 4.1, `xacml-pdp` tests; done: `evidence/tests/004-cycleA-red-green` (8,220 tests green)
- [x] 4.3 Gate: JMH pair (D12), reactor and conformance, review by another family; done: JMH `005`/`006`; reactor `tests/011`, `017`; reviews rv006/rv007 APPROVE (A), final rv011/rv012 APPROVE

## 5. Cycle B: loops on the attribute lookup path (D6)

- [x] 5.1 Characterization tests of `getEntity`, `findByIssuer`, `findValues`, `getValuesByType` (null and non-null issuer, several types, duplicates, order); done: `evidence/tests/005-cycleB-characterization-green` (8 of 8 before)
- [x] 5.2 Implement D6; check: the tests of 5.1, `xacml-core` tests; done: `evidence/tests/005-cycleB-characterization-green` (8,228 tests green)
- [x] 5.3 Gate: JMH pair, reactor and conformance, review; done: JMH `006`/`009`; reactor `tests/011`, `017`; reviews rv006-rv012, final APPROVE (B; serialization fix applied)

## 6. Cycle C: bags without the Guava multiset (D7)

- [x] 6.1 Characterization tests of `BagOfValues` (empty, singleton, interleaved duplicates and their iteration order, equality and hash, bag and set functions with NaN and signed zero, `values()` multiset behavior, serialization round trip) and of `AttributeReference` absent and present paths; a `Match` case where an extension predicate fails before a later value matches, and a target with Indeterminate followed by No-match (today's results kept); done: `evidence/tests/007-cycleC-characterization`
- [x] 6.2 Plan review of D7 as decided on 2026-10-27 (type-created storage, primitive storage for integer, double, boolean, predicate fast paths, serialized form kept) by another family; batch 3; done: `evidence/reviews/plan/rvc7-cycleC-agy`
- [x] 6.3 Tests: golden serialized streams in both directions (string, integer, double with `0.0`, `-0.0`, NaN, boolean `[false, true, false]` and `[true, false, false]`), written by the current class before any change; iteration order, counts, `value()`, `single()`, equality, hash (equal to today's without calling `values()`), and set operations of every storage against today's multiset; fast-path predicates against the generic loop, also with NaN, signed zero, validation on and off, a custom invocation with a standard id, and failing predicates (`policy4j-0972`); no allocation for a primitive bag matched by a built-in predicate or read by `one-and-only`; done: `BagStorageCompatibilityTest` (golden streams both ways, order, equality, hash), `MatchStorageFastPathTest`; no allocation test (maintainer rule)
- [x] 6.4 Implement the storage of D7: the `ValueType` default method, object, `long`, `double`, and `boolean` storages, `BagOfValues` on the storage with today's equality, hash, and serialized form; check: 6.1 and 6.3, `EmptyBagCharacterizationTest`, `DoubleBagSetEqualityTest`, `BagOfValuesTest`; done: `BagStorage`, `BagStorages`, `ValueType` default, `BagOfValues` on storage (Codex Terra); worktree run: core 6,619, reactor without JMH 8,151, conformance 938/12
- [x] 6.5 Wire the callers: `Match` fast path for built-in predicates (provenance as in D15), `BagFunctions`, `SetFunctions`, `HigherOrderFunctions`, `DecidingOperands`, `BaseDecisionRuleResponseExpression`, `Quantified*` off `values()`; check: function tests, conformance; done: `Match` fast path via `FunctionSpecBuilder.matchBuiltIn`, bag, set, higher-order, `Quantified*`, response expressions off `values()`; merged into the main tree
- [x] 6.6 Gate: review by another family, reactor and conformance; JMH with batch 3; done: review Claude Fable APPROVE WITH CHANGES, rework by Codex Terra merged; reactor `tests/037-reactor-batch2-D21-D23`; JMH with the batch-2 pair (18.1)

## 7. Cycle D: fixed-arity predicate calls (D8)

- [x] 7.1 Tests: fixed, optional, and variadic functions, custom providers, a fixed-arity lazy custom function whose second operand throws and must stay unevaluated, argument order, validation mode, status codes of failures; done: `evidence/tests/008-cycleD-characterization-green`
- [x] 7.2 Implement D8; check: the tests of 7.1; done: `evidence/tests/008-cycleD-characterization-green`, `011-reactor-cycles-DEF`
- [x] 7.3 Gate: JMH pair, reactor and conformance, review; done: JMH `010`/`011`, final tree `017`/`018` (no claim); reactor `tests/017`; final reviews rv011 APPROVE, rv012 APPROVE WITH CHANGES (applied)

## 8. Cycle E: the fixed cost of a decision (D9)

- [x] 8.1 Tests: multiple-decision handler outcomes for repeated categories and attribute-value checks, response attributes, resolved attributes, `ReturnPolicyIdList` order and duplicates, nested obligation and advice staging, TTL minimum, concurrent first writes and callbacks in one decision, concurrent separate decisions; done: `evidence/tests/009-cycleE-characterization-green`
- [x] 8.2 Implement D9; check: the tests of 8.1, reactor; done: `evidence/tests/009-cycleE-characterization-green`, `011-reactor-cycles-DEF`
- [x] 8.3 Gate: JMH pair (including `-t 4`), reactor and conformance, review; done: JMH `011`/`012`, `-t 4` `014`/`015`, final tree `017`/`018`; reactor `tests/017`; final reviews APPROVE WITH CHANGES (applied)

## 9. Cycle F: policy-level allocation (D10)

- [x] 9.1 Tests: variables, references with versions, cyclic references, nested policy sets, repository changes during decisions, reference cache invalidation; done: `evidence/tests/010-cycleF-characterization-green`
- [x] 9.2 Implement D10; check: the tests of 9.1; done: `evidence/tests/010-cycleF-characterization-green`, `011-reactor-cycles-DEF`
- [x] 9.3 Gate: JMH pair, reactor and conformance, review; done: JMH `012`/`013`, final tree `017`/`018`; reactor `tests/017`; final reviews APPROVE

## 10. Cycle G: compact object headers (D11)

- [ ] 10.1 JMH pair with the flag off and on; docs recommendation if it helps

## 11. Second profiling round

- [ ] 11.1 Profile again after cycles A to F (same set as run 004); swarm analysis; new groups or a research swarm (proposal stop rule)

## 12. Cycle H: empty bags (D14)

- [x] 12.1 Tests: empty bag type, equality, hash, serialization, the `DynamicType` failure, the result of `AttributeReference.evaluate` with and without the attribute and `MustBePresent` (characterization); one shared empty bag per type (failing first); done: `evidence/tests/018-cycleH-red-green`
- [x] 12.2 Implement D14; check: the tests of 12.1, reactor; done: `evidence/tests/018-cycleH-red-green`, `019-reactor-cycleH`
- [x] 12.3 Gate: JMH pair, reactor and conformance, review by another family; done: JMH pair `benchmarks/jmh/019-cycleH-before`, `020-cycleH-after` (round 1; round 2 is measured with batch 2, D12 as changed), reviews `reviews/code/rvh1-cycleH-sol`, `rvh2-cycleH-recheck-sol`, `tests/020-cycleH-round2`, `022-rvh2-hash-fix`, `023-commit-tree-cycleH`

## 13. Cycle I: direct two-argument calls (D15)

- [x] 13.1 Tests: for every bound built-in predicate the direct and the reflective path give the same result; operand order; an `EvaluationException`, a `RuntimeException`, and an `Error` from the target keep status and root cause; a wrong argument type; a custom factory and a custom spec with a standard id are not bound; validation on and off; concurrent calls (characterization first, then a failing allocation-free test); done: `evidence/tests/024-cycleI-red`
- [x] 13.2 Implement D15; check: 13.1, function and conformance tests; done: `evidence/tests/024-cycleI-red`, `025-cycleI-green`
- [x] 13.3 Gate: review by another family; reactor and conformance; done: reviews `rvi1-cycleI-sol`, `rvi2-cycleI-recheck-sol`, `rvi3-cycleI-recheck-sol`; `tests/026-rvi1-fixes`, `027-rvi2-fix`, `028-rvi3-fix`; JMH with batch 2 (18.1)

## 14. Cycle J: index loops on the policy tree (D16)

- [x] 14.1 Tests: target, AnyOf, AllOf truth tables and short-circuit; combining algorithms with an `ArrayList` and a `LinkedList`; normalization with `RandomAccess` and other lists; done: `evidence/tests/029-cycleJ-characterization`
- [x] 14.2 Implement D16; check: 14.1, reactor; done: `evidence/tests/030-cycleJ-green`
- [x] 14.3 Gate: review by another family; reactor and conformance; done: reviews `rcj1-cycleJ-sol`, `rcj2-cycleJ-recheck-sol`, `rcj3-cycleJ-recheck-sol` (APPROVE); `tests/031-rcj1-fix`, `032-rcj2-fix`; JMH with batch 2 (18.1)

## 15. Cycle K: context queries in one loop (D17)

- [x] 15.1 Tests: which level answers each query in nested policy sets and references; a foreign context in the chain; staging; one `Optional` instance per attribute per decision; done: `ContextChainQueryCharacterizationTest` (4, green before), handler test red (`tests/033-cycleK-red`)
- [x] 15.2 Implement D17; check: 15.1, cycle E and F tests, conformance; done: `tests/034-cycleK-green` (core 6,582, PDP 218), reactor in 15.3
- [x] 15.3 Gate: review by another family; reactor and conformance; done: reviews `rck1-cycleK-sol`, `rck2-cycleK-recheck-sol` (APPROVE WITH CHANGES, P3 applied); reactor `tests/035-rck1-fix`; JMH with batch 2 (18.1)

## 16. Cycle L: guarded debug logging (D18)

- [x] 16.1 Tests: none; log output is not pinned by tests (maintainer, 2026-10-27); the logging test written earlier was removed
- [x] 16.2 Implement D18; check: 16.1, reactor; done: D18 as built (design); Claude Opus and Fable
- [x] 16.3 Gate: review by another family; done: review Codex Sol (P2 was the lane split, P3 obsolete with the deleted test)

## 17. Cycle M: lazy obligation and advice staging (D19)

- [x] 17.1 Tests: published obligations and advice and their order for nested rules, policies, and policy sets (cycle E characterization), with and without applicable expressions; done: no new test; existing staging characterization and conformance pin it; reference tests now verify no empty calls
- [x] 17.2 Implement D19; check: 17.1, conformance; done: Claude Fable; worktree run: core 6,586, conformance 938/12
- [x] 17.3 Gate: review by another family; reactor and conformance; done: review Codex Astra APPROVE WITH CHANGES (P3 list capacity, applied)

## 19. Cycle N: cheap exceptions on the function error path (D20)

- [x] 19.1 Tests: for one-and-only on an empty and a two-value bag, a function that throws an `EvaluationException`, a `RuntimeException` with a cause chain, and an `Error`: decision, status code, status message, and class, message, and cause of `Status.getError()` as before; no stack trace by default and a full one with `xacml4j.exceptions.stackTrace=true`; serialized form of `CoreException` unchanged; done: `FunctionErrorCompatibilityTest` (status code, message, error chain); no stack-trace or UID test (maintainer rule)
- [x] 19.2 Implement D20; check: 19.1, function tests, conformance statuses; done: (c) only, see D20 as built; (a) and (d) await a maintainer decision
- [x] 19.3 Gate: review by another family; reactor and conformance; done: review Claude Opus APPROVE WITH CHANGES (applied)

## 18. Batch 2 measurement and third profiling round

- [ ] 18.1 One JMH pair: the commit before batch 2 against batch 2 (D12 as changed on 2026-10-27), no agent session alive; ledger
- [ ] 18.2 Profile the batch and run the third profiling swarm; record its reports in `research/`
- [ ] 18.3 Commit batch 2 locally on the verified tree

Batch 3 (design D20 (d), D21 to D25; decisions of 2026-10-27). Tests only where they pin
decisions, statuses, order, serialized form, or public API; no allocation, log, UID, or
mechanism tests. Gate per group: review by another model family; reactor and conformance.

## 19b. D20 (d): a wrong bag size without a stack trace

- [x] 19.4 Implement D20 (d) in `BagFunctions` (package-private stackless `IllegalArgumentException` subclass, same message); check: bag function tests, conformance; done: Claude Opus (`BagFunctions.BagSizeException`); core and PDP tests green on the main tree
- [x] 19.5 Gate: review by another family; done: review `rd22t-D22-terra`; reactor `tests/038-reactor-batch3-ABCD`

## 20. Cycle O: canonical designator keys (D21)

- [x] 20.1 Tests only for uncovered key contract: builder against constructor, equality and separation by category, type, id, issuer, category-less keys; values and hash, not pool identity; done: `AttributeDesignatorKeyTest`, `AttributeReferenceKeyInternerTest`
- [x] 20.2 Implement D21 in `AttributeDesignatorKey`; done: Codex Terra; simplified to Guava `Interners.newWeakInterner()` at the maintainer's request; SPI per the D21 amendment in progress
- [x] 20.3 Gate: review by another family; reactor and conformance; done: reviews `rd21f-D21-fable`, `rso-D21-spi-opus` (applied); reactor `tests/038-reactor-batch3-ABCD`

## 21. Cycle P: results and structural hashes (D22)

- [x] 21.1 Tests only for uncovered behavior: builder reuse, advice and obligation order and merge, hash values against the pre-change formulas (one-element and superclass seeds), status lookup with equal but distinct rules; done: `ResultTest` +4, `RuleTest` +2, `PolicyTest`, `PolicySetTest`, `BagFunctionsTest` +1 each (order, merge, builder reuse, hash compatibility, equal-but-distinct rules)
- [x] 21.2 Implement D22, with the shared OK status; done: Claude Opus; core 6,645 and PDP 217 green (offline Maven, main tree)
- [x] 21.3 Gate: review by another family; reactor and conformance; done: review `rd22t-D22-terra` (P3s applied); reactor `tests/038-reactor-batch3-ABCD`

## 22. Cycle Q: one-operand bindings and calls without reflection (D23)

- [x] 22.1 Tests: every bound one-parameter built-in gives the same result or the same failure (class, status code, message) through `invoke(context, operand)` as through `invokeWithList`, validation off and on; a custom `FunctionSpec` still receives `Apply`'s one- and two-operand calls on its varargs method; a custom factory and a custom spec with a standard id keep their invocation; done: `DirectPairCallTest`, `ApplyTest` (Claude Opus)
- [x] 22.2 Implement D23 (a) and (b); done: Claude Opus
- [x] 22.3 Implement D23 (c); done: Claude Opus; reactor `tests/037-reactor-batch2-D21-D23`
- [x] 22.4 Gate: review by another family (exception mapping, provenance, custom routes); reactor and conformance; done: review `rd23s-D23-sol` (fix applied); reactor `tests/038-reactor-batch3-ABCD`

## 23. Cycle R: restricted target index (D24)

- [ ] 23.1 Tests: `TargetIndexDifferentialTest` (every XACML 3.0 and 2.0 conformance case and every rsa2008 request through two PDPs over independent equivalent repositories and PIPs, index on and off; `Result.equals` plus the status error chain and the order of obligations, advice, policy ids, and attribute assignments); `TargetIndexTest` for what conformance lacks, each comparing indexed and linear responses: a `first-applicable` set whose first child decides while a later indexed key is only in the PIP; a PIP failing once then succeeding for the key; a multi-valued request bag under `first-applicable` and `deny-overrides` with obligations; an empty bag and a `MustBePresent="true"` child; a reference unresolvable at build and resolvable later, and the reverse; a version replaced inside a reference's range; `only-one-applicable` with zero, one, and two matching positions (two of them the same reference object); a `Target` or `Match` subclass, a custom provider under the `string-equal` id, a custom algorithm under a built-in id; runtime validation on; a foreign root context
- [ ] 23.2 Implement the registry (`PreparedPolicySets`, immutable snapshots, the single ordered listener, the generation contract) with D25's `BoundReference` entries; the step D25 depends on
- [ ] 23.3 Implement the index for policy-set children, `targetIndex()`, and the public plumbing (`PreparedPolicySets.create()`, `invalidate()`, the `RootEvaluationContext` constructor)
- [ ] 23.4 Implement the rule step in `Policy.combineDecisions`; add the index on/off parameter to `PdpDecideBenchmark`
- [ ] 23.5 Gate: review by another family; reactor and conformance

## 24. Cycle S: bound reference resolution (D25)

- [ ] 24.1 Tests only for uncovered behavior, full responses compared: two PDPs over two repositories sharing one root object decide differently; a policy replaced by a new version inside a reference's range changes the decision after the repository event; missing and cyclic reference statuses as today; reuse `NestedResponseStagingPdpTest` for response order; replace `removalDuringALookupLeavesTheLookedUpPolicyCached` with a test that the next resolution after a removal during a lookup asks the repository again
- [ ] 24.2 Implement D25 as revised (prepared lists and `BoundReference` in D24's registry, resolver generation and race fix); depends on 23.2; plan review `PR-fable` applied
- [ ] 24.3 Gate: review by another family; reactor and conformance

## 25. Batch 3 measurement

- [ ] 25.1 One JMH pair, batch 2 commit against batch 3, plus the D24 on/off parameter; ledger
- [ ] 25.2 Profile and run the next research swarm; commit batch 3 locally on the verified tree

## 3. Close out

- [ ] 3.1 `openspec validate optimize-pdp-evaluation --strict`
- [ ] 3.2 Audit requirement → task → test → evidence in `evidence/README.md`, and the final ledger against the stop rule of the proposal
- [ ] 3.3 Close the beads epic and run `br sync --flush-only`
