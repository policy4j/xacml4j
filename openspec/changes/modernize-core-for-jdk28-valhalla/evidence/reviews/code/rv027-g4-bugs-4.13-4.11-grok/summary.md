# rv027: bugs policy4j-the.4.13 and policy4j-the.4.11 review (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; read the XACML 3.0 OS text; replayed
time-in-range cases in JDK 21 jshell; ast-grep scan exit 0. Maven not re-run;
ripwire UNAVAILABLE (the lane copy has no .git).

Scope: same 5-file patch as rv025.

Verdict: REQUEST_CHANGES (1 MEDIUM).

- 4.13: approve. notifyPolicySetRemoved calls policySetRemoved
  (`AbstractPolicyRepository.java:188-192`); other notifiers already matched;
  red log shows policySetAdded 2 times.
- 4.11 algorithm and URN: approve. Canonical 2.0 id, legacy 1.0 alias
  (`NonNumericComparisonFunctions.java:114-115`); inclusive range, wrap when
  the third UTC time-of-day is earlier than the second, context zone only for
  an unzoned first argument, unzoned bounds inherit it (`:123-142`). jshell:
  23:00 unzoned at GMT-08:00 -> true, 23:00Z -> false, zoned first with
  unzoned bounds -> true.

Finding, MEDIUM, `ISO8601Time.java:121-123`: toXacmlString prints the
placeholder UTC offset (ISO_TIME), so an unzoned 23:00:00 serializes as
23:00:00Z; re-parsing sets timeZoneDefined and the GMT-08:00 case flips from
true to false. XML and JSON marshalling use that string
(`TypeToString.java:249-251`, `TypeToXacml30.java:421-422`,
`TypeToGSon.java:212-213`). Fix: ISO_LOCAL_TIME when the zone was not given;
test that a lexical round trip keeps the result.

Checked: equals/hashCode/compareTo ignore the flag (`Value.java:128-142`,
`ISO8601Time.java:151-152`), so 23:00:00 and 23:00:00Z stay equal and
ordering functions are unchanged.

Residual (reviewer): no test for zoned first argument with unzoned bounds;
the new boolean defaults to false on deserialization; 24:00:00 still throws
DateTimeException (OffsetTime.of).
Resolution: repair packet to the implementer lane (bugs lane).
