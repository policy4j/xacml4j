# rv083: review of the IIA005 request syntax error mapping, run 075 (Codex)

Date: 2026-10-07. Lane: adversarial code reviewer, Codex CLI, one shot
(`codex exec -s read-only --skip-git-repo-check`), no tools, no repository
access. Input: `round1-prompt.md` followed by the complete diff of the
change (harness, two manifest rows, new test class), home path removed.

Verdict: APPROVE, no confirmed defect.

| Question | Answer of the reviewer |
| --- | --- |
| Can the mapping hide a defect of another case or another syntax error? | No: it needs `special-harness`, case id `IIA005`, status syntax-error, and the exact message; the mapped response is still compared. |
| Does a `SyntaxException` from `decide` reach the mapping? | Yes in the reviewed diff, without effect on IIA005. Suggested: limit the `try` to request parsing. |
| Do the tests pin the mapping? | Yes for the named case, the disposition, and the comparison. Missing: the guard with another message. |
| Is the manifest change consistent? | Yes. |

After the review, in the same run: the `try` now holds only the request
parsing, and `acceptsOnlyTheMessageOfTheMissingAttributeId` tests the guard
directly. These two edits were not reviewed again.

Limits: the reviewer saw the diff and the facts in the prompt only. It did
not run the build.
