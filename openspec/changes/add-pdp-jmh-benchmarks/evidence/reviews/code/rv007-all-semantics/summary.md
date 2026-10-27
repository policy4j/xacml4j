# rv007: code, design, and XACML semantics review of all files (task 3.7)

Date: 2026-10-27. Lane: Agy 1.4.7, Gemini 4 Argon (high), role `policy4j_code_review`, read-only. This
lane carries the semantics focus in place of a Grok or Kimi lane (waiver in the design's Execution Plan).
Report: `report.md`.

Verdict: APPROVE (3 minor, 2 nits).

| # | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| 1 | minor | `importXml` relies on `add`, which the import tool does not check | load setup preflights both load paths with `verifyLookups`; test `loadSetUpRejectsAStoreThatLosesImportedPolicies` |
| 2 | minor | a store without `LOAD` must override `load` | documented in `StoreSession.load` (no-op allowed; the fixture checks every lookup) |
| 3 | minor | unknown manifest keys ignored | rejected; manifest-problem case "misspelled key" |
| 4 | nit | test comment on extended Indeterminate | reworded in `CorpusRequestTest` |
| 5 | nit | unused load `Cursor` | kept (D13) |

Accuracy note: the report's "checked and found correct" part describes code that does not exist (a
`requireCapabilities` method, a `CyclicBarrier`, a synthetic corpus with subjects alice and bob, 28
request pairs, JSON directory formats). The conductor checked each finding above in the source before
acting; the descriptive part is not relied on.
