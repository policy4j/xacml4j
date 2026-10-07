# Run 070: maintainer rulings on the run 061 decisions

Date: 2026-10-07. Scope: preparation for gate 4.7 and the beads listed below.
JDK: NOT APPLICABLE (this run changes no code, test, or spec).

The maintainer was asked how to rule on the 18 pending decisions in
`builds/061-pending-decisions/summary.md` and selected:

> Accept all recommendations

This accepts the "Rec." option of run 061 for every decision that was still
open. Three decisions were already ruled on 2026-10-04 in
`reviews/plan/rv077-resume-ntm` and implemented in
`tests/069-task46-spec-compliance`. That earlier ruling stands. For D2 it
differs from the run 061 recommendation.

| # | Ruling | Ruled in | Unblocks |
| --- | --- | --- | --- |
| D1 | (a) integer-divide returns xs:integer, quotient toward zero | rv077; run 069 | done in task 4.6 |
| D2 | (b) IEEE: double-equal(NaN, NaN) is false; -0.0 equals 0.0. Run 061 recommended (a); rv077 selected specification compliance | rv077; run 069 | bead the.4.29: check against run 069 |
| D3 | (b) a PIP resolver failure is Indeterminate processing-error regardless of MustBePresent (fail closed) | this run | bead the.4.34; two disabled PDP tests |
| D4 | (a) integer overflow outside 64 bits is Indeterminate processing-error | this run | task 5.4 |
| D5 | (a) round and floor follow IEEE: NaN stays NaN, INF stays INF, ties to even | this run | row 60 of the 4.3 safety net |
| D6 | (a) double-to-integer of NaN or INF is Indeterminate processing-error | this run | row 12 of the 4.3 safety net |
| D7 | (a) integer-mod by zero is Indeterminate processing-error (current behavior) | this run | row 43 of the 4.3 safety net |
| D8 | (a) keep the ipAddress set functions registered at 1.0 as extensions | this run | extension test |
| D9 | (a) x500Name-match compares terminal RDN sequences with x500Name-equal | this run | bead the.4.44 |
| D10 | (a) on-permit-apply-second implements the CS01 profile and is registered | rv077; run 069 | done in task 4.6 |
| D11 | (a) or, and, n-of: a later deciding value wins over an earlier error (A.3.5) | this run | bead the.4.12 |
| D12 | (a) keep PDP.decide(RequestContext); map an unmarshal SyntaxException to an Indeterminate syntax-error response in the adapter or harness | this run | bead the.4.17; IIA005 |
| D13 | (a) document hierarchical resource scope and legacy 2.0 request XPath as unsupported | this run | beads the.4.18, the.4.20 |
| D14 | (a) keep the engine; skip IIIG300 with the adjudicated reason (Plus Errata 01 5.48) | this run | bead the.3.9 |
| D15 | keep IIA023 and IID029 skipped with the adjudicated reasons | this run | bead the.3.9 |
| D16 | (a) configurable bound on multiple-resources XPath expansion with a safe default; exceeding it is Indeterminate | this run | bead the.4.50 |
| D17 | (a) bound regex input length and evaluation time; bounded LRU pattern cache | this run | bead the.4.51 |
| D18 | (a) a non-syntax XPath evaluation error is Indeterminate processing-error whatever MustBePresent is; missing-attribute only for an empty selection with MustBePresent=true | this run | bead the.11 |

Scope: the rulings only. Each ruling is implemented under its bead or task
with a failing test first, the spec delta, the reviews, and the gate
(docs/ai/OPENSPEC.md, "Apply loop"). Gate 4.7 stays open until those items
and beads the.4.53 and the.4.54 are done.

Each bead above carries a comment that names its ruling and this run.

Recorded by Claude Code (claude-opus-5-5) on the maintainer's instruction.
