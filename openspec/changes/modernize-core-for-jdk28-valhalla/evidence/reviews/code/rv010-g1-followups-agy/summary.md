# rv010: group 1 round 5 fix verification (Agy)

Date: 2026-10-01. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity,
Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3. Read-only.

Scope: packet 1.3i (rv007 LOW lexical scale, sub-nanosecond truncation in
DateTimeDurationUtils) and packet 1.3j (rv009 LOW: MapFunctionResolverValidator
.resolve unchecked exception; exact status codes in variadic tests).

Verdict: APPROVE. Findings: none.

Files the reviewer reports opening: DateTimeDurationUtils.java,
HigherOrderFunctions.java (MapFunctionResolverValidator), DefaultFunctionInvocation.java,
FunctionInvocationException.java, Apply.java, ISO8601DurationReviewTest.java,
ISO8601FractionalDateTimeArithmeticTest.java, HigherOrderVariadicFunctionsTest.java,
run 005 review-fixes.md and summary.md.

Reviewer statements: integral seconds keep scale 0; sub-nanosecond fractions
truncate toward zero (RoundingMode.DOWN) with the sign applied after; equality
and hash contracts unchanged; resolve delegates to the same validation and
raises the syntax-error path instead of IllegalArgumentException; variadic
tests assert exact status codes; tests evaluate through Apply.
