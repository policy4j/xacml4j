# 004: parse prototype, SAX side table (D3 candidate b) (task 2.2)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the prototype's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

The boolean filter keeps the SAX `Locator`; an `Unmarshaller.Listener` records target, line, and column per bound object in growable arrays. The 253 xacml-xml tests pass.

## Against capture off (002)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 72.6 ± 2.6 | 77.49 ± 3.5 | us/op | +6.7% | overlap: no claim | 1.1434e+05 | 1.1512e+05 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 59.14 ± 1.7 | 64.18 ± 3.1 | us/op | +8.5% | disjoint | 1.0132e+05 | 1.0264e+05 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 75.99 ± 2.9 | 82.12 ± 4 | us/op | +8.1% | overlap: no claim | 1.1123e+05 | 1.1201e+05 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.52 ± 1.1 | 49.58 ± 3.9 | us/op | +6.6% | overlap: no claim | 96302 | 97339 |
