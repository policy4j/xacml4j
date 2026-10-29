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

- [x] 18.1 One JMH pair: the commit before batch 2 against batch 2 (D12 as changed on 2026-10-27), no agent session alive; ledger; done: `benchmarks/jmh/022-batch2-before`, `023-batch2-after` (rsa2008 -21% time, -31% bytes; synthetic:1000 full scan -26%, -65%)
- [x] 18.2 Profile the batch and run the third profiling swarm; record its reports in `research/`; done: profile `benchmarks/jmh/026-profile-decide-batch3` (after batch 3 part); swarm analysis in design D29 and the D24/D25 motivation
- [x] 18.3 Commit batch 2 locally on the verified tree; done: commit `87133237` (batch 2 and batch 3 part, with tests/038)

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

## 23. Cycles R and S: prepared policy sets, the restricted target index, and the resolution snapshot (D24/D25, round 5)

- [x] 23.1 Tests (Codex Terra: `PreparedPolicySetsPdpTest`, 81 cases; (f) and (g) with 23.4).
  - **Rules:** response level only. Compare full responses: `Result.equals`, plus the status error chain and the
    order of obligations, advice, policy ids, and attribute assignments. No count assertions.
  - **Baseline:** the same PDP built through the factory's package-private constructor without a registry (the
    unprepared, linear path). Run with the index on and off.
  - **Fixtures:**
    - `InMemoryPolicyRepository`, over a direct executor and over a two-thread executor.
    - The in-decision hook: a resolver referenced by a child's condition.
  - **(a) Unvisited failing child.** For every built-in policy-combining algorithm, a position the algorithm does not
    visit holds a reference whose lookup throws a `RuntimeException`, throws an `AssertionError`, throws a
    `PolicyResolutionException`, or returns no policy. The response equals the baseline, and nothing escapes. Cases:
    - deny-overrides, permit-overrides, and their ordered variants;
    - deny-unless-permit and permit-unless-deny;
    - first-applicable;
    - only-one-applicable, after an Indeterminate target, and after two matches;
    - on-permit-apply-second: position 0 Permit with the failing child at 2; position 0 Deny with the failing child
      at 1;
    - the legacy deny-overrides and permit-overrides.
  - **(b) Visited failure.**
    - A `RuntimeException` and an `Error` escape with the same class, message, and cause as in the baseline.
    - A visited miss and a visited non-matching target give the baseline's Indeterminate with the same status.
  - **(c) Mismatch.**
    - A repository returns a non-matching target on the first lookup and a matching one after. Decision 1 equals the
      baseline (Indeterminate); decision 2, with no event, is Permit.
    - `DefaultPolicyReferenceResolver` with its cache on asks again after a mismatch.
  - **(d) Snapshot per decision.** Explicit expected responses; the baseline is live and differs here by design. The
    hook changes the repository during the decision:
    - one reference at two positions answers once;
    - an outer pinned reference and a cold nested set's reference to the same policy answer one target, so the
      response holds one set of obligations;
    - a deferred position whose first lookup failed replays the failure in the extended-Indeterminate pass, and the
      next decision asks again;
    - the next decision sees the new state.
  - **(e) Wiring.** After `policyAdded`, `policyRemoved`, and the policy-set events, on both executors, the next
    decision equals a fresh PDP's. The same holds after an event during a decision.
  - **(f)** Dispatch: round 4's (f).
  - **(g)** The index: round 4's (g), with runtime validation on giving no index.
  - **(h) A pin from another decision (PR5 P1-1).** A repository returns no policy on the first lookup of a
    reference and a matching policy after, with no event. Decision 1 records the miss in one set. A hook in it runs
    decision 2, which prepares a second set over the same reference and pins it. Decision 1 then evaluates the
    second set and gets the same Indeterminate as for the first set, with the index off and with the index on (its
    request does not match the pinned target).
  - **Resolver tests:** keep 73, 95, 106, and 137. The resolver's caches keep their mechanism; only the matching rule
    is new.
- [x] 23.2 Implement the snapshot: `Generation.targets`, the root's `misses`, the order of `resolve`, `outcome`, and
  the root constructor. Implement the wiring: the listener class and the resolver with its cache off. Implement the
  resolver's matching rule. The conformance and rsa2008 counts must not change.
- [x] 23.3 Implement preparation: `PreparedPolicySets`, `PreparedList`, `PinnedReference`, and `Prepared` in
  `PolicySet` and `Policy`, without the index. Implement dispatch. Counts unchanged.
- [x] 23.4 Implement `TargetIndex`, `targetIndex()`, `create(boolean)`, and the candidate filtering.
- [x] 23.5 The `targetIndex` parameter of `PdpDecideBenchmark`.
- [ ] 23.6 Gate: a plan review of round 5 by another family before 23.2 (done: PR5, Agy Argon, APPROVE WITH CHANGES,
  applied as round 5a); a code review by another family; the reactor and conformance; the batch JMH pair (D12).

## 26. Cycle T: context root and scan parameters (D29)

- [ ] 26.1 Implement D29 (a) and (b); existing context-chain and scan tests pin the behavior (`ContextChainQueryCharacterizationTest`, the external rule context tests, `MatchStorageFastPathTest`); no new test unless a query's answer is uncovered
- [ ] 26.2 Gate: review by another family; reactor and conformance

## 27. Cycle W: resolution state ready for concurrency (D27)

- [x] 27.1 Tests (public outcomes only): overlapping same-key lookups return one bag and compatible statuses; independent dependency chains give no false cycle; a real cycle ends with today's missing or fallback outcome; equal `ResolverCacheKey`s from separate contexts find each other in a hashed collection, two decisions share a primed cache, and a key from a stream written before the fix finds its entry
- [x] 27.2 Implement flights, publication of the winner, ancestry, `ResolverContext` confinement (Claude Fable; after R-D27-sol REJECT: a lookup inside a flight never waits, so no wait graph and no execution seam; stale-miss recheck; selector failure mapped in the shared attempt)
- [x] 27.3 Fix `ResolverCacheKey` (bead `policy4j-3huw`) and the put-only-on-miss in the default PIP
- [x] 27.4 Gate: review by another family; reactor and conformance (R2-D27-sol APPROVE WITH CHANGES, P3 docs fixed; reactor green)

## 28. A PIP-heavy JMH corpus and a configurable fixture (D28)

- [x] 28.1 `pip:N` corpus with its full-response oracle (independent, dependent, shared key, missing, failure, skipped failure, fallback, XML selector)
- [x] 28.2 Fixture PIP, latency, serial and parallel backend reads, bounded in-flight reads, cache off and hot; the additive `PdpFixture.open(...)` overload taking a caller's PIP
- [x] 28.3 Tests only for uncovered decisions, statuses, order, and public API (concurrent decisions, a wrong full response rejected)
- [ ] 28.4 Gate: review by another family; reactor; first measurements (serial against parallel, cache off and on)

## 29. Cycle V: parallel resolver work behind an internal runner (D26, Argon's shape)

- [x] 29.1 Plan review (RPD26, Agy Argon) and maintainer decision: Argon's minimal shape
- [x] 29.2 Implement after D27 (Claude Opus; merged; PDP 240, conformance 938/12): `attribute(id, type, Function)`, `buildParallel()`, the PIP builder settings, the internal runner; tests: the same fallback and empty-bag behavior as synchronous descriptors, a hung candidate does not block fallback past the timeout, one resolver call for two attributes in one decision; gate and measurement with D28

## 30. PIP session and the whole-result-set memo (D27, PR11 rank 1), after D28

- [x] 30.1 Additive `PolicyInformationPoint.forDecision()` and the constrained memo; measured with D28's corpus (Claude Fable; reviews R-FABLE, R-FABLE2, R-FABLE3 closed by FIX-30, FIX-30b, FIX-30c; merged)

## 31. Cycle U: hashing and iteration (D30)

- [x] 31.1 Implement U1 to U3; existing hash, combining, and bag tests pin the values and order; tests only for uncovered hash values
- [ ] 31.2 Gate: review by another family; reactor and conformance

## 32. Cycle X: small bags without a hash map (D31)

- [x] 32.1 Implement X1 and X2; `BagStorageCompatibilityTest` (order, hash, equality, golden streams) pins the behavior; add bags of 8 and 9 values (the threshold) with duplicates
- [ ] 32.2 Gate: review by another family; reactor and conformance

## 33. Cycle Y: per-decision state only when read (D32)

- [x] 33.1 Implement Y1 to Y7 with the maintainer's decisions (Y2 empty collection, Y3 and Y4 additive methods); Y8 (MDC) moved to D34 by the maintainer; tests for the policy id list with and without `ReturnPolicyIdList` and the include-in-result attributes (Claude Fable; merged; core 6,660, PDP 233, conformance 938/12)
- [x] 33.2 Gate: review by another family; reactor and conformance (R-FABLE-sol: cycle Y fixes APPROVE; reactor green)

## 34. Cycle Z: function calls with fewer copies (D33)

- [x] 34.1 Implement Z1 and Z2; existing function, higher-order, and validation tests pin results, statuses, and messages
- [x] 34.2 Gate: review by another family; reactor and conformance (R-YZ-sol: cycle Z APPROVE, no findings; reactor green)

## 36. Observability SPI (D34)

- [x] 36.1 Final API (Claude Opus: D34b-opus, round 3 D34c-opus after Agy Argon's PR-D34): the SPI with attribute and resolver spans, the correlation, the non-generic observation with Java-stack tokens, emission points, the exception order; plan review of round 3 by another family before 36.2
- [x] 36.2 Implement after 23.3 and Terra's extended-Indeterminate fix: `DecisionObserver`, `AttributeSource`, `DecisionObservation` in `org.xacml4j.v30.spi.observe`; `EvaluationContext.getDecisionObservation()`, both `RootEvaluationContext` overloads, descendant forwarding; `PolicyDecisionPointContext.createEvaluationContext(request, observation)` (factory, `Budget`); `PolicyDecisionPointBuilder.observer(...)`; `PolicyDecisionPoint.getCorrelationId(long)`; the emission points of D34, with argument evaluation out of the function wrapper and the type-assertion report; `MDCSupport` and its calls removed; the correlation formatted when read
- [x] 36.3 Tests: the public callback sequence and its tokens over nested sets, a pinned and an unpinned reference, a pass, a condition, a failing function followed by a successful combining outcome, staging, a decision cache hit, the five attribute sources and an attribute failure, resolver spans (fallback, a resolver failure, a resolver cache hit); observer exceptions fail the decision (one absorbed by an engine guard, `decisionFinished` with no `decisionFailed`, a failure callback during an engine failure); the correlation equals the context's id; conformance responses unchanged with a recording observer; `CorrelationIdTest` without MDC; no allocation, log, or count tests
- [ ] 36.4 Gate: review by another family; reactor and conformance; measured with the batch (default observer and an allocation-free observer)

## 37. TTL cache SPI for resolved attributes across decisions (D35)

- [ ] 37.1 Design: SPI shape, Guava, Caffeine, and cache2k implementations with per-entry TTL (verified against current documentation), packaging and dependencies, bounds; maintainer approval of the API and the dependencies
- [ ] 37.2 Plan review by another family
- [ ] 37.3 Implement the SPI and the three implementations; tests for expiry, misses, failures, and absence not cached, and responses unchanged
- [ ] 37.4 Gate: review by another family; reactor and conformance; JMH with D28's `pip` corpus (`pipCache` on, each implementation)

## 38. Fix: the extended-Indeterminate pass

- [x] 38.1 Pass state reaches descendants; no advice, obligations, or evaluated-policy records inside a pass; nested Indeterminate targets still combine their children (Codex Terra; `ExtendedIndeterminatePassPdpTest`; merged; core 6,683, PDP 332, conformance 938/12)
- [ ] 38.2 Gate: review by another family

## 39. Cycle AA: `StagedResponses` only for subtrees that can respond (D36)

- [x] 39.1 Response-capability flag at build time; staging only where a subtree may respond (Codex Terra; `NestedResponseStagingPdpTest`; merged)
- [ ] 39.2 Gate: review by another family (R-TERRA-opus, R-AA-astra, R-AA2-astra APPROVE); measured with the batch

## 40. Decision caching (D37), after D35

- [ ] 40.1 Research: literature and prior art on decision caching in ABAC and XACML engines, TTL from participating attribute sources, and request keys over the attributes a policy reads
- [ ] 40.2 Design: TTL rule, request key over the policy tree's attributes, invalidation; maintainer approval
- [ ] 40.3 Plan review by another family; implement; tests; JMH with D28's corpus

## 41. Batch 6 cycles (D38 to D41)

- [x] 41.1 Cycle AF (D38): small bags and attribute bags built once per entity (Claude Fable; bag storage approved in R-FABLE2; the index memo fixed in FIX-30b and approved in R-FABLE3)
- [x] 41.2 Cycle AG (D39): function dispatch without per-call allocation; after D34
- [x] 41.3 Cycle AH (D40): per-decision handler and root trim (Claude Fable; default request-bag table chosen by the conductor; review R-AH-sol APPROVE, no findings)
- [x] 41.4 Cycle AI (D41): target before context; after D34, D36, and 23.4
- [x] 41.6 Cycle AJ (D42): PIP-path allocation, pr13 proposals 1, 2, 4 (Codex Astra; merged; PDP 462; pip:8 smoke -16% B/op cache off, -4% cache on; review R-AJ-argon APPROVE, no findings)
- [x] 41.7 Cycle AK (D43): Category on the memo; compact object headers measured and documented (Claude Fable; core 6,745)
- [x] 41.8 Cycle AL (D44): dependency maps and resolver keys without staging, pr13 proposal 3 (Claude Fable; review R-AL-sol APPROVE, no findings)
- [x] 41.9 Benchmark corpus `rules:N` (Claude Opus): integer and date ranges, repeated conditions, multi-valued bags, narrow and wide requests, responses; exact expected results checked at fixture setup (pr15 workload gaps); JMH 213; review R-CORPUS-argon APPROVE, no findings
- [ ] 41.5 Gates: review by another family per cycle; reactor and conformance; the batch 6 JMH pair
  - FIX-IDX (Codex Terra, R-IDX P2-1 and P3-1): the index request path without per-decision lambdas, iterator, or list for empty, unknown, and one-value bags; merged; PDP 483, conformance module 1,818, JMH 213; synthetic:1000 index-on smoke 896 to 765 B/op; re-review R-IDX2-argon APPROVE, no findings
  - FIX-AI (Claude Fable, R-AI-fable P2-1 and P3-1 to P3-7): the precheck answers MATCH as well as NOMATCH, so a matching child matches its target once; one handler contract; the helper back in `DecisionCombiningAlgorithms`; `FunctionSpec.hasDirectScan` instead of the reflective class; merged; PDP 493, conformance module 1,818, JMH 213; smoke synthetic:100 2,613 to 2,366 B/op, rsa2008 bytes and time within fork spread (the +28.5% time of the AI smoke is gone); review R-AI2-argon APPROVE WITH CHANGES (P2-1 the precheck slot confined to the deciding thread, P3-1 slot cleared in a finally and context validity kept for a prechecked MATCH, P3-2 shorter javadoc), fixes FIX-AI2 (Claude Fable): the root records its deciding thread, the precheck answers nothing on another thread, the slot is cleared in a finally, a prechecked MATCH keeps the context check; merged with cycle AN; review in R-AN-sol

## 42. Batch 7: profiles of the `rules` corpus, then cycles

- [x] 42.1 Profile P-FIXED (Claude Opus): the fixed cost of one decision (`research/pr17-opus-fixed-cost-profile.md`): narrow 4,936 B, of which IncludeInResult copies and scan 61% and the eager `Result` hash 14%; wide 21.8 B per unread attribute (the IncludeInResult scan); rsa2008 3,052 B, of which contexts and staging 36%, the request-bag memo 11%, the PIP session for keys no resolver supplies 10%
  - Cycle AM (Claude Opus): lazy `Result` hash; IncludeInResult attributes recorded at index build; the IncludeInResult category built once per category; `Optional<Entity>` held by the category; allocation-free `AttributeIndex.resolve`; `Result` keeps its IncludeInResult categories as a list and its obligations without staging; merged; reactor green (core 6,753, xml 254, PDP 497, conformance module 1,818, JMH 213); smoke narrow 4,648 to 448 B/op, wide 5,512 to 448 B/op, rsa2008 2,985 to 2,736 B/op; review R-AM-argon APPROVE, no findings
  - Cycle AN (Claude Fable): no request-bag memo in unobserved decisions (observed decisions keep it, so the D34 events are unchanged); `PolicyInformationPoint.hasNoResolver` and `ResolverRegistry.hasNoResolver` (additive SPI, default false, so a test double still gets the attempt) skip the PIP session for a key no resolver supplies; constant obligations and advice built once; staging frames in the root context, created on the first staged response; merged; reactor green (PDP 500, conformance module 1,818, JMH 213); smoke rsa2008 2,974 to 2,167 B/op (-27%), rules:100 responses 17,210 to 8,498 B/op (-51%); review R-AN-sol: FIX-AI2 APPROVE, cycle AN REJECT (P2-1 staging must stay scoped to the context chain, not thread and depth; P2-2 the protected `DecisionRuleResponse.Builder` field restored; P2-3 constant responses only for the exact expression classes); fixes FIX-AN (Claude Fable): `CompositeRuleEvaluationContext` stages only responses that pass through a staging rule's own context, direct root writes are never staged, the `StagedResponses` wrapper stays wherever custom code can run, the builder field back at HEAD, constant responses only for the exact classes; merged (its files only); reactor green; rsa2008 1,660.3 B/op before and after; re-review R-AN2-sol REJECT (P2-2 and P2-3 closed; P2 closing an Indeterminate pass can restore a staging scope that never opened, dropping an enclosing set's Permit responses; P2 a joined worker bypasses an already-open enclosing scope because membership is rechecked on the writing thread); fix FIX-AN2 (Claude Opus): the root records the open staging scopes in a 64-bit record over the innermost scope's chain (close restores the recorded scope; membership independent of the writing thread; opening limited to the deciding thread); merged; reactor green (PDP 702); rsa2008 1,223 to 1,231 B/op (+8), the wrapper-only fallback measured at 1,497 (+22%, about 30% slower); review R-AN3-argon APPROVE, no findings (Sol's two R-AN2 findings closed)
  - Cycle AP (Claude Opus): one context for a policy reference and its target in unobserved decisions of the engine's root, for exact `Policy` and `PolicySet` targets; merged; PDP 501, conformance module 1,818, JMH 213; rsa2008 2,737 to 2,610 B/op (2 forks), synthetic:100 432 to 412 B/op (8 forks); a shared per-policy rule context is designed (sound in unobserved decisions with built-in rule algorithms) but not built: about 47 B on rsa2008 once AO prechecks date rules; finding: a cyclic policy-set reference throws `IllegalStateException` from `decide` (pre-existing; violates XACML 3.0 §7.15, which R-AP-argon quotes: a circular reference detected at runtime evaluates to Indeterminate with processing-error; fix FIX-CYCLE, Codex Terra, pending); review R-AP-argon APPROVE, no findings
  - Cycle AQ (Codex Astra): the fixed objects of the request path: multiple decision triggers recorded when the request is built (`RequestContext`), a direct path for an unobserved request without triggers under the exact built-in handler chain (`RequestContextHandlerChain.canHandleSingleRequestDirectly()`, approved by the conductor), a shared PDP configuration context with per-call correlation, `ResponseContext.of(Result)` sharing plain Permit, Deny, and NotApplicable responses; merged; reactor green (core 6,757, PDP 623, conformance module 1,818, JMH 213); smoke narrow 456 to 376 B/op, rsa2008 1,660 to 1,593, synthetic:100 420 to 308; review R-C2AQ-argon APPROVE, no findings
  - D45 (draft Codex Sol, `research/pr20-sol-d45-draft.md`; design approved for drafting by the maintainer 2026-10-28): shared expensive condition subexpressions within one decision: an internal allow-list (string one-and-only, normalize-space, normalize-to-lower-case over exact literals and designators), request-backed positive singleton bags only, success-only memo in the root's existing lazy map, off while observed, zero bytes when nothing qualifies; expected about 17 KB on repeated-conditions (unmeasured); plan review PR-D45-argon APPROVE WITH CHANGES (P1 count occurrences across the policy's rules; P1 expression entries must not trip `hasMisses()`; P1 gate on the deciding thread; P2 a prepared expression spine; P2 a zero-byte policy flag; P3 O(1) built-in provenance), revised (D-SHARE2) and recorded in `design.md` D45; approved by the maintainer 2026-10-28 with the public bridge `FunctionSpecBuilder.isShareableBuiltin`, off while observed, no PIP-backed sharing; built after regex D6 (Argon: regex compilation is about 90% of repeated-conditions bytes today)
- [x] 42.2 Analysis P-COND (Codex Sol; `research/pr19-sol-condition-analysis.md`, source analysis only: the lane is read-only, so no profile): every regex evaluation compiles its pattern (`cacheRegExps` defaults to false; translation and scanning run before the cache); the literal-pattern binding is design D6 of change `add-regex-engine-spi` (2 of 23 tasks done); shared subexpressions need canonical plan identities in the generation and a per-decision memo of successful pure built-ins, a new design decision with purity, retry, and observation contracts; general literal folding finds no eligible call in rsa2008, synthetic, or rules
- [x] 42.3 Profile P-RANGE (Codex Astra; `research/pr18-astra-range-profile.md`): a `Lookup` per request-bag lookup is 88% of integer-ranges bytes (removed by AM's allocation-free `AttributeIndex.resolve`); temporal comparisons have no bag scan, so the precheck cannot reject a date rule and every date rule gets a context (48 to 83% of date-ranges); multi-valued keeps argument lists and variadic arrays of its conditions (52%); range filter (sorted endpoints, two bound tests per AllOf, first AnyOf only) and multi-key filter (alternative identity kept) designed, both need a design decision, deferred until AM and AO are measured
  - Cycle AO (Codex Astra): a bag scan for the date, dateTime, and time comparison functions, with the function's own comparison (date and dateTime keep their fixed UTC default for an absent zone; time needs a zone on both sides) and `NONE` otherwise; merged; 108 outcome cases (`TemporalComparisonTargetPdpTest`), reactor green; smoke on a tree without AM: date-ranges index on 3,841 to 465 B/op, index off 3,857 to 8,522 B/op from per-comparison `Lookup` and `Optional`, which AM removes; review R-AODEC-argon APPROVE, no findings
  - Cycle I-DECLINE (Codex Terra): no index for fewer than two children or without an indexable key (`Policy`, `PolicySet`, `TargetIndex`); merged; reactor green (PDP 485, conformance module 1,818, JMH 213); one-fork smokes too noisy to show a net change; the remaining index-on cost is the root lookup where the index can dispatch; review R-AODEC-argon APPROVE, no findings
- [ ] 42.4 Cycles chosen from 42.1 to 42.3 (a design decision for each one that changes the model or the SPI)
  - FIX-CYCLE (Codex Terra): a circular reference detected at runtime evaluates to Indeterminate with processing-error (XACML 3.0 §7.15), a policy reference entered inside a policy to Indeterminate with syntax-error (§5.14, §7.19.2); evaluating flags on the reference contexts tell a same-instance recursion from the idempotent reuse of a reference's own context; merged; reactor green (core 6,756, PDP 640, conformance module 1,818, JMH 213); review R-CYCBENCH-argon APPROVE WITH CHANGES, redo before the batch measurement (P1-1 a cycle back to the root, through an inline policy set, or across version constraints is caught only on the second back edge: check the resolved target against the active ancestors by identity; P2-1 the evaluating flag grows the merged reference context from 32 to 40 B); fixes FIX-CYCLE2 (Codex Terra): active resolved targets detected by identity on the first back edge, no flag on the merged contexts; merged; re-review R-C2AQ-argon APPROVE WITH CHANGES (its P1-1 is the same integration failure); integration failure on main with FIX-AN (the identity walks do not see a policy behind the `StagedResponses` wrapper: stack overflow and syntax-error instead of processing-error in `ReferenceContextPdpTest`), fix FIX-CYCLE3 (Claude Opus): the walks in `isEvaluating`, `setEvaluating`, and the active-target checks step by `getParent()`, so a `StagedResponses` wrapper no longer hides its delegate; merged; `ReferenceContextPdpTest` 78 with 16 custom-algorithm cases; full reactor on main green (core 6,765, PDP 700, conformance module 1,818, JMH 225); review R-C3AS-argon APPROVE, no findings
  - Cycle AR (Claude Opus): no deep re-hash of the policy tree per Indeterminate rule (RX-BENCH finding 1: the root's status map is keyed by `PolicySet`, `Policy`, and `Rule`, whose hash codes walk the whole tree; 92% of a decision's bytes with one failing rule, 5.35 MB and 5.5 ms with 100): `Rule`, `Policy`, and `PolicySet` cache their D22 hash (single-check idiom), the status map keeps equality keys (public lookup by an equal copy); `regex-malformed` back to n failing rules; merged; reactor green in its lane; smoke 1 failing rule 61 µs and 54 KB to 3.8 µs and 4.4 KB, 100 rules 5.9 ms and 5.3 MB to 0.32 ms and 375 KB, 1,000 rules 530 ms and 543 MB to 3.6 ms and 3.7 MB (quadratic to linear); review R-AR-astra REJECT (P1: the cache assumes stable hash inputs, but `Binary` keeps a caller's `ByteBuffer` and the composite defaults store any object, so an input changed before map insertion breaks the equal-copy status lookup); conductor decision: make those inputs immutable (copy at construction) rather than make the cache conditional; fix FIX-AR (Codex Astra): `Binary` copies a caller's buffer once at construction and refuses `readExternal` on an initialized value; composite defaults accept only exact immutable scalar classes (`XPathVersion`, `String`, boxed primitives, `BigInteger`, `BigDecimal`, `URI`), anything else `IllegalArgumentException`; the cache invariant stated on `Rule`, `Policy`, `PolicySet`; residual pre-existing routes documented (XML content DOM, `PortRange.readExternal`, trusted extensions); merged; rsa2008 bytes unchanged; review R-FIXAR-argon APPROVE, no findings
  - Cycle AS (Codex Terra): built-in function calls without per-call argument lists or variadic arrays (fixed arity, lazy `and`/`or` over the immutable operand list, one- and two-operand higher-order shapes; validation and custom factories keep the old path); merged; reactor green; smoke multi-valued 2,962 to 914 B/op, rsa2008 1,660 to 1,310 B/op; review R-C3AS-argon APPROVE WITH CHANGES (P2 a custom `FunctionSpec` predicate of binary any-of, all-of, all-of-any, any-of-all is called through its two-argument method instead of its variadic one; P3 the `fits` bounds guard and a smaller public helper surface); fix FIX-AS (Codex Terra): custom higher-order predicates use their variadic `invoke` for one and two operands again, built-ins keep the direct path; `fits` bounds-checked; `FunctionReference` helpers reduced to the unary and pair `invoke` overloads; `usesBuiltInInvocation` is the one provenance query for D45; merged; reactor green (core 7,204, PDP 702); rsa2008 1,231 B/op; re-review R-FIXAS-argon APPROVE WITH CHANGES (P2 `invokeFixed` checked `fits` past the parameter count, so methods with fewer than four Java parameters lost their `invokeExact` dispatch and fell back to reflection), fixed by the conductor with the `count > i` guards of `invokeFast`
  - Cycle AT (Codex Terra): value hash codes without temporaries: `ISO8601Date`, `ISO8601DateTime`, `ISO8601Time` compare and hash from epoch seconds and nanoseconds with the same values, cached; the durations cache their provider hash; `Entity`'s hash without a varargs array; merged; reactor green (core 6,769); smoke date-ranges 351 to 300 B/op; review R-AT-argon APPROVE, no findings
- [x] 42.6 Profiles P-RANGE2 (Codex Astra; `research/pr21-astra-range2-profile.md`: range rejection is now CPU, about 80% of date-ranges, allocation near zero; GO for a narrow D46) and P-FIXED2 (Claude Opus; `research/pr22-opus-fixed2-profile.md`: rsa2008 1,231 B/op, 80% of its time in policy evaluation; the response path 36% of its bytes; nine ranked items)
- [ ] 42.7 D46 ordered-range candidates (approved 2026-10-28; Codex Terra; with item 9, `TargetIndex` candidates pre-sized): implementation, review by another family, reactor and conformance, a controlled pair with rule counts below and above 100
  - Implemented (Codex Terra): `RangeTargetIndex` with sorted bound postings and separate minimum and maximum witnesses, the equality plan first, `TargetIndex` candidates pre-sized; merged; reactor green (PDP 714); smoke index on: rules:100 integer-ranges 6.05 to 0.66 µs, date-ranges 7.45 to 0.88 µs, rules:1000 date-ranges 93.0 to 3.67 µs and 1,980 to 761 B/op, rules:10 about +160 B/op, multi-valued 1,031 to 872 B/op; review R-D46-argon APPROVE, no findings; the controlled pair decides
- [ ] 42.8 Cycle AU (Claude Opus): responses as immutable lists from rule to `Result`; the generation `Optional` held once; a smaller root; an engine `Result` without its builder; the policy identifier list; no extended-Indeterminate walk when nothing is recorded (P-FIXED2 items 1, 3a, 3c, 4, 8, C1)
  - Implemented: rule response lists kept per decision for constant responses, staging by reference, one root holder of four immutable lists merged by id by compare-and-set with live views, `Result` lists with one entry per id, the generation `Optional` held once, the root 104 to 88 B, `Result.of` factories, the policy identifier list from a linked record sorted once, no extended-Indeterminate walk when nothing is recorded; obligations and advice now in evaluation order (were hash order); merged; reactor green (core 7,213, PDP 703); three-fork smoke rsa2008 1,231 to 826 B/op, responses 5,057 to 2,737 B/op and -32% time, narrow 384 to 312 B/op and -14% time, synthetic:100 315 to 278 B/op; the shared configuration object (root to 80 B) moved to cycle AX; review R-AU-argon APPROVE, no findings (no order of obligations or advice is normative: §5.32, §5.33, §7.18 define sets); the batch pair decides
- [ ] 42.9 Cycle AV (Codex Astra): the handler from 48 to 24 B; bag scans without iterators; no duplicated validity check in context creation (P-FIXED2 items 3b, 5, C4)
  - Implemented: the handler's resolution state in one lazy holder (handler 48 to 24 B), index loops in `Match` and `BagOfValues.containsAll`, `PolicySet.createContext` without the second current-set query for its own parent; merged; reactor green; three-fork smoke rsa2008 1,231 to 1,179 B/op, narrow, multi-valued, synthetic -24 B/op each; review R-AV-opus APPROVE (the holder +8 B per PIP decision, measured by class histograms, against -24 B per request-only decision; three P3: handler comments shortened, redundant static cache suppliers, C4's time gain near zero), the P3 fixes go to Astra after AX; the batch pair decides
- [ ] 42.10 Cycle AW (Codex Terra): function call shapes computed once, higher-order argument checks once where static, Indeterminate failures without avoidable objects (P-FIXED2 C2 and item 6, function part)
  - Implemented in part (Codex Terra): smoke rsa2008 1,231 to 1,224 B/op, multi-valued 895 to 871 B/op, times within noise. The conductor merged the shared processing-error `StatusCode`, the no-message `FunctionInvocationException` path, and the fast argument range computed when the spec is built. Not merged: the higher-order checked-arguments path (a public `Apply.CheckedArguments`, a shape query coupling three classes, for -24 B/op within noise). Deferred message formatting declined (`Status` and `CoreException` keep eager messages). Review R-AW-argon of the merged part APPROVE, no findings; the batch pair decides
- [ ] 42.12 FIX-TSTATUS (Codex Terra): a failing rule target reports its status (D5 syntax-error, missing-attribute) as a condition does; today `Match` records it per rule only and the rule reports processing-error without a message (found by Claude Fable in RX-WIRE)
  - Implemented: `Match` also writes the decision status for a rule-target failure, the per-rule write kept; `RuleTargetStatusPdpTest` (malformed regex and missing attribute in a rule target, a policy target, a failure inside an extended-Indeterminate pass; observation and the index on and off); merged; conformance: no decision or status code changed, seven cases carry a more detailed status message (IIA007, IIIF002, IIIF004, IIIF005); rsa2008 766.7 B/op unchanged; reactor green on main with AX (core 7,224, PDP 747, conformance 1,818); review R-TSTATUS-argon APPROVE, no findings (a determinate result always reports ok; statuses stay last-write-wins as for conditions, allowed by Annex C)
- [ ] 42.11 Cycle AX (Codex Astra): one shared configuration object per decision point (root 88 to 80 B); non-constant obligations and advice without the multimap builder; `AttributeAssignment` hash without an array (AU's 3c remainder, P-FIXED2 item 7)
  - Implemented: `RootEvaluationContext.Configuration` shared per decision point (root 88 to 80 B; the old constructors kept), `Obligation.of` and `Advice.of` from the evaluated immutable list (the `ImmutableSetMultimap` representation and its order kept), the assignment hash without `Object[4]` (not cached: `Value` is extensible); merged; reactor green on main (core 7,224, PDP 731); three-fork smoke rsa2008 774 to 766 B/op, `responses` 2,723 to 2,368 B/op (P-FIXED2 estimated about 1.5 KB), `narrow` and `synthetic:100` -8 B/op, times within noise; review R-AX-argon APPROVE, no findings; the batch pair decides
- [ ] 42.5 D45 shared condition subexpressions (approved 2026-10-28), after `add-regex-engine-spi` D6: implementation, review by another family, reactor and conformance, JMH pair on `rules:1,100 repeated-conditions`, rsa2008, synthetic:100 and 1000
  - Implemented (Claude Opus, I-D45) as designed, no `Apply.java` edit: `PreparedConditions` (scan, Policy-wide preparation, canonical keys, maximal roots, cloned spine, memo-aware wrapper, gates, request-bag guard), the `Condition` count and `Policy` flag, `Generation.conditions`, the root's snapshot map with `ReferenceMiss.INSTANCE`, the bridge `isShareableBuiltin` and the binding proof; merged; reactor green on main (core 7,242, PDP 781, conformance 1,818); three-fork smoke `rules:100 repeated-conditions` 39,403 to 22,186 B/op index off (22,154 on), 29.3 to 23.3 µs; histograms -17,197 B per decision (99 normalization pairs); rsa2008, synthetic:100 and 1000, rules:1 unchanged; preparation about 245 KB and 365 µs per 100-rule policy per generation; nine mutants killed; review R-D45-sol REJECT: P2 a `Condition` subclass that overrides `getExpression()` is evaluated through the accessor once its Policy qualifies (also when sharing is off), P3 the weak `Policy` key can be retained through its strong value; provenance, keys, selection, spine, gates, memo and storage confirmed; FIX-D45 (Claude Opus) done: subclasses are not counted and every fallback evaluates the private predicate (the new test fails 3 of 4 cases before); the Policy owns its bundle, `Generation.conditions` removed (a live-object histogram shows the dropped policy released); D45 smoke unchanged (22,138 B/op, rsa2008 758.62); re-check pending
- [ ] 42.13 Profile P-FIXED3 (Codex Astra; `research/pr23-astra-fixed3-profile.md`): exact class histograms on the current tree: rsa2008 758.6 B, narrow 280, responses 2,336, multi-valued 848, synthetic:100 245.7, pip:8 independent 5,864 B per decision; rsa2008 CPU 97.7% policy evaluation, 2.3% fixed cost; ranked R1 PIP response assembly (200-500 B), R2 rule contexts (D47), R3 single-key resolver cache key (PIP 128 B), R4 literal assignments in mixed expressions (responses 32 B+), R5 shared plain `Result` (25-48 B), R6 `Attribute.Builder` index loop (PIP 48 B); CPU: the current-policy walk from `PolicySet.java:372` about 3%
- [ ] 42.14 D47 rules evaluated in their policy's context (draft Codex Sol; maintainer 2026-10-28: plan review first, then build after D45; recommendations 1 to 3 accepted): plan review PR-D47-argon APPROVE WITH CHANGES (P2 the eligibility scan also covers function references, type assertions, exact classes and variable graphs; P2 the parent by `instanceof`, `canIndexRules()` kept on seven classes; P3 status order in a pass context; P3 savings from the exact histograms: rsa2008 -100.7, multi-valued -192 B/op), amendments folded into D47; build after FIX-D45 and AZ (same files)
- [ ] 42.15 Cycle AY (Codex Astra): P-FIXED3 R3, R4, R5, R6 and the `Attribute` hash fold
- [ ] 42.16 Cycle AZ (Claude Opus): P-FIXED3 R1 (one-category resolved-attribute response) and the current-policy walk (CPU)
  - Implemented: R1 builds the one-category list without the multimap, today's hash order and dedup kept (`Sets.newHashSetWithExpectedSize(2)`, as `HashMultimap` makes), a second category falls back to the unchanged multimap pass; exact histograms -440 B per `pip:8 independent` decision in every JVM; three-fork smoke pip:8 independent 6,184 to 5,662, shared 7,630 to 6,720, shared-static 6,355 to 6,120 B/op; the `PolicySet` constructor skips the current-policy walk below a root top (proof in the code, an assertion in every test run), its inclusive CPU share 3.1% to 1.4% in two profile pairs, 0 B; reactor green in the lane (core 7,245, PDP 790); review pending. Named, not done: `AttributeIndex.Builder`'s Guava staging (about 500 B for two attributes) needs a representation decision. Found: `DefaultPolicyDecisionPoint.close()` does not remove the `PreparedPolicySetsListener` registered at `PolicyDecisionPointContextFactory.java:135`, so a live repository keeps every closed PDP's generation

## 35. Batch 4 and 5 measurement

- [ ] 35.1 JMH pair (commit `87133237` against the batch), the D24 on/off parameter, and the D28 PIP corpus; profile; local commit

## 25. Batch 3 measurement

- [ ] 25.1 One JMH pair, batch 2 commit against batch 3, plus the D24 on/off parameter; ledger
- [ ] 25.2 Profile and run the next research swarm; commit batch 3 locally on the verified tree

## 3. Close out

- [ ] 3.1 `openspec validate optimize-pdp-evaluation --strict`
- [ ] 3.2 Audit requirement → task → test → evidence in `evidence/README.md`, and the final ledger against the stop rule of the proposal
- [ ] 3.3 Close the beads epic and run `br sync --flush-only`
