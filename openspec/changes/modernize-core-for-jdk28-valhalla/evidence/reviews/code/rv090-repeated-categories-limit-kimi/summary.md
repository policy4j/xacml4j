# rv090: review of the limit for the multiple-decision expansion, run 081 (Kimi)

Dates: 2026-10-07 and 2026-10-08. Lane: adversarial code reviewer, Kimi
CLI 2.1.1 with the default model of the local configuration (its name was
not recorded), one shot per round
(`kimi -p "<packet>" --output-format text`) from a directory that held
only the packet; no repository access. Input per round: `roundN-prompt.md`
followed by the complete diff (the four changed handler classes, the new
`MultipleResourcesBudgetHandler.java`, the spec delta, and the new
`MultipleResourcesRepeatedCategoriesLimitTest.java`), home path removed.

Final verdict: APPROVE (round 2).

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | REQUEST_CHANGES | MEDIUM-1: with selectors that select no node, limit references x limit repeats made limit x limit selector steps with no evaluation and an empty response; the budget counted evaluations and results only. LOW-1: the count of the results inside the loop of the references handler was redundant with the count in the budget handler, and the reported mutation M3 could not have been detected by the code shown. LOW-2: four missing tests. Remark: `getRequestHandlers()` of the wrapper returns the real chain. | A second counter and the `Checkpoint` handler count the individual requests that reach the selector step (test red first, run 081 red 3). The count in the loop is removed; the mutation check was run again on the final code (four mutations, all detected). The four tests are added. A comment on the override. |
| 2 | APPROVE | No HIGH or MEDIUM. LOW-1: the spec scenario "101 `<Attributes>` elements of one category" is Indeterminate only when the elements differ; equal elements count once. | The three scenarios say "different"; a scenario "Equal elements count once" is added. |

The reviewer's bound for one request after round 1, with N the limit: at
most N request references resolved, at most N + 1 individual requests
built (the last one trips the counter), at most N runs of the legacy and
XPath handlers, at most N evaluations, at most 2N results before the
final count and N in the response.

What the prompts got wrong or left out:

- Round 1 gave the mutation result M3 of a version that did not yet count
  the results in the budget handler. The reviewer found the
  contradiction (LOW-1).
- Round 1 gave the gate of its version of the code as 7,876 tests; round
  2 gave 7,887 for the final handler code. The tree as committed passes
  with 7,889 tests (run 081, gate 5). The differences are tests of this
  bead and of bead policy4j-the.4.60, which was in the tree for every
  gate of 2026-10-08.
- After round 2 the spec text changed (the reviewer's LOW-1) and one
  Javadoc sentence of `MultipleResourcesBudgetHandler`. No code changed.

Earlier attempt, 2026-10-07: Kimi was started on the first version (limit
per expansion step, no budget). It was stopped after about 50 minutes
without a report. Its working notes named the two gaps that led to the
budget: the request-references handler was not bounded, and "limit times
limit" was not a bound for the chain. The load average of the machine was
between 26 and 116 that evening.

Limits: the reviewer saw the diff and the facts in the prompts only. It
ran no build and no test. One model family reviewed this run.
