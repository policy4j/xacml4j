# rv023: task 3.5 review, vendored-fixture integrity check (Grok, adversarial)

Date: 2026-10-01. Lane: adversarial code reviewer, Grok Build grok-4.7
(xhigh), role `.ntm/roles/policy4j_code_review.md`, session cleared first.
Session `policy4j--g1`, pane 0. Read-only; recomputed every working-tree blob
in JDK 21 jshell and compared with the manifest and one
`git hash-object --stdin-paths` pass. Did not run Maven.

Scope: same as rv022.

Verdict: APPROVE.

Findings:
- LOW, `GenerateVendoredFixtureManifest.java:96-98`: a new empty directory in
  either vendored tree is invisible (the walk keeps regular files and
  symlinks). The tree has none, and git does not store empty directories, so
  this is not a byte-integrity hole. Accepted; documented here.

Reviewer statements: walks xacml-conformance-tests/src/test/resources (2,906
files, including log4j2-test.xml) and xacml-xml-jaxb/src/main/resources/xsd
(5 files); git blob ids on raw bytes; strictly sorted LF manifest of 2,911
unique rows outside both trees; repository root found from user.dir in a
module or root working directory; the generator has no @Test and writes only
with `--write`; headers and tabs match siblings; JUnit 5 only. Recomputation:
added=0, missing=0, changed=0, git mismatch=0, restored fixtures 17/17; the
in-memory byte flip matches the recorded mutation id. Only the six task 1.5
restorations are dirty in the vendored trees.

Residual (reviewer): empty directories and mode-only changes stay green;
symlink hashing unexercised (no links); `-Dvendored.fixture.root` can point
the scan at another tree while the manifest stays (unset in the POM). The
stale run 015 note about the TESTING.md pointer was corrected by the conductor.
