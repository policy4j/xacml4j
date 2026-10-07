Both round-5 findings are closed in `RegExpFunctions.java`:

1. Cache size is capped at `Integer.MAX_VALUE - 1` in both property parsing and `PatternCache` construction, preserving the eviction condition.
2. `withoutQuoting` starts its builder at `length`; both property parsing and `Limits` construction enforce the reduced pattern-length maximum. The reported negative-capacity scenario is prevented.

No additional defect is demonstrable from the supplied diff and facts within the accepted scope.

Verification: static review only. The supplied reactor result is 7,785 tests, 0 failures, 0 errors, 22 skipped.

Residual limits remain: policy4j-the.4.58 is open, and compilation cannot be interrupted. Fresh-JVM property initialization remains untested.

VERDICT: APPROVE
codex exit=0
