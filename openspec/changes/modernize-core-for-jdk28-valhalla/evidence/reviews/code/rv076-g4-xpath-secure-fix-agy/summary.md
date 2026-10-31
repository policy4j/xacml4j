# rv076: rv070/rv074 fix verification on bug 4.52 (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 19 tool calls. Read-only: merged main worktree,
evidence tests/064-xpath-secure-processing/review-fix (author Luna/Codex).

Verdict: APPROVE (all prior findings FIXED).

- rv074-1 / rv070 LOW FIXED: `XmlContentTest.testXPathFactoryEnablesSecureProcessing`
  asserts `getFeature(FEATURE_SECURE_PROCESSING)` through the package-private
  `DefaultXPathProvider.isSecureProcessingEnabled()`, on the same supplier
  `newXPath` now uses; red with the setFeature line removed (expected true,
  was false), green with it. No public API change.
- rv074-2 LOW FIXED: `MatchTest` no longer pins missing-attribute for an XPath
  error; it keeps INDETERMINATE, `setEvaluationStatus(eq(rule), any(Status))`
  and the interaction checks.

Conductor check: `INSTANCE` is created before `XPATH_FACTORY`, but the
default constructor's supplier lambda reads `XPATH_FACTORY` only when called,
so there is no NPE during class initialization.
Out of scope (separate bug, recorded in rv074): `AttributeReference` maps a
non-syntax XPath failure to missing-attribute when MustBePresent is true.
