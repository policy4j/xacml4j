# 012: parse prototype, as 011, plus a JAXB `Unmarshaller` per thread and unmarshaller object (task 2.2, D9 cycle 1)

Session 4, 2026-10-27 09:54 to 10:16 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 11 on 12 cores from other processes. Jars from frozen tree copies (sha256 in `env.txt`). A difference is claimed only when the 99.9% intervals are disjoint.

## Against capture off of this session (007)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 71.32 ± 3 | 21.38 ± 0.53 | us/op | -70.0% | disjoint | 1.1436e+05 | 32279 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 58.7 ± 1.9 | 13.19 ± 0.37 | us/op | -77.5% | disjoint | 1.0165e+05 | 20777 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 79.79 ± 13 | 21.54 ± 0.43 | us/op | -73.0% | disjoint | 1.1122e+05 | 33612 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.43 ± 1.4 | 5.292 ± 0.13 | us/op | -88.6% | disjoint | 96398 | 11848 |

The StAX variants differ from today in 16 DTD cases of `BaseJAXBUnmarshallerSecurityTest` (DOCTYPE rejected, as design D6 plans); the other 237 xacml-xml tests pass.

## Reading of session 4 (runs 007 to 012)

Most of today's parse cost is setup per parse: one factory per JVM alone saves 26% to 42% of the time and 54% to 64% of the bytes (008); a reset parser per thread saves 38% to 65% of the time and 78% to 90% of the bytes (009), the fewest bytes of every variant (rsa2008 policy 25.5 KB). Woodstox is the fastest (011: -69% to -88% time; rsa2008 policy 21.9 us against 44.0 us for 009) with more bytes than 009 (33 KB). An `Unmarshaller` per thread adds at most 9% on top (012). Position capture on top of any of them costs about 4% to 8% (runs 003 to 005). These are the inputs of the D3 and D9 decisions.
