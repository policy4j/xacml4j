# 006: cycle A after (pair with 005)

Session 2, 2026-10-27 07:45 to 08:08 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average at start 7.6 on 12 cores (other processes), the same for both runs of a pair, which run back to back. Commands: `command.txt` of the session script, below. A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the tree with cycle A (design D5), before cycle B.

## Mix

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 13.71 ± 0.47 | 12.47 ± 0.39 | us/op | -9.1% | disjoint | 41507 | 34896 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 35.72 ± 0.54 | 16.43 ± 0.77 | us/op | -54.0% | disjoint | 1.6901e+05 | 46026 |

## Scan (synthetic:1000, design D12)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 358 ± 10 | 152.8 ± 3.9 | us/op | -57.3% | disjoint | 1.6684e+06 | 3.8684e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 2.644 ± 0.057 | 2.337 ± 0.11 | us/op | -11.6% | disjoint | 12169 | 10764 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 457.1 ± 18 | 190.6 ± 2.7 | us/op | -58.3% | disjoint | 2.1233e+06 | 5.3271e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 456.7 ± 8.6 | 183.3 ± 3.2 | us/op | -59.9% | disjoint | 2.1101e+06 | 5.1132e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 453.2 ± 15 | 188.2 ± 7.3 | us/op | -58.5% | disjoint | 2.118e+06 | 5.1134e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 232.4 ± 5.2 | 96.69 ± 4.3 | us/op | -58.4% | disjoint | 1.0622e+06 | 2.6107e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 456.3 ± 13 | 199.4 ± 20 | us/op | -56.3% | disjoint | 2.1045e+06 | 5.1133e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 466.5 ± 15 | 184.5 ± 8.5 | us/op | -60.5% | disjoint | 2.0518e+06 | 5.0827e+05 |

## Result

Cycle A gains on every set (disjoint intervals): rsa2008 -9.1% time and -16% bytes; synthetic:100 -54% time and -73% bytes; a full scan of synthetic:1000 (first-permit) 457 to 191 us and 2.12 MB to 0.53 MB. The cost per non-matching policy falls from about 0.45 us and 2.1 KB to about 0.19 us and 0.52 KB. The fixed cost (first-deny) falls 11.6% in time and 12% in bytes.
