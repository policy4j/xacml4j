# 007: group 1 with -t 4, before (pair with 008, task 1.4)

Session 2, 2026-10-27 07:45 to 08:08 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average at start 7.6 on 12 cores (other processes), the same for both runs of a pair, which run back to back. Commands: `command.txt` of the session script, below. A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the tree with the base sources of the three group 1 files (random sources in place), cycle A included as in 008. Comparison in `008-group1-t4-after/summary.md`.
