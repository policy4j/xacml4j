# PR9: concurrent attribute resolution, the code side (Codex GPT-6.1 Sol, 2026-10-27)

Maintainer request: "analyze concurrent attribue resolution strategies via selector or designator and how structured tasks can be used for high performance parallel attribute resolution with effects etc".

**Start with parallel I/O inside an existing resolver.** Engine-wide prefetch or speculative evaluation needs an explicit opt-in contract: today’s SPI exposes synchronous callbacks and a mutable evaluation context, without purity, cancellation or speculative-execution guarantees.

Paths below abbreviate `xacml-core/src/main/java/org/xacml4j/v30/` as `C/`, and the corresponding `xacml-pdp` path as `D/`.

**Current resolution paths**

Each individual decision gets a new handler and root context; the PIP, registry and configured resolver cache are shared. Attribute resolution normally forwards through descendant contexts to `RootEvaluationContext.resolve`, which passes the root context to the handler. Custom forwarding remains an extension boundary. (`D/pdp/PolicyDecisionPointContextFactory.java:161`; `C/policy/DescendantEvaluationContext.java:295`; `C/policy/RootEvaluationContext.java:313`.)

| Path | Current sequence |
|---|---|
| **Designator** | `AttributeReference.evaluate` → `AttributeDesignator.doContextResolve` → context → handler. Handler checks the **PIP bag map first**, then the positive request memo, then the request entity by category/id/type/issuer, then PIP. Request successes use `putIfAbsent` and return the winning bag; absence and failures are not memoized. (`C/policy/AttributeReference.java:107`; `AttributeDesignator.java:101`; `D/pdp/DefaultEvaluationContextHandler.java:168,222`.) |
| **Selector** | Handler checks its PIP selector map, then request content. Without `ContextSelectorId`, it selects directly from content; with it, `Entity.resolve` supplies the entity containing the XPath-valued context selector. XML evaluation checks its category and requires exactly one context node before evaluating the main path. (`DefaultEvaluationContextHandler.java:193,214`; `C/types/Entity.java:213`; `C/content/XmlContent.java:200`.) |
| **Selector fallback** | Try PIP content resolution; if absent and `ContextSelectorId` exists, resolve an **ENTITY designator** with that id/category, take its single entity, then evaluate the selector there. Only successful PIP-path selector results enter the selector map. Request-content results are recomputed. (`DefaultEvaluationContextHandler.java:262`.) |
| **PIP and resolver cache** | Registry selects candidates; attribute resolvers run in registration order until one supplies the requested attribute. Each gets a fresh `ResolverContext`. Cacheable calls perform cache lookup, invoke on miss, then cache a present `AttributeSet`/`ContentRef`. Earlier failures are retained if nobody supplies a value; a later value wins. (`D/spi/pip/DefaultResolverRegistry.java:80`; `DefaultPolicyInformationPoint.java:66,137`.) |
| **Resolver dependencies** | Cache-key construction or annotated invocation resolves the descriptor’s context references serially through the same evaluation context. `ResolverContext` lazily stores dependency bags and constructs `ResolverCacheKey` from resolver id and those bags. These resolutions can recursively enter either path. (`ResolverContext.java:142,174`; `AnnotatedResolverFactory.java:266`.) |

The default resolver cache is `PolicyInformationPointCacheProvider.NoCachePolicyInformationPointCacheProvider`; configured implementations own storage behavior. This is the cache boundary corresponding to “ResolverCache.” (`PolicyInformationPoint.java:94`; `PolicyInformationPointCacheProvider.java:31`.)

Two selector details matter:

- Request/entity path syntax failures and PIP-boundary failures have different mappings. Missing results follow MustBePresent; retrieval failures remain errors. Preserve the existing boundary-specific classes, codes and messages. (`DefaultEvaluationContextHandler.java:193,314`; `AttributeReference.java:123`.)
- **`JsonContent.resolve(selector, entitySupplier)` currently returns empty.** JSONPath helper methods exist, but they do not implement selector-to-bag resolution. (`C/content/JsonContent.java:114`.)

**Effects and shared state to control**

- **One published bag:** handler maps are volatile/CAS-published concurrent maps. Request memoization returns its insertion winner. PIP paths instead ignore the `putIfAbsent` winner and return their local result: concurrent same-key calls can return different bags. A parallel design needs per-key single-flight resolution and one authoritative result. Sharing failures or misses permanently would change today’s retry behavior. (`DefaultEvaluationContextHandler.java:114,229,248,290`.)
- **Cycles:** the two `ConcurrentLinkedDeque`s are shared resolution stacks. Overlapping calls can mistake another task’s work for recursion, and `pop()` can remove another task’s entry. Use propagated resolution ancestry plus cross-task wait-cycle detection; thread-local stacks alone do not detect independently started A↔B future waits. (`DefaultEvaluationContextHandler.java:236,262`.)
- **Context mutation:** root and descendant statuses are plain mutable fields; `setEvaluationStatusIfAbsent` is not atomic. Policy variable results use `HashMap`; response staging uses mutable lists. Concurrent maps for per-rule statuses/policy ids and an atomic minimum TTL do not make the whole context safe. (`C/policy/RootEvaluationContext.java:92,198,247`; `DescendantEvaluationContext.java:75`; `Policy.java:245`; `BaseCompositeDecisionRule.java:237`.)
- **Publication:** PIP designator bags become response `resolvedAttributes`, even for NotApplicable or Indeterminate. Prefetch must not publish unused results, statuses, policy ids, responses or TTL changes. Completion order must not determine duplicate-response merge order. (`D/pdp/DefaultPolicyDecisionPoint.java:199,243`.)
- **Resolver/cache effects:** callbacks can perform external I/O, logging and context mutation; cache hits also cause cache puts. `ResolverContext.resolvedKeys/cacheKey` are unsynchronized lazy fields and should remain invocation-confined. Registry outer maps are concurrent, but attribute entries contain mutable `LinkedHashMultimap`s; registration changes require coordination. (`DefaultPolicyInformationPoint.java:74`; `ResolverContext.java:106`; `DefaultResolverRegistry.java:106`.)
- **Content and threads:** XPath factories are thread-local and expressions compile per call; virtual-thread-per-selector execution would create more factories. Shared DOM/JSON content and custom providers need a demonstrated concurrent-read contract. (`C/content/XPathProvider.java:203,239`; `JsonContent.java:56`.)
- **Logging/MDC:** PDP and request MDC are set/cleared around synchronous evaluation; resolver and XPath logging remains observable. Workers need capture, installation and restoration of MDC. Logback explicitly does not automatically inherit parent MDC. (`D/pdp/DefaultPolicyDecisionPoint.java:97,133`; `MDCSupport.java:58`; [Logback MDC manual](https://logback.qos.ch/manual/mdc.html), checked **2026-10-27**.)

An additional cache-key hazard precedes reusing it for deduplication: `ResolverCacheKey.hashCode` hashes its bag array as an object, while `equals` compares array contents. Audit that contract before building single-flight maps around it. (`D/spi/pip/ResolverCacheKey.java:63,78`.)

**What a PIP can assume**

Observed execution is synchronous on the deciding thread, with nested dependency resolution on that call stack. The ordinary path supplies the root context, not the active rule context. Annotated providers retain the same provider instance across calls.

There is **no exactly-once guarantee**: a resolver may receive no call because request/cache data suffices, repeated calls for different keys, and retries after misses/failures. Shared providers can already receive calls from concurrent *decisions*; that does not establish safety for sharing one decision’s context or one `ResolverContext`. The SPI has no thread-transfer, purity or interruption contract. (`Resolver.java:61`; `ResolverContext.java:97`; `AnnotatedResolverFactory.java:250`.)

Order is observable through first-applicable, lazy `and`/`or`, first retained failure, MustBePresent, variable evaluation, and response staging. A speculative failure must not make an unreached branch Indeterminate. Preserve immutable per-attribute bags, missing/error distinctions and response effects under [XACML §§7.3.5, 7.18–19, Annex C](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html). Code anchors: `FirstApplicable.java:52`; `functions/impl/LogicalFunctions.java:67,118`; `VariableDefinition.java:115`.

**Strategy classification**

| Strategy | What can stay exact | Observable changes / required API |
|---|---|---|
| **1. Static policy-set prefetch** | Request precedence, bag consistency and eventual responses, **if** speculative outcomes remain private until demanded. Analysis finds *possible* keys, not keys necessarily needed; references, resolver dependencies and content selectors extend that graph. | Fetches skipped branches, changes read time, call order/count and backend/cache/log effects. Needs opt-in prefetch-safe resolvers, complete dependency declarations, policy-version invalidation, concurrency limits and cancellation semantics. Not a transparent replacement. |
| **2. Speculative independent designators** | Potentially result-exact for declared independent, stable, transferable resolutions, with single-flight bags and owner-thread ordered commit. | Changes timing/thread and may fetch an unnecessary second operand. Needs separate in-flight/committed state, dependency-cycle detection, private outcomes and a read-only resolver-input contract. Legacy callbacks receiving mutable contexts remain sequential. |
| **3. Batch per resolver** | **Best first step:** one resolver already returns an `AttributeSet`. Resolve its context inputs serially, then parallelize independent backend I/O inside that callback. Existing engine call boundaries remain intact. | No public engine API needed for provider-internal fan-out. Engine coalescing across separate invocations changes call count, timestamps and potentially values; that needs an optional batch/snapshot contract with per-request outcomes. Resolver id alone is insufficient grouping. |
| **4. Parallel combining children** | Pure isolated children can produce compatible responses if the existing algorithm consumes outcomes and commits effects in its required order. | Broadest risk: skipped PIP calls, status races, variables, obligations, policy ids and logs. Needs isolated branch contexts, shared authoritative attribute resolution, ordered effect replay and explicit eligibility for known algorithm implementations. Custom algorithms retain their existing execution contract. |

Cancellation cannot undo completed external calls or cache writes. Interrupt-insensitive I/O can also delay task shutdown.

**JDK API status — primary sources checked 2026-10-27**

| API/version | Verified status |
|---|---|
| StructuredTaskScope, **25** | **Fifth preview**, requiring preview flags; factories and Joiner policies. It cannot enter this project’s current no-preview baseline. [JEP 505](https://openjdk.org/jeps/505) |
| **26** | Sixth preview: timeout hook; `allSuccessfulOrThrow` returns results as a list; “any successful” factory renamed. [JEP 525](https://openjdk.org/jeps/525) |
| **27** | Seventh preview: typed join exceptions, exception-mapping overloads, `awaitAll` removed, timeout hook revised. [JEP 533](https://openjdk.org/jeps/533) |
| **28** | Finalization is **Integrated for JDK 28**, not delivered in this project’s JDK 25 baseline. [JEP 543](https://openjdk.org/jeps/543) |
| ScopedValue | **Final in 25**. Share immutable decision metadata; binding a mutable context does not make it thread-safe. [JEP 506](https://openjdk.org/jeps/506), [25 API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/ScopedValue.html) |
| Virtual threads | **Final since 21**. JDK 25’s per-task executor is available without preview and creates an unbounded number of threads; backend concurrency still needs limits. [JEP 444](https://openjdk.org/jeps/444), [25 Executors API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Executors.html) |

For JDK 25, default/“all successful” joiners fail fast; “any successful result” selects by completion rather than resolver precedence. Neither implements current PIP fallback. `awaitAll` retains failures without cancelling; `allUntil` permits a thread-safe completion predicate. Represent speculative retrieval failures as private outcomes and let the owner apply demand/order semantics. Scope closure interrupts unfinished tasks **and waits for termination**. ([25 Joiner API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/StructuredTaskScope.Joiner.html), [scope API](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/StructuredTaskScope.html).)

**Measurement**

Existing JMH can measure scheduling/allocation overhead and throughput across decisions with `-t 1/-t N` and `-prof gc`. It cannot establish PIP latency gains: `PdpFixture.java:129` hardcodes default resolvers and no-cache PIP. Add fixture-level PIP injection plus a corpus with controlled I/O latency, dependencies, shared keys, failures, skipped branches and selectors. Compare full responses alongside latency, allocation and backend work. Performance gains remain **UNVERIFIED**.

Verification: read-only source tracing, scoped searches, partial name-based Ripwire impact, and primary-source documentation. No edits, builds, tests or measurements.
