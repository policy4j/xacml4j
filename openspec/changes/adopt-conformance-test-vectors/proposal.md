## Why

The maintainer asked (2026-10-27): "look for XACML test vectors in other popular XACML implementations I
would like to adopt test vectors and stream line conformance tests". A four-lane research swarm
(`research/`) found that no normative OASIS XACML 3.0 suite exists, that our 3.0 suite is AT&T's v0.4
set byte for byte, and that other engines (above all AuthzForce, Apache-2.0) carry corrections and extra
cases we lack. Today four runners and three comparators drive 880 manifest rows, special cases are case ids
hard-coded in `ConformanceCaseRunner`, and nothing records where a suite came from or under which terms.

## What Changes

- One descriptor per suite: the CSV manifest gains columns (case kind, comparison mode, structured special
  handling with fixture guards, category, source, review record), and a `suite.properties` and, where
  needed, a `pip.csv` per suite record title, version, media type, root, provenance, license, edits, and
  supplied attributes, outside the vendored tree (`xacml-conformance-tests/src/test/suites/`).
- One JUnit 5 `@TestFactory` runner with a container per suite and group; case kinds for evaluation,
  policy rejection, and request and response validation; adapters for OASIS XML 2.0 and 3.0, RSA 2008, and
  JSON.
- Comparison: a `ResponseComparison` entry point in `xacml-test` over wire-level comparators (the existing
  JAXB comparison for XML; a JSON comparator supplied by the conformance module), with legacy modes that
  reproduce each old comparator for the equivalence gate, and `full`, `decision-status`, and `decision`
  modes; one explicit exception, `legacy-model`, compares model responses as today for the RSA 2008 and
  example suites and stays as a second check after their move to `full`, because the XACML 2.0 response
  marshaller does not write nested status codes and details; `XacmlPolicyTestSupport.assertResponse`
  keeps its model-level semantics.
- Today's results stay: a frozen record of every row's outcome and per-field results, with altered-field
  controls, is checked after the runner change, after the comparator change, and before the old runners
  (`Xacml20ConformanceTest`, `Xacml30ConformanceTest`, `RSA2008InteropTest`, `XacmlExampleTest`), the
  hand-written resolver, and the case-id logic are folded in; `RSA2008PerformanceTestCase` is removed.
- Provenance per suite (source, license basis, edits, hashes); corrections, harness choices, engine
  conventions, and unsupported features recorded apart; duplicates only by whole case.
- The 72 `xacml3.0-deprecated` cases run (XACML 3.0 §10.2.9 makes their identifiers mandatory).
- The 34 RSA 2008 request and response files lose the LGPL header this project's license plugin added
  (maintainer exception to the read-only rule, D9), with a check that nothing else changed.
- New vectors, each file with a documented redistribution basis and each case with a review record:
  AuthzForce's own additional cases and JSON validation fixtures (Apache-2.0), project-written gap vectors,
  and small licensed examples (Curity, Go, `ndg_xacml`). Deferred until a basis is documented: OASIS 2.0
  patch #1, later OASIS 2.0 files, inherited OASIS files inside other sources, and JSON derived from OASIS
  fixtures.
- An audit of our skips and adjudications against the AuthzForce errata list.
- Not adopted: GPL-3.0 AuthzForce repositories, unlicensed mirrors, research datasets with unclear terms,
  and copies of suites we hold.

## Capabilities

### New Capabilities

- `conformance-suite-harness`: descriptors and case kinds, the runner, wire comparison and its modes,
  structured special handling, the table-driven resolver, JSON vectors.
- `conformance-vector-provenance`: provenance, per-file redistribution basis, duplicate rule, categories
  of corrections and conventions, review records.

### Modified Capabilities

None (no archived specs exist).

## Research Questions

Answered by the swarm (`research/README.md`) and the maintainer (design, Open Questions). Open: the OASIS
terms for the legacy test XML (kept as is, recorded); the standard ids of the `ipAddress` and `dnsName`
set functions (task 6.1); how far `xacml-json` reads and writes the published JSON Profile forms (task 5.1).

## Verification

- `RunnerEquivalenceTest` against the frozen record, three times, with controls; `SuiteDescriptorTest`,
  `ResponseComparatorTest`, `TablePipTest`, `VectorAdoptionCheckTest`, the ported guard tests.
- Each adopted case has a review record and runs in the reactor; a failing case is fixed with a bead or
  disabled with reason and bead.
- RSA 2008: same results, exactly 34 files changed, ledger delta of exactly 34 entries.
- Reactor `mvn -o clean test` on JDK 25 with conformance; `openspec validate --strict`.

## Impact

- `xacml-conformance-tests`: the runner, adapters, descriptors under `src/test/suites/`, the JSON
  comparator, test dependencies on `xacml-json` and Gson; new vendored directories for adopted sources
  (each with its license and NOTICE); the RSA 2008 header removal (34 files) and a wider
  `license.excludes`; the fixture inventory and its tests for the 72 deprecated cases.
- `xacml-test` (a published test-support artifact): new `ResponseComparison` and `WireComparator` types;
  the public methods of `Xacml20TestUtility` and `Xacml30TestUtility` delegate in their legacy modes with
  unchanged results; `XacmlPolicyTestSupport` unchanged; no new dependency.
- `docs/ai/TESTING.md`: the conformance section.
- Downloads: each external source, file, and size only with the maintainer's explicit permission; plan
  approval is not download permission.

## Tracking

Beads epic: created after approval (docs/ai/BEADS.md).
