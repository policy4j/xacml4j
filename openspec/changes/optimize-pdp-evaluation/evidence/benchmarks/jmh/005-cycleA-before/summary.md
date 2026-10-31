# 005: cycle A before (pair with 006)

Session 2, 2026-10-27 07:45 to 08:08 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average at start 7.6 on 12 cores (other processes), the same for both runs of a pair, which run back to back. Commands: `command.txt` of the session script, below. A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the tree with the base `DefaultEvaluationContextHandler` (no request memo), groups 1 and the logging change in place. Results: `results-mix.json`, `results-scan.json`; the comparison is in `006-cycleA-after/summary.md`.
