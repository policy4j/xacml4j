# Evidence

Layout: `tests/`, `reviews/<lane>/`, `benchmarks/jmh/`, `builds/`, `final/`.
One directory per run: `<NNN>-<scope>` (reviews: `rvNNN-<scope>-<lane>`).
Each run holds `summary.md`, `command.txt`, and its raw output. See
docs/ai/OPENSPEC.md "Evidence".

Work runs on branch `integration/modernize-core-jdk28-valhalla` (worktree
`<checkouts>/xacml4j-worktrees/policy4j`). The baseline numbers in
`design.md` (Context) come from planning-time probes and are re-recorded by
task 1.6.

## Requirements to proof

| Capability | Proof (planned) | Run |
| --- | --- | --- |
| attribute-resolution | attribute-resolution tests (task 4.1); conformance | - |
| condition-evaluation | condition tests (task 4.2); conformance | - |
| function-semantics | function tests (task 4.3); conformance | - |
| datatype-registry | registry tests (task 4.4) | - |
| combining-algorithm-registry | registry tests (task 4.4); URN set check (task 6.3) | - |
| conformance-coverage | conformance manifest and fixture integrity check (tasks 3.1-3.6) | - |
| test-framework | per-module test counts; no JUnit 4 or EasyMock imports (task 2.7) | - |
| jdk28-valhalla-build | JDK 28 reactor build and CI job (tasks 8.1-8.5); JMH before/after per value-class family (tasks 9.2-9.4) | - |

## Gate tasks to proof

| Task | Run(s) | Status |
| --- | --- | --- |
| 1.6 baseline test counts | - | pending |
| 2.7 test-stack removal gate | - | pending |
| 3.6 conformance gate | - | pending |
| 4.7 safety-net gate | - | pending |
| 5.5, 6.7, 7.4 refactor gates | - | pending |
| 8.4 JDK 28 gate | - | pending |
| 9.3, 9.4 value-class gates (JMH) | - | pending |

## Runs

| Run | Kind | Command | Result |
| --- | --- | --- | --- |
| `builds/001-donor-preservation` | task 1.1 | git plumbing (see command.txt) | PASS: donor tree equals working tree; HEAD, index, status unchanged |
| `builds/002-integration-branch` | task 1.2 | `git worktree add -b integration/modernize-core-jdk28-valhalla … a8a90d46` | PASS |

## Not applicable

- JMH for groups 1-4 and 8: NOT APPLICABLE: they change tests, fixtures,
  semantics fixes, and the toolchain, not hot paths.
- JMH for groups 5-7: decided per gate task (the refactor touches value
  types and function invocation); group 9 records the value-class baselines.
