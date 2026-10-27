# 023: batch 2 pair, after (cycles I, J, K, L, M, N, C)

Pair with 022 (before: commit 21858796), back to back on 2026-10-27, Oracle GraalVM 25.0.4
with C2 (-XX:-UseJVMCICompiler), -Xms2g -Xmx2g -XX:+UseG1GC, 3 forks x 5 iterations,
-prof gc; no lane building. Jars from frozen copies: before = commit HEAD, after = the main
tree with D21 and D23 reversed (batch 2 only); sha256 in env.txt. A difference is claimed
only when the 99.9% intervals are disjoint. Load averages at start 10.3 and 12.2 (endpoint
security agents, the same during both runs).

Credited to batch 2 (design D12 as changed): every row but last-deny is disjoint.
rsa2008 -21.4% time, -31.0% bytes; synthetic:100 -27.5%, -57.0%; synthetic:1000 full scan
-25.9%, -65.4%; the per-request scans -13% to -25% time, -65% to -67% bytes.

| Parameters | Before | After | Time | Alloc before | Alloc after | Alloc | Disjoint 99.9% |
| --- | --- | --- | --- | --- | --- | --- | --- |
| corpus=rsa2008, request=all, store=in-memory | 8.869 ± 0.2851 | 6.968 ± 0.1159 | -21.4% | 1.873e+04 | 1.293e+04 | -31.0% | yes |
| corpus=synthetic:100, request=all, store=in-memory | 10.59 ± 0.1473 | 7.678 ± 0.1764 | -27.5% | 1.71e+04 | 7357 | -57.0% | yes |
| corpus=synthetic:1000, request=all, store=in-memory | 102.7 ± 4.446 | 76.18 ± 6.691 | -25.9% | 1.359e+05 | 4.704e+04 | -65.4% | yes |
| corpus=synthetic:1000, request=first-permit, store=in-memory | 130.6 ± 4.507 | 107.3 ± 4.98 | -17.8% | 1.8e+05 | 5.912e+04 | -67.2% | yes |
| corpus=synthetic:1000, request=first-deny, store=in-memory | 1.083 ± 0.01225 | 0.9438 ± 0.03923 | -12.9% | 3952 | 2904 | -26.5% | yes |
| corpus=synthetic:1000, request=middle-permit, store=in-memory | 131.4 ± 2.141 | 106.9 ± 3.909 | -18.6% | 1.72e+05 | 5.916e+04 | -65.6% | yes |
| corpus=synthetic:1000, request=middle-deny, store=in-memory | 67.18 ± 3.999 | 53.1 ± 1.278 | -21.0% | 8.825e+04 | 3.114e+04 | -64.7% | yes |
| corpus=synthetic:1000, request=last-permit, store=in-memory | 131.4 ± 4.716 | 98.76 ± 10.26 | -24.8% | 1.719e+05 | 5.912e+04 | -65.6% | yes |
| corpus=synthetic:1000, request=last-deny, store=in-memory | 134.8 ± 4.5 | 115 ± 17.46 | -14.7% | 1.801e+05 | 5.909e+04 | -67.2% | no |
| corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 125.4 ± 2.067 | 104.7 ± 6.004 | -16.5% | 1.703e+05 | 5.804e+04 | -65.9% | yes |
