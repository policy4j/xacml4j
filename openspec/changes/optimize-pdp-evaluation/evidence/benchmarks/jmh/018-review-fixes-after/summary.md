# 018: review fixes, after (pair with 017)

Session 4, 2026-10-27 09:54 to 10:16 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 11 on 12 cores from other processes. Jars from frozen tree copies (sha256 in `env.txt`). A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the final tree with the fixes of reviews rv006 to rv012.

## Mix

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.771 ± 0.32 | 8.835 ± 0.31 | us/op | +0.7% | overlap: no claim | 20117 | 20269 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 11.65 ± 0.34 | 11.65 ± 0.32 | us/op | +0.1% | overlap: no claim | 28457 | 27991 |

## Scan

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 112.2 ± 3.8 | 109.5 ± 3.6 | us/op | -2.4% | overlap: no claim | 2.4282e+05 | 2.4309e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.076 ± 0.026 | 1.118 ± 0.038 | us/op | +4.0% | overlap: no claim | 4211.2 | 4360.5 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 140.1 ± 4.3 | 142.7 ± 3.6 | us/op | +1.9% | overlap: no claim | 3.0809e+05 | 3.0832e+05 |

## Result

No claim on any set: the review fixes cost no measurable time. First-deny bytes rise from 4.21 KB to 4.36 KB, the maps that the result now creates when it reads the advice, obligation, and resolved-attribute views (design D9). The gains of cycles A to F stand for the final tree.
