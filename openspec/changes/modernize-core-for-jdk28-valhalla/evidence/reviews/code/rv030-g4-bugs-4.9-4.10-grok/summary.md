# rv030: bugs policy4j-the.4.9 and policy4j-the.4.10 review (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; read the patch, the seven files,
AttributeReference, AttributeSelector, Match, DefaultEvaluationContextHandler,
Entity.resolve, the MDP handler, DOMUtil.getXPath, Xacml20XPathTo30Transformer,
TypeToString, TypeToXacml30, XACML 3.0 OS 5.30 and 7.3.7; JDK 21 jshell probe.

Scope: same 7-file patch as rv029.

Verdict: REQUEST_CHANGES (2 HIGH).

1. HIGH, 4.9: `XmlContent.java:216` throws syntax-error for a non-singleton
   ContextSelectorId, but `AttributeReference.resolveImpl`
   (`AttributeReference.java:131-137`) turns it into an empty bag when
   MustBePresent is false and into missing-attribute when it is true, and
   `DefaultEvaluationContextHandler.doPipResolve` (`:196-198`) returns empty
   on the PIP path. Section 7.3.7 step 2 requires Indeterminate with
   urn:oasis:names:tc:xacml:1.0:status:syntax-error in both cases. The handler
   tests never call AttributeSelector.evaluate.
2. HIGH, 4.10 (regression introduced by the fix):
   `DateTimeDurationUtils.toXmlDayTimeDuration` (`:304-306`) passes a
   negative-scale seconds BigDecimal for a seconds component of 10, 20, 30,
   40 or 50; JDK 21 DurationImpl.toString throws
   StringIndexOutOfBoundsException, so toXacmlString and JSON/XML marshalling
   throw. Fix: reuse the parser's scale-0 rule
   (new BigDecimal(seconds.toBigInteger()) when the fraction is zero).

Consistent with the spec (reviewer): document node when ContextSelectorId is
absent; explicit one-node selectors; the 2.0 transformer; unsigned
fractional getSeconds; -PT0.5S; PT1S/PT1.0S equality; RoundingMode.DOWN.
The lane suites do not call AttributeSelector.evaluate with a bad context
selector or format a 10-second java.time.Duration.

Residual (reviewer): a content-selector attribute with several XPath values
still uses the first value (findValue); Duration.ofSeconds(Long.MIN_VALUE).abs()
throws ArithmeticException. ISO8601Duration.java has no LGPL header
(pre-existing).
Resolution: repair packet to the implementer lane (bugs2).
