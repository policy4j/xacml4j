# rv036: rv030 fix verification, bugs 4.9 and 4.10 (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: bugs2 lane after the rv030 repairs (585-line patch).

Verdict: APPROVE. New issues: none.

- rv030 finding 1 (4.9, syntax-error swallowed): FIXED
  (`AttributeReference.java:132-136`, `DefaultEvaluationContextHandler.java:197-203`).
  DefaultEvaluationContextHandlerTest:443-476 covers zero and two nodes with
  MustBePresent false and true (all StatusCodeId.SYNTAX_ERROR); :479-504 the
  PIP-supplied entity; MatchTest:189-206 shows MatchResult.INDETERMINATE with
  syntax-error status.
- rv030 finding 2 (4.10, negative-scale seconds): FIXED
  (`DateTimeDurationUtils.java:306-317`): integral seconds use
  new BigDecimal(seconds.toBigInteger()) (scale 0); fractional nanos trimmed
  by integer division without stripTrailingZeros, so no negative scale.
  ISO8601DurationReviewTest:326-356 round-trips 10, 20, 59, 60 s, 1 min,
  1 min 30 s, negatives, and 10.5 s; XML (TypeToXacml30Test:534-541,
  "P0DT0H0M10S") and JSON (JsonEntityMarshallingTest:570-584, "P0DT0H0M20S")
  round trips pass.
- Residuals recorded in the lane evidence: a content-selector attribute with
  several XPath values uses the first (findValue); Duration.ofSeconds(Long.MIN_VALUE).abs()
  throws ArithmeticException.
