# rv006: group 1 round 3 review, identifier aliases (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md` (session cleared before
the review). Session `policy4j--g1`, pane 0. Read-only; read the XACML 3.0 OS
specification text; did not run Maven.

Scope: same as rv005.

Verdict: REQUEST_CHANGES.

Findings:
1. HIGH, `HigherOrderFunctions.java:54-70` (anyOf), `:73-89` (allOf),
   `:92-110` (anyOfAny), `:172-191` and `:209-241` (map and
   MapFunctionResolverValidator).
   XACML 3.0 A.3.12 defines any-of and all-of as n+1 arguments (n-1
   primitives and one bag), any-of-any as n+1 arguments (primitives or bags,
   cross product), and map as n+1 arguments (n-1 primitives and one bag),
   and table 10.2.8 makes the 3.0 URNs mandatory. The 3.0 URNs are bound to
   the 1.0 fixed-arity methods. With runtime parameter checks off (the
   default, `PolicyDecisionPointContextFactory.java:80`),
   `DefaultFunctionInvocation` copies only the fixed number of arguments and
   `Method.invoke` fails on a bag; the error becomes
   `FunctionInvocationException` and `Condition.evaluate` returns
   Indeterminate. With checks on, validation rejects the call. IIC164 uses the
   1.0-shaped any-of, so conformance does not catch it.
   Smallest fix: implement the A.3.12 shapes in these four methods and bind
   both URNs to them (1.0 calls are the n=2 case for any-of, all-of,
   any-of-any and the n=1 case for map); MapFunctionResolverValidator accepts
   n+1 arguments, one bag, a function of arity n, and returns a bag of that
   function's return type.
   Resolution: assigned to the implementer lane (packet 1.3h); tracked as
   beads bug `policy4j-the.4.8`.
2. MEDIUM, `DurationFunctionIdentifierTest.java:80-89`,
   `HigherOrderFunctionIdentifierTest.java:80-91`: lookup-only tests; a wrong
   binding stays green. Resolution: evaluation tests in packet 1.3h.

Checked, no defect (reviewer): duration URNs match tables 10.2.8 and 10.2.9;
one registration each; parameter and return types are the XSD duration
types and the XQuery duration URIs still parse as aliases; getId() reports
the 3.0 URN and the 3.0 marshaller writes it; legacy lookup accepts the 1.0
URN; the three providers changed only annotations plus one import; headers
and tabs unchanged; all-of-any, any-of-all, all-of-all correctly stay 1.0.

Recorded, outside this edit: time-in-range registered only under the 1.0
URN (`NonNumericComparisonFunctions.java:111`) while table 10.2.8 lists the
2.0 URN; duplicate function ids are silently dropped
(`BaseFunctionProvider.java:83-86`, pre-existing); duration set cases
IIC340-IIC349 are outside the suite loop bounds.
