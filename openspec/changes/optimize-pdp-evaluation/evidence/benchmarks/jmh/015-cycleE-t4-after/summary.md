# 015: cycle E with -t 4, after (pair with 014)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the cycle's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the cycle E jar. Four threads share one PDP.

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 11.28 ± 0.18 | 9.477 ± 0.25 | us/op | -16.0% | disjoint | 23291 | 20414 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 21.02 ± 1.4 | 20.67 ± 1.4 | us/op | -1.7% | overlap: no claim | 34799 | 31523 |

## Result

rsa2008 -16.0% time and -12% bytes, as with one thread: the lazily created structures and the compare-and-set publication show no contention. synthetic:100: no claim.
