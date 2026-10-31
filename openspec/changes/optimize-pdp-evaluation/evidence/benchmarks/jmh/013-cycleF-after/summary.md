# 013: cycle F after (pair with 012)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the cycle's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

Jar: cycles A, B, D, E, F (design D10: no variable map for a policy without variables; reference caches as concurrent maps).

## mix

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.945 ± 0.35 | 8.667 ± 0.28 | us/op | -3.1% | overlap: no claim | 20282 | 20187 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 12.63 ± 0.66 | 11.53 ± 0.68 | us/op | -8.7% | overlap: no claim | 32185 | 28398 |

## scan

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 121.5 ± 3.8 | 109.7 ± 4.7 | us/op | -9.7% | disjoint | 2.8054e+05 | 2.4277e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.072 ± 0.041 | 1.056 ± 0.043 | us/op | -1.5% | overlap: no claim | 4189.9 | 4149.8 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 160.7 ± 6.7 | 141.5 ± 3.3 | us/op | -12.0% | disjoint | 3.6395e+05 | 3.1609e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 158.1 ± 13 | 145.7 ± 6.7 | us/op | -7.8% | overlap: no claim | 3.6403e+05 | 3.1601e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 163.4 ± 7.1 | 140.7 ± 6.3 | us/op | -13.9% | disjoint | 3.7218e+05 | 3.0816e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 78.98 ± 2.5 | 69.61 ± 1.8 | us/op | -11.9% | disjoint | 1.8049e+05 | 1.5647e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 154.6 ± 5.5 | 144.8 ± 9.6 | us/op | -6.3% | overlap: no claim | 3.561e+05 | 3.1601e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 155 ± 23 | 135.3 ± 4.4 | us/op | -12.7% | overlap: no claim | 3.6233e+05 | 3.0616e+05 |

## Result

Cycle F gains per policy: synthetic:1000 all -9.7%, first-permit -12.0%, last-permit -13.9%, middle-deny -11.9% (disjoint); the other scan sets point the same way with overlapping intervals; bytes -13% on every scan set (a full scan 364 KB to 316 KB, one 48-byte map fewer per policy context). rsa2008 and first-deny: no claim.
