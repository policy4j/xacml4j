## 1. Plan

- [x] 1.1 Research swarm TV1 to TV4; done: `research/README.md`
- [x] 1.2 `openspec validate adopt-conformance-test-vectors --strict`; plan reviews by another family: RVT1 to RVT4 REJECT, each revised; RVT5 APPROVE WITH CHANGES, applied; done: `evidence/reviews/plan/rvt1-terra` to `rvt5-terra`
- [ ] 1.3 Maintainer approval; beads epic and issues

## 2. Descriptors, the runner, and lossless comparison with the same outcomes

- [ ] 2.1 Frozen record: every current row's outcome (pass, fail, skip) and comparison state (per-field results, or not compared with the reason, phase, and defect) under the old runners and comparators, plus altered-field control copies; `RunnerEquivalenceTest` reads it, with controls for the `IIA004` and `IIC012` rows of both suites
- [ ] 2.2 Quoted CSV with `kind`, `compare`, structured verb fields, `category`, `source`, `review`; `suite.properties` per suite; `SuiteDescriptorTest` (missing fields, unknown kinds and verbs, disabled rows without reason, adopted rows without review record)
- [ ] 2.3 `ConformanceSuiteTest` (`@TestFactory`), the OASIS XML and RSA 2008 adapters, the structured verbs with fixture guards, `TablePip` with `pip.csv`; `TablePipTest` (mixed-case positive, other-subject negative); port the guard tests of `ConformanceStaticRejectionTest`, `ConformanceRequestSyntaxErrorTest`, `NaNFixtureAdjudicationTest` to verb rows; check: `RunnerEquivalenceTest` against the frozen record
- [ ] 2.4 `ResponseComparison` and the `WireComparator` interface in `xacml-test` main code; the JAXB comparator with modes `legacy-20`, `legacy-30`, `full`, `decision-status`, `decision`; the `legacy-model` adapter over the unchanged `XacmlPolicyTestSupport` comparison, with a control whose model responses differ in a nested status code that the 2.0 marshaller does not write; typed equality, unordered bags, every difference reported (in `legacy-model`: the old verdict and the first difference with the case id); `Xacml20TestUtility` and `Xacml30TestUtility` delegate in their legacy modes; `XacmlPolicyTestSupport.assertResponse` unchanged; `ResponseComparatorTest` (lexical forms, reordered bags, multiplicity, two differences, the legacy-30 versus full nested-code control, the existing negative tests); check: `RunnerEquivalenceTest` again in legacy modes, controls included
- [ ] 2.5 `VectorAdoptionCheckTest`: whole-case duplicates, the shared-file negative case, and provenance completeness (an adopted file without an origin and basis record, a partially covered group)
- [ ] 2.6 Fold in the old runners, the hand-written resolver, and the case-id logic of `ConformanceCaseRunner`; remove `RSA2008PerformanceTestCase`; check: `RunnerEquivalenceTest` once more, reactor and conformance
- [ ] 2.7 Move rows from their legacy modes to `full`, one at a time, recorded; rows of `legacy-model` suites keep that check as a second check (control: an extra nested code and a changed status detail still fail); then compare every row's outcome with the unchanged frozen record plus approved per-row overrides; a changed row stays in its legacy mode until fixed or until its override is reviewed and approved (the review and approval linked as evidence); controls: an unoverridden change fails, the approved exact override passes, an override without approval, with a stale old outcome, with a wrong new outcome, or duplicated fails
- [ ] 2.8 Gate: review by another family; reactor and conformance

## 3. Provenance of the suites we hold

- [ ] 3.1 Records for the 2.0, 3.0, RSA 2008, and example suites (source, license basis, edits, integrity); the legacy OASIS terms recorded as unclear (maintainer: keep as is)
- [ ] 3.2 Remove the project header from the 34 RSA 2008 request and response files (D9, maintainer exception): baseline ledger verified and kept, results characterized; a check that exactly these 34 files changed, each only by the header block; ledger regenerated with a delta of exactly 34 entries; upstream equality claimed only for files verified against an upstream copy (download with permission); `license.excludes` widened to every vendored suite; check: same RSA 2008 results, `VendoredFixtureIntegrityTest`
- [ ] 3.3 Run the 72 `xacml3.0-deprecated` cases (XACML 3.0 §10.2.9: their identifiers are mandatory): migrate the fixture inventory of `ConformanceManifest` and its tests (keep the 487 active checks; add a bijection of the 72 request files and their rows); first the 72 rows are added, enabled, with their review records; then every case is run once and its individual outcome kept (including parse and evaluation failures); then a failing case is fixed, or disabled with its specific reason and bead
- [ ] 3.4 Gate: review; reactor and conformance

## 4. AuthzForce additional cases

- [ ] 4.1 With permission, fetch the additional cases with their license and NOTICE; record each file's origin and basis (inherited OASIS files deferred); whole-case duplicates removed; each case given its kind, reviewed against the XACML text (review record), and categorized
- [ ] 4.2 Enable the cases that hold; beads for engine failures; gate: review, reactor

## 5. JSON vectors

- [ ] 5.1 `xacml-json` and Gson test dependencies of the conformance module; the JSON `WireComparator` there with `JsonResponseComparatorTest` (typed values, reordered bags, multiplicity, the modes, several differences reported); the JSON adapter end to end (published request, PDP, `JsonResponseContextMarshaller`, comparison with the published response), recording the profile version; a bead for each valid form the module cannot read or write, with its cases disabled
- [ ] 5.2 With permission, the AuthzForce JSON validation fixtures as validation rows (verdict against expected validity; valid and invalid controls for requests and responses; AuthzForce extensions recorded as conventions); project-written JSON evaluation vectors; gate: review, reactor

## 6. Gap vectors

- [ ] 6.1 Check the standard ids of the `ipAddress` and `dnsName` set functions against the XACML text
- [ ] 6.2 Project-written vectors for the listed gaps (D7 item 3), each with its review record; gate: review, reactor

## 7. Small licensed examples

- [ ] 7.1 With permission, Curity, Go (mode `decision`), and `ndg_xacml` vectors; Balana `basic/3` and AT&T on-permit-apply-second reviewed for novelty; gate: review, reactor

## 8. Errata audit and deferred OASIS material

- [ ] 8.1 Our skips and adjudications against the AuthzForce errata (D8); changes only for corrections
- [ ] 8.2 Compare and disposition the OASIS 2.0 v0.5 index and change list against the vendored suite; patch #1 and derived OASIS JSON stay deferred until a redistribution basis is documented (D7 item 5); gate: review

## 9. Close out

- [ ] 9.1 `docs/ai/TESTING.md` conformance section
- [ ] 9.2 `openspec validate --strict`; requirement to test to evidence audit in `evidence/README.md`
- [ ] 9.3 Close the beads epic; `br sync --flush-only`
