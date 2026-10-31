# Task 1.3 continued: approved baseline restorations pass; IIC106 remains blocked

Result: **BLOCKED**, not a green full reactor. Latest run:
`31-reactor-verify-baseline-restoration.txt`, 677 Surefire tests / 2 failures /
0 errors / 9 existing skips. Every module and its test sources compile.
The only failed conformance case is IIC106, once in each OASIS suite.

The conductor approved the previously proposed core duration and XML content
fixes. Both were applied after focused tests were written and recorded red.
Only ISO8601DateTime.minus(ISO8601DayTimeDuration)'s sign condition changed in
core production code; the three checked sibling operations needed no change.
XML selection skips whitespace-only mixed-content text before selecting the
DOM element, retaining the existing significant-text fallback.

Approval is recorded in `baseline-restoration-approval.md`. The earlier
compile-scope junit:junit:4.13.2 POM change remains exactly as approved,
documented in `conductor-approval.md`; no other POM edits were made.

JDK: `openjdk version "11.0.32.1" 2026-08-18 LTS`;
Zulu11.90+205-CA, build 11.0.32.1+1-LTS (`00-jdk-version.txt`).
Branch: integration/modernize-core-jdk28-valhalla; HEAD
befdd116bd11385ac8d007732fa8e4d347af3143; core API base a8a90d46.
The newly failing ISO8601Date code matches a8a90d46 exactly (scope check).

## Latest full-reactor gate

```sh
JAVA_HOME="$(jenv prefix 11)" rtk mvn -Dlicense.skipUpdateLicense=true verify -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/005-reactor-green-jdk11/31-reactor-verify-baseline-restoration.txt
```

| Module | Tests run | Failures | Errors | Skipped | Reactor result |
| --- | ---: | ---: | ---: | ---: | --- |
| xacml parent | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-core | 506 | 0 | 0 | 4 | SUCCESS |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-xml | 34 | 0 | 0 | 0 | SUCCESS |
| xacml-json | 5 | 0 | 0 | 1 | SUCCESS |
| xacml-pdp | 71 | 0 | 0 | 4 | SUCCESS |
| xacml-test | 20 | 0 | 0 | 0 | SUCCESS |
| xacml-conformance-tests | 41 | 2 | 0 | 0 | FAILURE |
| Total | 677 | 2 | 0 | 9 | BUILD FAILURE |

Counts are fresh Maven Results from this gate, retained in
`surefire-summary.txt` and `31-module-counts.json`. Surefire method counts
differ from the OASIS cases executed inside each grouped method.

## Approved fixes: tests first and sibling-operation checks

`28-baseline-restoration-before.txt`: the new 12-test duration class runs
before production edits; positive and negative day-time subtraction fail.
The other ten checks pass. Fail-at-end still skips dependent XML tests after
core fails, so a separate XML red run is recorded.

`29-xml-content-before.txt`: the new five-test XML class runs before the fix;
whitespace preceding the element causes content loss and fails. Compact XML,
absent content, whitespace-only content and significant JSON text pass.

`30-baseline-restoration-after.txt`: all 12 duration tests and all 5 XML
tests pass after applying the approved patches. XML assertions preserve
whitespace inside the element, namespace-based XPath access and the existing
JSON fallback. The full gate repeats these tests inside their modules.

| Every ISO8601DateTime operation checked | Positive / negative / zero before | After | Change |
| --- | --- | --- | --- |
| plus(ISO8601DayTimeDuration) | Pass / pass / pass | All pass | None |
| minus(ISO8601DayTimeDuration) | Fail / fail / pass | All pass | Negate sign condition only |
| plus(ISO8601YearMonthDuration) | Pass / pass / pass | All pass | None |
| minus(ISO8601YearMonthDuration) | Pass / pass / pass | All pass | None |

No other operation or core production file was changed.
`33-approved-diagnostics-after.txt` additionally replays real unchanged
conformance requests through the production unmarshaller: IIIF001 and
IIIG006 now retain resource content; IIC104 subtraction yields the correct
date; the IIA001 empty-response comparison passes.

## Conformance results and classifications

`31-conformance-testcases.json` retains every latest method's case IDs and
failure trace. OASIS execution counts come from actual `Executing test=`
output; RSA's 17 methods each execute one interop case.

| Suite | Surefire methods | Case executions | Failed invocations / IDs |
| --- | ---: | ---: | --- |
| OASIS 2.0 | 10 | 221 | 1: IIC106 |
| OASIS 3.0 | 10 | 221 | 1: IIC106 |
| RSA 2008 interop | 17 | 17 | 0 |
| Examples | 2 | 2 | 0 |
| IIA002 resolver characterization | 2 | Not a conformance case | 0 |

OASIS/RSA: **459 case executions, 2 failed invocations, 0 errors**.
Examples: both pass. Each OASIS suite has a non-case testName method and
invokes IIIG006 twice. Existing exclusion sets and loop bounds remain; an
assertion stops each IIC group at IIC106. Later cases are UNVERIFIED.
Each suite retains 30 explicit case exclusions (60 total), recorded in
31-existing-case-exclusions.json; these are separate from Surefire skips.
Early aborts and cases outside inherited bounds are not counted as skips.

Resolved failures from the earlier gate are confirmed in run 31:

| Earlier case failure | Classification | Current disposition |
| --- | --- | --- |
| IIC104, both OASIS suites | Pre-existing core sign defect | Pass after approved ISO8601DateTime fix |
| IIIF001, OASIS 3.0 | Pre-existing XML mixed-content selection defect | Pass after approved mapper fix |
| IIIG006, both OASIS 3.0 invocations | Same XML defect | Both pass |
| IIA001/IIB001/IIC001/IID001/IIE001/IIIA006 comparison errors in run 22 | Pre-existing harness/test-support absent-versus-empty collection handling | Pass after the owned comparator repair; assertions retained |

**New blocker: IIC106 in both OASIS suites.** Classification:
**pre-existing core defect**, not an adaptation regression, XML defect,
harness failure or fixture failure. The unchanged cases call
date-add-yearMonthDuration(2002-03-22, -P1Y2M), requiring 2001-01-22 and Permit.
Production returns 2002-03-22; equality is false and the decision is
NotApplicable.

`ISO8601Date.java:145–150` uses toDuration() in both year/month operations.
`ISO8601Duration.java:36–37` converts only day/time fields, so these operands
become PT0S. ISO8601Date.java remains unchanged from a8a90d46.
The retained run-31 IIC output tails show the operands and unchanged result.

`32-date-period-diagnostics.txt` checks production date plus/minus with
positive, negative and zero year-month operands: 6 checks, 4 nonzero checks
fail, both zero checks pass. Existing toPeriod() retains signed calendar
fields; all six independent candidate calculations match expected dates.
This is diagnosis/candidate evidence, not a post-application gate.

`required-date-period-fix.patch` is a checked, **unapplied** two-line proposal
to use toPeriod() in ISO8601Date.plus/minus. The latest packet explicitly
forbids other core files, so this source was not edited. A new conductor
scope decision was requested; focused tests must precede application if
authorized. Broader fixture correctness and later IIC behavior are UNVERIFIED.

## Changed files

This continued lane changed sixteen project files plus run 005:

| File | Change |
| --- | --- |
| `xacml-core/src/main/java/org/xacml4j/v30/types/ISO8601DateTime.java` | Conductor-approved day-time subtraction sign condition only. |
| `xacml-core/src/test/java/org/xacml4j/v30/types/ISO8601DateTimeArithmeticTest.java` | New twelve-test signed/zero truth table covering four duration operations. |
| `xacml-xml/src/main/java/org/xacml4j/v30/xml/Xacml30RequestContextFromJaxbToObjectModelMapper.java` | Conductor-approved whitespace-only mixed-content filtering before selection. |
| `xacml-xml/src/test/java/org/xacml4j/v30/xml/Xacml30RequestContentTest.java` | New five-test production unmarshaller characterization. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java` | Entity instead of EntityValue; id/datatype/issuer designator overload; Optional<Entity> contract; content-only resolution without an explicit/generated context selector. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java` | Path instead of PathValue; Entity.attributes(); ValueType extraction. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java` | Entity.findValues() and Entity.attributes(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java` | Four selector metadata, issuer/duplicate/Entity and PIP/cache tests. |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java` | RequestContext.getAttribute() instead of Entity.single(); metadata-preserving expansion test. |
| `xacml-test/pom.xml` | Exactly the conductor-approved compile-scope junit:junit:4.13.2 patch. |
| `xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java` | Extract ValueType from getEvaluatesTo(). |
| `xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java` | Two scalar/bag datatype, issuer/category, resolver-output and multiplicity tests. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java` | Normalize absent optional ID/obligation collections to empty; read actual obligations. |
| `xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java` | Fourteen empty/mismatched collection and production response-marshalling tests. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java` | StringVal and explicit Value<?> cast; preserve IIA002 matching and role contents. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java` | Two mixed-case/unmatched-subject resolver tests. |

All original assertion calls and case expectations remain. The response
helper retains count, identifier, version and obligation-assignment checks;
reading actual obligations fixes the former expected-versus-expected bug.
The compiler appendix records every compiler error -> fix, including the
four corrected temporary edit/setup diagnostics. No compiler errors remain.

## Evidence and scope

Exact commands are in `command.txt`; failed and passing receipts are retained.
`26-summary-before-restoration.md` preserves the prior summary.
Checkout paths are normalized to <repo>. Current evidence extraction uses
`finalize-baseline-restoration.py`; earlier finalizers are historical.

`scope-check.txt`: 3,340 protected files match the new pre-write snapshot;
10 edited existing Java headers are unchanged and 5 new headers match their
module templates. Both main-source edits match their approved patch exactly.
The approved POM remains exact, whitespace checks pass, and ISO8601Date.java
matches a8a90d46. Core files other than the approved ISO8601DateTime source
and new focused test are untouched. Other XML changes, generated/vendored
source inputs, conformance fixtures, tasks.md, evidence/README.md and run 003
are preserved.

Shared status includes prior XML edits, another lane's fixture restoration
and a pre-existing JSON resource deletion. These were preserved. The earlier
concurrent ExpressionTypeBuilder change has author UNVERIFIED.
No staging, commit, br, task tick, issue closure or evidence-index edit.

Task 1.3 remains unfinished: the required full gate is not green.
ISO8601Date repair needs a new scope decision or another lane. Proposed
post-fix behavior, later IIC cases and complete vendored-case coverage remain
UNVERIFIED; existing exclusions and group bounds are unchanged.

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
