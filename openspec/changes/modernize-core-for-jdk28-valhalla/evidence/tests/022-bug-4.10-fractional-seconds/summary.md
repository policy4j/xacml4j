# policy4j-the.4.10: preserve fractional day-time seconds

## Change

`ISO8601Duration.getSeconds()` now returns the XML duration's `BigDecimal` seconds field (zero when undefined), so fractional seconds remain observable. `DateTimeDurationUtils.isZero` checks the decimal seconds field's signum, so a nonzero fraction is not treated as zero. `toXmlDayTimeDuration` now converts the absolute Java duration into day/hour/minute components plus decimal seconds and applies the sign separately; fractional seconds survive Java `Duration` to XML day-time duration conversion, including negative subsecond values.

Tests cover `PT0.5S`, `PT1.5S`, `-PT0.5S`, and `PT0.001S`, nonzero `isZero`, XML/Java round trips, and unchanged equality/comparison for equivalent whole-second durations. Existing tests still cover subnanosecond conversion truncation toward zero at the Java nanosecond boundary.

## Caller impact

The source scan found no production call sites for `ISO8601Duration.getSeconds()` beyond its declaration; updated references are test assertions. `ISO8601Duration.isZero()` delegates to `DateTimeDurationUtils.isZero`; the production call path is the duration API, with existing callers in duration tests. `DateTimeDurationUtils.toXmlDayTimeDuration` is called by `ISO8601DayTimeDuration` construction from `java.time.Duration` and by the JDK-duration parser fallback. Those routes now retain fractional seconds. `java.time.Duration.isZero()` in `BaseResolverDescriptor` is unrelated and unchanged.

The `getSeconds()` return type changes from `int` to `BigDecimal` to represent the XML Schema decimal seconds field. A repository-wide source search found no production callers depending on the prior return type; downstream external source compatibility was not tested.

## Tests and gates

The pre-fix red run failed on `PT0.5S`: Java round-trip conversion produced zero seconds, and `isZero()` returned true (11 tests, 2 failures, 0 errors). The focused post-fix core set passed: 35 tests, 0 failures, 0 errors, 0 skips. The full JDK 21 `-pl xacml-core test` gate passed: 763 tests, 0 failures, 0 errors, 2 skips.

The conformance gate also passed without case changes: OASIS 2.0 **347**, OASIS 3.0 **347**, RSA 2008 **17**, examples **2**. See the logs in `../021-bug-4.9-context-selector/` for the shared full conformance reactor output.

The LGPL blocks on edited files match the read-only `bugs2.base` copy byte-for-byte. `ISO8601Duration.java` has no `%L` header in either copy; no header was added or altered. Maven used Oracle Java 21.0.6, Maven 3.9.16, and `-Dlicense.skipUpdateLicense=true`.

## rv030 repair

`DateTimeDurationUtils.toXmlDayTimeDuration` no longer calls `stripTrailingZeros()`. It uses a scale-0 `BigDecimal` when fractional nanoseconds are zero, avoiding the negative scale JDK 21's XML duration serializer rejects. For a nonzero fractional part it trims trailing decimal zeroes from the nanosecond integer while reducing the explicit scale, preserving prior lexical precision without `stripTrailingZeros()`.

Coverage now includes `Duration.ofSeconds(10, 20, 59, 60)`, one minute, 1m30s, all negative mirrors, and positive/negative 10.5s. Each round-trips through `ISO8601DayTimeDuration.of(Duration)` and `toXacmlString`; exact lexical assertions cover 10s and ±10.5s. `TypeToXacml30Test` and `JsonEntityMarshallingTest` each round-trip a whole-second day-time duration through their marshaller.

Test-first evidence: `rv030-red-core.log` failed with `StringIndexOutOfBoundsException` for 10-second construction and an assertion failure where Match converted syntax-error to NOMATCH. The final focused core run passed 15 tests. The XML focused marshalling test passed 4 tests; JSON focused marshalling passed 3 with 1 existing disabled test. The final JDK 21 gates passed: core 765 tests (0 failures/errors, 2 skips), XML 35 (0/0/0), JSON 6 (0 failures/errors, 1 existing skip), PDP 75 (0/0/3), test-support 20 (0/0/0), and conformance 42 (0/0/0). Conformance totals remained OASIS 2.0 347, OASIS 3.0 347, RSA 2008 17, examples 2; no case changed or failed.

Residual: `Duration.ofSeconds(Long.MIN_VALUE)` still raises `ArithmeticException` at `Duration.abs()` in `DateTimeDurationUtils.toXmlDayTimeDuration` (line 303); no clearer exception was added. This is recorded as an unaddressed edge case. The existing nanosecond-boundary truncation toward zero remains covered by `subNanosecondConversionTruncatesTowardZero`.

The LGPL blocks on all 8 edited Java files were compared byte-for-byte to their `.base` copies and match.
