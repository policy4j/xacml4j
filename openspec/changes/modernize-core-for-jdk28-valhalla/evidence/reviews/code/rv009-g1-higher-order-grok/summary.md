# rv009: group 1 round 4 review, variadic higher-order functions (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; traced expressions by hand; did
not run Maven.

Scope: same as rv008, with emphasis on the shared function machinery.

Verdict: REQUEST_CHANGES (one MEDIUM, pre-existing; see resolution).

Findings:
1. MEDIUM, `HigherOrderFunctions.java:72-78, 96-102, 219-221`: any-of,
   all-of and any-of-any stop at the first element whose predicate throws
   (for example a string-regexp-match pattern error), so the result is
   Indeterminate even when a later element would decide it (True for
   any-of/any-of-any, False for all-of). XACML 3.0 A.3.12 combines them with
   or/and; A.3.5 says or is True if any argument is True, and and is False if
   any argument is False. all-of-any and any-of-all inherit the path;
   all-of-all has its own loop with the same behavior.
   Conductor check: the `a8a90d46` anyOf loop also propagates the first
   exception, and LogicalFunctions or/and (`:105-111`) do the same, so this
   is pre-existing engine-wide behavior, not a regression of this fix. Fixing
   only the higher-order loops would make them disagree with or/and.
   Resolution: tracked as beads bug `policy4j-the.4.12` (decide the semantics
   for or, and, and the six higher-order functions together; tests in 4.3).
2. LOW (reviewer residual risk): `MapFunctionResolverValidator.resolve`
   (`:359`) throws IllegalArgumentException when the referenced function
   returns a bag; validate already rejects that case, but
   Apply.getEvaluatesTo can surface the unchecked exception. Resolution:
   packet 1.3j.

What holds (reviewer): success paths match A.3.12 (one or more operands,
exactly one bag replaced at its index; any-of-any walks the Cartesian
product, including all-primitive calls); empty bags give False, True, False
and an empty bag; arity and types are validated by replaying the referenced
function's parameter specs; no bag, two bags, missing operand and arity
mismatch are rejected; the 1.0 functions keep fixed arity and 1.0 calls
evaluate as before. Shared machinery: an empty typeId is accepted only for an
Expression varargs parameter (`JavaMethodToFunctionSpecConverter.java:195-198`),
used only by the four higher-order methods; numeric varargs still allocate
IntegerVal[]/DoubleVal[]; and, or, n-of now get Expression[], the type
they declare; no success-path change found by inspection. Runtime validation
failure and method-body EvaluationException both become Indeterminate
(processing-error via `DefaultFunctionInvocation.java:136-138`). Headers,
tabs, no console output.

Note: the tests check that a status was captured, not which status code.
