# rv005: JMH method review (task 3.7)

Date: 2026-10-27. Lane: `policy4j_jmh_claude_fable`, Claude Code, Claude Fable 5.1 (a family other than the
benchmarks' author, Codex Terra), read-only, second packet in the rv004 pane. Report: `report.md`.

Verdict: APPROVE WITH CHANGES (2 major, 4 minor, 2 nits).

| # | Severity | Finding | Resolution |
| --- | --- | --- | --- |
| 1 | major | import tool per document | as rv004 F1 |
| 2 | major | scaling command uses the `request=all` mix | Benchmark Plan runs the seven request ids as separate trials |
| 3 | minor | JIT not chosen or recorded (GraalVM uses the Graal JIT) | baseline passes `-XX:-UseJVMCICompiler` (C2); `env.txt` records the JIT flags |
| 4 | minor | no check after setup (drift) | `PdpFixture.recheck()` in every tear-down; test `tearDownFailsWhenTheStoreDriftedDuringTheTrial` |
| 5 | minor | `Optional` from `lookup` | as rv004 F2 |
| 6 | minor | run record: heap and GC flags, console log, gc counts, completeness | `-Xms2g -Xmx2g -XX:+UseG1GC` in the annotations; `-o` log; completeness needs a finite error and 15 samples |
| 7 | nit | few operations per 1 s iteration for loads | load benchmark iterates 2 s |
| 8 | nit | `lookup` mixes query kinds by corpus | summary reports counts per kind; no cross-corpus comparison |

Its "What one operation includes" and "must not claim" lists go into the baseline summary (task 4.2).
