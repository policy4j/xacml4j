# 042 rv063 — integer-to-double, regexp substring match, XPath Content order

Review `reviews/code/rv063-g4-functions-4.3-sol` was REQUEST_CHANGES (3 MEDIUM): policy4j-the.4.38, 4.41, and 4.40. JDK: java 21.0.6 (Oracle GraalVM 21.0.6+8.1), selected with `jenv prefix 21`. Offline. License header rewrite skipped on every Maven command. Commands: `command.txt`.

The lane directory is not a git checkout, so `git status` could not be run here. Beads were not claimed, noted, or closed. `tasks.md` was not edited.

## Production

- `NumericConversionFunctions.integerToDouble` rejects a double outside `[-2^63, 2^63)` before the cast back to `long` (JLS 5.1.3 saturation). `Long.MAX_VALUE` is processing-error. `Long.MIN_VALUE` (`-2^63`) stays accepted.
- `RegExpFunctions` no longer rewrites an empty pattern to `.*` and no longer wraps the pattern in `.*`. `matches` uses `Matcher.find` on both the cache and non-cache paths. Unicode-block and character-class subtraction translation is unchanged. `string-regexp-match` and the five typed `*-regexp-match` functions already call `matches`.
- `xpath-node-equal` and `xpath-node-match` resolve both categories' Content, return false if either is absent (A.3.15), and only then evaluate either XPath. The 1.0 wrappers delegate to those methods.

## Tests

`FunctionSafetyNet43Test` adds the review rows. `2^53` and `2^53+1` stay the existing `integerToDoubleKeepsAnExactWideInteger` and `row13IntegerToDoubleRejectsAValueThatIsNotExact` tests. `RegularExpressionFunctionsTest` now expects the unpadded translation, class subtraction `[a-z-[aeiou]]` to `[a-z&&[^aeiou]]`, and an empty pattern to stay empty.

Red, before the production edits (`red.log`): `FunctionSafetyNet43Test` 109 tests, 9 failures, 6 errors, 12 skipped. `RegularExpressionFunctionsTest` 7 tests, 1 failure. `Long.MAX_VALUE` returned a double. Empty pattern vs `"\n"`, `^foo|bar` vs `xbar`, and `foo|bar$` vs `foox` were false. The five typed branch-anchor rows were false. All four XPath absence orders threw `FunctionInvocationException` ("xpath evaluation failed"). The both-present control errored with `UnnecessaryStubbingException` because evaluation threw before the second `resolveContent` stub was used.

## Counts

| Run | Log | Result |
| --- | --- | --- |
| Focused red | `red.log` | BUILD FAILURE. 116 tests, 10 failures, 6 errors, 12 skipped. |
| Focused green | `green.log` | BUILD SUCCESS. Total time 6.713 s. 116 tests, 0 failures, 0 errors, 12 skipped. `FunctionSafetyNet43Test` 109/0/12 skip. `RegularExpressionFunctionsTest` 7/0. |
| Module `clean test`, AnyURI lexical suites excluded | `module-clean-test-excluding-anyuri.log` | BUILD SUCCESS. Total time 16.327 s. Finished 2026-10-02T04:06:06-04:00. core 4576/0/17 skip, xml 53/0/0, json 21/0/1 skip, pdp 79/0/3 skip. Core is 21 above the rv056 count of 4555 (the new rows). |
| Named conformance | `conformance.log` | BUILD SUCCESS. Total time 8.694 s. Finished 2026-10-02T04:06:56-04:00. 882 tests, 0 failures, 0 errors, 23 skipped. Executed 859. RSA 17/0, XACML 2.0 375/0/12 skip, XACML 3.0 488/0/11 skip, examples 2/0. Same executed and skipped counts as rv056. |

The twelve skipped `FunctionSafetyNet43Test` methods are the same decision rows as the 042 green summary. `rfc822Name-match` of an empty pattern stays false; that row is not a regexp-match test.

## Residual risk

- Java `$` matches before a final line terminator. XPath `$` matches the end of the string unless the `m` flag is set, and these functions take no flags. `^foo$` against `"foo\n"` was not tested.
- The regexp cache is off unless `RegExpFunctions.cacheRegExps` is set. Tests ran the non-cache `find` path. The cache path uses the same `find` call and was not executed.
- No `long` converts to a double below `-2^63`, so that reject arm has no positive input. `Long.MIN_VALUE` shows the low bound is strict.
- The translator still only rewrites `\p{Is` / `\P{Is` and character-class subtraction.
- `AnyURILexicalTest` stays excluded. Reactor `clean verify` was not re-run. rv056 left xacml-test at 5 failures.
- Edits are in this lane copy. The main worktree was not modified.
