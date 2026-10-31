# Task 3.2 snapshot-store isolated compile

Lane: snapshot store; persona policy4j_implementer_codex_astra; CLI Codex; model GPT-6 Astra (task packet).

Result: implementation delivered for conductor testing. SUPPORTED: isolated offline Maven test-compile exited 0 using Maven 3.9.16 and JDK 21.0.6 (Oracle GraalVM). See command.txt, env.txt, and output.txt. No sandbox blocker occurred. Maven did not run in the shared source tree; xacml-jmh/src/test and the other implementation lanes were excluded from the copy. No tests were run by this compile.

## Files written

- xacml-jmh/src/main/java/org/xacml4j/jmh/store/SnapshotPolicyRepository.java
- xacml-jmh/src/main/java/org/xacml4j/jmh/store/SnapshotPolicyStore.java
- This evidence run and evidence/tests/006-snapshot-astra-characterization/.

## Decisions within the fixed contract

- One volatile Snapshot record contains both immutable id maps. All four write hooks share updateLock, copy the affected version map and id map, and publish one replacement holder. Exact lookup reads the holder once; read methods take no lock. Constructor uses the specified Runnable::run executor (SnapshotPolicyRepository.java:53, :57, :61, :94, :110, :126, :142, :199).
- Version keys use TreeMap ordering, preserving comparison-equivalent versions. Existing entries are checked before mutation, so duplicates retain the first rule. Empty indexes are retained, matching InMemoryPolicyRepository's policy-id precedence for exact lookup, including after removal (SnapshotPolicyRepository.java:96, :112, :159; xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/InMemoryPolicyRepository.java:109).
- Unconstrained latest lookup uses lastEntry. Constrained lookup walks descending versions and applies every supplied VersionMatch.match predicate, matching the production repository. Collection queries keep ascending order and return an empty list for missing ids. Null returns occur only at the three PolicyRepository single-result boundaries (SnapshotPolicyRepository.java:72, :77, :155, :167, :183, :193).
- SnapshotPolicyStore registers with AutoService, declares LOAD and CONCURRENT_READS, rejects any supplied argument, and opens a new repository with the context providers/id. The lambda session inherits no-op close (SnapshotPolicyStore.java:38, :49, :54; harness/StoreSession.java:62).

## Verification

- Before implementation, 25 production-repository characterization checks passed: ../../tests/006-snapshot-astra-characterization/output.txt.
- The generated PolicyStore service descriptor includes both InMemoryPolicyStore and SnapshotPolicyStore. The two compiled source copies are byte-identical to the delivered files; SHA256 values and matching LGPL headers are recorded in verification.txt.
- Both no-index whitespace checks emitted no diagnostics. Each returned 1 because a new file differs from /dev/null; these are not test results.
- openspec validate add-pdp-jmh-benchmarks --strict passed before implementation: command output was "Change 'add-pdp-jmh-benchmarks' is valid".
- Shared-tree git status was inspected before work and after both builds. Unrelated dirty files were preserved; no Git mutation was performed.

## Tracking and residual work

The initial br update --claim returned exit 4 because policy4j-ba4s.3.1 blocks this issue. br update --help documents --force for blocked issues. Under the explicit parallel-lane task packet, br update policy4j-ba4s.3.2 --claim --force --actor policy4j_implementer_codex_astra succeeded and assigned this lane. Dependencies were not changed. The task checkbox is not changed and the issue remains in progress for conductor review. Final br sync --flush-only exited 0 with nothing to export; br show confirmed the in-progress claim. See tracking.txt.

UNVERIFIED: snapshot runtime parity, unknown-id behavior at runtime, concurrent reads/writes, fixture decisions, and runtime ServiceLoader discovery. Per packet, the conductor must run SnapshotPolicyRepositoryTest, the snapshot cases of PdpFixtureTest, and the remaining group gates. No xacml-jmh test, conformance suite, JMH smoke run, or performance measurement ran in this lane. JMH applies to the group and remains with the conductor. No current implementation blocker.
