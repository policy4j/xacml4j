# Run 073: adjudicated conformance dispositions (beads the.4.18, the.4.20, the.3.9)

Date: 2026-10-07. Rulings: run 061 D13, D14, D15
(`builds/070-maintainer-rulings`). JDK: NOT APPLICABLE for the edit; the
gate ran on JDK 21.

## Change

Only the reason column of 12 rows in
`xacml-conformance-tests/src/test/manifest/conformance-manifest.csv`
changed. No id, path, disposition, enabled flag, or bug id changed, and no
vendored fixture was touched.

| Rows | Ruling | New reason starts with |
| --- | --- | --- |
| IIIC002, IIIC003 (OASIS 2.0 and 3.0) | D13 | Unsupported feature: hierarchical Children/Descendants scope |
| IIIG001 to IIIG005 (OASIS 2.0) | D13 | Unsupported feature: legacy 2.0 XPath over request Attribute/Subject nodes |
| IIIG300 (OASIS 3.0) | D14 | Fixture/spec adjudication: engine kept (Plus Errata 01 section 5.48) |
| IIA023, IID029 (OASIS 3.0) | D15 | Fixture/spec adjudication: fixture kept skipped or disabled |

The earlier reasons called the first two groups an "engine defect" pending
a decision. The decision is now recorded: they are unsupported features.

## Documentation

The repository has no user README. The record of "unsupported" is the
manifest and a new requirement in `specs/conformance-coverage/spec.md`
("Unsupported features and disputed fixtures are named").

## Proof

- `rtk git diff --stat`: 1 file, 12 insertions, 12 deletions.
- Full reactor: conformance module 927 tests, 0 failures, 14 skipped, the
  same counts as run 069; `ConformanceManifestTest` passes
  (`tests/071-resolver-failure-fail-closed/surefire-summary.txt`, gate 5).
- `openspec validate modernize-core-for-jdk28-valhalla --strict` passes.

## Bead state

The three beads carry the ruling. Bead the.3.9 has no open disposition
left. Beads the.4.18 and the.4.20 need no code. All three are closed with
this run as proof.
