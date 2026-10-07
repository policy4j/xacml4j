# Run 043: donor pre-audit for task 5.2, Value/BagOf rename (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High) per the pane; the report labels itself
gemini-2.5-pro (known label drift). Session `policy4j--g1`, pane 3, about 3
minutes, 33 tool calls. Read-only: donor `b9f114a3` via `git show`, callers via
`rtk rg`. Raw report: [agy-report.txt](agy-report.txt) (A 20 defects, B fixes
to preserve, C bridge plan and caller counts, D 38 tests).

Reported verdict: do not apply the donor 5.2 hunks as they are.

## Conductor adjudication

- Rejected: top defect 1 (`BagOf.java:137-162` union/intersection "collapse
  duplicates, violating multiset semantics"). XACML 3.0 A.3.11 requires the
  union and intersection results to contain no duplicates (as determined by
  type-equal); collapsing is correct. Bag multiplicity matters for bags in
  general (designator results, bag functions), not for these set functions.
  Run 037 table B rows 46-47 test the required behavior.
- Consistent with earlier evidence (run 040, design D9): `SeqOf` and
  `ValueStreamOfType` extend `Stream` (task 6.4 reworks them); `Duration`
  checks `getYears()` in `months()` and `days()`; `UriVal` normalizes
  (rv038); `PortRange` does not compile (the donor tree does not compile,
  design table).
- B (fixes to preserve) and C (bridge types: BagOfValues, BagOfValuesType,
  ValueExp, AnyURI, IntegerVal, ISO8601Duration, ValueExpTypeInfo; about 525
  BagOfValues references, most in xacml-core) are planning input, unverified.
  Records cannot be subclassed, so the bridges are delegating adapters.
- The remaining A rows are unverified until the 5.2 packet's tests exercise
  them.
