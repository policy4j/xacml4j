# 001: characterization of logging before the change (task 2.1)

Date: 2026-10-27. Static: read from the `HEAD` root POM and file list, because the
logging edits were written while a JMH session held the machine (no Maven run
may overlap a measurement); the gate (task 2.4) runs `dependency:tree` on the
changed tree.

Facts (`output.txt`): the root `<dependencies>` block, which every module
inherits, declares `log4j-api`, `log4j-core` (no scope: compile),
`log4j-slf4j-impl` (runtime through dependency management), and Yammer
`metrics-core`; `log4j.version` is 2.14.0 and `slf4j.version` 1.7.30. So every
published module carried `log4j-core` 2.14.0, which Apache lists as affected
by CVE-2021-44228 and CVE-2021-44832 (research Q1 note). Four `log4j2-test.xml`
files are tracked (core, pdp, json, conformance); the benchmark module's
`log4j2.xml` was new in this branch (untracked).
