# 018: red and green runs of the review-gate fixes (task 3.7)

Date: 2026-10-27. Oracle GraalVM 21.0.6 (JDK 21), Maven 3.9.16, offline. Raw logs stay in the
scratch directory (Maven logs carry local account data); the lines below are copied from them.

| Run | What | Result |
| --- | --- | --- |
| a | fix batch of rv004 to rv007 applied, existing tests | 168 tests, 0 failures |
| b | `ReviewGateFixesTest` (16) and its stores added | 184 tests, 0 failures |
| c | rv009 test with the old `catch(RuntimeException \| Error)` | 1 failure: `loadSetUpClosesTheFixtureWhenAPreflightSessionThrowsACheckedException`, "expected: 0 but was: 1" (one session left open) |
| d | `catch(Throwable)` restored | 185 tests, 0 failures |
| e | rv009 minor test with the old `try/finally` tear-down | 1 failure: `tearDownKeepsTheCheckFailureWhenCloseAlsoFails`, "expected to contain: lookup but was: Cannot close the session of Store \"drifting\" with corpus \"rsa2008\""; `CorrelationIdTest` 7 of 7 with the synchronized audit list |
| f | try-with-resources restored | 186 `xacml-jmh` tests and 7 `CorrelationIdTest` tests, 0 failures |

The reactor gate after all fixes is run 016.
