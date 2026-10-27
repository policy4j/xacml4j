# RVH2: re-check of cycle H round 2

Reviewer: Codex GPT-6.1 Sol, NTM pane, read-only. Verdict: APPROVE WITH CHANGES.

| Finding | Disposition |
| --- | --- |
| RVH1 P2 custom-type retention | resolved (reviewer) |
| RVH1 P2 custom `bagBuilder()` bypass | resolved (reviewer) |
| RVH1 P3 laziness test | resolved (reviewer) |
| P2 new: `BagOfValues.hashCode` read its cache field twice, so a racing reader could get 0 (JDK-8166842 pattern); shared empty bags make concurrent first hashes routine | Fixed: one read into a local; D14 text corrected; stress test `EmptyBagCharacterizationTest.concurrentFirstHashesOfOneBagAgree` (`tests/022-rvh2-hash-fix`) |

Not a finding: publication of a shared bag (final fields, JLS 17.5) and serialization of the enum
cache field (enum constants serialize by name).
