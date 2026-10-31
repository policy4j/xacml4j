# 002: logging gate (tasks 2.2 to 2.4)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline, release 25.

Versions: the repository mirror was unreachable (an online build could not connect), so the
POM uses the newest versions in the local repository as a recorded stopgap: slf4j-api 2.0.17
and Logback 1.3.16 (an SLF4J 2 provider; `<include>`, `AsyncAppender`, `<shutdownHook/>` and
`${name:-default}` all exist in 1.3). The approved targets 2.0.20 and 1.6.6 need one online
build with `-U` (the failed attempt cached "not found" markers) once the mirror is reachable;
the configuration and tests do not change.

| Check | Result |
| --- | --- |
| reactor `clean test` | BUILD SUCCESS; 8,206 tests, 0 failures, 18 skipped (8,202 plus the four new logging tests: `xacml-core` 2, `xacml-conformance-tests` 1, `xacml-jmh` 1) |
| conformance | 938 run (937 plus the logging test), 12 skipped |
| `LoggingConfigurationTest` (`xacml-core`) | shared defaults; child JVM with `-Dxacml4j.log.level=TRACE` reports root TRACE; all 10,000 queued messages in the file after `main` returns |
| `LoggingConfigurationTest` (conformance, jmh) | module override INFO; benchmark config never-block on, caller data off, standard error |
| `dependency:tree` | every module: `slf4j-api` compile, `logback-classic`/`-core` test; `xacml-jmh`: Logback runtime; no `org.apache.logging.log4j` artifact anywhere |
| ban rule | scratch POM with `log4j-core` 2.14.0: `BannedDependencies` fails ("log4j-core:jar:2.14.0 <--- banned via the exclude/include list") |
| jar listings | main jars of core, pdp, xml, json, test: no logging configuration; `xacml-jmh` jar and `benchmarks.jar`: `logback.xml`, `xacml4j-appenders.xml`, Logback classes, no Log4j classes |
| test logs | `target/logs/<module>-test.log` written in core, pdp, json, xml, conformance |

Vendored-fixture manifest: the maintainer approved deleting
`xacml-conformance-tests/src/test/resources/log4j2-test.xml`; its line was removed from
`xacml-conformance-tests/src/test/integrity/vendored-fixtures.sha1` (2,911 to 2,910 entries),
so `VendoredFixtureIntegrityTest` guards every other vendored file as before (it failed with
"MISSING ... log4j2-test.xml" until then).
