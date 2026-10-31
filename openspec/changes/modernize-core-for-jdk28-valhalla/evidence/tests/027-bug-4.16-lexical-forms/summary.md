# Task 4.16: XACML lexical forms

## Result

Fixed `TypeToString` integer output to use the XACML lexical value instead of the `IntegerVal` debug representation. `DoubleVal.toXacmlString()` now maps positive infinity, negative infinity, and NaN to `INF`, `-INF`, and `NaN`; finite values retain Java's numeric spelling, including negative zero. `TypeToString` now delegates to this formatter and uses `X500Name.toXacmlString()`.

`TypeToStringTest.testXmlSchemaLexicalFormsRoundTrip` covers integer, double (including infinities, NaN, and negative zero), boolean, string, anyURI, base64Binary, hexBinary, date, dateTime, dayTimeDuration, yearMonthDuration, x500Name, rfc822Name, ipAddress, and dnsName. XPath has no `TypeToString` capability; the existing XML XPath round-trip tests cover its path and category fields. Time was excluded per task instructions. XML and JSON integer marshalling tests assert the lexical value `56` and round-trip it.

## Red/green and gate

- Red: before the integer formatter fix, the round-trip test failed with `NumberFormatException` for `IntegerVal{type=integer, value=56}`.
- Red: before the double formatter fix, `-INF` formatted as `-Infinity`.
- Green: JDK 21 focused core, XML, and JSON tests passed; the complete reactor test command and counts are in `../029-bug-4.22-policy-id-list/full-reactor.log`.
- Full reactor: xacml-core 767 tests, 0 failures/errors, 2 skipped; xacml-xml 38, 0 failures/errors/skips; xacml-json 8, 0 failures/errors, 1 skipped.
- Direct diagnostic runs of the listed conformance fixtures did not pass the legacy response comparator: IIA022 and IIA024 compare expected `27.50` with actual `27.5` as unequal strings, although both are the same XML Schema double value. IIIA340 throws `NullPointerException` in `Xacml30TestUtility.assertAttributesType` when expected `AttributesType.content` is absent. These are harness comparison failures, so those cases do not verify the fixed `56`/`INF` values. No fixture was changed; temporary diagnostic test methods were removed after recording these results in `lexical-fixture-tests.log`.

The XML and JSON adapters delegate scalar serialization to `TypeToString` (`TypeToXacml30` and `TypeToGSon`), so these fixes propagate to those encodings. The test matrix verifies parse/format/parse equality; only the regression-prone integer and special double outputs have explicit canonical-string assertions.

## rv037 review repair: string-from lexical forms

`DoubleVal.toStringExp()` now delegates to `toXacmlString()`. Before the fix,
the new default-provider `Apply` tests failed for positive and negative
infinity (`Infinity` and `-Infinity` instead of XACML `INF` and `-INF`). The
post-fix suite checks both infinities and NaN, plus string-from-integer,
boolean, date, dateTime, zoned time, dayTimeDuration, yearMonthDuration,
anyURI, x500Name, rfc822Name, ipAddress, and dnsName. All 15 parameterized
cases pass through `Apply` and `FunctionProviderBuilder.withDefaultFunctions()`.

The conversion audit found no other debug-form conversions in those
functions: integer delegates to `IntegerVal.toXacmlString`; booleans use the
same lowercase lexical form as `BooleanVal.toXacmlString`; date/dateTime,
zoned time, and both durations delegate to their XACML string methods; URI
uses `URI.toString()` in both its conversion and XACML formatter; X500 uses
the same `X500Principal.toString()` in both methods; RFC822, IP address, and
DNS name use their XACML formatting methods. `*-from-string` functions parse
the string and do not format a value.

One separate issue surfaced while initially probing unzoned time: this lane's
`ISO8601Time.toXacmlString()` returns `23:00:00Z` for input `23:00:00`. The
bugs3 task packet explicitly assigns time formatting to task 4.11 and says not
to edit it here, so the committed regression matrix uses an explicitly zoned
time. The task 4.11 fix is not present in this lane copy; carry it into the
integration baseline before claiming unzoned time string conversion is fixed.

The requested `TypeToStringTest` indentation issue is corrected. Verification
on JDK 21: focused string-function and formatter tests passed (17 total);
full core passed 782 tests (0 failures, 0 errors, 2 skipped); XML passed 38
(0 failures/errors/skips); JSON passed 8 (0 failures/errors, 1 skipped); PDP
passed 75 (0 failures/errors, 3 skipped). Raw logs and exact commands are in
this directory. The rv037 code changes are ready to apply, but this lane is
not complete as a combined integration baseline until the separate unzoned
time fix is present.
