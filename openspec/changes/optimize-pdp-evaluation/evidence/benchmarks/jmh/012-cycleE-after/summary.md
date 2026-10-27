# 012: cycle E after (pair with 011)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the cycle's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

Jar: cycles A, B, D, E (design D9: the fixed cost of a decision).

## mix

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 10.65 ± 0.17 | 8.945 ± 0.35 | us/op | -16.0% | disjoint | 23192 | 20282 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 13.34 ± 0.54 | 12.63 ± 0.66 | us/op | -5.4% | overlap: no claim | 33717 | 32185 |

## scan

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 126.5 ± 6.5 | 121.5 ± 3.8 | us/op | -4.0% | overlap: no claim | 2.8899e+05 | 2.8054e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.531 ± 0.043 | 1.072 ± 0.041 | us/op | -30.0% | disjoint | 6371.4 | 4189.9 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 163.5 ± 9.2 | 160.7 ± 6.7 | us/op | -1.7% | overlap: no claim | 3.6624e+05 | 3.6395e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 160.6 ± 5.7 | 158.1 ± 13 | us/op | -1.5% | overlap: no claim | 3.5824e+05 | 3.6403e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 155.7 ± 5.1 | 163.4 ± 7.1 | us/op | +4.9% | overlap: no claim | 3.5824e+05 | 3.7218e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 81.27 ± 3.5 | 78.98 ± 2.5 | us/op | -2.8% | overlap: no claim | 1.8664e+05 | 1.8049e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 156 ± 3.6 | 154.6 ± 5.5 | us/op | -0.8% | overlap: no claim | 3.5824e+05 | 3.561e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 154.5 ± 4.7 | 155 ± 23 | us/op | +0.3% | overlap: no claim | 3.5638e+05 | 3.6233e+05 |

## Result

Cycle E gains on the fixed cost: rsa2008 -16.0% time and -12.5% bytes; first-deny -30.0% time and -34% bytes (6.37 KB to 4.19 KB). Scans: no claim (the cost there is per policy). With 4 threads (runs 014 and 015) rsa2008 gains the same 16%.
