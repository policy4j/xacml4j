# 002: release 25 gate (task 2.3)

Date: 2026-10-27. Oracle GraalVM 25.0.4+7.1, Maven 3.9.16 offline.
Changes: root compiler `<release>25</release>`; enforcer execution
`enforce-java-25` (`requireJavaVersion [25,)`); the redundant
`<release>21</release>` override in `xacml-core/pom.xml` removed (found by
the reference search; it would have kept `xacml-core` at 21).

| Check | Result |
| --- | --- |
| reactor `clean test` on JDK 25 | BUILD SUCCESS; 8,202 tests, 0 failures, 0 errors, 18 skipped; per module equal to run 001 and to the JDK 21 gate |
| conformance | 937 run, 12 skipped |
| class files (`javap -v`, one class per module: core, pdp, xml, json, test, jmh) | major version 69, minor version 0 (no preview) |
| enforcer | runs in all nine projects; on JDK 21 `validate` fails: "Detected JDK ... is version 21.0.6 which is not in the allowed range [25,)" |
| AutoService services | generated in every module that declares `@AutoService`: `xacml-core` (FunctionProvider, DecisionCombiningAlgorithmProvider), `xacml-xml` (MarshallingProvider), `xacml-jmh` (PolicyStore, PolicyCorpusProvider); `xacml-json` and `xacml-pdp` declare none |
| JDK warnings | the Yammer metrics `sun.misc.Unsafe` warning of run 001 only |
