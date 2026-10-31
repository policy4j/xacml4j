# 027-pair-d47

Proves D47's allocation saving and its latency against its gate (design D47, Gate).

Benchmark: `xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java#decide`, `store=in-memory`,
Oracle JDK 25.0.4, C2 (`-XX:-UseJVMCICompiler`), `-prof gc`, 3 forks x 5 iterations. Baseline: the same
tree with D47's eligibility off, so its 8 B per policy context is on both sides. Host load was 7 to 8 at
the start of each side, with other projects running, so read the times with care.

A difference counts only when the 99.9% error intervals do not overlap.

| Benchmark | Parameters | Before | After | Time | Alloc before B/op | Alloc after B/op | Alloc |
| --- | --- | ---: | ---: | --- | ---: | ---: | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, targetIndex=false | 1.577 ± 0.09887 us/op | 1.619 ± 0.05346 | +2.7% (no significant difference) | 751.7 | 717.2 | -4.6% |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, targetIndex=true | 1.635 ± 0.07002 us/op | 1.589 ± 0.04535 | -2.8% (no significant difference) | 782.2 | 748.3 | -4.3% |
| PdpDecideBenchmark.decide | corpus=rules:100, request=multi-valued, targetIndex=false | 9.109 ± 0.8477 us/op | 9.323 ± 0.7605 | +2.4% (no significant difference) | 744.9 | 744.8 | -0.0% (no significant difference) |
| PdpDecideBenchmark.decide | corpus=rules:100, request=multi-valued, targetIndex=true | 2.966 ± 0.1929 us/op | 3.018 ± 0.3267 | +1.8% (no significant difference) | 864.3 | 864.3 | +0.0% (no significant difference) |
| PdpDecideBenchmark.decide | corpus=rules:100, request=narrow, targetIndex=false | 0.3571 ± 0.0149 us/op | 0.3721 ± 0.01315 | +4.2% (no significant difference) | 288.0 | 256.0 | -11.1% |
| PdpDecideBenchmark.decide | corpus=rules:100, request=narrow, targetIndex=true | 0.2685 ± 0.06571 us/op | 0.2368 ± 0.00783 | -11.8% (no significant difference) | 288.0 | 256.0 | -11.1% |
| PdpDecideBenchmark.decide | corpus=rules:100, request=responses, targetIndex=false | 4.916 ± 0.3755 us/op | 5.155 ± 0.2612 | +4.9% (no significant difference) | 1,104.5 | 1,064.5 | -3.6% |
| PdpDecideBenchmark.decide | corpus=rules:100, request=responses, targetIndex=true | 0.9562 ± 0.05285 us/op | 0.8833 ± 0.02582 | -7.6% (no significant difference) | 1,160.1 | 1,112.1 | -4.1% |

Conclusion: D47 ships under its gate: an allocation saving on all but multi-valued, and no significant
time difference. The same rows are in `LEDGER.md`.
