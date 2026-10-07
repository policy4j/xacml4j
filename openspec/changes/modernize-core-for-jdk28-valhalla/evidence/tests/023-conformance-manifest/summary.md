# Conformance manifest and group 3 gate

Tasks 3.1–3.4 are implemented; task 3.6's current-JDK gate passes. Tracking remains with the conductor.

JDK: Java version 21.0.6, vendor Oracle Corporation (GraalVM); Maven 3.9.16; macOS aarch64. See jdk-version.txt.

## Result and manifest baseline

The manifest has 880 unique cases. All 836 enabled cases pass: 833 executed entries and three working special-harness entries. Forty-three entries are skipped and the remaining special-harness entry is blocked, for 44 disabled cases.

| Suite | Request cases | Executed disposition | Special-harness | Skipped disposition | Passed cases |
| --- | ---: | ---: | ---: | ---: | ---: |
| oasis20 | 374 | 360 | 2 (2 passed) | 12 | 362 |
| oasis30 | 487 | 454 | 2 (1 passed) | 31 | 455 |
| rsa2008 | 17 | 17 | 0 (0 passed) | 0 | 17 |
| example | 2 | 2 | 0 (0 passed) | 0 | 2 |

Every one of run 005/007's 711 distinct passing cases still executes. The old 347+347 OASIS events contained IIIG006 twice in each suite; the manifest runs each ID once. Coverage grows by 125 unique passing cases; no prior distinct case was dropped.
The 72 superseded requests in xacml3.0-deprecated are outside the packet's 487 active 3.0 cases. The guard inventories all other active request directories recursively, rejects duplicate IDs, validates both directions and checks every referenced policy/response file.

## Changes

- xacml-conformance-tests/src/test/manifest/conformance-manifest.csv
- xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceManifest.java
- xacml-conformance-tests/src/test/java/org/xacml4j/v20/XacmlExampleTest.java
- xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceManifestTest.java
- xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformanceCaseRunner.java
- xacml-conformance-tests/src/test/java/org/xacml4j/v20/RSA2008InteropTest.java
- xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml30ConformanceTest.java
- xacml-conformance-tests/src/test/java/org/xacml4j/v20/ConformancePolicyImportTest.java
- xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceTest.java

The four runners use Jupiter parameterized tests with one independent result per manifest row. Numeric bounds and the duplicate IIIG006 invocation are gone. The two existing testName assertions remain. RSA and examples keep XacmlPolicyTestSupport's existing response assertions; OASIS keeps the existing 2.0/3.0 utility assertions.
Policy loading is isolated per case and imports exactly the manifest's policies; missing or broken imports fail immediately with the case, file and original error. No shared import exception is logged and discarded. XACML 3.0 roots and dependencies use the 3.0 media type. Default multiple-decision handlers are enabled.
IID029/IID030 import both roots and use the engine's only-one-applicable PolicySet to consider Target matching. Both 2.0 cases and 3.0 IID030 pass. 3.0 IID029's Policy1 uses the wrong action category; it remains special-harness with enabled=false and BUG-TBD-IID029.
IIE001/IIE002's missing 3.0 dependency paths were corrected from the draft, including the actual lowercase-i IIE001Policyid1.xml spelling. IIE003 uses its Special.txt method 1: explicitly assert PolicyId2's static type mismatch, retain PolicyId1, and compare the unchanged response. This is the documented alternative, not an ignored import failure.

## Red/green and gate receipts

- 01: test-first compile red because the new manifest/runner APIs did not exist; 02: all 12 initial guard/import tests pass. The compile-red receipt is not presented as a semantic failure.
- 03: extra closing brace in the RSA replacement caused a compile error; corrected before enumerating cases.
- 04: all 880 cases attempted, with 48 failures/errors. 05: dependency-inventory and path-escape tests red (two failures). 06: both 3.0 reference cases and both IIE003 cases now pass; 44 out-of-scope failures remain.
- 07: nested-request and duplicate-request guards red (two failures). Final guards pass all 13 tests. Import/media-type tests pass all three tests.
- 08: requested conformance -am test gate passes; 1,788 tests, zero failures/errors, 49 skips. The JSON module is not in this dependency slice.
- 09: full JDK 21 clean verify passes with 1,793 tests, zero failures/errors and 50 skips. 15: the final full clean gate passes with 1,801 tests, zero failures/errors and 50 skips. Concurrent outside-lane tests raised core 760 to 765, XML 34 to 35, JSON 5 to 6 and PDP 73 to 74. Test-support and conformance counts are unchanged. The final run's 3,546 recorded project-file hashes are identical before/after (15-final-gate-stability.json).

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| root (pom) | 0 | 0 | 0 | 0 |
| xacml-core | 765 | 0 | 0 | 2 |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 |
| xacml-xml | 35 | 0 | 0 | 0 |
| xacml-json | 6 | 0 | 0 | 1 |
| xacml-pdp | 74 | 0 | 0 | 3 |
| xacml-test | 20 | 0 | 0 | 0 |
| xacml-conformance-tests | 901 | 0 | 0 | 44 |
| **Total** | **1801** | **0** | **0** | **50** |

Conformance Surefire's 901 tests are 880 fixture rows plus 21 helper/name/resolver/integrity tests. The 44 conformance skips are explicit manifest blockers; the other six skips retain the prior core/JSON/PDP reasons. Every IIC340–349 duration-set case passes. Per-case outcomes and every inherited skip are in per-case-results.csv and inherited-skip-dispositions.csv.

## Failure classification and residual risk

All 44 blocked cases have fresh failing receipts, a concrete reason and a bug reference: 36 engine defects, seven fixture/spec issues, one out-of-scope comparison-harness defect. See conformance-blockers.md/JSON for every case ID and cause. IIIE301–303 use existing policy4j-the.3.7; the other 41 suite/case rows use BUG-TBD-<case> pending conductor triage. The manifest gate is green with these exclusions; it is not complete conformance acceptance.
The 21 inherited fixture-backed exclusions in each suite are reconciled individually. Twelve now pass in 2.0 and sixteen in 3.0; nine and five respectively remain blocked. The nine historical skip numbers without request fixtures are not phantom manifest rows.
No fixture, expected response, POM or production file was edited by this lane. Original four runner license headers are byte-identical. The starting snapshot detected eight later outside-lane source edits; those were preserved and included in the final stable clean gate. final-scope-audit.json lists them and marks the wider scope audit PARTIAL. No author is inferred.
The retained response utilities have prior oracle gaps (2.0 obligation self-comparison; 3.0 lacks AssociatedAdvice comparison). They were not changed or weakened; passing cases prove the existing assertions. Hierarchy and policy-reference tracking root-cause details still need maintainer review.

## rv026 correction verification

Trees 10/11 encountered sandbox metadata-cache write denial; native escalation retries 12/13 completed across all eight projects. Objenesis 3.3 appears only below Mockito core 5.24.0. The JUnit 4/Vintage/EasyMock filter is empty in every project. The approved verification addendum is appended to run 017 summary.md; no POM was edited.

No commit, stage, install, br command, task tick or issue close was performed.
