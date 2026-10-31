# Research

Status: REQUIRED

Three tool facts decide the module setup (Q1 to Q3). One XACML fact decides
how the decision check compares Indeterminate results (Q4). Expected
decisions come from vendored responses or from the synthetic generator.

## Questions and answers

### Q1. Which JMH release is current, and under which license?

- Answer: 1.37 is the latest release of `org.openjdk.jmh:jmh-core` and
  `jmh-generator-annprocess`. JMH is licensed under the GPL 2.0 with the
  Classpath Exception.
- Sources: Maven Central, `https://central.sonatype.com/artifact/org.openjdk.jmh/jmh-core`
  (retrieved 2026-10-26: version 1.37); the local Maven metadata of
  `jmh-core` (`<latest>1.37</latest>`, last updated 2023-08-03); the
  `licenses` element of `jmh-parent-1.37.pom`.
- Confidence: high.
- Disagreement / drift risk: none. A newer JMH would change only the version
  property.

### Q2. How does JMH report allocation per operation?

- Answer: the GC profiler (`-prof gc`, class `GCProfiler`) adds the secondary
  metric `gc.alloc.rate.norm` in bytes per operation (B/op), next to
  `gc.alloc.rate` (MB/s), `gc.count`, and `gc.time`. The normalized value does
  not depend on the speed of the benchmark, so it is the number to compare.
  The JSON result format (`-rf json`) writes every secondary metric.
- Sources: JMH sample `JMHSample_35_Profilers` and
  `jmh-core/.../results/format/JSONResultFormat.java`, through Context7
  `/openjdk/jmh` (retrieved 2026-10-26).
- Confidence: high.
- Disagreement / drift risk: the names of the `gc.churn.*` metrics depend on
  the garbage collector. The harness reports only `gc.alloc.rate.norm`. JMH
  1.37 leaves the metric out when allocation profiling fails to start or when
  an iteration measures no allocation (`GCProfiler.java:93-96`, `:152-169`,
  plan review rv003 F5), so a run record says "not reported" instead of 0.

### Q3. Does a jar on the host classpath reach the forked JVMs?

- Answer: yes. JMH 1.37 starts each fork with the host's `java.class.path`
  (`Runner.getForkedMainCommand`, `jmh-core-1.37-sources.jar`,
  `org/openjdk/jmh/runner/Runner.java:844`). A store or corpus jar is added
  with `java -cp benchmarks.jar:<extra.jar> org.openjdk.jmh.Main ...`;
  `java -jar` ignores `-cp`, so it cannot add a jar.
- Sources: the JMH 1.37 source jar from Maven Central (read locally on
  2026-10-26).
- Confidence: high for the source reading. Task 3.6 confirms it with a run.
- Disagreement / drift risk: `-Djmh.separateClasspathJAR=true` writes the
  same classpath into a manifest jar; it does not change which jars are
  used.

### Q4. How does a response carry the extended Indeterminate values?

- Answer: a response `<Decision>` is one of `Permit`, `Deny`, `Indeterminate`,
  or `NotApplicable` (XACML 3.0 §5.53). `Indeterminate{D}`,
  `Indeterminate{P}`, and `Indeterminate{DP}` exist only inside the combining
  algorithms (§7.10, Appendix C.1). So an expected `Indeterminate` matches
  each of the four Indeterminate values of `org.xacml4j.v30.Decision`.
- Sources: OASIS XACML 3.0 core specification,
  `https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html`,
  §5.53 and §7.10 (retrieved 2026-10-26).
- Confidence: high.
- Disagreement / drift risk: none.

## Lanes

Research was done by the conductor; no swarm research lane ran.

| Lane | Profile / model | Report | Status |
| --- | --- | --- | --- |
| conductor | Claude Code, Claude Opus 5.5 | this file | done |

## Synthesis

n/a
