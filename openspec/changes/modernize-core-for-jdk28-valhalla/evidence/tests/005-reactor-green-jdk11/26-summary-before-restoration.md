# Task 1.3 continued: all modules compile; full reactor blocked at conformance

Result: **BLOCKED**, not a green reactor. Latest gate:
`26-reactor-verify-response-comparison.txt`, 660 Surefire tests / 5 failures /
0 errors / 9 existing skips. Every module, including test sources, compiles.
The five remaining failures require core/XML fixes outside the current scope.

The conductor explicitly approved `required-pom-fix.patch`. It was applied
exactly to `xacml-test/pom.xml`: junit:junit:4.13.2, compile scope, six added
lines. No other POM edits. Approval is recorded in `conductor-approval.md`;
`14-approved-pom.txt` and `scope-check.txt` prove the exact resulting POM.
The previous JUnit main-source compilation blocker is resolved.

JDK: `openjdk version "11.0.32.1" 2026-08-18 LTS`;
Zulu11.90+205-CA, build 11.0.32.1+1-LTS (`00-jdk-version.txt`).
Branch: integration/modernize-core-jdk28-valhalla; HEAD
befdd116bd11385ac8d007732fa8e4d347af3143; core API base a8a90d46.
Live core matches a8a90d46 (`scope-check.txt`, exit 0).

## Latest full-reactor gate

Exact command, with Maven logging to retain evidence:

```sh
JAVA_HOME="$(jenv prefix 11)" rtk mvn -Dlicense.skipUpdateLicense=true verify -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/005-reactor-green-jdk11/26-reactor-verify-response-comparison.txt
```

| Module | Tests run | Failures | Errors | Skipped | Reactor result |
| --- | ---: | ---: | ---: | ---: | --- |
| xacml parent | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-core | 494 | 0 | 0 | 4 | SUCCESS |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 | SUCCESS; no tests |
| xacml-xml | 29 | 0 | 0 | 0 | SUCCESS |
| xacml-json | 5 | 0 | 0 | 1 | SUCCESS |
| xacml-pdp | 71 | 0 | 0 | 4 | SUCCESS |
| xacml-test | 20 | 0 | 0 | 0 | SUCCESS |
| xacml-conformance-tests | 41 | 5 | 0 | 0 | FAILURE |
| Total | 660 | 5 | 0 | 9 | BUILD FAILURE |

Counts are from the latest gate's Maven Results, extracted in
`surefire-summary.txt` and `26-module-counts.json`. Method totals are distinct
from cases executed inside grouped conformance methods.

## Conformance results and causes

`26-conformance-testcases.json` preserves case IDs and traces from this run.

| Suite | Surefire methods | Case executions | Failed invocations / IDs |
| --- | ---: | ---: | --- |
| OASIS 2.0 | 10 | 219 | 1: IIC104 |
| OASIS 3.0 | 10 | 214 | 4: IIC104, IIIF001, IIIG006 twice |
| RSA 2008 interop | 17 | 17 | 0 |
| Examples | 2 | 2 | 0 |
| IIA002 resolver characterization | 2 | Not a conformance case | 0 |

OASIS and RSA: **450 case executions, 5 failed invocations, 0 errors**.
Examples: 2 executions, both pass. OASIS counts come from actual
`Executing test=` output, rather than loop bounds. RSA methods execute one
case each. OASIS `testName` executes no case; both suites invoke IIIG006 twice.

All remaining failures have a production cause predating these adaptations:

- **IIC104, OASIS 2.0 and 3.0:** unchanged core
  `ISO8601DateTime.java:190–198` adds a positive duration in minus().
  Production subtraction returns 2002-03-27T10:23:47-05:00 for
  2002-03-22T08:23:47-05:00 - P5DT2H0M0S. The unchanged cases require
  2002-03-17T06:23:47-05:00 and Permit; actual is NotApplicable.
  Design lines 26–27 and tasks 4.3/4.5 already record this baseline defect.
- **IIIF001 and IIIG006, OASIS 3.0:** the base and current helper
  `Xacml30RequestContextFromJaxbToObjectModelMapper.java:473–486` selects only
  the first mixed-content object. These unchanged requests contain
  whitespace / DOM element / whitespace. Production unmarshalling loses
  resource content, causing required selectors to fail (IIIF001:
  Indeterminate) and XPath conditions to fail (IIIG006: NotApplicable).
  This is pre-existing a8a90d46 XML behavior, not a regression in this lane.

`23-conformance-diagnostics.txt` records fresh production-entrypoint proof.
`conformance-blockers.md` records the case-level classification. No observed
failure is attributed to a fixture; broader fixture correctness is UNVERIFIED.

`required-core-fix.patch` and `required-xml-content-fix.patch` are concrete,
checked, **unapplied proposals**. Their post-fix behavior is UNVERIFIED.
Core is forbidden by the packet; XML is under review and outside this
continuation's ownership. Both defects were reported, and an explicit
conductor scope decision was requested. No approval for them has arrived.

## Changed files

This continuation changed twelve project files plus run 005:

| File | Change |
| --- | --- |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java` | Entity instead of EntityValue; id/datatype/issuer designator overload; Optional<Entity> contract; content-only selector resolution without an explicit/generated context selector. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionHandler.java` | Path instead of PathValue; Entity.attributes(); extract ValueType from expression information. |
| `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXPathExpressionLegacyHandler.java` | Entity.findValues() and Entity.attributes(). |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandlerTest.java` | Four tests: selector metadata, missing explicit selectors, issuer/duplicates/Entity retrieval, PIP fallback/caching. |
| `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/request/MultipleResourcesViaXacmlXPathHandlerTest.java` | RequestContext.getAttribute() instead of Entity.single(); one metadata-preserving expansion test. |
| `xacml-test/pom.xml` | Only the conductor-approved compile-scope JUnit dependency. |
| `xacml-test/src/main/java/org/xacml4j/v30/ExpectedAttributeResolverBuilder.java` | value.getEvaluatesTo().getValueType(). |
| `xacml-test/src/test/java/org/xacml4j/v30/ExpectedAttributeResolverBuilderTest.java` | Two tests: scalar/bag datatype, issuer/category, actual resolver output, duplicate multiplicity. |
| `xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java` | Normalize absent optional ID/obligation collections to empty; compare actual obligations using b.getObligation(). |
| `xacml-test/src/test/java/org/xacml4j/v20/Xacml30TestUtilityTest.java` | Fourteen tests: empty/absent collections, rejected missing/unexpected IDs/obligations, version/value differences, real production response marshalling. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolver.java` | StringVal and explicit cast from Value<?>; retain case-insensitive subject matching and role contents. |
| `xacml-conformance-tests/src/test/java/org/xacml4j/v20/Xacml20ConformanceAttributeResolverTest.java` | Two tests: mixed-case matching and unmatched subject. |

All original assertion calls and case expectations remain. The response
helper retains count, identifier, version and obligation-assignment checks.
Reading b.getObligation() corrects a pre-existing expected-versus-expected
bug; new tests reject mismatches formerly accepted by that bug.

## Characterization and intermediate runs

| Run | Observation |
| --- | --- |
| 02 | First PDP characterization written before production edits; compile blocked by removed PathValue. |
| 06 | 68 PDP tests: 3 existing selector errors, 4 skips. |
| 07 / 08 | Content-only selector tests before code change: 21 tests with 3 errors; the same 21 pass after. |
| 13 | All 71 PDP tests pass; reactor stops at the now-resolved JUnit dependency. |
| 14 / 15 | Approved POM clears JUnit errors; new builder tests blocked by ValueExpTypeInfo compile error. |
| 16 | Both builder characterization tests pass. |
| 17 / 18 | New resolver tests precede the fix; conformance compile blocked by removed String class. |
| 19 / 20 | Required Value<?> cast is exposed; run 20 passes xacml-test's 6 tests then stops at that compiler error. |
| 21 | Both resolver tests pass after the cast. |
| 22 | Full compilation succeeds; conformance has 4 failures, 6 errors. IIA001/IIB001/IIC001/IID001/IIE001/IIIA006 stop at the null policy-ID comparison. |
| 23 | Diagnostics confirm core sign bug, XML content loss, absent expected versus empty actual collections. |
| 24 / 25 | Helper tests written first: 14 tests with 2 failures and 6 errors before; the same 14 pass after. |
| 26 | Latest full gate: 660 tests, 5 conformance failures, 0 errors, 9 existing skips. |

Exact commands are in `command.txt`; logs retain failed and passing runs.
Checkout paths are replaced with <repo>. The old `finalize-evidence.py` is
historical; current extraction uses `finalize-approved-continuation.py`.
The compiler appendix below lists every error -> fix; the same mapping is
also in `compiler-fixes.md` with full raw contexts in `compile-diagnostics.json`.

## Scope and residual risk

`scope-check.txt`: 3,342 protected files match the post-approval snapshot;
8 existing Java headers are unchanged and 3 new headers match their module
templates. Whitespace checks and exact approved POM checks pass.
Core, other POMs, XML, JAXB source inputs, conformance fixtures, tasks.md,
evidence/README.md and run 003 were not modified in this approved continuation.

Shared status also contains prior XML edits, another lane's fixture
restoration and a pre-existing JSON resource deletion. They were preserved.
An earlier concurrent ExpressionTypeBuilder change has author UNVERIFIED;
the new snapshot includes it, and the latest gate passes all 29 XML tests.
No staging, commit, br, task tick, issue closure or evidence-index edit.

Task 1.3 remains unfinished. The full reactor requires a scope decision or
another lane for core/XML repairs. Existing case exclusions and loop bounds
remain, and each IIC group stops at IIC104. Later cases and the proposed
repairs' post-fix behavior remain UNVERIFIED.

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
