## ADDED Requirements

### Requirement: Suite descriptors and case kinds
Every conformance suite SHALL be described outside its vendored fixture directory by a suite descriptor
(title, XACML version, media type, fixture root, provenance, and license) and by rows of the conformance
manifest. Every row SHALL name its case kind and the fields that kind requires: `evaluation` (request,
ordered policies, expected response or decision, comparison mode), `policy-rejection` (the policies and
the expected defect), `request-validation` and `response-validation` (the document and whether it is
valid). A row that names a missing file, an unknown kind or verb, a missing required field, or a disabled
row without a reason and a bead SHALL fail the build before any case runs.

#### Scenario: A row without a reason
- **GIVEN** a manifest row that is disabled and has no reason
- **WHEN** the manifest is read
- **THEN** reading fails with a message that names the suite and the case (`SuiteDescriptorTest`)

#### Scenario: A kind without its fields
- **GIVEN** a `policy-rejection` row without an expected defect
- **WHEN** the manifest is read
- **THEN** reading fails with a message that names the field and the case (`SuiteDescriptorTest`)

### Requirement: One runner
The conformance module SHALL run every suite through one JUnit 5 runner that creates a test container
per suite and per group and a test per case, and that runs a disabled row as a skipped test whose message
holds its reason and bead. A suite SHALL need code of its own only for a format the shared adapters do
not read.

#### Scenario: Skipped rows
- **GIVEN** a disabled row with a reason and a bead
- **WHEN** the runner runs its suite
- **THEN** the case is reported as skipped and the report holds the reason and the bead (`ConformanceSuiteTest`)

### Requirement: Same outcomes and fields after the move
Before an old runner or comparator is removed, the new runner SHALL give, for every row of the manifest,
the same outcome (pass, fail, or skip) and the same comparison state (compared with its per-field
results, or not compared with the reason, for example a skipped row or an expected policy rejection with
its phase and defect) as a frozen record made by the old runners and comparators before any change, using
for each suite the legacy mode that reproduces exactly what its old comparator checked; the check SHALL be
run again after the comparator change and before the old runners are folded in, and it SHALL include
control cases with deliberately altered response fields that both the old record and the new runner
reject. A row moves from its legacy mode to `full` only after the last equivalence check, one row at a
time, with the move recorded; a row of a `legacy-model` suite keeps its `legacy-model` check in addition to
`full`, so a status difference that the XACML 2.0 response marshaller does not write still fails. After the
moves, every row's outcome SHALL equal the frozen record, which stays unchanged, combined with the
approved overrides: a row whose outcome changes stays in its legacy mode until the cause is fixed, or
until an override for that row (old and new outcome, reason, approver, date) is reviewed and approved.

#### Scenario: Overrides of the frozen record
- **GIVEN** a row whose outcome changed after its move to `full`
- **WHEN** the final comparison runs without an override, with an approved override for that row and its exact old and new outcome, and with an override that lacks approval, names a stale old outcome, names a wrong new outcome, or duplicates another override
- **THEN** it fails without an override, passes with the approved exact override, and fails for each faulty override (`RunnerEquivalenceTest`)

#### Scenario: A passing rejection without a comparison
- **GIVEN** the rows of `IIA004` and `IIC012` in the 2.0 and 3.0 suites, which pass after the expected static rejection
- **WHEN** the frozen record and the new runner record them
- **THEN** both record pass and not compared, with the rejection phase and defect (`RunnerEquivalenceTest`)

#### Scenario: Every current row
- **GIVEN** the frozen record of the 880 rows made by the old runners at the change's first commit
- **WHEN** the new runner runs them after the comparator change
- **THEN** each row has the same outcome and the same per-field results (`RunnerEquivalenceTest`)

#### Scenario: Altered response fields
- **GIVEN** control copies of expected responses with one obligation, one status detail, one repeated category, or one resource id changed
- **WHEN** the new runner compares them
- **THEN** each control fails, as it fails in the frozen record (`RunnerEquivalenceTest`)

### Requirement: Lossless comparison
The runner SHALL compare responses in a representation that keeps every field the fixture states: for XML
suites at the wire level (repeated obligations and advice, repeated categories, the XACML 2.0 resource id
and status detail, nested status codes), except in mode `legacy-model`, which keeps today's lossy model
comparison (the model merges obligations by id and categories by category id), for JSON suites on the JSON Profile document produced by the
public JSON response marshaller. A comparison entry point in `xacml-test` SHALL select the wire comparator
by format through a comparator interface; the JSON comparator SHALL be supplied by the conformance module
(no new dependency of `xacml-test`). Modes: the legacy modes of each old comparator (`legacy-20`,
`legacy-30`, and `legacy-model`, which compares the model responses with the unchanged
`XacmlPolicyTestSupport` comparison, because the XACML 2.0 response marshaller does not write nested status
codes or status details), `full` (every stated
field), `decision-status`, and `decision` (decision only, for vectors that state no status). Typed values
SHALL compare by value (integer `1` equals `01`), and bags of values without order but with multiplicity.
In every mode except `legacy-model`, a mismatch SHALL name the case and every differing field, not only
the first; in `legacy-model` the verdict stays the old one and the message names the case and the first
difference, as the unchanged utility reports it. The public
`XacmlPolicyTestSupport.assertResponse(ResponseContext, ResponseContext)` SHALL keep its current
model-level semantics.

#### Scenario: Equivalent lexical forms and reordered bags
- **GIVEN** an expected and an actual response that differ only in the lexical form of an integer and the order of a bag
- **WHEN** the comparator compares them with mode `full`
- **THEN** they are equal (`ResponseComparatorTest`)

#### Scenario: Two differences
- **GIVEN** an actual response with a different decision and a missing obligation
- **WHEN** the comparator compares it with mode `full`
- **THEN** the message names the case, the decision, and the obligation (`ResponseComparatorTest`)

#### Scenario: A legacy-model row after its move to full
- **GIVEN** an RSA 2008 row moved to `full`, and an actual model response whose status has an extra nested code or a different status detail
- **WHEN** the row runs
- **THEN** it fails through its `legacy-model` check, although the marshalled response does not show the difference (`ResponseComparatorTest`, `RunnerEquivalenceTest`)

#### Scenario: Two differences in legacy-model
- **GIVEN** an expected and an actual model response with two differences
- **WHEN** they are compared in mode `legacy-model`
- **THEN** the comparison fails as the old utility fails, and the message names the case and the first difference (`ResponseComparatorTest`)

#### Scenario: Legacy 3.0 mode and full mode
- **GIVEN** two 3.0 responses with the same top-level status code and different nested status codes
- **WHEN** they are compared in the legacy 3.0 mode and in mode `full`
- **THEN** the legacy mode accepts them and `full` rejects them (`ResponseComparatorTest`)

#### Scenario: A repeated obligation
- **GIVEN** an expected response with an obligation that appears twice and an actual response with it once
- **WHEN** the comparator compares them with mode `full`
- **THEN** it fails and names the obligation (`ResponseComparatorTest`)

#### Scenario: Decision-only vectors
- **GIVEN** a vector that states only a decision
- **WHEN** it runs with mode `decision`
- **THEN** only the decision is compared (`ResponseComparatorTest`)

### Requirement: Structured special handling
Special handling SHALL be named by a verb in the manifest row with structured fields, not by a case id in
code: the expected phase, the expected defect or status, a fixture guard (the content hash of the fixture
the handling applies to), and for an adjudicated case the replacement expectation and the XACML section.
A case whose fixture hash differs from its guard, or whose failure is a different defect than the one
stated, SHALL fail.

#### Scenario: Static rejection by verb
- **GIVEN** the row of `IIE003` with the static-rejection verb naming its rejected policy file and defect
- **WHEN** the runner runs it
- **THEN** that policy fails the static check with that defect and the case is evaluated with the remaining policies, as today (`ConformanceSuiteTest`)

#### Scenario: A different failure than the stated one
- **GIVEN** the `IIA005` row whose verb states the request defect it expects
- **WHEN** the request fails with another defect
- **THEN** the case fails (`ConformanceSuiteTest`, ported from `ConformanceRequestSyntaxErrorTest`)

### Requirement: Table-driven attribute resolution
Attributes that a suite supplies outside its requests SHALL come from a table in the suite's non-vendored
descriptor data whose rows name the attribute supplied (category, id, data type, value) and the condition
under which it is supplied (an attribute of the request, its value, and the match rule), not from a
resolver written for the suite.

#### Scenario: The physician role
- **GIVEN** the table row that supplies role `Physician` when the subject id equals `Julius Hibbert` ignoring case
- **WHEN** a case asks for the role of subject `julius hibbert`, and another case for the role of another subject
- **THEN** the first gets `Physician` and the second gets nothing (`TablePipTest`)

### Requirement: JSON vectors
The runner SHALL run vectors in the XACML 3.0 JSON Profile format as published (the profile version
recorded per suite), with the profile's `Request` and `Response` envelopes and without rewriting the
source document. An evaluation case SHALL marshal the PDP's response with the public JSON response
marshaller and compare that document with the published expectation. A validation case SHALL pass when
the module's verdict matches the row's expected validity: rejecting an invalid document passes; rejecting
a valid document, or accepting an invalid one, is an `xacml-json` defect with a bead, and the case is
disabled until fixed.

#### Scenario: A published JSON response
- **GIVEN** a JSON Profile response document with a top-level `Response` array
- **WHEN** the runner reads it as an expected response
- **THEN** it is read without rewriting, or the case is disabled with the `xacml-json` bead (`ConformanceSuiteTest`)

#### Scenario: Expected-invalid documents
- **GIVEN** a request row and a response row marked invalid, and a request row and a response row marked valid
- **WHEN** the runner validates them
- **THEN** rejection of the invalid documents passes, and the valid documents pass only when they are read (`ConformanceSuiteTest`)
