# 002: correlation tests after the change (task 1.2)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1. Maven 3.9.16, offline.
Sources: `5fbbd6bc` plus the group 1 edits of `PolicyDecisionPointContextFactory`,
`DefaultPolicyDecisionPoint`, and `MDCSupport`, the test, and the rule.

Result: GREEN.

| Check | Result |
| --- | --- |
| `CorrelationIdTest` | 4 tests, 0 failures, 0 errors |
| `DefaultPolicyDecisionPointTest` | 2 tests, 0 failures, 0 errors |
| `ast-grep scan --rule java-no-random-on-decide-path` on `xacml-pdp/src/main/java` | no finding, exit 0 |
| `ast-grep test` | 2 rules pass |

Raw lines: `surefire-summary.txt`, `ast-grep-scan.txt`.
