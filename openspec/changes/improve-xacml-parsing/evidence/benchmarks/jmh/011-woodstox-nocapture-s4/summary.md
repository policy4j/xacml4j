# 011: parse prototype, Woodstox 7.1.1 without capture again (as run 006) (task 2.2, D9 cycle 1)

Session 4, 2026-10-27 09:54 to 10:16 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 11 on 12 cores from other processes. Jars from frozen tree copies (sha256 in `env.txt`). A difference is claimed only when the 99.9% intervals are disjoint.

## Against capture off of this session (007)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 71.32 ± 3 | 21.92 ± 0.66 | us/op | -69.3% | disjoint | 1.1436e+05 | 33034 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 58.7 ± 1.9 | 13.72 ± 0.38 | us/op | -76.6% | disjoint | 1.0165e+05 | 21597 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 79.79 ± 13 | 21.65 ± 0.74 | us/op | -72.9% | disjoint | 1.1122e+05 | 34319 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.43 ± 1.4 | 5.81 ± 0.23 | us/op | -87.5% | disjoint | 96398 | 12467 |

The StAX variants differ from today in 16 DTD cases of `BaseJAXBUnmarshallerSecurityTest` (DOCTYPE rejected, as design D6 plans); the other 237 xacml-xml tests pass.
