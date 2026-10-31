<!-- Lane TV3 of the test-vector research swarm, Claude Fable 5.1, read-only; scope: this repository's conformance module and a streamlined design; report from the NTM pane, paths made repository-relative. -->

TV3, read-only, 2026-10-27. Scope: this repository. All paths are repository-relative. Nothing was written to the repository; no Maven, tests, benchmarks, git or `br` commands ran.

Research tools of this lane and their status: `mcp__tavily__tavily_search` SUPPORTED (five queries, listed in section 2); `mcp__duckduckgo__search` available, not used (no Tavily failure); `WebSearch` and `WebFetch` deferred, not used; GitHub MCP available, not used (no claim needed it). Local evidence came from file reads, `rtk rg`, and two read-only Python scripts run from this lane's scratch directory outside the repository (`tv3_inventory.py` writes `tv3-inventory.md`; `tv3_experimental.py` prints the EXPERIMENTAL case ids); every number below cites the file it was derived from.

## 1. How each suite is wired today

Module: `xacml-conformance-tests` (`pom.xml:9`), depends on `xacml-test` and `xacml-xml` only (`pom.xml:16-27`); `xacml-json` is not a dependency, so no JSON vector can run today. The license plugin is kept off `oasis-xacml20-compat-test/*`, `rsa2008-interop/XacmlPolicySet*` and `Example_*/*` (`pom.xml:13`). Surefire excludes `**/*TestCase.java` (root `pom.xml:388-390`). Seventeen test classes live under `src/test/java/org/xacml4j/v20` and `.../logging`.

Common machinery:

- Case discovery: one unquoted CSV, `src/test/manifest/conformance-manifest.csv`, 880 rows, header `suite,case_id,request_file,policy_files,expected_response_file,disposition,enabled,reason,bug_id` (`ConformanceManifest.java:38-40`; counts from the inventory script). `ConformanceManifest.cases(suite)` filters rows by suite (`:61-67`); `read` validates nine fields, unique `suite/case_id`, dispositions `executed|special-harness|skipped`, the enabled flag rules, a mandatory reason and a bug id for disabled rows, and safe relative paths (`:69-120`). `validateFixtureInventory` derives the expected row keys from file names: `<id>Request.xml` under the two OASIS directories, `XacmlRequest-<id>.xml` under `rsa2008-interop`, `Example_*/Request.xml`, and skips `xacml3.0-deprecated` (`:122-194`). `ConformanceManifestTest.manifestExactlyCoversActiveVendoredRequests` pins the row counts 374, 487, 17, 2 (`ConformanceManifestTest.java:140-146`).
- Row dispositions today (inventory of the manifest): oasis20 362 executed, 5 special-harness enabled, 7 skipped; oasis30 476 executed, 6 special-harness enabled, 1 special-harness disabled, 4 skipped; rsa2008 17 executed; example 2 executed. The 12 disabled rows and their reasons are the skip ledger: oasis20 IIIC002, IIIC003 (hierarchical Children/Descendants scope not expanded, bead policy4j-the.4.18), IIIG001 to IIIG005 (XACML 2.0 XPath over request nodes, bead policy4j-the.4.20); oasis30 IIA023 (fixture timezone -14:30 beyond the XML Schema range, bead policy4j-the.3.9), IID029 (fixture category mismatch, same bead), IIIC002, IIIC003 (as above), IIIG300 (fixture expects 2 policy ids, errata §5.48 gives 3, bead policy4j-the.3.9).
- Skips: `ConformanceCaseRunner.requireEnabled` turns a disabled row into a JUnit assumption with the reason and bug id, so Surefire counts it as skipped (`ConformanceCaseRunner.java:72-76`).
- PDP per case: `buildPdp` creates a new `InMemoryPolicyRepository` with the default functions and algorithms (`:66-68,83-86`), imports every `policy_files` entry with the suite's media type (`mediaType`: `oasis30` is XACML 3.0 XML, everything else XACML 2.0 XML, `:78-81`), takes the first policy as root (`:120`), and registers the default resolvers plus `Xacml20ConformanceAttributeResolver` for both OASIS suites (`:129-132`). Hard-coded case logic: IIE003 asserts that `IIE003PolicyId2.xml` fails the static type check and continues with the rest (`:107-116`); IID029 and IID030 wrap two roots in a synthetic policy set with only-one-applicable (`:121-128`).
- Attribute resolution: `Xacml20ConformanceAttributeResolver` returns role `Physician` for subject `Julius Hibbert`, case-insensitively (`Xacml20ConformanceAttributeResolver.java:38-59`). The 3.0 suite ships the same fact as data in `src/test/resources/xacml3.0-ct-v.0.4/PIP.txt` (one pipe-separated row: category, attribute id, data type, value), which nothing reads (`rtk rg` for `PIP.txt` in `src/test/java`: no hit).
- Expected-result comparison, OASIS suites (`verifyOasis`, `:143-187`): the request is parsed with the version's unmarshaller; a `SyntaxException` is mapped to an Indeterminate syntax-error response only for IIA005 with the exact message `Request Attribute is missing AttributeId` (`:158-174,215-222`; ruling D12); the model response is marshalled back to JAXB and compared to the JAXB-parsed fixture. 3.0 comparison (`xacml-test/src/main/java/org/xacml4j/v20/Xacml30TestUtility.java:73-90`): decision, policy identifier list in order, included attributes as a multiset, status code value only, obligations and advice as multisets with typed value comparison and NaN-equal doubles (`:135-159`). 2.0 comparison (`xacml-test/src/main/java/org/xacml4j/v20/Xacml20TestUtility.java:59-120`): decision, resource id, status code chain and status detail content, obligations as a multiset. Static rejections are accepted only for IIA004 (syntax error, exact message) and IIC012 (non-boolean condition, exact message) (`ConformanceCaseRunner.java:224-241`). IIC350 and IIC358 have their expected decision rewritten in memory from Permit to NotApplicable (`expectedResponse30`, `:189-213`), the NaN adjudication of XACML 3.0 A.3.1.
- Expected-result comparison, rsa2008 and examples: `RSA2008InteropTest` and `XacmlExampleTest` call `XacmlPolicyTestSupport.verifyXacml20Response` (`RSA2008InteropTest.java:41-47`, `XacmlExampleTest.java:41-47`), which compares model objects: decision, full `Status` equality including message, obligations by id, effect and attribute multiset, advice by id only, included categories by reference id and category only, policy identifiers by id (`xacml-test/src/main/java/org/xacml4j/v30/XacmlPolicyTestSupport.java:174-251`).
- XACML 2.0 versus 3.0: version is a property of the suite, not the case; the 2.0 unmarshaller maps requests into the 3.0 model and the response marshaller maps back; rsa2008 and the examples go through the 2.0 path of `XacmlPolicyTestSupport`.

Per suite:

| Suite | Directory | Files | Index and helpers | Runner | Cases | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| oasis20 | `src/test/resources/oasis-xacml20-compat-test` | 1,143: 1,130 XML, 12 `*Special.txt`, `ConformanceTests.html` | Index "XACML Version 2.0 Conformance Tests (draft)", version 0.4, 10 Oct 2005, author Argyn Kuketayev (`ConformanceTests.html:3,10-12`) | `Xacml20ConformanceTest` | 374 rows; 362 executed | Triples `<id>Request/Policy/Response.xml`; IID029/030 two policies; IIE001-003 root plus two referenced files (manifest rows); 47 cases marked EXPERIMENTAL in the index (IIC231, IIC232, IIE003, IIIA001-028, IIIC001-003, IIIF001-007, IIIG001-006; `tv3_experimental.py` over the index) |
| oasis30 | `src/test/resources/xacml3.0-ct-v.0.4` | 1,707: 1,685 XML (216 under `xacml3.0-deprecated`), 14 txt, 5 `*Repository.properties`, 2 JSON, `ConformanceTests.html` | Index "XACML Version 3.0 Conformance Tests (draft)", version 0.5, January 2014, contributors incl. Glenn Griffin (`ConformanceTests.html:3,10-11`); directory name says 0.4 | `Xacml30ConformanceTest` | 487 rows; 476 executed; 72 deprecated cases never run | `IIE00x Repository.properties` list referenced policies (`IIE001Repository.properties:1`), duplicated by the manifest; `conformance.txt` is an upstream run log (543 tests, 6 unknown functions, `conformance.txt:1-9`); `IIA022Response.json` and `IIIE303Response.json` are JSON-profile responses nothing reads; `IIA003mvbPolicy.txt` is a stray 2.0-syntax policy; 232 of 559 linked ids marked EXPERIMENTAL (all 3xx cases, IIIA, IIIC, IIIE, IIIF, IIIG and every `d` case) |
| rsa2008 | `src/test/resources/rsa2008-interop` | 43 XML: 9 policy sets, 17 requests, 17 responses | none | `RSA2008InteropTest` | 17 rows, all executed; each imports all 9 policy sets | `RSA2008PerformanceTestCase` rebuilds the same PDP and loops 10,000 times (`RSA2008PerformanceTestCase.java:53-145`), excluded by Surefire; the 34 request and response files carry the project's LGPL header (`XacmlRequest-01-01.xml:2-14`), the policy sets do not |
| example | `src/test/resources/Example_1`, `Example_2` | 3 and 9 files (`record.xsd`, `Barts_record.xml`, 4 rules, a policy set) | none | `XacmlExampleTest` | 2 rows | The XACML 2.0 specification's section 4 examples (`Example_2/Policy_set.xml:4`, `Example_1/Rule_1.xml:2-11`: ids `urn:oasis:names:tc:xacml:2.0:example:*`) |

Duplicated between runners: the two OASIS test classes are identical apart from the suite string, including a stray `testName` unit test (`Xacml20ConformanceTest.java:37-59`, `Xacml30ConformanceTest.java:37-59`); the RSA and Example classes are identical; three response comparators with three strictness levels (`Xacml20TestUtility`, `Xacml30TestUtility`, `XacmlPolicyTestSupport`); two request-loading stacks; PDP construction repeated in `RSA2008PerformanceTestCase.init`; `MANIFEST_PATH` declared twice (`ConformanceManifest.java:38`, `NaNFixtureAdjudicationTest.java:69`); special-instruction knowledge split between manifest rows, string literals in `ConformanceCaseRunner`, and three guard test classes (`ConformanceStaticRejectionTest`, `ConformanceRequestSyntaxErrorTest`, `NaNFixtureAdjudicationTest`).

Hard to extend: a new suite needs a `SUITES` entry (`ConformanceManifest.java:41`), a `collect` call with its own file-name convention (`:125-127`), a new test class, and new branches in `mediaType`, `buildPdp`, `verifyOasis`; a special instruction needs Java per case id; the manifest cannot hold commas in reasons (rows 150-151 of the manifest write `(NaN;NaN)`), has no provenance, license, version, profile or feature columns, and knows only a full response file as the expected outcome; JSON vectors have no path; version is per suite; one PDP is built per case (880 builds, 153 policy-set imports for rsa2008 alone).

## 2. Sources of test vectors (the vendored suites, one row each)

Fetched facts, from Tavily results of 2026-10-27: the OASIS XACML TC page (now "ACAL, supersedes XACML") lists "XACML 2.0 Interop at RSA 2008, San Francisco. See Interop scenarios (HealthCare) (zip)" and the 1.1 conformance test page `https://www.oasis-open.org/committees/xacml/ConformanceTests/ConformanceTests.html` (`https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=acal`); the REST profile states that the XACML TC operates under the "RF on Limited Terms" IPR mode (`https://docs.oasis-open.org/xacml/xacml-rest/v1.0/xacml-rest-v1.0.html`); the OASIS IPR Policy grants OASIS a perpetual copyright licence over contributions "to copy, publish, and distribute ... and to prepare derivative works ... for the purpose of developing and promoting the OASIS Deliverable and enabling the implementation of the same" (`https://www.oasis-open.org/policies-guidelines/ipr`). Not found: a current OASIS URL for the 3.0 suite archive "xacml-3.0-ct v0.4/0.5" (UNVERIFIED; the index cites only the 1.0/1.1 zip `http://www.oasis-open.org/committees/download.php/6076/ConformanceTests.zip`, `ConformanceTests.html:111`). Inference: none of the vendored suites carries a licence text of its own; their reuse rests on OASIS publishing them "as an aid in achieving conformance" (`ConformanceTests.html:60-67` in both indexes) and on the IPR policy quoted above.

| Project and URL | Licence of the test files | Versions and profiles | Format and layout | Count | Origin | Last change seen | Overlap with vendored suites | Adoption needs | LGPL v3 vendoring |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| OASIS XACML 2.0 conformance tests, upstream index `ConformanceTests.html` (local copy `src/test/resources/oasis-xacml20-compat-test/ConformanceTests.html`); upstream URL not recorded in the repository | No LICENSE file, no copyright line in the index or fixtures (`rtk rg -i opyright`: no hit outside rsa2008); OASIS TC work product under the OASIS IPR Policy; "non-normative" (`ConformanceTests.html:69-76`) | XACML 2.0 core; optional areas obligations, defaults, hierarchical resources, ResourceContent, multiple decisions, selectors, non-mandatory functions (`:43-53`) | Triples `<id>Request.xml`, `<id>Policy.xml`, `<id>Response.xml`; expected result is a full response document; `*Special.txt` per special case; groups II.A to III.G | 374 cases | OASIS: converted by script from the 1.0/1.1 suite, "not tested with XACML 2.0 implementations yet" (`:86-93`) | Index version 0.4, 10 Oct 2005 (`:10`) | It is the vendored suite | Already wired; 7 skipped cases need the hierarchical-resource and 2.0 XPath features | Yes, as today: unmodified data files, no code linkage; add a NOTICE with origin and the OASIS IPR statement (none exists today) |
| OASIS XACML 3.0 conformance tests, upstream index local copy `src/test/resources/xacml3.0-ct-v.0.4/ConformanceTests.html`; upstream URL UNVERIFIED | Same as above (`ConformanceTests.html:93-100`) | XACML 3.0 core, "Release 3.0 Features" group, deprecated identifiers of §10.2.9 (`:41,54-76`); two JSON-profile responses | Triples plus `*Repository.properties`, `PIP.txt`, `*Special.txt`, `xacml3.0-deprecated/<id>d*.xml` | 487 active, 72 deprecated | OASIS: converted from the 1.0/1.1 suite, "run against one implementation and adjusted as needed" (`:110-116`), so some expectations encode that implementation's reading (IIC350/358 NaN, IIIG300 policy id list, IIA023 timezone, IID029 category: manifest rows 148-156) | Index version 0.5, January 2014 (`:10`); directory name 0.4 | It is the vendored suite | Deprecated cases not executed; JSON responses unused | Yes, as above |
| RSA 2008 XACML 2.0 interop, "Interop scenarios (HealthCare) (zip)" linked from `https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=acal` | No licence text; the vendored requests and responses carry a wrong "Copyright (C) 2009 - 2014 Xacml4J.org" LGPL header added by the licence plugin (`src/test/resources/rsa2008-interop/XacmlRequest-01-01.xml:2-14`; `pom.xml:13` excludes only `XacmlPolicySet*`) | XACML 2.0; healthcare role scenario, policy set references, obligations | 9 policy sets, requests `XacmlRequest-NN-NN.xml`, responses `XacmlResponse-NN-NN.xml` | 17 | OASIS TC interop event, participant-contributed | Unknown; event 2008 | It is the vendored suite | None | Yes, but the misattributed headers should be removed (a deliberate upstream-file change, see `GenerateVendoredFixtureManifest --write`) and a NOTICE added |
| XACML 2.0 specification examples (`Example_1`, `Example_2`) | Derived from the OASIS specification text; no notice in the files | XACML 2.0; selectors, variables, obligations, policy set with reference (`Example_2`) | Rule files, a policy set, a request, a response, the record XML and XSD | 2 | OASIS specification section 4 examples, transcribed | Unknown | It is the vendored suite | None | Yes; the OASIS document notice permits derivative works that assist implementation if the notice is kept, so add it |

Rows for other engines' suites come from the other research lanes; this lane did not leave the repository for them.

## 3. A streamlined design

Goals: one descriptor format, one JUnit 5 runner, one skip ledger, adapters only where a suite needs them, provenance and licence recorded next to the data.

Descriptor per suite, `src/test/suites/<suite>/suite.yaml`, with a case table next to it:

```yaml
id: oasis-xacml30-ct
title: OASIS XACML 3.0 conformance tests (index 0.5, Jan 2014; directory v0.4)
xacml: "3.0"
media: xacml+xml            # xacml+xml | xacml+json
root: xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4
provenance:
  origin: oasis             # oasis | oasis-derived | project | <engine name>
  url: <upstream archive or directory URL>
  retrieved: 2014-01-?? / commit <sha> of this repository
  license: {file: NOTICE.md, note: OASIS TC work product, no licence text in files}
  integrity: src/test/integrity/vendored-fixtures.sha1   # existing ledger
layout: triples             # <id>Request.xml / <id>Policy.xml / <id>Response.xml; cases.csv overrides
adapter: oasis-xml          # oasis-xml | rsa2008 | json | <engine>
pip: PIP.txt                # table-driven attributes: category|attributeId|dataType|value
compare: full               # default strictness: decision | decision+status | full
cases: cases.csv
```

`cases.csv`, quoted CSV, one row per case:

```text
id,group,policies,request,expected,expected_status,compare,tags,special,skip_reason,bead,note
IIA001,II.A,,,,,,"mandatory",,,,
IIE003,II.E,"IIE003Policy.xml;IIE003PolicyId1.xml;IIE003PolicyId2.xml",,,,,"mandatory;reference","reject-policy:IIE003PolicyId2.xml:type-mismatch",,,"IIE003Special.txt method 1"
IIC350,II.C,,,,"ok",,"experimental;nan","expect-decision:NotApplicable:XACML 3.0 A.3.1",,"policy4j-the.4.6","fixture expects Permit"
IIA005,II.A,,,,,,"mandatory","reject-request:syntax-error:Request Attribute is missing AttributeId",,,"ruling D12"
IID029,II.D,"IID029Policy1.xml;IID029Policy2.xml",,,,,"repository","multi-root:only-one-applicable","fixture category mismatch","policy4j-the.3.9",
IIIC002,III.C,,,,,,"optional;hierarchical",,"hierarchical scope not expanded","policy4j-the.4.18",
```

Rules: empty `policies`, `request`, `expected` mean the layout's default names; `expected` may name a response file (XML or JSON) or an inline `decision:Permit` for engines that publish only decisions; `expected_status` is the status code when the file does not carry one; `tags` carry profile, feature and provenance labels (`mandatory`, `optional`, `experimental`, `deprecated`, `json`, `multiple-decision`, `entity`); `special` holds the machine-readable special instruction, so no case id lives in Java; a non-empty `skip_reason` requires a `bead` and makes the case an assumption failure; the loader rejects unknown tags and special verbs. The skip ledger is the union of the `skip_reason` columns, printed by one test as a table (today's `requireEnabled` message), and `ConformanceManifestTest`'s inventory rules move to a `SuiteDescriptorTest` that checks every descriptor against its directory, including the deprecated directory.

Runner and adapter API:

```java
public class ConformanceSuiteTest {
    @TestFactory
    Stream<DynamicContainer> suites() throws IOException {
        return SuiteDescriptor.discover(Path.of("src/test/suites")).map(suite ->
            dynamicContainer(suite.id(), suite.cases().map(c ->
                dynamicTest(c.id(), () -> {
                    assumeTrue(c.enabled(), () -> c.id() + ": " + c.skipReason() + " [" + c.bead() + "]");
                    SuiteAdapter adapter = SuiteAdapter.forId(suite.adapter());
                    Outcome actual = adapter.decide(suite, c);          // handles special verbs
                    adapter.comparator(suite, c).assertSame(c.expected(suite), actual);
                }))));
    }
}

public interface SuiteAdapter {
    PolicyDecisionPoint pdp(SuiteDescriptor suite, CaseDescriptor c);   // policies root-first, PIP table, multi-root
    RequestContext request(SuiteDescriptor suite, CaseDescriptor c);    // XML 2.0, XML 3.0, JSON
    Outcome decide(SuiteDescriptor suite, CaseDescriptor c);            // applies reject-policy / reject-request / expect-decision
    ResponseComparator comparator(SuiteDescriptor suite, CaseDescriptor c);  // decision | decision+status | full
}
```

One model-level `ResponseComparator` replaces the three comparators; strictness `full` compares decision, status code, obligations and advice with assignments, included attributes with values, and policy identifiers in order; `decision+status` serves engines whose vectors state only a decision and status. A `TablePip` resolver built from `PIP.txt` or from a `pip:` block in the descriptor replaces the hand-written resolver. The JSON adapter uses `xacml-json`'s `JsonRequestContextUnmarshaller` and `JsonResponseContextUnmarshaller` (`xacml-json/src/main/java/org/xacml4j/v30/marshal/json/`), which needs the module dependency added to `pom.xml`. PDPs are cached per distinct policy list within a suite, which turns the 17 rsa2008 builds into one.

Mapping of today's classes:

| Class | Action |
| --- | --- |
| `ConformanceManifest` | Replace by `SuiteDescriptor` and `CaseDescriptor` (quoted CSV or YAML, new columns); keep its validation rules |
| `ConformanceManifestTest` | Merge into `SuiteDescriptorTest` |
| `Xacml20ConformanceTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`, `XacmlExampleTest` | Merge into `ConformanceSuiteTest`; delete the `testName` tests |
| `ConformanceCaseRunner` | Split into `OasisXmlAdapter`, `Rsa2008Adapter` (or the same adapter with a policy-list layout), `JsonAdapter`, `ResponseComparator`; the case ids become descriptor `special` values |
| `Xacml20ConformanceAttributeResolver`, its test | Delete; `TablePip` reads `PIP.txt` |
| `ConformanceStaticRejectionTest`, `ConformanceRequestSyntaxErrorTest`, `NaNFixtureAdjudicationTest` | Keep as adapter tests of the `special` verbs, parameterised by descriptor rows rather than case ids |
| `ConformancePolicyImportTest` | Keep (error messages of the adapter) |
| `GenerateVendoredFixtureManifest`, `VendoredFixtureIntegrityTest` | Keep; the descriptor's `provenance.integrity` points at the ledger |
| `RSA2008PerformanceTestCase` | Delete; `xacml-jmh` already benchmarks the rsa2008 corpus (`AGENTS.md`, module table) |
| `LoggingConfigurationTest` | Keep |
| `xacml-test` `Xacml20TestUtility`, `Xacml30TestUtility`, `XacmlPolicyTestSupport.assertResponse` | Reduce to loaders; comparison moves to `ResponseComparator` |

## 4. Coverage gaps

Method: the inventory script collected `FunctionId`, `MatchId`, `DataType`, `RuleCombiningAlgId` and `PolicyCombiningAlgId` values from every XML fixture and the ids registered by `@XacmlFuncSpec` and `@XacmlLegacyFunc` under `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl`, the algorithm classes listed in `policy/combine/impl/DefaultXacml30DecisionCombiningAlgorithms.java:36-73`, and the constants of `xacml-core/src/main/java/org/xacml4j/v30/types/XacmlTypes.java:44-157`. Registered: 275 function ids plus 39 legacy ids in 16 provider classes (`XacmlDefaultFunctions.java:42-57`), 25 algorithm registrations, 19 data types. Used by fixtures: 246 distinct function ids (3.0 suite 246, 2.0 suite 194, rsa2008 6, examples 7); every fixture id is registered; 24 algorithm ids; 17 data types.

Functions no vendored vector covers:

- XACML 3.0 A.3.9 conversion functions, 26 ids: `urn:oasis:names:tc:xacml:3.0:function:{anyURI,boolean,date,dateTime,dayTimeDuration,dnsName,double,integer,ipAddress,rfc822Name,time,x500Name,yearMonthDuration}-from-string` and `string-from-{anyURI,boolean,date,dateTime,dayTimeDuration,dnsName,double,integer,ipAddress,rfc822Name,time,x500Name,yearMonthDuration}`.
- `urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case` (A.3.1); `urn:oasis:names:tc:xacml:2.0:function:string-concatenate` (A.3.9; only the deprecated `uri-string-concatenate` appears, in `xacml3.0-deprecated/IIC500dPolicy.xml`); `urn:oasis:names:tc:xacml:2.0:function:time-in-range` (A.3.8).
- A.3.13 regular-expression functions on non-string types: `urn:oasis:names:tc:xacml:2.0:function:{anyURI,dnsName,ipAddress,rfc822Name,x500Name}-regexp-match` (only `string-regexp-match` is exercised, 23 files).
- A.3.10 and A.3.11 on `ipAddress` and `dnsName`: `urn:oasis:names:tc:xacml:2.0:function:{dnsName,ipAddress}-{bag,bag-size,is-in,one-and-only}`; the engine's `ipAddress-{at-least-one-member-of,intersection,set-equals,subset,union}` are registered under a `1.0` prefix and no `dnsName` set function is registered at all; whether the standard ids carry the `2.0` prefix is UNVERIFIED against §10.2.8 and should be checked before vectors are written. The two types appear in fixtures only as request and response attribute values (4 uses each, 3.0 suite).
- `urn:oasis:names:tc:xacml:3.0:function:access-permitted` (A.3.16, optional): not registered (`rtk rg`: no hit in `xacml-core/src/main/java`).
- Entities profile: `entity-bag`, `entity-bag-size`, `entity-equal`, `entity-one-and-only` (registered with a `1.0` prefix) and `urn:oasis:names:tc:xacml:3.0:function:attribute-designator`, `attribute-selector`; no vector, data type `entity` unused; the profile's own ids are UNVERIFIED.
- Engine-specific ids that no OASIS vector can cover: `urn:xacml4j:names:tc:xacml:1.0:function:{entity-bag-is-empty,string-bag-is-empty,string-normalize-to-upper-case}`, `urn:oasis:names:tc:xacml:3.0:function:uri-{contains,ends-with,starts-with}`, and the `3.0` aliases of `date-equal`, `dateTime-equal`, `time-equal`, plus `1.0`-prefixed `*-regexp-match` aliases.

Data types: `jsonPathExpression` and `entity` have no vector; `base64Binary` and `hexBinary` only through equality and bag functions; the 2.0 suite uses the `WD-xquery-operators` duration ids, which `XacmlTypes` accepts as aliases (`XacmlTypes.java:74-75,150-151`).

Combining algorithms: all sixteen 3.0 ids and the eight legacy 1.0/1.1 ids are exercised; `urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second` (Additional Combining Algorithms Profile 1.0, registered at `DefaultXacml30DecisionCombiningAlgorithms.java:59`) has no vector; the legacy ordered algorithms are exercised only by the deprecated directory, which never runs.

Features with no or almost no vector (element counts from the inventory script): `VariableDefinition` and `VariableReference` appear only in `Example_2` (2 each); `CombinerParameters`, `RuleCombinerParameters`, `PolicyCombinerParameters`, `PolicyIssuer`, `RequestDefaults`, `CombinedDecision="true"`: no file; `MaxDelegationDepth` only in `IIF310Policy.xml` and `IIF311Policy.xml`; `ReturnPolicyIdList="true"`: 2 requests; `MultiRequests`: 1 request (IIIE); hierarchical resources: 3 cases, skipped; XACML 2.0 XPath over request nodes: 5 cases, skipped; JSON requests: none (two JSON responses unused); the 72 deprecated-identifier cases of §10.2.9 are vendored but not executed; no vectors for the REST, JSON, Entities, Time Extensions or Dynamic Attribute Authority profiles.

Comparator blind spots: the 3.0 comparator checks the status code only, so status messages, status details and nested minor codes are never verified for 3.0; advice assignments are compared, but the rsa2008 and example path compares advice by id only.

## 5. Findings, refuted ideas and open questions

- Finding, provenance defect: 34 rsa2008 request and response files carry the project's LGPL header and copyright (`src/test/resources/rsa2008-interop/XacmlRequest-01-01.xml:2-14`); `pom.xml:13` excludes only `XacmlPolicySet*`. Removing the headers is a deliberate vendored-file change that needs `GenerateVendoredFixtureManifest --write` (`docs/ai/TESTING.md:93-97`).
- Finding: the 3.0 directory name (`v.0.4`) and its index version (0.5, January 2014) disagree; the descriptor should record both and the upstream URL once found.
- Finding: three upstream helper conventions are already machine-readable and unused: `PIP.txt`, `*Repository.properties`, and the two JSON responses.
- Refuted: a single `@ParameterizedTest` over all suites. It flattens 880 cases into one method; `@TestFactory` containers keep suite and group structure in Surefire and allow per-suite setup such as a cached PDP.
- Refuted: deriving expectations from decisions only. The vendored OASIS responses carry obligations, advice, included attributes and policy id lists that the harness verifies today; `compare` per case keeps that strictness and still admits decision-only vectors from other engines.
- Open: whether to execute the 72 deprecated 3.0 cases (the engine registers every legacy id they use); whether `xacml-json` becomes a test dependency of this module; where to host the NOTICE files for the five sources; the standard ids of the `ipAddress` and `dnsName` set functions (UNVERIFIED) before any new vector targets them.

FINAL REPORT (TV3 JMH benchmark engineer lane, read-only research, Claude Fable 5.1)

Summary: four runners and three comparators drive 880 manifest rows over five vendored sources; all special instructions are case ids hard-coded in `ConformanceCaseRunner`; 12 rows are skipped with reasons and beads; 72 deprecated 3.0 cases, two JSON responses, a PIP table and five repository property files are vendored but unused. The proposed design is one YAML descriptor plus a quoted CSV case table per suite, one `@TestFactory` runner, adapters for OASIS XML, rsa2008 and JSON, one model-level comparator with per-case strictness, and the manifest's skip columns as the single ledger. Coverage gaps: 26 string conversion functions, `string-equal-ignore-case`, `string-concatenate`, `time-in-range`, five non-string regexp functions, the `ipAddress` and `dnsName` bag and set functions, `access-permitted`, the Entities profile, `on-permit-apply-second`, variables, combiner parameters, policy issuers, combined decisions, JSON requests, and the deprecated-identifier cases.

Evidence: file and line citations throughout; counts from the read-only scripts `tv3_inventory.py` and `tv3_experimental.py` in this lane's scratch directory (`<scratch>/`, outside the repository) over `src/test/resources`, `src/test/manifest/conformance-manifest.csv`, `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl`, `policy/combine/impl`, and `types/XacmlTypes.java`. Labels: repository facts SUPPORTED; the OASIS licence position PARTIAL (no licence text in the files; policy pages fetched via Tavily); the 3.0 suite's upstream URL and the standard ids of the `ipAddress` and `dnsName` set functions UNVERIFIED.

Verification run: reads with the native file tool, `rtk rg`, `rtk find`, `rtk python3 -I` on the two scratch scripts, five `mcp__tavily__tavily_search` queries. No repository file was written; no Maven, test, benchmark, git or `br` command ran.

Residual risk: the registered-id lists come from regular expressions over annotations and the algorithm provider; an id registered another way would be missed. The experimental-case lists come from the index pages' list items and may miss cases listed without a link. The standard function ids for `ipAddress` and `dnsName` set functions were not checked against the specification text in this lane.
