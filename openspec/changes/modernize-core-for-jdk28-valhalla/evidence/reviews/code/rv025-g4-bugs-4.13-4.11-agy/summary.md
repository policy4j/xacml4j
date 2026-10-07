# rv025: bugs policy4j-the.4.13 and policy4j-the.4.11 review (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High) (report header names "gemini-2.5-pro"; recorded as
reported). Session `policy4j--g1`, pane 3. Read-only; 17 files opened
(patch, both lane trees, both evidence runs and their red/green logs).

Scope: 5-file patch from the private bug lane.

Verdict: APPROVE. Findings: none.

4.13 (reviewer): `AbstractPolicyRepository.java:191` now calls
policySetRemoved; all four notify* methods dispatch the matching callback.
New `InMemoryPolicyRepositoryTest.testAddRemovePolicySetNotifiesMatchingCallbacks`
(:240-260) waits on a two-callback latch, verifies one policySetAdded and one
policySetRemoved, then verifyNoMoreInteractions. Red run: TooManyActualInvocations
(policySetAdded 2 times). Green: 5/5 in class; pdp 74 tests, 0 failures, 3 skips.

4.11 (reviewer): canonical id urn:oasis:names:tc:xacml:2.0:function:time-in-range
(table 10.2.8), legacy alias urn:oasis:names:tc:xacml:1.0:function:time-in-range.
Inclusive range; when end < start (UTC nanoseconds of day) the end moves 24 h
forward, and a time before the start moves 24 h forward. Unzoned first
argument uses the context time zone's offset at the context clock's instant;
unzoned second/third arguments take the first argument's offset (XACML 3.0
A.3.8 text). ISO8601Time records `timeZoneDefined`; equals/hashCode (final,
inherited from Value.OtherValue), compareTo (OffsetTime) and toXacmlString
are unchanged, so other time functions and bag/set operations are not
affected. Red: canonical 2.0 id missing; 00:30 in [23:00, 02:00] was false.
Green: 9/9 in class; core 763 tests, 0 failures, 2 skips.
