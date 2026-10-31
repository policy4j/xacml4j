# 020: cycle H after (pair with 019)

Session 5, 2026-10-27 10:26 to 10:38 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive. Jars from frozen tree copies that differ only in the five files of cycle H (sha256 in `env.txt`). A difference is claimed only when the 99.9% intervals are disjoint.

Jar: cycle H (design D14): the empty bag of a type is shared, and no empty bag is built when the attribute is present.

## Mix

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.91 ± 0.3 | 8.93 ± 0.29 | us/op | +0.2% | overlap: no claim | 20243 | 18774 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 11.29 ± 0.38 | 10.64 ± 0.51 | us/op | -5.7% | overlap: no claim | 27996 | 17748 |

## Scan

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 109.9 ± 5.7 | 100.4 ± 2.2 | us/op | -8.6% | disjoint | 2.4306e+05 | 1.359e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.093 ± 0.02 | 1.084 ± 0.024 | us/op | -0.8% | overlap: no claim | 4387.2 | 4077.7 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 142 ± 7.6 | 127.2 ± 4 | us/op | -10.4% | disjoint | 3.1639e+05 | 1.7202e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 139.5 ± 6.3 | 129.7 ± 4.5 | us/op | -7.0% | overlap: no claim | 3.0839e+05 | 1.721e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 143.9 ± 8.4 | 128.7 ± 4.3 | us/op | -10.5% | disjoint | 3.1639e+05 | 1.721e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 68.9 ± 2.7 | 67.5 ± 3.8 | us/op | -2.0% | overlap: no claim | 1.5667e+05 | 88252 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 141.1 ± 5 | 132.8 ± 4.3 | us/op | -5.9% | overlap: no claim | 3.0839e+05 | 1.721e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 139.3 ± 7.9 | 127.7 ± 7.3 | us/op | -8.3% | overlap: no claim | 3.1431e+05 | 1.7025e+05 |

## Result

Cycle H removes 44% to 46% of the bytes of every synthetic:1000 set that evaluates many policies (a full scan 316 KB to 172 KB), 37% of synthetic:100, and 7% of rsa2008; time falls 7% to 10.5% on the scans (disjoint on all, first-permit, and last-permit). The fixed cost (first-deny) does not change measurably in time; its bytes fall 7%.
