# Every existing arithmetic operation checked

Each row is exercised through the production DateTimeArithmeticFunctions entrypoint by
DateTimeArithmeticBoundaryTest.arithmeticPreservesSignedCalendarBoundaries; the parameter
label identifies the operation and exact operands. All 50 independent expected values and
red/green dispositions are retained in 36-arithmetic-operation-tests.json.

| XACML operation | Test parameter label | Checks | Red failures | Green result |
| --- | --- | ---: | ---: | --- |
| date-add-yearMonthDuration | `DATE_ADD_YEAR_MONTH` | 9 | 8 | All pass |
| date-subtract-yearMonthDuration | `DATE_SUBTRACT_YEAR_MONTH` | 9 | 8 | All pass |
| dateTime-add-yearMonthDuration | `DATETIME_ADD_YEAR_MONTH` | 9 | 2 | All pass |
| dateTime-subtract-yearMonthDuration | `DATETIME_SUBTRACT_YEAR_MONTH` | 9 | 2 | All pass |
| dateTime-add-dayTimeDuration | `DATETIME_ADD_DAY_TIME` | 7 | 0 | All pass |
| dateTime-subtract-dayTimeDuration | `DATETIME_SUBTRACT_DAY_TIME` | 7 | 0 | All pass |

Every operation covers positive, negative and zero durations. Calendar operations cover
leap/non-leap February clipping in both directions, negative equivalents and combined
year/month periods from February 29. Day-time operations cover month and leap-day rollover
in both directions and retain the time-zone offset.

The prior ISO8601DateTimeArithmeticTest also checks the four type-level operations:
- plus(ISO8601DayTimeDuration): addPositive/Negative/ZeroDayTimeDuration.
- minus(ISO8601DayTimeDuration): subtractPositive/Negative/ZeroDayTimeDuration.
- plus(ISO8601YearMonthDuration): addPositive/Negative/ZeroYearMonthDuration.
- minus(ISO8601YearMonthDuration): subtractPositive/Negative/ZeroYearMonthDuration.

DateTimeValueArithmeticFunctionTest.testProvidedFunctions verifies registration for all six
functions and their legacy identifiers. Those 3 tests and the prior 12 tests pass in run 35.

Time duration arithmetic and date day-time arithmetic are not defined by XACML 3.0 A.3.7
and are not supplied by the opened production class. No new operation was introduced;
ISO8601Time and the duration classes remain unchanged. See arithmetic-reference.md.
