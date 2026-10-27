# 036: xacml-core of the merged main tree (batch 2 and D23), local build

Date: 2026-10-27. Oracle GraalVM 25.0.4, C2. Maven could not run (the local Maven
repository was emptied at 11:08 and the mirror is off VPN), so `xacml-core` was
compiled with `javac --release 25` (auto-service processor) against the jars of the
local Gradle cache, nothing fetched, and its tests run on the JUnit Platform 1.14.4
launcher with the Mockito agent and Surefire's naming filters (script by the cc_1 lane).

- Tree: main checkout, batch 2 (cycles I, J, K, L, M, N, C with the review changes)
  plus D23 (cycle Q).
- Main code compiles.
- Tests: found 6,595, succeeded 6,594, failed 0, skipped 1.
- Not run: nine test classes that need Truth8 (not in the local cache):
  `AttributeIndexTest`, `CategoryIdTest`, two `ContentTest`s, `JsonContentTest`,
  `EntityTest`, `AttributeDesignatorFunctionTest`, `BinaryTest`,
  `TypeToStringCapabilityTest`; the JSON tests (json-path stubs); `xacml-pdp`, the
  other modules, and conformance. The Maven reactor gate stays open.
