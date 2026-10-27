# RCJ1: review of cycle J (D16)

Reviewer: Codex GPT-6.1 Sol, NTM pane, read-only. Verdict: REJECT.

| Finding | Disposition |
| --- | --- |
| P2 indexed traversal loses the snapshot of a `CopyOnWriteArrayList` (Deny can become Permit) | Fixed: combiners restored to iterator loops; regression test (`tests/031-rcj1-fix`) |
| P2 copying a list without random access reads a lazy list past the first decision | Fixed: same; regression test |

No regression found in the target classes or normalization. Re-check: RCJ2.
