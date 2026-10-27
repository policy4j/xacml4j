# rv006: code review of the Claude-authored files (task 3.7)

Date: 2026-10-27. Lane: `policy4j_code_review_codex_sol`, Codex CLI, GPT-6.1 Sol, read-only. Scope: the
spine, the tests, the build. Report: `report.md`.

Verdict: REJECT (3 major).

| # | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| 1 | major | the lookup oracle used `VersionMatch`, which reads `*` as one digit, so it agrees with the defect | `VersionPatterns` (XACML 3.0 §5.13); engine defect filed as bead `policy4j-w842`; tests `versionPatternsFollowXacml30Section513`, `wildcardReferenceExpectsTheHighestMatchingVersion`, `inMemoryStoreFailsTheWildcardLookupBecauseOfTheEngineDefect` |
| 2 | major | the concurrent check does not read the store (reference cache) | `verifyConcurrent` runs every lookup from every thread; test `concurrentCheckRunsTheLookupsOnTheStore` |
| 3 | major | repository ids repeat across forks | process id and start time in the id; test `repositoryIdsCarryTheProcessAndUseOnlySafeCharacters` |

Verified by the conductor before acting: F1 (`VersionMatch.java` `AST_REPLACEMENT` is `\\d`), F2 (the
lookup benchmark returned the store's answer unchecked), F3 (the counter is per JVM). Re-review: rv009.
