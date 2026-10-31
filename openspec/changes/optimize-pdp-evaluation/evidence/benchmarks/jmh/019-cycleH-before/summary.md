# 019: cycle H before (pair with 020)

Session 5, 2026-10-27 10:26 to 10:38 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive. Jars from frozen tree copies that differ only in the five files of cycle H (sha256 in `env.txt`). A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the committed tree (cycles A to F with the review fixes, commit 9855935a). Comparison in `020-cycleH-after/summary.md`.
