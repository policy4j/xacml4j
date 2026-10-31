# Task 1.3 continued: full reactor green on JDK 11

Result: **PASS**. The full reactor compiles and verifies on JDK 11, including
xacml-test and the existing conformance suites: **774 Surefire tests,
0 failures, 0 errors, 9 existing skips**. OASIS 2.0 and 3.0 each complete
347 case executions; all 17 RSA interop cases and both examples pass.

JDK: `openjdk version "11.0.32.1" 2026-08-18 LTS`;
Zulu11.90+205-CA, build 11.0.32.1+1-LTS. Raw version line: 38-jdk-version.txt.
Fresh final-gate Surefire JVM properties: 46-jdk-properties.json.
Branch: integration/modernize-core-jdk28-valhalla; HEAD
befdd116bd11385ac8d007732fa8e4d347af3143; core API base a8a90d46.

## Final full-reactor gate

```sh
JAVA_HOME="$(jenv prefix 11)" rtk mvn -Dlicense.skipUpdateLicense=true verify -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/005-reactor-green-jdk11/46-reactor-verify-higher-order-identifiers.txt
```

| Module | Tests run | Failures | Errors | Skipped | Reactor result |
| --- | ---: | ---: | ---: | ---: | --- |
| xacml parent | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-core | 603 | 0 | 0 | 4 | SUCCESS |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-xml | 34 | 0 | 0 | 0 | SUCCESS |
| xacml-json | 5 | 0 | 0 | 1 | SUCCESS |
| xacml-pdp | 71 | 0 | 0 | 4 | SUCCESS |
| xacml-test | 20 | 0 | 0 | 0 | SUCCESS |
| xacml-conformance-tests | 41 | 0 | 0 | 0 | SUCCESS |
| Total | 774 | 0 | 0 | 9 | BUILD SUCCESS |

These are fresh run 46 Maven Results, retained in surefire-summary.txt and
46-module-counts.json. Surefire counts grouped conformance methods; individual
case counts below come from their retained production execution output.

## Approved identifier repairs, tests first

The conductor approved required-duration-bag-alias-fix.patch and expanded
scope to missing/wrong identifiers and aliases only. The requested family
scan found ten related duration set names and, after IIC164 was reached,
four higher-order names. Each of the 22 added 3.0 identifiers and retained
1.0 aliases is listed with its original file:line, focused test and normative
reference in identifier-audit.md and 46-identifier-audit.json. The original
1.0 annotations are confirmed at a8a90d46 in 46-identifier-baseline.txt.

| Error / original source location -> fix | Red receipt | Green receipt |
| --- | --- | --- |
| IIC150 import; BagFunctions.java:491/499/507/516/529/538/547/557 -> exact approved eight 3.0 duration bag annotations plus eight 1.0 legacy aliases. | Run 39 before either duration provider edit: 36 failures (18 missing 3.0 names, 18 canonical/legacy metadata expectations). | Run 41: all 42 focused tests pass; run 46 repeats all 36 duration lookup checks and evaluates IIC150 successfully. |
| Same-family SetFunctions.java:585/594/603/612/621/630/639/648/657/666 -> ten 3.0 duration set annotations plus ten 1.0 legacy aliases. | Same run 39. | All duration lookup checks pass in runs 41/46. Later duration-set conformance cases beyond the unchanged loop bounds remain UNVERIFIED. |
| Run 40 SetFunctions.java:586/596/606/616/626/636/646/656/666/676 -> add import for XacmlLegacyFunc; every compiler diagnostic retained in compile-diagnostics.json. | Temporary edit compile failure, ten locations. | Run 41 and final gate compile without diagnostics. |
| IIC164 import; HigherOrderFunctions.java:54/72/90/169 -> four 3.0 names (any-of, all-of, any-of-any, map), plus four 1.0 legacy aliases. | Run 43 directly reproduces unchanged IIC164 import; run 44 before production edit: 11 tests / 8 failures / 3 passes. | Run 45: all 53 focused tests pass; run 46 repeats all 11 registry checks and evaluates IIC164 successfully. |

Production method bodies and type/parameter annotations match the captured
pre-write source exactly. Bag aliases match the approved patch byte for byte.
Identifier tests use the default production FunctionProvider and assert
presence, canonical/legacy metadata and object identity for each old/new name.
No assertion, expected result or fixture was changed.

References: [OASIS XACML 3.0 Standard](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html),
Appendix A.3.10/A.3.11 for bag/set names, A.3.12/A.4 for higher-order names,
Tables 10.2.8/10.2.9 for the duration versioned names. No type or algorithm
identifier needed a repair; no production semantic change was made in this
identifier continuation. Decisions and receipts: identifier-approval.md.

## Conformance case totals and dispositions

| Suite | Surefire methods | Cases executed | Failed case IDs |
| --- | ---: | ---: | --- |
| OASIS 2.0 | 10 | 347 | None |
| OASIS 3.0 | 10 | 347 | None |
| RSA 2008 interop | 17 | 17 | None |
| Examples | 2 | 2 | None |
| Resolver characterization | 2 | Not conformance cases | None |

OASIS/RSA: **711 executed, 711 passed, 0 failed**. Including examples:
**713 executed, 713 passed**. Each OASIS suite invokes IIIG006 twice, as before.
46-conformance-testcases.json records every method, emitted case ID and
failure field. IIC232 is reached by both suites; IIC150 and IIC164 pass.

The 30 explicit case exclusions in each OASIS suite (60 total), inherited
group bounds, RSA performance exclusion and Surefire's 9 skips are unchanged.
The exclusion sets are extracted again and compared to run 36 in
46-existing-case-exclusions.json. Suite and fixture hashes are unchanged.
The passed gate certifies the currently configured coverage, not all vendored
cases. The XACML 3.0 IIC loop still stops before 233; later duration set cases
IIC340-IIC349 are outside that loop and were not added to this task's gate.

| Previously observed failure | Classification / cause | Final disposition |
| --- | --- | --- |
| IIC104, both suites | Pre-existing core day-time subtraction sign defect | PASS; earlier approved sign restoration retained. |
| IIC106/IIC107, both suites | Pre-existing date/year-month conversion defect | PASS after approved Period fix. |
| IIIF001 and both IIIG006 invocations, OASIS 3.0 | Pre-existing XML whitespace/content selection defect | PASS after approved content mapper fix. |
| IIA001/IIB001/IIC001/IID001/IIE001/IIIA006 errors, run 22 | Pre-existing harness/test-support optional collection comparison defects | PASS; owned comparator repair retains assertions. |
| IIC150, run 36 | Pre-existing core duration bag registration; unchanged policy's required 3.0 name absent | PASS after conductor-approved aliases. |
| IIC164, run 42 | Pre-existing core higher-order registration; unchanged policy's required 3.0 any-of name absent | PASS after approved identifier-class repair. |

Import failures print no execution marker: IIC149 in run 36 and IIC163 in
run 42 are the last successful markers, not the failing cases. Actual failed
IDs were directly reproduced by production imports in runs 37 and 43.
No adaptation regression or new semantic failure is left by the final gate.
conformance-blockers.md marks every recorded blocker resolved; no proposal
needs further authorization to obtain this configured green baseline.

## Prior approved arithmetic checks retained

The conductor approved required-date-period-fix.patch and expanded the lane
to the date/time/duration arithmetic class of defects. The decision is
recorded in `date-arithmetic-approval.md`. The prior approved JUnit dependency
and core/XML restoration decisions persist; no further POM edits were made.

`34-date-arithmetic-before.txt` records tests written before either new
production fix: 65 tests, **20 failures**, no errors or skips. The new
50-case DateTimeArithmeticBoundaryTest has 16 nonzero date failures and
four combined year/month dateTime boundary failures. The earlier 12 signed
type tests and three function-provider tests pass.

| Original source location -> fix | Red characterization | Green verification |
| --- | --- | --- |
| `ISO8601Date.java:145–150` -> use `duration.toPeriod()` in both plus/minus, exactly the approved patch. Year/month operands were converted to day/time PT0S. | DATE_ADD_YEAR_MONTH and DATE_SUBTRACT_YEAR_MONTH: eight nonzero cases fail in each; zero cases pass. | All 18 date cases pass in run 35 and the full gate. |
| `ISO8601DateTime.java:158`, `:182` before this turn -> use the signed Period in both year/month operations. Separate year/month adjustments clipped February 29 before the final month was known. | DATETIME_ADD_YEAR_MONTH and DATETIME_SUBTRACT_YEAR_MONTH each fail both P1Y1M directions from 2020-02-29; expected March 29 / January 29, actual day 28. | All 18 dateTime year/month cases pass in run 35 and the full gate. |

The second fix is within the newly approved arithmetic class and is backed
by the requested month-end characterization. It is an additional pre-existing
calendar defect found by those tests, not a newly observed conformance case.
The previous day-time subtraction sign fix remains unchanged.

`35-date-arithmetic-after.txt`: all **65 tests pass**, no failures/errors/skips.
`36-reactor-verify-date-arithmetic.txt`: the full core module repeats all of
them successfully. No duration/time class or function-provider production
class needed a change.

## Every operation checked and its test

Each row below is tested through DateTimeArithmeticFunctions by
`DateTimeArithmeticBoundaryTest.arithmeticPreservesSignedCalendarBoundaries`.
The parameter label uniquely identifies the operation and exact operands.
`36-arithmetic-operation-tests.json` lists all 50 checks, expected values,
full test names and observed red/green results. `arithmetic-operations.md`
also lists the twelve prior type-level test names.

| Function / type operation | Test parameter | Checks | Red failures | Green |
| --- | --- | ---: | ---: | --- |
| date-add-yearMonthDuration / ISO8601Date.plus | DATE_ADD_YEAR_MONTH | 9 | 8 | 9 pass |
| date-subtract-yearMonthDuration / ISO8601Date.minus | DATE_SUBTRACT_YEAR_MONTH | 9 | 8 | 9 pass |
| dateTime-add-yearMonthDuration / ISO8601DateTime.plus | DATETIME_ADD_YEAR_MONTH | 9 | 2 | 9 pass |
| dateTime-subtract-yearMonthDuration / ISO8601DateTime.minus | DATETIME_SUBTRACT_YEAR_MONTH | 9 | 2 | 9 pass |
| dateTime-add-dayTimeDuration / ISO8601DateTime.plus | DATETIME_ADD_DAY_TIME | 7 | 0 | 7 pass |
| dateTime-subtract-dayTimeDuration / ISO8601DateTime.minus | DATETIME_SUBTRACT_DAY_TIME | 7 | 0 | 7 pass |

Every operation covers positive, negative and zero durations. Calendar
operations also cover leap/non-leap February month ends, both directions,
negative equivalents and combined periods starting at February 29. Day-time
operations cover month/leap-day rollover and retain the original offset.

The OASIS XACML 3.0 Standard Appendix A.3.7 defines these six operations;
it defines neither time duration arithmetic nor date day-time arithmetic.
The opened production function class provides the same six. Those absent
operations are not added. XACML delegates arithmetic to XML Schema Appendix
E; E.1 applies the combined calendar adjustment before clipping the day.
See `arithmetic-reference.md` and the official sources:
[OASIS XACML 3.0](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html),
[XML Schema Part 2 Appendix E](https://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes).

## Changed project files in the continued lane

The continued task 1.3 lane changed **23 project files** plus this run 005
directory. Five project files changed/appeared in the latest identifier
continuation; all earlier repairs and accepted xacml-xml review files stayed
unchanged. The original xacml-xml adaptation evidence in run 003 is preserved.

| File | Change |
| --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601Date.java` | Approved two-line year/month Period conversion. |
| `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java` | Signed combined Period for year/month add/subtract; prior approved day-time subtraction sign condition retained. |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DateTimeArithmeticBoundaryTest.java` | New 50-case test through all six production arithmetic entrypoints. |
| `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeArithmeticTest.java` | Prior twelve-test signed/zero truth table for four type operations. |
| `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java` | Previously approved whitespace-only mixed-content filter; unchanged this turn. |
| `xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml30RequestContentTest.java` | Prior five-test production content-unmarshaller characterization. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java` | Entity API, designator overload, Optional contract; content-only selectors without an explicit/generated context ID. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java` | Path, Entity.attributes(), ValueType extraction. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java` | Entity.findValues() and attributes(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java` | Four selector, issuer/duplicate/Entity and PIP/cache tests. |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java` | RequestContext.getAttribute(); metadata-preserving expansion. |
| `xacml-test/pom.xml` | Exact conductor-approved compile-scope junit:junit:4.13.2 patch; unchanged this turn. |
| `xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java` | getEvaluatesTo().getValueType(). |
| `xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java` | Two production resolver/multiplicity characterizations. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java` | Normalize absent optional collections to empty; compare actual obligations. |
| `xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java` | Fourteen empty/mismatch and production response-marshalling tests. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java` | StringVal and explicit BagOfValues scalar cast; preserve subject/role behavior. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java` | Two mixed-case/unmatched-subject tests. |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/BagFunctions.java` | Eight standard 3.0 duration bag names; retain eight 1.0 aliases; exact approved patch. |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/SetFunctions.java` | Ten standard 3.0 duration set names; retain ten 1.0 aliases; add annotation import. |
| `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.java` | Four standard 3.0 higher-order names; retain four 1.0 aliases; annotations only. |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/DurationFunctionIdentifierTest.java` | 36 parameterized standard/legacy registry checks; red before repairs. |
| `xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/HigherOrderFunctionIdentifierTest.java` | Eight standard/legacy and three unchanged standard registry checks; red before repairs. |

The xacml-test POM change is the exact conductor-approved
junit:junit:4.13.2 compile-scope patch. No other POM edit was made. Original
continued compiler errors -> fixes follow below; all raw diagnostics remain
in compile-diagnostics.json, including the temporary missing import in run 40.

## Evidence, scope and remaining risk

command.txt records exact Maven and evidence commands; surefire-summary.txt
retains Maven Results and per-module totals through final run 46. Logs and
receipts normalize the checkout path to <repo>. Earlier runs and run 003
are unchanged; 36-summary-before-identifiers.md and
36-blockers-before-identifiers.md retain the former blocked state.

scope-check.txt records 3,436 protected files with unchanged hashes, 14
existing edited Java license headers unchanged and eight new test headers
matching their module templates. The three provider sources match only
the expected annotation/alias edits and one import. Prior arithmetic/XML/POM
hashes remain unchanged. Diff whitespace checks and reverse checks of the
approved patches pass. Git has no staged changes. Shared fixture restoration,
other-lane XML work, evidence and the pre-existing JSON fixture deletion were
preserved; their presence in git status is not attributed to this lane.

Residual risk: inherited exclusions and loop bounds still limit conformance
coverage. Alias lookup tests and the configured suites do not certify all
3.0 variadic higher-order semantics, map signatures or semantic open questions
in tasks 4.5/4.6; these bodies were preserved. Broader semantics and vendored
cases outside the current harness remain UNVERIFIED. No commit, br command,
task tick, issue closure or evidence-index edit was performed.

# Compiler errors and fixes

Locations are the compiler's original locations, including temporary test insertions.
Repeated errors from reruns are retained in compile-diagnostics.json and raw logs.

## PDP diagnostics (14)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:46:29` | cannot find symbol | Import and cast public Path instead of removed top-level PathValue. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java:68:54` | cannot find symbol | Use Entity.findValues(id, type); preserve filtering/cardinality. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java:90:56` | no suitable method found for find(no arguments) | Iterate Entity.attributes(). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:49:7` | org.xacml4j.v30.pdp.DefaultEvaluationContextHandler is not abstract and does not override abstract method getEntity(org.xacml4j.v30.CategoryId) in org.xacml4j.v30.policy.EvaluationContextHandler | Implement Optional<Entity> getEntity(CategoryId). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:131:81` | incompatible types: org.xacml4j.v30.AttributeDesignatorKey cannot be converted to org.xacml4j.v30.AttributeSelectorKey | Resolve designators by attribute id, datatype and issuer. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:132:61` | incompatible types: java.util.Optional<java.lang.Object> cannot be converted to java.util.Optional<org.xacml4j.v30.BagOfValues> | Cascading Optional inference error resolved by the designator overload fix. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:180:48` | cannot find symbol | Cast the single bag value directly to Entity. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:181:48` | cannot find symbol | Remove EntityValue.get(); Entity is the value. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:196:48` | name clash: getEntity(org.xacml4j.v30.CategoryId) in org.xacml4j.v30.pdp.DefaultEvaluationContextHandler and getEntity(org.xacml4j.v30.CategoryId) in org.xacml4j.v30.policy.EvaluationContextHandler have the same erasure, yet neither overrides the other | Delegate Optional<Entity> getEntity to the request callback. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:119:47` | cannot find symbol | Stream Entity.attributes(). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:145:81` | incompatible types: org.xacml4j.v30.ValueExpTypeInfo cannot be converted to org.xacml4j.v30.types.ValueType | Use path.getEvaluatesTo().getValueType(). |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:146:49` | incompatible types: inference variable T has incompatible bounds | Cascading inference error resolved by the transform value-type fix. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:163:36` | no suitable method found for find(no arguments) | Iterate Entity.attributes(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java:184:78` | cannot find symbol | Use RequestContext.getAttribute(category, id); keep value/path assertions. |

## JUnit main-source diagnostics (24)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:25:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:25:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:26:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:26:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:27:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:27:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:27:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:27:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:28:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:28:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:29:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/AttributeResolverTestSupport.java:29:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:25:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:25:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:26:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:26:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:34:17` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:25:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:25:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:26:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:26:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:27:24` | package org.junit does not exist | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:27:1` | static import only from classes and interfaces | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |
| `xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:63:10` | cannot find symbol | Apply conductor-approved junit:junit:4.13.2 compile-scope dependency; all assertion/lifecycle code retained. |

## Downstream API diagnostics (3)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java:63:53` | incompatible types: org.xacml4j.v30.ValueExpTypeInfo cannot be converted to org.xacml4j.v30.types.ValueType | Use value.getEvaluatesTo().getValueType(). |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java:32:29` | cannot find symbol | Import StringVal instead of removed String. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java:52:51` | incompatible types: org.xacml4j.v30.types.Value<capture#1 of ?> cannot be converted to org.xacml4j.v30.types.StringVal | Cast the annotation-constrained string BagOfValues.value() result to StringVal. |

## Temporary edit errors, also corrected (4)

| Compiler file:line:column | Diagnostic | Fix |
| --- | --- | --- |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:145:66` | cannot find symbol | Use path.getEvaluatesTo().getValueType(); resolves subsequent inference error. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:146:49` | incompatible types: inference variable T has incompatible bounds | Use path.getEvaluatesTo().getValueType(); resolves subsequent inference error. |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java:105:84` | cannot find symbol | New test setup uses Result.ok(Decision.PERMIT), not nonexistent Result.permit(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java:106:85` | cannot find symbol | New test setup uses Result.ok(Decision.PERMIT), not nonexistent Result.permit(). |


## Identifier-continuation compiler errors (run 40), corrected

SetFunctions.java:586/596/606/616/626/636/646/656/666/676 (column 10):
`cannot find symbol: class XacmlLegacyFunc` -> add the annotation import.
These ten errors have the same root cause. Runs 41, 42, 44, 45 and 46
have zero compiler diagnostics; intentional red lookup runs fail assertions.
