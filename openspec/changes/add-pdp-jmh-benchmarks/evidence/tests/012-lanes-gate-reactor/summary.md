# 012: lanes gate, full reactor and scans (part of task 3.7)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1. Maven 3.9.16, offline.
Sources: the spine and all group 3 lane files, plus the uncommitted work of
`optimize-pdp-evaluation` group 1 and `upgrade-build-plugins`.

Result: BUILD SUCCESS in 55.7 s.

| Module | Tests | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- |
| Core Engine | 6,501 | 0 | 0 | 2 |
| XML marshallers | 253 | 0 | 0 | 0 |
| JSON | 24 | 0 | 0 | 1 |
| PDP | 199 | 0 | 0 | 3 |
| Policy Unit Test Support | 99 | 0 | 0 | 0 |
| Conformance Tests | 937 | 0 | 0 | 12 |
| JMH Benchmarks | 165 | 0 | 0 | 0 |
| **Total** | **8,178** | **0** | **0** | **18** |

- Conformance unchanged; `git status` shows no change under
  `xacml-conformance-tests`.
- `ast-grep scan` of `xacml-jmh/src/main` and `xacml-pdp/src/main` (rules
  `java-no-console-output`, `java-no-random-on-decide-path`): no finding.
- The one-fork smoke of `benchmarks.jar` with `-foe true` is run 008.
