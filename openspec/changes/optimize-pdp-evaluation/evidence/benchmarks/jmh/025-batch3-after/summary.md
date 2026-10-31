# 025: batch 3 pair, after (D20 (d), D21 with the interner SPI, D22, D23)

Pair with 024 (before: the batch-2 jar of run 023), back to back on 2026-10-27, the flags of
runs 022/023 (Oracle GraalVM 25.0.4, C2, 2 GB G1, 3 forks x 5 iterations, -prof gc); no lane
building; jars sha256 in env.txt. Load averages at start 10.6 and 12.5 (endpoint security
agents). A difference is claimed only when the 99.9% intervals are disjoint.

Credited to batch 3 (D12): every row but middle-deny is disjoint. rsa2008 -30.0% time,
-22.6% bytes (D23 unary bindings and method-handle calls, D20 (d), D22); synthetic:100
-16.3%; synthetic:1000 full scan -20.5% time with bytes unchanged (D21 canonical keys: CPU
only); per-request scans -8.6% to -23.7% time.

| Parameters | Before | After | Time | Alloc before | Alloc after | Alloc | Disjoint 99.9% |
| --- | --- | --- | --- | --- | --- | --- | --- |
| corpus=rsa2008, request=all, store=in-memory | 7.117 ± 0.237 | 4.983 ± 0.1946 | -30.0% | 1.296e+04 | 1.002e+04 | -22.6% | yes |
| corpus=synthetic:100, request=all, store=in-memory | 7.54 ± 0.2473 | 6.312 ± 0.149 | -16.3% | 7366 | 7055 | -4.2% | yes |
| corpus=synthetic:1000, request=all, store=in-memory | 82.72 ± 3.308 | 65.79 ± 1.51 | -20.5% | 4.7e+04 | 4.667e+04 | -0.7% | yes |
| corpus=synthetic:1000, request=first-permit, store=in-memory | 103.2 ± 13.14 | 84.01 ± 3.957 | -18.6% | 5.901e+04 | 5.867e+04 | -0.6% | yes |
| corpus=synthetic:1000, request=first-deny, store=in-memory | 0.9757 ± 0.06482 | 0.8916 ± 0.01626 | -8.6% | 2904 | 2568 | -11.6% | yes |
| corpus=synthetic:1000, request=middle-permit, store=in-memory | 110.2 ± 9.536 | 84.09 ± 2.911 | -23.7% | 5.909e+04 | 5.867e+04 | -0.7% | yes |
| corpus=synthetic:1000, request=middle-deny, store=in-memory | 50.29 ± 6.814 | 41.95 ± 2.166 | -16.6% | 3.114e+04 | 3.08e+04 | -1.1% | no |
| corpus=synthetic:1000, request=last-permit, store=in-memory | 99.63 ± 7.755 | 85.45 ± 4.684 | -14.2% | 5.909e+04 | 5.875e+04 | -0.6% | yes |
| corpus=synthetic:1000, request=last-deny, store=in-memory | 102.4 ± 5.494 | 83.53 ± 1.717 | -18.4% | 5.909e+04 | 5.867e+04 | -0.7% | yes |
| corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 100 ± 4.571 | 91.45 ± 3.145 | -8.6% | 5.812e+04 | 5.779e+04 | -0.6% | yes |
