# 025: cycle I after the change (task 13.2)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline. Working tree: commit 21858796 plus D15.

- Focused (`focused.txt`): `DirectPairCallTest` 9 of 9, including
  `pairCallOfABuiltInPredicateAllocatesNothing` (under 1 byte per call; 72.0 before, `024-cycleI-red`),
  and every function test class of xacml-core (`FunctionInvocationCharacterizationTest` 11,
  `FunctionInvocationStatusTest` 8, `LaterDecidingOperandTest` 67, `HigherOrderVariadicFunctionsTest`
  54, `MatchTest` 5, and the provider tests): 0 failures.
- Reactor (`reactor.txt`): 8,291 tests, 0 failures, 0 errors, 18 skipped; xacml-core 6,565 (2
  skipped), conformance 938 (12 skipped), xacml-jmh 195.

No JMH run: batch 2 is measured once (task 18.1).
