=== BEGIN REPORT R-D48-argon ===
# Code Review R-D48-argon: Codex Terra's D48 and Codex Astra's FIX-RXP3

- **Reviewer**: Lane `agyx_1` (Gemini Argon)
- **Work under review**:
  1. **D48** (Codex Terra, [`PATCH-D48.diff`](`.ntm/worktrees/xacml4j--batch2/agyx_1/PATCH-D48.diff`), [`REPORT-I-D48-terra.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/REPORT-I-D48-terra.md`), [`design.md`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/optimize-pdp-evaluation/design.md`)): evaluated responses retain a normalized assignment list, build the multimap on demand, and remove PDP repository listeners on `close()`.
  2. **FIX-RXP3** (Codex Astra): fixes for the two P3 findings of [`R-FIXRX-argon`](`.ntm/worktrees/xacml4j--batch2/agyx_1/openspec/changes/add-regex-engine-spi/evidence/reviews/code/r-FIXRX-argon/report.md`) in `add-regex-engine-spi`.
- **Verdict**:
  - **D48**: **APPROVE** (`0 P1`, `0 P2`, `0 P3`)
  - **FIX-RXP3**: **APPROVE** (`0 P1`, `0 P2`, `0 P3`; both P3 findings fixed)

---

## 1. D48 Review (`optimize-pdp-evaluation`)

### 1.1 The Normalized Assignment List (`SUPPORTED`)

- **Normalization and bound ([`DecisionRuleResponse.java:61-99`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`))**:
  - The package-private constructor [`DecisionRuleResponse(String, Effect, Collection)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) copies `assignments` with `ImmutableList.copyOf(assignments)` (a no-op when the engine passes an `ImmutableList`) and checks [`isNormalized(values)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`).
  - `isNormalized` enforces the bound `assignments.size() <= 8` (`:77-79`). Without allocating, it tracks the start index `start` of the current contiguous `attributeId` run:
    - When `key` changes at index `i` (`:84`), it scans `j` in `[0, start)` (`:85-89`) and rejects the list if `key` appeared in an earlier run (interleaved attribute IDs) before advancing `start = i` (`:90`).
    - Within the active run `[start, i)`, it scans `j` in `[start, i)` (`:92-96`) and rejects the list if `assignment.equals(assignments.get(j))` (duplicate `AttributeAssignment`).
  - When `isNormalized(values)` holds, `values` already has the exact element sequence of `ImmutableSetMultimap.values().asList()` (distinct assignments grouped by first-seen `attributeId` in first-seen order), so `this.assignmentList = values` is kept directly and `this.attributes` remains `null` (`:66-67`).
  - When `isNormalized(values)` is `false` (more than 8 assignments, interleaved IDs, or duplicate assignments), `:69-72` builds `ImmutableSetMultimap<String, AttributeAssignment> grouped = attributes(values)` via [`attributes(ImmutableList)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`), stores `this.attributes = grouped`, and sets `this.assignmentList = grouped.values().asList()`.

### 1.2 The Lazy Multimap and Subclass Builder Keys (`SUPPORTED`)

- **Volatile field and eager builder constructor**:
  - [`DecisionRuleResponse.java:40`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) declares `protected volatile Multimap<String, AttributeAssignment> attributes;`, preserving the field's name, type, and `protected` visibility while ensuring safe publication on lazy materialization.
  - [`DecisionRuleResponse(Builder<?>)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) (the only constructor reachable by an external subclass; [`Advice`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Advice.java`) is `final` and [`Obligation`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Obligation.java`) has only package-private and private constructors) eagerly assigns `this.attributes = values` and `this.assignmentList = values.values().asList()`.
- **Engine reads**:
  - [`getAttributes()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) returns `assignmentList`.
  - [`getAttribute(String)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) and [`toString()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) read through the private [`attributes()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) accessor.
  - [`Obligation.from`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Obligation.java`), [`Obligation.merge`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Obligation.java`), [`Advice.from`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Advice.java`), and [`Advice.merge`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/Advice.java`) all read `getAttributes()`.
- **Completeness and correctness of the subclass-builder key fallback ([`keysMatchAssignments()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`))**:
  - Because [`DecisionRuleResponse.Builder.attributes`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) is `protected Multimap<String, AttributeAssignment>`, a subclass builder can insert an entry `(k, v)` with `!k.equals(v.getAttributeId())`.
  - When `attributes == null` (every normalized factory-built response prior to multimap materialization), `keysMatchAssignments()` returns `true` in $O(1)$ without building `attributes` (`:131-133`).
  - When `attributes != null`, `keysMatchAssignments()` verifies that every entry's key equals `entry.getValue().getAttributeId()` (`:135-139`). If any entry has a custom key:
    - [`hashCode()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) falls back to `attributes().hashCode()`;
    - [`equalsTo(DecisionRuleResponse)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) bypasses the `assignmentList.equals(r.assignmentList)` fast path unless `keysMatchAssignments() && r.keysMatchAssignments()` holds on both operands, falling back to `attributes().equals(r.attributes())`;
    - [`Builder.from(DecisionRuleResponse)`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`) copies `this.attributes.putAll(r.attributes())` when `!r.keysMatchAssignments()` so custom keys survive builder copies, and calls `attributes(r.getAttributes())` otherwise so factory-built responses are copied without materializing their multimap;
    - `getAttributes()`, `getAttribute(id)`, and `toString()` already preserve custom keys because `assignmentList` is `values.values().asList()` and `this.attributes` is `values`.
  - This handling is both sound and complete, and is pinned in [`ResponseFactoryTest.subclassBuilderKeepsItsSuppliedMultimapKeys`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/ResponseFactoryTest.java`).

### 1.3 Equality, Hash, `toString`, and Marshalling (`SUPPORTED`)

- **Bidirectional equality ([`DecisionRuleResponse.java:198-202`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`))**:
  - `assignmentList.size()` always equals `attributes().size()` because `assignmentList` contains the deduplicated `(key, assignment)` entries. When two responses have the same `id` and the same set of assignments per key in the same order, `assignmentList.equals(r.assignmentList)` succeeds without materializing `attributes`. When their key order or intra-key value order differs, `equalsTo` falls back to `attributes().equals(r.attributes())`, which compares the `ImmutableSetMultimap`s symmetrically. Pinned in [`ResponseFactoryTest.java:71-72, 89-106`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/ResponseFactoryTest.java`).
- **Bit-identical hash ([`DecisionRuleResponse.java:175-196`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/DecisionRuleResponse.java`))**:
  - `ImmutableSetMultimap.hashCode()` delegates to `asMap().hashCode()`, which is $\sum_{(k, S_k)} \bigl(k.\text{hashCode}() \oplus \sum_{a \in S_k} a.\text{hashCode}()\bigr)$.
  - Because `assignmentList` partitions distinct assignments into contiguous runs per `attributeId`, the loop at `:180-188` sums `assignment.hashCode()` across each run into `setHash` and accumulates `key.hashCode() ^ setHash` into `mapHash`, producing the exact same `int` as `attributes().hashCode()` for every input (including `0`, `1`, multi-key, duplicate, interleaved, and $>8$ assignments; pinned against `31 * (31 + "id".hashCode()) + attributes.build().hashCode()` at [`ResponseFactoryTest.java:58-62, 73-74`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/ResponseFactoryTest.java`)).
- **`toString` and marshalling**:
  - `toString()` (`:205-211`) passes `attributes()` to `MoreObjects.toStringHelper`, matching the previous output (`ResponseFactoryTest.java:75`).
  - XML and JSON marshallers iterate `getAttributes()`, which yields the same elements in the same order as `attributes.values()`.

### 1.4 PDP Repository Listener Cleanup (`SUPPORTED`)

- **Idempotent removal on `close()` ([`DefaultPolicyDecisionPoint.java:417-419`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`), [`PolicyDecisionPointContextFactory.java:134-155`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`))**:
  - `DefaultPolicyDecisionPointContextFactory` retains `repository`, `policyReferenceResolver`, and `preparedListener`, and `synchronized void close()` removes both `preparedListener` (when present) and `policyReferenceResolver` from `repository` once (`if (!closed)`).
  - Pinned for `prepared = false` and `prepared = true` (including a double `close()`) in [`PreparedPolicySetsPdpTest.closingAPdpStopsRepositoryChangesReachingIt`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/test/java/org/xacml4j/v30/pdp/PreparedPolicySetsPdpTest.java`).
- **Acceptability of already-queued callbacks running after `close()`**:
  - In [`AbstractPolicyRepository.java:82, 168-203`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/AbstractPolicyRepository.java`), `listeners` is a `CopyOnWriteArrayList` and repository events submit `() -> l.policyAdded(p)` tasks to `notificationExecutor`.
  - [`PolicyRepository`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/PolicyRepository.java`) provides `addPolicyRepositoryListener` and `removePolicyRepositoryListener` only; it has no task-cancellation API, and closing one PDP must not shut down a shared repository's `notificationExecutor`.
  - If a callback queued before `close()` executes after `close()`, [`PreparedPolicySetsListener`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PreparedPolicySetsListener.java`) only increments the `PreparedPolicySets` generation counter and [`DefaultPolicyReferenceResolver`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java`) only evicts matching entries from its `ConcurrentHashMap`s, after which the `Runnable` completes and drops its reference to the listener. Any repository mutation after `close()` does not observe the removed listeners. This is completely acceptable.

---

## 2. FIX-RXP3 Review (`add-regex-engine-spi`)

| # | `R-FIXRX-argon` Finding | Status | Evidence (`file:line`) |
| --- | --- | --- | --- |
| 1 | **P3-1** — `XacmlPattern` class back-reference validation and range endpoints (`[\1]`, `(a[\1])`, `(a)[a-\1]`) | **Fixed** (`SUPPORTED`) | [`XacmlPattern.java:605-608`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/regex/XacmlPattern.java`) calls [`parseBackReference()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/regex/XacmlPattern.java`) when `peek()` is `'1'..'9'`, validating `group <= groups && closed[group]` (`MALFORMED` when the group does not exist or is open) before throwing `RegexCompileException.unsupported("back-reference in a character class", "xf parser")`. In [`parseClassEscape`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/regex/XacmlPattern.java`) (used for range endpoints at `:622`), `'1'..'9'` now falls through to `default -> throw RegexCompileException.malformed("invalid escape")`. Pinned in [`XacmlPatternTest.java:75, 103-108`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/function/regex/XacmlPatternTest.java`) and [`RegexStatusMatrixTest.java:59-82`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/test/java/org/xacml4j/v30/policy/function/impl/RegexStatusMatrixTest.java`). |
| 2 | **P3-2 (part 1)** — `LiteralBinder` visibility and package placement | **Fixed** (`SUPPORTED`) | `org.xacml4j.v30.policy.function.LiteralBinder` is removed and replaced by package-private [`LiteralBinder`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/LiteralBinder.java`) in `org.xacml4j.v30.policy.function.impl`, alongside [`RegExpFunctions.Matching`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java`) and [`DefaultFunctionInvocation`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java`). |
| 3 | **P3-2 (part 2)** — `XacmlPattern.prepare()` replaced by static initialization and memoized `XF_READY` probe | **Fixed** (`SUPPORTED`) | [`XacmlPattern.java:51-53`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/regex/XacmlPattern.java`) initializes `UnicodeSets.prepare()` in `static {}` and removes `public static void prepare()`. [`RegExpFunctions.Matching.<init>`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java`) runs [`XF_READY.get()`](`.ntm/worktrees/xacml4j--batch2/agyx_1/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java`) for every `RegexProfile.XF` provider outside any evaluation deadline. |

---

## 3. Findings (P1 to P3)

None (`0 P1`, `0 P2`, `0 P3`).

---

## 4. Verification Summary

- **Focused test execution (`SUPPORTED`)**:
  - `JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am -Dtest=ResponseFactoryTest,ExternalResponseBuilderCompatibilityTest,ExternalObligationExpressionCompatibilityTest,ObligationTest,AdviceTest,ObligationExpressionTest,NestedResponseStagingTest,PreparedPolicySetsPdpTest,DefaultPolicyDecisionPointTest,XacmlPatternTest,RegexStatusMatrixTest,RegexEngineContractTest,RegexBindingTest,RegexServiceLoadingTest,RegexEngineSelectionTest -Dsurefire.failIfNoSpecifiedTests=false test` — exit 0.
- **Static & call-graph checks (`SUPPORTED`)**:
  - `ast-grep scan` — exit 0 (0 findings).
  - `ripwire . --exclude=/evidence/ --edit-check=DecisionRuleResponse` — exit 0 (`incompatible="0"`).
=== END REPORT R-D48-argon ===
