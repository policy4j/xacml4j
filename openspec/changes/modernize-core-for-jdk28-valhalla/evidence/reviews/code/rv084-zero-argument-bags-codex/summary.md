# rv084: review of type-bag functions without arguments, run 076 (Codex)

Date: 2026-10-07. Lane: adversarial code reviewer, Codex CLI, one shot per
round (`codex exec -s read-only --skip-git-repo-check`), no tools, no
repository access. Input per round: `roundN-prompt.md` followed by the
complete diff (`DefaultFunctionInvocation`, the new test class), home path
removed. The prompts quote the `type-bag` text of A.3.10.

Final verdict: APPROVE (rounds 1 and 2).

| Round | Verdict | Notes of the reviewer | Result |
| --- | --- | --- | --- |
| 1 | APPROVE | A single real `null` argument is treated like no argument (it was before, too). A primitive component type would fail the `Object[]` cast; no such function is known. Missing test: one and two arguments. | Test added. After the round the author found that a parameter with a minimum (`integer-add`) would get an empty array and compute a value; the fix was narrowed with `isOptional()`. The round-1 prompt had wrongly said that minimums are checked before the invocation. |
| 2 | APPROVE | The `isOptional()` guard keeps the earlier handling for parameters with a minimum. Missing test: `or()` and `and()` through the registry. | Covered by `LaterDecidingOperandTest` (run 074), rows `or ""` and `and ""`. |

Limits: the reviewer saw the diff and the facts in the prompt only. It did
not run the build.
