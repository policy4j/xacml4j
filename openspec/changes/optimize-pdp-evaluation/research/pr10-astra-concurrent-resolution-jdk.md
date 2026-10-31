# PR10: concurrent attribute resolution, the JDK side (Codex Astra, 2026-10-27)

Maintainer request: "analyze concurrent attribue resolution strategies via selector or designator and how structured tasks can be used for high performance parallel attribute resolution with effects etc".

FINAL REPORT (CR-astra, Codex Astra)

**Recommendation: write the design now for JDK 25 without preview.** Start with concurrent backend I/O inside an already-demanded resolver. Keep policy evaluation, dependency resolution, status selection, and response publication on the evaluation thread. Hide task execution behind an internal adapter so structured concurrency can replace it when final.

All external API claims below were verified from primary sources on **2026-10-27**. Performance recommendations are hypotheses, not measurements.

| API | Verified status | Relevant changes |
|---|---|---|
| `StructuredTaskScope`, JDK 25 | Fifth preview; requires preview enabled | `open(...)` factories and `Joiner` policies. [JEP 505, updated 2025-10-09](https://openjdk.org/jeps/505) |
| JDK 26 | Sixth preview | `allSuccessfulOrThrow` returns results as a list; `anySuccessfulResultOrThrow` becomes `anySuccessfulOrThrow`; adds `onTimeout`. [JEP 525, updated 2026-04-02](https://openjdk.org/jeps/525) |
| JDK 27 | Seventh preview | Adds an exception type parameter; standard throwing joiners use `ExecutionException`; removes `awaitAll`; replaces `onTimeout` with `timeout`. [JEP 533, updated 2026-08-19](https://openjdk.org/jeps/533) |
| `ScopedValue` | **Final in JDK 25** | No preview required. [JEP 506, updated 2025-06-06](https://openjdk.org/jeps/506) |
| Virtual threads | **Final since JDK 21** | Available on this baseline without preview. [JEP 444](https://openjdk.org/jeps/444) |

Structured concurrency therefore cannot enter the current production build unchanged: [pom.xml:362](pom.xml:362) targets release 25, and the project contract excludes preview.

The **JDK 25 joiners** have materially different failure semantics:

| Joiner | Behavior | Fit here |
|---|---|---|
| `allSuccessfulOrThrow()` | Returns subtasks in fork order; first failure cancels siblings and makes `join()` throw. | Suitable only when every result is required and failure selection is acceptable. |
| `awaitAllSuccessfulOrThrow()` | Same failure policy; successful result is `Void`, with individual results read through subtask handles. | Useful for heterogeneous required results. |
| `anySuccessfulResultOrThrow()` | First successful completion wins and cancels siblings; throws if all fail. | **Does not preserve resolver precedence.** Returning `Optional.empty()` also counts as successful execution. |
| `awaitAll()` | Waits for all; a subtask failure neither cancels siblings nor makes the joiner throw. | Owner must inspect outcomes and apply deterministic resolution rules. |

Source: [JDK 25 `Joiner` documentation](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/StructuredTaskScope.Joiner.html).

In JDK 25, a throwing joiner causes `join()` to throw `StructuredTaskScope.FailedException` with its failure as the cause. Subtask handles retain thrown exceptions **and errors**. A configured `withTimeout(Duration)` starts counting when the scope opens; expiry cancels the scope and causes `StructuredTaskScope.TimeoutException`. Cancellation interrupts unfinished workers. **`close()` still waits for their termination**, potentially indefinitely if they ignore interruption. A timeout is therefore not a hard execution bound. [Scope API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/StructuredTaskScope.html), [Subtask API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/StructuredTaskScope.Subtask.html).

**The two practical alternatives without preview are:**

- `Executors.newVirtualThreadPerTaskExecutor()` with timed `invokeAll`: returns futures in submission order, waits for completion or timeout, and cancels unfinished tasks. Individual failures surface through `Future.get()` as `ExecutionException`; this is not automatic failure-based sibling cancellation. The executor creates an unbounded number of threads, so separately limit outstanding backend operations. Its `close()` also waits for termination. [Executors](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Executors.html), [ExecutorService](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/ExecutorService.html), [Future](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Future.html).
- `CompletableFuture`: appropriate for an already-asynchronous client. `allOf` waits for all completions; `anyOf` accepts the first completion, including failure. `join()` uses `CompletionException`. `orTimeout` completes the future exceptionally; `cancel(true)` does **not** interrupt its computation. Backend cancellation must be wired explicitly. Supply an executor for blocking operations rather than using the common pool. [JDK 25 CompletableFuture](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/CompletableFuture.html).

Prefer virtual threads for blocking clients and stages for clients already offering asynchronous operations. Both require explicit ownership, cancellation, backend deadlines, and admission limits.

**Scoped context and pinning**

Use a `ScopedValue` containing immutable request metadata—request ID, decision time, deadline, prepared inputs. It does not make a mutable `EvaluationContext` safe to share. Structured scopes inherit bindings automatically; ordinary executor tasks need explicit capture and rebinding:

```java
var metadata = REQUEST_METADATA.get();
executor.submit(() ->
    ScopedValue.where(REQUEST_METADATA, metadata).call(() -> fetch(inputs)));
```

Binding restoration is automatic when `call` exits. [JDK 25 ScopedValue](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ScopedValue.html). This does not automatically populate SLF4J MDC: [MDCSupport.java:57](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/MDCSupport.java:57) currently calls `MDC.put/remove`. Existing logging would need a task-entry bridge that restores the previous MDC in `finally`, or a logging integration that reads scoped metadata.

**JDK 25 does not pin a virtual thread merely because it blocks inside `synchronized`.** JEP 491 delivered that change in JDK 24, including monitor acquisition and `Object.wait`. Lock contention still serializes access. Native/FFM callbacks and certain class-initialization situations can still pin; inspect `jdk.VirtualThreadPinned` JFR events. Do not replace monitors wholesale for virtual-thread compatibility. [JEP 491](https://openjdk.org/jeps/491).

**Concrete constraints in this tree**

- [DefaultPolicyInformationPoint.java:72](xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java:72) tries matching resolvers in registration order. A later value can recover an earlier failure; otherwise the first failure survives. First-completion selection changes this contract. Parallel execution also introduces backend calls that sequential fallback might never make.
- [DefaultEvaluationContextHandler.java:239](xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:239) shares cycle stacks across the decision. Concurrent `contains/push/pop` sequences can confuse another task’s lookup with recursion. At line 248, `putIfAbsent` retains one bag but each caller returns its own fetched value. Concurrent maps alone do not establish one observed bag.
- [ResolverContext.java:142](xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/ResolverContext.java:142) lazily mutates dependency results and cache keys; line 177 recursively resolves dependencies. Prepare inputs on the owner thread before starting backend tasks.
- [XPathProvider.java:203](xacml-core/src/main/java/org/xacml4j/v30/content/XPathProvider.java:203) caches `XPathFactory` in a thread-local. A fresh virtual thread per selector loses that reuse. Sharing the factory through `ScopedValue` would require synchronization: the factory is not thread-safe. Initially parallelize remote content acquisition and keep XPath/JSONPath evaluation on the owner thread. [JDK XPathFactory contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/javax/xml/xpath/XPathFactory.html).

Cross-reference parallelism would additionally need per-branch dependency ancestry, coordination of duplicate requests, cross-branch cycle detection, and one canonical completed bag. That is a larger follow-up than parallel backend I/O.

**SPI direction**

Batching already exists at the descriptor boundary: [AttributeResolverDescriptor.java:286](xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AttributeResolverDescriptor.java:286) accepts a function returning `Map<String, BagOfValues>`. An existing resolver can perform independent backend calls concurrently and return that map without changing the public PIP interface.

For engine-managed asynchronous resolution, an **additive proposal**, requiring maintainer approval, is:

```java
AttributeResolverDescriptor.builder("profile", "Profile", category)
    .attribute("role", XacmlTypes.STRING)
    .attribute("department", XacmlTypes.STRING)
    .contextRef(subjectKey)
    .noCache()
    .buildAsync(backend::resolve);
```

Here `buildAsync` is proposed, not implemented. Its input should contain immutable, already-resolved dependencies rather than the live context. Its result should expose completion **and backend cancellation**, with distinct per-attribute outcomes: complete bag, missing, failure. A plain map cannot distinguish a failed attribute from an absent one.

Retain existing `.build(...)`, registry registration, and synchronous `PolicyInformationPoint.resolve(...)`. Add executor/deadline configuration through the existing `PolicyInformationPoint.builder(...)` style. Public method names, executor ownership, cancellation contract, limits, and partial-result type remain **Open Questions**. Annotation support also needs an explicit extension: [AnnotatedResolverFactory.java:139](xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/AnnotatedResolverFactory.java:139) currently requires the synchronous map return type.

**Statuses and effects**

The design should preserve these distinctions:

- **Missing value:** empty bag when `MustBePresent=false`; Indeterminate with `missing-attribute` when true.
- **Timeout or retrieval failure:** a resolution failure, not missing data. Preserve this engine’s `processing-error` and fixed message **“Attribute resolution failed”**, independently of `MustBePresent`.
- **Partial batch:** retain outcomes separately. Never publish an incomplete bag as a successful complete attribute. A failed unused attribute must not poison the decision; a demanded failure follows normal resolver fallback and combining semantics.

The first rule is normative in XACML §7.19.3. Timeout mapping is the proposed application of the engine’s existing failure contract, not a timeout rule specified by XACML. [XACML 3.0 Errata 01, §§7.3.5, 7.19](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html); [AttributeReference.java:104](xacml-core/src/main/java/org/xacml4j/v30/policy/AttributeReference.java:104), [AttributeReferenceEvaluationException.java:99](xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceEvaluationException.java:99).

Unwrap task-transport exceptions before existing mapping; preserve causes and the current treatment of `Error`. Workers must not publish statuses, resolved attributes, obligations, or advice. The owner applies outcomes when evaluation reaches them, preserving Annex C short-circuits and §7.18 staging. A future structured-task adapter must preserve these rules rather than select whichever failure happens first.

**Proof and measurement**

Reuse [AttributeResolutionSafetyNetTest.java:80](xacml-pdp/src/test/java/org/xacml4j/v30/pdp/AttributeResolutionSafetyNetTest.java:80) and the existing nested staging tests. Add only decision/status/order/API cases for reversed completion order, deadline plus fallback, partial failure, repeated attribute use, and short-circuited branches. Compare full responses and error causes.

Existing JMH measures decision time, allocation, and concurrent callers; it can detect overhead on RSA/synthetic workloads. It cannot establish remote-resolution benefit: [PdpFixture.java:126](xacml-jmh/src/main/java/org/xacml4j/jmh/harness/PdpFixture.java:126) installs the default resolver registry without a PIP cache. A new PIP-heavy corpus and configurable PIP fixture are needed: independent versus dependent lookups, controlled latency, missing/failing sources, cache hits, selectors, cancellation, and backend saturation. Compare sequential and concurrent implementations, including latency distributions and JFR observations. No speedup is measured here.

Verification: primary JEPs, JDK APIs, official OASIS text, and scoped source reads. DuckDuckGo returned no cross-check results; direct JDK documentation confirmed the JEP status. **No files changed; no builds, tests, or benchmarks run. Runtime behavior and performance remain UNVERIFIED.**
