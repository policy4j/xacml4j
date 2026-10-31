# 020: cycle H round 2, after review RVH1 (tasks 12.2, 12.3)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25.

Review RVH1 (Codex GPT-6.1 Sol, `evidence/reviews/code/rvh1-cycleH-sol`) gave APPROVE WITH CHANGES on
round 1: the static `BagOfValues.empty(ValueType)` map kept custom value types alive and bypassed a
custom `bagBuilder()` (two P2), and no test proved that a present attribute builds no empty bag (P3).

Round 2: `BagOfValues` and `ValueType` are back to HEAD (the `ValueType` default is
`bagBuilder().build()`); the enum `XacmlTypes` caches its own empty bag in an instance field, built
by `bagBuilder()` on first use; `BagOfValuesType.emptyBag()` returns that bag for a built-in type and
`builder().build()` otherwise. `AttributeReference.evaluate` and `AttributeDesignatorFunctions` keep
`v.isPresent() ? v.get() : ...emptyBag()`. New ast-grep rule `java-no-eager-empty-bag` with a test.

| Run | Tree | Result |
| --- | --- | --- |
| `red-on-HEAD.txt` | HEAD plus the 6-test file | 6 run, 2 failures, as intended: `oneEmptyBagIsSharedPerType` (no sharing) and `presentAttributeDoesNotBuildAnEmptyBag` (eager `orElse`) |
| `green.txt` | working tree | 126 run, 0 failures: `EmptyBagCharacterizationTest` 6 (with `customTypeKeepsItsBagBuilderAndIsNotShared`), `XacmlTypesTest` 2, `TypeRegistryTest` 77, `BagOfValuesCharacterizationTest` 6, `BagOfValuesTest` 11, `AttributeSelectorTest` 15, `AttributeDesignatorTest` 9 |
| rule | working tree | `ast-grep test`: 3 rules pass; `ast-grep scan`: clean; a probe file with `v.orElse(type.emptyBag())` matches |

`customTypeKeepsItsBagBuilderAndIsNotShared` passes on HEAD too (characterization); it fails on round 1,
which is the RVH1 finding. The JMH pair 019/020 measured round 1; round 2 shares the same bag per
built-in type, and is measured with the next batch (maintainer, 2026-10-27: measure after the changes
are complete). Reactor: `021-reactor-H-harness`.
