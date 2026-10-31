# rv001: gate review of adopt-jdk25-baseline 2.3 and upgrade-dependencies 2.4

Date: 2026-10-27. Codex GPT-6 Astra (a family other than the author's), read-only, NTM session
`xacml4j--pdp-profile-1`, after one follow-up (its first turn ended with a progress note).
Report: `report.md`. Verdicts: adopt-jdk25-baseline APPROVE WITH CHANGES; upgrade-dependencies
REJECT.

| # | Finding | Disposition |
| --- | --- | --- |
| 1 | Async appenders flush for at most 1 s at shutdown (Logback 1.3.16 default) | fixed: `maxFlushTime` 0 in both shared appenders; tests assert it; slow-backlog test and its 1 s control (upgrade-dependencies run 004) |
| 2 | Spec says dropped benchmark events are counted; Logback does not count | spec corrected: events past a full queue are dropped, not counted |
| 3 | Child-JVM test reads output before its timed wait | fixed: output to a file, timed wait first, forced kill on timeout |
| 4 | Gate runs keep raw output only in scratch; a stale jar claim | new runs with sanitized output: adopt-jdk25-baseline 003, upgrade-dependencies 004; earlier runs kept as history |
| 5 | Dependency spec and proposal still name JDK 21 for the gate | changed to JDK 25 |
