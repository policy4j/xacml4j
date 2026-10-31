# 010: a store from another jar in the forked JVMs (task 3.6, research Q3)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1.

Setup: `probe-store.jar` holds only `org.xacml4j.jmh.testing.ProbeStore`, its
helper `TestStores`, and a `META-INF/services/org.xacml4j.jmh.harness.PolicyStore`
file that names `ProbeStore`. Neither class is in `benchmarks.jar`.

| Command | Result |
| --- | --- |
| `java -jar benchmarks.jar PdpDecideBenchmark -p store=probe ...` | fails in setup: `Unknown store "probe"; known stores: [in-memory, snapshot]` (`java -jar` ignores the class path) |
| `java -cp benchmarks.jar:probe-store.jar org.openjdk.jmh.Main PdpDecideBenchmark -p store=probe -p corpus=rsa2008 -f 1 -wi 0 -i 1 -r 200ms -foe true` | PASS: the forked JVM found `ProbeStore` through `ServiceLoader`, the fixture passed its decision and lookup checks, and the run gave a finite score |

This confirms research Q3 by a run: JMH 1.37 starts each fork with the host
class path, so an external store needs no change to `xacml-jmh`. A first
attempt that packed `ProbeStore` without `TestStores` failed in the fork with
`ClassNotFoundException: org.xacml4j.jmh.testing.TestStores`, which also shows
that the extra jar was on the fork's class path.

The score is a single unwarmed iteration, not a measurement.

Files: `output.txt`, `results.json`.
