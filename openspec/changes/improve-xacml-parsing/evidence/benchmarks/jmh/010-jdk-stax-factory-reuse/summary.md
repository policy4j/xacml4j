# 010: parse prototype, byte input through the JDK `XMLInputFactory` (one per JVM, DTD support off), JAXB over StAX (task 2.2, D9 cycle 1)

Session 4, 2026-10-27 09:54 to 10:16 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 11 on 12 cores from other processes. Jars from frozen tree copies (sha256 in `env.txt`). A difference is claimed only when the 99.9% intervals are disjoint.

## Against capture off of this session (007)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 71.32 ± 3 | 47.36 ± 1.4 | us/op | -33.6% | disjoint | 1.1436e+05 | 46094 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 58.7 ± 1.9 | 34.29 ± 1.1 | us/op | -41.6% | disjoint | 1.0165e+05 | 33549 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 79.79 ± 13 | 51.49 ± 1.1 | us/op | -35.5% | disjoint | 1.1122e+05 | 45584 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.43 ± 1.4 | 19.4 ± 0.47 | us/op | -58.2% | disjoint | 96398 | 25680 |

The StAX variants differ from today in 16 DTD cases of `BaseJAXBUnmarshallerSecurityTest` (DOCTYPE rejected, as design D6 plans); the other 237 xacml-xml tests pass.
