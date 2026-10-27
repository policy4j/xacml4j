FINAL REPORT (RVC1R code reviewer, Codex GPT-6)

All six original findings are resolved in their reported scenarios. **Cycle E remains REJECT:** its replacement `LiveValues` introduces a response-removal regression and still changes null handling.

**Original findings**

| Finding | Status | Source and proof inspected |
| --- | --- | --- |
| 1. D: binary overload bypassed custom varargs overrides | **RESOLVED** | `FunctionSpec.java:158` delegates through an explicit Expression array to the existing virtual method. `FunctionInvocationCharacterizationTest.java:208` distinguishes the override from `invokeWithList` and exercises Match. |
| 2. D: binary invocation received a fixed-size list | **RESOLVED** | `FunctionSpecBuilder.java:382` creates an ArrayList. `FunctionInvocationCharacterizationTest.java:181` removes and adds operands through both invocation paths. |
| 3. B: Attribute accessor changed its default serialization UID | **RESOLVED** | `rtk git diff HEAD -- xacml-core/src/main/java/org/xacml4j/v30/Attribute.java` is empty. The helper now resides privately in `AttributeIndex.java:259`, using `Attribute.values()`. `RequestFixedCostCharacterizationTest.java:158` asserts the recorded baseline UID, `7950110929829202273L`. |
| 4. E: cached hash became stale after subclass mutation | **RESOLVED** | `SemanticIdentifier.java:106` recomputes the hash with null-safe field hashes. `RequestFixedCostCharacterizationTest.java:165` covers mutation, HashSet lookup, and null abbreviated identifiers. |
| 5. E: retained empty views failed to observe later writes | **RESOLVED for the original liveness failure** | `RootEvaluationContext.java:145` supplies a forwarding collection. Handler getters at `DefaultEvaluationContextHandler.java:156,160` now initialize backing caches and return their unmodifiable views. Tests retain collections, maps, and an entry set before writes (`RootEvaluationContextCharacterizationTest.java:199`; `DefaultEvaluationContextHandlerTest.java:965`). New wrapper defects follow below. |
| 6. E: concurrent test did not assert status writes | **RESOLVED** | The test retains every rule/status pair and checks each status after joining workers (`RootEvaluationContextCharacterizationTest.java:189`). |

Paths above are under `xacml-core/src/main/java/org/xacml4j/v30/`, its corresponding test tree, or the named `xacml-pdp` production/test tree.

The additional Terra fixes also match their dispositions: null predicate checks are restored in `AttributeIndex.java:124`, null rule lookup rejects before accessing optional storage (`RootEvaluationContext.java:235`), and the empty policy list is modifiable (`RootEvaluationContext.java:448`). The repository-removal test now checks both synchronization waits and actually clears repository content before notification (`DefaultPolicyReferenceResolverCacheTest.java:137`).

**New findings, most severe first**

1. **P2 — `LiveValues.removeIf` can delete a newly merged response.**

   **Location:** `xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:145`.

   `LiveValues` overrides only `iterator()` and `size()`. Its inherited `Collection.removeIf` removes through the iterator after testing a value. The previous ConcurrentHashMap values view instead removes conditionally against the tested value. OpenJDK’s iterator removal is unconditional by key. These implementations are visible in the official [Collection source](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/util/Collection.java) and [ConcurrentHashMap source](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/util/concurrent/ConcurrentHashMap.java), retrieved 2026-10-27.

   **Failure scenario:** An extension retains the obligation view before the first write. Thread A starts `removeIf` and pauses after inspecting an obligation. Thread B merges another assignment into that obligation’s id through `addObligations`. Thread A resumes and removes the replacement obligation through the iterator. The previous values-view implementation retains the changed value. This can remove response elements.

   **Smallest fix:** Initialize `responses()` in the getters and return the actual map’s values view, as the handler now does for resolved maps. Alternatively, preserve the wrapper but delegate `removeIf` to the backing values view.

   **Test:** Use two asserted latches around the predicate; merge a distinct assignment while removal is paused; assert the merged obligation remains. Repeat for advice.

   **Checked:** wrapper, response merge path, and OpenJDK 25 update sources. **Not checked:** executable reproduction on the recorded GraalVM build; runtime result is UNVERIFIED.

2. **P3 — the retained response view still changes `contains(null)`.**

   **Location:** `xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java:145`.

   The inherited AbstractCollection implementation returns false for null on this null-free collection. The former ConcurrentHashMap values view delegates to `containsValue(null)`, which throws NullPointerException. Thus observable behavior depends on whether the caller obtained its view before initialization. [OpenJDK ConcurrentHashMap source](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/util/concurrent/ConcurrentHashMap.java).

   **Smallest fix:** Return the actual values view as above, or explicitly preserve its null rejection in `LiveValues.contains`.

   **Test:** Retain advice and obligation views before initialization; assert `contains(null)` throws both before and after writes.

   **Checked:** source paths. **Not checked:** runtime reproduction.

**Cycle verdicts**

| Cycle | Verdict |
| --- | --- |
| A — request attribute memo | **APPROVE** |
| B — attribute lookup loops | **APPROVE** |
| D — binary invocation | **APPROVE** |
| E — decision fixed costs | **REJECT** — new findings 1–2 |
| F — policy allocation | **APPROVE** |

F’s removal-during-lookup race remains explicitly characterized as pre-existing; its preservation is not a claim that the race is correct.

**Verification and residual risk**

Inspected both disposition tables, the named production fixes and tests, and summaries/output for runs 012, 014, and 015. Run 012 records **seven failures and two errors** before fixes; its BUILD SUCCESS reflects `maven.test.failure.ignore=true`, not passing tests. Run 014 records passing baseline/candidate checks. Run 015 records **8,274 tests, zero failures/errors, 18 skipped**, including **938 conformance tests with 12 skipped**. These are retained results, not fresh execution.

The scoped `ast-grep scan` exited 0. No files were edited; no Maven, tests, benchmarks, profilers, Git writes, or beads writes were run. The new removal/null scenarios are not covered by the inspected tests.

Tavily failed to retrieve the requested OpenJDK tag URL; DuckDuckGo successfully fetched the official `jdk25u` sources as fallback. Exact GraalVM binary behavior and performance after further fixes remain UNVERIFIED.
