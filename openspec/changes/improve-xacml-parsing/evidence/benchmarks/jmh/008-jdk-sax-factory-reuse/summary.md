# 008: parse prototype, one hardened `SAXParserFactory` per JVM, a new `SAXParser` per parse (task 2.2, D9 cycle 1)

Session 4, 2026-10-27 09:54 to 10:16 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 11 on 12 cores from other processes. Jars from frozen tree copies (sha256 in `env.txt`). A difference is claimed only when the 99.9% intervals are disjoint.

## Against capture off of this session (007)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 71.32 ± 3 | 52.48 ± 1.8 | us/op | -26.4% | disjoint | 1.1436e+05 | 52939 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 58.7 ± 1.9 | 38.9 ± 0.98 | us/op | -33.7% | disjoint | 1.0165e+05 | 40331 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 79.79 ± 13 | 57.4 ± 2.4 | us/op | -28.1% | disjoint | 1.1122e+05 | 50141 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.43 ± 1.4 | 27 ± 0.79 | us/op | -41.8% | disjoint | 96398 | 35176 |

All 253 xacml-xml tests pass on this variant.
