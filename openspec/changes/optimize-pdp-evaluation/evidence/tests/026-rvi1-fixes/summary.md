# 026: fixes of review RVI1 (cycle I)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline.

- Red (`red.txt`), the three new tests on the first cycle I implementation: 12 run, 3 failures, each
  the difference RVI1 described: a function-reference parameter failed with `ClassCastException` on
  the direct path; a function reference given to `string-equal` failed with `ClassCastException`
  instead of the reflective `IllegalArgumentException("argument type mismatch")`; a non-public provider
  class gave `true` on the direct path and `IllegalAccessException` on the reflective one.
- Green (`green.txt`): `DirectPairCallTest` 12 of 12 and every function test class of xacml-core, 0
  failures. The allocation test still passes, so the built-in predicates stay bound.
- Reactor (`reactor.txt`, `mvn -B -o clean test`): 8,294 tests, 0 failures, 0 errors, 18 skipped;
  xacml-core 6,568 (2 skipped), conformance 938 (12 skipped), xacml-jmh 195.
