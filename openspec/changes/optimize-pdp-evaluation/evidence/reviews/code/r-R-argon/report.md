FINAL REPORT (agyx_1, Gemini 4 Argon)

## Verdict

**APPROVE WITH CHANGES.**

Codex Terra's implementation of D24/D25 tasks 23.1 to 23.3 (round 5a, with target indexing off) in `the main checkout` faithfully implements the approved round 5a design and all four changes from `PR5-argon` (`P1-1`, `P2-1`, `P2-2`, `P3-1`).

There are no `P1` blockers. One `P2` finding (`P2-1` on removing the unapproved `resolutionLocks` + `activeLookups` per-reference locking in [`RootEvaluationContext.java:81-82, 89-90, 450-492`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)) and one `P3` cleanup (`P3-1` on skipping the empty-cache probe in [`DefaultPolicyReferenceResolver.java:79, 103`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java`) when `enableRefCache` is `false`) should be addressed alongside 23.4.

---

## 1. Checklist Review

### 1.1 The snapshot (`RootEvaluationContext`, `PreparedPolicySets`)
- **`Generation.targets` keeps only matching targets:**
  - **Pass.** [`PreparedPolicySets.java:61`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java`) and [`RootEvaluationContext.java:509-512`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) insert into `current.targets` via `putIfAbsent(ref, resolved)` only when `resolved != null` and `ref.isReferenceTo(resolved)`.
- **Decision `misses` replay a miss, a failure (`RuntimeException` and `Error`), and a mismatch exactly as the linear path does:**
  - **Pass.** [`RootEvaluationContext.java:495-532`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) checks `misses` before `current.targets`:
    - `Missing.INSTANCE` (`resolved == null`, `:509`) throws a fresh `new PolicyResolutionException(this)` on each replay (`:529`), caught by `PolicyIDReference.createContext` (`:83-85`) / `PolicySetIDReference.createContext` (`:87-89`) to return `refContext` (`Decision.INDETERMINATE`).
    - A thrown `RuntimeException` or `Error` (including a `PolicyResolutionException` thrown by the resolver, `:513-515`) is stored in `misses` and rethrown as the same object (`:527-528`), escaping `createContext` after `new PolicyIDReferenceEvaluationContext(context)` / `new PolicySetIDReferenceEvaluationContext(context)` has run its cycle check.
    - A non-matching target (`!ref.isReferenceTo(resolved)`, `:510`) is stored in `misses` and returned by `replay` (`:526`), so `ref.evaluate` sees `!isReferenceTo(...)` and returns `Decision.INDETERMINATE`.
- **Terra's "per-reference concurrency coordination" in `RootEvaluationContext` (`resolutionLocks` and `activeLookups`):**
  - **What it is:** [`RootEvaluationContext.java:81-82, 89-90, 450-492`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) adds a `ConcurrentMap<CompositeDecisionRuleIDReference, ReentrantLock> resolutionLocks` and a `volatile int activeLookups` counter. Every call to `snapshot(ref, current)` (`:480-492`) creates `resolutionLocks`, allocates a `new ReentrantLock()` per `ref`, acquires `lock.lock()`, increments `activeLookups` with `ACTIVE_LOOKUPS.getAndAdd(this, 1)`, runs `snapshotLocked`, decrements `activeLookups`, and unlocks; `isMissed(ref)` (`:450-465`) locks `locks.get(ref)` when `activeLookups != 0`.
  - **Is it needed?** **No (`P2-1`).** Policy evaluation and reference resolution on a single `RootEvaluationContext` are strictly single-threaded on the deciding thread (even under D26 parallel attribute resolvers, worker threads receive `PreparedContext`, whose `getEvaluationContext()` throws `IllegalStateException`). Moreover, `current.targets` (`ConcurrentHashMap.putIfAbsent`) and `misses` (`ConcurrentHashMap.putIfAbsent`) are already thread-safe across decisions.
  - **Can it deadlock?** Not with `InMemoryPolicyRepository`, which only reads a map, but if a custom `PolicyRepository` or `PolicyReferenceResolver` resolves nested references in opposite orders across two threads sharing one `RootEvaluationContext`, holding `lock(refA)` across `resolver.resolve(...)` (`:484-506`) can deadlock against `lock(refB)`.
  - **Is it clean?** **No (`P2-1`).** Even on a `current.targets` hit (`:498-499`), `snapshot(ref, current)` allocates the `resolutionLocks` `ConcurrentHashMap` and a `ReentrantLock` per reference on `RootEvaluationContext` and executes two atomic `getAndAdd` barriers before checking `misses` or `current.targets`, defeating zero-allocation on `RootEvaluationContext` when a decision prepares a cold policy set or resolves an unpinned reference. And in `snapshot` (`:484-485`), `lock.lock()` runs *before* `ACTIVE_LOOKUPS.getAndAdd(this, 1)`, so `if (activeLookups != 0)` in `isMissed` (`:452`) is racy anyway.

### 1.2 `PinnedReference` (`PinnedReference.java`, `PolicyIDReference.java`, `PolicySetIDReference.java`)
- **`isMissed` check (PR5 P1-1):**
  - **Pass.** [`PinnedReference.java:49, 56, 61, 64-67`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PinnedReference.java`) checks `missed(context)` via `DescendantEvaluationContext.topOf(context)` and `root.isMissed(ref)`, delegating `createContext`, `evaluate`, and `isMatch` to `ref` when this decision has recorded a miss for `ref`, and calling `target.evaluate(context)` / `target.isMatch(context)` directly otherwise.
- **`createContext(context, target)` with no map probe (PR5 P2-1):**
  - **Pass.** [`PolicyIDReference.java:88-93`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`) and [`PolicySetIDReference.java:92-97`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`) run the existing precondition, idempotency check, and `PolicyIDReferenceEvaluationContext` / `PolicySetIDReferenceEvaluationContext` cycle check, then call `target.createContext(...)` directly without calling `resolve` or probing any map.

### 1.3 Preparation (`PolicySet.java`, `Policy.java`)
- **Dispatch by exact built-in class:**
  - **Pass.** [`PolicySet.java:198-211`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`) checks `ALGORITHMS.contains(set.combiningAlgorithm.getClass())` against all 13 built-in policy-combining algorithm classes, and returns `set.decisionRules` for foreign roots or roots without a generation.
- **`Policy` untouched while the index is off (PR5 P2-2):**
  - **Pass.** `Policy.java` is untouched.
- **`getDecisions()`, `contains`, and `accept` unchanged:**
  - **Pass.** [`PolicySet.java:230-250`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`) continues to expose `decisionRules`.

### 1.4 Wiring (`PolicyDecisionPointBuilder`, `PolicyDecisionPointContextFactory`, `PreparedPolicySetsListener`, `DefaultPolicyReferenceResolver`)
- **Listener and resolver with cache off:**
  - **Pass.** [`PolicyDecisionPointBuilder.java:161`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointBuilder.java`) passes `PreparedPolicySets.create(false)`; [`PolicyDecisionPointContextFactory.java:132-135`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`) constructs `new DefaultPolicyReferenceResolver(repository, false, 1024)` and registers `new PreparedPolicySetsListener(value)`.
- **Baseline constructor keeps today's resolver (PR5 P3-1):**
  - **Pass.** [`PolicyDecisionPointContextFactory.java:90-100, 132-134, 213-224`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`) preserves the package-private constructor without `PreparedPolicySets`, using `new DefaultPolicyReferenceResolver(repository)` with its cache on and constructing `RootEvaluationContext` without a generation.
- **`create(true)` rejects the index for now:**
  - **Pass.** [`PreparedPolicySets.java:44-47`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java`) throws `IllegalArgumentException("Target indexing is not available yet")`.

### 1.5 Tests (`PreparedPolicySetsPdpTest`, `DefaultPolicyReferenceResolverTest`, `DefaultPolicyReferenceResolverCacheTest`)
- **Cases 23.1(a)–(e) and (h) against the unprepared baseline:**
  - **Pass.** [`PreparedPolicySetsPdpTest.java:103-370`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/PreparedPolicySetsPdpTest.java`) covers:
    - **(a)** 60 short-circuit cases across all 13 built-in algorithms and 4 failure kinds (`:103-137`);
    - **(b)** escaping `IllegalStateException` and `AssertionError` error-chain equality (`:139-150`) and visited miss/mismatch response equality (`:152-157`);
    - **(c)** mismatch not cached across decisions in the PDP (`:160-167`) and in `DefaultPolicyReferenceResolver` for policies (`:191-197`) and policy sets (`:200-209`, plus [`DefaultPolicyReferenceResolverTest.java:83-97`](`xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverTest.java`));
    - **(d)** snapshot consistency across an in-decision repository update and a cold nested set on both direct and 2-thread notification executors (`:211-246`), plus in-decision miss/failure replay across `combineDecisions` and `getExtendedIndeterminate` followed by retry on the next decision (`:170-188`);
    - **(e)** all 4 repository events (`policyAdded`, `policyRemoved`, `policySetAdded`, `policySetRemoved`) on both executors matching a fresh baseline PDP (`:338-370`);
    - **(h)** PR5 P1-1: a decision that recorded a miss ignores a pin published in the same generation by a concurrent decision (`:249-265`).
  - Existing resolver cache tests (`73, 95, 106, 137` in [`DefaultPolicyReferenceResolverCacheTest.java`](`xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverCacheTest.java`)) are untouched.

### 1.6 Public API Surface
- **Pass.** Only [`PreparedPolicySets`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java`) (`create(boolean)` and `invalidate()`) and the one [`RootEvaluationContext`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) constructor taking `PreparedPolicySets` are public; `Generation`, `PreparedList`, `PinnedReference`, and `PreparedPolicySetsListener` are all package-private.

---

## 2. Findings (P1 to P3)

### [P2-1] Remove unapproved per-reference `resolutionLocks` and `activeLookups` coordination from `RootEvaluationContext`
- **File:line**: [`RootEvaluationContext.java:39, 81-82, 89-90, 450-492`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) and [`PreparedPolicySetsPdpTest.java:267-327`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/PreparedPolicySetsPdpTest.java`).
- **Issue**:
  1. `RootEvaluationContext` belongs to a single decision evaluated on one deciding thread (D26 parallel attribute resolver workers use `PreparedContext`, which forbids `getEvaluationContext()`). Yet `snapshot(ref, current)` (`:480-492`) unconditionally creates a `ConcurrentHashMap<CompositeDecisionRuleIDReference, ReentrantLock> resolutionLocks`, allocates a `new ReentrantLock()` per reference via `computeIfAbsent`, acquires and releases the lock, and performs two atomic `VarHandle.getAndAdd` operations (`ACTIVE_LOOKUPS`) on **every** `resolve(ref)` / `outcome(ref)` call — even when `ref` is already present in `current.targets` (`:498-499`) or `misses` (`:496-497`).
  2. When all references in a decision resolve to matching targets, `RootEvaluationContext` is supposed to allocate **zero** maps (`misses` stays `null`). With `resolutionLocks`, every decision that prepares a policy set or resolves a cold/unpinned reference allocates a `ConcurrentHashMap` and `ReentrantLock` instances on `RootEvaluationContext`.
  3. Holding a per-reference `ReentrantLock` across `resolver.resolve(...)` (`:484-506`) introduces a lock-ordering deadlock hazard if a custom resolver/repository ever resolves two references in opposite orders across threads, and `lock.lock()` before `ACTIVE_LOOKUPS.getAndAdd(this, 1)` (`:484-485`) is racy with `if (activeLookups != 0)` (`:452`) anyway.
- **Fix**: Remove `RESOLUTION_LOCKS`, `ACTIVE_LOOKUPS`, `resolutionLocks`, and `activeLookups` from [`RootEvaluationContext.java`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) so `isMissed(ref)` is simply `ConcurrentMap<CompositeDecisionRuleIDReference, Object> m = misses; return m != null && m.containsKey(ref);` and `snapshot(ref, current)` is the direct body of `snapshotLocked(ref, current)` (`:494-516`), matching [`design.md:611-620`](`openspec/changes/optimize-pdp-evaluation/design.md`). Remove the two synthetic multi-threaded-within-one-decision tests at [`PreparedPolicySetsPdpTest.java:267-327`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/PreparedPolicySetsPdpTest.java`).

### [P3-1] Skip the empty cache lookup in `DefaultPolicyReferenceResolver.resolve` when `enableRefCache` is `false`
- **File:line**: [`DefaultPolicyReferenceResolver.java:79-82, 103-106`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java`).
- **Issue**: The default PDP now constructs `DefaultPolicyReferenceResolver` with `enableRefCache = false` ([`PolicyDecisionPointContextFactory.java:133`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`)). In `DefaultPolicyReferenceResolver.resolve(PolicyIDReference)` (`:79`) and `resolve(PolicySetIDReference)` (`:103`), `policyIDRefCache.get(ref)` and `policySetIDRefCache.get(ref)` are still executed on every lookup even when `enableRefCache` is `false` (where the maps are permanently empty), computing `ref.hashCode()` a second time after `current.targets.get(ref)`.
- **Fix**: Guard the cache read with `enableRefCache` in both `resolve` methods:
  ```java
  Policy p = enableRefCache ? policyIDRefCache.get(ref) : null;
  ```
  and
  ```java
  PolicySet p = enableRefCache ? policySetIDRefCache.get(ref) : null;
  ```

---

## 3. Verification and Residual Risk

- **Verification performed (`SUPPORTED` by read-only source inspection and `ast-grep`)**:
  - Read Terra's report (`<scratchpad>/report-I-R-terra.md`), `patch-R.diff`, [`design.md:555-760`](`openspec/changes/optimize-pdp-evaluation/design.md`), and [`tasks.md:149-199`](`openspec/changes/optimize-pdp-evaluation/tasks.md`).
  - Read all new and modified files in the main checkout: [`PreparedPolicySets.java:1-70`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java`), [`PinnedReference.java:1-71`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PinnedReference.java`), [`PreparedPolicySetsListener.java:1-57`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PreparedPolicySetsListener.java`), [`RootEvaluationContext.java:1-689`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`), [`DescendantEvaluationContext.java:125-150`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`), [`PolicyIDReference.java:1-203`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`), [`PolicySetIDReference.java:1-194`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`), [`PolicySet.java:1-250`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java`), [`PolicyDecisionPointBuilder.java:1-163`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointBuilder.java`), [`PolicyDecisionPointContextFactory.java:1-229`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`), [`DefaultPolicyReferenceResolver.java:1-182`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolver.java`), [`PreparedPolicySetsPdpTest.java:1-504`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/PreparedPolicySetsPdpTest.java`), [`DefaultPolicyDecisionPointTest.java:1-163`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPointTest.java`), [`DefaultPolicyReferenceResolverTest.java:1-98`](`xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverTest.java`), and [`DefaultPolicyReferenceResolverCacheTest.java:1-169`](`xacml-pdp/src/test/java/org/xacml4j/v30/spi/repository/DefaultPolicyReferenceResolverCacheTest.java`).
  - Ran `ast-grep scan` on `xacml-core/.../policy` and `xacml-pdp/.../pdp`: 0 findings (exit code 0).
  - Strictly read-only: no files were created, edited, or deleted, and no builds were run.
- **Residual risk**:
  - Tasks 23.4 (`TargetIndex`, `targetIndex()`, `create(true)`, and test cases 23.1(f) and (g)) and 23.5 (`PdpDecideBenchmark` `targetIndex` parameter) remain open.
  - In [`PreparedPolicySets.java:62`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PreparedPolicySets.java`), `Collections.synchronizedMap(new IdentityHashMap<>())` avoids allocating an identity-wrapper key and avoids calling `BaseCompositeDecisionRule.hashCode()` on every `PolicySet` evaluation, at the cost of a brief monitor acquisition per `PolicySet` under multi-threaded PDP load; its single- and multi-threaded throughput will be measured in the batch JMH pair (23.6 / 35.1).
