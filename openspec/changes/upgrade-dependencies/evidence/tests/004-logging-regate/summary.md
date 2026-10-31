# 004: logging gate again, with durable output (task 2.4, after review rv001)

Date: 2026-10-27. Oracle GraalVM 25.0.4, Maven 3.9.16 offline. Supersedes the claims of run 002,
which kept raw logs only in scratch and named `xacml4j-appenders.xml` in the benchmark jar (the
jar has carried `xacml4j-console.xml` since the split of the fragments). Changes since run 002,
from review rv001: async appenders wait for every queued event at shutdown
(`maxFlushTime` 0); the child-JVM test waits with a timeout before reading output and kills a
stuck child; two new tests with a slow downstream appender (2,000 events, about two seconds of
backlog): all written with the shared setting, part lost with Logback's 1 s default (the
control); the spec no longer claims that dropped events are counted (Logback does not count
them). Output is sanitized (checkout `.`, home `$HOME`, URL hosts `<host>`).

| File | Result |
| --- | --- |
| `output-reactor.txt` | exit 0; 8,230 tests, 0 failures, 18 skipped; `ban-log4j` runs in every module |
| `output-dependency-tree.txt` | every module: `slf4j-api` 2.0.17 compile, Logback 1.3.16 test; `xacml-jmh`: Logback runtime; no `org.apache.logging.log4j` artifact |
| `output-jars.txt` | library jars without logging configuration; `benchmarks.jar`: `logback.xml`, `xacml4j-console.xml`, Logback classes, no Log4j classes |
| `output-ban-proof.txt` | exit 1: "log4j-core:jar:2.14.0 <--- banned via the exclude/include list" |
