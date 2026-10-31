| Benchmark | Parameters | Score ± error | Unit | Alloc B/op ± error | gc.count | gc.time ms | Fork means | Samples |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=in-memory | 18.97 ± 0.43 | us/op | 45115 ± 8.4e+02 | 28 | 71 | 18.58 to 19.39 | 15 |
| PdpDecideBenchmark.decide | corpus=rsa2008, request=all, store=snapshot | 19.48 ± 0.4 | us/op | 45944 ± 1.4e+02 | 27 | 66 | 19.17 to 19.82 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=in-memory | 43.87 ± 0.9 | us/op | 1.7621e+05 ± 1.4e+03 | 47 | 51 | 43.47 to 44.24 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:100, request=all, store=snapshot | 43.66 ± 0.74 | us/op | 1.7603e+05 ± 27 | 47 | 50 | 43.11 to 44.04 | 15 |
| PolicyStoreLoadBenchmark.importXml | corpus=rsa2008, store=in-memory | 777.2 ± 24 | us/op | 1.1205e+06 ± 1.7e+03 | 36 | 376 | 768.7 to 791.2 | 15 |
| PolicyStoreLoadBenchmark.importXml | corpus=rsa2008, store=snapshot | 767.1 ± 44 | us/op | 1.1077e+06 ± 8.4e+02 | 35 | 364 | 742.4 to 794.3 | 15 |
| PolicyStoreLoadBenchmark.importXml | corpus=synthetic:100, store=in-memory | 6709 ± 2e+02 | us/op | 1.0641e+07 ± 4.9e+03 | 38 | 384 | 6595 to 6870 | 15 |
| PolicyStoreLoadBenchmark.importXml | corpus=synthetic:100, store=snapshot | 6936 ± 1.8e+02 | us/op | 1.105e+07 ± 6.9e+04 | 41 | 410 | 6822 to 7105 | 15 |
| PolicyStoreLoadBenchmark.loadParsed | corpus=rsa2008, store=in-memory | 2.383 ± 0.051 | us/op | 23527 ± 1.3 | 244 | 2.77e+03 | 2.363 to 2.399 | 15 |
| PolicyStoreLoadBenchmark.loadParsed | corpus=rsa2008, store=snapshot | 2.578 ± 0.069 | us/op | 11352 ± 0.51 | 103 | 97 | 2.547 to 2.616 | 15 |
| PolicyStoreLoadBenchmark.loadParsed | corpus=synthetic:100, store=in-memory | 9.572 ± 0.58 | us/op | 68553 ± 4.9 | 172 | 809 | 9.407 to 9.814 | 15 |
| PolicyStoreLoadBenchmark.loadParsed | corpus=synthetic:100, store=snapshot | 176.1 ± 5.7 | us/op | 5.1184e+05 ± 65 | 68 | 65 | 172.3 to 181.3 | 15 |
| PolicyStoreLookupBenchmark.cursor | corpus=rsa2008, store=in-memory | 0.7806 ± 0.022 | ns/op | 0.00014475 ± 0.00031 | 0 | not reported | 0.7687 to 0.7995 | 15 |
| PolicyStoreLookupBenchmark.cursor | corpus=rsa2008, store=snapshot | 0.7939 ± 0.024 | ns/op | 0.00014576 ± 0.00031 | 0 | not reported | 0.7701 to 0.8207 | 15 |
| PolicyStoreLookupBenchmark.cursor | corpus=synthetic:100, store=in-memory | 0.7978 ± 0.033 | ns/op | 0.00026734 ± 0.00058 | 0 | not reported | 0.7785 to 0.8167 | 15 |
| PolicyStoreLookupBenchmark.cursor | corpus=synthetic:100, store=snapshot | 0.7877 ± 0.027 | ns/op | 0.00026457 ± 0.00057 | 0 | not reported | 0.7673 to 0.8205 | 15 |
| PolicyStoreLookupBenchmark.lookup | corpus=rsa2008, store=in-memory | 32.16 ± 2.6 | ns/op | 175.44 ± 18 | 64 | 61 | 29.56 to 34.99 | 15 |
| PolicyStoreLookupBenchmark.lookup | corpus=rsa2008, store=snapshot | 15.54 ± 1 | ns/op | 0.0028925 ± 0.0062 | 0 | not reported | 14.8 to 16.8 | 15 |
| PolicyStoreLookupBenchmark.lookup | corpus=synthetic:100, store=in-memory | 37.94 ± 1.2 | ns/op | 176.46 ± 17 | 54 | 52 | 36.69 to 39 | 15 |
| PolicyStoreLookupBenchmark.lookup | corpus=synthetic:100, store=snapshot | 17.74 ± 0.63 | ns/op | 0.0059607 ± 0.013 | 0 | not reported | 17.04 to 18.33 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=first-permit, store=in-memory | 11.16 ± 0.76 | us/op | 34422 ± 1.2e+03 | 36 | 59 | 10.28 to 11.64 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=first-deny, store=in-memory | 6.334 ± 0.3 | us/op | 15726 ± 5.7e+02 | 30 | 62 | 5.985 to 6.567 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=middle-permit, store=in-memory | 11.24 ± 0.23 | us/op | 34969 ± 4.7e+02 | 37 | 59 | 11.03 to 11.42 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=middle-deny, store=in-memory | 9.395 ± 0.26 | us/op | 26624 ± 1.6e+02 | 34 | 61 | 9.164 to 9.595 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=last-permit, store=in-memory | 11.19 ± 0.52 | us/op | 34955 ± 4.6e+02 | 37 | 59 | 10.74 to 11.73 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=last-deny, store=in-memory | 11.28 ± 0.62 | us/op | 34571 ± 1.1e+03 | 36 | 58 | 10.59 to 11.63 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:10, request=unknown-notapplicable, store=in-memory | 10.04 ± 0.21 | us/op | 30638 ± 2.5e+02 | 34 | 52 | 9.782 to 10.19 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-permit, store=in-memory | 495.9 ± 15 | us/op | 2.1325e+06 ± 1.1e+04 | 50 | 50 | 484.8 to 509.3 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=first-deny, store=in-memory | 6.503 ± 0.2 | us/op | 15816 ± 2.9e+02 | 29 | 74 | 6.419 to 6.591 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-permit, store=in-memory | 491.2 ± 11 | us/op | 2.1244e+06 ± 8.7e+03 | 51 | 50 | 483 to 499.5 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=middle-deny, store=in-memory | 252.5 ± 10 | us/op | 1.0754e+06 ± 5.7e+03 | 50 | 50 | 245.5 to 265 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-permit, store=in-memory | 480 ± 29 | us/op | 2.1217e+06 ± 1.8e+04 | 52 | 53 | 450.1 to 510.8 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=last-deny, store=in-memory | 475.9 ± 18 | us/op | 2.1324e+06 ± 2.1e+04 | 53 | 53 | 465 to 486.8 | 15 |
| PdpDecideBenchmark.decide | corpus=synthetic:1000, request=unknown-notapplicable, store=in-memory | 479.1 ± 16 | us/op | 2.1276e+06 ± 1.1e+04 | 52 | 52 | 468.2 to 488.3 | 15 |
