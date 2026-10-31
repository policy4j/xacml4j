# 002: JMH baseline on JDK 25 (adopt-jdk25-baseline task 3.1)

Date: 2026-10-27, 06:06 to 06:29 UTC. Oracle GraalVM 25.0.4+7.1 with the C2 JIT
(`-XX:-UseJVMCICompiler`; `env.txt`), forks with `-Xms2g -Xmx2g -XX:+UseG1GC`, JMH 1.37,
3 forks x 5 iterations, `-t 1`, `-prof gc`, `-foe true`; the jar at release 25 (class files 69).
Logging backend in this jar: Log4j 2.14.0 at WARN (built before the Logback switch of
`upgrade-dependencies`; later pairs run on the Logback build, each with its own before run).
No agent session alive. Completeness: default 20 of 20 sets, scaling 14 of 14, finite errors,
15 samples each. Full tables: `tables.md`.

This is the reference baseline from now on (the JDK 21 run `001-baseline` stays as evidence).

## Decide

| Set | JDK 25 |
| --- | --- |
| rsa2008, in-memory | 18.97 ± 0.43 us/op (45,115 B/op) |
| rsa2008, snapshot | 19.48 ± 0.4 us/op (45,944 B/op) |
| synthetic:100, in-memory | 43.87 ± 0.9 us/op (176,207 B/op) |
| synthetic:100, snapshot | 43.66 ± 0.74 us/op (176,025 B/op) |

Per request, store `in-memory` (JDK 21 values in `001-baseline`):

| Request | synthetic:10 | synthetic:1000 |
| --- | --- | --- |
| first-deny | 6.334 ± 0.3 us/op (15,726 B/op) | 6.503 ± 0.2 us/op (15,816 B/op) |
| middle-deny | 9.395 ± 0.26 us/op (26,624 B/op) | 252.5 ± 10 us/op (1,075,387 B/op) |
| last-permit | 11.19 ± 0.52 us/op (34,955 B/op) | 480 ± 29 us/op (2,121,673 B/op) |
| unknown-notapplicable | 10.04 ± 0.21 us/op (30,638 B/op) | 479.1 ± 16 us/op (2,127,619 B/op) |
| first-permit | 11.16 ± 0.76 us/op (34,422 B/op) | 495.9 ± 15 us/op (2,132,461 B/op) |
| middle-permit | 11.24 ± 0.23 us/op (34,969 B/op) | 491.2 ± 11 us/op (2,124,425 B/op) |
| last-deny | 11.28 ± 0.62 us/op (34,571 B/op) | 475.9 ± 18 us/op (2,132,400 B/op) |

## JDK 25 against JDK 21 (same jar sources, different JDK and release)

Only `first-deny` on synthetic:1000 moved beyond the error intervals: 7.21 ± 0.12 to
6.50 ± 0.20 us (-9.8%), 16,725 to 15,816 B/op. Every other set overlaps (changes between
-7.6% and +6.6%). The per-policy cost of a full scan stays near 0.47 us and 2.1 KB.

## Store and load

| Benchmark | Set | JDK 25 |
| --- | --- | --- |
| PolicyStoreLoadBenchmark.importXml | rsa2008, in-memory | 777.2 ± 24 us/op (1,120,540 B/op) |
| PolicyStoreLoadBenchmark.importXml | rsa2008, snapshot | 767.1 ± 44 us/op (1,107,723 B/op) |
| PolicyStoreLoadBenchmark.importXml | synthetic:100, in-memory | 6709 ± 2e+02 us/op (10,640,652 B/op) |
| PolicyStoreLoadBenchmark.importXml | synthetic:100, snapshot | 6936 ± 1.8e+02 us/op (11,050,206 B/op) |
| PolicyStoreLoadBenchmark.loadParsed | rsa2008, in-memory | 2.383 ± 0.051 us/op (23,527 B/op) |
| PolicyStoreLoadBenchmark.loadParsed | rsa2008, snapshot | 2.578 ± 0.069 us/op (11,352 B/op) |
| PolicyStoreLoadBenchmark.loadParsed | synthetic:100, in-memory | 9.572 ± 0.58 us/op (68,553 B/op) |
| PolicyStoreLoadBenchmark.loadParsed | synthetic:100, snapshot | 176.1 ± 5.7 us/op (511,844 B/op) |
| PolicyStoreLookupBenchmark.cursor | rsa2008, in-memory | 0.7806 ± 0.022 ns/op (0 B/op) |
| PolicyStoreLookupBenchmark.cursor | rsa2008, snapshot | 0.7939 ± 0.024 ns/op (0 B/op) |
| PolicyStoreLookupBenchmark.cursor | synthetic:100, in-memory | 0.7978 ± 0.033 ns/op (0 B/op) |
| PolicyStoreLookupBenchmark.cursor | synthetic:100, snapshot | 0.7877 ± 0.027 ns/op (0 B/op) |
| PolicyStoreLookupBenchmark.lookup | rsa2008, in-memory | 32.16 ± 2.6 ns/op (175 B/op) |
| PolicyStoreLookupBenchmark.lookup | rsa2008, snapshot | 15.54 ± 1 ns/op (0 B/op) |
| PolicyStoreLookupBenchmark.lookup | synthetic:100, in-memory | 37.94 ± 1.2 ns/op (176 B/op) |
| PolicyStoreLookupBenchmark.lookup | synthetic:100, snapshot | 17.74 ± 0.63 ns/op (0 B/op) |
