# Registry follow-up and fixes 4.33 / 4.27

## Part 1: task 4.4 follow-up

- `TypeRegistryTest` now tests alias parsing through the resolved type and has explicit canonical lexical expectations. The JDK 21 red run failed for XML Schema negative zero: expected `-0.0E0`, actual engine output `-0.0` (`part1-red.log`). Per the packet, this row is disabled as `BUG-TBD-4.44`; the observed engine lexical output is `-0.0`. XSD canonical double lexical forms include `INF`, `-INF`, `NaN`, and `-0.0E0` ([W3C XML Schema Datatypes](https://www.w3.org/TR/xmlschema11-2/)).
- Expected lexical rows for `INF`, `-INF`, `NaN`, zoned date (`Z`, `-12:00`), fractional dateTime with `+01:00`, offset time, padded base64, lower-case hex input, and whitespace-preserving string pass in the gate. AnyURI dot-segment and percent-encoding expectations remain disabled under `policy4j-the.4.15` pending the lexical fix.
- The function registry table test covers the mandatory XACML 3.0 conformance table 10.2.8 function identifiers across their 1.0, 2.0, and 3.0 namespaces, including optional 3.0 XPath identifiers. The authoritative table marks the 2.0 regex identifiers and most `string-from-*` rows mandatory ([OASIS XACML 3.0, section 10.2.8](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html)). The known regex rows are disabled under `policy4j-the.4.36`; the known broken `string-from-*` rows under `policy4j-the.4.37`. No other missing identifier surfaced.
- Disabled identifier groups: 4.36 covers `anyURI`, `ipAddress`, `dnsName`, `rfc822Name`, and `x500Name` `urn:oasis:names:tc:xacml:2.0:function:*-regexp-match` IDs. 4.37 covers 3.0 `string-from-double`, `string-from-time`, `string-from-date`, `string-from-dateTime`, `string-from-anyURI`, `string-from-dayTimeDuration`, `string-from-yearMonthDuration`, `string-from-x500Name`, `string-from-rfc822Name`, `string-from-ipAddress`, and `string-from-dnsName`. The conforming `string-from-boolean` and `string-from-integer` rows stay enabled.
- Combining algorithm rows assert decision fingerprints using fixed child outcomes, without asserting provider implementation class names. The legacy `only-one-applicable` row expects generic `INDETERMINATE` for multiple applicable policies. `on-permit-apply-second` remains disabled for task 4.6.

## Part 2: bug 4.33

- Red evidence in `registry-and-provider-red.log`: for absent category plus `MustBePresent=false`, current code threw `EvaluationException: Entity is null`; for `true`, it reported `processing-error` instead of `missing-attribute`.
- `AttributeDesignatorFunctions.designator` now returns the declared type's empty bag for absent category when optional, and raises `missing-attribute` when required. Both direct function tests pass, asserting the empty bag size, exact status code, category lookup call, and no extra context interactions.

## Part 3: bug 4.27

- Red evidence in `registry-and-provider-red.log`: `ServiceLoader.load(DecisionCombiningAlgorithmProvider.class)` returned no providers.
- `DefaultXacml30DecisionCombiningAlgorithms` now carries `@AutoService(DecisionCombiningAlgorithmProvider.class)`. The ServiceLoader discovery test and explicit `withDefaultAlgorithms()` lookup both pass. No descriptor is handwritten; annotation processing generates the service entry.

## Verification

- Focused registry tests: green (`focused-green.log`); the expanded table test also ran on its own (`function-inventory-green.log`).
- Core + PDP clean test: green (`core-pdp-clean-test.log`): xacml-core 4,755 tests, 0 failures/errors, 9 skips; xacml-pdp 77 tests, 0 failures/errors, 3 skips.
- Full reactor clean verify: green (`full-reactor-verify.log`): 5,913 tests, 0 failures/errors, 34 skips across the six test-bearing modules. Per-module counts are in `surefire-summary.txt`.
- Maven reports Java 21.0.6 (Oracle GraalVM) under the selected `jenv` prefix (`command.txt`).
- The conformance manifest in this lane matches its `.base` copy and currently enables 362 OASIS 2.0 cases (360 `executed`, 2 `special-harness`), 478 OASIS 3.0 cases (477 `executed`, 1 enabled special-harness), 17 RSA cases, and 2 examples. All enabled cases passed. These OASIS counts are higher than the older 347/347 gate cited in prior packets; no conformance manifest was changed here.
- Other disabled registry rows retained from 4.4: AnyURI canonicalization (`http://example.com/a/../b` and percent-encoded URI) under 4.15; XML Schema negative-zero output under `BUG-TBD-4.44` (engine currently prints `-0.0`, expected `-0.0E0`); duplicate type/function identifiers under 4.26; and on-permit-apply-second under task 4.6.

## Files changed

- Production: `AttributeDesignatorFunctions.java`; `DefaultXacml30DecisionCombiningAlgorithms.java`.
- Tests: `TypeRegistryTest.java`; `FunctionProviderServiceLoaderTest.java`; `DecisionCombiningAlgorithmRegistryTest.java`; `AttributeDesignatorFunctionTest.java`.
- Evidence: this directory. No POM, fixture, generated, or conformance manifest files were changed.
