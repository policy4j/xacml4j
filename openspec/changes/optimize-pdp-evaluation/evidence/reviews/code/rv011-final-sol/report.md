FINAL REPORT (RVC1F code reviewer, Codex GPT-6)

**All earlier production findings are RESOLVED.** No new P1 or P2 defect was identified in the inspected fixes. One P3 test weakness remains; cycle E is **APPROVE WITH CHANGES**.

**Earlier findings**

| Finding | Status and evidence |
| --- | --- |
| D: two-argument overload bypassed custom varargs overrides | **RESOLVED.** `FunctionSpec.java:158` delegates through an explicit Expression array. `FunctionInvocationCharacterizationTest.twoArgumentCallKeepsAnOverriddenVariadicInvoke` exercises direct invocation and Match. |
| D: optimized arguments used a fixed-size list | **RESOLVED.** `FunctionSpecBuilder.java:382` creates an ArrayList. `eagerPairInvocationMayChangeItsArgumentList` exercises removal and insertion through both invocation paths. |
| B: Attribute accessor changed its serialization UID | **RESOLVED.** The helper is private to `AttributeIndex.java:259` and reads the existing `Attribute.values()` API. `RequestFixedCostCharacterizationTest.java:158` asserts baseline UID `7950110929829202273L`; run 012 records the predecessor comparison. |
| E: identifier hash became stale after subclass mutation | **RESOLVED.** `SemanticIdentifier.java:106` recomputes null-safe field hashes. `RequestFixedCostCharacterizationTest.java:165` covers mutation, HashSet lookup, and null abbreviated identifiers. |
| E: retained empty views stopped observing later writes | **RESOLVED.** `RootEvaluationContext.java:451,456` returns actual concurrent-map values views. Handler getters return unmodifiable wrappers of initialized backing maps (`DefaultEvaluationContextHandler.java:156,160`). The retained collection/map/entry-set tests cover the original failure. |
| E: concurrent test omitted status assertions | **RESOLVED.** `RootEvaluationContextCharacterizationTest.java:189` checks every retained rule/status pair after joining workers. |
| RVC1R P2: inherited `removeIf` could delete a replacement response | **RESOLVED.** Getters now expose the actual map values views (`RootEvaluationContext.java:451,456`). The four maps are independently published using compare-and-exchange (`:145–163`). `keptViewRemovesOnlyTheTestedResponse` exercises the replacement scenario, subject to the P3 weakness below. |
| RVC1R P3: early response views changed `contains(null)` | **RESOLVED.** Actual concurrent-map values views restore null rejection. `responseViewsKeptBeforeTheFirstWriteSeeLaterWrites` asserts rejection before and after writes. |

Production references above are under `xacml-core/src/main/java/org/xacml4j/v30/`, except the handler under `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/`. Tests are in their corresponding module test trees.

The additional Terra findings are also addressed:

- **Validator list aliasing:** validation is read once (`FunctionSpecBuilder.java:400`); validated calls use a separate evaluated list, while in-place evaluation is restricted to unvalidated pairs (`:419`). `FunctionInvocationCharacterizationTest.java:208` retains the validator’s list and verifies invocation does not mutate it.
- **Null checks and empty-list behavior:** predicate checks remain in AttributeIndex; null rule lookup rejects before accessing lazy storage (`RootEvaluationContext.java:214`); an empty policy list remains newly allocated and modifiable (`:425`).

**New finding**

**P3 — the response-removal test can pass after its synchronization times out.**

**Location:** `xacml-core/src/test/java/org/xacml4j/v30/policy/RootEvaluationContextCharacterizationTest.java:261`.

The predicate returns the Boolean result of `resume.await(...)`. If that wait times out before the main thread performs the merge, the predicate returns false and makes no removal attempt. The later merge can still satisfy the final assertions. `removal.get(...)` at `:270` does not establish that synchronization succeeded. Consequently, even the previous faulty implementation could pass this schedule.

**Smallest fix:** Assert that `resume.await(...)` succeeds, then return true explicitly. Keep the Future join so an assertion inside the worker fails the test.

**Verification:** The existing predecessor/candidate comparison should still distinguish the faulty wrapper. A missed release must fail the test rather than become a false predicate result.

**Checked:** complete test body and recorded red/green output. **Not checked:** executable reproduction; none was run.

**Final cycle verdicts**

| Cycle | Verdict |
| --- | --- |
| A — request attribute memo | **APPROVE** |
| B — attribute lookup loops | **APPROVE** |
| D — binary invocation | **APPROVE** |
| E — decision fixed costs | **APPROVE WITH CHANGES** — assert the predicate latch succeeds |
| F — policy allocation | **APPROVE** |

**Verification and residual risk**

Read disposition tables rv006, rv008, and rv009; runs 012, 014, and 016; and reactor run 017. Inspected the named final implementations and relevant characterization tests.

Run 016 records the validator-aliasing, null-query, and response-removal tests failing on the intermediate tree and passing on predecessor/fixed trees. Run 017 records **8,276 tests, zero failures/errors, 18 skipped**, including **938 conformance tests with 12 skipped** (`evidence/tests/017-reactor-recheck2-fixes/summary.md:5`; module counts confirmed in `output.txt`). These are retained results, not fresh execution.

No files were edited. No Maven, tests, benchmarks, profilers, Git writes, or beads writes were run. Runtime and performance acceptance were not independently reproduced. Cycle F’s previously characterized repository-removal race remains outside these optimization fixes.
