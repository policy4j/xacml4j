# rv057: engine bugs 4.24, 4.28, 4.30, 4.31, 4.32 adversarial review (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only on the merged main worktree, lane
evidence, XPath F&O 3.1 func-time-equal, XACML 3.0 A.3; JDK 21 jshell probe.
Report text from the Grok session's chat history.

Verdict: REQUEST_CHANGES (1 HIGH).

HIGH, `ISO8601Time.java:153-170`: `equals`, `hashCode` and `compareTo` use
`utcTime()` = `withOffsetSameInstant(UTC).toLocalTime()`, which drops the
calendar day. `time-equal(08:00:00+09:00, 17:00:00-06:00)` is true and
`time-less-than` false; XF `op:time-equal` places both on 1972-12-31 and
compares as dateTimes (1972-12-30T23:00:00Z vs 1972-12-31T23:00:00Z: not
equal; the XF normative example returns false). jshell: localEqual=true,
epochEqual=false. Fix: one day-preserving key for all three methods
(`toNanoOfDay() - offsetSeconds * 1e9`, or the instant on 1972-12-31); tests
for that pair (not equal, less-than true) while keeping 12:00:00Z ==
13:00:00+01:00 and unzoned 23:00:00 == 23:00:00Z.

Confirmed correct: Value equals/hashCode un-final (AnyURI lexical override
safe, instanceof-guarded); date/dateTime instant equality and ordering;
offsets kept in print; +00:00/-00:00/Z equal; fractional seconds; Locale.ROOT
ignore-case (final sigma, Turkish I); yearMonthDuration rejects P30D, PT1H,
P1Y2M3D, P-1Y; XPathCategory copied for advice and obligation only from
otherAttributes; selector MustBePresent=false; red-then-green logs; no
fixture or manifest change.

Residual (pre-existing, not the reject reason): `time-in-range`
(`NonNumericComparisonFunctions.java:138-142`) also drops the day and uses
the context timezone for an unzoned first argument; 24:00:00 collapses to
00:00 and a leap second throws an unwrapped DateTimeException in the parser;
unzoned dateTime prints with Z; `+P1Y` passes through a fallback parser;
`ISO8601Date.serialVersionUID` unchanged after new fields.
