FINAL REPORT (agyx_1, Gemini 4 Argon)

## Verdict

**REJECT** the 4-type `CompletionStage` public SPI in `REPORT-D26-astra.md` (`ResolverInput`, sealed `AttributeResolution`, `AsyncAttributeResolver`, `AttributeResolutionBatch`, `buildAsync`, `getAsyncResolver`) as drafted.
- **Primary recommendation**: **Defer D26** until D27 (`design.md:688-728`) and D28 (`design.md:730-745`) are measured. With D27's `ResolverContext` confinement fix and `PolicyInformationPoint.forDecision()` whole-`AttributeSet` memo (`design.md:704-728`), existing synchronous descriptors already support parallel backend I/O inside `Function<ResolverContext, Map<String, BagOfValues>>` (`AttributeResolverDescriptor.java:286`) with zero public SPI additions.
- **If an engine-managed concurrency SPI is still wanted now (`APPROVE WITH CHANGES`)**: Replace the 4 new public types and `CompletionStage` plumbing with the minimal, `StructuredTaskScope`-ready callable/timeout seam shown in Section 2 below, preserving `ResolverDescriptor.getResolverFunction()` and D27's `AttributeSet` caching.

---

## 1. Is D26 Needed at All? What Does It Add Beyond Synchronous Fan-Out + D27?

Both research reports (`pr9-sol-concurrent-resolution-code.md:5, 54` and `pr10-astra-concurrent-resolution-jdk.md:7, 66`) and D28 (`design.md:736`) already establish that an existing resolver descriptor:
```java
AttributeResolverDescriptor.builder("id", "name", category)
    .attribute("role", XacmlTypes.STRING)
    .attribute("department", XacmlTypes.STRING)
    .contextKeys(keys)
    .build(ctx -> ...); // Map<String, BagOfValues>
```
can resolve its context keys once on the evaluation thread (enforced by D27, `design.md:704-709`), fan out blocking backend calls concurrently on virtual threads inside the callback, and return `Map<String, BagOfValues>`. Furthermore, D27's approved `PolicyInformationPoint.forDecision()` (`design.md:724-728`, `pr11-fable-concurrent-resolution-literature.md:45`) memoizes that returned `AttributeSet` per decision so sibling attributes (`role`, `department`) are fetched in **one** batch and served on demand without polluting `getResolvedDesignators()`.

**What D26 adds beyond synchronous `Map<String, BagOfValues>` + D27:**
1. **Engine-enforced admission (`Semaphore`) and deadline (`Duration`)** around a resolver invocation, mapping timeout/cancellation to `AttributeReferenceEvaluationException.forResolverFailure` (`processing-error`) and falling back to the next candidate resolver in `DefaultPolicyInformationPoint.java:72`.
2. **Per-attribute failure isolation within a multi-attribute resolver**: if one descriptor provides both `role` and `department` from separate backend calls and `department` fails while the policy only evaluates `role`, D26's per-attribute `AttributeResolution` avoids failing `role`.

**Why (2) does not justify the 4-type `CompletionStage` SPI:**
- If `role` and `department` come from **one** backend call (e.g., one SQL/LDAP/HTTP profile fetch), their I/O fate is shared anyway; per-attribute `CompletionStage<AttributeResolution>` and per-attribute cancellation add ceremony for a single network round-trip.
- If `role` and `department` come from **separate** backend calls with independent failure modes, XACML 3.0 and `xacml4j` already model them as **two separate `AttributeResolverDescriptor`s** (each invoked only when its attribute is demanded, with its own cache TTL and fallback precedence).
- Worse, because Astra's D26 forces `.noCache()` (`REPORT-D26-astra.md:111`) and immediately cancels sibling stages when the first requested attribute completes (`REPORT-D26-astra.md:88-89, 102`), a policy that reads `role` and then `department` from the same 2-attribute resolver will **abort `department` on the first lookup and re-run the entire resolver on the second lookup**—doing *more* backend work than a synchronous resolver under D27's `forDecision()` memo.

---

## 2. Public API Shape Review & Simpler Idiomatic Shape

### Critique of Astra's Proposed Public API (`REPORT-D26-astra.md:14-55`)
1. **Four new public ceremonial types (`ResolverInput`, `AttributeResolution`, `AsyncAttributeResolver`, `AttributeResolutionBatch`) violate `xacml4j`'s minimal fluent builder style**:
   - In `xacml-pdp` (`AttributeResolverDescriptor.java:220-289`, `ContentResolverDescriptor.java:53-86`), descriptors take a single `@FunctionalInterface` (`Function<ResolverContext, Map<String, BagOfValues>>` or `Function<ResolverContext, ContentRef>`) and wrap it into `Function<ResolverContext, Optional<AttributeSet>>`.
   - `AttributeResolutionBatch` (`REPORT-D26-astra.md:31-48`) forces providers to construct a `Map<String, CompletionStage<AttributeResolution>>` plus a manual `Runnable cancellation` hook because `CompletableFuture.cancel(true)` does not interrupt blocking threads (`pr10-astra-concurrent-resolution-jdk.md:37`).
2. **Hostile to `StructuredTaskScope` (JDK 28, JEP 543)**:
   - `StructuredTaskScope` is **lexical**: subtasks are forked as `Callable`s inside `try (var scope = StructuredTaskScope.open(...))`, joined in the same scope, and interrupted automatically on scope exit.
   - A public SPI whose callback returns detached `Map<String, CompletionStage<AttributeResolution>>` (`REPORT-D26-astra.md:28, 32`) **cannot** be backed by `StructuredTaskScope` without keeping `CompletableFuture` bridges forever, because the callback returns before joining.
3. **Un-idiomatic `resolutionExecutor(Supplier<? extends ExecutorService> factory)` (`REPORT-D26-astra.md:54, 99`)**:
   - Builders in `xacml4j` take configured instances or providers, not a factory that instantiates and closes a fresh `ExecutorService` on every candidate resolver invocation.

### Simpler, Idiomatic `xacml4j` Code Shape

Keep `ResolverContext` (already confined and pre-populated on the owner thread by D27, `design.md:704-709`) and `AttributeSet` (already cached across sibling attributes by `PolicyInformationPoint` and D27's `forDecision()`, `design.md:724-728`).

If engine-managed parallel attribute fetchers and resolver deadlines are wanted, add:
1. **Per-attribute callable registration on `AttributeResolverDescriptor.Builder`** (no new public wrapper types; runs lexically inside PIP's structured runner):
```java
// AttributeResolverDescriptor.java — additions to Builder
public Builder attribute(
        String id,
        DataType type,
        Function<ResolverContext, BagOfValues> attributeResolver) {
    attributes.put(id, AttributeDescriptor.of(id, type));
    attributeResolvers.put(id, Objects.requireNonNull(attributeResolver));
    return this;
}

public AttributeResolverDescriptor buildParallel() {
    Preconditions.checkState(!attributeResolvers.isEmpty(),
        "At least one attribute resolver function must be specified");
    Preconditions.checkState(
        attributeResolvers.keySet().equals(attributes.keySet()),
        "Every declared attribute must have a resolver function");
    ImmutableMap<String, Function<ResolverContext, BagOfValues>> tasks =
        ImmutableMap.copyOf(attributeResolvers);
    return new AttributeResolverDescriptorImpl(this, tasks);
}
```
2. **Timeout and concurrency limit on `PolicyInformationPoint.Builder`** (`PolicyInformationPoint.java:88-133`):
```java
// PolicyInformationPoint.Builder<T>
public T resolverTimeout(Duration timeout);
public T maxConcurrentResolutions(int maxConcurrent);
public T resolverExecutor(Executor executor); // optional; default virtual threads
```
3. **Internal execution seam inside `DefaultPolicyInformationPoint` (`DefaultPolicyInformationPoint.java:72-99`)**:
```java
@FunctionalInterface
interface StructuredResolverRunner {
    <T> List<T> runAll(
        ResolverContext context,
        Collection<? extends Callable<T>> tasks,
        Duration timeout) throws Exception;
}
```
- **JDK 25 implementation (no preview)**: Acquires a permit from the PIP's `Semaphore(maxConcurrent)` per task (released in the worker's `finally` block), captures `MDC.getCopyOfContextMap()`, submits tasks to a shared virtual-thread executor (`Executors.newVirtualThreadPerTaskExecutor()`), awaits completion up to `timeout`, and on failure/timeout calls `Future.cancel(true)` on remaining futures without blocking the evaluation thread on unresponsive I/O.
- **JDK 28 swap (`StructuredTaskScope`, JEP 543)**: Replaces the body of `StructuredResolverRunner.runAll` with `try (var scope = StructuredTaskScope.open(Joiner.allSuccessfulOrThrow(), cf -> cf.withTimeout(timeout)))`—zero public API changes.
- **Single-call async/blocking resolvers**: Existing `.build(Function<ResolverContext, Map<String, BagOfValues>>)` (`AttributeResolverDescriptor.java:286`) automatically gets the PIP's `resolverTimeout` and `Semaphore` admission when executed through `StructuredResolverRunner`, while remaining 100% compatible with `ResolverDescriptor.getResolverFunction()` and D27's `forDecision()` memo.

---

## 3. Findings (Severity-Ordered, P1 to P3)

### [P1-1] `getResolverFunction()` throwing `UnsupportedOperationException` breaks `ResolverDescriptor` and `Resolver`
- **Location**: `REPORT-D26-astra.md:82`; compare `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverDescriptor.java:64`, `AttributeResolverDescriptor.java:129`, `Resolver.java:67-70`.
- **Failure scenario**: `AttributeResolverDescriptor` implements `ResolverDescriptor<AttributeSet>`, whose core contract is `Function<ResolverContext, Optional<AttributeSet>> getResolverFunction()`. `Resolver.resolve(ResolverContext)` (`Resolver.java:67-70`) and existing tests invoke `descriptor.getResolverFunction().apply(context)`. Astra's draft makes `getResolverFunction()` return a non-null function that throws `UnsupportedOperationException` whenever `buildAsync` is used, and forces callers to check a new side-channel `Optional<AsyncAttributeResolver> getAsyncResolver()`. Any code using the `ResolverDescriptor` or `Resolver` abstraction throws `UnsupportedOperationException` at runtime.
- **Smallest fix**: Do not add `getAsyncResolver()` or throw `UnsupportedOperationException` from `getResolverFunction()`. Keep `getResolverFunction()` working for all `AttributeResolverDescriptor` instances by adapting parallel/async execution inside the descriptor/PIP seam.
- **Test that catches it**: Build a descriptor via the parallel/async builder, call `descriptor.getResolverFunction().apply(context)` and `new Resolver<>(descriptor).resolve(context)`, and assert it returns `Optional<AttributeSet>` instead of throwing `UnsupportedOperationException`.

### [P1-2] Mandatory `.noCache()` + cancelling sibling attributes on first attribute completion defeats multi-attribute batching and conflicts with D27
- **Location**: `REPORT-D26-astra.md:88-89, 102, 111`; compare `openspec/changes/optimize-pdp-evaluation/design.md:711, 724-728` (D27) and `DefaultPolicyInformationPoint.java:74-96`.
- **Failure scenario**: Suppose an async descriptor declares `role` and `department`. Rule 1 requests `role`; Rule 2 requests `department`. Under Astra's D26:
  1. `.noCache()` is forced (`REPORT-D26-astra.md:111`), even though D27 (`design.md:711`) already fixes `ResolverCacheKey.hashCode` (`policy4j-3huw`, `ResolverCacheKey.java:63, 78`) and adds `PolicyInformationPoint.forDecision()` (`design.md:724-728`) to memoize whole `AttributeSet`s per decision.
  2. When Rule 1 requests `role`, D26 awaits only `role`'s stage and **immediately cancels `department`'s in-flight stage and closes the executor** (`REPORT-D26-astra.md:88-89, 102`).
  3. When Rule 2 requests `department` microseconds later, the PIP must invoke the resolver a **second** time from scratch.
- **Smallest fix**: Return an `AttributeSet` (or cache completed sibling attributes in D27's decision-scoped `forDecision()` memo and `PolicyInformationPointCacheProvider` when `isCacheable()`), and do not forbid caching on parallel/async descriptors.
- **Test that catches it**: Register a 2-attribute resolver (`role`, `department`) with an invocation counter (`AtomicInteger`). Evaluate a policy that references both `role` and `department` in the same decision. Assert the resolver is invoked **once**, not twice, and `getResolvedDesignators()` contains only the attributes demanded by the policy.

### [P1-3] Per-invocation `ExecutorService.close()` on the evaluation thread blocks timeout fallback when a worker ignores interruption
- **Location**: `REPORT-D26-astra.md:89-90, 99, 102-103`; compare `DefaultPolicyInformationPoint.java:72-99` and `pr10-astra-concurrent-resolution-jdk.md:32, 36`.
- **Failure scenario**: Astra's draft states (`line 99`): *"Create a fresh service for each asynchronous candidate invocation and close it there"* and (`line 89-90`): *"finish execution cleanup before returning a bag or visiting another candidate"*, while also claiming (`line 103`): *"A timeout bounds the resolution wait, not guaranteed thread termination."* Those two statements are contradictory: `Executors.newVirtualThreadPerTaskExecutor().close()` blocks unconditionally until every submitted task terminates (`pr10-astra-concurrent-resolution-jdk.md:36`). If Candidate Resolver 1 hangs on an uninterruptible socket read and times out after 50 ms, calling `executor.close()` before visiting Candidate Resolver 2 stalls the evaluation thread forever, so Candidate Resolver 2 is never reached and the timeout never takes effect.
- **Smallest fix**: Do not create and `close()` an `ExecutorService` per candidate invocation on the evaluation thread. Share a PIP-owned virtual-thread executor; bound concurrency with a PIP-owned `Semaphore` whose permit is released in the worker task's `finally` block (so runaway workers hold their permit until they actually exit); on timeout on the evaluation thread, call `future.cancel(true)` and immediately proceed to the next fallback candidate in `DefaultPolicyInformationPoint.java:72`.
- **Test that catches it**: Configure a 50 ms timeout and two candidate resolvers for the same attribute: Resolver 1 blocks on a `CountDownLatch` that ignores `InterruptedException` for 5 seconds; Resolver 2 returns `"permit-role"` immediately. Assert `pip.resolveAttributeDesignator(...)` returns `"permit-role"` in well under 500 ms (and release the latch in test `@AfterEach`).

### [P2-1] Public `CompletionStage` + manual `Runnable cancellation` SPI prevents a clean JDK 28 `StructuredTaskScope` migration
- **Location**: `REPORT-D26-astra.md:27-48, 97-105`; compare `openspec/changes/optimize-pdp-evaluation/design.md:684` and `pr9-sol-concurrent-resolution-code.md:61-70`.
- **Failure scenario**: D26's goal (`design.md:684`) is to hide task execution behind an internal seam replaceable by `StructuredTaskScope` once final in JDK 28 (JEP 543). Exposing `Map<String, CompletionStage<AttributeResolution>>` and `Executor` in the public `AsyncAttributeResolver` signature pushes unstructured `CompletableFuture` composition into every provider implementation and cannot be executed as `StructuredTaskScope.fork(Callable)` subtasks without breaking or deprecating the public SPI in JDK 28.
- **Smallest fix**: Keep task fork/join inside `DefaultPolicyInformationPoint` over synchronous/blocking `Function<ResolverContext, ...>` / `Callable` units run on virtual threads (Section 2 above).
- **Test that catches it**: Swap the internal `StructuredResolverRunner` implementation in a unit test without modifying any resolver descriptor or public PIP type.

### [P2-2] `ResolverInput` duplicates `ResolverContext` with a divergent map contract and leaks `requestedKey`
- **Location**: `REPORT-D26-astra.md:15-19, 75, 86`; compare `xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverContext.java:71-147`.
- **Failure scenario**:
  1. `ResolverContext.getResolvedKeys()` (`ResolverContext.java:93, 141`) returns `Map<AttributeReferenceKey, BagOfValues>` where absent/empty context refs are omitted from the map and `resolveContextRef(key)` returns `Optional<BagOfValues>` (`ResolverContext.java:114`). `ResolverInput` instead exposes `Map<AttributeReferenceKey, Optional<BagOfValues>>`, creating two incompatible context representations in `org.xacml4j.v30.spi.pip`.
  2. `ResolverInput` passes `AttributeDesignatorKey requestedKey` into a multi-attribute resolver (`REPORT-D26-astra.md:17`), encouraging providers to compute only `requestedKey` and omit declared sibling attributes, which breaks whole-descriptor `AttributeSet` caching (`DefaultPolicyInformationPoint.java:81-96`).
- **Smallest fix**: Delete `ResolverInput`. With D27 (`design.md:704-709`), `ResolverContext` already resolves all `contextKeys` eagerly on the evaluation thread before the resolver runs and is safe for concurrent reads by worker threads.
- **Test that catches it**: Verify concurrent worker threads reading `ResolverContext.resolveContextRef(key)` and `getResolvedKeys()` after owner-thread preparation observe identical bags with zero `EvaluationContext` calls on worker threads.

### [P3-1] Treating `Values(emptyBag)` as `Missing` diverges from synchronous `AttributeResolverDescriptor`
- **Location**: `REPORT-D26-astra.md:78`; compare `DefaultPolicyInformationPoint.java:81-96` and `AttributeSet.java:117-122`.
- **Failure scenario**: In synchronous `DefaultPolicyInformationPoint.java:81-96`, if a resolver returns a map containing `attributeId -> emptyBag`, `AttributeSet.get(attributeId)` returns `Optional.of(emptyBag)` (`r.isPresent() == true`), which stops candidate fallback and records the empty bag in `designatorCache` (`DefaultEvaluationContextHandler.java:248`). Omitting `attributeId` from the map returns `Optional.empty()`, continuing to the next candidate resolver. Astra's `REPORT-D26-astra.md:78` states *"an empty bag is treated as `Missing`"*, making an explicit empty bag fall through to fallback resolvers in async descriptors while stopping fallback in sync descriptors.
- **Smallest fix**: Keep `AttributeSet` semantics identical between sync and parallel/async resolvers: an omitted/null mapping means missing (continue fallback); a present mapping (or follow existing `AttributeSet.Builder.attribute(id, bag)` rules) behaves identically across both paths.
- **Test that catches it**: Parameterize a fallback precedence test across sync and parallel descriptors where Resolver 1 returns an empty bag vs omits the attribute, and assert identical fallback and `designatorCache` behavior.

---

## 4. Exactness Checklist Against Requirements

| Requirement | Status in `REPORT-D26-astra.md` | Required Action |
| --- | --- | --- |
| **Fluent API & clean `xacml4j` style** | **Fails (P2-1, P2-2)**: Adds 4 new public types + `CompletionStage` + manual `Runnable` cancellation + per-call `ExecutorService` factory. | Use existing `ResolverContext` + `AttributeSet`; if engine-managed parallelism is added, use per-attribute `Function<ResolverContext, BagOfValues>` + PIP `resolverTimeout`/`maxConcurrentResolutions`. |
| **Resolver precedence & fallback (`DefaultPolicyInformationPoint.java:72`)** | **Broken on timeout if worker ignores interrupt (P1-3)**: `ExecutorService.close()` blocks before fallback candidate is tried. | Do not call blocking `ExecutorService.close()` per candidate; cancel futures and fall through immediately while worker holds its `Semaphore` permit until exit. |
| **Missing vs failure** | **Mostly preserved, minor drift on empty bag (P3-1)**: Missing continues fallback; failure retained and thrown as `processing-error` if no later candidate supplies values. | Keep `AttributeSet` missing vs empty-bag behavior identical to sync descriptors. |
| **Deadlines & cancellation -> `processing-error`** | **Preserved**: Unwraps `ExecutionException`/`CompletionException`, rethrows `Error`, maps timeouts/cancellation/runtime failures to `AttributeReferenceEvaluationException.forResolverFailure` (`AttributeReferenceEvaluationException.java:99`). | Keep this exact mapping and fixed message `"Attribute resolution failed"`. |
| **Nothing published from workers** | **Preserved**: Context keys resolved on owner thread; `designatorCache`, statuses, and `resolvedAttributes` updated only on owner thread. | Keep; aligns directly with D27 (`design.md:704-709`). |
| **Executor seam replaceable by `StructuredTaskScope` (JDK 28)** | **Fails (P2-1)**: Public `CompletionStage` callback escapes lexical scope. | Keep execution lexical (`Callable` tasks run and joined inside `DefaultPolicyInformationPoint`) so JDK 28 `StructuredTaskScope` is a drop-in internal replacement. |
| **No hand-rolled infrastructure** | **Fails (P2-1, P1-3)**: Custom `AttributeResolutionBatch` cancellation plumbing + per-call `ExecutorService` lifecycle. | Use JDK `Executors.newVirtualThreadPerTaskExecutor()`, `Semaphore`, and `Future.cancel(true)` (and Guava `ImmutableMap`). |

---

## 5. Verification and Residual Risk

- **Verification performed (`SUPPORTED` by read-only source inspection)**:
  - Inspected `REPORT-D26-astra.md:1-149`, `openspec/changes/optimize-pdp-evaluation/design.md:676-745` (D26, D27, D28), and research reports `pr9-sol-concurrent-resolution-code.md:1-76`, `pr10-astra-concurrent-resolution-jdk.md:1-101`, `pr11-fable-concurrent-resolution-literature.md:1-64`.
  - Verified descriptor, PIP, and handler contracts in `AttributeResolverDescriptor.java:52-289`, `ResolverDescriptor.java:43-65`, `Resolver.java:50-81`, `DefaultPolicyInformationPoint.java:65-198`, `PolicyInformationPoint.java:56-134`, `ResolverContext.java:71-202`, `ResolverCacheKey.java:63-90`, `AttributeSet.java:117-122`, and `DefaultEvaluationContextHandler.java:168-336`.
  - No files were created, edited, or deleted; no builds or tests were executed (read-only constraint while JMH is running).
- **Residual risk**:
  - Runtime behavior and latency/throughput effects of parallel PIP resolution remain `UNVERIFIED` until D27 and D28 are implemented and benchmarked under JMH.
