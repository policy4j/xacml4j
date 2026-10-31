FINAL REPORT (RVC1 code reviewer, Codex GPT-6)

Reviewed cycles A, B, D, E, and F against HEAD `5fbbd6bcd473de56384dcdab0a912a1e1f74e88b` and the shared working tree. B, D, and E need corrections before acceptance. No files were edited.

1. **P2 — D: the new overload bypasses existing custom overrides.**  
   **Location:** `xacml-core/src/main/java/org/xacml4j/v30/policy/FunctionSpec.java:155`.  
   The default two-argument overload calls `invokeWithList` directly. A custom `FunctionSpec` that overrides the existing varargs `invoke` therefore loses that override when callers compile against the new overload. `Match.match` makes exactly this call (`Match.java:123`), so an extension predicate can return a different result or status after recompilation.  
   **Smallest fix:** Have the default overload delegate to `invoke(context, new Expression[]{arg0, arg1})`. Keep the optimized override in `FunctionSpecImpl`.  
   **Test:** Supply a custom spec with distinct varargs and list implementations; verify the two-argument call, explicit-array call, and `Match` preserve the existing override.  
   **Checked:** signatures, dispatch path, and Match caller. **Not checked:** executable reproduction or deployed extension usage.

2. **P2 — D: eager binary calls receive a list with different mutation behavior.**  
   **Location:** `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:382`.  
   The optimized path passes an `Arrays.asList` list through to the public `FunctionInvocation` interface (`:417`). The existing evaluation path supplies an `ArrayList` (`:635`). A custom invocation that consumes arguments with `remove(0)` previously worked; the binary path now throws `UnsupportedOperationException`, which `doInvoke` wraps as an invocation failure. In a Match, that becomes Indeterminate instead of the predicate’s result.  
   **Smallest fix:** Create one two-element `ArrayList` for the optimized path, preserving structural mutation while still skipping normalization and the separate evaluation list.  
   **Test:** Use a real eager binary invocation that removes an operand and returns a Boolean; compare two-argument and `invokeWithList` results and statuses.  
   **Checked:** list construction, invocation interface, wrapping, and Match failure handling. **Not checked:** runtime reproduction.

3. **P2 — B: the package-private accessor breaks default Java serialization compatibility.**  
   **Location:** `xacml-core/src/main/java/org/xacml4j/v30/Attribute.java:109`.  
   `Attribute` implements `Serializable` without an explicit `serialVersionUID`. Adding the non-private `valueList()` method changes the default UID calculation, despite leaving serialized fields unchanged. Existing serialized attributes consequently lose compatibility. Java serialization includes non-private methods in this calculation ([Serialization Specification §4.6](https://docs.oracle.com/en/java/javase/25/docs/specs/serialization/class.html#stream-unique-identifiers)). D6 does not declare this breaking change.  
   **Smallest fix:** Remove the new accessor and obtain the stored list through the existing `values()` method inside the index implementation, or explicitly preserve the predecessor’s computed UID.  
   **Test:** Deserialize an Attribute fixture written by the predecessor build. A same-build round trip does not detect this regression.  
   **Checked:** complete Attribute declaration, diff, and serialization rule. **Not checked:** numeric UIDs or cross-version deserialization; runtime failure remains UNVERIFIED.

4. **P2 — E: cached hashes become stale for supported subclasses.**  
   **Location:** `xacml-core/src/main/java/org/xacml4j/v30/SemanticIdentifier.java:104`.  
   `id` and `abbreviatedId` remain protected, mutable fields (`:53–54`), while `equals` reads their current values. A subclass can compute a hash, update an identifier, and then compare equal to a fresh instance while retaining a different hash. This can break set membership or map lookup even when the update occurs before insertion.  
   **Smallest fix:** Keep the allocation-free arithmetic but recompute it on each call. Making the protected fields immutable would require a separately declared compatibility change.  
   **Test:** A mutable subclass hashes an instance, changes its id, then compares equality, hashes, and HashSet lookup against a fresh equivalent instance.  
   **Checked:** field accessibility, equality, and cache implementation. **Not checked:** runtime reproduction or external subclass usage.

5. **P2 — E: lazy initialization changes live collection views into detached empty results.**  
   **Locations:** `xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:436` and `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:152`.  
   Before these changes, matching advice/obligation collections and resolved-attribute maps were views of their backing maps. Obtaining one before the first write now returns a permanently empty collection or map. An extension retaining that result no longer observes subsequently added obligations, advice, or PIP results. These getters are exposed through `EvaluationContext` (`EvaluationContext.java:293`, `:301`, `:325`, `:332`); the handler comment that they are read after the decision does not enforce that restriction.  
   **Smallest fix:** Initialize backing storage when these getters are called, or return forwarding views that continue to observe lazy storage.  
   **Test:** Retain each initially empty view, perform its first corresponding write or resolution, and assert the retained view sees the entry.  
   **Checked:** prior getter implementations in the diff, current empty branches, and public interfaces. **Not checked:** runtime reproduction.

6. **P3 — E: the concurrent-first-write test cannot detect lost status writes.**  
   **Location:** `xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextCharacterizationTest.java:174`.  
   Workers write rule statuses, but the post-join assertions check only policy and advice counts (`:181–182`). A regression that loses every status write could still pass this test.  
   **Smallest fix:** Retain each worker’s rule and expected status, then assert every `getEvaluationStatus(rule)` after joining.  
   **Test:** The strengthened existing `concurrentFirstWritesAreAllKept` test.  
   **Checked:** complete test body. **Not checked:** execution.

| Cycle | Verdict | Basis |
| --- | --- | --- |
| A — request-bag memo | **APPROVE** | Request results remain separate from response provenance; `putIfAbsent` returns the winning request bag. Same-key race and separate-decision tests exercise those paths (`DefaultEvaluationContextHandler.java:216`; `DefaultEvaluationContextHandlerTest.java:922`; `RequestAttributeMemoPdpTest.java`). |
| B — attribute lookup loops | **REJECT** | Serialization compatibility regression, finding 3. |
| D — binary invocation | **REJECT** | Extension dispatch and argument-list regressions, findings 1–2. |
| E — decision fixed costs | **REJECT** | Hash and collection-view regressions, findings 4–5; concurrency proof gap, finding 6. |
| F — policy allocation | **APPROVE** | Undefined-variable validation precedes writes to the shared empty map (`Policy.java:251`, `:284`). Reference lookup and listener invalidation retain the prior structure (`DefaultPolicyReferenceResolver.java:75`, `:133`, `:145`). |

Verification performed: scoped source/test reads, HEAD diffs, design/spec/task and review-disposition inspection, and recorded evidence review. `ast-grep scan --config sgconfig.yml xacml-core/src/main/java xacml-pdp/src/main/java` exited 0. Ripwire doctor passed 7/7; impact queries ran with caching disabled. Ripwire results are **PARTIAL**, because edges are name-based. `--quality-delta` exited 2 against the broad dirty tree; its repository-wide findings are not attributed wholesale to these cycles.

Recorded reactor evidence reports 8,264 tests, zero failures/errors, and 18 skips ([run 011 summary](openspec/changes/optimize-pdp-evaluation/evidence/tests/011-reactor-cycles-DEF/summary.md:15)). This is retained evidence, not a fresh execution. No Maven, tests, benchmarks, profilers, Git writes, or beads writes were run.

Residual risk: the proposed regression tests remain unrun. Performance and allocation claims in `evidence/benchmarks/jmh/LEDGER.md` were not independently reproduced. Approval of A and F is limited to this static review.
