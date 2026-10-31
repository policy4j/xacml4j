# rv031: rv027 fix verification, unzoned time round trip (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: the bugs lane after the rv027 fix (ISO8601Time.toXacmlString uses
ISO_LOCAL_TIME for unzoned values and ISO_TIME for zoned values).

Verdict: APPROVE; rv027 finding verified fixed.

Reviewer statements: "23:00:00" formats as "23:00:00" and stays unzoned after
re-parse; "23:00:00Z" and "23:00:00-05:00" format as before; "23:00:00.125"
keeps its fraction. NonNumericComparisionFunctionTest covers the GMT-08:00 case
through a lexical round trip (true) and a zoned first argument with unzoned
bounds (01:30-08:00 in [01:00, 02:00] -> true). XML
(TypeToXacml30Test.unzonedTimeRoundTripPreservesLexicalForm) and JSON
(JsonEntityMarshallingTest.testUnzonedTimeValueRoundTripPreservesLexicalForm)
round trips added. Only the 8 patch files changed; LGPL headers identical;
ast-grep scan clean. Lane gates: core 765/0/0/2, xml 35, json 6 (1 skip),
pdp 74 (3 skips).
