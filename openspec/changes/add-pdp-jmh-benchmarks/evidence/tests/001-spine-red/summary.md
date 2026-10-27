# 001: spine test before the harness (task 2.1)

Date: 2026-10-26. JDK: Oracle GraalVM 21.0.6+8.1. Maven 3.9.16, offline. The
`xacml-jmh` module is in the reactor with its POM, the store SPI types
(`PolicyStore`, `StoreSession`, `StoreCapability`, `PolicyStoreContext`,
`Selection`, `SelectedStore`, `Rules`), and the test `PdpFixtureTest`.

Result: RED, as intended. The main sources compile with the JMH annotation
processor appended to AutoService (no benchmark yet). The test sources fail to
compile: `PdpFixture`, `CorpusRequest`, and `LookupQuery` do not exist
(`compiler-output.txt`).

An earlier attempt in the same task failed for another reason (a
`getVersion()` call on `CompositeDecisionRule`, which has none; versions come
from `Versionable`). It was fixed with the `Rules` helper before this run.
