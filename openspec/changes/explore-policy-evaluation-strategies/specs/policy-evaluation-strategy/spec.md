## ADDED Requirements

### Requirement: Same decisions under every evaluation strategy
An alternative evaluation strategy for the policy tree SHALL give the same decision, status code,
obligations, advice, and policy identifier list as the recursive top-down evaluation for every request.
(Exploration stage: further requirements follow the maintainer's decisions in `research/README.md`.)

#### Scenario: Conformance under an alternative strategy
- **GIVEN** a PDP configured with an alternative evaluation strategy
- **WHEN** the OASIS conformance suites and the RSA 2008 suite run
- **THEN** every case has the same result as with the recursive top-down evaluation
