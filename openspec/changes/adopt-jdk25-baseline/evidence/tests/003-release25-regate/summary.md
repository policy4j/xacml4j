# 003: release 25 gate again, with durable output (task 2.3, after review rv001)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline. Re-run because review rv001
(Astra) found that runs 001 and 002 kept their raw logs only in a scratch directory. Output is
sanitized (checkout path `.`, home `$HOME`, URL hosts `<host>`).

| File | Result |
| --- | --- |
| `output-reactor.txt` | exit 0; 8,230 tests, 0 failures, 0 errors, 18 skipped (later cycles add tests; per module listed); conformance 938 run, 12 skipped; `enforce-java-25` and `ban-log4j` run in every module |
| `output-class-versions.txt` | one class per module: major 69, minor 0 (no preview) |
| `output-enforcer-jdk21.txt` | exit 1: "Detected JDK ... version 21.0.6 which is not in the allowed range [25,)" |
| `output-services.txt` | core 2 service files, xml 1, jmh 2; json and pdp none (no `@AutoService` there) |
