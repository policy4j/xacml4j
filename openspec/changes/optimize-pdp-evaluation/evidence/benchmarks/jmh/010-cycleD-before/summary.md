# 010: cycle D before (the cycle B jar; pair with 011)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the cycle's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the cycle B jar of session 2 (`after-cycleB-benchmarks.jar`, cycles A and B). First run of the chain 010 -> 011 -> 012 -> 013.

## Drift against run 009 (the same jar in session 2)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 10.58 ± 0.26 | 10.6 ± 0.29 | us/op | +0.2% | overlap: no claim | 24411 | 24345 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 15.55 ± 0.64 | 15.69 ± 0.49 | us/op | +0.9% | overlap: no claim | 45290 | 39526 |

Times agree within the intervals. The bytes of synthetic:100 differ by 13% between the two sessions for the same jar (45,290 and 39,526); the scan sets and rsa2008 agree within 1%. Compare bytes of synthetic:100 only inside one session.
