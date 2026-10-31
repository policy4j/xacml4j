READ-ONLY PLAN REVIEW (a new packet in this pane; rules: no file writes, no Maven, tests, benchmarks, git or
br writes; do not use the GitHub connector and do not sign in to anything). Role: architecture reviewer from
a model family other than the author's (the conductor, Claude Opus 5.5, wrote the plan). Lane tag RVT1.

Review the new OpenSpec change openspec/changes/adopt-conformance-test-vectors: proposal.md, research/README.md
(and the four lane reports next to it, one of which is yours, TV2), specs/conformance-suite-harness/spec.md,
specs/conformance-vector-provenance/spec.md, design.md, tasks.md, evidence/README.md, against the code of
xacml-conformance-tests (src/test/java/org/xacml4j/v20/*, src/test/manifest/conformance-manifest.csv, pom.xml)
and xacml-test (Xacml20TestUtility, Xacml30TestUtility, XacmlPolicyTestSupport).

Look for: requirements that cannot be tested as written or whose scenario names a test that the tasks never
create; an order of work that could change today's 880 outcomes unnoticed; a design that needs a dependency
the build does not have; gaps between the research answers and the plan (sources missed, licenses misread,
a convention adopted as a requirement); the vendored-file rule of AGENTS.md (never edit vendored fixtures)
and whether every step respects it; the download rule (each external download only with the maintainer's
explicit permission); the open questions and whether any blocks approval.

Report: findings most severe first (P1 to P3) with file:line and the smallest fix; then a verdict on the plan:
APPROVE, APPROVE WITH CHANGES, or REJECT.

Write your report between these two lines, each on its own line, exactly as written:
=== BEGIN RVT1 REPORT ===
=== END RVT1 REPORT ===
