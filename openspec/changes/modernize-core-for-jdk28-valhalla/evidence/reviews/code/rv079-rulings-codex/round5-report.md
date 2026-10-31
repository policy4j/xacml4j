1. **MEDIUM — checked exceptions thrown by `getMessage()` can still bypass mapping.**  
   File: `xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceEvaluationException.java:148`.

   `messageOf` catches only `RuntimeException`. An overridden `getMessage()` can throw a checked exception through a generic sneaky-throw helper, without reflection.

   Concrete scenario: a selector PIP throws a `RuntimeException` whose `getMessage()` throws a checked exception `C`. Give `C` a cause containing an `EvaluationException` with `syntax-error` and back-end text. Make the first call to `C.getMessage()` throw `C` again, and subsequent calls return normally.

   The first mapping attempt in `resolveViaPip` escapes with `C`. The enclosing `doPipResolve` catches `C`, but its mapping attempt also escapes. Neither attempt constructs the mapped failure. The outer selector catch handles only `PathEvaluationException`, so `C` escapes through the `attribute-selector` function. Under the supplied `FunctionInvocationException` behavior, the resolver-selected `EvaluationException` in its cause chain supplies the response status and text.

   **Fix:** avoid calling the foreign exception’s `getMessage()` in these factories; use its class name for diagnostics. Alternatively, guard against checked exceptions as well. Add a PDP-level `attribute-selector` regression for this scenario.

The round-4 fix handles the tested runtime exceptions, but does not fully satisfy the stated trust boundary. Apart from this bypass, the shown resolver loops preserve failures when no resolver supplies a value, and ordinary absence still follows `MustBePresent`. I found no assertion weakened beyond the rulings.

Verification was limited to the supplied diff and facts. The passing reactor result was supplied, not independently verified.

VERDICT: REQUEST_CHANGES
codex exit=0
