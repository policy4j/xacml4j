# Plan Review Report: `optimize-pdp-evaluation` Task `37.2` — Design `D35` (`pr28-luna-cache-spi-draft.md`)

- **Reviewer**: `agyx_1` (`policy4j_reviewer_argon`, Agy Gemini 4 Argon High)
- **Date**: 2026-10-31
- **Change**: `optimize-pdp-evaluation` (Task `37.2`, Design `D35`)
- **Artifact under review**: `${HOME}/Projects/Personal/xacml4j/openspec/changes/optimize-pdp-evaluation/research/pr28-luna-cache-spi-draft.md`
- **Verdict**: **APPROVE WITH CHANGES** (`0` P0, `2` P1, `5` P2, `2` P3)

---

## 1. Executive Summary

The D35 draft (`pr28-luna-cache-spi-draft.md`) honors the maintainer's settled architectural choices:
1. One cache instance per resolver (`create(ResolverDescriptor<?>, ResolverCacheConfig)`).
2. Cross-decision coalescing of concurrent cache misses on `(resolver, ResolverCacheKey)`.
3. Non-retention of failures, timeouts, misses, and absence (`Optional.empty()`).
4. Separate Maven modules for Caffeine (`xacml-pdp-cache-caffeine`) and cache2k (`xacml-pdp-cache-cache2k`), keeping Guava in `xacml-pdp`.
5. One xacml4j `ServiceLoader` locator (`PolicyInformationPointCacheProviders`) with explicit builder precedence and no Artagon core locator dependency.

However, source verification against `DefaultPolicyInformationPoint`, `ResolverContext`, `AttributeSet`, `BasePolicyInformationPointCacheProvider`, `DefaultEvaluationContextHandler`, and the official documentation of Guava `33.7.2-jre`, Caffeine `3.3.1`, and cache2k `2.6.1.Final` exposes **two P1 correctness blockers** and **five P2 design gaps** that must be corrected in the D35 design before implementation:

- **P1-1 (D26 Incomplete `AttributeSet`s & Single-Flight Semantics)**: A D26 parallel resolver (`buildParallel()`) whose attributes partially succeed and partially fail/time out returns `Optional.of(incompleteSet)` with `!incompleteSet.isComplete()` (`AttributeResolverDescriptor.java:184-200`, `DefaultPolicyInformationPoint.java:151-160, 207-212`). The cache must **not** store `incompleteSet`, yet `getOrLoadAttributes` **must still return `Optional.of(incompleteSet)` to the caller(s) of the current flight** so a caller requesting a succeeded attribute receives its value while a caller requesting a failed attribute rethrows that attribute's failure (`ParallelAttributeResolverTest.java:131-140, 171-181`). Native library loaders (`LoadingCache.get`, Caffeine `Cache.get(key, fn)`, `computeIfAbsent`) cannot represent "return `Optional.of(incompleteSet)` to the caller(s) without storing it in the cache", and Caffeine `Cache.get(key, fn)` does not share failed or `null`-returning loads across waiting threads (waiters retry sequentially one by one; [Caffeine FAQ](https://github.com/ben-manes/caffeine/wiki/Faq), fetched 2026-10-31). The draft's "cross-library recommendation" (`pr28-luna-cache-spi-draft.md:65`) — a shared xacml4j transient in-flight `CompletableFuture` table + `getIfPresent`/`peek` + conditional `put` — must be **mandatory** across all three adapters, not an optional alternative to native `LoadingCache.get` / `Cache.get(key, fn)` (`:61-63`).
- **P1-2 (Eager `ResolverCacheKey` Evaluation & Double-Resolution / Cache Poisoning on Absent Dependencies)**: Defining `getOrLoadAttributes(ResolverCacheKey key, ...)` forces `context.getCacheKey()` before calling the cache (`pr28-luna-cache-spi-draft.md:45-48`). In `ResolverContext.Default` (`ResolverContext.java:149-197`), when all declared `contextKeyRefs` resolve to `Optional.empty()`, `this.resolvedKeys` is `ImmutableMap.of()` (`isEmpty() == true`) while `this.cacheKey` is memoized as `ResolverCacheKey(resolverId, [])`. When `loader` subsequently runs the resolver and calls `context.resolveContextRef(depKey)` (`:193`), `resolvedKeys.isEmpty()` triggers `resolveAllContextRefs()` **a second time in the same lookup** — breaking `DefaultPolicyInformationPointForDecisionTest.aDependencyAbsentThenFailedKeepsTheFirstLookupAbsenceAndTheLaterRetry:164-174`, duplicating D34 observer spans, and poisoning the cross-decision cache under the empty-dependency key if the second resolution returns a value. Eager `getCacheKey()` on `NoCache` also breaks lazy dependency resolution (`DefaultPolicyInformationPointTest:206-210`, `r-FABLE-sol` P2), and `DecisionSets` (task 30) must be checked after `getCacheKey()` and before `getOrLoadAttributes` for dependent resolvers (`DefaultPolicyInformationPoint.java:187-196`).

---

## 2. Severity-Ordered Findings

### [P1-1] D26 incomplete `AttributeSet`s (`!set.isComplete()`) and absent loads break under native `LoadingCache.get` / `Cache.get(key, fn)` and under the draft's "exceptionally/empty" flight rule

- **Severity**: P1 (Correctness / XACML 3.0 §7.19 Indeterminate vs partial-success semantics / D26 contract)
- **Locations**:
  - Draft: `pr28-luna-cache-spi-draft.md:53, 61-65, 71`
  - Source: `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:151-160, 207-212`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptor.java:184-200`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AttributeSet.java:138-140`
  - Tests: `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ParallelAttributeResolverTest.java:131-140, 171-181`
- **Concrete failure scenario**:
  1. A D26 parallel resolver (`AttributeResolverDescriptorBuilder.buildParallel()`) declares two attributes: `"role"` (returns `"admin"`) and `"department"` (throws `XacmlEvaluationException` or times out). `invoke(d, context)` returns `Optional.of(incompleteSet)` where `incompleteSet.isComplete() == false` (`AttributeResolverDescriptor.java:195-199`).
  2. In `DefaultPolicyInformationPoint.java:151-160, 207-212`, `cached(...)` must **not** cache `incompleteSet` (`if (v.isComplete()) cache.putAttributes(...)`), **yet must still return `Optional.of(incompleteSet)`** to `resolve(...)` so that:
     - Decision A requesting `"role"` receives `Optional.of(BagOfAttributeExp("admin"))` (`ParallelAttributeResolverTest.java:138, 177`).
     - Decision B requesting `"department"` calls `incompleteSet.rethrowFailure("department")` and throws `AttributeReferenceEvaluationException` (`:139`, XACML 3.0 §7.19 Indeterminate).
     - The next decision does not serve `incompleteSet` from the cache (`ParallelAttributeResolverTest.onlyACompleteSetIsServedFromTheCache:171-181`).
  3. In `pr28-luna-cache-spi-draft.md:53`, the draft states: *"only a present complete set or present content is retained; an empty `Optional`, thrown failure, or timeout completes the current flight exceptionally/empty and leaves no cache entry"*, and in `:61-63` permits Guava `LoadingCache.get(key)` and Caffeine `Cache.get(key, function)`.
     - If the library loader returns `incompleteSet`, Guava, Caffeine, and cache2k unconditionally store it in the cache, breaking `onlyACompleteSetIsServedFromTheCache` (`:177-180`).
     - If the loader turns `!set.isComplete()` into `null` / `Optional.empty()` or throws an exception to prevent caching, Decision A requesting `"role"` either gets `Optional.empty()` (losing the valid `"admin"` attribute) or fails with an exception (violating `aSucceededAttributeIsReturnedWhenASiblingAttributeFails:131-140`).
     - Moreover, in Caffeine `3.3.1`, `Cache.get(key, fn)` delegates to `ConcurrentHashMap.compute`; per the official Caffeine FAQ ([`Faq`](https://github.com/ben-manes/caffeine/wiki/Faq), fetched 2026-10-31), if `fn` returns `null` (for `Optional.empty()` or an uncacheable incomplete set) or throws an exception, **blocked waiters on the same key do not share that flight's outcome — each waiter re-runs `fn` sequentially while holding the bin lock**, defeating D35's single-flight coalescing on slow failing/absent remote calls.
     - In Guava `33.7.2-jre`, `LoadingCache.get(key)` uses a cache-wide `CacheLoader` fixed at build time (unable to take the per-call `CheckedSupplier`), while `Cache.get(key, callable)` throws `InvalidCacheLoadException` on `null` and increments `loadExceptionCount()` in `CacheStats` whenever an exception sentinel is thrown for `Optional.empty()` ([`Cache.html`](https://guava.dev/releases/33.7.2-jre/api/docs/com/google/common/cache/Cache.html), [`CacheStats.html`](https://guava.dev/releases/33.7.2-jre/api/docs/com/google/common/cache/CacheStats.html), fetched 2026-10-31).
- **Smallest change to the plan**:
  - Make the draft's "cross-library recommendation" (`pr28-luna-cache-spi-draft.md:65`) **the mandatory contract for all three adapters (Guava, Caffeine, and cache2k)**:
    1. Keep one shared transient `ConcurrentHashMap<ResolverCacheKey, CompletableFuture<Optional<R>>>` single-flight coordinator in `xacml-pdp` (using non-callback `putIfAbsent` + `try/finally remove(key, flight)`, matching `DefaultEvaluationContextHandler.java:422-442`).
    2. Use each library strictly as a bounded TTL value store (`getIfPresent`/`put`/`invalidate` in Guava and Caffeine; `peek`/`put`/`remove` in cache2k).
    3. Complete the transient flight with the exact `Optional<AttributeSet>` returned by `loader` (even when `!set.isComplete()`) or with the thrown exception so all concurrent waiters of that flight observe the exact per-attribute outcome, and call `cache.put(key, set)` **only** when `set != null && set.isComplete() && !isExpired(set, context)`.
- **Test that catches it**: `ParallelAttributeResolverTest.onlyACompleteSetIsServedFromTheCache` (`:171-181`) plus a new concurrent test where two threads simultaneously request `"role"` (succeeds) and `"department"` (fails) from a cached parallel resolver and a third thread requests `"role"` after the flight completes (verifying single remote invocation on flight 1, `"role"` returned to thread 1, exception thrown to thread 2, and cache miss on thread 3).

---

### [P1-2] Eager `ResolverCacheKey` computation before `getOrLoad*` causes double dependency resolution and cache poisoning when all context keys are absent, breaks `NoCache` lazy dependency timing, and bypasses `DecisionSets` on dependent resolvers

- **Severity**: P1 (Correctness / Cache Poisoning / Layering with Task 30 `DecisionSets` and D34 `DecisionObserver`)
- **Locations**:
  - Draft: `pr28-luna-cache-spi-draft.md:27, 45-48, 72`
  - Source: `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverContext.java:149-197`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:184-228`
  - Tests: `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointForDecisionTest.java:148-190`, `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointTest.java:206-210`, `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ResolverContextTest.java:116-124`
- **Concrete failure scenarios**:
  1. **Double dependency resolution and cross-decision cache poisoning when all `contextKeyRefs` are absent**:
     - `ResolverCache.getOrLoadAttributes(ResolverCacheKey key, ...)` requires `DefaultPolicyInformationPoint` to evaluate `ResolverCacheKey key = context.getCacheKey()` before calling `getOrLoadAttributes`.
     - In `ResolverContext.Default.getCacheKey()` (`ResolverContext.java:149-166`), `resolveAllContextRefs()` resolves every declared `AttributeReferenceKey` in `getDescriptor().getContextKeyRefs()`. When all declared context keys resolve to `Optional.empty()`, `this.resolvedKeys` is `ImmutableMap.of()` (`isEmpty() == true`) and `this.cacheKey` is memoized as `ResolverCacheKey(resolverId, [])`.
     - On a cache miss, `loader` executes the resolver, which reads its context key via `context.resolveContextRef(depKey)` (`ResolverContext.java:191-197`) or `context.getResolvedKeys()` (`:182-188`). Because `resolvedKeys.isEmpty()` is `true`, line 193 (`if (resolvedKeys == null || resolvedKeys.isEmpty()) resolveAllContextRefs();`) **re-resolves all `contextKeyRefs` a second time during the same resolver invocation**:
       - *Failure mode A (Absent-then-failed dependency)*: If the dependency returned `Optional.empty()` during `getCacheKey()` and throws `XacmlEvaluationException` on the second call inside `loader` (the exact scenario of `DefaultPolicyInformationPointForDecisionTest.aDependencyAbsentThenFailedKeepsTheFirstLookupAbsenceAndTheLaterRetry:164-174`), the first lookup throws `AttributeReferenceEvaluationException` whenever a cache is configured.
       - *Failure mode B (Cross-decision cache poisoning)*: If the dependency returned `Optional.empty()` during `getCacheKey()` and returns `"alice"` on the second call inside `loader`, the resolver resolves Alice's attributes and `getOrLoadAttributes` stores Alice's `AttributeSet` under the pre-computed `key = ResolverCacheKey(resolverId, [])` (the empty-dependency key). Every subsequent decision whose dependency is genuinely absent hits `ResolverCacheKey(resolverId, [])` and receives Alice's attributes.
       - *Failure mode C (D34 observer duplication)*: `DecisionObserver` records two `attributeRequested`/`attributeResolved` events for the absent context key inside a single `resolverStarted` span.
  2. **Broken lazy dependency timing when caching is disabled (`NoCache`)**:
     - When `NoCachePolicyInformationPointCacheProvider` is configured (or `!context.isCacheable()`), `DefaultPolicyInformationPoint` currently does **not** call `context.getCacheKey()` before `invoke(d, context)`; context keys are resolved lazily only when the resolver body calls `context.resolveContextRef(key)` (fixed in Cycle FABLE, `r-FABLE-sol` P2, tested by `DefaultPolicyInformationPointTest.testMatchingResolverFoundAndResultIsCacheable:206-210`). Calling `context.getCacheKey()` unconditionally to pass `ResolverCacheKey` to a no-op `ResolverCache` regresses both `DefaultPolicyInformationPointTest:206-210` and `DefaultPolicyInformationPointForDecisionTest:164-174`.
  3. **Bypassing `DecisionSets` (Task 30) on dependent resolvers**:
     - In `DefaultPolicyInformationPoint.java:187, 226`, `hasKey(context)` is `false` before `getCacheKey()` for any resolver with non-empty `contextKeyRefs`. Under the old SPI, `cache.getAttributes(context)` populated `context.cacheKey`, and line 194 checked `DecisionSets` (`seen`) *before* `invoke(d, context)`. Under fused `getOrLoadAttributes(key, loader)`, if `DecisionSets` is only checked before `getCacheKey()`, a second attribute demand on a dependent resolver in the same decision skips `DecisionSets` and calls `resolverCache.getOrLoadAttributes(key, loader)` first — breaking `theSetOfTheDecisionAnswersASiblingWhenTheCacheFails` (`:178-190`) and `theSetOfADependentResolverServesTheDecisionOnceTheCacheResolvedItsKey` (`:148-162`).
- **Smallest change to the plan**:
  1. In `ResolverContext.Default` (`ResolverContext.java:184, 193`), only retry `resolveAllContextRefs()` when `resolvedKeys.isEmpty()` if `cacheKey == null` (`if (resolvedKeys == null || (resolvedKeys.isEmpty() && cacheKey == null))`). Once `getCacheKey()` has pinned `this.cacheKey` for that `ResolverContext`, `resolveContextRef` and `getResolvedKeys` must read the pinned `this.resolvedKeys` snapshot so the resolver body and its cache key cannot diverge. (When `cacheKey == null`, `ResolverContextTest.contextKeysThatWereAllAbsentAreAskedAgain:116-124` remains intact.)
  2. In `DefaultPolicyInformationPoint`, when the resolver has no cross-decision cache (`NoCache` or `!context.isCacheable()`), do **not** call `context.getCacheKey()` before `invoke(d, context)`; check `DecisionSets` before `invoke` only when `hasKey(context)` is true (no-dependency resolvers) and reconcile with `DecisionSets` after `invoke` if `hasKey(context)` is now true.
  3. When a cross-decision `ResolverCache` is active for a cacheable resolver, resolve `ResolverCacheKey key = context.getCacheKey()` once, check `DecisionSets` (`seen(context, attributeId)`) **after** `getCacheKey()` and **before** `resolverCache.getOrLoadAttributes(key, loader)`, and reconcile the result via `remember(context, candidate, attributeId)`.
- **Test that catches it**: `DefaultPolicyInformationPointForDecisionTest.aDependencyAbsentThenFailedKeepsTheFirstLookupAbsenceAndTheLaterRetry` (`:164-174`) run with Guava/Caffeine/cache2k enabled; `DefaultPolicyInformationPointForDecisionTest:148-190`; `DefaultPolicyInformationPointTest:206-210`.

---

### [P2-1] Nested or cyclic resolver calls inside a D35 `loader` can self-deadlock, AB-BA deadlock across decisions, or throw `IllegalStateException: Recursive update` in Caffeine

- **Severity**: P2 (Concurrency / Deadlock hazard — Hypothesis 1)
- **Locations**:
  - Draft: `pr28-luna-cache-spi-draft.md:27, 72`
  - Source: `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:415-442`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverContext.java:100`
- **Concrete failure scenario**:
  - Resolving `context.getCacheKey()` before entering `getOrLoad*` keeps declared `contextKeyRefs` outside the cache flight. However, `ResolverContext.getEvaluationContext()` is public (`ResolverContext.java:100`), so a custom resolver body executing inside `loader` can still call `context.getEvaluationContext().resolve(otherKey)`.
  - D27's cycle guard (`Frame.inside(ancestry, this)` in `DefaultEvaluationContextHandler.java:426`) only detects re-entry on a flight owned by the **same decision's handler** (`f.handler == this`). It has no visibility into D35 cross-decision cache flights:
    1. *Same-cache re-entry*: If `loader` for `(R1, K1)` triggers an evaluation lookup that resolves another attribute of `(R1, K1)` (before `DecisionSets` records the result), waiting on `(R1, K1)`'s in-flight `CompletableFuture` deadlocks the carrier/caller thread on itself; if implemented with Caffeine `Cache.get(key, fn)`, `ConcurrentHashMap.compute` throws `IllegalStateException: Recursive update` ([Caffeine FAQ — Recursive Computations](https://github.com/ben-manes/caffeine/wiki/Faq), fetched 2026-10-31).
    2. *Cross-decision AB-BA cycle*: If Decision 1's `loader` in `R1` resolves an attribute backed by `R2` while Decision 2's `loader` in `R2` resolves an attribute backed by `R1`, Decision 1 waits on Decision 2's `R2` flight while Decision 2 waits on Decision 1's `R1` flight — an unbreakable cross-decision deadlock.
- **Smallest change to the plan**:
  - Mirror D27's non-waiting re-entry rule in the shared xacml4j D35 flight helper: bind a `ScopedValue<Boolean>` (or `Frame` chain) while executing a D35 `loader`. If a thread is already inside a D35 `loader` when it enters `getOrLoad*`, check `getIfPresent`/`peek`; on a miss, run `loader.get()` directly without joining an in-flight `CompletableFuture` (and store the result via `put` if complete and un-expired).
- **Test that catches it**: A concurrency test with two resolvers `R1` and `R2` whose implementations call `context.getEvaluationContext().resolve(...)` on each other concurrently from two decisions, asserting completion without deadlock or `Recursive update`.

---

### [P2-2] `ResolverCache.getOrLoad*` lacks a hit-vs-load signal required by D34 `DecisionObserver.resolverCacheHit`

- **Severity**: P2 (Observability contract drift with D34)
- **Locations**:
  - Draft: `pr28-luna-cache-spi-draft.md:44-50, 74`
  - Design: `openspec/changes/optimize-pdp-evaluation/design.md:1416-1420`
  - Source: `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:202-204, 391-393`
  - Tests: `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointForDecisionTest.java:226-260`, `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DecisionObserverPdpTest.java:705-815`
- **Concrete failure scenario**:
  - `design.md:1418-1419` defines the exact D34/D35 interaction:
    - *"A hit is a value that this decision did not compute. A load that waits for another decision's load is also a hit."*
    - *"A load that runs the resolver reports no hit: the resolver's own work, and the context keys it reads, nest in the span."*
  - Furthermore, `DefaultPolicyInformationPoint.java:202-204` and `DefaultPolicyInformationPointForDecisionTest.aCachedSetThatLosesToASiblingLookupIsNoCacheHit` (`:226-260`) require that on an attribute cache hit, `o.resolverCacheHit(d, context, selected)` is emitted **only** if `remember(context, candidate, attributeId)` selects `candidate` (`selected == candidate`), and **after** `DecisionSets` reconciliation.
  - Because `ResolverCache.getOrLoadAttributes` returns only `Optional<AttributeSet>`, `DefaultPolicyInformationPoint` cannot tell whether `getOrLoadAttributes` returned a warm cache hit / coalesced waiter hit (`loader` did not run in this decision) or ran `loader` in this decision unless it tracks `loader` invocation.
- **Smallest change to the plan**:
  - Specify in D35 that `DefaultPolicyInformationPoint` wraps `loader` with a per-call boolean flag (`boolean[] loadedByThisCall = {false}`, set to `true` when `loader` begins), and after `getOrLoad*` returns a present value:
    - For attributes: reconcile `AttributeSet selected = remember(context, candidate, attributeId)` first, then emit `o.resolverCacheHit(d, context, selected)` if and only if `!loadedByThisCall[0] && selected == candidate`.
    - For content: emit `o.resolverCacheHit(d, context, content)` if and only if `!loadedByThisCall[0]`.
- **Test that catches it**: `DefaultPolicyInformationPointForDecisionTest.aCachedSetThatLosesToASiblingLookupIsNoCacheHit` (`:226-260`), `DecisionObserverPdpTest` (`:705-815`), and a 2-thread coalesced miss test asserting that the leader records `0` `resolverCacheHit` events and the coalesced waiter records `1` `resolverCacheHit` event.

---

### [P2-3] Relying on library `expireAfterWrite(ttl)` alone ignores `EvaluationContext.getClock()` and `AttributeSet.getTimestamp()` / `ContentRef.getTimestamp()`, extending value lifetime past `timestamp + ttl`

- **Severity**: P2 (TTL correctness — Hypothesis 4)
- **Locations**:
  - Draft: `pr28-luna-cache-spi-draft.md:26, 73`
  - Source: `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/BasePolicyInformationPointCacheProvider.java:57, 73, 95-99`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:280-283`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AttributeSet.java:57`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ContentRef.java:43`
  - Tests: `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/BasePolicyInformationPointCacheProviderTest.java:83-138`, `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPointForDecisionTest.java:125-133`
- **Concrete failure scenario**:
  1. `AttributeSet.getTimestamp()` and `ContentRef.getTimestamp()` are stamped from `EvaluationContext.getClock()` (`AttributeSet.java:57`, `ContentRef.java:43`), or supplied explicitly via `AttributeSet.builder(d).timestamp(ts)` (`AttributeSet.java:155`).
  2. Both `BasePolicyInformationPointCacheProvider.isExpired` (`BasePolicyInformationPointCacheProvider.java:96`) and `DecisionSets.expired` (`DefaultPolicyInformationPoint.java:281`) compute age against `context.getClock().instant()`:
     `Duration.between(timestamp, context.getClock().instant()).compareTo(ttl) > 0`.
  3. By contrast, Guava `expireAfterWrite`, Caffeine `expireAfterWrite`, and cache2k `expireAfterWrite` measure expiry from **cache insertion time** using wall-clock `System.nanoTime()` (`Ticker.systemTicker()` / `TimeReference.DEFAULT`), not from `value.getTimestamp()` and not against the per-decision `EvaluationContext.getClock()`.
  4. If the adapter delegates TTL solely to the library's `expireAfterWrite(ttl)` (`pr28-luna-cache-spi-draft.md:26, 73`):
     - An `AttributeSet` or `ContentRef` created with an older `timestamp` (or produced before a slow network return) and passed to `put` gets a full `ttl` starting at `put` time (`putTime + ttl > timestamp + ttl`), extending its lifetime past its declared `timestamp + ttl` and breaking `BasePolicyInformationPointCacheProviderTest.attributesOlderThanTheTimeToLiveAreNotKept` (`:83-90`) and `contentOlderThanTheTimeToLiveIsNotKept` (`:102-109`).
     - A test or caller that advances `EvaluationContext.getClock()` without sleeping wall-clock `System.nanoTime()` receives stale hits from the cross-decision cache past `timestamp + ttl`.
- **Smallest change to the plan**:
  - Keep the library's fixed `expireAfterWrite(Duration.ofSeconds(config.ttlSeconds()))` for bounded eviction, **and** retain the `BasePolicyInformationPointCacheProvider.isExpired(timestamp, context)` check against `context.getClock().instant()` in the xacml4j cache wrapper on both **admission** (skip `cache.put` if already expired relative to `context.getClock()`) and **lookup** (if `getIfPresent`/`peek` returns an entry whose `getTimestamp()` is expired relative to `context.getClock()`, remove/invalidate it and treat as a cache miss).
- **Test that catches it**: `BasePolicyInformationPointCacheProviderTest` (`:59-138`) and a clock-advancing cross-decision test using a mutable `Clock` on `EvaluationContext`.

---

### [P2-4] Inaccurate cache2k exception-caching claim in the draft vs actual cache2k `2.6.1.Final` documentation

- **Severity**: P2 (Documentation / Technical accuracy — Hypothesis 2)
- **Locations**:
  - Draft: `pr28-luna-cache-spi-draft.md:21, 63, 65, 85`
- **Verification against official cache2k `2.6.1.Final` documentation (fetched 2026-10-31)**:
  - The draft claims (`:21, 63`) that cache2k `2.6.1.Final` read-through loading with `expireAfterWrite` caches or suppresses exceptions unless exception expiry is specially verified.
  - Official cache2k `2.6.1.Final` documentation refutes this as a default:
    - [cache2k User Guide — Resilience](https://cache2k.org/docs/latest/user-guide.html) (fetched 2026-10-31): *"Without a specified `ResiliencePolicy` the cache will only keep successfully loaded values. If a load yields an exception the load is retried immediately when the value is requested again."* Section 9.1.1 (*"Behavior with `expireAfterWrite`"*) applies only *"When enabled"* via `builder.setup(UniversalResiliencePolicy::enable)`.
    - [cache2k `ResiliencePolicy` Javadoc](https://cache2k.org/docs/latest/apidocs/cache2k-api/org/cache2k/io/ResiliencePolicy.html) (fetched 2026-10-31): `ResiliencePolicy.DISABLED_POLICY` (*"A policy always returning zero, thus disabling resilience features"*) *"is also used as default when no resilience policy is set."*
  - Conversely, the draft omits cache2k's real default behavior on `null` (`Optional.empty()`): `permitNullValues` defaults to `false` ([`Cache2kBuilder` Javadoc](https://cache2k.org/docs/latest/apidocs/cache2k-api/org/cache2k/Cache2kBuilder.html), [User Guide §8.7](https://cache2k.org/docs/latest/user-guide.html), fetched 2026-10-31), so returning `null` from a cache2k loader or `computeIfAbsent` throws `NullPointerException` / `CacheLoaderException`, whereas `permitNullValues(true)` caches `null` negatively.
- **Smallest change to the plan**:
  - Update the cache2k notes in D35: state that cache2k `2.6.1.Final` uses `ResiliencePolicy.DISABLED_POLICY` by default (and calling `ResiliencePolicy.disable(builder)` explicitly guards against external XML defaults), and that xacml4j uses the shared transient flight + `peek`/`put` across Guava, Caffeine, and cache2k to handle `Optional.empty()` (without null-loader exceptions or negative caching) and D26 incomplete `AttributeSet`s (P1-1) uniformly.

---

### [P2-5] `ResolverCacheKey` collision when a resolver declares $\ge 2$ optional context keys and a subset is absent, plus compatibility bridge signature gap

- **Severity**: P2 (Key correctness & SPI compatibility — Hypotheses 3 and 5)
- **Locations**:
  - Draft: `pr28-luna-cache-spi-draft.md:27, 35, 44-50, 72, 80`
  - Source: `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverContext.java:163, 218-231`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverCacheKey.java:51-54, 74-77, 108-120`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/PolicyInformationPointCacheProvider.java:29-83`, `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/BasePolicyInformationPointCacheProvider.java:39-100`
  - Tests: `xacml-pdp/src/test/java/org/xacml4j/v30/spi/pip/ResolverContextTest.java:70-86`
- **Concrete failure scenarios**:
  1. **Multi-key subset collision in `ResolverCacheKey` (Hypothesis 3)**:
     - `ResolverContext.Default.resolved(List<AttributeReferenceKey> keys)` (`ResolverContext.java:218-231`) iterates `getDescriptor().getContextKeyRefs()` in declared order and inserts **only present** bags into `ImmutableMap<AttributeReferenceKey, BagOfValues>`:
       `if (value.isPresent()) { contextKeys.put(key, value.get()); }`.
     - `getCacheKey()` (`:163`) then passes `resolvedKeys.values()` — an unkeyed collection of `BagOfValues` with absent slots stripped — to `ResolverCacheKey.of(getDescriptor().getId(), resolvedKeys.values())`.
     - Suppose a resolver declares two optional string context keys `[USER, ROLE]`:
       - Decision 1 has `USER = bag("admin")` and `ROLE = absent`. `resolvedKeys.values()` is `[bag("admin")]`. `getCacheKey()` produces `ResolverCacheKey(id, [bag("admin")])`.
       - Decision 2 has `USER = absent` and `ROLE = bag("admin")`. `resolvedKeys.values()` is **also** `[bag("admin")]`. `getCacheKey()` produces the **exact same** `ResolverCacheKey(id, [bag("admin")])` (`equals == true`, identical `hashCode`)!
       - Decision 2 hits Decision 1's cached `AttributeSet` or coalesces into Decision 1's in-flight load even though a different context key was supplied.
     - Note: `ResolverContextTest.contextKeysKeepTheirDeclaredOrderAndTheAbsentOnesAreLeftOut` (`ResolverContextTest.java:70-86`) currently pins the 1-element `ResolverCacheKey` when `USER` is absent and `ROLE` is `"admin"`.
  2. **Compatibility adapter gap (`Hypothesis 5`)**:
     - The draft (`pr28-luna-cache-spi-draft.md:35`) recommends replacing the `get*`/`put*` pair on `PolicyInformationPointCacheProvider` *"while retaining a documented migration adapter if compatibility requires it"*, and proposes `ResolverCache.getOrLoadAttributes(ResolverCacheKey key, CheckedSupplier<Optional<AttributeSet>> loader)` (`:45-48`).
     - Legacy custom implementations of `PolicyInformationPointCacheProvider` or `BasePolicyInformationPointCacheProvider` implement `getAttributes(ResolverContext)` / `putAttributes(ResolverContext, AttributeSet)` and `getContent(ResolverContext)` / `putContent(ResolverContext, ContentRef)`, which require `ResolverContext` (not just `ResolverCacheKey`, e.g. for `context.getClock()` in `BasePolicyInformationPointCacheProvider.java:96` and `context.getDescriptor()` in `:60, 76`).
     - A migration adapter from legacy `PolicyInformationPointCacheProvider` to `ResolverCache` **cannot be written** if `ResolverCache.getOrLoad*` only receives `ResolverCacheKey` instead of `ResolverContext`!
- **Smallest change to the plan**:
  1. Pass `ResolverContext` (or `(ResolverContext context, ResolverCacheKey key, CheckedSupplier<...> loader)`) to `ResolverCache.getOrLoadAttributes` / `getOrLoadContent`. This simultaneously enables:
     - The `context.getClock()` TTL check on admission and hit (P2-3),
     - A clean default-method bridge on `PolicyInformationPointCacheProvider` / `BasePolicyInformationPointCacheProvider` so existing custom providers and tests (`BasePolicyInformationPointCacheProviderTest`, `PipWorkload.CacheProvider`, etc.) can either migrate or work through the bridge without breaking, and
     - Lazy `context.getCacheKey()` evaluation inside the active cache path (P1-2).
  2. Flag the positional absent-key collision in `ResolverContext.getCacheKey()` (`ResolverContext.java:163, 226`) for maintainer confirmation: either preserve a `null` / empty-bag placeholder per declared slot in `ResolverCacheKey.keys` (updating `ResolverContextTest:83`) or explicitly document that `ResolverCacheKey` omits absent context keys per `ResolverContextTest:83`.

---

### [P3-1] Lifecycle and ownership of per-resolver caches across `DefaultPolicyInformationPoint.forDecision()` and `Cache.close()` for cache2k

- **Severity**: P3 (Resource lifecycle — Hypothesis 6)
- **Locations**:
  - Draft: `pr28-luna-cache-spi-draft.md:40-50, 77`
  - Source: `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:74-80`
- **Concrete failure scenario**:
  1. `DefaultPolicyInformationPoint.forDecision()` (`DefaultPolicyInformationPoint.java:74-80`) creates a lightweight per-decision `DefaultPolicyInformationPoint(this)` wrapper that shares `this.id`, `this.registry`, and `this.cache` with the root PIP. If per-resolver `ResolverCache` instances were created inside the per-decision wrapper rather than once in the root PIP (or cached by `ResolverDescriptor` in the root PIP), every decision would allocate fresh caches and never share entries across decisions!
  2. In cache2k `2.6.1.Final`, `org.cache2k.Cache` implements `Closeable` (`Cache.close()` releases internal scheduler tasks and JMX registrations; [`Cache.html`](https://cache2k.org/docs/latest/apidocs/cache2k-api/org/cache2k/Cache.html), fetched 2026-10-31). If `ResolverCache` and `PolicyInformationPointCacheProvider` (or `PolicyInformationPoint`) have no `close()` hook (`default void close() {}` / `AutoCloseable`), tests and applications that create and discard multiple cache2k-backed PIP instances leak cache2k instances or collide on cache2k manager names if names are set.
- **Smallest change to the plan**:
  - Specify that per-resolver `ResolverCache` instances are created once per registered cacheable `ResolverDescriptor` when the root `DefaultPolicyInformationPoint` is built (stored in an immutable `Map<ResolverDescriptor<?>, ResolverCache>` shared across all `forDecision()` views), that cache2k builders remain anonymous (no fixed `.name(...)` collision across PIP instances), and that `ResolverCache` and `PolicyInformationPointCacheProvider` include `default void close() {}` (`AutoCloseable`) forwarded to `org.cache2k.Cache.close()`.

---

### [P3-2] Dual-method `ResolverCache` (`getOrLoadAttributes` + `getOrLoadContent`) vs per-descriptor typed cache allocation

- **Severity**: P3 (API / Footprint hygiene)
- **Locations**:
  - Draft: `pr28-luna-cache-spi-draft.md:40-50`
  - Source: `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverDescriptor.java:42`, `AttributeResolverDescriptor.java:42`, `ContentResolverDescriptor.java:32`
- **Concrete failure scenario**:
  - `PolicyInformationPointCacheProvider.create(ResolverDescriptor<?> resolver, ResolverCacheConfig config)` is called once per `ResolverDescriptor<?>`. Every descriptor is either an `AttributeResolverDescriptor` (`ResolverDescriptor<AttributeSet>`) or a `ContentResolverDescriptor` (`ResolverDescriptor<ContentRef>`), never both.
  - Giving `ResolverCache` both `getOrLoadAttributes` and `getOrLoadContent` forces each per-resolver adapter instance to either allocate two underlying cache instances (`Cache<ResolverCacheKey, AttributeSet>` and `Cache<ResolverCacheKey, ContentRef>`, one of which is permanently unused) or use an untyped `Cache<ResolverCacheKey, Object>` with unchecked casts.
- **Smallest change to the plan**:
  - Either parameterize `ResolverCache<R>` (`<R> ResolverCache<R> create(ResolverDescriptor<R> resolver, ResolverCacheConfig config)`) with a single `Optional<R> getOrLoad(...)` method (keeping an internal predicate `Predicate<R> isCacheableValue` so `AttributeSet::isComplete` is checked for attributes and `true` for `ContentRef`), or have `create(...)` inspect `resolver instanceof AttributeResolverDescriptor` vs `ContentResolverDescriptor` to allocate only the single matching underlying cache.

---

## 3. Evaluation of the Six Task Hypotheses

### Hypothesis 1: Layering with D27 and Task 30 (`DecisionSets`)
- *Can a flight deadlock, nest, or share a `ResolverContext` or `EvaluationContext` across decisions?*
  - **Confirmed (`P1-2`, `P2-1`)**:
    1. **Sharing `ResolverContext` / `EvaluationContext` across decisions**: A coalesced waiter in Decision 2 does **not** run its own `loader`; it receives the `Optional<AttributeSet>` or `Optional<ContentRef>` produced by Decision 1's `loader` (`DefaultPolicyInformationPoint.java:149, 252`). Neither `AttributeSet` nor `ContentRef` retains a reference to `ResolverContext` or `EvaluationContext` (`AttributeSet.java:43-58` holds `ResolverDescriptor`, `ImmutableMap<String, BagOfAttributeExp>`, `ImmutableMap<String, XacmlEvaluationException>`, and `Instant timestamp`; `ContentRef.java:34-44` holds `ContentResolverDescriptor`, `Node`, and `Instant timestamp`). Thus the cached/coalesced return value does not leak `ResolverContext` or `EvaluationContext` across decisions (`SUPPORTED`).
    2. **What happens when a resolver's dependency keys are resolved?** Declared `contextKeyRefs` are resolved in `context.getCacheKey()` (`ResolverContext.java:155-165`). However, because `ResolverContext.Default.resolveContextRef` re-runs `resolveAllContextRefs()` when `resolvedKeys.isEmpty()` (`ResolverContext.java:193`), a resolver whose declared dependencies are all absent resolves them **twice** (once before `getOrLoad*` and once inside `loader`), risking absent-then-failed errors and cache-key poisoning (`P1-2`).
    3. **Can a flight nest or deadlock?** Yes (`P2-1`): if a custom resolver calls `context.getEvaluationContext().resolve(otherKey)` inside `loader`, D27's `Frame.inside(ancestry, this)` (`DefaultEvaluationContextHandler.java:426`) only skips waiting on flights of the *same* decision's handler (`f.handler == this`). A nested lookup that hits the same `(resolver, key)` or a cross-decision `R1 <-> R2` cycle will deadlock (or throw `IllegalStateException: Recursive update` in Caffeine `Cache.get(key, fn)`).
    4. **Layering with Task 30 (`DecisionSets`)**: For resolvers with `contextKeyRefs`, `hasKey(context)` is `false` until `context.getCacheKey()` runs (`DefaultPolicyInformationPoint.java:187, 226`). `DecisionSets` must be checked **after** `context.getCacheKey()` and **before** `resolverCache.getOrLoadAttributes` (`P1-2`).

### Hypothesis 2: Failure Semantics & Official Library Documentation Verification
- *Does the proposed adapter contract really keep failures, timeouts, incomplete sets, and absence out of every library, including a waiter that joins a flight that then fails?*
  - **Refuted for native `LoadingCache.get` / `Cache.get(key, fn)` (`P1-1`), Confirmed for the shared xacml4j transient flight + `getIfPresent`/`peek` + conditional `put` pattern**:
    - D26 incomplete `AttributeSet`s (`!set.isComplete()`) must be returned to the flight's caller(s) as `Optional.of(incompleteSet)` without being stored in the cache (`DefaultPolicyInformationPoint.java:151-160, 207-212`; `ParallelAttributeResolverTest.java:131-140, 171-181`). Native read-through loaders (`LoadingCache.get`, Caffeine `Cache.get(key, fn)`, `computeIfAbsent`) cannot return a non-null `incompleteSet` without caching it (`P1-1`).
    - In Caffeine `3.3.1`, `Cache.get(key, fn)` does **not** coalesce concurrent waiters when `fn` returns `null` (absence) or throws an exception: blocked waiters retry `fn` sequentially one by one ([Caffeine FAQ](https://github.com/ben-manes/caffeine/wiki/Faq), fetched 2026-10-31).
    - Under the shared xacml4j transient `CompletableFuture` flight table + `getIfPresent`/`peek` + `put` (only when `isPresent() && isComplete() && !isExpired()`), a waiter joining a flight that fails, times out, returns `Optional.empty()`, or returns an incomplete `AttributeSet` receives the exact outcome of that flight, the flight is removed in `finally`, and nothing is written to Guava, Caffeine, or cache2k (`SUPPORTED`).
- *Check the draft's cache2k claim against its official documentation:*
  - **Refuted as a default (`P2-4`)**: Official cache2k `2.6.1.Final` documentation states that `ResiliencePolicy.DISABLED_POLICY` is the default when no resilience policy is configured, so uncaught loader exceptions are **not** cached or suppressed by default ([cache2k User Guide §9](https://cache2k.org/docs/latest/user-guide.html), [`ResiliencePolicy` Javadoc](https://cache2k.org/docs/latest/apidocs/cache2k-api/org/cache2k/io/ResiliencePolicy.html), fetched 2026-10-31). Meanwhile, cache2k defaults to `permitNullValues(false)` ([`Cache2kBuilder` Javadoc](https://cache2k.org/docs/latest/apidocs/cache2k-api/org/cache2k/Cache2kBuilder.html), fetched 2026-10-31).

### Hypothesis 3: Key Correctness (`ResolverCacheKey`)
- *Does anything in `ResolverCacheKey` hold a per-decision object?*
  - **Refuted by source (`SUPPORTED`)**: `ResolverCacheKey` (`ResolverCacheKey.java:51-54`) holds only `String resolverId`, `BagOfValues[] keys`, and `int hashCode`. `BagOfValues` holds immutable `Value` elements. No `EvaluationContext` or `ResolverContext` reference is retained.
- *Can one request produce unequal keys, or two different requests with different results produce equal keys?*
  - **One request producing unequal keys**: Within a single `ResolverContext`, `getCacheKey()` memoizes `this.cacheKey` (`ResolverContext.java:154-165`), and `resolveAllContextRefs()` iterates `getDescriptor().getContextKeyRefs()` in deterministic list order (`:222`). However, if all context keys are absent on the first call and `resolveContextRef` re-evaluates them on the second call (`P1-2`), `this.resolvedKeys` can change after `this.cacheKey` was pinned. Fixing `P1-2` prevents that divergence.
  - **Two different requests producing equal keys (`P2-5`)**: When a resolver declares $\ge 2$ optional `contextKeyRefs` (e.g., `[USER, ROLE]`), `ResolverContext.Default.resolved(...)` (`ResolverContext.java:226-228`) omits absent keys from `resolvedKeys`, and `getCacheKey()` (`:163`) builds `ResolverCacheKey` from `resolvedKeys.values()` without key names or positional placeholders. Thus `(USER="admin", ROLE=absent)` and `(USER=absent, ROLE="admin")` both produce `ResolverCacheKey(resolverId, [bag("admin")])` (`ResolverContextTest.java:70-86`).

### Hypothesis 4: TTL Semantics
- *Can a hit extend a value past its declared lifetime, given the original timestamp rule? Is one fixed expiry per cache consistent with the descriptor TTL and with the age check in `BasePolicyInformationPointCacheProvider`?*
  - **Confirmed (`P2-3`)**:
    - One fixed TTL per per-resolver cache matches `ResolverDescriptor.getPreferredCacheTTL()` (`BaseResolverDescriptor.java:70-72`), which is fixed per descriptor.
    - Because Guava, Caffeine, and cache2k `expireAfterWrite` is non-sliding (unlike `expireAfterAccess`), a cache **read** does not reset the library's write timer.
    - **However**, `BasePolicyInformationPointCacheProvider.isExpired` (`BasePolicyInformationPointCacheProvider.java:95-99`) and `DecisionSets.expired` (`DefaultPolicyInformationPoint.java:280-283`) measure age from `value.getTimestamp()` against `context.getClock().instant()`, whereas library `expireAfterWrite(ttl)` measures from cache insertion time using wall-clock `System.nanoTime()`. Without retaining the `isExpired(value.getTimestamp(), context)` check on admission and hit, an `AttributeSet`/`ContentRef` constructed with an earlier timestamp (or after a slow load) lives until `putTime + ttl > timestamp + ttl`, and tests using a synthetic `EvaluationContext.getClock()` observe expired values (`P2-3`).

### Hypothesis 5: Compatibility & Public Surface Inventory
- *Which public types change, and is the replace-or-keep recommendation the right one for existing custom providers?*
  - **Inventory of affected public types (`SUPPORTED` via `rtk rg` and `ripwire --uses`)**:
    1. `org.xacml4j.v30.spi.pip.PolicyInformationPointCacheProvider` (`PolicyInformationPointCacheProvider.java:29`) and nested `NoCachePolicyInformationPointCacheProvider` (`:66`).
    2. `org.xacml4j.v30.spi.pip.BasePolicyInformationPointCacheProvider` (`BasePolicyInformationPointCacheProvider.java:39`).
    3. `org.xacml4j.v30.spi.pip.PolicyInformationPoint.Builder` (`PolicyInformationPoint.java:137-146`).
    4. `org.xacml4j.v30.spi.pip.ResolverDescriptor` and `ResolverDescriptor.BaseBuilder` (`ResolverDescriptor.java:42, 97`).
  - **In-repository callers and implementations**:
    - Production: `DefaultPolicyInformationPoint.java:56, 192, 211, 390, 398`, `PolicyInformationPoint.java:122, 138, 144`.
    - Benchmarks: `xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/PipWorkload.java:274` (`PipWorkload.CacheProvider`).
    - Tests: `DefaultPolicyInformationPointTest.java`, `DefaultPolicyInformationPointForDecisionTest.java`, `ParallelAttributeResolverTest.java`, `BasePolicyInformationPointCacheProviderTest.java`, `ResolverCacheKeyTest.java`, `DecisionObserverPdpTest.java`.
  - **Assessment (`P2-5`)**: Replacing `PolicyInformationPointCacheProvider` in place by adding `create(ResolverDescriptor<?>, ResolverCacheConfig)` while keeping a default bridge (or deprecating `get*`/`put*` and `BasePolicyInformationPointCacheProvider` with a bridge) is the right compatibility strategy, **provided** `ResolverCache.getOrLoad*` receives `ResolverContext` so the bridge can call legacy `getAttributes(ResolverContext)` / `putAttributes(ResolverContext, AttributeSet)` and `BasePolicyInformationPointCacheProvider.isExpired(..., context)`.

### Hypothesis 6: Missing Maintainer Decisions Before Implementation
- **Confirmed (`P1-2`, `P2-5`, `P3-1`, `P3-2`)**:
  1. **Multi-key optional `contextKeyRefs` representation in `ResolverCacheKey` (`P2-5`)**: Should `ResolverCacheKey` preserve positional `null`/empty-bag slots when a resolver declares $\ge 2$ context keys and a subset is absent (fixing the `[USER="admin", ROLE=absent]` vs `[USER=absent, ROLE="admin"]` collision and updating `ResolverContextTest:83`), or keep the legacy present-values-only array?
  2. **Legacy `get*`/`put*` bridge vs clean removal (`P2-5`)**: Should `PolicyInformationPointCacheProvider` keep deprecated default `get*`/`put*` methods bridged via `ResolverContext` (preserving `BasePolicyInformationPointCacheProvider` and custom providers), or remove `get*`/`put*` and `BasePolicyInformationPointCacheProvider` outright and migrate the 6 test classes and `PipWorkload`?
  3. **`Closeable` lifecycle (`P3-1`)**: Confirm whether `PolicyInformationPointCacheProvider` and `ResolverCache` should implement `AutoCloseable` (`default void close() {}`) so `cache2k` `Cache.close()` is reachable.

---

## 4. Official Library Documentation Verification Table

All external library claims were verified via `tavily_extract` on **2026-10-31** (`SUPPORTED`):

| Library & Version | Claim / Behavior Checked | Official Source URL (Fetched 2026-10-31) | Verified Behavior |
| --- | --- | --- | --- |
| **Guava `33.7.2-jre`** | `Cache.get(K, Callable)` / `LoadingCache.get(K)` null & exception rules | `https://guava.dev/releases/33.7.2-jre/api/docs/com/google/common/cache/Cache.html`, `https://guava.dev/releases/33.7.2-jre/api/docs/com/google/common/cache/CacheLoader.html` | Observable cache state is never modified if `valueLoader` throws; returning `null` throws `InvalidCacheLoadException`. Concurrent calls on the same key share one load. |
| **Guava `33.7.2-jre`** | `CacheStats.loadExceptionCount()` on sentinel exceptions | `https://guava.dev/releases/33.7.2-jre/api/docs/com/google/common/cache/CacheStats.html`, `https://github.com/google/guava/wiki/CachesExplained` | *"if the `CacheLoader` throws an exception, the cache entry is not created, and the exception is propagated to the caller... `loadExceptionCount()` returns the number of times `Cache` lookup methods threw an exception"* — using a thrown sentinel for `Optional.empty()` pollutes `loadExceptionCount`. |
| **Caffeine `3.3.1`** | `Cache.get(K, Function)` null & exception behavior under contention | `https://github.com/ben-manes/caffeine/wiki/Population`, `https://github.com/ben-manes/caffeine/wiki/Faq` | Returning `null` inserts no mapping (`get` returns `null`), and thrown exceptions propagate without caching. **However**, per the FAQ (*"Why does `Cache.get(key, function)` block other threads when computing a value?"*), `Cache.get` runs inside `ConcurrentHashMap.compute`; if the function returns `null` or throws, waiting threads on that key are **not** coalesced into that result and instead retry sequentially one by one. Recursive same-key calls throw `IllegalStateException: Recursive update`. |
| **Caffeine `3.3.1`** | `Caffeine.recordStats()` opt-in | `https://github.com/ben-manes/caffeine/wiki/Statistics` | Stats recording is opt-in via `Caffeine.newBuilder().recordStats()`. |
| **cache2k `2.6.1.Final`** | Default exception caching (`ResiliencePolicy`) | `https://cache2k.org/docs/latest/user-guide.html`, `https://cache2k.org/docs/latest/apidocs/cache2k-api/org/cache2k/io/ResiliencePolicy.html` | *"Without a specified `ResiliencePolicy` the cache will only keep successfully loaded values. If a load yields an exception the load is retried immediately when the value is requested again."* `ResiliencePolicy.DISABLED_POLICY` is the default when no resilience policy is set. `ResiliencePolicy.disable(builder)` explicitly disables resilience. |
| **cache2k `2.6.1.Final`** | Null values & `Cache.close()` lifecycle | `https://cache2k.org/docs/latest/apidocs/cache2k-api/org/cache2k/Cache2kBuilder.html`, `https://cache2k.org/docs/latest/apidocs/cache2k-api/org/cache2k/Cache.html` | `permitNullValues` defaults to `false` (loader returning `null` throws `CacheLoaderException` / `NullPointerException`). `org.cache2k.Cache` implements `Closeable` (`close()` frees resources). |

---

## 5. Verification Coverage & Unverified Items

- **Verified (`SUPPORTED`)**:
  - Opened and inspected all PIP, cache, resolver context, descriptor, evaluation context handler, and JMH corpus sources and tests in `PROJECT_ROOT`.
  - Ran `rtk rg` and `ripwire --uses` across `PROJECT_ROOT` to inventory all callers and implementations of `PolicyInformationPointCacheProvider`, `BasePolicyInformationPointCacheProvider`, and `ResolverCacheKey`.
  - Fetched and verified official documentation for Guava `33.7.2-jre`, Caffeine `3.3.1`, and cache2k `2.6.1.Final` on `2026-10-31`.
- **Unverified (`UNVERIFIED` per read-only plan-review constraints)**:
  - No Maven builds, unit tests, conformance suites, or JMH benchmarks were executed in this review session (prohibited by the read-only task packet).
  - Runtime behavior of Caffeine `3.3.1` and cache2k `2.6.1.Final` on JDK 25 / JDK 28 preview (`--enable-preview`) was not executed at runtime (`UNVERIFIED`).
