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
- [ ] 6.2 Implement D7 after the maintainer's serialization decision (design Open Question 1); check: the tests of 6.1, reactor
- [ ] 6.3 Gate: JMH pair, reactor and conformance, review (serialization question recorded)

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

- [ ] 13.1 Tests: for every bound built-in predicate the direct and the reflective path give the same result; operand order; an `EvaluationException`, a `RuntimeException`, and an `Error` from the target keep status and root cause; a wrong argument type; a custom factory and a custom spec with a standard id are not bound; validation on and off; concurrent calls (characterization first, then a failing allocation-free test)
- [ ] 13.2 Implement D15; check: 13.1, function and conformance tests
- [ ] 13.3 Gate: review by another family; reactor and conformance

## 14. Cycle J: index loops on the policy tree (D16)

- [ ] 14.1 Tests: target, AnyOf, AllOf truth tables and short-circuit; combining algorithms with an `ArrayList` and a `LinkedList`; normalization with `RandomAccess` and other lists
- [ ] 14.2 Implement D16; check: 14.1, reactor
- [ ] 14.3 Gate: review by another family; reactor and conformance

## 15. Cycle K: context queries in one loop (D17)

- [ ] 15.1 Tests: which level answers each query in nested policy sets and references; a foreign context in the chain; staging; one `Optional` instance per attribute per decision
- [ ] 15.2 Implement D17; check: 15.1, cycle E and F tests, conformance
- [ ] 15.3 Gate: review by another family; reactor and conformance

## 16. Cycle L: guarded debug logging (D18)

- [ ] 16.1 Tests: the debug lines at DEBUG are the same as before (a list appender over a decision); at INFO no argument of a guarded line is computed
- [ ] 16.2 Implement D18; check: 16.1, reactor
- [ ] 16.3 Gate: review by another family

## 17. Cycle M: lazy obligation and advice staging (D19)

- [ ] 17.1 Tests: published obligations and advice and their order for nested rules, policies, and policy sets (cycle E characterization), with and without applicable expressions
- [ ] 17.2 Implement D19; check: 17.1, conformance
- [ ] 17.3 Gate: review by another family; reactor and conformance

## 18. Batch 2 measurement and third profiling round

- [ ] 18.1 One JMH pair: the commit before batch 2 against batch 2 (D12 as changed on 2026-10-27), no agent session alive; ledger
- [ ] 18.2 Profile the batch and run the third profiling swarm; record its reports in `research/`
- [ ] 18.3 Commit batch 2 locally on the verified tree

## 3. Close out

- [ ] 3.1 `openspec validate optimize-pdp-evaluation --strict`
- [ ] 3.2 Audit requirement → task → test → evidence in `evidence/README.md`, and the final ledger against the stop rule of the proposal
- [ ] 3.3 Close the beads epic and run `br sync --flush-only`
