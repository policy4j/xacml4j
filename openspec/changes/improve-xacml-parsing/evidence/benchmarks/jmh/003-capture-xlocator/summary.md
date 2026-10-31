# 003: parse prototype, -Xlocator (D3 candidate a) (task 2.2)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the prototype's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

XJC `-Xlocator`: every generated class gets a `Locator` field that the RI fills with a `LocatorImpl` copy per bean; `jaxb-impl` at compile scope in `xacml-xml-jaxb` and `xacml-xml` (the generated classes implement `com.sun.xml.bind.Locatable`). The 253 xacml-xml tests pass.

## Against capture off (002)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 72.6 ± 2.6 | 81.09 ± 16 | us/op | +11.7% | overlap: no claim | 1.1434e+05 | 1.151e+05 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 59.14 ± 1.7 | 60.7 ± 1.3 | us/op | +2.6% | overlap: no claim | 1.0132e+05 | 1.022e+05 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 75.99 ± 2.9 | 77.95 ± 1.5 | us/op | +2.6% | overlap: no claim | 1.1123e+05 | 1.1249e+05 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.52 ± 1.1 | 46.71 ± 1.2 | us/op | +0.4% | overlap: no claim | 96302 | 96478 |
