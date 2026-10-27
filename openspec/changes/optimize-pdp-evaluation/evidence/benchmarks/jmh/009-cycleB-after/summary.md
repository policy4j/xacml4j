# 009: cycle B after (pair with 006)

Session 2, 2026-10-27 07:45 to 08:08 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average at start 7.6 on 12 cores (other processes), the same for both runs of a pair, which run back to back. Commands: `command.txt` of the session script, below. A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the tree with cycles A and B (design D6). Before: run 006 (the cycle A jar), run directly before in the same session.

## Mix

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 12.47 ± 0.39 | 10.58 ± 0.26 | us/op | -15.2% | disjoint | 34896 | 24411 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 16.43 ± 0.77 | 15.55 ± 0.64 | us/op | -5.3% | overlap: no claim | 46026 | 45290 |

## Scan

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 152.8 ± 3.9 | 149.5 ± 3.4 | us/op | -2.1% | overlap: no claim | 3.8684e+05 | 4.0651e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 2.337 ± 0.11 | 1.664 ± 0.027 | us/op | -28.8% | disjoint | 10764 | 6729 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 190.6 ± 2.7 | 185.2 ± 5.9 | us/op | -2.8% | overlap: no claim | 5.3271e+05 | 4.8654e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 183.3 ± 3.2 | 187.5 ± 3.8 | us/op | +2.3% | overlap: no claim | 5.1132e+05 | 4.8655e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 188.2 ± 7.3 | 184.2 ± 5.6 | us/op | -2.1% | overlap: no claim | 5.1134e+05 | 5.0792e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 96.69 ± 4.3 | 94.35 ± 4.5 | us/op | -2.4% | overlap: no claim | 2.6107e+05 | 2.6182e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 199.4 ± 20 | 190.5 ± 9.5 | us/op | -4.5% | overlap: no claim | 5.1133e+05 | 5.3728e+05 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 184.5 ± 8.5 | 187.3 ± 6.5 | us/op | +1.5% | overlap: no claim | 5.0827e+05 | 4.8441e+05 |

## Result

Cycle B gains where request attributes are looked up: rsa2008 -15.2% time and -30% bytes, the fixed cost (first-deny) -28.8% time and -37% bytes (10.8 KB to 6.7 KB). The full scans show no claim: after cycle A a non-matching policy no longer looks the attribute up again.
