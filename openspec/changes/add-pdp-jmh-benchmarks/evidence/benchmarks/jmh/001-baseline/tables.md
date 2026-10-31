| Benchmark | Parameters | Score ± error | Unit | Alloc B/op ± error | gc.count | gc.time ms | Fork means | Samples |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 19.97 ± 0.45 | us/op | 45720 ± 1e+02 | 27 | 64 | 19.92 to 20 | 15 |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=snapshot | 19.89 ± 0.95 | us/op | 45355 ± 7e+02 | 28 | 66 | 19.17 to 20.87 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 44.92 ± 2.1 | us/op | 1.8087e+05 ± 32 | 48 | 110 | 42.83 to 46.87 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=snapshot | 43.92 ± 1.4 | us/op | 1.8255e+05 ± 2.7e+03 | 49 | 114 | 42.3 to 45.32 | 15 |
| PolicyStoreLoadBenchmark.importXml | corpus=rsa2008, store=in-memory | 812.2 ± 30 | us/op | 1.1697e+06 ± 2.2e+03 | 36 | 315 | 797 to 834.3 | 15 |
| PolicyStoreLoadBenchmark.importXml | corpus=rsa2008, store=snapshot | 818.5 ± 32 | us/op | 1.1577e+06 ± 1.7e+03 | 35 | 304 | 800.8 to 836.5 | 15 |
| PolicyStoreLoadBenchmark.importXml | corpus=synthetic:100, store=in-memory | 7457 ± 2.1e+02 | us/op | 1.1124e+07 ± 3e+04 | 36 | 366 | 7329 to 7525 | 15 |
| PolicyStoreLoadBenchmark.importXml | corpus=synthetic:100, store=snapshot | 7585 ± 2.5e+02 | us/op | 1.1574e+07 ± 3e+04 | 38 | 340 | 7501 to 7660 | 15 |
| PolicyStoreLoadBenchmark.loadParsed | corpus=rsa2008, store=in-memory | 2.617 ± 0.046 | us/op | 23685 ± 1.1 | 222 | 4.39e+03 | 2.6 to 2.625 | 15 |
| PolicyStoreLoadBenchmark.loadParsed | corpus=rsa2008, store=snapshot | 2.866 ± 0.082 | us/op | 11512 ± 0.57 | 95 | 233 | 2.84 to 2.895 | 15 |
| PolicyStoreLoadBenchmark.loadParsed | corpus=synthetic:100, store=in-memory | 9.835 ± 0.49 | us/op | 68706 ± 8.9 | 166 | 1.23e+03 | 9.537 to 10.23 | 15 |
| PolicyStoreLoadBenchmark.loadParsed | corpus=synthetic:100, store=snapshot | 203.6 ± 5.8 | us/op | 5.1201e+05 ± 75 | 59 | 136 | 200.3 to 206.2 | 15 |
| PolicyStoreLookupBenchmark.cursor | corpus=rsa2008, store=in-memory | 0.7733 ± 0.029 | ns/op | 0.00014466 ± 0.00031 | 0 | not reported | 0.7577 to 0.8003 | 15 |
| PolicyStoreLookupBenchmark.cursor | corpus=rsa2008, store=snapshot | 0.7738 ± 0.027 | ns/op | 0.00014212 ± 0.0003 | 0 | not reported | 0.7484 to 0.7925 | 15 |
| PolicyStoreLookupBenchmark.cursor | corpus=synthetic:100, store=in-memory | 0.7792 ± 0.027 | ns/op | 0.00026811 ± 0.00058 | 0 | not reported | 0.7604 to 0.8134 | 15 |
| PolicyStoreLookupBenchmark.cursor | corpus=synthetic:100, store=snapshot | 0.7857 ± 0.031 | ns/op | 0.00025857 ± 0.00056 | 0 | not reported | 0.7618 to 0.8014 | 15 |
| PolicyStoreLookupBenchmark.lookup | corpus=rsa2008, store=in-memory | 31.41 ± 0.95 | ns/op | 164.22 ± 0.012 | 62 | 147 | 30.84 to 32.08 | 15 |
| PolicyStoreLookupBenchmark.lookup | corpus=rsa2008, store=snapshot | 15.27 ± 0.61 | ns/op | 0.0027983 ± 0.006 | 0 | not reported | 14.93 to 15.93 | 15 |
| PolicyStoreLookupBenchmark.lookup | corpus=synthetic:100, store=in-memory | 39.94 ± 1.3 | ns/op | 187.08 ± 0.029 | 55 | 129 | 38.35 to 40.8 | 15 |
| PolicyStoreLookupBenchmark.lookup | corpus=synthetic:100, store=snapshot | 18.19 ± 0.56 | ns/op | 0.0060747 ± 0.013 | 0 | not reported | 17.78 to 18.52 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=first-permit, store=in-memory | 11.76 ± 0.76 | us/op | 35510 ± 17 | 35 | 85 | 11.6 to 11.87 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=first-deny, store=in-memory | 6.855 ± 0.25 | us/op | 16499 ± 30 | 27 | 64 | 6.589 to 7.08 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=middle-permit, store=in-memory | 11.66 ± 0.31 | us/op | 35377 ± 1.8e+02 | 36 | 97 | 11.46 to 11.94 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=middle-deny, store=in-memory | 9.434 ± 0.26 | us/op | 26915 ± 1.2e+02 | 33 | 77 | 9.289 to 9.537 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=last-permit, store=in-memory | 11.66 ± 0.45 | us/op | 35542 ± 44 | 37 | 87 | 11.2 to 11.98 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=last-deny, store=in-memory | 11.55 ± 0.45 | us/op | 35467 ± 88 | 35 | 92 | 11.01 to 11.86 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=unknown-notapplicable, store=in-memory | 10.06 ± 0.32 | us/op | 30734 ± 33 | 37 | 86 | 9.948 to 10.28 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 465 ± 29 | us/op | 2.0709e+06 ± 7.4e+04 | 53 | 123 | 438.5 to 497.2 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 7.205 ± 0.12 | us/op | 16725 ± 3.4e+02 | 27 | 69 | 7.074 to 7.29 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 492.4 ± 17 | us/op | 2.2342e+06 ± 7.8e+04 | 54 | 125 | 477.9 to 509.4 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 260.8 ± 7.8 | us/op | 1.1361e+06 ± 8.5e+03 | 51 | 119 | 255.4 to 269 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 509.2 ± 25 | us/op | 2.2477e+06 ± 1.5e+04 | 52 | 121 | 483.9 to 523.3 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 489.3 ± 26 | us/op | 2.1646e+06 ± 9.9e+04 | 53 | 123 | 473.3 to 520.2 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 492 ± 21 | us/op | 2.1894e+06 ± 2.2e+04 | 52 | 121 | 471.9 to 511.6 | 15 |
