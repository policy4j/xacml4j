# rv009: re-check of the review fixes (Codex GPT-6.1 Terra)

Date: 2026-10-27. Read-only NTM lane. Packet `packet.md`; report `report.md`.

Verdicts: A APPROVE; B APPROVE; D REJECT; E APPROVE WITH CHANGES; F APPROVE.

## Disposition (conductor, 2026-10-27)

| Finding | Disposition | Proof |
| --- | --- | --- |
| rv009 P2 D: with runtime validation, the pair path evaluated in place the list a validator had seen | fixed: in place only when validation is off; with validation the operands go to a new list, as in the generic path; one validation check per call | `FunctionInvocationCharacterizationTest.validatorListIsNotChangedByTheCall`: fails on the round-1 tree, passes before D and after |
| rv009 P3 and rv010 P3 E: an early advice or obligation view answered `contains(null)` with false | fixed with rv010 P2: the getters create the map of the asked effect and return its own values view | `responseViewsKeptBeforeTheFirstWriteSeeLaterWrites` asserts `contains(null)` throws before and after the first write |
| rv010 P2 E: `removeIf` on an early view removed through the iterator and could drop a response merged meanwhile | fixed: the four response maps are created separately on first use, and the getters return the real `values()` view, which removes conditionally | `keptViewRemovesOnlyTheTestedResponse` (latches around the predicate, merge while paused): fails on the round-1 tree, passes before E and after |
| rv009 P3 E: comment and design claimed no cache on a request-only decision; hash cache claim | fixed: the handler comment and design D8/D9 notes describe the final behavior (the designator cache and the advice and obligation maps of the decision's effect are created when the result reads them) | design.md D8, D9 implementation notes |
| rv009 P3 E: JMH evidence for the final tree | pair 017/018 (cycle F jar against the final tree) in measurement session 4 | `benchmarks/jmh/017-review-fixes-before`, `018-review-fixes-after` |

Runs: `tests/016-recheck2-fixes`, `tests/017-reactor-recheck2-fixes`.
