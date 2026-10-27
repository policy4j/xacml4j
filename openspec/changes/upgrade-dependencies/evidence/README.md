# Evidence

Layout: `tests/`, `reviews/<lane>/`, `builds/`, `final/`. One directory per
run: `<NNN>-<scope>`. Each run holds `summary.md`, `command.txt`, and its raw
output. See docs/ai/OPENSPEC.md "Evidence".

## Requirements to proof

| Capability / requirement / scenario | Check | Run |
| --- | --- | --- |
| dependency-hygiene / No vulnerable logging backend / Dependency tree | `dependency:tree` per module | `tests/002-logging-gate` |
| dependency-hygiene / No vulnerable logging backend / Ban rule | scratch POM with `log4j-core` | `tests/002-logging-gate` |
| dependency-hygiene / Current and used dependencies / Version audit | Maven Central metadata on the gate day | pending |
| dependency-hygiene / Current and used dependencies / Unused entries | `dependency:analyze`, property search | pending |
| dependency-hygiene / Behavior unchanged / Gate | reactor with both Mavens, conformance | pending |
| dependency-hygiene / Behavior unchanged / Vulnerability scan | scan of research Q8 | pending |
| logging-configuration / Modular Logback configuration / Module override | `LoggingConfigurationTest` | `tests/002-logging-gate` |
| logging-configuration / Modular Logback configuration / Shared defaults, Property override | `LoggingConfigurationTest` | `tests/002-logging-gate` |
| logging-configuration / Modular Logback configuration / Library jars | jar listings | `tests/002-logging-gate` |
| logging-configuration / Asynchronous appenders / Flush at exit | `LoggingConfigurationTest` | `tests/002-logging-gate` |
| logging-configuration / Asynchronous appenders / Benchmark appender never blocks | `LoggingConfigurationTest` (`xacml-jmh`) | `tests/002-logging-gate` |

## Gate tasks to proof

| Task | Run(s) | Status |
| --- | --- | --- |
| 1.2 Plan review | `reviews/plan/` | pending |
| 2.4 Logging gate | `tests/004-logging-regate`, `reviews/code/rv001-gates-astra` | done (review REJECT; five findings fixed) |
| 3.4 Runtime libraries gate | `reviews/code/`, `tests/` | pending |
| 4.4 Test libraries gate | `reviews/code/`, `tests/` | pending |
| 5.1 Final gate | `tests/` | pending |

## Runs

| Run | Kind | Command | Result |
| --- | --- | --- | --- |
| `tests/001-characterization` | characterization (static) | `git show HEAD:pom.xml` | `log4j-core` 2.14.0 compile scope in every module |
| `tests/004-logging-regate` | gate with sanitized output (after rv001) | regate.py | 8,230 tests; no Log4j; ban fails a log4j-core POM; flush tests |
| `reviews/code/rv001-gates-astra` | gate review, Codex GPT-6 Astra | NTM pane, read-only | REJECT; fixed |
| `tests/003-offline-upgrades` | removals, auto-service 1.1.1, Hamcrest 3.0 | reactor offline | 8,206 tests, 0 failures; remaining upgrades wait for the mirror |
| `tests/002-logging-gate` | gate checks | reactor, dependency tree, ban proof, jar listings | 8,206 tests, 0 failures; no Log4j; ban rule fails a log4j-core POM |

## Not applicable

- JMH for every group: NOT APPLICABLE: the maintainer decided on 2026-10-27 that dependency upgrades need no JMH runs.
