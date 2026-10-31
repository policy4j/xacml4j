# Task 1.3 continued: arithmetic green; IIC150 registration blocker

Result: **BLOCKED**. The full reactor and all test sources compile on JDK 11.
The final required verify gate runs 727 Surefire tests, with 0 failures,
1 error and 9 existing skips. The sole error is **OASIS 3.0 IIC150**, during
policy import because a required duration bag function is not registered.
This is outside the approved arithmetic/content class of repairs. No code
was changed to address that blocker; an unapplied proposal is retained.

JDK: `openjdk version "11.0.32.1" 2026-08-18 LTS`;
Zulu11.90+205-CA, build 11.0.32.1+1-LTS (`00-jdk-version.txt`, refreshed
in `38-jdk-version.txt`).
Branch: integration/modernize-core-jdk28-valhalla; HEAD
befdd116bd11385ac8d007732fa8e4d347af3143; core API base a8a90d46.
The actual arithmetic red, green and full-gate runs all select JDK 11.

## Final full-reactor gate

```sh
JAVA_HOME="$(jenv prefix 11)" rtk mvn -Dlicense.skipUpdateLicense=true verify -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/005-reactor-green-jdk11/36-reactor-verify-date-arithmetic.txt
```

| Module | Tests run | Failures | Errors | Skipped | Reactor result |
| --- | ---: | ---: | ---: | ---: | --- |
| xacml parent | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-core | 556 | 0 | 0 | 4 | SUCCESS |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-xml | 34 | 0 | 0 | 0 | SUCCESS |
| xacml-json | 5 | 0 | 0 | 1 | SUCCESS |
| xacml-pdp | 71 | 0 | 0 | 4 | SUCCESS |
| xacml-test | 20 | 0 | 0 | 0 | SUCCESS |
| xacml-conformance-tests | 41 | 0 | 1 | 0 | FAILURE |
| Total | 727 | 0 | 1 | 9 | BUILD FAILURE |

Counts are fresh Maven Results from run 36, retained in
`surefire-summary.txt` and `36-module-counts.json`. Surefire counts include
the grouped conformance methods, not each case inside those loops.

## Arithmetic approval, red tests and fixes

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

## Conformance totals and dispositions

`36-conformance-testcases.json` retains method-level IDs and the error trace.
The execution marker is printed after policy import and decision evaluation,
before response comparison. An import error therefore has no marker for its
own case. IIC149 is the last successful IIC marker; **IIC150** is the failed
import, independently reproduced in run 37 against the unchanged fixture.

| Suite | Surefire methods | Cases attempted | Cases evaluated | Failed case / phase |
| --- | ---: | ---: | ---: | --- |
| OASIS 2.0 | 10 | 347 | 347 | None |
| OASIS 3.0 | 10 | 265 | 264 | IIC150: policy import error, before evaluation |
| RSA 2008 interop | 17 | 17 | 17 | None |
| Examples | 2 | 2 | 2 | None |
| IIA002 resolver characterization | 2 | Not conformance cases | Not conformance cases | None |

OASIS/RSA: **629 attempted cases, 628 evaluated, 1 import error**.
All evaluated cases pass. Including examples: 631 attempted / 630 evaluated;
both examples pass. OASIS suites each execute IIIG006 twice, as before.
Each suite retains 30 explicit case exclusions; see
`36-existing-case-exclusions.json`. These 60 exclusions are distinct from
Surefire's 9 skips. Loop bounds and exclusions have not been changed.
Later XACML 3.0 IIC cases remain unexecuted after the IIC150 import error.

| Earlier failure | Classification | Run 36 disposition |
| --- | --- | --- |
| IIC104, both OASIS suites | Pre-existing core day-time subtraction sign defect | Pass after the previously approved sign fix. |
| IIC106, both suites | Pre-existing core date/year-month conversion defect | Pass after the approved date Period fix. |
| IIC107, both suites | Sibling date subtraction check | Pass with the same date Period fix. |
| IIIF001 and both IIIG006 invocations, OASIS 3.0 | Pre-existing XML content selection defect | Pass after the earlier approved mixed-content fix. |
| IIA001/IIB001/IIC001/IID001/IIE001/IIIA006 errors in run 22 | Pre-existing harness/test-support collection comparison defects | Pass after the owned comparator repair; all assertions retained. |

### Remaining proposal: IIC150 duration bag identifiers

Classification: **pre-existing core function-registration defect**. It is
not date/time arithmetic or XML content mapping. BagFunctions.java:491
registers only the 1.0 dayTimeDuration-one-and-only name. XacmlDefaultFunctions
includes BagFunctions, but a default provider lookup returns empty for the
required 3.0 identifier. Policy import throws PolicySyntaxException before
evaluating the case.

Run 37 checks the live default provider: all eight duration bag 1.0 names are
present and all eight corresponding 3.0 names are absent. It then imports
the unchanged `xacml3.0-ct-v.0.4/IIC150Policy.xml` and reproduces the exact
missing-identifier error. This is direct runtime evidence, not an inference
from an empty source search. BagFunctions, XacmlDefaultFunctions, the suite
and the IIC150 policy all match a8a90d46 (`scope-check.txt`). The official
XACML normative function table requires these 3.0 duration bag identifiers;
the fixture uses a valid standard name.

`required-duration-bag-alias-fix.patch` is **unapplied** and passes
`rtk git apply --check`. It proposes changing the eight duration bag
annotations to 3.0 IDs with @XacmlLegacyFunc retaining their 1.0 IDs. Existing
function bodies and semantics remain. The converter already supports the
legacy annotation (`JavaMethodToFunctionSpecConverter.java:98–105`).
This patch needs conductor authorization for BagFunctions and focused bag
tests. Proposed checks: all sixteen old/new identifiers resolve; production
function invocation preserves bag values and cardinality errors; unchanged
IIC150 imports, evaluates and matches its response; rerun the full gate.
The proposal's behavior and later IIC cases are **UNVERIFIED**. No fixture or
expected response is changed or proposed to change.

## Changed files in this continuation

Eighteen project files plus run 005 have been changed by the continued lane.
Only the two core sources and the new boundary test changed in this latest
turn; the approved XML and downstream repairs remain as reviewed.

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

Earlier accepted xacml-xml API repairs and run 003 were preserved. Every
continued compiler error -> fix is listed in the appendix below and in
`compiler-fixes.md`; original diagnostics remain in `compile-diagnostics.json`.
Runs 34–36 have zero compiler diagnostics. No original assertion was removed
or weakened, no expected result changed and no new skip/exclusion added.

## Evidence, scope and unfinished work

`command.txt` records exact commands; `surefire-summary.txt` includes the raw
Maven Results sections and per-module totals through run 36. Checkout paths
are normalized to <repo>. Prior runs, run 003 and historical receipts stay
intact. `31-summary-before-date-arithmetic.md` and
`31-blockers-before-date-arithmetic.md` retain the prior state.
The current evidence finalizer is `finalize-date-arithmetic.py`; older
finalizers describe their historical runs and must not be run on this state.

`scope-check.txt`: 3,334 protected files match the pre-write snapshot;
11 edited existing Java license headers are unchanged and 6 new test headers
match module templates. The date change matches the exact approved patch;
the dateTime change matches only the two signed Period replacements since
the snapshot, which retains the previous approved sign fix. The other five
authorized arithmetic sources are unchanged. The XML mapper and approved
POM still match their exact earlier patches. Diff whitespace checks pass.

The shared working tree includes prior accepted XML edits, another lane's
vendored fixture restoration, and a pre-existing JSON resource deletion.
Those changes were preserved, not produced by this turn. Generated/vendored
inputs, other core sources, other POMs, tasks.md, evidence/README.md and run
003 were not edited. No staging, commit, br, task tick, closure or index edit.

Task 1.3 remains unfinished because the required full gate is not green.
The IIC150 bag-registration proposal is ready for review but outside scope.
Later XACML 3.0 IIC cases and cases outside inherited exclusions/bounds remain
unverified. The existing execution bounds/exclusions limit claims of complete
vendored-corpus coverage even in the passing 2.0 suite.
