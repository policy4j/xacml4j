# ISO date GregorianCalendar month correction

Task 5.8 (`policy4j-the.5.8`), Codex GPT-6 Luna, worktree `cod_4`.

## Change

- Replaced the time-dependent `Calendar.getInstance()` assertion with fixed
  GregorianCalendar outcomes for January 31, October 31, December 31, and
  leap-day February 29. Each checks the expected year, 1-based month, and day.
- Removed both `System.out` calls.
- `DateTimeDurationUtils.toISO860Date(GregorianCalendar)` now adds one to
  `Calendar.MONTH` when constructing the `LocalDate`.

## Caller search

Searched `ofAny`, `of`, and `toISO860Date` usages with Calendar or
GregorianCalendar inputs across core, PDP, JSON, XML, test-support, and
conformance modules. The date-specific path is
`ISO8601Date.ofAny(GregorianCalendar)` -> `ISO8601Date.of(GregorianCalendar)`
-> `DateTimeDurationUtils.toISO860Date(GregorianCalendar)`. No separate caller
was found that relies on the old shifted date. Other Calendar `of` usages found
are for dateTime or time types, which do not use this date conversion.

## Runs

All builds used the shared `run-build.py` runner, `--maven /opt/homebrew/bin/mvn`,
JDK 25, offline mode, and `-Dlicense.skipUpdateLicense=true`. Each run used
`clean`; the runner recorded exit codes in the corresponding fresh log.

| Log | Result |
| --- | --- |
| `focused-red-mvn310-clean.log` | Exit 1, expected semantic red: 7 tests; 1 failure and 3 errors. January rejected month 0, October 31 and December 31 became invalid prior-month dates, and February 29 became month 1. |
| `focused-green-mvn310-clean.log` | Exit 0: `ISO8601DateTest`, 7 run; 0 failures/errors/skips. |
| `core-suite-mvn310-clean.log` | Exit 0: `xacml-core clean test`, 7,368 run; 0 failures/errors; 2 skipped. |

`rtk git diff --check` passed. JMH does not apply to this correctness fix.
No task checkbox or Beads state was changed. Full reactor/conformance checks
were not part of this task's requested gate.
