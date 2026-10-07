# Review fixes: round 2

Both MEDIUM findings are repaired. Focused tests were written and run before
production edits. Run 48 has 97 arithmetic tests / 35 failures; run 49 has
24 selector/expansion tests / 1 failure. The unchanged production hashes were
checked when recording those red reports in 49-review-red-tests.json.
Runs 51 and 52 repeat the same commands: 97/97 and 24/24 pass.
Run 53 repeats them in the full JDK 11 reactor: 812 tests / 0 failures /
0 errors / 9 existing skips. Compiler diagnostics: zero in runs 48–53.

| Finding / original file:line -> repair | Tests written first | Red -> green |
| --- | --- | --- |
| MEDIUM context node; DefaultEvaluationContextHandler.java:127–134 before review -> branch only on a null ContextSelectorId; resolve directly against Content in that branch. Keep entity.resolve for an explicit ID. | DefaultEvaluationContextHandlerTest.testRelativePathWithoutContextSelectorUsesDocumentNode changes the requested relative-path assertion to an empty integer bag; testRelativePathWithExplicitContextSelectorUsesSelectedNode asserts 555555 with the same path and explicit ID. | No-ID test fails in run 49 (555555 instead of empty bag); explicit-ID test already passes. Both pass in runs 52/53. |
| MEDIUM fractional seconds; ISO8601DateTime.java:162–184 and DateTimeDurationUtils.java:210–228, :249–259 before review -> retain BigDecimal SECONDS during lexical parsing and XML-to-Java conversion, then apply signed whole seconds plus nanos through plus/minus(java.time.Duration). | ISO8601FractionalDateTimeArithmeticTest.addPreservesFractionalSeconds and subtractPreservesFractionalSeconds; ISO8601DurationReviewTest's four fractional preservation methods. | All 30 parameterized arithmetic checks and all four fraction-preservation methods fail in run 48; all pass in runs 51/53. |
| Conditional reviewer note, programmatic P14M; DateTimeDurationUtils.java:244 before review -> add normalized d.getMonths(), rather than the original 14 months, to the normalized year count. | ISO8601DurationReviewTest.programmaticFourteenMonthsAreCountedOnce checks raw getMonths()==14, conversion to +14/-14 total months, and dateTime add/subtract. | Run 48: expected 14, actual 26. Runs 51/53: pass. ISO8601YearMonthDuration itself is unchanged. |

The fraction matrix covers PT1.5S, PT0.5S and PT0.001S, each positive and
negative, addition and subtraction, both whole-second and .250 base values.
It also covers month-end rollover, combined days/hours/minutes/seconds,
offset preservation and a one-nanosecond duration. Round-trip, decimal
equality, hash equivalence, ordering and XML-to-Java conversion checks pass.
53-review-green-tests.json and the stripped green XML reports retain every
test name. 53-review-fixes.patch contains exactly this review's six-file delta.

## Every arithmetic operation checked

The unchanged DateTimeArithmeticBoundaryTest tests all six functions defined
by XACML 3.0 Appendix A.3.7 through DateTimeArithmeticFunctions; all 50 pass
again. The twelve existing ISO8601DateTimeArithmeticTest checks also pass.

| Operation | Test / parameter family | Checks |
| --- | --- | ---: |
| date-add-yearMonthDuration / ISO8601Date.plus | arithmeticPreservesSignedCalendarBoundaries[DATE_ADD_YEAR_MONTH, ...] | 9 |
| date-subtract-yearMonthDuration / ISO8601Date.minus | arithmeticPreservesSignedCalendarBoundaries[DATE_SUBTRACT_YEAR_MONTH, ...] | 9 |
| dateTime-add-yearMonthDuration / ISO8601DateTime.plus | arithmeticPreservesSignedCalendarBoundaries[DATETIME_ADD_YEAR_MONTH, ...] | 9 |
| dateTime-subtract-yearMonthDuration / ISO8601DateTime.minus | arithmeticPreservesSignedCalendarBoundaries[DATETIME_SUBTRACT_YEAR_MONTH, ...] | 9 |
| dateTime-add-dayTimeDuration / ISO8601DateTime.plus | arithmeticPreservesSignedCalendarBoundaries[DATETIME_ADD_DAY_TIME, ...]; addPreservesFractionalSeconds[...] | 7 + 15 |
| dateTime-subtract-dayTimeDuration / ISO8601DateTime.minus | arithmeticPreservesSignedCalendarBoundaries[DATETIME_SUBTRACT_DAY_TIME, ...]; subtractPreservesFractionalSeconds[...] | 7 + 15 |

The calendar checks cover positive/negative/zero periods, leap and non-leap
month ends, and combined year/month boundaries. The programmatic P14M test
adds both signs and both dateTime directions. The standard defines no time
duration arithmetic functions and no date/day-time duration functions; none
were added. Existing arithmetic evidence and operation inventories are kept.

## Explicitly retained gaps and reviewer notes

* DefaultEvaluationContextHandlerTest.testZeroNodeContextSelectorCurrentlyFallsBackToDocument
  and testTwoNodeContextSelectorCurrentlyUsesFirstNode pass before/after and
  record unsupported cardinality behavior. Zero nodes fall back to the
  document; two nodes select the first and yield 555555. XACML 3.0 section
  7.3.7 requires Indeterminate syntax-error. The packet explicitly permits
  recording this gap; XmlContent.java and XPathProvider are unchanged.
* MultipleResourcesViaXacmlXPathHandlerTest's six tests pass before/after and
  in the full gate. IIIE301–303 are not part of the configured OASIS loops;
  direct unchanged-fixture checks run before/after and fail identically.
  Their classifications and concrete repair proposals are in review-proposals.md.
* ISO8601Duration.getSeconds():59–60 still returns integer 0 for PT0.5S.
  Its isZero() delegates to DateTimeDurationUtils.isZero():74–81, which still
  classifies PT0.5S as zero. Neither is used by the repaired dateTime arithmetic.
* DateTimeDurationUtils.toXmlDayTimeDuration():296–297 and the
  ISO8601DayTimeDuration.of(java.time.Duration) factory still turn 500ms into
  P0DT0H0M0S. The JDK-style PT-0.5S fallback throws an IllegalArgumentException
  for a negative SECONDS field; XSD-style -PT0.5S is covered and passes.
  These other conversion gaps are recorded only, as requested.
* Sub-nanosecond XML durations are rejected with ArithmeticException rather
  than rounded; Java dateTime arithmetic has nanosecond precision.
* Xacml30TestUtility.assertResults():73–81 has no AssociatedAdvice comparison.
  This pre-existing gap is recorded only; its body matches a8a90d46.
* Mixed Content whose first non-whitespace item is a comment or processing
  instruction remains UNVERIFIED. No XML mapper change was made this review.

Fraction observations: 55-fractional-duration-gaps.txt. Base-source proof:
55-review-baseline-snippets.json. Literal accessor search of production core,
PDP, XML, JSON and test-support sources finds the remaining getSeconds /
toSecondsPart truncation sites listed above; this is not an exhaustive audit
of every possible precision conversion.

Normative references: [XACML 3.0 section 7.3.7 and Appendix A.3.7](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html),
[XML Schema Part 2 Appendix E.1](https://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes).
