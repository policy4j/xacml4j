## ADDED Requirements

### Requirement: Provenance per suite
Every suite descriptor SHALL record the upstream source (URL and revision or date), the license basis
of its files, the local edits made to them, and the content hash of every vendored file. A vendored file
whose hash differs from the record SHALL fail the build.

#### Scenario: A changed fixture
- **GIVEN** a vendored fixture whose bytes differ from the hash in its suite record
- **WHEN** the integrity check runs
- **THEN** it fails and names the file (`VendoredFixtureIntegrityTest`)

#### Scenario: Every suite has a record
- **GIVEN** the suites of the conformance module
- **WHEN** the descriptors are read
- **THEN** each has a source, a license basis, and an edit list, possibly empty (`SuiteDescriptorTest`)

### Requirement: Adoption of a new source
A new source of vectors, and every file derived from one, SHALL be vendored only with a documented
redistribution basis for this LGPL v3 repository, recorded for each file or for a file group shown to be
uniform: its origin (authored by the source project, inherited from another source such as an OASIS
package, or generated) and the basis that applies to that origin (for example the MIT, BSD, or Apache-2.0
license of the project that authored it), with the license text and NOTICE next to it. GPL-only material, unlicensed material, and
material whose basis is not documented (including new files of the legacy OASIS packages and files derived
from them) SHALL NOT be vendored. A candidate case SHALL count as a duplicate only when its whole case
(ordered policies, request, expected outcome, and harness configuration) equals an existing case.

#### Scenario: A file without an origin record
- **GIVEN** an adopted file with no origin and basis record and no membership in a documented uniform group, and a group record that covers only some of its files
- **WHEN** the adoption check runs
- **THEN** it fails and names each uncovered file (`VectorAdoptionCheckTest`)

#### Scenario: A duplicate case
- **GIVEN** a candidate case whose policies, request, expected outcome, and configuration equal an existing case
- **WHEN** the adoption check runs
- **THEN** the case is reported as a duplicate and not added (`VectorAdoptionCheckTest`)

#### Scenario: A shared file in a distinct case
- **GIVEN** a candidate case that shares its request file with an existing case but has other policies
- **WHEN** the adoption check runs
- **THEN** the case is not a duplicate (`VectorAdoptionCheckTest`)

### Requirement: Corrections and conventions kept apart
Every difference between a vendored fixture's expectation and the expectation the runner uses SHALL be
recorded in the manifest as one of: a correction (with the XACML section that justifies it), a harness
choice, an engine convention, or an unsupported feature; an engine convention of another implementation
SHALL NOT be adopted as an expectation of this one.

#### Scenario: An adjudicated case
- **GIVEN** a row whose expectation differs from its fixture
- **WHEN** the manifest is read
- **THEN** the row names its category, and a correction names an XACML section (`SuiteDescriptorTest`)

### Requirement: Every adopted case is reviewed against the standard
Each case adopted from another implementation SHALL have a review record (the XACML section, the reviewer,
the date, and the reasoning for its expected result) before it is enabled; a structural test SHALL reject
an enabled adopted case without a record. A case whose expectation encodes another engine's convention
SHALL be disabled with its category and a reason, and a case that this engine fails SHALL be enabled only
after the engine is fixed, or disabled with a bead.

#### Scenario: An adopted case without a review record
- **GIVEN** an enabled adopted case with no review record
- **WHEN** the manifest is read
- **THEN** reading fails and names the case (`SuiteDescriptorTest`)
