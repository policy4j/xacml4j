# rv029: bugs policy4j-the.4.9 and policy4j-the.4.10 review (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High) (report header names "gemini-2.5-pro"; recorded as
reported). Session `policy4j--g1`, pane 3. Read-only.

Scope: 7-file, 308-line patch from the private bugs2 lane: XmlContent
(context selector cardinality, document node when ContextSelectorId is
absent), ISO8601Duration.getSeconds() returning BigDecimal,
DateTimeDurationUtils (isZero on decimal seconds, fraction-preserving
toXmlDayTimeDuration), and tests in xacml-core and xacml-pdp.

Verdict: APPROVE. Findings: none.

Files the reviewer opened: the patch, both evidence summaries, red, focused
green and conformance logs, XmlContent (lane and base), ISO8601Duration (lane
and base), DateTimeDurationUtils, Entity, AttributeDesignatorFunctions,
MultipleResourcesViaXPathExpressionHandler, DefaultEvaluationContextHandler,
AttributeSelector, AttributeReference, the changed tests. It searched all
`.getSeconds()` callers and duration marshalling in xacml-json and xacml-xml,
and confirmed the RoundingMode.DOWN nanosecond rule.
