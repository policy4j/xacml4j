# Run 048: donor pre-audit for task 6.6, date, time and duration types (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High). Session `policy4j--g1`, pane 3, 60-65 tool
calls, 27 files. Read-only: donor `b9f114a3`, worktree types after the 4.10,
4.11, 4.28 and 4.31 merges. Raw report: [agy-report.txt](agy-report.txt)
(13 defects, 4 group 4 reversions, 24 test rows).

Reported top defects: Duration months()/days() erase fields when years is
undefined; ISO8601DateTime minus(dayTimeDuration) inverted; ISO8601Date
plus/minus(yearMonthDuration) calls an invalid toDuration(); Duration
compareTo returns INDETERMINATE (2); donor record equality would revert 4.28.

## Conductor spot checks (2026-10-02)

Confirmed in the donor:
- `b9f114a3:.../types/ISO8601DateTime.java:199-210`:
  `minus(ISO8601DayTimeDuration)` subtracts when the duration is negative and
  adds when it is positive (inverted; `plus` at `:176-190` uses the same
  magnitude pattern the right way round).
- `b9f114a3:.../types/Duration.java:268-270`: `compareTo` returns
  `javax.xml.datatype.Duration.compare`, which yields
  `DatatypeConstants.INDETERMINATE` (2) for incomparable durations
  (for example P1M vs P30D), violating the Comparable contract.
The reviewer's line numbers differ from `git show` output; the defects hold.
Other rows unverified.

Use: 6.6 keeps the worktree's fixed temporal types (4.10, 4.11, 4.28, 4.31
tests are the oracle) and takes donor ideas only where they pass them.
