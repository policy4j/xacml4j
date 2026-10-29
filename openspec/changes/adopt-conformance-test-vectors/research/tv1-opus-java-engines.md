<!-- Lane TV1 of the test-vector research swarm, Claude Opus 5.5, read-only; scope: Java engines; report from the NTM pane, paths made repository-relative. -->

FINAL REPORT (TV1 test-vector research lane, Claude Opus 5.5)

## Summary

- **No OASIS XACML 3.0 conformance suite exists.**
  - The XACML 3.0 core says test cases "can be located from the OASIS XACML TC Web page" (§10.2, https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html, OASIS Standard 22 Jan 2013).
  - The TC page lists only the XACML 1.x and 2.0 suites; fetched 2026-10-27, it has no 3.0 test link (https://www.oasis-open.org/committees/tc_home.php?wg_abbrev=acal).
  - Every Java engine uses the same non-normative 3.0 set, `xacml3.0-ct-v.0.4`.
- **Our 3.0 suite is AT&T's copy, byte for byte.** All 1,707 files of `xacml3.0-ct-v.0.4` have the same git blob hashes as AT&T's set (https://github.com/att/xacml-3.0/tree/master/xacml-pdp/src/test/resources/testsets/conformance/xacml3.0-ct-v.0.4).
- **Our 2.0 suite is the OASIS v0.4 package with small edits.**
  - It matches `xacml2.0-ct-v.0.4.zip` by name for all 1,143 files.
  - Content: 825 files are identical after CRLF→LF, 311 differ only in whitespace, and 7 policies were changed. IIA001 and IIA010–IIA015 had their duplicated OASIS ids `IIA1` renumbered.
  - It lacks the three tests of OASIS patch #1 (IIC233, IIF001, IIF002). No Java engine copy I checked has them either.
- **AuthzForce is the most valuable source to adopt from.**
  - It is Apache-2.0. Its errata README lists about 33 numbered defects and fixes in the AT&T v0.4 set, plus 5 cases it deliberately does not support.
  - It splits the cases into mandatory (455), optional (18), and unsupported (14).
  - It adds 23 "others" cases: policy and variable references, id uniqueness, obligations under combining algorithms, and `MissingAttributeDetail`.
  - It derives JSON Profile vectors from the XML ones with XSLT. Its `xacml-json-model` repository has valid and invalid JSON request and response samples.
- **Licensing for an LGPL v3 project.**
  - AT&T (MIT), Apache-2.0 sources, Sun (BSD-style), and PicketBox (LGPL 2.1-or-later) can be vendored if their notices are kept.
  - The OASIS-origin fixtures carry no license or notice of their own. Their basis is the OASIS IPR Policy and notice (inference, see "Vendoring").
  - Avoid AuthzForce's server, restful-pdp, and authzen repositories: they are GPL-3.0.

## Research tools (2026-10-27)

| Tool | Label | Note |
| --- | --- | --- |
| GitHub MCP (`mcp__github__*`) | UNAVAILABLE | Every call returned `401 Bad credentials`. Fallback was the `gh` CLI. |
| `gh api` (read-only REST: repos, git trees, contents, commits, search) | SUPPORTED | GitHub code search covers only a partial index (it found only xacml4j for the 2.0 ids). |
| Tavily search | SUPPORTED | |
| Tavily extract | PARTIAL | `lists.oasis-open.org` failed; one upstream timeout. |
| DuckDuckGo search and fetch | SUPPORTED | Fetch returned 403 on `lists.oasis-open.org` and on the SourceForge code browser. |
| WebFetch | SUPPORTED | 403 on `lists.oasis-open.org`. |
| `curl` (read-only; zips read in memory, no file written) | SUPPORTED | OASIS documents 14846, 17732, 6076; Sun XACML SVN over HTTPS. |
| Local read-only Python and `git log` | SUPPORTED | Used on our vendored fixtures. |

The primary post for the 3.0 suite's origin was not fetchable (403 from both WebFetch and DuckDuckGo; Tavily failed): https://lists.oasis-open.org/archives/xacml-comment/201404/msg00001.html. The AT&T-origin claim therefore rests on the AuthzForce README (secondary) and on the byte-identity result above.

## Sources of test vectors (one block per source)

**S1. OASIS XACML 2.0 conformance tests (primary source, the baseline)**

- **Location**
  - `xacml2.0-ct-v.0.4.zip`, document 14846, "XACML 2.0 Conformance Tests v0.4 (Revision 1)", submitted by Argyn Kuketayev on 2005-10-11. Page: https://groups.oasis-open.org/higherlogic/ws/public/document?document_id=14846; file: https://groups.oasis-open.org/higherlogic/ws/public/download/14846/xacml2.0-ct-v.0.4.zip
  - Patch #1: `2006-04-16-conformance2.0-patch.zip`, document 17732, 2006-04-18. Page: https://groups.oasis-open.org/higherlogic/ws/public/document?document_id=17732
  - XACML 1.0/1.1 tests (2004-03-25): https://www.oasis-open.org/committees/download.php/6076/ConformanceTests.zip
  - The "Current tests in Subversion" link (https://tools.oasis-open.org/version-control/svn/xacml/) now leads to an Atlassian login.
- **License**
  - Neither zip contains a license file, and no fixture has a notice (checked in memory and in our copies).
  - The document pages show only the site footer "Copyright 2026. All rights reserved." and "Download Agreement: None at this time."
  - The XACML 3.0 spec Notices allow copies and derivative works that "assist in its implementation ... provided that the above copyright notice and this section are included". That text governs the spec. Whether it also covers these TC-repository files is UNVERIFIED.
- **Versions:** XACML 2.0 core, with optional sections III.A, C, F, G. The 1.x zip has the same structure (not opened).
- **Format:** flat triples `IIA001Policy.xml`, `…Request.xml`, `…Response.xml`. Extra files: `Special.txt` per test, `IIE…PolicyId…xml` and `…PolicySetId…xml` for references, and `ConformanceTests.html` as the description. The expected result is a full `<Response>`.
- **Count:** 374 cases in 1,143 files. The patch adds IIC233, IIF001, IIF002 and an updated HTML index.
- **Origin:** OASIS TC. The 2.0 set was "(mostly) automatically converted from the previous version of the suite for XACML 1.0" (page for document 14846).
- **Last change:** 2006-04-18.
- **Overlap:** our `oasis-xacml20-compat-test` is this zip, as described in the Summary.
- **To adopt:** add the three patch tests and the 2006 `ConformanceTests.html`. IIF002 needs policy references.
- **Vendorable:** yes in practice (inference). Keep the OASIS notice in a provenance README.

**S2. AT&T XACML** (https://github.com/att/xacml-3.0)

- **Repository:** created 2020-11-19, last push 2023-09-05. The conformance directory last changed 2021-01-19 (`4298827814`). The original `att/XACML` repository returns 404 (`gh api`, 2026-10-27).
- **License:** MIT, "Copyright (c) 2014 AT&T Intellectual Property" (https://github.com/att/xacml-3.0/blob/master/LICENSE). The fixtures have no headers.
- **Versions:** XACML 3.0 core, the Multiple Decision Profile (III.E), XPath (III.F/III.G), and Hierarchical Resource (III.C); JSON request tests live in Java code (`xacml/src/test/java/com/att/research/xacml/std/json/*Test.java`).
- **Format:** flat triples, plus `xacml3.0-deprecated/` (216 files) and `PIP.txt`.
  - `IID029`/`IID030`/`IIE00x` `Repository.properties` files list root and referenced policies.
  - `conformance.txt` is AT&T's own result summary: "Tests run = 543", all seven comparison aspects match, "Unknown functions = 6".
- **Count:** 543 tests in 1,707 files. Extra test sets under `testsets/`:
  - `algorithms/onPermitApplySecond`: 8 policies, with the expected result in the file name;
  - `algorithms/combinedPermitOverrides`: 6 policies and 6 JSON requests named like `Request.01.Permit.json`;
  - `custom/category`, `custom/datatype-function` (AT&T extensions);
  - `entity` (5 policies);
  - `spec`: 19 example fragments from spec sections 4.1, 4.2, 7.6, and A.3.12; only 4.1.3 has a response;
  - `timezoneattribute`, `timerecurring`, `annotation` (AT&T features).
- **Origin:** AT&T's April 2014 contribution of the 2.0 set converted to 3.0, with new 3.x-numbered tests (per the AuthzForce README; primary post not fetched).
- **Overlap:** 100%. Our `xacml3.0-ct-v.0.4` is byte-identical.
- **To adopt:** the v0.4 set needs nothing. The extra sets need conversion from AT&T `xacml.properties` and from expectations encoded in file names. The custom, annotation, and time-recurring sets are engine extensions; skip them.
- **Vendorable:** yes; MIT is compatible with LGPL v3. Caveat (inference): the MIT grant covers AT&T's own work, not the OASIS-derived content.

**S3. Apache OpenAZ** (https://github.com/apache/incubator-retired-openaz)

- **Repository:** archived; last push 2017-04-28. The conformance directory last changed 2016-03-17 (`a6e96a6ff9`).
- **License:** Apache-2.0 (`LICENSE`). Every fixture starts with an ASF license comment. The NOTICE says: "Portions of OpenAZ were originally developed by AT&T ... licensed ... under the Software Grant and Corporate Contribution License Agreement".
- **Versions:** same as S2, plus PIP test sets: `testsets/pip/configurable-{csv,csv-hyper,ldap,sql}` with data.
- **Format and count:** `openaz-xacml-test/src/test/resources/testsets/conformance/xacml3.0-ct-v.0.4/` holds 1,707 files. They are AT&T's set with the ASF header added; only 7 are byte-identical to ours.
- **Overlap:** full, except the headers.
- **To adopt:** nothing; take S2 or keep our copy. If OpenAZ files are copied, the ASF headers must stay.
- **Vendorable:** yes; Apache-2.0 code "can therefore be included in GPLv3 projects" (https://www.apache.org/licenses/GPL-compatibility.html). Extending that to LGPLv3 is my inference: LGPLv3 is GPLv3 plus extra permissions.

**S4. AuthzForce CE core** (https://github.com/authzforce/core)

- **Repository:** branch `develop`; latest release `release-21.2.0`, 2026-08-25. The conformance directory last changed 2023-11-23 (`99a1867f6e`).
- **License:** Apache-2.0 (https://github.com/authzforce/core/blob/develop/LICENSE). The sampled fixture has no header.
- **Versions:** XACML 3.0 core; optional XPath, Multiple Decision (IIIE302 only), and Hierarchical Resource (immediate scope); JSON Profile through conversion.
- **Format:** one directory per case containing `Policy.xml`, `Request.xml`, `Response.xml`.
  - Optional per case: `AttributeProvider.xml`, and an `IIExxxRepository/` directory for referenced policies.
  - Expected static errors are encoded by renaming files to `Request.xml.ignore`, `Response.xml.ignore`, or `Policy.xml.ignore`.
  - Fixed cases get suffixes such as `_FIXED`, `_FIXED_NO_XPATH`, `_FIXED_WITH_XPATH`, `_FIXED_NO_CONTENT_NO_XPATH`.
- **Count:** `pdp-testutils/src/test/resources/conformance/xacml-3.0-from-2.0-ct/`:
  - mandatory: 455 cases (IIA 18, IIB 55, IIC 261, IID 57, IIE 3, IIF 3, IIIA 58); 6 of them use `Request.xml.ignore`;
  - optional: 18 (14 `xml`, 4 `xml+json`);
  - unsupported: 14 ids;
  - `others/` 23, `custom/` 14 (AuthzForce extensions), `NonRegression/` 6, JSON-only 2 (`resources.json/`).
- **Origin:** derived from S2 with documented changes. README: https://github.com/authzforce/core/blob/develop/pdp-testutils/src/test/resources/conformance/xacml-3.0-from-2.0-ct/README.md
  - Fixes (fetched README) include: schema-invalid items (IIA006, IIB010/011 `SubjectCategory`; IIIA `FulfillOn`; IIIC `ResourceId` on `Result`), invalid values in IIA023 (timezone −14:30, rfc822Name), the wrong XPath-version URI (IIF300/301/310, IIIF001–007, IIIG001), and the IIIG renumbering.
  - It moves XPath and `RequestDefaults` cases to optional, and the obligation tests (IIIA) to mandatory.
  - It records missing cases: IID321–329, IID334–339, IIIA029, III.B.
- **Engine conventions in that README (not the standard):**
  - IIA016/018/020: an `Issuer="pep"` is added to `current-time` so AuthzForce does not override it.
  - IIIE302: attribute order changed in the response.
  - IIIG301/302: AuthzForce's own definition of an "applicable" policy for `ReturnPolicyIdList`. It cites a spec ambiguity raised on xacml-comment in 2016-05 (not fetched).
  - Unsupported: IIA010/012/024 (same AttributeId with different DataTypes is rejected), IID029/030 (multiple root policies), IIIC002/003, IIIE301/303, IIIG002–006.
- **Overlap:** the same base suite as ours, restructured, with fixes.
- **To adopt:**
  - Map each errata line to our `ConformanceManifest` skip, adjudication, and justification list. Normative OASIS text wins over AuthzForce conventions.
  - Port the 23 `others/` cases.
  - Convert the case-directory layout, or let our runner read both layouts.
  - `AttributeProvider.xml` is AuthzForce-specific; map it to our resolver configuration.
- **Vendorable:** yes (Apache-2.0); add attribution in a provenance README.

**S5. AuthzForce `xacml-json-model` (JSON Profile vectors)** (https://github.com/authzforce/xacml-json-model)

- **Repository:** Apache-2.0 `LICENSE`, plus `LICENSE_HEADER.txt`; last push 2026-03-06. Test resources last changed 2020-01-05 (`d8508dc7b5`).
- **Versions:** XACML JSON Profile requests and responses. The profile version is UNVERIFIED (1.0 or 1.1). The JSON policy format is AuthzForce's own, per the commit message "JSON schema for XACML Policy in (Authzforce custom) JSON format", so it is not standard.
- **Format and count:** `src/test/resources/`:
  - `xacml+json.samples/Requests` (12 valid, 15 invalid);
  - `xacml+json.samples/Responses` (12 valid, 11 invalid);
  - `xacml+json.samples/Policies/valid` (5, AuthzForce format);
  - `json.samples/{valid,invalid}` (13 each);
  - `xacml+xml.samples/xacml-3.0-ct/{mandatory,optional}`: about 95 cases converted to JSON in the tests.
  - JSON Schemas: `src/main/resources/org/ow2/authzforce/xacml/json/model/{Request,Response,Policy,common-std,common-ng}.schema.json`.
- **Origin:** the project's own work.
- **Overlap:** none. We have no JSON vectors.
- **To adopt:** run the request and response samples through `xacml-json` for parse/reject parity. First check the profile version and AuthzForce extensions; `common-ng` looks non-standard (inference).
- **Vendorable:** yes (Apache-2.0).
- **JSON vectors in core (S4):** core generates JSON vectors at test time from the XML suite. `pdp-testutils/src/test/resources.json/` holds `xacml-request-xml-to-json.xsl`, `xacml-response-xml-to-json.xsl`, `xacml-common-xml-to-json.xsl`, plus an Ant `build.xml` and `ivy.xml`. The runners are `XacmlJsonConformanceV3MandatoryTest` and `XacmlJsonConformanceMultipleDecisionProfileTest`.

**S6. WSO2 Balana** (https://github.com/wso2/balana)

- **Repository:** Apache-2.0 `LICENSE.txt`. The `NOTICE.txt` says it is based on Sun XACML SVN `branches/seth` r137 under Sun's license. Last push 2025-10-08; the conformance directory last changed 2021-12-07 (`c9da0169e2`).
- **Versions:** XACML 2.0 and 3.0.
- **Format and count:** `modules/balana-core/src/test/resources/`:
  - `conformance/2/{policies,requests,responses}` (382/374/374): the OASIS 2.0 v0.4 set with its original `IIA1` ids and CRLF line endings;
  - `conformance/3/`: IIIA001–IIIA028 converted to 3.0, named `*.xacml3.xml`, 28 each;
  - `basic/3/`: WSO2's own tests, 19 policies and 56 request/response pairs (`TestPolicy_00NN`, `request_00NN_0M`);
  - `advance/3/`: 4 policies, 4 requests, 3 responses.
- **Overlap:** the 2.0 set is the same as ours (no byte-identical files: ids and whitespace differ). `basic/3` and `advance/3` are new.
- **To adopt:** review the expected responses in `basic/3` against the spec. They were written by WSO2 and may encode Balana behavior. Convert the directory layout to triples.
- **Vendorable:** yes (Apache-2.0). The OASIS-derived files keep the OASIS caveat.

**S7. Sun's XACML implementation (no vectors; useful runner design)**

- **Location:** https://sunxacml.sourceforge.net/ (guide dated 2004). SVN: https://svn.code.sf.net/p/sunxacml/svn/trunk/tests/ (fetched).
- **License:** BSD-style with a non-endorsement clause and a "not designed ... for use in ... nuclear facility" acknowledgement (https://sunxacml.sourceforge.net/license.txt). The test files carry the same header.
- **Vectors:** none. The README says "You also need the standard XACML conformance tests, which can be found off the XACML TC's home page".
- **Manifest:** `tests.xml` and `tests2.xml` each have 374 `<test name="NNN">` entries.
  - Flags: `errorExpected="true"` (5 uses) and `experimental="true"` (3 uses).
  - Groups: `II` with A–E, and `III experimental="true"` with A, C, F, G.
  - IIE references are declared inline as `<policyReference>` and `<policySetReference>`.
- **Last change:** UNVERIFIED (SVN log not fetched).
- **To adopt:** the manifest design only.

**S8. Heras-AF** (https://github.com/tailorfastGmbH/herasaf-xacml-core)

- **Repository:** a third-party organization's mirror; last push 2023-07-13. The conformance directory last changed 2021-12-01 (`66d1746fd4`).
- **License:** no LICENSE file. The README says "available under the Apache 2.0 License", and `pom.xml` `<licenses>` declares Apache-2.0. Label: PARTIAL (no license text in the repository).
- **Versions:** XACML 2.0.
- **Format and count:** `src/test/resources/conformanceTests/{attributeReferences,targetMatching,combiningAlgorithms,functionEvaluation}`: 977 files of the OASIS 2.0 mandatory IIA–IID sections, with `IIA1` ids. Also `ownConformanceTests/` (9 files) and `missingAttributes/` (7 files).
- **Overlap:** a subset of ours.
- **To adopt:** only the small own sets; low value.
- **Vendorable:** yes in principle, but the license text must be obtained from an upstream release first.

**S9. JBoss PicketBox XACML** (https://github.com/picketbox/security-xacml)

- **Repository:** archived; last push 2018-05-02. The conformance directory last changed 2009-03-24 (`aeb133515e`, "SECURITY-337").
- **License:** no LICENSE file; GitHub detects none. Java sources carry "GNU Lesser General Public License ... version 2.1 ... or (at your option) any later version", copyright 2008 Red Hat. The fixtures have no headers.
- **Format and count:**
  - `jboss-sunxacml/src/test/resources/conformance/mandatory/`: 998 files. This is the XACML 1.x suite (namespace `urn:oasis:names:tc:xacml:1.0:policy`, ids `IIA1`), sections IIA–IIE.
  - `jboss-xacml/src/test/resources/test/{policies,requests}/interop/rsaconf08`: RSA 2008 interop policies and 21 requests, with no responses.
  - HIMSS 2009 interop: `himss-policy*.xml`, `himss-request-01.xml`, emergency scenarios.
  - `security/xacml/testN`: JBoss's own tests, 3 files each.
- **Overlap:** RSA 2008 is a variant of ours. Ours has responses and the extra request `04-04`, and our policy is named `03-N-RPS-med-rec-vrole` where theirs is `…virt-med-rec-role`. The 1.x suite is not ours.
- **To adopt:** HIMSS 2009 needs expected results adjudicated against the spec. The 1.x suite needs 1.x support; skip it.
- **Vendorable:** yes, through the "or later" clause (LGPL-2.1+ → LGPL-3.0).

**S10. enterprise-java-xacml** (https://github.com/MitchellChu/enterprise-java-xacml)

- **Repository:** a 2015 export of Google Code. No LICENSE file; the Google Code archive metadata says `"license": "asf20"` (https://storage.googleapis.com/google-code-archive/v2/code.google.com/enterprise-java-xacml/project.json). Label: PARTIAL.
- **Format and count:** `an/tests/data/` holds 1,130 files of the OASIS 2.0 v0.4 set (duplicated in `an.jaxb/`). Runner configuration: `tests/conformance20/config/*.properties`, one file per feature group, plus Ant.
- **Overlap:** full; nothing new.
- **To adopt:** nothing.

**S11. Other Java sources (low value)**

- **OpenConext-pdp** (Apache-2.0, OpenAZ-based, active 2026-10-26): `src/test/resources/test-harness/`, about 58 `reg_*` regression cases. They are application-level OpenConext policies, not general XACML vectors.
- **ONAP policy-xacml-pdp** (`NOASSERTION`): application decisions only.
- **`canhnt/sne-xacml`** (LGPL-3.0): 12 test resources, not a suite.
- **BiTKOO** (.NET, not Java): announced its own 3.0 translation of the 2.0 tests on 2011-03-10 at a hosted URL, with no files (https://groups.oasis-open.org/communities/community-home/digestviewer/viewthread?MessageKey=6B6BFFCC-D6BA-43DE-8FB9-4781F69912F7).

## How each project runs its vectors (ideas to reuse)

- **AT&T and OpenAZ** (`xacml-pdp/src/test/java/com/att/research/xacmlatt/pdp/test/conformance/`)
  - Discovery: `ConformanceTestSet` walks the directory and groups files by the prefix before `Policy`, `Request`, `Response`, or `Repository`.
  - References: `ConformanceRepository` reads root and referenced policies from `*Repository.properties`.
  - Missing attributes: `ConformancePIPEngine` serves attributes the request lacks from `PIP.txt` rows of the form `category|attributeId|dataType|value[|issuer]` (`xacml.properties` → `ConformancePIPEngine.file`).
  - Comparison: `ResponseMatchResult`/`ResultMatchResult` compare each result on seven aspects: decision, status code, attributes, PolicyIdentifiers, PolicySetIdentifiers, associated advice, obligations. They emit the per-aspect counts seen in `conformance.txt`.
- **AuthzForce** (`ConformanceV3FromV2Test`; `TestUtils.assertNormalizedEquals`)
  - Normalizes both responses, then compares the JAXB models for equality.
  - Status message and status detail can be ignored; `MissingAttributeDetail` is compared field by field; attribute categories are normalized.
  - Missing attributes come from a per-case `AttributeProvider.xml`.
- **Sun, Balana, PicketBox**
  - A Java `TestAttributeFinderModule` and `TestResourceFinderModule` (hierarchical resources); Sun also has `TestPolicyFinderModule` for references.
  - Balana's `TestUtil.isMatching` compares decision, status, and encoded advice and obligations, ignoring order.
  - PicketBox compares only the decision of the first result. Too weak to copy.
- **Heras-AF:** a TestNG DataProvider of case ids; marshals both responses and calls XMLUnit `assertXMLEqual` (ignore whitespace and attribute order).
- **Ideas worth combining (inference):**
  - One manifest per suite carrying Sun-style flags (`errorExpected`, `optional`/`experimental`, `unsupported` with a reason, a spec section for each adjudication).
  - AT&T's seven-aspect result comparison, with AuthzForce-style normalization.
  - One attribute-provider file per suite or per case, in place of Java fixtures.
  - XSLT-derived JSON vectors from the same XML cases.

## Recommendations (inference, by value)

1. **Provenance and license hygiene.** Add a README to each vendored directory with the source URL, version or date, the OASIS notice, and our edits (the 2.0 whitespace changes and the 7 id renumberings). Nothing records this today: `git log` shows the suites added in `451e0b44` (2014-06-30) and `c92be7b7` (2023-04-19), with no provenance note.
2. **Add OASIS 2.0 patch #1** (IIC233, IIF001, IIF002) directly from document 17732.
3. **Use the AuthzForce errata list to audit our 3.0 skips and adjudications** (task 3.4; NaN adjudication). Treat each item as either a normative fix or an engine convention: IIIG301/302, the IIA016/018/020 issuer workaround, IIIE302 attribute order, and the mixed-datatype rejection are conventions.
4. **Port AuthzForce `others/`** (23 cases) after a review against the spec.
5. **Build JSON Profile vectors** from the XML suite (AuthzForce XSLTs or our own converter), and add the `xacml-json-model` valid and invalid samples to `xacml-json`.
6. **Review Balana `basic/3`** (56 cases) and AT&T `onPermitApplySecond` (8 policies). On-permit-apply-second is a standard algorithm, but the requests and expected responses still need to be written.
7. **Skip these:** OpenAZ, Heras-AF, enterprise-java-xacml, and the PicketBox 2.0/1.x copies (subsets of S1 or S2), OpenConext, and every GPL-3.0 AuthzForce repository.

## Vendoring under LGPL v3 (reasoning)

- **Apache-2.0** (AuthzForce, Balana, OpenAZ, Heras-AF, enterprise-java-xacml): may go into (L)GPLv3 works (Apache's own statement, above). Keep the per-file ASF headers on OpenAZ files and add attribution.
- **MIT** (AT&T) and **BSD-style** (Sun): keep the notice.
- **LGPL-2.1-or-later** (PicketBox): fine under "or later".
- **Flags:**
  - No LICENSE file: the Heras-AF mirror, the enterprise-java-xacml export, and PicketBox.
  - No license statement on the OASIS test packages: rely on the OASIS notice and IPR Policy (UNVERIFIED for test files).
  - GPL-3.0: AuthzForce `server`, `restful-pdp`, `authzen`, `fiware` (`gh` search metadata). Do not copy test files from them.
  - `org.xacml4j:xacml-conformance-tests` is published on mvnrepository with "License LGPL 3.0" (Tavily snippet, not fetched). If resources ship in that artifact, the notices must ship with them (inference).

## Verification run

- `gh api` on `repos/<owner>/<repo>`, `git/trees/<branch>?recursive=1`, `contents/<path>`, and `commits?path=`, plus repository and code search.
- In-memory git blob-hash comparison of AT&T, OpenAZ, Balana, Heras-AF, PicketBox, and enterprise-java-xacml trees against our vendored files.
- `curl` the OASIS zips 14846 and 17732 (and a size probe of 6076) into Python `zipfile` in memory.
- Sun SVN listings over HTTPS.
- WebFetch, DuckDuckGo, and Tavily on the OASIS pages and spec Notices, the Sun guide and license, the BiTKOO thread, and the Apache compatibility page.
- No repository file was written; no Maven or git write.

## Residual risk

- The April 2014 AT&T origin post was not fetched.
- The OASIS test-package license is not stated in any source I fetched.
- Fixture differences in Balana, Heras-AF, PicketBox, and enterprise-java-xacml were sampled with one diff each, not classified for the whole set.
- The origin of our RSA 2008 responses was not traced. The OASIS "XACML 2.0 RSA 2008 Interop Scenarios" document was found but not fetched.
- Counts come from default-branch trees on 2026-10-27.
- Non-Java engines are out of scope.
