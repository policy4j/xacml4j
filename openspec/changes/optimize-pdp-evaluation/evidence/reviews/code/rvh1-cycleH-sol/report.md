<!-- Review RVH1 of cycle H round 1, Codex GPT-6.1 Sol, read-only; report from the NTM pane. -->

FINAL REPORT (RVH1 code reviewer, GPT-6 / Codex)

Reviewed cycle H, D14 and task group 12 against HEAD `9855935affb0f479c5102c410e627acb7f30e6b4`. The five production-file diffs remained unchanged during review.

1. **P2 — The static cache permanently retains arbitrary extension types.**  
   `xacml-core/src/main/java/org/xacml4j/v30/BagOfValues.java:69` and `:270` introduce an unbounded map with strong keys and values. Each distinct custom `ValueType` passed to `emptyBag()` remains reachable for the lifetime of `BagOfValues`; the cached bag also retains that type through its descriptor. Recreating types or reloading extensions therefore retains instances and potentially their class loaders. Custom types are accepted by the public API (`types/ValueType.java:39`, `types/TypeFactory.java:51`).  
   **Smallest fix:** cache only canonical built-in types and retain uncached construction for extensions; scope D14 accordingly.  
   **Test:** request empty bags for disposable custom types and verify they are not admitted to the permanent cache. Runtime retention was **UNVERIFIED**.

2. **P2 — `emptyBag()` bypasses custom `bagBuilder()` behavior.**  
   `xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java:127` replaces the previous virtual `bagBuilder().build()` call with a helper that directly constructs `new Builder(valueType)` (`BagOfValues.java:269`). A custom type overriding `bagBuilder()` to reject bags now inherits an `emptyBag()` that succeeds, provided its `bagType()` remains available. This changes the previous exception contract.  
   **Smallest fix:** preserve `bagBuilder().build()` for custom implementations; restrict the optimized path to canonical built-ins.  
   **Test:** implement a custom type whose `bagBuilder()` throws; assert that inherited `emptyBag()` preserves that exception. The proposed failure case was **UNVERIFIED** by execution.

3. **P3 — The tests do not protect the lazy fallback optimization.**  
   `xacml-core/src/test/java/org/xacml4j/v30/policy/EmptyBagCharacterizationTest.java:97` checks the returned bag’s identity. Restoring the old eager `orElse(type.emptyBag())` still returns that same resolved bag, so this assertion passes. The sharing assertions at `:105` also pass after that regression.  
   **Smallest fix:** add an observable fallback invocation check for a present attribute and cover both changed designator-function sites.  
   **Test:** reverting each ternary to eager `orElse` must fail its focused test.

Verification performed: scoped source/diff review, caller searches, `git diff --check` and scoped `ast-grep scan` passed. Ripwire’s signature check reported unchanged; its impact results were **PARTIAL** because name resolution missed visible callers. Serena was **UNAVAILABLE** (`route=not-ready`). Ripwire quality-delta exited 2 for its class-length threshold and also flagged JUnit methods as dead code; these are not additional correctness findings.

Retained evidence `tests/018-cycleH-red-green/output.txt:2` and `:9` records the intended red/green result. `tests/019-reactor-cycleH/output.txt:2`–`:16` records 8,280 tests, zero failures/errors, 18 skips, including 938 conformance tests. JMH pair 019/020 is present; `benchmarks/jmh/020-cycleH-after/summary.md:18` reports the full-scan improvement. These are supplied historical results; no Maven, tests or benchmarks were run in this lane.

No additional defect was established in immutable values, built-in hash sharing or the final bag-type class. Extension behavior, class-loader reclamation and serialized compatibility across builds remain **UNVERIFIED** at runtime. No files were changed.

**Verdict: APPROVE WITH CHANGES — address both P2 findings and add the laziness regression test before closing 12.3.**
