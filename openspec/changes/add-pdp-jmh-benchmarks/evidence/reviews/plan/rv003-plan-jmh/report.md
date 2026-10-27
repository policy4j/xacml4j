FINAL REPORT (policy4j_jmh, Codex GPT-6.1 Terra)

**Lane:** rv003-plan-jmh. **CLI/model:** Codex GPT-6.1 Terra, as assigned in the packet; runtime identity was not independently verified.

**Verdict: REJECT.** The load lifecycle does not yet support the promised range of stores, and several gaps could produce incomplete or misleading comparison results. The design is repairable within the benchmark harness.

Path abbreviations: `D` is `openspec/changes/add-pdp-jmh-benchmarks/design.md`; `S` is `openspec/changes/add-pdp-jmh-benchmarks/specs/pdp-benchmark-harness/spec.md`; `R` is `xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository`; `P` is `xacml-pdp/src/main/java/org/xacml4j/v30/pdp`; `J` is `~/.m2/repository/org/openjdk/jmh/jmh-core/1.37/jmh-core-1.37-sources.jar!/org/openjdk/jmh`.

### Findings

1. **Blocker — Fresh load repositories have no complete lifecycle or isolation contract.** `D:113-119`, `D:151-173`, and `D:200-227` reuse one repository ID while each measured load creates another repository. Only the trial fixture is specified to close a repository. An external store that uses the ID as a persistent namespace can collide with the already-loaded fixture or an earlier operation; stores with connections, pools, or background threads can retain resources. A pre-populated read-only store also cannot satisfy the stated empty-store contract without a separate provisioning path. The executor created by each built-in repository does **not** start a thread without listener notifications, so thread creation must not be assumed for that case (checked in `R/AbstractPolicyRepository.java:66-72,167-179`).

   **Change:** Add a harness-owned store handle with an explicit, failure-safe release hook and isolated namespace for each load invocation. Define whether cleanup and durability or visibility are inside the measured operation. Provide an attach-and-validate path for pre-populated stores; mark load benchmarks unsupported when a store cannot be provisioned. Test repeated loads, failure cleanup, overlapping workers, and a resource-counting external adapter.

2. **Major — Decision checks do not validate the lookups being compared.** `D:175-183` and `S:50-80` validate request decisions, while `D:200-204` benchmarks every corpus ID and consumes the lookup result. A store can omit a policy unused by those requests, pass fixture validation, and return `null` for its timed lookup. The snapshot equivalence test covers the built-in stores, not every external adapter (`S:82-97`).

   **Change:** Before measurement, independently derive the expected type, ID, version, and content for every timed query from the parsed corpus; validate exact and latest lookups for the selected store. Add a test store that drops an unvisited policy and another that returns the wrong latest version.

3. **Major — The synthetic scaling claim is false for Deny requests.** `D:235-244` says every decision checks every policy target. The deny-overrides loop returns immediately on the first Deny (checked in `xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/DenyOverrides.java:73-78`). A Deny targeting the first policy can therefore traverse much less of the corpus than a last-policy Deny or a full-scan Permit.

   **Change:** Correct D9 and report early-, middle-, late-, and full-scan cases separately by request ID. Add an untimed characterization that counts visited targets or references on a fresh PDP. Label the `request=all` result as the mean of the specified periodic mix.

4. **Major — The direct lookup workload is not necessarily the resolver workload.** `D:202-204` cycles corpus IDs and calls latest without constraints. The resolver passes each reference’s version, earliest, and latest constraints to the appropriate policy or policy-set lookup (checked in `R/DefaultPolicyReferenceResolver.java:92-96,123-127`). Corpus order can also include the root or unreferenced policies.

   **Change:** Add reproducible query sets derived from references, including policy versus policy-set, constrained versions, multiple versions, and the intended hit frequency. Report the query distribution. Include missing-ID behavior as a separately labeled case; the existing in-memory missing-ID defect must be reported rather than hidden (checked in `R/InMemoryPolicyRepository.java:81-87,192-198`).

5. **Major — The allocation requirement promises a metric JMH cannot always provide.** `S:166-183` requires `gc.alloc.rate.norm` for every benchmark. JMH 1.37 disables allocation profiling when initialization fails and omits the normalized metric when the iteration’s measured allocation delta is zero (checked in `J/profile/GCProfiler.java:93-96,152-169`). Its snapshots are JVM-local; the global path can include other JVM threads, while the fallback can miss short-lived threads (checked in `J/profile/GCProfiler.java:244-249,380-398`). An external service’s allocation is outside that JVM.

   **Change:** Require a supported profiler for the recorded baseline and explicitly report missing or unavailable metrics rather than treating them as zero. Label B/op as benchmark-JVM allocation, including invocation setup or cleanup allocations captured during an iteration. Do not describe it as backend allocation for remote stores.

6. **Major — The `-t` safety claim is unproven for shared fixtures.** `D:219-225` infers thread safety from the in-memory map and a new context per decision. The context shares a resolver and other objects; the PDP also has shared counters and uses shared random generators for correlation IDs (checked in `P/PolicyDecisionPointContextFactory.java:68,99,109-111,181-186`, `P/DefaultPolicyDecisionPoint.java:71-88,158-167`, and `P/MDCSupport.java:49,54-57`). These observations do not prove a race, but neither do they establish safety for arbitrary external stores.

   **Change:** Default the baseline explicitly to `-t 1`. Require a store adapter to declare whether shared reads are supported and add a concurrent correctness check before reporting `-t > 1`. Record thread count and describe multi-thread scores as the shared-PDP workload, including its counters and correlation-ID work.

7. **Major — A baseline can silently omit failed benchmark combinations.** `D:303-310` does not enable failure-on-error or require a complete result matrix. JMH 1.37 defaults failure-on-error to false and can clear a failed fork’s results without rethrowing (checked in `J/runner/Defaults.java:95-98` and `J/runner/Runner.java:694-702`).

   **Change:** Add `-foe true` to recorded runs and `shouldFailOnError(true)` to the smoke runner. Assert that results contain every expected method and parameter combination, with finite primary scores and an explicit allocation-metric status. Use distinct result files for the default and scaling runs.

8. **Major — The baseline is not reproducible from its stated identity.** `D:303-310` identifies the baseline as the commit under test, while the execution plan has lanes writing directly into the shared tree (`D:345-363`). A commit ID alone cannot identify uncommitted benchmark and corpus changes.

   **Change:** Record the base commit, a reproducible source snapshot or patch including new files, benchmark-jar and corpus hashes, store adapter/version, exact JDK build, JVM flags, thread count, and JMH parameters. Associate the eventual landed commit with that record. Preserve separate raw results for each command.

9. **Minor — Cursor overhead and request-mix assumptions are unspecified.** `D:219-227` specifies ordered per-thread cursors but not their wrap method or starting offsets. Modulo or bounds checks can matter for a nanosecond-scale lookup; synchronized starting positions can create an artificial hot-key pattern. False sharing and JIT specialization are **UNVERIFIED** until measured.

   **Change:** Specify a bounded per-thread cursor and deterministic offsets, test wraparound and selected IDs, and run a cursor-only control under the same JMH settings. Report the control separately without subtracting it mechanically. Document that `all` is an equal-weight periodic mix; use a weighted or trace-derived mix when making a production-traffic claim.

10. **Minor — Load scores need a precise operation label.** `D:200-204` calls one method `importXml`, but does not state parse/load ordering or whether file I/O is included. The existing `PolicyImportTool.importPolicy` unmarshals and adds one policy at a time (checked in `R/PolicyImportTool.java:71-80`); the proposed corpus holds source bytes in memory (`D:130-135`).

    **Change:** Define one operation as one whole corpus and label time and allocation per corpus. State whether parsing uses preloaded bytes and whether the production import tool is invoked. `AverageTime` is suitable for repeated warm loads; use a separately designed single-shot, fresh-fork experiment only for a cold-start claim (checked in `J/annotations/Mode.java:44-52,64-75`).

11. **Minor — Parallel build lanes can overlap the in-process JMH smoke test.** `D:288` puts a JMH runner in unit tests, while `D:350-355` permits parallel private builds. JMH uses a lock in `java.io.tmpdir` by default (checked in `J/runner/Runner.java:65-67,182-202`), and the project requires a quiet machine for benchmark runs (`docs/ai/SWARMS.md:60-61`).

    **Change:** Schedule JMH-based smoke tests in a serialized gate with no other build or benchmark running. Treat `forks(0)` solely as a functional smoke test; retain the separately planned forked-jar smoke test.

### Checked and found correct

- A benchmark-scoped fixture and thread-scoped cursor have the intended sharing relationship (checked in `J/annotations/Scope.java:32-42,59-70`).
- The default PDP has a no-op decision cache, while successfully resolved policy references are cached; separating warm decisions from direct store lookups is warranted (checked in `P/PolicyDecisionPointBuilder.java:66-70`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pdp/NoCachePolicyDecisionCache.java:31-40`, and `R/DefaultPolicyReferenceResolver.java:55-58,80-108`).
- Three forks, five warmup iterations, and five measurement iterations meet the project minimum (`D:211-215`; `docs/ai/BENCHMARKS.md:40-49`).
- Returning results protects the proposed methods from simple dead-code elimination (`D:226-227`); it does not establish that the workload avoids all JIT specialization.
- The manifest marks 17 RSA 2008 rows executed (`xacml-conformance-tests/src/test/manifest/conformance-manifest.csv:863-879`). Their passing status was supplied by the conductor and was not rerun in this lane.

### Verification and residual risk

Read the requested artifacts and cited source anchors with `rtk proxy nl`, `rtk proxy sed`, and `rtk rg`; inspected the local JMH 1.37 source archive with `rtk proxy unzip -p`. No files were changed, and no Maven build, test, or benchmark was run. One separate Log4j candidate-file search returned exit 2 because the named candidate did not exist; effective shaded-jar logging remains **UNVERIFIED**.

No timing, allocation, external-store lifecycle, concurrency, or forked smoke result exists from this review. JDK 28 is deferred by the plan; a future JDK 28 run must enable preview in both the host and fork JVMs and record the flags (`docs/ai/BENCHMARKS.md:52-53`).
