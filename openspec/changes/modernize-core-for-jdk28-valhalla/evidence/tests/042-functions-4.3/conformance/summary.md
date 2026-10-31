# 042 conformance — named suites on JDK 21

JDK: java 21.0.6 (Oracle GraalVM 21.0.6+8.1), selected with `jenv prefix 21`. Offline. License header rewrite skipped. Raw log: `surefire.log`. Command: `command.txt`.

Result: BUILD SUCCESS. Total time 8.672 s. Finished 2026-10-02T02:38:30-04:00.

Surefire: tests run 882, failures 0, errors 0, skipped 23. Executed 859. Failed cases 0.

| Class | Tests run | Failures | Errors | Skipped |
| --- | --- | --- | --- | --- |
| RSA2008InteropTest | 17 | 0 | 0 | 0 |
| Xacml20ConformanceTest | 375 | 0 | 0 | 12 |
| Xacml30ConformanceTest | 488 | 0 | 0 | 11 |
| XacmlExampleTest | 2 | 0 | 0 | 0 |

The 23 skips are the manifest rows already disabled. Twenty-two use disposition `skipped`. The extra skip is `oasis30/IID029`, disposition `special-harness`, enabled false, bug `policy4j-the.3.9`. `oasis20/IID029` is special-harness and enabled, and it ran. The manifest was not edited.

No executed case changed result. Checks that this packet can move stayed passes: oasis20 and oasis30 `IIC082`, `IIC083`, `IIC168`, `IIA022`, and `IIA024`. `IIC020` stays skipped under `policy4j-the.4.6` (decision C2, not fixed here).
