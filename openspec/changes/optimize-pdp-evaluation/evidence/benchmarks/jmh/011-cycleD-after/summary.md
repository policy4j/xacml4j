# 011: cycle D after (pair with 010)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the cycle's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

Jar: cycles A, B, D (design D8: two-argument predicate calls).

## mix

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 10.6 ± 0.29 | 10.65 ± 0.17 | us/op | +0.5% | overlap: no claim | 24345 | 23192 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 15.69 ± 0.49 | 13.34 ± 0.54 | us/op | -14.9% | disjoint | 39526 | 33717 |

## scan

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 143.6 ± 3.7 | 126.5 ± 6.5 | us/op | -12.0% | disjoint | 4.0026e+05 | 2.8899e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.66 ± 0.041 | 1.531 ± 0.043 | us/op | -7.8% | disjoint | 6729.1 | 6371.4 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 184.2 ± 7.1 | 163.5 ± 9.2 | us/op | -11.3% | disjoint | 5.2918e+05 | 3.6624e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 184.1 ± 4.9 | 160.6 ± 5.7 | us/op | -12.8% | disjoint | 4.8648e+05 | 3.5824e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 191.3 ± 5.5 | 155.7 ± 5.1 | us/op | -18.6% | disjoint | 5.079e+05 | 3.5824e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 95.16 ± 4.2 | 81.27 ± 3.5 | us/op | -14.6% | disjoint | 2.5782e+05 | 1.8664e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 189.3 ± 7.3 | 156 ± 3.6 | us/op | -17.6% | disjoint | 5.2929e+05 | 3.5824e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 188.9 ± 8.6 | 154.5 ± 4.7 | us/op | -18.2% | disjoint | 4.9243e+05 | 3.5638e+05 |

## Result

Cycle D gains wherever many predicates run: synthetic:100 -14.9%; every synthetic:1000 set -7.8% to -18.6% in time (all disjoint) and about -28% in bytes (a full scan 529 KB to 366 KB). rsa2008: no claim (-5% bytes).
