# rv053: engine bugs 4.24, 4.28, 4.30, 4.31, 4.32 review, lane g4eq (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (report self-label unreliable). Session
`policy4j--g1`, pane 3, 20 tool calls. Read-only: lane patch (546 lines, 14
files), lane and baseline sources, run 039 evidence logs.

Verdict: APPROVE (no findings).

- `Value.java:125-155`: equals/hashCode no longer final; other value types
  inherit the previous behavior; ISO8601Date, ISO8601DateTime and
  ISO8601Time override them on UTC-normalized values with matching hashes;
  compareTo consistent with equals.
- 4.28: 2002-10-10+14:00 vs 2002-10-10-12:00 not equal; same instant at
  different offsets equal; unzoned equals Z (implicit timezone UTC, XPath
  F&O 10.4); dates keep their offset in toXacmlString; arithmetic keeps
  zone metadata; 47 temporal regression tests (4.10/4.11) pass.
- 4.30: Locale.ROOT lower-casing; Straße vs STRASSE false, ÄB vs äb true;
  string-equal unchanged.
- 4.31: `[+-]?P(\d+Y)?(\d+M)?` with at least one component; P30D, PT1H,
  P1Y2M3D, P1.5Y, P rejected with syntax-error.
- 4.32: `((v == null || !v.isPresent()) && isMustBePresent())`.
- 4.24: `getOtherAttributes().putAll(...)` on the assignment; obligation and
  advice tested; IIIA030/IIIA330 pass when enabled.
- Red-then-green receipts for all five; no manifest or fixture change.

Merged into the main worktree on 2026-10-02 02:06 (no file had changed in
the worktree since the lane cut). The adversarial review is pending (Grok).
