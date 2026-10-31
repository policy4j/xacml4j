# rv081: review of "a deciding operand wins", run 074 (Codex)

Date: 2026-10-07. Lane: adversarial code reviewer, Codex CLI, one shot per
round (`codex exec -s read-only --skip-git-repo-check`), no tools, no
repository access. Input per round: `roundN-prompt.md` followed by the
complete diff of the change (three production files, `DecidingOperands`,
the two test classes, the spec delta), home path removed. The prompts quote
A.3.5 and summarize A.3.12 of XACML 3.0 Plus Errata 01.

Final verdict: APPROVE in round 3.

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | REQUEST_CHANGES | MEDIUM: an operand that a later value overrules has recorded its status in the context; `and(or(M, T), E)` could report the status of `M`. Missing row: quantifier order with a predicate that tells the two orders apart. | No code change: `Condition.evaluate` overwrites the status with the one of the failure that escapes. Test `LaterDecidingOperandPdpTest` added; quantifier test added. |
| 2 | REQUEST_CHANGES | MEDIUM: a Permit rule with condition `or(M, T)` and an obligation that fails would report missing-attribute. Missing rows: `n-of` with a failed integer; number of predicate applications. | The finding rested on a wrong statement in the round-2 prompt (written by the conductor): obligations and advice are evaluated in `BaseDecisionRule`, which overwrites the status before it throws again. No code change. Tests for an obligation and an advice added; both rows added. |
| 3 | APPROVE | none. Notes: `nof` now takes `Expression` as its first Java parameter, which breaks direct Java callers compiled against the old signature; one more row (first failure missing-attribute, second processing-error, no deciding value). | Row added for `any-of`, `all-of`, `all-of-all`. The signature note is in the limits of run 074. |

Confirmed by the reviewer in every round: `atLeast` has no off-by-one
error; the six higher-order functions implement the quantifiers of A.3.12
(`all-of-any` for each value of the first bag, `any-of-all` for each value
of the second bag); reuse of the `parameters` array is safe; a failed
`n-of` integer escapes at once.

Limits: the reviewer saw the diff and the facts in the prompt only. It did
not run the build. It could not see the invocation layer
(`DefaultFunctionInvocation`, `FunctionSpecBuilder`) and said so.
