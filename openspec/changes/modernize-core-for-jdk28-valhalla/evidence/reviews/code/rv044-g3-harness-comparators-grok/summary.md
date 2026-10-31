# rv044: bugs 3.10, 3.11, IIIE302 review, conformance comparators (Grok, adversarial)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only: the three utilities and tests, the
value types, run 030 evidence, JDK 21 jshell probes (URI, LocalDate,
OffsetDateTime, Double, DatatypeFactory, isEqualNode), XACML 3.0 OS A.3.1.

Scope: same as rv042.

Verdict: REQUEST_CHANGES (2 MEDIUM, 3 LOW).

1. MEDIUM, `Xacml30TestUtility.java:143-162` via `AnyURI.java:75-81`: typed
   anyURI comparison goes through URI.normalize/URI.equals, so
   `http://example.com/a/../b` equals `http://example.com/b` and
   `HTTP://Example.COM/a` equals `http://example.com/a`. A.3.1 anyURI-equal is
   codepoint equality of string-from-anyURI. Fix: compare anyURI lexical
   forms after whitespace collapse; negative tests for both pairs.
2. MEDIUM, `ISO8601Date.java:75-76`, `Value.java:133-142`: date parsing drops
   the timezone (2002-10-10+14:00 equals 2002-10-10-12:00), and zoned
   dateTime/time compare with OffsetDateTime.equals, not instant equality
   (12:00:00Z vs 13:00:00+01:00 is a false reject; op:date-equal,
   op:dateTime-equal, op:time-equal). Fix in the comparator: compare zoned
   values as UTC instants; tests for both directions.
3. LOW, `XacmlPolicyTestSupport.java:201-249`: findMatchingObject does not
   consume matches (two identical expected obligations bind to one actual);
   Obligation.equals ignores FulfillOn in the domain path. Supersedes rv042
   LOW (Advice.equals already compares attributes). Fix: consume matches as
   sameMultiset does and compare FulfillOn.
4. LOW, `DoubleVal.java:62-68`: -0.0 and 0.0 differ under Double.equals;
   IEEE 754 equality treats them as equal. Fix: Double.compare == 0 in the
   comparator (keeps NaN == NaN, right for a response oracle).
5. LOW, `DateTimeDurationUtils.java:290-299`: an invalid yearMonthDuration
   such as P30D parses to zero and equals P0Y. Fix: reject D/H/S in a
   year-month lexical form.

Confirmed: strings strict; type mismatch fails first; category multisets
consume matches and compare Content; hex vs base64 split; isEqualNode rejects
namespace, prefix and comment differences; 2.0 obligations read the actual
response; manifest paths unchanged.
Residual: xpathExpression, dnsName, ipAddress, x500Name equality not probed;
3.0 status compares the top-level code only and 2.0 ignores StatusMessage
(pre-existing).

Conductor correction (2026-10-02, JDK 21 jshell): the suggested fix for
finding 4 is wrong. `Double.compare(-0.0, 0.0)` is -1 (it orders -0.0 below
0.0), and `Double.compare(NaN, NaN)` is 0. The comparator rule that treats
signed zeros as equal and NaN as equal to NaN is
`a == b || (Double.isNaN(a) && Double.isNaN(b))`.

Engine follow-ups: the engine's equality functions call Value.equals
(`EqualityPredicates._eq`), so findings 2, 4 and 5 are engine defects too.
Filed: policy4j-the.4.28 (temporal equality), 4.29 (double-equal IEEE 754),
4.31 (yearMonthDuration lexical form). Conductor check of the same file found
policy4j-the.4.30: string-equal-ignore-case calls the case-sensitive `_eq`
(`EqualityPredicates.java:104-111`), with no unit test or fixture.
Finding 1 is fixed by the AnyURI lexical-form repair (rv038).
Resolution: findings 1, 3, 4 to the harness owner with the rv038 repair;
findings 2 and 5 through the engine bugs.
