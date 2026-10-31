<!-- Gate review RVG of adopt-jdk25-baseline 2.3 and upgrade-dependencies 2.4: Codex GPT-6 Astra, read-only, NTM session xacml4j--pdp-profile-1 pane 5, after one follow-up. -->

FINAL REPORT (RVG code reviewer, Codex GPT-6)

| Change | Verdict |
| --- | --- |
| A. `adopt-jdk25-baseline`, gate 2.3 | **APPROVE WITH CHANGES** — compiler configuration is supported by source and retained class files; complete the durable gate evidence before closing 2.3. |
| B. `upgrade-dependencies`, gate 2.4 and offline upgrades | **REJECT** — shutdown flushing and dropped-event accounting do not satisfy the logging specification. |

1. **P2 — B: shutdown can lose queued log events.**

   `build-support/logback/xacml4j-file.xml:40` and `build-support/logback/xacml4j-console.xml:42` configure async appenders without `maxFlushTime`. The pinned Logback 1.3.16 source sets its default to 1,000 ms, starts a daemon worker, and waits only that long during shutdown (`logback-core:1.3.16`, `AsyncAppenderBase.java:70`, `:114`, `:142`). Slow output can therefore leave queued events unwritten when the JVM exits, violating `openspec/changes/upgrade-dependencies/specs/logging-configuration/spec.md:34`.

   **Smallest fix:** set `<maxFlushTime>0</maxFlushTime>` on both appenders to wait for draining, as documented in the [Logback async-appender manual](https://logback.qos.ch/manual/appenders-async-sift.html).

   **Test:** force a backlog whose downstream appender takes longer than one second to drain, exit the child JVM, and verify every accepted event. The existing 10,000-message test does not establish that events remain queued when shutdown begins (`xacml-core/src/test/java/org/xacml4j/logging/LoggingConfigurationTest.java:70`).

   **Checked:** configuration, exact-version upstream source, official documentation. **Not checked:** runtime reproduction; tests were prohibited.

2. **P2 — B: benchmark drops are silent, contrary to the specification.**

   `xacml-jmh/src/main/resources/logback.xml:11` enables `neverBlock` on the standard appender declared at `build-support/logback/xacml4j-console.xml:42`. Logback 1.3.16’s `AsyncAppenderBase.java:173` calls `blockingQueue.offer(eventObject)` and ignores its result. It does not count rejected events. The specification explicitly requires full-queue drops to be counted (`openspec/changes/upgrade-dependencies/specs/logging-configuration/spec.md:37`).

   **Smallest fix:** use an appender that records failed enqueue attempts and exposes the count outside the measured benchmark operation.

   **Test:** block the downstream appender, fill a small queue, submit additional events, and assert both prompt producer completion and the exact drop count. The current test checks configuration getters only (`xacml-jmh/src/test/java/org/xacml4j/jmh/LoggingConfigurationTest.java:59`).

   **Checked:** configured appender class and its exact-version enqueue implementation. **Not checked:** saturation behavior at runtime.

3. **P2 — B: the child-JVM test’s timeout cannot bound a stuck child.**

   `xacml-core/src/test/java/org/xacml4j/logging/LoggingConfigurationTest.java:80` calls `readAllBytes()` before `waitFor(60, …)` at line 82. If the child remains alive with stdout open—for example, during blocked logging—the read waits indefinitely and the timeout is never reached.

   **Smallest fix:** redirect child output to a temporary file, perform the timed wait first, and forcibly terminate the child on timeout in cleanup. Read its output afterward.

   **Test:** use a deliberately nonterminating probe and verify bounded failure and child cleanup.

   **Checked:** process-control ordering in source. **Not checked:** induced-hang execution.

4. **P2 — A and B: the executed gates lack the required durable output, and B’s packaging summary is stale.**

   The evidence inventory contains only `command.txt` and `summary.md` for A’s runs 001/002 and B’s runs 002/003. B’s static characterization run does retain `output.txt`. A’s characterization explicitly leaves its raw log in scratch (`openspec/changes/adopt-jdk25-baseline/evidence/tests/001-jdk25-at-release21/summary.md:4`).

   Consequently, the reported reactor results, dependency trees, and negative enforcer probes cannot be independently checked from these gate directories. This falls short of `docs/ai/OPENSPEC.md:107`, which requires commands and supporting output, and line 116, which excludes temporary transcripts as durable evidence.

   B’s packaging claim also describes `xacml4j-appenders.xml` in the benchmark artifacts (`openspec/changes/upgrade-dependencies/evidence/tests/002-logging-gate/summary.md:20`), while the current POM packages only `xacml4j-console.xml` (`xacml-jmh/pom.xml:64`). Inspection of the retained thin JMH jar confirmed the latter.

   **Smallest fix:** preserve sanitized output for the executed checks, including per-module test totals, dependency trees, enforcer failures, class versions, and service-file comparisons. Record a new run for configuration changes made after the previous gate rather than rewriting its history.

   **Verification:** reconcile every summary row with its indexed output and identify the source revision or hashes measured.

   **Checked:** complete inventory of both evidence directories, their indexes, summaries, and current artifact contents. **Not checked:** scratch logs or fresh gate execution.

5. **P3 — A/B: an active gate still requires JDK 21.**

   `openspec/changes/upgrade-dependencies/specs/dependency-hygiene/spec.md:40` requires the upgraded reactor’s gate on JDK 21; `proposal.md:85` repeats it. That conflicts with the Java 25 enforcer and the approval’s explicit JDK 25 instruction at `proposal.md:119`. A marks the active-gate documentation update complete (`openspec/changes/adopt-jdk25-baseline/tasks.md:12`).

   **Smallest fix:** update these active verification requirements to JDK 25, preserving historical JDK 21 results and the intentional negative enforcer probe.

   **Verification:** repeat the scoped reference search and classify remaining JDK 21 references as historical or intentional.

   **Checked:** contradictory specification, proposal, approval, and task text. **Not checked:** execution of the obsolete command.

**Checked beyond the findings**

- Parsed all nine reactor POMs: the sole explicit compiler release is the root’s `25`; Logback is declared in root test scope and overridden to runtime only in JMH (`pom.xml:107`; `xacml-jmh/pom.xml:49`).
- Inspected cached Enforcer 3.6.3 plugin metadata: `enforce` defaults to `validate`, with failure enabled and skipping disabled. Both requested executions are under active build plugins (`pom.xml:313`, `pom.xml:329`).
- Read retained class-file headers across all eight modules, using test classes for the conformance module: inspected classes were **69.0**, without preview marking. This is artifact inspection, not a fresh build.
- Inspected existing core, PDP, JSON, XML, and XML-JAXB jars: no Logback/Log4j backend classes or logging configuration entries were found. A current `xacml-test` jar was unavailable for that check.
- Confirmed the fixture-manifest diff removes exactly the approved `log4j2-test.xml` entry (`xacml-conformance-tests/src/test/integrity/vendored-fixtures.sha1`, diff at line 13).
- Scanned all 13 evidence files for common home and absolute filesystem path patterns: **zero matches**. This was a bounded pattern scan.
- Confirmed CI selects Temurin 25 and runs `verify` (`.github/workflows/main.yml:23`).

**Verification performed:** read-only `rtk git diff`, `rtk rg`, focused file reads, in-memory XML/ZIP/class-header inspection, and Logback documentation lookup. No files changed; no Maven, tests, benchmarks, Git writes, or beads writes ran.

**Residual risk:** fresh dependency resolution, negative enforcer execution, CI execution, and decision/conformance equivalence remain **UNVERIFIED** in this lane. The recorded offline stopgaps and pending online upgrades were assessed as explicitly deferred scope, not completed upgrades.
