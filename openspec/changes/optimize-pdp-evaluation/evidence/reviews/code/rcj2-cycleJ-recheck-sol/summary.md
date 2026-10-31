# RCJ2: re-check of cycle J

Reviewer: Codex GPT-6.1 Sol, NTM pane, read-only. Verdict: REJECT (one new P2).

| Finding | Disposition |
| --- | --- |
| RCJ1 P2 combiner snapshot | resolved (reviewer) |
| RCJ1 P2 eager combiner copy | resolved (reviewer) |
| P2 new: normalization indexes caller lists and loses a copy-on-write snapshot | Fixed: index only lists read the same way; copy others through their iteration; test red then green (`tests/032-rcj2-fix`) |

Target classes: no concern (their lists are built and owned as immutable lists). Re-check: RCJ3.
