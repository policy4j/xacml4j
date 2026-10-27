# 030: cycle J after the change (task 14.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

Change (D16): `Target`, `MatchAnyOf`, `MatchAllOf` keep their lists in `List` fields and index them;
`BaseDecisionCombiningAlgorithm.indexed(List)` returns a random-access list as is and copies any other once;
the ten combining loops index it; `FunctionSpecImpl.normalize` is one index-based method.

- Focused (`focused.txt`): the characterization of 029 and the target, combiner, and function tests, 587
  run, 0 failures.
- Reactor (`reactor.txt`): 8,301 tests, 0 failures, 0 errors, 18 skipped; xacml-core 6,575 (2 skipped),
  conformance 938 (12 skipped), xacml-jmh 195.

No JMH run: batch 2 is measured once (task 18.1).
