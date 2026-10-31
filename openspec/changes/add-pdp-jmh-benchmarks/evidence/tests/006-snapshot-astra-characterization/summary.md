# Production repository characterization before task 3.2

Lane: snapshot store; persona policy4j_implementer_codex_astra; CLI Codex; model GPT-6 Astra (task packet).

SUPPORTED: 25 standalone checks passed, exit code 0. See output.txt. The checked code is the existing xacml-pdp InMemoryPolicyRepository, compiled from the initial isolated source copy before either snapshot production file was written. This is a Java source-launcher check, not a JUnit or JMH test run. No JMH lock is acquired.

Environment: Maven 3.9.16, JDK 21.0.6 (Oracle GraalVM), macOS aarch64. The baseline command compiled the xacml-jmh reactor prerequisites and generated the dependency classpath offline; exit code 0, no output. Environment identity is recorded in ../../builds/007-snapshot-astra-compile/env.txt.

Checks cover policy and policy-set version ordering, exact lookups using equivalent versions, latest lookup, intersection of all supplied VersionMatch predicates, duplicate rejection with first-instance retention, removal by id and version, absent exact lookup, and the known unknown-id collection exceptions (policy4j-dofm). The exceptions characterize the existing defect and are deliberately not the desired snapshot behavior.

Source and command are retained here. No source under xacml-jmh/src/test was written or executed. Snapshot behavior, ServiceLoader runtime discovery, concurrency, conformance, and JMH performance remain UNVERIFIED by this run; the conductor owns those gates.
