Beads: epic `<!-- epic id -->`; task X.Y is issue `<epic>.X.Y`.
Gate (default for code tasks): `<!-- command from docs/ai/TESTING.md -->`

## 1. <!-- Group name -->

- [ ] 1.1 Write a failing (or characterization) test for <!-- behavior -->; check: `<!-- test class -->`
- [ ] 1.2 <!-- Implementation task --> ; check: `<!-- test class or command -->`
- [ ] 1.3 Gate: code review (evidence/reviews/), module tests and conformance (evidence/tests/), JMH decision (evidence/benchmarks/jmh/ or "not applicable: <reason>"), docs/spec consistency

## 2. Close out

- [ ] 2.1 `openspec validate <!-- change-id --> --strict`
- [ ] 2.2 Audit requirement → task → test → evidence in evidence/README.md
- [ ] 2.3 Close the beads epic and run `br sync --flush-only`
