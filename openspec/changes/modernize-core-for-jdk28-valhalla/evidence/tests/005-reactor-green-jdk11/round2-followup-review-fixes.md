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
