# 009: parse prototype, one factory per JVM and a `SAXParser` per thread, `reset()` before each parse (task 2.2, D9 cycle 1)

Session 4, 2026-10-27 09:54 to 10:16 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 11 on 12 cores from other processes. Jars from frozen tree copies (sha256 in `env.txt`). A difference is claimed only when the 99.9% intervals are disjoint.

## Against capture off of this session (007)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 71.32 ± 3 | 44.02 ± 2.9 | us/op | -38.3% | disjoint | 1.1436e+05 | 25536 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 58.7 ± 1.9 | 30.73 ± 1.5 | us/op | -47.6% | disjoint | 1.0165e+05 | 16000 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 79.79 ± 13 | 48.46 ± 1.7 | us/op | -39.3% | disjoint | 1.1122e+05 | 24287 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.43 ± 1.4 | 16.28 ± 0.83 | us/op | -64.9% | disjoint | 96398 | 9330.8 |

All 253 xacml-xml tests pass on this variant.
