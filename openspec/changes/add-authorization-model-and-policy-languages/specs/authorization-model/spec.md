## ADDED Requirements

### Requirement: Opt-in authorization model
An authorization model, a policy type check against it, and the ALFA and Cedar front ends SHALL be opt-in.
With all of them disabled, every decision, every conformance result, and the public API SHALL be the same
as without the enhancement. (Exploration stage: further requirements follow the maintainer's decisions in
`research/README.md`.)

#### Scenario: Everything disabled
- **GIVEN** a PDP configured without an authorization model and without a language front end
- **WHEN** the OASIS conformance suites and the RSA 2008 suite run
- **THEN** every case has the same result as before the enhancement (reactor and conformance run of the first implementing task)
