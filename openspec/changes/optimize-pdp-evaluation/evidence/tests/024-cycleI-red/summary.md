# 024: cycle I tests before the change (task 13.1)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, tree at commit 21858796.

`xacml-core/src/test/java/org/xacml4j/v30/policy/function/DirectPairCallTest.java`, 9 tests:
- characterization, pass before D15: every two-parameter function of the default provider gives the
  same outcome (value, or failure class, status code, cause class and message) on the two-argument
  path and on `invokeWithList`, over sample values of 16 types and their bags; operands evaluated once
  each and in order; failures of a function (an `EvaluationException`, a `RuntimeException`, one
  wrapping an `EvaluationException`, an `Error`) and arguments of the wrong type fail the same way on
  both paths; validation at runtime; a custom invocation factory sees every call; a custom spec with
  a standard id keeps its invocation; concurrent calls;
- the optimization, red as intended: `pairCallOfABuiltInPredicateAllocatesNothing` measured
  **72.0 bytes per call** (`string-equal` and `integer-equal`, 20,000 calls after 40,000 warm-up
  calls, `ThreadMXBean.getCurrentThreadAllocatedBytes`, logger `org.xacml4j` at INFO for the
  measurement): the argument `ArrayList` (24 B), its array (24 B), and the reflective `Object[]`
  (24 B), as profile run 021 found.

Result: 9 run, 1 failure (the allocation test). Output: `output.txt`.

Note: the test configuration logs `org.xacml4j` at DEBUG; at DEBUG the same loop measured 9,322 bytes
per call, nearly all debug formatting. Cycle L (D18) concerns that path.
