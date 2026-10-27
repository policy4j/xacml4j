# rv004: code review of the Codex-authored files (task 3.7)

Date: 2026-10-27. Lane: `policy4j_code_review_claude_fable`, Claude Code, Claude Fable 5.1, read-only,
NTM session `xacml4j--pdp-jmh-review`. Scope: snapshot store, synthetic and directory corpora, the three
benchmarks. Report: `report.md`.

Verdict: APPROVE WITH CHANGES (1 major, 2 minor, 5 nits).

| # | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| 1 | major | `importXml` builds an import tool (and a provider discovery) per document | one tool per session (`importAll`); test `importXmlBuildsOneImportToolPerOperation` |
| 2 | minor | `lookup` returns an `Optional` (16 B/op not in the control) | `lookup` dispatches on the kind and returns the store's answer |
| 3 | minor | hidden files break a `dir` corpus | hidden files skipped; test `hiddenFilesAreNotCorpusFiles` |
| 4 | nit | missing `root` key message | named; manifest-problem case "no root key" |
| 5 | nit | load setup opens a fixture before the capability check | capability first |
| 6 | nit | snapshot latest lookup allocates; quadratic load | `lowerKey` walk without allocation; quadratic load kept (copy-on-write, D10) |
| 7 | nit | duplicated `checkThreads`, unused load `Cursor` | kept (D13 surface) |
| 8 | nit | catch-and-rethrow in `checkThreads` | kept (harmless) |

Design D14 records the changes; the fixed code is re-reviewed in rv009.
