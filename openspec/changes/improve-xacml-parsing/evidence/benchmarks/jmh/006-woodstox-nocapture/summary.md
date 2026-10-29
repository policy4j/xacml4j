# 006: parse prototype, JAXB over Woodstox without capture (task 2.2)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the prototype's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

As 005 without the listener: the parser's own cost. Same 16 DTD differences.

## Against capture off (002)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 72.6 ± 2.6 | 21.74 ± 0.72 | us/op | -70.1% | disjoint | 1.1434e+05 | 33030 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 59.14 ± 1.7 | 13.49 ± 0.33 | us/op | -77.2% | disjoint | 1.0132e+05 | 21553 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 75.99 ± 2.9 | 22.2 ± 2.6 | us/op | -70.8% | disjoint | 1.1123e+05 | 34276 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.52 ± 1.1 | 5.802 ± 0.2 | us/op | -87.5% | disjoint | 96302 | 12536 |
