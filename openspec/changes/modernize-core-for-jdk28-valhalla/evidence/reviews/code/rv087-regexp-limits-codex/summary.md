# rv087: review of the regular-expression limits, run 078 (Codex)

Date: 2026-10-07. Lane: adversarial code reviewer, Codex CLI, one shot per
round (`codex exec -s read-only --skip-git-repo-check`), no tools, no
repository access. Input per round: `roundN-prompt.md` followed by the
complete diff (`RegExpFunctions.java` and the new `RegExpLimitsTest.java`),
home path removed.

Final verdict: APPROVE in round 6, with policy4j-the.4.58 open.

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | REQUEST_CHANGES | HIGH: `(?:(?:){2147483647}){2147483647}` loops without a read of the input, so the deadline never fires. MEDIUM: the compilation has no budget. LOW: the timing test allows 20 seconds. | Bound on nested counted repetitions (`repetitionWeight`), checked before the pattern is compiled; the clock starts before the compilation and is checked after it; the test asserts less than 2 s. |
| 2 | REQUEST_CHANGES | HIGH: escapes hide a repetition from the scanner (`\c[`, an empty `\Q\E`). HIGH: zero-width branching (`(?:(?=)|(?=))` 40 times, then `(?!)`) backtracks without a read of the input. MEDIUM: `[]a{1000001}]` is rejected. Missing: a test of the properties. | The scanner follows `java.util.regex` for quoting, `\cX`, and a `]` at the start of a class; the author found and closed two more ways (a quantifier directly after `(`; a `(` in a class with a leading `]`). The clock is checked after `find()`. The second HIGH is not fixed: bug policy4j-the.4.58. `Limits.from(lookup)` with tests. |
| 3 | REQUEST_CHANGES | MEDIUM: quantifiers next to each other are multiplied (`(?:){1000}{1001}` rejected). Notes: the late-result test does not isolate its check; no further way to hide a repetition found; the partial scope is accepted. | A quantifier after a quantifier has weight 1 as its base. The late-result test uses given clock readings (`matches` with a clock). The rules of the scanner are shown on `java.util.regex` by what patterns match. Mutation check. |
| 4 | REQUEST_CHANGES | MEDIUM: with `.maxRepetition` at `Long.MAX_VALUE` a saturated weight is accepted. LOW: the property test does not exercise the time limit. | The largest weight is rejected whatever the limit is. The property test checks the time limit through the clock. The author moved `.cacheSize` to the same check as the limits. |
| 5 | REQUEST_CHANGES | MEDIUM: `.cacheSize` at `Integer.MAX_VALUE` switches eviction off. LOW: `new StringBuilder(2 * length)` overflows for a pattern of 2^30 characters under the largest pattern limit. | Largest cache size `Integer.MAX_VALUE - 1`, largest pattern length `Integer.MAX_VALUE / 8`; the builder starts with `length`. |
| 6 | APPROVE | none. Remaining limits named: policy4j-the.4.58 open; compilation can not be interrupted; no fresh-JVM test of the properties. | - |

A first start of round 2 gave no report: the network dropped while the
reviewer ran. `round2-prompt.md` is the packet of the second start.

Between rounds 2 and 3 the author removed the scanner from the working
tree and put it back before any test or review ran on that state
(`tests/078-regexp-limits/summary.md`, "Course of rounds 2 to 5").

The prompts state that `or(regexp-match(slow), true)` is true after at most
one time limit. That holds only for a match that reads its input (see
policy4j-the.4.58).

No second reviewer: three attempts with other model families on the
approved diff gave no report (`tests/078-regexp-limits/summary.md`,
"Review").

Limits: the reviewer saw the diff and the facts in the prompts only. It
ran no build, no test, and no probe; the measurements in the prompts are
the author's. Its statement that no further way to hide a repetition was
found is a result of reading, not a proof.
