# Task 1.3: xacml-xml API repair on JDK 11

Result: **XML PASS; full reactor BLOCKED outside this lane.**
The XML main and test sources compile and all 29 XML tests pass.
Full reactor verification fails while compiling
`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:46`:
the file imports the removed top-level `PathValue`.
PDP, test-support, and conformance tests did not run. Task 1.3 is not accepted.

Lane: `policy4j_implementer_codex_sol`; model: GPT-6 (Codex).
Coverage: SUPPORTED by Maven compiler/Surefire output, opened source, RTK Git,
and byte comparisons of existing LGPL headers. Conformance behavior remains UNVERIFIED.

## Environment and tracking

JDK version line: `openjdk version "11.0.32.1" 2026-08-18 LTS`.
Runtime: Zulu11.90+205-CA, build `11.0.32.1+1-LTS`.
Every Maven run selected `JAVA_HOME="$(jenv prefix 11)"` and passed
`-Dlicense.skipUpdateLicense=true`.

Branch: `integration/modernize-core-jdk28-valhalla`.
HEAD: `befdd116bd11385ac8d007732fa8e4d347af3143`.
`rtk git diff --quiet a8a90d46 -- xacml-core` returned 0:
the core source matches the assigned base API.
Strict OpenSpec validation passed. Beads issue `policy4j-the.1.3` was claimed
and remains `in_progress`, assigned to this lane.
Evidence: `00-environment-and-tracking-output.txt`,
`09-final-scope-checks-output.txt`.

## Compiler errors and fixes

The first compiler run reported **4** unresolved `PathValue` locations.
After those were repaired, subsequent runs exposed **33** more main-source
and **23** test-source locations: **60 compiler-confirmed locations total**.
The planning description's five-error count did not describe the complete
diagnostics reached by this compiler.

Main file prefix: `xacml-xml/src/main/java/org/xacml4j/v30/xml/`.
All coordinates below are from the failing runs; source lines stay at those
coordinates after the edits.

| File:line | Error / fix |
| --- | --- |
| `Xacml20RequestContextUnmarshaller.java:55`, `:279` | Removed top-level `PathValue` / import and use the public `Path` interface returned by the transformer. |
| `Xacml20RequestContextUnmarshaller.java:260` | `Collection<Value>` rejected by `Attribute.Builder.values` / declare `Collection<Value<?>>` at line 249. |
| `Xacml20PolicyFromJaxbToObjectModelMapper.java:98`, `:702` | Missing `PathValue` / import `Path` and return it from `transformSelectorXPath`. |
| `TypeToXacml30.java:44`, `:462` | Missing `PathValue` / import and cast to `Path`. |
| `TypeToXacml30.java:100,120,140,161,182,203,224,244,266,316,337,358,379,400,420,441,497` | `ValueExpTypeInfo` has no `getTypeId()` / obtain `getValueType()` first. |
| `TypeToXacml30.java:267` | Missing `EntityValue` / cast directly to core `Entity`, which is itself a value. |
| `TypeToXacml30.java:274` | No zero-argument `Entity.find()` / enumerate `attributes()`. |
| `TypeToXacml30.java:548` | Capability lookup requires `ValueType` / obtain `getValueType()`. |
| `Xacml30RequestContextFromJaxbToObjectModelMapper.java:253` | No zero-argument `Entity.find()` / enumerate `attributes()`. |
| `Xacml30RequestContextFromJaxbToObjectModelMapper.java:266,514` | Capability lookup requires `ValueType` / obtain `getValueType()`. |
| `Xacml30PolicyFromObjectModelToJaxbMapper.java:262` | No zero-argument `Entity.find()` / enumerate `attributes()`. |
| `Xacml30PolicyFromObjectModelToJaxbMapper.java:284` | Capability lookup requires `ValueType` / obtain `getValueType()`. |
| `Xacml20ResponseContextUnmarshaller.java:129` | Enum factory has no `STRING.of(String)` / use `STRING.ofAny(String)`. |
| `Xacml20ResponseContextMarshaller.java:163` | `Entity.get(attributeId)` removed / use `find(attributeId)`. |
| `Xacml20ResponseContextMarshaller.java:167,178,227` | Capability lookup requires `ValueType` / obtain `getValueType()`. |
| `Xacml20ResponseContextMarshaller.java:171` | `findAttributeValues` removed / use `findValues` and `Collection<Value<?>>`. |
| `ExpressionTypeBuilder.java:96` | `ValueExpTypeInfo` has no `getTypeId()` / obtain `getValueType()`; preserve indentation width using tabs on the edited line. |

Test file prefix: `xacml-xml/src/test/java/org/xacml4j/v30/xml/`.

| File:line | Error / fix |
| --- | --- |
| `XacmlPolicyUnmarshallerTest.java:67`, `:358` | Removed value class `String` / import and assert `StringVal`. |
| `Xacml20RequestContextUnmarshallerTest.java:69,70,80,87,111,118,125,132,140,154,161,168,177,183,192,206,213,217,224,239` | Removed `Entity.get(attributeId)` / use `find(attributeId)`, preserving all assertions. |
| `Xacml30ResponseContextUnmarshallerTest.java:102,103` | Removed `find()` / `get(attributeId)` / use `attributes()` and `find(attributeId)`, preserving all assertions. |

The request test's source-confirmed stale `PathValue` import/cast at lines
41/241 were also adapted before production edits; these are not counted as
compiler-reported locations. It now also checks the XPath value type.

`TypeToXacml30Test.java` adds three characterization tests:
XPath round-trip preserves its path/category/type; category-free XPath
serialization omits the category; entity round-trip preserves issuers,
duplicate values, and include-in-result.
The entity test failed with an empty restored entity after compilation was
repaired. Fix at `TypeToXacml30.java:308`: return `b.build()` directly
instead of reparsing the already-built core entity through `ofAny`.
The XML module run and full reactor's XML stage both pass after this fix.

All 60 compiler diagnostics, their fixes, and their source logs are individually
listed in `compiler-errors-and-fixes.txt`. Raw supporting diagnostics:
`01-baseline-compile-output.txt`, `03-after-path-compile-output.txt`,
`04-adapters-test-compile-output.txt`, `05-stringval-test-compile-output.txt`.
Behavioral red-test evidence: `06-characterization-tests-output.txt`.

## Runs

Exact commands and the output-capture flags are in `command.txt`.
Maven `-l` captures its output without changing the build/test selection;
checkout paths and ANSI color codes are normalized in retained output.

| Run | Exit / result |
| --- | --- |
| 01 initial XML compile | 1 / four unresolved `PathValue` locations. |
| 02 characterization tests before production edits | 1 / blocked by the same four main errors; no test assertions executed. |
| 03 compile after Path fixes; test-compile; one unfiltered capture retry | 1 each / 33 main-source diagnostics; retry retained the complete list after tool truncation. |
| 04 test-compile after adapter repairs | 1 / main compiled; test import `String` failed. |
| 05 test-compile after StringVal repair | 1 / main compiled; 22 entity lookup calls in tests failed. |
| 06 focused characterization tests | 1 / XML 9 run, 1 failure, 0 errors, 0 skipped; entity round-trip dropped attributes. |
| 07 XML module tests with dependencies | 0 / core 494/0/0/4; XML 29/0/0/0. |
| 08 full reactor verify | 1 / core, XML, JSON tests pass; PDP compilation fails at the out-of-scope import. |
| 09 final scope/license/diff checks | 0 / existing headers identical; new header present; diff check passes; core unchanged. |

## Full reactor counts

Source: `08-full-reactor-verify-output.txt`; extracted Maven Results sections
and reactor summary: `surefire-summary.txt`.

| Module | Tests run | Failures | Errors | Skipped | Gate stage |
| --- | ---: | ---: | ---: | ---: | --- |
| `xacml` (parent) | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| `xacml-core` | 494 | 0 | 0 | 4 | SUCCESS |
| `xacml-xml-jaxb` | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| `xacml-xml` | 29 | 0 | 0 | 0 | SUCCESS |
| `xacml-json` | 5 | 0 | 0 | 1 | SUCCESS |
| `xacml-pdp` | — | — | — | — | COMPILE FAILURE; tests not run |
| `xacml-test` | — | — | — | — | REACTOR SKIPPED; tests not run |
| `xacml-conformance-tests` | — | — | — | — | REACTOR SKIPPED; tests not run |
| Reported test total | 528 | 0 | 0 | 5 | Full build FAILED |

Test totals include skipped tests: 523 actually executed.
Dashes indicate no test report, not a passing zero-test suite.
Conformance suites and their case counts are UNVERIFIED because the reactor
stopped before them.

## Scope and remaining work

Changed: eight XML production files, three existing XML test files, one new
XML test file, and this assigned evidence directory. The tables and
`09-final-scope-checks-output.txt` enumerate them.
Byte checks confirm all 11 existing XML LGPL headers are unchanged.
The new test includes the LGPL header. No existing assertion was removed or
weakened. No core, PDP, POM, generated JAXB, or vendored standards file was
written by this lane.

The shared final status also contains another lane's fixture changes and
`xacml-json/src/test/resources/policy.json` deletion, plus its fixture
evidence directory. Those were preserved; the whole worktree is not solely
this lane's diff. Final status is retained in
`09-final-scope-checks-output.txt`.

No commit, staging, branch switch, task checkbox change, or issue closure
was performed. The evidence index is outside this packet's write scope;
the conductor must index this run and handle tracking export.
The full reactor gate must be rerun after the PDP compile blocker is repaired
by its owner. Further downstream diagnostics may then become reachable.
JMH: NOT APPLICABLE for this baseline API adaptation (design's groups 1-4 disposition).
