# rv007: group 1 round 2 fix verification (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; JDK 11 jshell probes of
javax.xml.datatype.Duration and java.time; did not run Maven.

Scope: the six-file delta that fixes rv004 findings 1 and 2
(`evidence/tests/005-reactor-green-jdk11/review-fixes.md`,
`53-review-fixes.patch`).

Verdict: APPROVE. No remaining MEDIUM or HIGH defect.

- rv004 finding 1 (context node): fixed.
  `DefaultEvaluationContextHandler.java:129-132` uses the Content document node
  whenever ContextSelectorId is null (`content.resolve` passes a null entity
  callback, so the implicit content-selector lookup does not run) and
  `entity.resolve` only for an explicit id (XACML 3.0 section 7.3.7). Callers
  checked: multiple-decision XPath handlers only rewrite attributes; XACML 2.0
  selectors never carry a ContextSelectorId and are rooted at the Content
  document. IIIE301-303 fail identically before and after.
- rv004 finding 2 (fractional seconds): fixed.
  `DateTimeDurationUtils.java:224-235` keeps the BigDecimal seconds and applies
  the sign once; PT1.5S, PT0.5S, PT0.001S and negatives verified; offsets
  kept; whole-second results unchanged.
- P14M double count: fixed (`DateTimeDurationUtils.java:238-248`); both
  signs; lexical and programmatic values agree.

New findings:
- LOW, `DateTimeDurationUtils.java:260-264`: PT1.0S now serializes as
  P0DT0H0M1.0S (was P0DT0H0M1S); equality, hash and ordering unchanged.
  Smallest fix: pass a scale-0 BigDecimal when the fraction is zero (not
  stripTrailingZeros, which yields 1E+0). Resolution: packet 1.3i.
- New limit (disclosed by the implementer): sub-nanosecond durations
  (PT0.0000000001S) now throw ArithmeticException from intValueExact()
  (`DateTimeDurationUtils.java:227`), wrapped as FunctionInvocationException;
  the old path truncated to 0. Resolution: packet 1.3i (truncate toward zero
  at nanosecond precision, with a test).

Recorded gaps confirmed as pre-existing, not regressions: zero/two-node
context selector (bug policy4j-the.4.9); ISO8601Duration.getSeconds/isZero,
toXmlDayTimeDuration and ISO8601DayTimeDuration.of(Duration) truncation, and
PT-0.5S (bug policy4j-the.4.10); no AssociatedAdvice comparison (bug
policy4j-the.3.8); comment/PI-first Content UNVERIFIED.

Residual (reviewer): the attribute-selector function with an Entity argument
calls Entity.resolve directly (`AttributeDesignatorFunctions.java:104-105`),
so a null ContextSelectorId there still uses the implicit content-selector
(`XmlContent.java:199`); the category-URI overload follows section 7.3.7.
Added to bug policy4j-the.4.9.
