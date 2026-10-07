## Round 4 review fixes

Started after packet 1.3i: run 71 full gate and run 73 final audit were complete.
Reviews: rv008 Agy APPROVE; rv009 Grok REQUEST_CHANGES. The conductor defers
the pre-existing engine-wide first-error behavior under policy4j-the.4.12;
this packet repairs only the LOW resolver escape and strengthens status tests.

| Finding / original file:line -> change | Tests first / observed result |
| --- | --- |
| LOW HigherOrderFunctions.java:359; map type resolution leaked IllegalArgumentException for a bag-returning reference. -> replace that precondition with a bag-only call to spec.validateParametersAndThrow(arguments), final :359-361. | mapRejectsBagReturningFunctionDuringApplyTypeResolution constructs Apply(map, string-bag, bag) with both 3.0/1.0 IDs, in runtime-checks off/on rows. getEvaluatesTo must produce the same exception class, message and syntax-error status as explicit validation; evaluate must retain processing-error. Run 75: both rows fail on IllegalArgumentException. Runs 77/78/80: both pass. |
| Strengthen status-only variadic assertions; HigherOrderVariadicFunctionsTest.java:317 before review. -> retain hasCaptured and add exact StatusCodeId comparisons; reset capture before each Condition evaluation. | assertInvalid covers no bag, two bags, missing operands, referenced arity/types/return type and the packet's malformed binary predicate. Explicit validation asserts syntax-error; Apply invocation and captured INDETERMINATE Condition status assert processing-error. mapRejectsBagReturningFunctionAtValidation also asserts syntax-error. All stronger assertions already pass in run 75 and again in runs 77/78/80. |
| MEDIUM first predicate error stops or/and/higher-order loops. | DEFERRED by conductor as policy4j-the.4.12. Every loop, helper and logical/invocation class is byte-identical to the round 4 snapshot; no behavior change or claim of error-order conformance. |

Smallest-change choice: return-type resolution is a separate entrypoint from
validation. Only a reference whose declared return type is already a bag is
delegated to the same validation path; its existing validator returns false
and FunctionSpecBuilder.java:435-437 raises PolicySyntaxException (syntax-error).
Valid map calls keep the existing type-resolution path and return type.
No new global validation, invocation behavior or exception mapping is added.
The engine's syntax exception is a structured EvaluationException; this fix
removes the raw IllegalArgumentException escape rather than changing that
exception hierarchy. Reflection invocation still maps failures to
FunctionInvocationException processing-error at DefaultFunctionInvocation:136-138.

Tests first: 75-round4-red.txt is 63/2/0/0 with the production hash equal to
the pre-test snapshot. Only the two new map-resolution rows fail. Run 77
repeats the focused command: 63/0/0/0. The 54 variadic rows, three invocation
checks and six legacy controls also pass inside both reactor attempts.
No compiler diagnostics in runs 75/77/78/80. No assertion was removed/weakened.

Full gate is BLOCKED outside this LOW-only scope. Run 78 reaches 875 tests /
2 failures / 0 errors / 9 skips; testOnePolicyDifferentVersion:143 and
testFindAllPoliciesWithTheSameId:219 fail listener verification in xacml-pdp.
With no intervening source edits, run 80 reaches 875/1/0/9; only the second
test fails. Core passes 762/0/0/4 in both; XML 34/0/0/0, JSON 5/0/0/1.
Both test-support and conformance are reactor SKIPPED, hence NOT RUN.
Prior conformance reports are stale for this round and excluded.

The repository and test match a8a90d46 byte for byte. Async notifications
run on an executor; immediate mock verification has no completion barrier.
The race cause is INFERRED from this path and differing failure counts.
No map call occurs in these test policies. Source proof and failed reports:
79-repository-race-proof.json, 80-repository-race-proof.json,
78-InMemoryPolicyRepositoryTest.xml and 80-InMemoryPolicyRepositoryTest.xml.
Concrete proposal: round4-gate-blocker-proposal.md and the unapplied,
UNVERIFIED required-repository-listener-barrier-fix.patch. No PDP edit made.

Scope: two authorized source/test files, two headers preserved, 3542 protected
files unchanged. Incremental delta: 82-round4-fixes.patch. Conformance fixture
bytes, loop bounds and thirty exclusions per suite remain unchanged; run 80
has zero case executions because conformance was not reached, not a conformance
pass. Last complete gate was run 71 (934/0/0/9; cases 347+347+17+2).
No commit, br, task tick, issue closure or evidence-index edit.
