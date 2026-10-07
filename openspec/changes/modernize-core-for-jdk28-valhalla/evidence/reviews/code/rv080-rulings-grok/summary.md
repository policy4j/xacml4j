# rv080: adversarial review of the ruled semantics, runs 071 to 073 (Grok)

Date: 2026-10-07. Lane: adversarial code reviewer, Grok Build grok-4.7
(high effort), one shot per round
(`grok --prompt-file ... --permission-mode plan`), run from a scratch
directory that held only the packet. The packets are the ones of rv079
(`../rv079-rulings-codex/roundN-prompt.md` plus the same diff). Scope: runs
071, 072, 073.

Final verdict: APPROVE in rounds 4 and 5.

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | no report | The run was stopped by the conductor after about 10 minutes without output. Later runs showed that a review of this size takes 7 to 14 minutes, so the stop was too early. | Started again as round 2 on the round-2 diff. |
| 2 | REQUEST_CHANGES | MEDIUM: a selector `PathEvaluationException` kept its status message. MEDIUM: an already wrapped `processing-error` skipped the fixed message. MEDIUM: the attribute PIP tests contradicted `emptyUnlessFailed`. | Fixed with the round-2 findings of rv079. |
| 3 | REQUEST_CHANGES | HIGH: in the content loop of `DefaultPolicyInformationPoint`, content with an empty selection ended the loop and dropped the failure of an earlier resolver. MEDIUM: a PIP could report `syntax-error` with its own text. | Fixed: both loops return only a present bag and decide with `emptyUnlessFailed` at the end; the handler maps every PIP exception. |
| 4 | APPROVE | none | - |
| 5 | APPROVE | none | - |

Round 5 reviewed the tree before the round-5 fix of rv079 (the removal of
the `getMessage()` call and one test). Grok did not review that last
change; Codex verified it in round 6 of rv079.

Named and accepted by the reviewer as a known gap: the `attribute-selector`
function with an entity argument evaluates the path on that entity outside
the context handler and keeps the status and message of the path error.

Limits: the reviewer saw the diff and the facts in the prompt only. It did
not run the build.
