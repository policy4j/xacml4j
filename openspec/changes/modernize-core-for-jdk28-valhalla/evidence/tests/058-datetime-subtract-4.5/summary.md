# Task 4.5: date and dateTime duration arithmetic

Result: the full JDK 21 reactor `clean verify` passed: **7,136 tests, 0 failures, 0 errors, 39 skips**.
All **864 enabled conformance cases passed**, with 16 inherited case skips.
The gate completed at 2026-10-02T05:07:45-04:00 in 29.362 seconds.
JDK: `java version "21.0.6" 2025-01-21 LTS`; Oracle GraalVM
`21.0.6+8-LTS-jvmci-23.1-b55` ([runtime receipt](jdk.json)).

## Changes and red/green proof

- `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java:50`:
  retain whether the input timezone was absent. String/StringVal and
  XMLGregorianCalendar factories preserve that state; all six add/subtract
  overloads carry it forward; lexical output omits the offset when it was absent.
- `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeArithmeticRegistryTest.java:57`:
  736 evaluations through Apply and the default provider, covering all six
  required operations, canonical 3.0 and legacy 1.0 URNs, and runtime validation
  both off and on.
- `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeZonePreservationTest.java:50`:
  27 factory, formatting, signed Java Duration, negative-year, and comparison controls.
- `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeTest.java:69`:
  update the exact unzoned lexical expectation to omit the former extra Z.
  All existing component and internal UTC-offset assertions remain. The class
  retains all eight tests. This corrects an expectation that pinned the defect.

The original registry characterization was **736 tests / 120 failures / 0 errors**.
Before production changes, the resumed focused run was **771 / 128 / 0 / 0 skips**:
120 registry failures, seven factory/overload failures, and the corrected legacy
lexical assertion. Every failure was an unzoned dateTime acquiring Z.
The focused green run was **771 / 0 / 0 / 0**, and all three classes also passed
the final clean reactor gate. Receipts: [red](02-red-results.json),
[green](03-green-results.json), and [final](05-reactor-results.json).

Existing arithmetic signs and month-end results were already correct when this
task resumed. No arithmetic sign branch needed a new production fix.
The new fix is timezone preservation, which the task explicitly requires.

## Operations and specification rule

Each literal row runs with timezone absent, Z, -05:00, and +05:30, under both
URN versions and both runtime-validation settings: 16 evaluations per row.

| Operation suffix, under both function URN prefixes | Literal rows | Registry evaluations |
| --- | ---: | ---: |
| dateTime-add-dayTimeDuration | 7 | 112 |
| dateTime-subtract-dayTimeDuration | 7 | 112 |
| dateTime-add-yearMonthDuration | 8 | 128 |
| dateTime-subtract-yearMonthDuration | 8 | 128 |
| date-add-yearMonthDuration | 8 | 128 |
| date-subtract-yearMonthDuration | 8 | 128 |
| Total | 46 | 736 |

All operations have positive, negative, and zero-duration controls. Day-time
rows also cover crossing a date boundary and fractional seconds.
Year-month rows cover January 31 + P1M -> February 28,
March 31 - P1M -> February 28, leap day 2004-02-29 + P1Y -> 2005-02-28,
and negative mirrors. Mixed periods pin the day once after combining years and
months: 2020-02-29 + P1Y1M -> 2021-03-29, not March 28.

The rules come from [XACML 3.0 A.3.7](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html),
[XML Schema Part 2 Appendix E](https://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes),
and [XPath F&O 1.0 sections 10.8.4–10.8.10](https://www.w3.org/TR/2007/REC-xpath-functions-20070123/#func-add-yearMonthDuration-to-dateTime):
subtract by negating the signed duration; combine the year/month change before
pinning the day; preserve the timezone, including absence.

## Comparison with a8a90d46

The seven historical sources were read with `rtk proxy git show`, archived,
checked byte-for-byte against Git, compiled unmodified on JDK 21, and loaded ahead
of current infrastructure classes. The probe asserts each historical class's
code-source location. This is a scoped historical arithmetic probe, not a full
historical reactor build. Current infrastructure/dependency jars supply the other
types. The arithmetic wrapper source is still byte-identical to a8a90d46;
both URNs delegate to the same operation.

[baseline-comparison.csv](baseline-comparison.csv) compares every one of the
184 input/zone combinations with the current expected result, independently
asserted in all four registry contexts. [Probe receipt](04-historical-arithmetic-probe.log);
[source hashes](historical-source-hashes.json);
[archive checks](historical-source-archive-check.json).

| Operation | Historical results equal to current | Different | Historical exception results |
| --- | ---: | ---: | ---: |
| dateTime-add-dayTimeDuration | 15 | 13 | 0 |
| dateTime-subtract-dayTimeDuration | 3 | 25 | 0 |
| dateTime-add-yearMonthDuration | 18 | 14 | 0 |
| dateTime-subtract-yearMonthDuration | 18 | 14 | 0 |
| date-add-yearMonthDuration | 1 | 31 | 0 |
| date-subtract-yearMonthDuration | 1 | 31 | 0 |

Historical defects confirmed by the probe:

- `ISO8601DateTime.minus(ISO8601DayTimeDuration)` inverted the sign branch:
  08:00Z minus PT1H produced 09:00Z; minus -PT1H produced 07:00Z.
  The current signed Duration subtraction was already repaired before 4.5.
- Historical day-time parsing/getters discarded fractional seconds.
- Sequential dateTime year/month adjustments prematurely clamped leap days:
  2020-02-29 + P1Y1M produced 2021-03-28.
- Historical date year/month arithmetic converted the duration to zero
  day-time Duration and left the date unchanged, rather than throwing.
  Date parsing also discarded explicit offsets.
- Historical unzoned dateTime parsing added Z. This last defect persisted
  until the timezone-preservation fix in this run.

The first four defects were already repaired by earlier approved work.
Pure year-month sign branches were correct in the baseline; their mixed-period
boundary calculation was not.

## Final reactor and conformance gate

Command: see [command.txt](command.txt).
All Maven Results sections and per-class/module count lines are retained in
[surefire-summary.txt](surefire-summary.txt); [structured counts](05-reactor-results.json)
include every reported class.

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 5,754 | 0 | 0 | 17 |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 |
| xacml-xml | 244 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 98 | 0 | 0 | 5 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 917 | 0 | 0 | 16 |
| Total | 7,136 | 0 | 0 | 39 |

| Suite | Passed cases | Failed cases | Skipped cases |
| --- | ---: | ---: | ---: |
| OASIS 2.0 | 365 | 0 | 9 |
| OASIS 3.0 | 480 | 0 | 7 |
| RSA 2008 | 17 | 0 | 0 |
| Examples | 2 | 0 | 0 |
| Total | 864 | 0 | 16 |

OASIS 2.0 has 361 standard executed rows and four enabled special-harness rows.
OASIS 3.0 has 477 standard executed rows, three enabled special-harness rows,
one disabled special-harness row (IID029), and six skipped-disposition rows.
RSA and examples execute all rows. The 917 Surefire count includes 880 manifest
cases, two suite helper tests, and 35 guard/harness unit tests.

The collector uses only the four suite reports for case receipts, excluding
the repeated IIA004/IIC012 passes from static-rejection harness unit controls.
Every enabled manifest row has a unique PASS receipt; disabled rows match
the actual suite skip counts. [Per-case table](conformance-case-results.csv).

No case disposition changed in 4.5. OASIS 2.0 IIIC001 remains enabled and passes;
its preceding authorized manifest change is recorded separately in
[run 066](../066-oasis20-iiic001/summary.md). The XXE and boolean/static-rejection
repairs also pass this combined gate; their proof remains in
[rv064-fix](../051-static-rejection-jaxb-boolean/rv064-fix/summary.md).
Run 064 was not recreated.

## Scope and residual risk

The task changes four source/test files plus this evidence directory.
[changes.patch](changes.patch) is relative to the task-start snapshots, so it
does not mix in earlier lane edits. [Scope review](scope-review.json) confirms
existing license headers are unchanged and new tests copy the local LGPL header.
The scoped `rtk git diff --check` passed.

ISO8601Date, DateTimeArithmeticFunctions, and DateTimeDurationUtils are
byte-identical to their task-start hashes. The shared ISO8601Time edit came from
another lane and was preserved. AnyURI, ISO8601Time, BaseJAXBUnmarshaller,
providers, registries, POMs, and fixtures were not edited by 4.5.
All 550 Java/POM/manifest gate inputs remained byte-identical throughout the
final gate ([input stability](05-reactor-input-stability.json)).

Unzoned values retain the pre-existing internal UTC comparison default.
Comparison, equality, and hash methods are byte-identical to the task-start
source, and a focused control asserts that convention. The change preserves
absence in lexical output and arithmetic; it does not alter temporal equality.
XML and JSON dateTime serializers use TypeToString/DATETIME, which delegates
to this lexical method; the full marshalling suites pass.

There are 39 inherited test skips, including 16 tracked conformance skips;
none of the new tests is skipped. No additional failing result remains.
No performance claim is made; a benchmark was not requested for this semantic
repair. No commit, beads operation, task tick, or issue closure was performed.
