## Context

`xacml-conformance-tests` runs five vendored sources (OASIS XACML 2.0 v0.4, the AT&T XACML 3.0 v0.4/0.5
set, RSA 2008, two examples) through four runners (`Xacml20ConformanceTest`, `Xacml30ConformanceTest`,
`RSA2008InteropTest`, `XacmlExampleTest`), `ConformanceCaseRunner`, and three comparators (the JAXB-level
`Xacml20TestUtility` and `Xacml30TestUtility` of `xacml-test`, and the model-level
`XacmlPolicyTestSupport.verifyXacml20Response`). The manifest `src/test/manifest/conformance-manifest.csv`
(880 rows) already holds the skip ledger. Special cases are case ids in code (`IIE003`, `IIA005`,
`IID029`, `IID030`, the NaN adjudication of `IIC350` and `IIC358`). Module dependencies:
`xacml-conformance-tests` depends on `xacml-test` and `xacml-xml`. Research: `research/README.md`. Plan
review RVT1 (Codex GPT-6.1 Terra, REJECT) shaped this revision; dispositions at the end.

## Goals / Non-Goals

Goals: one descriptor format with case kinds, one runner, one comparison entry point over lossless
comparators, adapters only where needed; provenance and license recorded per suite; adopt vectors with a
documented redistribution basis that add coverage; keep every current outcome and every compared field.

Non-goals: changing engine behavior to match another engine's conventions; editing vendored fixtures
beyond the one exception the maintainer approved (D9); a certification claim (the suites are
non-normative).

## Decisions

### D1. Descriptors without a new dependency

The manifest stays a CSV, quoted (RFC 4180), with new columns: `kind` (`evaluation`,
`policy-rejection`, `request-validation`, `response-validation`), `compare` (`legacy-20`, `legacy-30`,
`legacy-model`, `full`, `decision-status`, `decision`), `verb` and its structured fields (D4), `category` (correction, harness, convention,
unsupported, or empty), `source`, `review` (the path of the case's review record, D8). Each suite gets
`src/test/suites/<suite>/suite.properties` (title, `xacml`, `media`, `root`, `source.url`,
`source.revision`, `license`, `edits`, `integrity`) and, where needed, `pip.csv` (D5). Alternative: YAML
descriptors (TV3) need a parser in the test classpath; properties and CSV need none.

### D2. One `@TestFactory` runner

`ConformanceSuiteTest` reads all descriptors and returns a `DynamicContainer` per suite and per group
and a `DynamicTest` per row; a disabled row runs as an aborted test with its reason and bead. A PDP is
built per distinct policy list and reused within a suite. Rejected (TV3): one `@ParameterizedTest` over
all rows.

### D3. Wire comparators behind one entry point; the model comparator kept

`ResponseComparison` in `xacml-test` main code is the entry point of the runner; it dispatches by mode
and format: mode `legacy-model` goes to the model adapter (below), every other mode to a `WireComparator`
selected by format (RVT1 P1, RVT4 P2). The XML comparator is today's JAXB-level comparison of
`Xacml20TestUtility` and `Xacml30TestUtility`, which keeps repeated obligations, advice, and categories and
the 2.0 resource id and status detail. Outside `legacy-model`, `ResponseContext` is not used for
comparison, because `Result` merges obligations by id and categories by category id; `legacy-model` is the
one explicit exception, kept because the XACML 2.0 response marshaller does not write nested status codes
and status details (RVT3 P1, RVT4 P2). The JSON comparator is supplied
by the conformance module (test code, with `xacml-json` and Gson as its test dependencies), so
`xacml-test` gains no dependency (RVT2 P2). Modes:

- legacy modes that reproduce exactly what each old comparator checks: `legacy-20` (the 2.0 JAXB
  fields), `legacy-30` (the 3.0 JAXB fields: top-level status code only), and `legacy-model` for the RSA
  2008 and example suites, an adapter that compares the expected and actual model responses with the
  unchanged `XacmlPolicyTestSupport` comparison (RVT3 P1: the XACML 2.0 response marshaller writes only the
  top-level status code and message, so a wire comparison would lose nested codes and details); the
  equivalence gate runs every row in its suite's legacy mode (RVT2 P1); when an RSA 2008 or example
  row moves to `full`, its `legacy-model` check stays as a second check, until a separately approved
  lossless 2.0 marshalling exists (RVT4 P1); the adapter keeps the old verdict and adds the case id to the
  message (first difference only, RVT4 P2);
- `full` (every stated field, including nested status codes, status messages, and details), enabled per
  row after the last equivalence check;
- `decision-status` and `decision`.

Equality: typed values by value (integer `1` equals `01`), bags without order but with multiplicity, as
`Xacml30TestUtilityTest` pins today; the comparator collects every difference before it fails and names
the case and each field (RVT2 P2), except in `legacy-model`, which keeps the old verdict and reports
the first difference with the case id. The public methods keep their signatures and semantics:
`Xacml20TestUtility` and `Xacml30TestUtility` delegate in their legacy modes;
`XacmlPolicyTestSupport.assertResponse(ResponseContext, ResponseContext)` keeps its model-level
comparison, which cannot see merged fields, and its callers keep their tests (RVT2 P1).

### D4. Adapters and structured verbs

Adapters: OASIS XML (2.0 and 3.0, by suite version), RSA 2008 (policy-list layout), JSON (D10). Verbs
replace case ids and carry structured fields (RVT1 P2): `static-reject` (file, expected defect),
`request-defect` (phase, expected defect and status), `combine-roots` (algorithm), `adjudicated`
(replacement expectation, XACML section, fixture guard). Every verb has a fixture guard: the content hash
of the fixture it applies to. The existing guards and negative controls of
`ConformanceStaticRejectionTest`, `ConformanceRequestSyntaxErrorTest`, and
`NaNFixtureAdjudicationTest` are ported to verb rows.

### D5. Table-driven resolver with conditions

`TablePip` reads `pip.csv` rows from the suite's descriptor directory (never from the vendored tree):
the attribute supplied (category, id, data type, value) and its condition (request attribute, value,
match rule, for example `equals-ignore-case`). It replaces `Xacml20ConformanceAttributeResolver`; the
3.0 suite's vendored `PIP.txt` is the source of the 3.0 row, and the subject condition is added in the
descriptor (RVT1 P2).

### D6. Provenance and the adoption rule

Each suite's `suite.properties` records its source, license basis, and edits; `NOTICE` and license files
of adopted sources sit in the vendored directory of that source (new directories only). Existing suites:
the 3.0 set names AT&T's repository and the 2014 TC submission; the 2.0 set names the OASIS package and
lists its whitespace and seven id edits; the legacy OASIS terms are recorded as unclear and the files are
kept as they are (maintainer, Open Question 1). A candidate is a duplicate only when its whole case equals
an existing case: ordered policies, request, expected outcome, and harness configuration (RVT1 P2).

### D7. Sources, in order, each with a documented basis

1. AuthzForce additional cases, split by case kind (evaluation, policy rejection), each reviewed
   against the XACML text; conventions recorded and disabled. The origin of each file is recorded:
   files the project authored (Apache-2.0) are adopted; files inherited from the OASIS packages are
   deferred with item 5 (RVT2 P2).
2. JSON: AuthzForce's JSON validation fixtures (Apache-2.0) as `request-validation` and
   `response-validation` rows, and project-written JSON evaluation vectors, read as published (D10).
3. Project-written gap vectors (LGPL, ours): the 26 XACML 3.0 conversion functions,
   `string-equal-ignore-case`, `string-concatenate`, `time-in-range`, non-string `*-regexp-match`,
   `on-permit-apply-second`, variable definitions and references, combiner parameters, `PolicyIssuer`,
   `CombinedDecision`.
4. Small licensed examples after review: Curity JSON (Apache-2.0; the terms of any generated content
   checked first, RVT2 P2), Go decision assertions (MIT; mode `decision`), `ndg_xacml` tests (BSD);
   Balana `basic/3` and the AT&T on-permit-apply-second policies reviewed for novelty first.
5. Deferred until a redistribution basis is documented (RVT1 P1): OASIS XACML 2.0 patch #1 (IIC233,
   IIF001, IIF002), any later OASIS 2.0 files (the 2005 v0.5 index and change list are compared and
   dispositioned first, RVT1 P3), and JSON derived from OASIS fixtures.

Each external download is made only with the maintainer's explicit permission (source, file, size);
plan approval is not download permission.

### D8. Review records and the errata audit

Every adopted case has a review record (XACML section, reviewer, date, reasoning) under
`src/test/suites/<suite>/reviews/`; `SuiteDescriptorTest` rejects an enabled adopted case without one.
Our skips and adjudications are compared with the AuthzForce errata list item by item and classified;
our row changes only for a correction.

### D9. Removing the project header from the RSA 2008 fixtures (maintainer exception)

The maintainer approved (Open Question 2, 2026-10-27) removing the LGPL header that this project's
license plugin added to the 34 request and response files of `rsa2008-interop`. This is an explicit,
recorded exception to the read-only rule of AGENTS.md for this edit only: the header is a project edit of
the vendored files, and the edit removes it (RVT3 P3: equality with upstream content is claimed only for
files verified against an upstream copy). Plan review RVT1 (P2) objected
under the read-only rule; the maintainer's decision stands and the objection is recorded here. Steps
(RVT2 P2): the existing integrity ledger is verified and kept as the baseline, and the RSA 2008 results
are characterized; the 34 files lose only the comment block, and a check proves that exactly these 34
files changed and that each differs from its baseline only by that block; the ledger is regenerated with
`GenerateVendoredFixtureManifest --write` and its delta must contain exactly the 34 entries; equality
with upstream bytes is claimed only for files verified against an upstream copy fetched with permission;
`license.excludes` of `xacml-conformance-tests/pom.xml` grows to every vendored suite directory so a
later license update cannot add the header again.

### D10. JSON as published

The JSON adapter reads the JSON Profile documents as published (the profile version recorded per
suite): a `Request` envelope for requests and a top-level `Response` array for responses (for example
the vendored `IIA022Response.json`). Today's `RequestContextAdapter` and `ResponseContextAdapter` of
`xacml-json` expect the inner `Category` and `Result` objects, and `JsonResponseContextMarshaller` writes a
`Result` or a `Response` object (RVT1 P1, RVT2 P2); whether they accept and produce the profile forms is
characterized first, end to end: a published request is read, the PDP decides, the public marshaller
writes the response, and that document is compared with the published expectation. A valid form the
module cannot read or write is an `xacml-json` defect with its own bead (and its own change if it alters
public behavior); its cases are disabled until fixed; the harness never rewrites documents. Validation
rows pass when the module's verdict matches the expected validity (RVT2 P1); expectations that are
AuthzForce extensions of the profile are recorded as conventions.

## Test Strategy

Characterization first: a frozen record of every current row's outcome and per-field comparison results
is made by the old runners and comparators before any change (`RunnerEquivalenceTest`), with control
copies of responses whose fields are altered; it is checked after the runner change, again after the
comparator change, and once more before the old runners are folded in. Unit tests: `SuiteDescriptorTest`,
`ResponseComparatorTest`, `TablePipTest`, `VectorAdoptionCheckTest`, and the ported guard tests. Each
adopted source: review records, a red or characterization run, then green, then the reactor.

## Benchmark Plan

Not applicable: test-only modules; engine fixes found by new vectors get their own beads and, when they
touch the decide path, their own change.

## Code Intelligence Plan

ripwire and `rtk rg` for the callers of the `xacml-test` utilities before they delegate; Serena where it
is available; the compiler and the equivalence test as the final check.

## Execution Plan

Single conductor; the source reviews of D7 can use NTM lanes (one per source), read-only, reports stored
under `research/` or `evidence/reviews/`.

## Risks / Trade-offs

- A comparator change could accept different responses: the frozen record, the altered-field controls,
  and the repeated equivalence checks guard it.
- `xacml-json` may not read published JSON Profile documents: recorded as a defect, not hidden.
- New vectors will find engine bugs: each becomes a bead; the case is disabled with the bead until fixed.

## Migration and Rollback

The old runners and comparators stay until the last equivalence check passes; each task group is its own
commit; rollback is a revert of the group's commit.

## Plan review RVT1 dispositions

| Finding | Disposition |
| --- | --- |
| P1 comparator module | D3: the entry point and comparators live in `xacml-test` main code |
| P1 model loses fields | D3: lossless JAXB and JSON-document comparison; `ResponseContext` not used for comparison |
| P1 equivalence gate too early | Test Strategy and tasks 2.1, 2.4, 2.6: frozen record with per-field results and controls, checked three times |
| P1 JSON envelopes | D10 and the JSON requirement: as published, gaps are `xacml-json` beads |
| P1 patch #1 basis | D7 item 5: deferred with derived OASIS JSON until a basis is documented |
| P2 duplicate rule | D6 and the adoption requirement: whole-case equality, with a shared-file negative test |
| P2 case kinds | D1 and the descriptor requirement: `kind` with required fields; mode `decision` |
| P2 verbs | D4: structured fields and fixture guards; guard tests ported |
| P2 PIP condition | D5: condition columns in non-vendored `pip.csv` |
| P2 test creation and audit | tasks 2.2 to 2.6 create every named test; D8 review records with a structural test |
| P2 RSA headers | D9: kept as the maintainer decided, recorded as an explicit exception with the objection |
| P3 later 2.0 changes | D7 item 5: compared and dispositioned before any 2.0 increment |

## Plan review RVT2 dispositions

| Finding | Disposition |
| --- | --- |
| P1 model comparator vs wire contract | D3: `assertResponse` keeps its model semantics; the runner uses wire comparators |
| P1 legacy field sets | D3: legacy modes per old comparator for the equivalence gate; `full` per row afterwards; nested-code control |
| P1 expected-invalid JSON | D10 and the JSON requirement: verdict against expected validity, with valid and invalid controls |
| P1 proposal out of date | proposal rewritten to match the design |
| P2 JSON end to end | D10 and task 5.1: published request, PDP, public marshaller, wire comparison |
| P2 JSON comparator dependency | D3: supplied by the conformance module; `xacml-test` gains no dependency; task 5.1 |
| P2 72-case inventory | task 3.3: inventory and its tests migrated, a bijection of files and rows |
| P2 RSA ledger drift | D9: baseline ledger, exactly 34 changed files, delta of exactly 34 entries |
| P2 comparison semantics | D3 and the comparison requirement: typed equality, unordered bags, every difference named |
| P2 per-file basis | the adoption requirement and D7: origin and basis per file or uniform group |
| P3 evidence index | `evidence/README.md` maps the revised requirements and tasks now |

## Plan review RVT3 dispositions

| Finding | Disposition |
| --- | --- |
| P1 `legacy-model` path | D1 and D3: a `legacy-model` adapter compares model responses with the unchanged utility; task 2.4 implements it with a marshaller-loss control |
| P1 the 72 cases may stay unattempted | task 3.3: every case is run once and its outcome kept before any fix or disable |
| P2 passing rejection without comparison | the equivalence requirement: outcome and comparison state recorded apart; controls for `IIA004` and `IIC012` |
| P2 no gate after the move to `full` | the equivalence requirement and task 2.8: final per-row comparison with the frozen record |
| P2 JSON comparator controls | task 5.1: `JsonResponseComparatorTest` |
| P2 per-file provenance coverage | the adoption requirement and task 2.5: completeness check with missing and partial group controls |
| P3 upstream-equality claim | D9: the header is removed; equality only where verified |

## Plan review RVT4 dispositions

| Finding | Disposition |
| --- | --- |
| P1 moving model rows to `full` loses status checks | D3 and the equivalence requirement: `legacy-model` stays as a second check after the move; control with an extra nested code and a changed status detail |
| P2 wire-only wording vs the model mode | D3, the lossless requirement, and the proposal state the `legacy-model` exception and its dispatch |
| P2 baseline changes vs final equality | the equivalence requirement: the frozen record stays unchanged; per-row approved overrides; task 2.7 |
| P2 72 cases ordered before their rows | task 3.3: rows and review records first, then one attempt each, then fix or disable; evidence index requires all 72 outcomes |
| P2 review gate before the mode moves | tasks: the moves are 2.7, the group gate 2.8 |
| P2 legacy-model diagnostics | the lossless requirement: all differences except in `legacy-model` (old verdict, case id, first difference); control with two differences |

## Plan review RVT5 dispositions

Verdict: APPROVE WITH CHANGES.

| Finding | Disposition |
| --- | --- |
| P2 override path without negative controls | the equivalence requirement (scenario "Overrides of the frozen record") and task 2.7 |
| P3 model-exception wording and evidence map | the lossless requirement attaches the field list to the wire clause and states the lossy `legacy-model`; D3 and task 2.4 scope the every-difference rule; the evidence index maps 2.7 and 5.2 |

## Open Questions

Answered by the maintainer on 2026-10-27:

1. The OASIS terms for the legacy test XML: "keep as is" (D6).
2. The 34 RSA 2008 files with this project's LGPL header: "yes", remove the headers (D9).
3. The 72 cases of `xacml3.0-deprecated`: "why they were deprecated and if there is good reason tests are
   deprecated don't run them". Finding: the suite's index (`ConformanceTests.html`, "Identifiers planned for
   future deprecation") uses "deprecated" for the identifiers of XACML 3.0 §10.2.9 and states they "are
   still mandatory to implement in a 3.0 compliant PDP"; the normative §10.2.9 says "The implementation
   MUST properly process those features associated with the identifiers marked with an 'M'" (XACML 3.0
   core, OASIS Standard 22 January 2013, fetched 2026-10-27; errata 01 not re-checked). The optional
   identifiers there are the three `xpath-node` functions, which the suite leaves out, and
   `access-permitted`. No good reason to skip them: they run (task 3.3), each reviewed like the other
   experimental AT&T cases.
