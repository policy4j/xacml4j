# 002: parse prototype, capture off (the cycle F tree; the prototypes' base) (task 2.2)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the prototype's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

The JDK SAX path of `BaseJAXBUnmarshaller` as it is: a new `SAXParserFactory` and `SAXParser` per parse, a new JAXB `Unmarshaller` per parse.

| Benchmark | Parameters | Score ± error | Unit | Alloc B/op ± error | gc.count | gc.time ms | Fork means | Samples |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 72.6 ± 2.6 | us/op | 1.1434e+05 ± 85 | 20 | 221 | 70.63 to 74.89 | 15 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 59.14 ± 1.7 | us/op | 1.0132e+05 ± 5.5e+02 | 23 | 246 | 58.22 to 60.35 | 15 |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 75.99 ± 2.9 | us/op | 1.1123e+05 ± 38 | 20 | 229 | 74.29 to 76.94 | 15 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.52 ± 1.1 | us/op | 96302 ± 73 | 25 | 228 | 45.74 to 47.27 | 15 |

Against the baseline 001 (session 2, cycle B tree): policy rsa2008 75.2 to 72.6 us, no claim; the parse path did not change.
