# Run 045: donor pre-audit for task 5.4, numeric semantics (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (report self-label unreliable). Session
`policy4j--g1`, pane 3, 7 tool calls (shallow). Read-only: donor `b9f114a3`.
Raw report: [agy-report.txt](agy-report.txt) (A 15 defects, B integer owner,
C overflow options, D tests).

Reported top defects: Int64/UInt32 `mul` reduce from identity 0; Int64 has
null-returning stubs; UInt32 also claims the xs:integer id; Int32 does not
compile; Int64 `mod` uses `Math.ceilMod`.

## Conductor spot checks (2026-10-02)

Confirmed in `b9f114a3:xacml-core/src/main/java/org/xacml4j/v30/types/Int64.java`:
`mul` reduces with `0L` (`:163-166`), so every product is 0; eight
`return null` stubs (`:126-146`, `:205`, `:210`, `:245`); `mod` uses
`Math.ceilMod` (`:190`), so 5 mod 3 is -1 (worktree and run 037 C2 expect 2,
remainder with the dividend's sign).

Not accepted as stated: the overflow section cites "XACML 3.0 A.3.4" and
"7.3.3, 10.2.7" as requiring Indeterminate when a result cannot be computed.
Arithmetic functions are A.3.2, and run 037 (Grok, line-cited) found that
A.3.2 names no status code for overflow. The choice stays a maintainer
decision (C3). The reviewer recommends option 1 (64-bit with exact
arithmetic, overflow -> Indeterminate processing-error; 3 files; keeps Int64
a value-class candidate for group 9) over BigInteger (15+ files, heap
allocation, not flattenable).
