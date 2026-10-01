## ADDED Requirements

### Requirement: Every conformance case has a recorded disposition
The conformance module SHALL keep a manifest of every case in the vendored
OASIS XACML 2.0 suite, the OASIS XACML 3.0 suite, the RSA 2008 interop
suite, and the examples. Each case SHALL have exactly one disposition:
`executed`, `special-harness` (for example multi-root-policy repository
cases such as IID029 and IID030), or `skipped` with a written reason. The
test run SHALL fail if a case in the fixture directories is missing from
the manifest.

#### Scenario: New fixture without disposition
- **WHEN** a fixture case exists on disk but not in the manifest
- **THEN** the conformance test fails and names the case

#### Scenario: Skip without reason
- **WHEN** a manifest entry is `skipped` without a reason
- **THEN** the conformance test fails

### Requirement: Case enumeration comes from fixtures
Conformance tests SHALL enumerate cases from the fixture inventory, not
from hard-coded numeric loop bounds, and SHALL execute the last case of
each group.

#### Scenario: 3.0-specific cases run
- **WHEN** the XACML 3.0 conformance test runs
- **THEN** cases such as IIA022-IIA024, IIC300-IIC359, IID300-IID343, and IIIA301-IIIA340 are executed or carry a recorded disposition

#### Scenario: Last case of a group runs
- **WHEN** a group has cases numbered 1 to N
- **THEN** case N is executed or carries a recorded disposition

### Requirement: Policy import failures fail the run
Loading the policies of a conformance case or of the shared repository
SHALL fail the test with the parse or mapping error; it SHALL NOT be
logged and ignored.

#### Scenario: Broken policy fixture
- **WHEN** a policy file referenced by an executed case cannot be imported
- **THEN** the test fails with the import error and the file name

### Requirement: Vendored fixtures stay byte-identical to upstream
Files under `xacml-conformance-tests/src/test/resources/**` and
`xacml-xml-jaxb/src/main/resources/xsd/**` SHALL be byte-identical to their
upstream OASIS, RSA, or W3C sources, except for changes recorded with a
reason in the manifest. A check SHALL compare them with the recorded
baseline blobs.

#### Scenario: Prose edit to a fixture
- **WHEN** a find-and-replace changes a comment or description in a vendored fixture
- **THEN** the fixture integrity check fails and names the file

### Requirement: Acceptance gate
The full conformance run SHALL pass on the build's target JDK: every
`executed` case SHALL produce the expected response, and the number of
executed cases SHALL NOT decrease between phases without a recorded
disposition change.

#### Scenario: Case count regression
- **WHEN** a phase ends with fewer executed cases than the previous phase and no manifest change explains it
- **THEN** the phase gate fails
