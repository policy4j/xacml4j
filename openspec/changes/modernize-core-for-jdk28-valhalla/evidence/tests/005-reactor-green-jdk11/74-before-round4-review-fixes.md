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
* The sub-nanosecond rejection observed in round 2 is RESOLVED by the
  conductor-approved truncation toward zero, verified in runs 69/70/71 below.
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

## Round 2 verification follow-up

Started after packet 1.3h: round 3 implementation, run 63 full gate and run 65
final audit were complete. Grok's rv007 approves both original MEDIUM fixes.
The conductor's packet 1.3i authorizes these two refinements and their tests.

Decision: truncate XML duration fractional seconds toward zero at nanosecond
precision when converting to java.time.Duration. Apply RoundingMode.DOWN to
the nanos magnitude, then retain the existing duration sign handling. This is
the conductor's java.time precision/compatibility decision, not a claim that
XML Schema requires truncation. The XML duration value retains all nonzero
decimal digits; the precision reduction occurs only at the java.time boundary.

| Finding / original file:line -> fix | Tests written first | Result |
| --- | --- | --- |
| LOW integral lexical scale; DateTimeDurationUtils.java:260-264 -> if the fractional remainder is zero, pass new BigDecimal(seconds.toBigInteger()) to newDuration; no stripTrailingZeros. Final lines 262-264. | ISO8601DurationReviewTest.integralSecondsDiscardZeroFractionalScale covers PT1.0S/PT1S, negative mirrors, 10 and zero; checks lexical form, SECONDS scale, equality/hash/ordering. nonzeroFractionalLexicalFormStaysDecimal retains +/-P0DT0H0M1.5S. | Run 69: expected P0DT0H0M1S, actual P0DT0H0M1.0S. Runs 70/71: PASS. |
| Valid sub-nanosecond duration throws ArithmeticException; DateTimeDurationUtils.java:227 -> setScale(0, RoundingMode.DOWN) before intValueExact; whole seconds and sign handling unchanged. Final lines 228-229. | subNanosecondConversionTruncatesTowardZero tests +/-PT0.0000000001S -> 0ns, +/-PT1.0000000019S -> +/-1s1ns and +/-PT0.9999999999S -> +/-999999999ns, parsed and raw XML conversions. subNanosecondArithmeticEvaluatesThroughApply runs add/subtract on the default provider, both aliases and validation modes. Fractional arithmetic test adds eight signed rows with both directions and whole/.250 bases. | Run 69: 18 ArithmeticException-related errors, including wrapped Apply invocation failure. Runs 70/71: PASS; no runtime exception or Indeterminate. |

Tests were written before production edits. Run 69 is the behavior-red receipt:
117 tests / 1 failure / 18 errors / 0 skips. Its recorded utility hash equals
the pre-test snapshot. Run 70 repeats the same focused command: 117/0/0/0.
Run 71 repeats all 117 in the full reactor: 934/0/0/9, BUILD SUCCESS on JDK 11.
The four added duration-review methods and sixteen arithmetic rows add twenty
tests; the existing 97 checks pass unchanged in red and green. Per-test names
and stripped XML are retained in 69-, 70- and 71-*-tests.json/XML receipts.

Run 67 stopped during new-test compilation: ISO8601DurationReviewTest.java:115:57
and :116:67 assigned Apply.evaluate's ValueExp directly to ISO8601DateTime.
Explicit ISO8601DateTime casts fix both setup diagnostics. No test ran; its
receipt excludes stale Surefire reports. Runs 69/70/71 have zero compiler
diagnostics. compile-diagnostics.json and the raw logs retain every diagnostic.

Operation inventory: DateTimeArithmeticBoundaryTest retains all six standard
functions (50 checks), and ISO8601DateTimeArithmeticTest retains twelve type
checks. ISO8601FractionalDateTimeArithmeticTest now runs 23 add and 23 subtract
checks through DateTimeArithmeticFunctions, including the sixteen new signed
precision rows. subNanosecondArithmeticEvaluatesThroughApply additionally
checks both day-time operations with four signed operands, both 3.0/1.0 aliases
and runtime validation off/on. Fractional round-trip/equality/hash/ordering,
programmatic P14M and all prior month-end boundaries pass again.

Scope: only DateTimeDurationUtils.java and the two existing focused test files
changed. Three license headers and all prior test method bodies are preserved;
3541 protected source/POM/resource/run003 files are unchanged. Delta:
72-round2-followup-fixes.patch. Neither fixtures nor expected responses changed.
Both OASIS suites still execute 347 cases, RSA 17 and examples 2, all passed.
The thirty exclusions per OASIS suite, loop bounds and emitted case order match
run 63. No new failed case ID. Earlier direct IIIE301-303 proposals and other
recorded selector/fraction/advice gaps remain outside this follow-up; those
additional cases were not rerun. No commit, br, task tick or issue closure.
