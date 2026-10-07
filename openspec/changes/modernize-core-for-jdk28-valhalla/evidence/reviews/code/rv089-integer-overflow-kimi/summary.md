# rv089: review of the non-wrapping integer arithmetic, run 080 (Kimi)

Date: 2026-10-07. Lane: adversarial code reviewer, Kimi CLI 2.1.1 with the
default model of the local configuration (its name was not recorded), one
shot per round (`kimi -p "<packet>" --output-format text`) from a directory
that held only the packet; no repository access. Input per round:
`roundN-prompt.md` followed by the complete diff (`ArithmeticFunctions.java`,
`IntegerVal.java`, `FunctionSafetyNet43Test.java`, the spec delta, and the
new `IntegerOverflowTest.java`), home path removed.

Final verdict: APPROVE in both rounds.

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | APPROVE | No HIGH or MEDIUM. LOW: the `ArithmeticException` is dropped as the cause; the public methods of `IntegerVal` now throw for callers outside this repository; the largest square inside the range is not pinned; no zero-divisor test next to the new `try`. | The cause is passed on and asserted in the test; row `integer-multiply(3037000499, 3037000499)` added; the zero-divisor rows exist (rows 39 and 43 of `FunctionSafetyNet43Test`); the note on the public methods is a limit of the run. |
| 2 | APPROVE | No HIGH or MEDIUM. LOW: `integer-add` builds its result with `XacmlTypes.INTEGER.ofAny` and `integer-multiply` with `IntegerVal.of`; the `try` blocks also cover the construction of the result value. | Not changed. The first is the form the code had before this run; the second cannot fail today (the factories do not throw `ArithmeticException`). Both are for task 5.4, which replaces the type. |

The reviewer noted in round 2 that the prompt gave the red run as 43 tests
with 25 rows inside the range, while the diff has 44 and 26. The red run
was made with the first version of the test; the row for the largest
square and the cause assertion came after round 1. The prompt did not say
so.

Other lanes on 2026-10-07: Codex was started twice on the round-1 packet
and gave no report (stopped after 40 and 25 minutes). From about 16:00 the
load average of the machine was between 26 and 116 whenever it was read.
Grok and Agy had failed in the same way on run 078.

Limits: the reviewer saw the diff and the facts in the prompts only. It ran
no build and no test. One model family reviewed this run.
