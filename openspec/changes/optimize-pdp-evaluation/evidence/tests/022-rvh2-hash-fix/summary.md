# 022: the hash-cache fix of review RVH2

Date: 2026-10-27. JDK 25 (Oracle GraalVM 25.0.4), Maven 3.9.16 offline.

`BagOfValues.hashCode` reads its cache field once into a local, computes and stores the hash when
the local is 0, and returns the local. Tests: `EmptyBagCharacterizationTest` (7, with the new
stress test `concurrentFirstHashesOfOneBagAgree`: 2,000 rounds of two threads hashing a new bag at
once), `BagOfValuesCharacterizationTest`, `BagOfValuesTest`: 24 run, 0 failures.

Not red first: a data race cannot be forced to show in a unit test on this JVM, so the stress test
guards the result and the fix itself (one read) is checked by review. A jcstress test would be the
proof; jcstress is not in the build.
