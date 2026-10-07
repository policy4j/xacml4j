# rv048: task 4.4 adversarial review, registry tests (Grok)

Date: 2026-10-02. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only (tests, registry sources,
`a8a90d46` XacmlTypes, XACML 3.0 OS 10.2.3/10.2.7/10.2.8/B.9/A.3, Additional
Combining Algorithms Profile CS01). Report text taken from the Grok session's
chat history.

Verdict: REQUEST_CHANGES (1 HIGH, 3 MEDIUM, 1 LOW accepted).

Confirmed complete: 19 `a8a90d46` types with short names and 4 aliases; 23
combining URNs match B.9 (10.2.3 mandatory set plus 1.0 legacy and 1.1
ordered); on-permit-apply-second is the only profile URN (policy only).

1. HIGH, `TypeRegistryTest.java:119-127, 136-144`: the round trip compares
   parse(serialize(parse(x))) with parse(x), never with x. A parser that
   ignores its input passes, and lossy forms pass: the date sample
   "2002-03-22Z" loses its zone (`ISO8601Date.java:75-76, 115-116`) and the
   row is green. No rows for INF, -0, fractional seconds, offsets,
   `http://example.com/a/../b`, base64 padding, hex case. Fix: an expected
   canonical lexical column asserted against the serialized string; parse
   through the resolved type.
2. MEDIUM, `TypeRegistryTest.java:89-96`: alias resolution asserts only the
   type id string; parse through the resolved object (or assert the same
   instance while the registry is an enum).
3. MEDIUM, `DecisionCombiningAlgorithmRegistryTest.java:70-80`: only
   `getId()` is asserted, so swapping implementations behind URNs passes.
   Reviewer fix: assert the implementation class. On-permit-apply-second
   class exists but `DefaultXacml30DecisionCombiningAlgorithms` does not
   register it.
4. MEDIUM, `FunctionProviderServiceLoaderTest.java:34-41`: checks the
   provider class token only; never looks up a function id, so bugs 4.36 and
   4.37 are invisible. Fix: look up the 10.2.8 identifiers through the
   provider (the two-line check fails today).
5. LOW accepted: duplicate-id tests stay disabled until 5.3 (4.26).

## Conductor decision on the fixes

Finding 3: pinning implementation class names conflicts with the task's
purpose (guard the 6.3 class renames). Use a behavioral fingerprint instead:
evaluate each algorithm URN over a fixed set of child results that separates
the algorithms (for example Permit + Deny; Deny + Indeterminate{P};
NotApplicable only) and assert the decision. That catches a swap and survives
renames. Finding 4: assert every 10.2.8 identifier the engine claims to
support resolves through the provider; rows for 4.36/4.37 disabled with the
bead id until those fixes land. Finding 1 rows that fail today are disabled
with their bead (date zone 4.28, AnyURI rv038).
