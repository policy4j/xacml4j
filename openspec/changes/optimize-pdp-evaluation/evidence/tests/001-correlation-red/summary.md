# 001: correlation tests before the change (task 1.1)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1. Maven 3.9.16 (IntelliJ
bundled), offline. Sources: `feature_xacml_20` at `5fbbd6bc` plus the new test
and rule of this task; no main source changed.

Result: RED, as intended.

| Check | Result |
| --- | --- |
| `CorrelationIdTest` (4 tests) | 2 failures, 2 errors |
| `ast-grep test` | 2 rules pass (the new rule test: 1 valid, 5 invalid cases) |
| `ast-grep scan --rule java-no-random-on-decide-path` | 2 errors: `PolicyDecisionPointContextFactory.java:68`, `MDCSupport.java:49` |

The failures show the random request-id suffix (for example
`<cid>-ea7f8b1693214b3a` where `<cid>-1` is expected). The two errors are
`StringIndexOutOfBoundsException` in the test helper `prefixOf`: today's
correlation id is one random hex number without a `-`, so the expected
`<prefix>-<sequence>` form is absent.

`surefire-summary.txt` holds the Surefire lines.
