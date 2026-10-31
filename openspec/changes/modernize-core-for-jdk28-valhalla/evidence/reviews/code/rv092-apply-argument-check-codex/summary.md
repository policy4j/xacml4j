# rv092: review of the argument check in Apply, run 083 (Codex)

Date: 2026-10-08. Lane: adversarial code reviewer, Codex CLI 0.160.1 with
the default model of the local configuration, one shot per round
(`codex exec -s read-only --skip-git-repo-check`) from a directory that
held only the packet; no repository access. Input per round:
`roundN-prompt.md` followed by the complete diff of that round, home path
removed.

Final verdict: APPROVE (round 5).

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | REQUEST_CHANGES | MEDIUM: an argument of unknown type switched the whole check off; `integer-bag-size` counted a bag of strings from a designator function whose data type is an expression. | A check at evaluation, with the values of the arguments (later removed, see round 4). |
| 2 | REQUEST_CHANGES | MEDIUM: a type error that is known at build hid behind a deciding operand; `or(true, string-equal(a, bag))` was true and a Permit rule gave Permit. | An expression that holds an expression with a type error has a type error. Kept. |
| 3 | REQUEST_CHANGES | MEDIUM: the walk over variable references stopped after 64. MEDIUM: a known violation next to an argument of unknown type was not checked in a function that evaluates its own arguments. The reviewer judged the expression-level scope sound. | The walk has no limit. A stand-in argument let the known part be checked at build (later removed, see round 4). |
| 4 | REQUEST_CHANGES | MEDIUM: with the stand-in the additional validator of a higher-order function was skipped; `or(true, any-of(string-equal, 1, <bag of unknown type>))` was true. LOW: the spec did not name the function-reference exception. | The design was simplified: an argument whose type can not be told when the expression is built is a type error. The check at evaluation, the stand-in class, and the edits of four parameter specifications and of `FunctionSpecBuilder` were removed. |
| 5 | APPROVE | No functional defect. LOW: "every expression SHALL have a type" says more than the code enforces (it is the arguments of an `<Apply>`). Missing test: the catch of another exception of the check. | Spec wording corrected; test `testCheckOfTheArgumentsThatFailsIsATypeError` added to `ApplyTest`. |

How the design moved. Rounds 1, 3 and 4 found three corners of one
mechanism: an expression that was "checked later" because the type of an
argument was not known when it was built. Each fix of a corner left
another one. After round 4 the mechanism was removed and replaced by one
rule. Its cost is stated in the run summary: the bag of a designator
function with a data type that is not a literal can no longer be used.

Also changed on the way: the maintainer said on 2026-10-08 that the
stand-in class should not return null. The class was removed with the
mechanism; the remaining new code returns `Optional`.

What the prompts got wrong or left out:

- Rounds 1 to 3 gave mutation results as "of the final code". Each was of
  the code of its round.
- The prompts give the gates of their rounds (7,939 to 7,951 tests). The
  tree as committed passes with 7,952 tests (run 083, gate 6); the
  difference to round 5 is the one test that the reviewer asked for.

Limits: the reviewer saw the diff and the facts in the prompts only. It
ran no build and no test. One model family reviewed this run. The
reviewer noted in round 5 that the XACML text supports processing-error
for invalid static data types and does not itself prescribe the check
when the expression is built, the rule for unknown types, the
inheritance of a type error, or processing-error for a wrong number of
arguments; those are readings of this engine.
