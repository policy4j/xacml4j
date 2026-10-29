# 001: parse baseline (task 2.3)

Session 2, 2026-10-27 07:45 to 08:08 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average at start 7.6 on 12 cores (other processes), Commands: `command.txt` of the session script, below. A difference is claimed only when the 99.9% intervals are disjoint.

Jar: the tree of `optimize-pdp-evaluation` run 009 (cycles A and B; the parse path is unchanged by them). Default parameters: both corpora; requests as XACML 3.0 XML and as JSON (`RequestParseBenchmark` reads JSON through a `Reader`).

| Benchmark | Parameters | Score ± error | Unit | Alloc B/op ± error | gc.count | gc.time ms | Fork means | Samples |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 75.24 ± 2.9 | us/op | 1.144e+05 ± 35 | 19 | 233 | 75.12 to 75.4 | 15 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 60.41 ± 1.4 | us/op | 1.0138e+05 ± 5.5e+02 | 21 | 240 | 59.42 to 60.99 | 15 |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 78.68 ± 2 | us/op | 1.119e+05 ± 15 | 18 | 199 | 77.51 to 80.37 | 15 |
| RequestParseBenchmark.parse | corpus=rsa2008, format=json | 10.41 ± 0.41 | us/op | 48289 ± 13 | 55 | 53 | 10.2 to 10.66 | 15 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 47.35 ± 1.4 | us/op | 96651 ± 23 | 25 | 221 | 45.98 to 48.07 | 15 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=json | 4.14 ± 0.12 | us/op | 27800 ± 0.00081 | 80 | 81 | 4.022 to 4.228 | 15 |

## Reading

A XACML 3.0 XML request costs 7.6 times the time and 2.3 times the bytes of the same request as JSON (rsa2008: 78.7 us and 112 KB against 10.4 us and 48 KB). A policy of the rsa2008 corpus costs 75 us and 114 KB. These are the before values of the D9 parse cycles.
