FINAL REPORT (policy4j_code_review_codex_astra, GPT-6 Astra)

CLI: Codex. Lanes: `rv001-group1-code` and `rv001-plugins-code`, task 1.3.

**A — optimize-pdp-evaluation: APPROVE WITH CHANGES.**  
**B — upgrade-build-plugins: APPROVE WITH CHANGES.**

Three minor findings. No blocker or major defect confirmed in the reviewed production paths. Reviewed against HEAD `5fbbd6bcd473de56384dcdab0a912a1e1f74e88b` on `feature_xacml_20`. Final hashes matched the earlier hashes of all nine scoped code, configuration, and instruction files.

1. **MINOR — A1: The random-source guard misses equivalent calls.**  
   Checked in `tools/ast-grep/rules/java-no-random-on-decide-path.yml:19`. The rule matches `Math.random()` but misses `java.lang.Math.random()`. It also misses `randomUUID()` after `import static java.util.UUID.randomUUID;`. Fresh stdin scans using the actual rule returned one error for the positive control and zero matches for both bypasses. A future change could therefore reintroduce a prohibited random source while passing this guard.  
   **Fix:** Add the qualified Math form and import-aware coverage for statically imported methods. **Regression check:** Add both demonstrated bypasses as invalid rule cases. The current cases cover ordinary qualified calls only—checked in `tools/ast-grep/rule-tests/java-no-random-on-decide-path-test.yml:10`.

2. **MINOR — B1: The source-preservation contract contradicts the characterization results.**  
   Checked in `openspec/changes/upgrade-build-plugins/specs/build-plugin-versions/spec.md:20`. The specification requires unchanged headers and identical generated JAXB source trees. The retained characterization records header differences and a JAXB generator-comment change—checked in `openspec/changes/upgrade-build-plugins/evidence/tests/001-characterization/summary.md:38` and `:47`. The implementation instead protects normal builds by skipping header updates—checked in `pom.xml:47`. These are different acceptance criteria.  
   **Fix:** Reconcile the specification and proposal with D4/D5: distinguish default-build preservation from explicitly enabled header updates, and define the exact permitted JAXB comment differences. **Verification:** Retain the raw JAXB diff and a comparison that rejects every difference outside that explicit allowance. Generated-code equivalence was **not independently checked** in this lane.

3. **MINOR — B2: The characterization helper can produce misleading comparisons when rerun on the submitted tree.**  
   Checked in `openspec/changes/upgrade-build-plugins/evidence/tests/001-characterization/characterize.sh:20` and `:29`. Both license invocations inherit the new skip default. The JAXB substitution replaces only `0.15.1`, whereas the current POM already contains `0.15.3`—checked in `xacml-xml-jaxb/pom.xml:27`. Consequently, rerunning the documented command against this tree skips both header updates and selects JAXB 0.15.3 for both copies.  
   **Fix:** Require and verify a specific baseline snapshot, or explicitly set and assert each requested plugin version; force license updates only inside the scratch characterization copies. Propagate command failures. **Regression check:** Run the helper against the candidate tree and verify that its logs identify two distinct JAXB versions and actual header-update execution. This rerun was **not checked** because execution was prohibited.

For A, the following source checks support the implementation:

- The sequence is an instance `AtomicLong`, incremented once per context; the builder constructs a fresh factory per PDP. Checked in `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java:71`, `:115`, and `PolicyDecisionPointBuilder.java:151`.
- Each `decide` allocates its own counter. Built-in request-reference, repeated-category, and XPath expansion handlers pass the same context through sequential loops. Checked in `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java:98`, `:125`, and the corresponding request handlers at `MultipleResourcesViaRequestReferencesHandler.java:77`, `MultipleResourcesViaRepeatingAttributesHandler.java:97`, and `MultipleResourcesViaXPathExpressionHandler.java:146`.
- Request MDC cleanup covers cached returns and exceptions through `finally`; PDP MDC cleanup has its own `finally`. Checked in `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java:105`, `:143`, and `:168`.
- The public setter’s signature change is explicitly declared breaking. Serena and text search found the updated production caller. Checked in `openspec/changes/optimize-pdp-evaluation/proposal.md:18`, `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/MDCSupport.java:56`, and `DefaultPolicyDecisionPoint.java:134`. External consumers were **not checked**.

For B, all selected versions matched current Maven Central metadata on 2026-10-26. Versions below link to that metadata; locations were checked in the submitted POMs.

| Plugin | Version | Placement and evidence |
|---|---|---|
| clean | [3.5.0](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-clean-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:269` |
| resources | [3.5.0](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-resources-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:274` |
| jar | [3.5.1](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-jar-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:279` |
| install | [3.2.0](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-install-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:284` |
| deploy | [3.2.0](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-deploy-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:289` |
| compiler | [3.16.0](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:294` |
| surefire | [3.6.1](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-surefire-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:299` |
| dependency | [3.11.0](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-dependency-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:304` |
| shade | [3.6.2](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-shade-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:309` |
| site | [3.22.0](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-site-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:314` |
| versions | [2.23.0](https://repo.maven.apache.org/maven2/org/codehaus/mojo/versions-maven-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:319` |
| license | [2.7.1](https://repo.maven.apache.org/maven2/org/codehaus/mojo/license-maven-plugin/maven-metadata.xml) | Root pluginManagement; checked in `pom.xml:324` |
| enforcer | [3.6.3](https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-enforcer-plugin/maven-metadata.xml) | Explicit root build/plugins override; checked in `pom.xml:332` |
| maven-jaxb2-plugin | [0.15.3](https://repo.maven.apache.org/maven2/org/jvnet/jaxb2/maven2/maven-jaxb2-plugin/maven-metadata.xml) | Module build/plugins; checked in `xacml-xml-jaxb/pom.xml:24` |

The enforcer override resolves to 3.6.3 in both retained effective-POM inventories—checked in both `effective-plugins-after-*.txt:6` under `upgrade-build-plugins/evidence/tests/001-characterization/`. Compiler release 21, AutoService processing, Mockito’s agent, and the test exclusion remain configured—checked in `pom.xml:351`, `:352`, and `:376`. The license documentation agrees with the new default—checked in `AGENTS.md:60` and `pom.xml:47`.

Verification performed: scoped diffs and source reads; native Serena references; ripwire doctor, impact, uses, and quality-delta with caching disabled; full `ast-grep scan`; three stdin rule scans; public plugin metadata retrieval; final Git state and scoped SHA-256 comparison. Ast-grep passed. Ripwire doctor passed 7/7. Quality-delta exited 2; its scoped findings concerned test discovery, unchanged `close`, duplication, and size metrics. These remain **PARTIAL** heuristic evidence, with no additional actionable defect confirmed.

Retained Maven evidence records 8,013 tests, zero failures/errors, 18 skipped for both Maven versions—checked in `upgrade-build-plugins/evidence/tests/002-gate-both-mavens/gate-mvn39-offline-summary.txt:1` and `gate-mvn310-offline-summary.txt:1`. The later 8,015-test result and shaded-jar smoke are conductor-supplied and were **not checked** here. Tavily failed to fetch JAXB metadata; DuckDuckGo successfully fetched that endpoint.

Residual risks: the four correlation tests exercise sequential calls and normal completion—checked in `CorrelationIdTest.java:99`, `:115`, `:136`, and `:149`. Concurrent calls, hexadecimal rollover past 9, exceptional MDC cleanup, cache-hit MDC, and real multiple-decision numbering remain **UNVERIFIED** by these tests. The plain counter assumes a context stays on one thread; custom handlers sharing it across threads were **not checked**. Performance gains and task 1.3 completion remain **UNVERIFIED** pending the required benchmark and evidence gates.

No files were edited. No Maven commands, tests, benchmarks, or Git writes were run.
