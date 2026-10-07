# rv073: task 4.5 review, dateTime/date add/subtract and zone preservation (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 23 tool calls. Read-only: main worktree and run 058
evidence (author Sol/Codex).

Verdict: APPROVE (no findings).

- Arithmetic: all six add/subtract functions, 3.0 and 1.0 URNs, signed
  day-time durations, month-end pinning and leap days per XPath F&O.
- Zone preservation: an unzoned dateTime stays unzoned in results and in
  toXacmlString; zoned values keep their offset; equality still compares
  instants with unzoned anchored to UTC (equal, compareTo 0, same hashCode as
  the Z form; `ISO8601DateTimeZonePreservationTest`).
- a8a90d46 claim confirmed: `minus(ISO8601DayTimeDuration)` had inverted
  branches (`a8a90d46-ISO8601DateTime.java.txt:190-199`); it also truncated
  fractional seconds and clamped leap days early. The probe
  (`HistoricalArithmeticProbe.java`, log 04) compiled the historical sources
  and ran all 184 rows against them (`baseline-comparison.csv`).
- Scope: compareTo/equals/hashCode byte-identical to the baseline;
  ISO8601Date, DateTimeArithmeticFunctions and DateTimeDurationUtils
  unchanged; one existing test expectation that pinned the Z defect
  (`ISO8601DateTimeTest.java:69`) corrected.
