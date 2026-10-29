
Draft complete. No files changed. Public API and exception-policy choices below need maintainer approval.

### D34. Observability through synchronous callbacks without event allocations

The PDP shall accept one `DecisionObserver` through fluent `PolicyDecisionPointBuilder.observer(observer)`, following the existing auditor configuration style (`PolicyDecisionPointBuilder.java:127`). The interface shall live in `xacml-core`, proposed package `org.xacml4j.v30.spi.observe`, so policy evaluation does not depend on the PDP module.

Use typed default methods. Do not create event objects, argument arrays, captured lambdas, boxed numbers, formatted identifiers, timestamps, or collection snapshots for dispatch. This guarantees **no additional allocation by callback dispatch** on either path. Allocations performed by an observer, including a tracing SDK, remain its responsibility.

Proposed lifecycle shape:

```java
public interface DecisionObserver {
    Object NO_STATE = new Object();
    DecisionObserver NONE = new DecisionObserver() {};

    default Object requestStarted(PolicyDecisionPoint pdp, RequestContext request) {
        return NO_STATE;
    }
    default void requestFinished(Object state, ResponseContext response) {}
    default void requestFailed(Object state, Throwable failure) {}

    default void requestSplit(Object requestState, RequestContext request, int number) {}
    default Object decisionStarted(Object requestState, RequestContext request, int number) {
        return NO_STATE;
    }
    default void decisionFinished(Object state, Result result) {}
    default void decisionFailed(Object state, Throwable failure) {}

    default Object evaluationStarted(Object state, EvaluationContext context, DecisionRule rule) {
        return NO_STATE;
    }
    default void evaluationFinished(Object state, EvaluationContext context,
            DecisionRule rule, Object evaluationState, Decision decision) {}
    default void evaluationFailed(Object state, EvaluationContext context,
            DecisionRule rule, Object evaluationState, Throwable failure) {}
}
```

The remaining default no-op callbacks shall carry:

| Callback | Arguments after decision state and `EvaluationContext` |
|---|---|
| `targetMatched` | Existing owner `DecisionRule`, `MatchResult` |
| `attributeResolved` | `AttributeReferenceKey`, source enum, **existing returned** `Optional<BagOfValues>` |
| `attributeFailed` | Key, existing `Throwable` |
| `functionFailed` | `FunctionSpec`, existing `Throwable` |
| `responseStaged` | `Decision`, existing `DecisionRuleResponse` |
| `childrenSelected` | Owner, primitive `boolean indexed`, original child count, candidate count |
| `decisionCacheHit` | Existing `RequestContext` and `Result`; no evaluation context required |

Resolution sources shall distinguish request, request memo, PIP, PIP memo, and cycle. Absence uses the existing empty outcome; observing it shall not apply `MustBePresent` or synthesize a status.

**Context and state.** Reuse `RequestContext` for lifecycle callbacks and the actual `EvaluationContext` for evaluation callbacks. They expose request flags/categories and the current evaluation level, clock, validation mode, and status through existing APIs. Do not add a context wrapper.

The existing `NumberedDecisions` object shall retain request state. Each decision’s returned state shall be held locally, then stored by reference in its existing root context. Evaluation-scope tokens shall remain local variables, allowing a tracer to retain policy spans without a lookup map.

Proposed additive plumbing:

- Default `EvaluationContext.getDecisionObserver()` and `getDecisionObserverState()`, returning `NONE` and `NO_STATE`.
- Non-final descendant forwarding and a root constructor overload accepting observer/state.
- A default `PolicyDecisionPointContext.createEvaluationContext(request, observer, state)` overload, retaining the old method; the built-in implementation binds the state.

State returns shall be non-null. Existing constructors and external contexts retain no-op behavior unless they opt in. Callback arguments are borrowed engine objects; observers shall not mutate evaluation state.

**Dispatch and exceptions.** The PDP and root shall hold final observer references. Each call site shall read its observer once into a local and guard `observer != NONE`. This removes disabled dispatch without depending on successful JIT inlining; elimination of the remaining guard is expected, **UNVERIFIED**.

Recommended exception policy: one private adapter, created when the PDP is built, shall isolate observer `RuntimeException`s. It shall use direct forwarding, without callback lambdas or recursive logging. Fatal `Error`s propagate. Observers remain responsible for cleaning up partially initialized spans if their own start callback fails. Existing auditor exception behavior stays separate.

**Emission points and order.**

| Boundary | Placement and evidence |
|---|---|
| Request and decision lifecycle | Around `decide` and each `NumberedDecisions` callback, before cache lookup; `DefaultPolicyDecisionPoint.java:96,128,138`. |
| Multiple-decision split | Report each expanded leaf reaching the decision callback, in its existing order. Handler-generated rejection results appear in `requestFinished`; no fabricated evaluation events. |
| Cache hit | Before the existing audit/counter path; finish with the cached result. Do not create a root context; `DefaultPolicyDecisionPoint.java:147`. |
| Policy set, policy, rule, references | Pair entry with returned decision or escaped failure in `BaseCompositeDecisionRule.java:117`, `Rule.java:107`, `PolicyIDReference.java:89`, `PolicySetIDReference.java:93`. Include extended-Indeterminate passes actually performed. |
| Target | After the existing status mapping in `BaseDecisionRule.isMatch`, `BaseDecisionRule.java:129`. |
| Attribute resolution | At source-aware handler return/failure/cycle boundaries, after existing publication and mapping; `DefaultEvaluationContextHandler.java:190,216,245,317`. |
| Function failure | At engine invocation boundaries: `Apply.java:257`, `Match.java:108`, `FunctionReference.java:94`. Distinguish predicate invocation failure from attribute lookup failure. Do not also emit duplicate notifications from binding/reflection internals. |
| Obligation/advice staging | After successful staging in `BaseDecisionRule.java:162`; do not report publication through enclosing buffers as another creation event. Final surviving responses remain available in `Result`. |
| Prepared/indexed children | At consumption in `Policy.combineDecisions` and `PolicySet.combineDecisions`, including `PinnedReference` evaluation. Report counts, without enumerating skipped children or reporting them as evaluated; D24/D25, design.md:555. |

Calls shall be synchronous, on the deciding thread, in evaluation order. One observer can receive concurrent callbacks from different decisions and must support that. Resolver backend workers shall not emit engine callbacks. Any later concurrent evaluation contract must define worker ordering and state propagation explicitly.

Decision completion shall follow existing result construction, audit, and cache publication. Observer failures shall not move those operations. Preserve existing counters—including their current classification—and histogram registration (`DefaultPolicyDecisionPoint.java:75,155,166,173,176,186`).

**MDC and correlation compatibility.** Remove automatic MDC operations and `MDCSupport` (`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/MDCSupport.java:35`). Remove construction of the per-decision MDC string.

Retain public `getCorrelationId()` compatibility: assign the sequence at context creation, but format/cache its string only when the getter is called. D18’s guarded debug lines may call it inside the existing debug guard. At INFO, default evaluation shall not request that string (`PolicyDecisionPointContextFactory.java:108,156`; `DefaultPolicyDecisionPoint.java:149,163`).

D34 shall ship no MDC adapter or OpenTelemetry dependency. An application observer may populate MDC and restore the previous values on completion/failure; that optional adapter owns its string and MDC costs.

**OpenTelemetry adapter — primary sources checked 2026-10-27.** A later adapter can retain span/scope state in the returned tokens, create decision and policy spans, use explicit parents through `SpanBuilder.setParent(Context)` or scoped current context, and end spans on both terminal callbacks. These APIs are documented in the [Java instrumentation guide](https://opentelemetry.io/docs/languages/java/api/) and [Java API 1.67.0 SpanBuilder javadoc](https://javadoc.io/static/io.opentelemetry/opentelemetry-api/1.67.0/io/opentelemetry/api/trace/SpanBuilder.html).

Use application-defined attributes for policy ids, decisions, cache hits, and XACML status codes. Treat Deny and NotApplicable as normal authorization outcomes. Record existing function exceptions; a recovered function failure shall not automatically mark the final decision erroneous. `recordException`, `setStatus`, and `end` are separate operations in the [Span javadoc](https://javadoc.io/static/io.opentelemetry/opentelemetry-api/1.67.0/io/opentelemetry/api/trace/Span.html).

**Kept:** decisions, statuses, missing-attribute behavior, operand order and laziness, combining short-circuits, obligation/advice filtering, cache/audit ordering, and serialized requests/results ([XACML 3.0 §§7.3.5, 7.3.7, 7.18, 7.19 and Annex C](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html)).

Expected, unmeasured: remove the logging/id sites attributed up to 2.1% rsa2008 and 8.3% synthetic first-deny bytes in profile 026 (`research/pr12-opus-allocation-026.md:98`). These are upper bounds, not predicted net gains.

**Proposed task group 36.**

- **36.1** Approve SPI names, events, context plumbing, and exception policy.
- **36.2** Implement dispatch, state propagation, MDC removal, and lazy legacy correlation formatting.
- **36.3** Pin the public callback sequence with nested sets, a reference, a failing function followed by a successful combining outcome, staging, and a second-call cache hit. Assert unchanged responses/statuses. Cover observer failure isolation and an escaped audit failure. Reuse existing compatibility tests; add no allocation, logging, UID, or invocation-count tests.
- **36.4** Review by another model family; reactor and conformance after the hold is lifted; measure disabled and allocation-free enabled observers with the batch.

**Open Questions:** approve API/package names and event set; confirm leaf-split rather than intermediate-expansion events; approve `RuntimeException` isolation and fatal-error propagation; confirm removal of the public `MDCSupport` class rather than a deprecated compatibility facade.

Verification: source and primary documentation only. No edits, builds, tests, or JVMs. JIT behavior and net performance remain **UNVERIFIED**.
