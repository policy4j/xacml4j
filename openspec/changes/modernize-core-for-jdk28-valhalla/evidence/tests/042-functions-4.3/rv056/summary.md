# 042 rv056 — function status and empty rfc822Name-match

Review `reviews/code/rv056-g4-functions-4.3-agy` was REQUEST_CHANGES. This packet fixes policy4j-the.4.43 (MEDIUM) and policy4j-the.4.39 (LOW). JDK: java 21.0.6 (Oracle GraalVM 21.0.6+8.1), selected with `jenv prefix 21`. Offline. License header rewrite skipped on every Maven command. Commands: `command.txt`.

The lane directory is not a git checkout, so `git status` could not be run here. Beads were not claimed, noted, or closed. The conductor owns policy4j-the.4.43 and policy4j-the.4.39. `tasks.md` was not edited. The disabled originals in the main worktree `AttributeResolutionSafetyNetTest` were not enabled and were not edited.

## Production

- `FunctionInvocationException` routes every constructor through `statusFor`. The first `EvaluationException` in the cause chain keeps its status. Any other failure stays processing-error.
- `DefaultFunctionInvocation.invocationCause` returns that same first `EvaluationException`. A chain with none stays the root cause.
- `RFC822Name.matches` returns false for an empty pattern before `charAt(0)`.
- `AttributeDesignatorFunctions.designator` applies the main-worktree policy4j-the.4.33 block (lines 64-70). An absent category with `mustBePresent=true` throws missing-attribute. With `mustBePresent=false` it returns an empty bag. The absent-category PDP test cannot pass without the true branch. The conductor already has this block on the main worktree.

## Tests

New lane tests, not edits of the main-worktree safety net:

- `FunctionInvocationStatusTest`: boolean-from-string `"TRUE"` stays syntax-error through Apply; attribute-designator with the attribute absent and `mustBePresent=true` stays missing-attribute through Apply; a plain `RuntimeException` stays processing-error; the first `EvaluationException` wins over a deeper non-evaluation cause and over a deeper `SyntaxException`; the message constructor stays processing-error.
- `AttributeDesignatorFunctionStatusTest`: the two disabled PDP methods, enabled only in this lane copy.
- `FunctionSafetyNet43Test` gains the empty rfc822Name-match row (expect false).
- `HigherOrderVariadicFunctionsTest` no longer pins processing-error for every invalid higher-order evaluation. Runtime validation still builds a message-only `FunctionInvocationException`, so that path stays processing-error. With validation off, invoke keeps the first evaluation exception, and these calls are syntax-error.

## Counts

| Run | Log | Result |
| --- | --- | --- |
| Focused red, core | `red.log` | 14 tests, 4 failures, 1 error. Empty pattern threw `StringIndexOutOfBoundsException`. Four status rows were processing-error. |
| Focused red, PDP | `red-pdp.log` | 2 tests, 2 failures. Both were processing-error. |
| Focused green | `green.log` | BUILD SUCCESS. rfc822 filter 6/0. `FunctionInvocationStatusTest` 8/0. PDP 2/0. |
| Higher-order oracle | `higher-order-oracle.log` | BUILD SUCCESS. 54 tests, 0 failures. |
| Bare module `clean test` | `module-clean-test.log` | BUILD FAILURE before the oracle update. xacml-core 4589 tests, 29 failures, 4 errors, 17 skipped. PDP did not run. 19 failures and 4 errors are pre-existing `AnyURILexicalTest`. 10 failures were `HigherOrderVariadicFunctionsTest`. This command was not repeated after the oracle update. |
| Module `clean test`, AnyURI lexical suites excluded | `module-clean-test-excluding-anyuri.log` | BUILD SUCCESS. Total time 16.929 s. core 4555/0/17 skip, xml 53/0/0, json 21/0/1 skip, pdp 79/0/3 skip. `FunctionSafetyNet43Test` 88/0/12 skip. `FunctionInvocationStatusTest` 8/0. `AttributeDesignatorFunctionStatusTest` 2/0. `HigherOrderVariadicFunctionsTest` 54/0. |
| Reactor `clean verify`, same exclusion | `reactor-excluding-anyuri.log` | BUILD FAILURE at xacml-test. Total time 17.885 s. Finished 2026-10-02T03:24:29-04:00. core 4555/0/17, xml 53/0/0, json 21/0/1, pdp 79/0/3, xacml-test 97/5/0. Conformance module skipped. |
| Named conformance | `conformance.log` | BUILD SUCCESS. Total time 8.402 s. 882 tests, 0 failures, 0 errors, 23 skipped. Executed 859. RSA 17/0, XACML 2.0 375/0/12 skip, XACML 3.0 488/0/11 skip, examples 2/0. Same executed and skipped counts as the previous 042 conformance run. |

The five `xacml-test` failures are the same out-of-scope oracles as `verify/summary.md`: obligation matcher ignores `Effect`; signed-zero `DoubleVal`; `AnyURI` normalization. `AnyURILexicalTest` remains out of scope. The exclusion drops `AnyURILexicalTest` and `AnyURIRequestLexicalTest`.

## Residual risk

- A bare `-pl xacml-core,xacml-pdp -am clean test` stays red on pre-existing `AnyURILexicalTest`. The green module gate uses `-Dsurefire.excludes=**/AnyURI*LexicalTest.java`.
- Reactor `clean verify` is not green. xacml-test still has those 5 failures.
- An outer `EvaluationException` now wins over a deeper `SyntaxException`. The old `statusFor` did the reverse.
- Absent category with `mustBePresent=false` returns an empty bag. The previous lane behavior was processing-error.
- Invalid higher-order calls evaluated with runtime validation off now report syntax-error.
- Main-worktree copies of the two PDP tests stay `@Disabled` until the conductor merges and enables them.
