# 008: group 1 with -t 4, after (pair with 007, task 1.4)

Session 2, 2026-10-27 07:45 to 08:08 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average at start 7.6 on 12 cores (other processes), the same for both runs of a pair, which run back to back. Commands: `command.txt` of the session script, below. A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the tree with group 1 (cycle A included as in 007). Four threads share one PDP.

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 13.91 ± 0.33 | 13.56 ± 0.24 | us/op | -2.5% | overlap: no claim | 34869 | 34866 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 23.41 ± 3.5 | 28.46 ± 2 | us/op | +21.6% | overlap: no claim | 46735 | 48322 |

## Result

No claim: both intervals overlap, and bytes per operation are unchanged. With four threads the synthetic:100 score varies more (errors of 2 to 3.5 us); no contention on the removed random sources is visible. Group 1 keeps its purpose (determinism) without a measurable loss.
