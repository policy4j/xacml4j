# NTM remaining-work preflight

Date: 2026-10-29. Base: `759d90a6c75510d55127d8e22870f9edd985c991`.
Scope: resume approved xacml4j optimization and modernization. No ValueBox work.

The conductor created group `policy4j-cwtu.42`, its 21 checklist tasks, and four implementation/review subissues. Historical checked tasks were left open pending proof review. Five lane issues and the group were claimed; no implementation task was closed.

The initial NTM launch created five isolated worktrees but exited 1 before starting any agent: its pre-launch guard observed a transient occupied fish pane. A fresh process inventory then confirmed that all six new session panes were fish shells with no child processes, and every new worktree was clean. Only those five empty agent panes were restarted in their already-created worktrees. Older batch2 worktrees were preserved.

Actual startup headers confirmed the requested models and effort in `launch-receipt.json`. NTM's inferred `context_model` reported Astra for all Codex panes, so that metadata is PARTIAL; it was not used as model identity evidence. The CLI headers are the primary observation. Each lane received exactly one packet through `ntm send`, with a success receipt reporting `delivered: 1, failed: 0`. Later pane observations confirmed source-reading activity in all five lanes.

| Lane | Beads issue | Assignment |
| --- | --- | --- |
| Terra | `policy4j-cwtu.42.14` | D47 runtime-value eligibility regression and correction |
| Sol | `policy4j-cwtu.42.14.1` | Variable-DAG response-capability traversal and stored algorithm gate |
| Astra | `policy4j-cwtu.42.19.1` | Independent D52 adversarial source review |
| Luna | `policy4j-cwtu.42.14.2` | Independent D47 regression-coverage review |
| Agy Argon | `policy4j-cwtu.42.19.2` | Independent other-provider D52 adversarial review |

Writer file ownership is disjoint. D52's eight candidate source/test files were copied into a frozen packet snapshot and hashed before dispatch. The preserved candidate remains in the older batch2 cod_1 checkout. A shared advisory-lock wrapper serializes Maven commands, selects JDK 25 and Maven 3.9.16, and requires offline execution with license rewriting disabled.

Verification here covers launch, model selection, packet delivery, clean initial lane trees, wrapper syntax, and a serialized Maven `--version` run (`toolchain.log`): Maven 3.9.16, Oracle Java 25.0.4, aarch64. No compilation or tests ran in that version check. Builds, tests, conformance, source-review verdicts, controlled JMH pairs, and final acceptance remain UNRUN or IN PROGRESS. Launch and delivery are not completed implementation evidence.

## Build-path diagnosis

Maven 3.9.16 stopped before compilation when it could not accept Guava 33.7.2-jre's cached repository identity in the current offline context. The installed Maven 3.10.0, run through the same wrapper with JDK 25.0.4, resolved the dependencies without changing user configuration, routes, artifact provenance or dependency versions (`toolchain-maven310.log`). Its first incremental core test compilation hit javac's `unknown annotation tag` assertion (`maven310-core-test-compile.log`). The same unchanged source then passed `clean test-compile` (`maven310-clean-core-test-compile.log`): 287 production and 174 test source files compiled. This isolates the failed incremental-output path; it does not identify an individual bad class file or count as an executed regression test. The active writer packets were directed to explicit Maven 3.10/JDK 25 clean builds. Maven 3.9 remains unverified, and no test or performance acceptance is claimed by these compiler checks.
