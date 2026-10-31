# Evidence

Layout: `tests/`, `reviews/<lane>/`, `builds/`. One directory per run:
`<NNN>-<scope>`. Each run holds `summary.md` and `command.txt`
and cites tests, benchmarks, and specifications by reference, with no execution
output; runs before 2026-10-31 keep their files. See docs/ai/OPENSPEC.md "Evidence".

## Requirements to proof

| Capability / requirement / scenario | Check | Run |
| --- | --- | --- |
| jdk-baseline / Java 25 class files / Class file version | `javap -v` per module | `tests/002-release25-gate` |
| jdk-baseline / Java 25 class files / Older JDK | enforcer on JDK 21 | `tests/002-release25-gate` |
| jdk-baseline / Same behavior on JDK 25 / Reactor gate | reactor `clean test` on GraalVM 25 | `tests/001-jdk25-at-release21`, `tests/002-release25-gate` |
| jdk-baseline / Same behavior on JDK 25 / Service files | jar listings | `tests/002-release25-gate` |
| jdk-baseline / Measured on JDK 25 / Baseline on JDK 25 | `add-pdp-jmh-benchmarks` `benchmarks/jmh/002-baseline-jdk25` | done (34 of 34 sets) |

## Gate tasks to proof

| Task | Run(s) | Status |
| --- | --- | --- |
| 2.3 Release 25 gate | `tests/003-release25-regate`, `reviews/code/rv001-gates-astra` | done (review APPROVE WITH CHANGES; changes applied) |
| 3.1 JDK 25 baseline | `add-pdp-jmh-benchmarks` `benchmarks/jmh/002-baseline-jdk25` | done |

## Runs

| Run | Kind | Command | Result |
| --- | --- | --- | --- |
| `tests/001-jdk25-at-release21` | characterization | reactor on GraalVM 25 at release 21 | 8,202 tests, 0 failures |
| `tests/003-release25-regate` | gate with sanitized output (after rv001) | regate.py | 8,230 tests, 0 failures; class files 69.0; enforcer fails on 21 |
| `reviews/code/rv001-gates-astra` | gate review, Codex GPT-6 Astra | NTM pane, read-only | APPROVE WITH CHANGES |
| `tests/002-release25-gate` | gate | reactor at release 25; javap; enforcer on 21; services | 8,202 tests, 0 failures; class files 69.0 |

## Audit (task 4.2, 2026-10-27)

Every requirement and scenario of `specs/jdk-baseline/spec.md` has a run above; every
task of `tasks.md` is done; the gate review's changes are applied (`rv001-gates-astra`).
Later reactor runs on JDK 25 stay green (`optimize-pdp-evaluation` tests 011 to 017).

## Not applicable

None.
