# Bugs 3.9 and 4.47: static rejection and JAXB boolean attributes

PASS: the full JDK 21 reactor `clean verify` passed. All four requested static-rejection cases now pass.
JDK: `Java version: 21.0.6, vendor: Oracle Corporation`; Maven 3.9.16. See `jdk-version.txt`.

## Static-rejection harness (3.9)

`ConformanceCaseRunner.java:139` catches only policy-build import assertions. It accepts the precise static rejection for a `special-harness` IIA004/IIC012 case. A loaded policy still reaches the full vendored-response comparison outside that catch. Multi-root handling remains limited to IID029/IID030 (`:117`).

- IIA004 requires `SyntaxException`, syntax-error status, and the exact message `Policy AttributeDesignator is missing AttributeId` (`:170`). The two policy mappers now emit that identified error instead of an unlabelled null-pointer exception: `Xacml20PolicyFromJaxbToObjectModelMapper.java:717`, `Xacml30PolicyFromJaxbToObjectModelMapper.java:577`.
- IIC012 requires the `IllegalArgumentException` from Condition's static boolean return-type check and its exact boolean-versus-integer message. The 2.0 mapper wraps this validation exception in a syntax-error `SyntaxException`; the harness checks that wrapper too. The 3.0 mapper preserves the original validation exception (`ConformanceCaseRunner.java:177`).
- Both suites' `IIA004Special.txt:3` and `IIC012Special.txt:3` permit static rejection. The missing-id and non-boolean defects are shown by the actual policy fixtures. IIC012Special.txt has inherited copy references to IIC003Response/IIA004Policy; this harness uses the IIC012 manifest policy/response, as requested, and leaves that prose unchanged.
- Four manifest rows changed from skipped to enabled special-harness; their bug fields are cleared. OASIS 3.0 IIA023, IID029 and IIIG300 keep their exact adjudicated reason and `policy4j-the.3.9` id. Bug 3.9 remains open for those cases.

`ConformanceStaticRejectionTest.java:42` runs 16 checks: the four named rejections, four loadable correct-response controls, four loadable wrong-response controls (assert the actual Permit versus expected Indeterminate), and four unrelated-rejection controls (swap the other invalid policy). It uses real policy import and PDP evaluation. No mock manufactures the rejection or response.

## Boolean attribute validation (4.47)

`BooleanAttributeValidationFilter.java:44` checks the declared XACML boolean attributes before JAXB's lax converter. It reuses the strict core `BooleanVal.ofAny` parser (`:76`). Invalid lexical text throws a syntax error naming the attribute. `BaseJAXBUnmarshaller.java:84` routes XML source forms through this SAX filter and JAXB's existing UnmarshallerHandler. Prebuilt JAXBElement inputs retain the existing model-mapping path; they contain boolean values, with no original lexical token to inspect.

This unmarshal-time change avoids editing the JAXB bindings or generated code. The actual `xacml.xjb` is under the integrity-locked xsd directory; all files there remain unchanged. The filter checks XACML namespaces and declared element/attribute pairs, preserves omitted 2.0 MustBePresent defaults, and does not enable broad schema validation.

The lexical rule comes from [XML Schema Part 2 section 3.2.2](https://www.w3.org/TR/xmlschema-2/#boolean) and [section 4.3.6](https://www.w3.org/TR/xmlschema-2/#rf-whiteSpace). The SAX integration is supported by the [XML Binding specification, unmarshalling framework](https://github.com/jakartaee/jaxb-api/blob/main/spec/src/main/asciidoc/ch04-binding_framework.adoc); the local JAXB 2.3 runtime is the executed proof. Context7's first Eclipse JAXB RI lookup had no relevant result; resolving JAXB found the official Jakarta XML Binding API documentation, then the two scoped queries succeeded.

| Attribute / location | Valid tokens tested | Invalid tokens tested | Oracle |
| --- | --- | --- | --- |
| 3.0 Request ReturnPolicyIdList | true; false; 1; 0; ` true ` | yes; TRUE; 10; empty | Parsed flag / exact syntax-error code |
| 3.0 Request CombinedDecision | same five | same four | Parsed flag / exact syntax-error code |
| 3.0 request Attribute IncludeInResult | same five | same four | Included attributes / exact syntax-error code |
| 2.0 designator MustBePresent | same five | same four | Parsed reference flag / exact syntax-error code |
| 2.0 selector MustBePresent | same five | same four | Parsed reference flag / exact syntax-error code |
| 3.0 designator MustBePresent | same five | same four | Parsed reference flag / exact syntax-error code |
| 3.0 selector MustBePresent | same five | same four | Parsed reference flag / exact syntax-error code |

The 2.0 request schema has none of these boolean attributes; its policy designator/selector forms are covered. `BooleanAttributeLexicalTest.java:65` contributes 35 accepted-token and 28 rejected-token checks. Eighteen further checks cover valid/invalid text through InputSource, StreamSource, SAXSource, DOMSource, StAXSource, XMLStreamReader, Document, Element and bytes; two check missing AttributeId syntax errors; two check 2.0 omitted MustBePresent defaults. Total: 85. The generic Source path now also handles StAXSource; the old JAXB convenience overload rejected that source form even for valid text.

`MarshallerTestPolicy.xml` is unchanged. `Xacml20ResponseContextMarshallerTest.java:62` has a supplier for it, but its only test does not call that supplier (`:51`). No test was added to load the unrelated invalid boolean AttributeAssignment text.

## Tests first: red, then green

| Receipt | Tests | Failures | Errors | Skips | Meaning |
| --- | ---: | ---: | ---: | ---: | --- |
| 01 XML | 85 | 39 | 13 | 0 | Initial red plus a test setup error in 2.0 Condition XML |
| 02 harness | 16 | 8 | 0 | 0 | Initial red plus a too-specific wrong-response message assertion |
| 03 XML | 85 | 39 | 1 | 0 | Corrected fixture; invalid booleans and missing-id tests fail; valid StAXSource unsupported |
| 03 harness | 16 | 4 | 0 | 0 | Only the four permitted static rejections fail; all 12 controls pass |
| 04 XML | 85 | 0 | 0 | 0 | Small fixes green |
| 04 harness | 16 | 0 | 0 | 0 | Small fixes green |

The initial test setup issues were corrected before production edits. Run 03 uses failure-ignore only to collect both red suites in one reactor; its success exit is not treated as green. Run 04 and the full clean gate do not use that flag. Raw numbered logs and matching JSON receipts retain every run.

## Full clean JDK 21 gate

Command: `JAVA_HOME="$(jenv prefix 21)" rtk mvn -Dlicense.skipUpdateLicense=true -l openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/051-static-rejection-jaxb-boolean/07-full-reactor-clean-verify.log clean verify`

| Module | Tests run | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| xacml-core | 4761 | 0 | 0 | 8 |
| xacml-xml | 142 | 0 | 0 | 0 |
| xacml-json | 24 | 0 | 0 | 1 |
| xacml-pdp | 98 | 0 | 0 | 7 |
| xacml-test | 99 | 0 | 0 | 0 |
| xacml-conformance-tests | 917 | 0 | 0 | 17 |
| xacml-xml-jaxb | 0 | 0 | 0 | 0 |
| **Total** | **6041** | **0** | **0** | **33** |

Core and PDP counts exceed the existing pre-task reports because the clean run also picks up already-present ConditionSafetyNetTest (4), the additional AttributeDesignatorFunctionTest cases, and AttributeResolutionSafetyNetTest (21). Gate 05 passed with 5790 tests and 33 skips. Seven concurrent core/PDP source changes arrived after that gate; this lane preserved them and repeated the full clean gate as run 07. The changed-file and per-class count comparison is indexed in `scope-and-result-audit.json`; `skipped-tests.csv` records all actual skip reasons. Existing reports in `00-existing-reports.json` are historical receipts, not a fresh pre-task build.

## Conformance baseline and every changed case

| Suite | Manifest requests | Executed disposition | Special-harness disposition (enabled) | Passed | Skipped | Failed |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| OASIS 2.0 | 374 | 360 | 4 (4) | 364 | 10 | 0 |
| OASIS 3.0 | 487 | 477 | 4 (3) | 480 | 7 | 0 |
| RSA 2008 | 17 | 17 | 0 | 17 | 0 | 0 |
| Examples | 2 | 2 | 0 | 2 | 0 | 0 |
| **Total** | **880** | **856** | **8 (7)** | **863** | **17** | **0** |

Of 863 passing cases, four use static rejection; 859 evaluate and compare responses. Surefire's conformance module count is 917: 880 manifest cases plus 37 non-case checks (the prior 21 plus this packet's 16 harness controls). Enabled cases all pass.

| Case | Before | After | Observed rejection |
| --- | --- | --- | --- |
| oasis20/IIA004 | skipped | special-harness passed | org.xacml4j.v30.SyntaxException: Policy AttributeDesignator is missing AttributeId |
| oasis20/IIC012 | skipped | special-harness passed | org.xacml4j.v30.SyntaxException: Condition expects an expression with="boolean" return value, but got expression with return value type="integer" |
| oasis30/IIA004 | skipped | special-harness passed | org.xacml4j.v30.SyntaxException: Policy AttributeDesignator is missing AttributeId |
| oasis30/IIC012 | skipped | special-harness passed | java.lang.IllegalArgumentException: Condition expects an expression with="boolean" return value, but got expression with return value type="integer" |

No other case changes outcome, and no executed case is removed. `07-case-results.csv` is the final complete per-case table, with rejection receipts and unchanged skip reasons/bugs; the first passing gate's table remains in `05-case-results.csv`. Fixture integrity and ConformanceManifestTest pass in the full gate.

## Scope, headers and residual risk

Exactly the eight source/manifest files listed in `scope-and-result-audit.json` changed from the initial hash snapshot through this packet's first clean gate. Seven later concurrent core/PDP changes are listed separately in that audit; this lane did not edit them. This packet's eight files remain byte-identical to their first passing gate. `changes.patch` contains this packet's delta rather than the shared worktree's older or concurrent edits. Existing Java license headers are byte-identical; new Java files copy the corresponding module header exactly. All poms, reviewed AnyURI/comparator files, vendored fixtures, schemas and JAXB bindings retain their initial hashes. The final clean gate changed no source hashes.

The scoped tracked-file diff check passes. The whole-worktree diff check still reports pre-existing indentation in `ExpressionTypeBuilder.java:96`; this packet leaves that file byte-identical. `git-status.txt` records the shared dirty state. The direct Java ast-grep check confirms use of the strict BooleanVal parser; it is a syntax check, not independent semantic proof.

No work in this packet remains unverified by its requested gate. The 17 existing conformance skips and the unit safety-net skips retain their reasons. Bug 3.9 remains open for IIA023/IID029/IIIG300. Prebuilt JAXB objects cannot expose original lexical spelling; the new validation applies when XML text/DOM/StAX is unmarshalled through the production unmarshallers. XML unmarshal throughput is UNVERIFIED (not benchmarked). No commit, br command, task tick or issue closure was performed.
