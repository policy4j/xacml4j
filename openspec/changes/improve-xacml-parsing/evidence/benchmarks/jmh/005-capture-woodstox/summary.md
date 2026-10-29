# 005: parse prototype, JAXB over Woodstox with start offsets (D3 candidate c) (task 2.2)

Session 3, 2026-10-27 08:19 to 08:54 UTC: Oracle GraalVM 25.0.4 with C2 (`-XX:-UseJVMCICompiler`), `-Xms2g -Xmx2g -XX:+UseG1GC`, 3 forks x 5 iterations, `-prof gc`; no agent session of this project alive; load average 7 to 10 on 12 cores from other processes. Jars from frozen tree copies that differ only in the prototype's files (`env.txt` has each jar's sha256). A difference is claimed only when the 99.9% intervals are disjoint.

Byte and stream input go through a Woodstox 7.1.1 `XMLStreamReader2` from one static factory (namespace aware, DTD support off, external entities off); an `Unmarshaller.Listener` records the event-start character offset (a `long`) per bound object. Woodstox 7.1.1 is the newest version in the offline repository; DQ4 cites 7.3.0. 237 of 253 xacml-xml tests pass; the 16 others are the DTD cases of `BaseJAXBUnmarshallerSecurityTest` (internal-subset entities and the message of the external-entity rejection), which differ because the prototype rejects every DOCTYPE, as design D6 plans.

## Against capture off (002)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 72.6 ± 2.6 | 22.7 ± 0.63 | us/op | -68.7% | disjoint | 1.1434e+05 | 33908 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 59.14 ± 1.7 | 14.16 ± 0.75 | us/op | -76.1% | disjoint | 1.0132e+05 | 22399 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 75.99 ± 2.9 | 23.03 ± 1 | us/op | -69.7% | disjoint | 1.1123e+05 | 35113 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 46.52 ± 1.1 | 6.052 ± 0.54 | us/op | -87.0% | disjoint | 96302 | 13328 |

## Capture cost on Woodstox (006 -> 005)

| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| PolicyParseBenchmark.parse | corpus=rsa2008 | 21.74 ± 0.72 | 22.7 ± 0.63 | us/op | +4.4% | overlap: no claim | 33030 | 33908 |
| PolicyParseBenchmark.parse | corpus=synthetic:100 | 13.49 ± 0.33 | 14.16 ± 0.75 | us/op | +4.9% | overlap: no claim | 21553 | 22399 |
| Benchmark | Parameters | Before ± error | After ± error | Unit | Change | Intervals | Alloc before | Alloc after |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| RequestParseBenchmark.parse | corpus=rsa2008, format=xml30 | 22.2 ± 2.6 | 23.03 ± 1 | us/op | +3.8% | overlap: no claim | 34276 | 35113 |
| RequestParseBenchmark.parse | corpus=synthetic:100, format=xml30 | 5.802 ± 0.2 | 6.052 ± 0.54 | us/op | +4.3% | overlap: no claim | 12536 | 13328 |
