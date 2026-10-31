# JMH ledger

Every recorded JMH run of this change, one row per benchmark and parameter set.
Scores are averages with the 99.9% error that JMH reports. Compare two rows only
when they come from runs on the same machine, JDK, and flags, back to back.

| Run | Label | Benchmark | Parameters | Score | Error | Unit | Alloc B/op | Alloc error | Forks x iterations |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 001 | 001-parse-baseline policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 75.24 | 2.933 | us/op | 1.144e+05 | 34.72 | 3 x 5 |
| 001 | 001-parse-baseline policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 60.41 | 1.411 | us/op | 1.014e+05 | 545.1 | 3 x 5 |
| 001 | 001-parse-baseline request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 78.68 | 1.98 | us/op | 1.119e+05 | 15.05 | 3 x 5 |
| 001 | 001-parse-baseline request | RequestParseBenchmark.parse | corpus=rsa2008, format=json | 10.41 | 0.409 | us/op | 4.829e+04 | 12.51 | 3 x 5 |
| 001 | 001-parse-baseline request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 47.35 | 1.391 | us/op | 9.665e+04 | 23.23 | 3 x 5 |
| 001 | 001-parse-baseline request | RequestParseBenchmark.parse | corpus=synthetic:100, format=json | 4.14 | 0.1153 | us/op | 2.78e+04 | 0.0008085 | 3 x 5 |
| 002 | 002-capture-off policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 72.6 | 2.557 | us/op | 1.143e+05 | 84.9 | 3 x 5 |
| 002 | 002-capture-off policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 59.14 | 1.744 | us/op | 1.013e+05 | 553.1 | 3 x 5 |
| 002 | 002-capture-off request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 75.99 | 2.878 | us/op | 1.112e+05 | 37.52 | 3 x 5 |
| 002 | 002-capture-off request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.52 | 1.098 | us/op | 9.63e+04 | 72.76 | 3 x 5 |
| 003 | 003-capture-xlocator policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 81.09 | 15.75 | us/op | 1.151e+05 | 40.68 | 3 x 5 |
| 003 | 003-capture-xlocator policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 60.7 | 1.261 | us/op | 1.022e+05 | 646.8 | 3 x 5 |
| 003 | 003-capture-xlocator request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 77.95 | 1.544 | us/op | 1.125e+05 | 63.24 | 3 x 5 |
| 003 | 003-capture-xlocator request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.71 | 1.224 | us/op | 9.648e+04 | 576.4 | 3 x 5 |
| 004 | 004-capture-sidetable policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 77.49 | 3.537 | us/op | 1.151e+05 | 57.44 | 3 x 5 |
| 004 | 004-capture-sidetable policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 64.18 | 3.075 | us/op | 1.026e+05 | 62.48 | 3 x 5 |
| 004 | 004-capture-sidetable request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 82.12 | 3.984 | us/op | 1.12e+05 | 56.12 | 3 x 5 |
| 004 | 004-capture-sidetable request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 49.58 | 3.924 | us/op | 9.734e+04 | 48.13 | 3 x 5 |
| 005 | 005-capture-woodstox policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 22.7 | 0.6274 | us/op | 3.391e+04 | 28.15 | 3 x 5 |
| 005 | 005-capture-woodstox policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 14.16 | 0.7481 | us/op | 2.24e+04 | 36.25 | 3 x 5 |
| 005 | 005-capture-woodstox request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 23.03 | 1.008 | us/op | 3.511e+04 | 308.2 | 3 x 5 |
| 005 | 005-capture-woodstox request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 6.052 | 0.5368 | us/op | 1.333e+04 | 54.57 | 3 x 5 |
| 006 | 006-woodstox-nocapture policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 21.74 | 0.7183 | us/op | 3.303e+04 | 16.21 | 3 x 5 |
| 006 | 006-woodstox-nocapture policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 13.49 | 0.3273 | us/op | 2.155e+04 | 21.85 | 3 x 5 |
| 006 | 006-woodstox-nocapture request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 22.2 | 2.555 | us/op | 3.428e+04 | 299.5 | 3 x 5 |
| 006 | 006-woodstox-nocapture request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 5.802 | 0.2014 | us/op | 1.254e+04 | 40.24 | 3 x 5 |
| 007 | 007-capture-off-s4 policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 71.32 | 3.039 | us/op | 1.144e+05 | 41.43 | 3 x 5 |
| 007 | 007-capture-off-s4 policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 58.7 | 1.869 | us/op | 1.016e+05 | 25.19 | 3 x 5 |
| 007 | 007-capture-off-s4 request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 79.79 | 12.6 | us/op | 1.112e+05 | 16.79 | 3 x 5 |
| 007 | 007-capture-off-s4 request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.43 | 1.356 | us/op | 9.64e+04 | 184.6 | 3 x 5 |
| 008 | 008-jdk-sax-factory-reuse policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 52.48 | 1.824 | us/op | 5.294e+04 | 101.7 | 3 x 5 |
| 008 | 008-jdk-sax-factory-reuse policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 38.9 | 0.9763 | us/op | 4.033e+04 | 27.07 | 3 x 5 |
| 008 | 008-jdk-sax-factory-reuse request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 57.4 | 2.436 | us/op | 5.014e+04 | 245 | 3 x 5 |
| 008 | 008-jdk-sax-factory-reuse request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 27 | 0.792 | us/op | 3.518e+04 | 85.22 | 3 x 5 |
| 009 | 009-jdk-sax-parser-per-thread policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 44.02 | 2.851 | us/op | 2.554e+04 | 67.04 | 3 x 5 |
| 009 | 009-jdk-sax-parser-per-thread policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 30.73 | 1.489 | us/op | 1.6e+04 | 55.16 | 3 x 5 |
| 009 | 009-jdk-sax-parser-per-thread request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 48.46 | 1.689 | us/op | 2.429e+04 | 170.4 | 3 x 5 |
| 009 | 009-jdk-sax-parser-per-thread request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 16.28 | 0.8266 | us/op | 9331 | 73.83 | 3 x 5 |
| 010 | 010-jdk-stax-factory-reuse policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 47.36 | 1.428 | us/op | 4.609e+04 | 91.77 | 3 x 5 |
| 010 | 010-jdk-stax-factory-reuse policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 34.29 | 1.092 | us/op | 3.355e+04 | 81.41 | 3 x 5 |
| 010 | 010-jdk-stax-factory-reuse request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 51.49 | 1.145 | us/op | 4.558e+04 | 45.92 | 3 x 5 |
| 010 | 010-jdk-stax-factory-reuse request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 19.4 | 0.4684 | us/op | 2.568e+04 | 181.9 | 3 x 5 |
| 011 | 011-woodstox-nocapture-s4 policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 21.92 | 0.6619 | us/op | 3.303e+04 | 20.26 | 3 x 5 |
| 011 | 011-woodstox-nocapture-s4 policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 13.72 | 0.3753 | us/op | 2.16e+04 | 32.13 | 3 x 5 |
| 011 | 011-woodstox-nocapture-s4 request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 21.65 | 0.7402 | us/op | 3.432e+04 | 150.2 | 3 x 5 |
| 011 | 011-woodstox-nocapture-s4 request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 5.81 | 0.2304 | us/op | 1.247e+04 | 48.13 | 3 x 5 |
| 012 | 012-woodstox-unmarshaller-per-thread policy | PolicyParseBenchmark.parse | corpus=rsa2008 | 21.38 | 0.5301 | us/op | 3.228e+04 | 8.626 | 3 x 5 |
| 012 | 012-woodstox-unmarshaller-per-thread policy | PolicyParseBenchmark.parse | corpus=synthetic:100 | 13.19 | 0.3681 | us/op | 2.078e+04 | 22.41 | 3 x 5 |
| 012 | 012-woodstox-unmarshaller-per-thread request | RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 21.54 | 0.4262 | us/op | 3.361e+04 | 161.8 | 3 x 5 |
| 012 | 012-woodstox-unmarshaller-per-thread request | RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 5.292 | 0.1269 | us/op | 1.185e+04 | 12.52 | 3 x 5 |
