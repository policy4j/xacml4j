# rv004: group 1 round 2 review, downstream repair (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md` (session cleared before
this review). Session `policy4j--g1`, pane 0. Read-only; ran JDK 11 jshell
for javax.xml.datatype.Duration and java.time Period behavior.

Scope: same as rv003.

Verdict: REQUEST_CHANGES.

Findings:
1. MEDIUM, AttributeSelector context node.
   `xacml-pdp/.../DefaultEvaluationContextHandler.java:127-134` via
   `xacml-core/.../content/XmlContent.java:197-217`. XACML 3.0 section 7.3.7:
   without ContextSelectorId the context node is the Content document node.
   With a content-selector attribute present and no ContextSelectorId, the
   code uses the content-selector node. Example: relative path
   `md:patient-number/text()` returns 555555 where an empty bag is required,
   so a rule can Permit instead of NotApplicable/Indeterminate.
   Conductor check: base `a8a90d46` always called `entity.resolve(selector)`
   (content-selector as context); the repair added the spec path only for the
   no-content-selector case. Pre-existing defect, not a regression.
   Resolution: assigned to the implementer lane (packet 1.3g).
2. MEDIUM, fractional day-time seconds.
   `ISO8601DateTime.java:162-184`, `DateTimeDurationUtils.java:249-259`,
   `ISO8601Duration.java:59-61`. `Duration.getSeconds()` truncates, so
   dateTime + PT1.5S adds 1 s and + PT0.5S adds nothing (XML Schema
   Appendix E requires the fraction). Pre-existing. Resolution: packet 1.3g.

Checked and consistent (reviewer): year-month arithmetic via one Period
matches Appendix E (hand table: Feb 29 + P1Y1M = Mar 29; Jan 31 + P1M =
Feb 29/28; negative months); day-time whole-second sign handling; offsets
kept; designator issuer matching (section 5.29); MustBePresent; XPath errors
still throw; multiple-resource lookup equals the pre-a8a90d46 lookup;
assertObligations now reads the actual list and missing/extra ids and
obligations still fail; mixed-content whitespace skip.

Recorded gaps (no change requested): no AssociatedAdvice comparison in
Xacml30TestUtility.assertResults (pre-existing); comment/PI first in mixed
Content UNVERIFIED; ISO8601YearMonthDuration.of(Duration) with non-normalized
months may double-count; zoned xsd:date stored as LocalDate.

Grok asked approval for each command; the conductor approved read-only git,
rg, ast-grep, ripwire quality-delta, jenv lookups, and computation-only
jshell snippets.
