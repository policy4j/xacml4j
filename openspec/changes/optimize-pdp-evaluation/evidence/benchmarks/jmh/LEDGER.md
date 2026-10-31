# JMH ledger

Every recorded JMH run of this change, one row per benchmark and parameter set.
Scores are averages with the 99.9% error that JMH reports. Compare two rows only
when they come from runs on the same machine, JDK, and flags, back to back.

| Run | Label | Benchmark | Parameters | Score | Error | Unit | Alloc B/op | Alloc error | Forks x iterations |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 001 | group 1 before (JDK 21) | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 19.63 | 0.7188 | us/op | 4.536e+04 | 589.7 | 3 x 5 |
| 001 | group 1 before (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 45.57 | 2.078 | us/op | 1.826e+05 | 2722 | 3 x 5 |
| 002 | group 1 after (JDK 21) | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 20.38 | 1.573 | us/op | 4.579e+04 | 131 | 3 x 5 |
| 002 | group 1 after (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 43.87 | 1.441 | us/op | 1.809e+05 | 31.27 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 19.97 | 0.449 | us/op | 4.572e+04 | 100.8 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=snapshot | 19.89 | 0.9524 | us/op | 4.535e+04 | 695.2 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 44.92 | 2.053 | us/op | 1.809e+05 | 32.4 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=snapshot | 43.92 | 1.448 | us/op | 1.825e+05 | 2689 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLoadBenchmark.importXml | corpus=rsa2008, store=in-memory | 812.2 | 30.23 | us/op | 1.17e+06 | 2203 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLoadBenchmark.importXml | corpus=rsa2008, store=snapshot | 818.5 | 31.8 | us/op | 1.158e+06 | 1673 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLoadBenchmark.importXml | corpus=synthetic:100, store=in-memory | 7457 | 212.2 | us/op | 1.112e+07 | 2.987e+04 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLoadBenchmark.importXml | corpus=synthetic:100, store=snapshot | 7585 | 248.7 | us/op | 1.157e+07 | 3.042e+04 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLoadBenchmark.loadParsed | corpus=rsa2008, store=in-memory | 2.617 | 0.04592 | us/op | 2.369e+04 | 1.064 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLoadBenchmark.loadParsed | corpus=rsa2008, store=snapshot | 2.866 | 0.08243 | us/op | 1.151e+04 | 0.5721 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLoadBenchmark.loadParsed | corpus=synthetic:100, store=in-memory | 9.835 | 0.4915 | us/op | 6.871e+04 | 8.927 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLoadBenchmark.loadParsed | corpus=synthetic:100, store=snapshot | 203.6 | 5.825 | us/op | 5.12e+05 | 75.33 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLookupBenchmark.cursor | corpus=rsa2008, store=in-memory | 0.7733 | 0.02851 | ns/op | 0.0001447 | 0.0003083 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLookupBenchmark.cursor | corpus=rsa2008, store=snapshot | 0.7738 | 0.02706 | ns/op | 0.0001421 | 0.0003026 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLookupBenchmark.cursor | corpus=synthetic:100, store=in-memory | 0.7792 | 0.02742 | ns/op | 0.0002681 | 0.0005815 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLookupBenchmark.cursor | corpus=synthetic:100, store=snapshot | 0.7857 | 0.03087 | ns/op | 0.0002586 | 0.0005605 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLookupBenchmark.lookup | corpus=rsa2008, store=in-memory | 31.41 | 0.949 | ns/op | 164.2 | 0.01228 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLookupBenchmark.lookup | corpus=rsa2008, store=snapshot | 15.27 | 0.6118 | ns/op | 0.002798 | 0.005959 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLookupBenchmark.lookup | corpus=synthetic:100, store=in-memory | 39.94 | 1.294 | ns/op | 187.1 | 0.02942 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PolicyStoreLookupBenchmark.lookup | corpus=synthetic:100, store=snapshot | 18.19 | 0.5607 | ns/op | 0.006075 | 0.01316 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=first-permit, store=in-memory | 11.76 | 0.7564 | us/op | 3.551e+04 | 16.72 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=first-deny, store=in-memory | 6.855 | 0.2498 | us/op | 1.65e+04 | 30.07 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=middle-permit, store=in-memory | 11.66 | 0.31 | us/op | 3.538e+04 | 175.3 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=middle-deny, store=in-memory | 9.434 | 0.2595 | us/op | 2.692e+04 | 116.9 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=last-permit, store=in-memory | 11.66 | 0.4483 | us/op | 3.554e+04 | 44.22 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=last-deny, store=in-memory | 11.55 | 0.4506 | us/op | 3.547e+04 | 88.33 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=unknown-notapplicable, store=in-memory | 10.06 | 0.3179 | us/op | 3.073e+04 | 32.62 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 465 | 28.51 | us/op | 2.071e+06 | 7.379e+04 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 7.205 | 0.1214 | us/op | 1.672e+04 | 336.5 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 492.4 | 16.96 | us/op | 2.234e+06 | 7.845e+04 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 260.8 | 7.791 | us/op | 1.136e+06 | 8546 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 509.2 | 24.76 | us/op | 2.248e+06 | 1.48e+04 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 489.3 | 25.61 | us/op | 2.165e+06 | 9.857e+04 | 3 x 5 |
| harness-001 | reference: add-pdp-jmh-benchmarks 001-baseline (JDK 21) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 492 | 20.73 | us/op | 2.189e+06 | 2.225e+04 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 18.97 | 0.4285 | us/op | 4.512e+04 | 837.8 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=snapshot | 19.48 | 0.3965 | us/op | 4.594e+04 | 142.5 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 43.87 | 0.9004 | us/op | 1.762e+05 | 1446 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=snapshot | 43.66 | 0.7383 | us/op | 1.76e+05 | 26.52 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLoadBenchmark.importXml | corpus=rsa2008, store=in-memory | 777.2 | 24.29 | us/op | 1.121e+06 | 1715 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLoadBenchmark.importXml | corpus=rsa2008, store=snapshot | 767.1 | 44.04 | us/op | 1.108e+06 | 842.6 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLoadBenchmark.importXml | corpus=synthetic:100, store=in-memory | 6709 | 195.7 | us/op | 1.064e+07 | 4869 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLoadBenchmark.importXml | corpus=synthetic:100, store=snapshot | 6936 | 183.2 | us/op | 1.105e+07 | 6.936e+04 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLoadBenchmark.loadParsed | corpus=rsa2008, store=in-memory | 2.383 | 0.05143 | us/op | 2.353e+04 | 1.304 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLoadBenchmark.loadParsed | corpus=rsa2008, store=snapshot | 2.578 | 0.0692 | us/op | 1.135e+04 | 0.5082 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLoadBenchmark.loadParsed | corpus=synthetic:100, store=in-memory | 9.572 | 0.5824 | us/op | 6.855e+04 | 4.923 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLoadBenchmark.loadParsed | corpus=synthetic:100, store=snapshot | 176.1 | 5.738 | us/op | 5.118e+05 | 65.47 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLookupBenchmark.cursor | corpus=rsa2008, store=in-memory | 0.7806 | 0.02193 | ns/op | 0.0001448 | 0.0003085 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLookupBenchmark.cursor | corpus=rsa2008, store=snapshot | 0.7939 | 0.02434 | ns/op | 0.0001458 | 0.0003105 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLookupBenchmark.cursor | corpus=synthetic:100, store=in-memory | 0.7978 | 0.03255 | ns/op | 0.0002673 | 0.0005796 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLookupBenchmark.cursor | corpus=synthetic:100, store=snapshot | 0.7877 | 0.02739 | ns/op | 0.0002646 | 0.0005738 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLookupBenchmark.lookup | corpus=rsa2008, store=in-memory | 32.16 | 2.604 | ns/op | 175.4 | 17.57 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLookupBenchmark.lookup | corpus=rsa2008, store=snapshot | 15.54 | 1.023 | ns/op | 0.002892 | 0.006174 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLookupBenchmark.lookup | corpus=synthetic:100, store=in-memory | 37.94 | 1.173 | ns/op | 176.5 | 16.61 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PolicyStoreLookupBenchmark.lookup | corpus=synthetic:100, store=snapshot | 17.74 | 0.6257 | ns/op | 0.005961 | 0.01293 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=first-permit, store=in-memory | 11.16 | 0.7591 | us/op | 3.442e+04 | 1158 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=first-deny, store=in-memory | 6.334 | 0.2987 | us/op | 1.573e+04 | 566.6 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=middle-permit, store=in-memory | 11.24 | 0.2252 | us/op | 3.497e+04 | 471.2 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=middle-deny, store=in-memory | 9.395 | 0.2561 | us/op | 2.662e+04 | 162.7 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=last-permit, store=in-memory | 11.19 | 0.5247 | us/op | 3.496e+04 | 462.8 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=last-deny, store=in-memory | 11.28 | 0.6205 | us/op | 3.457e+04 | 1092 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:10, request=unknown-notapplicable, store=in-memory | 10.04 | 0.2092 | us/op | 3.064e+04 | 246.3 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 495.9 | 15.25 | us/op | 2.132e+06 | 1.135e+04 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 6.503 | 0.2049 | us/op | 1.582e+04 | 293.5 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 491.2 | 10.68 | us/op | 2.124e+06 | 8711 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 252.5 | 10.17 | us/op | 1.075e+06 | 5748 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 480 | 29.44 | us/op | 2.122e+06 | 1.841e+04 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 475.9 | 17.97 | us/op | 2.132e+06 | 2.097e+04 | 3 x 5 |
| harness-002 | reference: add-pdp-jmh-benchmarks 002-baseline-jdk25 (JDK 25, Log4j jar) | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 479.1 | 15.96 | us/op | 2.128e+06 | 1.14e+04 | 3 x 5 |
| 005 | 005-cycleA-before mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 13.71 | 0.4699 | us/op | 4.151e+04 | 1174 | 3 x 5 |
| 005 | 005-cycleA-before mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 35.72 | 0.5442 | us/op | 1.69e+05 | 2564 | 3 x 5 |
| 005 | 005-cycleA-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 358 | 10.47 | us/op | 1.668e+06 | 6902 | 3 x 5 |
| 005 | 005-cycleA-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 457.1 | 18.39 | us/op | 2.123e+06 | 2.621e+04 | 3 x 5 |
| 005 | 005-cycleA-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 2.644 | 0.05717 | us/op | 1.217e+04 | 458.6 | 3 x 5 |
| 005 | 005-cycleA-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 456.3 | 12.97 | us/op | 2.105e+06 | 8621 | 3 x 5 |
| 005 | 005-cycleA-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 232.4 | 5.18 | us/op | 1.062e+06 | 8476 | 3 x 5 |
| 005 | 005-cycleA-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 453.2 | 14.62 | us/op | 2.118e+06 | 4818 | 3 x 5 |
| 005 | 005-cycleA-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 456.7 | 8.554 | us/op | 2.11e+06 | 8706 | 3 x 5 |
| 005 | 005-cycleA-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 466.5 | 14.82 | us/op | 2.052e+06 | 6.722e+04 | 3 x 5 |
| 006 | 006-cycleA-after mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 12.47 | 0.3946 | us/op | 3.49e+04 | 65.64 | 3 x 5 |
| 006 | 006-cycleA-after mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 16.43 | 0.7716 | us/op | 4.603e+04 | 4044 | 3 x 5 |
| 006 | 006-cycleA-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 152.8 | 3.864 | us/op | 3.868e+05 | 211.9 | 3 x 5 |
| 006 | 006-cycleA-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 190.6 | 2.719 | us/op | 5.327e+05 | 3.34e+04 | 3 x 5 |
| 006 | 006-cycleA-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 2.337 | 0.1148 | us/op | 1.076e+04 | 471.6 | 3 x 5 |
| 006 | 006-cycleA-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 199.4 | 20.14 | us/op | 5.113e+05 | 3.345e+04 | 3 x 5 |
| 006 | 006-cycleA-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 96.69 | 4.282 | us/op | 2.611e+05 | 1.671e+04 | 3 x 5 |
| 006 | 006-cycleA-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 188.2 | 7.255 | us/op | 5.113e+05 | 3.342e+04 | 3 x 5 |
| 006 | 006-cycleA-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 183.3 | 3.212 | us/op | 5.113e+05 | 3.342e+04 | 3 x 5 |
| 006 | 006-cycleA-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 184.5 | 8.474 | us/op | 5.083e+05 | 1.81e+04 | 3 x 5 |
| 007 | 007-group1-t4-before t4 | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 13.91 | 0.3287 | us/op | 3.487e+04 | 39.31 | 3 x 5 |
| 007 | 007-group1-t4-before t4 | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 23.41 | 3.494 | us/op | 4.673e+04 | 3692 | 3 x 5 |
| 008 | 008-group1-t4-after t4 | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 13.56 | 0.2432 | us/op | 3.487e+04 | 54.87 | 3 x 5 |
| 008 | 008-group1-t4-after t4 | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 28.46 | 1.962 | us/op | 4.832e+04 | 6163 | 3 x 5 |
| 009 | 009-cycleB-after mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 10.58 | 0.2648 | us/op | 2.441e+04 | 14.56 | 3 x 5 |
| 009 | 009-cycleB-after mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 15.55 | 0.6438 | us/op | 4.529e+04 | 4634 | 3 x 5 |
| 009 | 009-cycleB-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 149.5 | 3.391 | us/op | 4.065e+05 | 2.291e+04 | 3 x 5 |
| 009 | 009-cycleB-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 185.2 | 5.901 | us/op | 4.865e+05 | 259.1 | 3 x 5 |
| 009 | 009-cycleB-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.664 | 0.02651 | us/op | 6729 | 183.3 | 3 x 5 |
| 009 | 009-cycleB-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 190.5 | 9.476 | us/op | 5.373e+05 | 2.092e+04 | 3 x 5 |
| 009 | 009-cycleB-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 94.35 | 4.526 | us/op | 2.618e+05 | 1.463e+04 | 3 x 5 |
| 009 | 009-cycleB-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 184.2 | 5.593 | us/op | 5.079e+05 | 3.342e+04 | 3 x 5 |
| 009 | 009-cycleB-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 187.5 | 3.838 | us/op | 4.865e+05 | 260.6 | 3 x 5 |
| 009 | 009-cycleB-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 187.3 | 6.478 | us/op | 4.844e+05 | 263.4 | 3 x 5 |
| 010 | 010-cycleD-before mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 10.6 | 0.2886 | us/op | 2.434e+04 | 34.68 | 3 x 5 |
| 010 | 010-cycleD-before mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 15.69 | 0.4929 | us/op | 3.953e+04 | 8.614 | 3 x 5 |
| 010 | 010-cycleD-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 143.6 | 3.699 | us/op | 4.003e+05 | 2.615e+04 | 3 x 5 |
| 010 | 010-cycleD-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 184.2 | 7.058 | us/op | 5.292e+05 | 3.331e+04 | 3 x 5 |
| 010 | 010-cycleD-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.66 | 0.04138 | us/op | 6729 | 126.7 | 3 x 5 |
| 010 | 010-cycleD-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 189.3 | 7.31 | us/op | 5.293e+05 | 3.339e+04 | 3 x 5 |
| 010 | 010-cycleD-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 95.16 | 4.227 | us/op | 2.578e+05 | 1.676e+04 | 3 x 5 |
| 010 | 010-cycleD-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 191.3 | 5.535 | us/op | 5.079e+05 | 3.344e+04 | 3 x 5 |
| 010 | 010-cycleD-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 184.1 | 4.902 | us/op | 4.865e+05 | 266.4 | 3 x 5 |
| 010 | 010-cycleD-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 188.9 | 8.595 | us/op | 4.924e+05 | 1.253e+04 | 3 x 5 |
| 011 | 011-cycleD-after mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 10.65 | 0.167 | us/op | 2.319e+04 | 76.24 | 3 x 5 |
| 011 | 011-cycleD-after mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 13.34 | 0.5421 | us/op | 3.372e+04 | 71.98 | 3 x 5 |
| 011 | 011-cycleD-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 126.5 | 6.495 | us/op | 2.89e+05 | 9864 | 3 x 5 |
| 011 | 011-cycleD-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 163.5 | 9.169 | us/op | 3.662e+05 | 1.253e+04 | 3 x 5 |
| 011 | 011-cycleD-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.531 | 0.04273 | us/op | 6371 | 110.5 | 3 x 5 |
| 011 | 011-cycleD-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 156 | 3.573 | us/op | 3.582e+05 | 170.8 | 3 x 5 |
| 011 | 011-cycleD-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 81.27 | 3.519 | us/op | 1.866e+05 | 6242 | 3 x 5 |
| 011 | 011-cycleD-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 155.7 | 5.064 | us/op | 3.582e+05 | 167.2 | 3 x 5 |
| 011 | 011-cycleD-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 160.6 | 5.668 | us/op | 3.582e+05 | 171.6 | 3 x 5 |
| 011 | 011-cycleD-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 154.5 | 4.674 | us/op | 3.564e+05 | 166.9 | 3 x 5 |
| 012 | 012-cycleE-after mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.945 | 0.3476 | us/op | 2.028e+04 | 57.82 | 3 x 5 |
| 012 | 012-cycleE-after mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 12.63 | 0.6575 | us/op | 3.219e+04 | 1062 | 3 x 5 |
| 012 | 012-cycleE-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 121.5 | 3.823 | us/op | 2.805e+05 | 172.6 | 3 x 5 |
| 012 | 012-cycleE-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 160.7 | 6.691 | us/op | 3.64e+05 | 1.252e+04 | 3 x 5 |
| 012 | 012-cycleE-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.072 | 0.04138 | us/op | 4190 | 18.23 | 3 x 5 |
| 012 | 012-cycleE-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 154.6 | 5.476 | us/op | 3.561e+05 | 205 | 3 x 5 |
| 012 | 012-cycleE-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 78.98 | 2.51 | us/op | 1.805e+05 | 86.47 | 3 x 5 |
| 012 | 012-cycleE-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 163.4 | 7.147 | us/op | 3.722e+05 | 1.253e+04 | 3 x 5 |
| 012 | 012-cycleE-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 158.1 | 13.43 | us/op | 3.64e+05 | 1.265e+04 | 3 x 5 |
| 012 | 012-cycleE-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 155 | 22.68 | us/op | 3.623e+05 | 1.253e+04 | 3 x 5 |
| 013 | 013-cycleF-after mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.667 | 0.2826 | us/op | 2.019e+04 | 47.26 | 3 x 5 |
| 013 | 013-cycleF-after mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 11.53 | 0.6782 | us/op | 2.84e+04 | 1015 | 3 x 5 |
| 013 | 013-cycleF-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 109.7 | 4.674 | us/op | 2.428e+05 | 149.3 | 3 x 5 |
| 013 | 013-cycleF-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 141.5 | 3.307 | us/op | 3.161e+05 | 1.258e+04 | 3 x 5 |
| 013 | 013-cycleF-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.056 | 0.04277 | us/op | 4150 | 29.21 | 3 x 5 |
| 013 | 013-cycleF-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 144.8 | 9.577 | us/op | 3.16e+05 | 1.247e+04 | 3 x 5 |
| 013 | 013-cycleF-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 69.61 | 1.818 | us/op | 1.565e+05 | 79.73 | 3 x 5 |
| 013 | 013-cycleF-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 140.7 | 6.287 | us/op | 3.082e+05 | 136.5 | 3 x 5 |
| 013 | 013-cycleF-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 145.7 | 6.749 | us/op | 3.16e+05 | 1.247e+04 | 3 x 5 |
| 013 | 013-cycleF-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 135.3 | 4.368 | us/op | 3.062e+05 | 176.1 | 3 x 5 |
| 014 | 014-cycleE-t4-before t4 | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 11.28 | 0.1821 | us/op | 2.329e+04 | 8.82 | 3 x 5 |
| 014 | 014-cycleE-t4-before t4 | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 21.02 | 1.362 | us/op | 3.48e+04 | 1653 | 3 x 5 |
| 015 | 015-cycleE-t4-after t4 | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 9.477 | 0.2531 | us/op | 2.041e+04 | 103.5 | 3 x 5 |
| 015 | 015-cycleE-t4-after t4 | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 20.67 | 1.36 | us/op | 3.152e+04 | 100.2 | 3 x 5 |
| 017 | 017-review-fixes-before mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.771 | 0.3223 | us/op | 2.012e+04 | 19.16 | 3 x 5 |
| 017 | 017-review-fixes-before mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 11.65 | 0.3436 | us/op | 2.846e+04 | 1026 | 3 x 5 |
| 017 | 017-review-fixes-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 112.2 | 3.807 | us/op | 2.428e+05 | 117.9 | 3 x 5 |
| 017 | 017-review-fixes-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.076 | 0.02567 | us/op | 4211 | 123.6 | 3 x 5 |
| 017 | 017-review-fixes-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 140.1 | 4.31 | us/op | 3.081e+05 | 183.1 | 3 x 5 |
| 018 | 018-review-fixes-after mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.835 | 0.3147 | us/op | 2.027e+04 | 50.03 | 3 x 5 |
| 018 | 018-review-fixes-after mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 11.65 | 0.3174 | us/op | 2.799e+04 | 94.7 | 3 x 5 |
| 018 | 018-review-fixes-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 109.5 | 3.58 | us/op | 2.431e+05 | 116.2 | 3 x 5 |
| 018 | 018-review-fixes-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.118 | 0.03798 | us/op | 4361 | 12.58 | 3 x 5 |
| 018 | 018-review-fixes-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 142.7 | 3.577 | us/op | 3.083e+05 | 183.2 | 3 x 5 |
| 019 | 019-cycleH-before mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.91 | 0.2954 | us/op | 2.024e+04 | 85.62 | 3 x 5 |
| 019 | 019-cycleH-before mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 11.29 | 0.3763 | us/op | 2.8e+04 | 87.47 | 3 x 5 |
| 019 | 019-cycleH-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 109.9 | 5.685 | us/op | 2.431e+05 | 107.1 | 3 x 5 |
| 019 | 019-cycleH-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 142 | 7.551 | us/op | 3.164e+05 | 1.252e+04 | 3 x 5 |
| 019 | 019-cycleH-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.093 | 0.02015 | us/op | 4387 | 54.29 | 3 x 5 |
| 019 | 019-cycleH-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 141.1 | 4.964 | us/op | 3.084e+05 | 135.3 | 3 x 5 |
| 019 | 019-cycleH-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 68.9 | 2.72 | us/op | 1.567e+05 | 66.53 | 3 x 5 |
| 019 | 019-cycleH-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 143.9 | 8.376 | us/op | 3.164e+05 | 1.252e+04 | 3 x 5 |
| 019 | 019-cycleH-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 139.5 | 6.318 | us/op | 3.084e+05 | 138 | 3 x 5 |
| 019 | 019-cycleH-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 139.3 | 7.939 | us/op | 3.143e+05 | 1.258e+04 | 3 x 5 |
| 020 | 020-cycleH-after mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.93 | 0.2898 | us/op | 1.877e+04 | 55.49 | 3 x 5 |
| 020 | 020-cycleH-after mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 10.64 | 0.5096 | us/op | 1.775e+04 | 937.5 | 3 x 5 |
| 020 | 020-cycleH-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 100.4 | 2.182 | us/op | 1.359e+05 | 68.7 | 3 x 5 |
| 020 | 020-cycleH-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 127.2 | 4.039 | us/op | 1.72e+05 | 142.3 | 3 x 5 |
| 020 | 020-cycleH-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.084 | 0.02425 | us/op | 4078 | 124.4 | 3 x 5 |
| 020 | 020-cycleH-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 132.8 | 4.286 | us/op | 1.721e+05 | 87.28 | 3 x 5 |
| 020 | 020-cycleH-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 67.5 | 3.776 | us/op | 8.825e+04 | 42.64 | 3 x 5 |
| 020 | 020-cycleH-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 128.7 | 4.347 | us/op | 1.721e+05 | 83.1 | 3 x 5 |
| 020 | 020-cycleH-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 129.7 | 4.467 | us/op | 1.721e+05 | 84.52 | 3 x 5 |
| 020 | 020-cycleH-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 127.7 | 7.263 | us/op | 1.703e+05 | 143 | 3 x 5 |
| 022 | 022-batch2-before mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 8.869 | 0.2851 | us/op | 1.873e+04 | 84.85 | 3 x 5 |
| 022 | 022-batch2-before mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 10.59 | 0.1473 | us/op | 1.71e+04 | 17.9 | 3 x 5 |
| 022 | 022-batch2-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 102.7 | 4.446 | us/op | 1.359e+05 | 65.42 | 3 x 5 |
| 022 | 022-batch2-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 130.6 | 4.507 | us/op | 1.8e+05 | 1.258e+04 | 3 x 5 |
| 022 | 022-batch2-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 1.083 | 0.01225 | us/op | 3952 | 12.55 | 3 x 5 |
| 022 | 022-batch2-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 131.4 | 2.141 | us/op | 1.72e+05 | 136.1 | 3 x 5 |
| 022 | 022-batch2-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 67.18 | 3.999 | us/op | 8.825e+04 | 43.01 | 3 x 5 |
| 022 | 022-batch2-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 131.4 | 4.716 | us/op | 1.719e+05 | 132.8 | 3 x 5 |
| 022 | 022-batch2-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 134.8 | 4.5 | us/op | 1.801e+05 | 1.249e+04 | 3 x 5 |
| 022 | 022-batch2-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 125.4 | 2.067 | us/op | 1.703e+05 | 142.9 | 3 x 5 |
| 023 | 023-batch2-after mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 6.968 | 0.1159 | us/op | 1.293e+04 | 37.56 | 3 x 5 |
| 023 | 023-batch2-after mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 7.678 | 0.1764 | us/op | 7357 | 0.3439 | 3 x 5 |
| 023 | 023-batch2-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 76.18 | 6.691 | us/op | 4.704e+04 | 60.54 | 3 x 5 |
| 023 | 023-batch2-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 107.3 | 4.98 | us/op | 5.912e+04 | 71.58 | 3 x 5 |
| 023 | 023-batch2-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 0.9438 | 0.03923 | us/op | 2904 | 0.3426 | 3 x 5 |
| 023 | 023-batch2-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 106.9 | 3.909 | us/op | 5.916e+04 | 71.35 | 3 x 5 |
| 023 | 023-batch2-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 53.1 | 1.278 | us/op | 3.114e+04 | 19.03 | 3 x 5 |
| 023 | 023-batch2-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 98.76 | 10.26 | us/op | 5.912e+04 | 66.28 | 3 x 5 |
| 023 | 023-batch2-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 115 | 17.46 | us/op | 5.909e+04 | 40.98 | 3 x 5 |
| 023 | 023-batch2-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 104.7 | 6.004 | us/op | 5.804e+04 | 131.2 | 3 x 5 |
| 024 | 024-batch3-before mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 7.117 | 0.237 | us/op | 1.296e+04 | 0.7002 | 3 x 5 |
| 024 | 024-batch3-before mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 7.54 | 0.2473 | us/op | 7366 | 14.31 | 3 x 5 |
| 024 | 024-batch3-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 82.72 | 3.308 | us/op | 4.7e+04 | 62.86 | 3 x 5 |
| 024 | 024-batch3-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 103.2 | 13.14 | us/op | 5.901e+04 | 122.8 | 3 x 5 |
| 024 | 024-batch3-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 0.9757 | 0.06482 | us/op | 2904 | 0.3537 | 3 x 5 |
| 024 | 024-batch3-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 110.2 | 9.536 | us/op | 5.909e+04 | 42.87 | 3 x 5 |
| 024 | 024-batch3-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 50.29 | 6.814 | us/op | 3.114e+04 | 18.13 | 3 x 5 |
| 024 | 024-batch3-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 99.63 | 7.755 | us/op | 5.909e+04 | 38.34 | 3 x 5 |
| 024 | 024-batch3-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 102.4 | 5.494 | us/op | 5.909e+04 | 39.4 | 3 x 5 |
| 024 | 024-batch3-before scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 100 | 4.571 | us/op | 5.812e+04 | 124 | 3 x 5 |
| 025 | 025-batch3-after mix | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 4.983 | 0.1946 | us/op | 1.002e+04 | 49.09 | 3 x 5 |
| 025 | 025-batch3-after mix | PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 6.312 | 0.149 | us/op | 7055 | 54.25 | 3 x 5 |
| 025 | 025-batch3-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=all, store=in-memory | 65.79 | 1.51 | us/op | 4.667e+04 | 59.25 | 3 x 5 |
| 025 | 025-batch3-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 84.01 | 3.957 | us/op | 5.867e+04 | 121.2 | 3 x 5 |
| 025 | 025-batch3-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 0.8916 | 0.01626 | us/op | 2568 | 0.3171 | 3 x 5 |
| 025 | 025-batch3-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 84.09 | 2.911 | us/op | 5.867e+04 | 120.6 | 3 x 5 |
| 025 | 025-batch3-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 41.95 | 2.166 | us/op | 3.08e+04 | 15.11 | 3 x 5 |
| 025 | 025-batch3-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 85.45 | 4.684 | us/op | 5.875e+04 | 31.82 | 3 x 5 |
| 025 | 025-batch3-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 83.53 | 1.717 | us/op | 5.867e+04 | 120.6 | 3 x 5 |
| 025 | 025-batch3-after scan | PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 91.45 | 3.145 | us/op | 5.779e+04 | 109.7 | 3 x 5 |
| 027 | 027-pair-d47 D47 eligibility off | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory, targetIndex=false | 1.577 | 0.09887 | us/op | 751.7 | 0.9858 | 3 x 5 |
| 027 | 027-pair-d47 D47 eligibility off | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory, targetIndex=true | 1.635 | 0.07002 | us/op | 782.2 | 0.05862 | 3 x 5 |
| 027 | 027-pair-d47 D47 eligibility off | PdpDecideBenchmark.decide | corpus=rules:100, request=multi-valued, store=in-memory, targetIndex=false | 9.109 | 0.8477 | us/op | 744.9 | 1.759 | 3 x 5 |
| 027 | 027-pair-d47 D47 eligibility off | PdpDecideBenchmark.decide | corpus=rules:100, request=multi-valued, store=in-memory, targetIndex=true | 2.966 | 0.1929 | us/op | 864.3 | 0.5859 | 3 x 5 |
| 027 | 027-pair-d47 D47 eligibility off | PdpDecideBenchmark.decide | corpus=rules:100, request=responses, store=in-memory, targetIndex=false | 4.916 | 0.3755 | us/op | 1104 | 0.9384 | 3 x 5 |
| 027 | 027-pair-d47 D47 eligibility off | PdpDecideBenchmark.decide | corpus=rules:100, request=responses, store=in-memory, targetIndex=true | 0.9562 | 0.05285 | us/op | 1160 | 25.04 | 3 x 5 |
| 027 | 027-pair-d47 D47 eligibility off | PdpDecideBenchmark.decide | corpus=rules:100, request=narrow, store=in-memory, targetIndex=false | 0.3571 | 0.0149 | us/op | 288 | 0.06801 | 3 x 5 |
| 027 | 027-pair-d47 D47 eligibility off | PdpDecideBenchmark.decide | corpus=rules:100, request=narrow, store=in-memory, targetIndex=true | 0.2685 | 0.06571 | us/op | 288 | 0.04598 | 3 x 5 |
| 027 | 027-pair-d47 D47 on | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory, targetIndex=false | 1.619 | 0.05346 | us/op | 717.2 | 0.05965 | 3 x 5 |
| 027 | 027-pair-d47 D47 on | PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory, targetIndex=true | 1.589 | 0.04535 | us/op | 748.3 | 0.05584 | 3 x 5 |
| 027 | 027-pair-d47 D47 on | PdpDecideBenchmark.decide | corpus=rules:100, request=multi-valued, store=in-memory, targetIndex=false | 9.323 | 0.7605 | us/op | 744.8 | 1.697 | 3 x 5 |
| 027 | 027-pair-d47 D47 on | PdpDecideBenchmark.decide | corpus=rules:100, request=multi-valued, store=in-memory, targetIndex=true | 3.018 | 0.3267 | us/op | 864.3 | 0.6248 | 3 x 5 |
| 027 | 027-pair-d47 D47 on | PdpDecideBenchmark.decide | corpus=rules:100, request=responses, store=in-memory, targetIndex=false | 5.155 | 0.2612 | us/op | 1064 | 37.56 | 3 x 5 |
| 027 | 027-pair-d47 D47 on | PdpDecideBenchmark.decide | corpus=rules:100, request=responses, store=in-memory, targetIndex=true | 0.8833 | 0.02582 | us/op | 1112 | 0.1608 | 3 x 5 |
| 027 | 027-pair-d47 D47 on | PdpDecideBenchmark.decide | corpus=rules:100, request=narrow, store=in-memory, targetIndex=false | 0.3721 | 0.01315 | us/op | 256 | 0.06958 | 3 x 5 |
| 027 | 027-pair-d47 D47 on | PdpDecideBenchmark.decide | corpus=rules:100, request=narrow, store=in-memory, targetIndex=true | 0.2368 | 0.00783 | us/op | 256 | 0.0428 | 3 x 5 |
