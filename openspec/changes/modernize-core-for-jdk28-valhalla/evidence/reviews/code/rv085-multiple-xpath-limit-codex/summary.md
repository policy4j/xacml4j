# rv085: review of the multiple-decision XPath expansion limits, run 077 (Codex)

Date: 2026-10-07. Lane: adversarial code reviewer, Codex CLI, one shot per
round (`codex exec -s read-only --skip-git-repo-check`), no tools, no
repository access. Input per round: `roundN-prompt.md` followed by the
complete diff (handler, public chain, new test class, spec delta, proposal
line), home path removed. The prompts quote the error statements of the
XACML v3.0 Multiple Decision Profile.

Final verdict: APPROVE in round 2.

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | REQUEST_CHANGES | MEDIUM: with selection sizes 10, 11, 0 the request has no individual request, but the running product reached 110 before the empty category was seen, so it was rejected. Notes: the WARN line holds the category id of the request; the IncludeInResult test compared sizes only. | Fixed: the sets are collected first; an empty set ends the expansion with no result; then the product is checked. Tests for both orders and for 100^5 with one empty set. The IncludeInResult test compares the attributes. |
| 2 | APPROVE | none. Missing test: the second constructor argument of the public chain. | Test added (`new MultipleResourcesHandler(100, 2)`). |

Both rounds: the arithmetic cannot overflow; no cross product is built or
iterated before the check; the unbounded repeated-categories handler is a
separate defect (bug policy4j-the.4.57).

Limits: the reviewer saw the diff and the facts in the prompt only. It did
not run the build.
