### Verdict: **APPROVE WITH CHANGES**

The D34 implementation by Claude Opus ([`REPORT-I-D34-opus.md`](`.ntm/worktrees/xacml4j--batch2/cc_1/REPORT-I-D34-opus.md`)) is clean, faithful to the round-3/3a design, and zero-allocation on the default (`DecisionObserver.none()`) path. There are no `P1` blockers, one `P2` finding on exception mapping for PIP selector content resolution (which also eliminates Deviation 4), and two minor `P3` items.

---

### 1. Seven-Point Verification Summary

1. **Allocation & Guard Cost (`SUPPORTED`)**:
   - **Zero allocation with `DecisionObserver.none()`**:
     - [`DefaultPolicyDecisionPoint.respond`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) and [`decide`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) check `observer != DecisionObserver.none()` and reuse the static singleton [`DecisionObservation.NONE`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`).
     - [`RootEvaluationContext.observationOf(context)`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) reads the pre-cached `top` field via [`DescendantEvaluationContext.topOf(context)`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) (`:168-170`) and performs a concrete `instanceof RootEvaluationContext` check rather than a megamorphic `EvaluationContext` interface call across 7 receiver classes.
     - [`DecisionObservation.isEnabled()`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`) checks `thread == Thread.currentThread() && observerFailure == null`. Because `NONE.thread` is `null` and `Thread.currentThread()` is a non-null JVM intrinsic (register read), `NONE.isEnabled()` short-circuits at `null == currentThread` without reading `observerFailure`.
     - [`DecisionObservation.resolvedFrom`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`) guards its field write with `if (thread == Thread.currentThread())` and receives only `AttributeSource` enum constants, so `NONE` is never mutated and allocates nothing on the unobserved path.
     - Unobserved decisions also save allocations that existed prior to D34: [`PolicyDecisionPointContextFactory.createContext`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`) no longer eagerly formats `correlationId` (`String` + `byte[]`) per `decide` call, and [`DefaultEvaluationContextHandler.doSelectorResolve`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`) and [`DefaultPolicyInformationPoint.resolve`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`) no longer allocate capturing lambdas for `.or(...)` / `.flatMap(...)`.

2. **Token Balance on Every Path (`SUPPORTED`)**:
   - **Span lifecycle**: Every `*Started` method in [`DecisionObservation`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`) saves `Object parent = current`, updates `current = token(observer.*Started(...))` inside `try`, and returns `parent`. If `*Started` throws (or returns `null`, rejected by `token(...)`), `current` remains `parent`, and because `*Started` is called outside the caller's `try` block, neither `*Failed` nor `*Finished` runs.
   - **Span termination**: Every `*Finished`, `attributeResolved`, and `*Failed` method calls [`end(parent)`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`) (`Object span = current; current = parent; return span;`) *before* checking `isEnabled()` or invoking the observer, and every normal completion (`*Finished`, `attributeResolved`) sits *after* the caller's `try/catch` block. An observer exception thrown from a normal end callback never triggers the same span's `*Failed` callback and leaves `current` restored to `parent`.
   - **Extended-Indeterminate pass**: [`BaseCompositeDecisionRule.getExtendedIndeterminate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java`) balances `extendedIndeterminateStarted` / `Failed` / `Finished` around `extendedIndeterminate(context)`. Child evaluations and `responsesStaged` callbacks inside the pass nest under the pass token.
   - **References (pinned and missed)**: [`PolicyIDReference.evaluate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicyIDReference.java`), [`PolicySetIDReference.evaluate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySetIDReference.java`), and [`PinnedReference.evaluate`](`xacml-core/src/main/java/org/xacml4j/v30/policy/PinnedReference.java`) all open a reference span (`evaluationStarted(context, ref)`). When resolved, the target's `evaluate(context)` nests inside the reference span; when missed, `PinnedReference` delegates to `ref.evaluate(context)` so `createContext` replays the snapshot miss inside `evaluateReferenced` and finishes the reference span with `INDETERMINATE` without opening a target span.
   - **Resolver spans (fallback, failure, cache hit, D26 parallel, and nested context attributes)**:
     - [`DefaultPolicyInformationPoint.resolve`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`) wraps each candidate resolver in `resolverStarted` -> `resolverFinished` / `resolverFailed`, including `ResolverContext.createContext`, [`cached(...)`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`) (`resolverCacheHit`), D26 parallel execution (`d.resolve(context, runner)`), and `value.get().rethrowFailure(designatorKey.getAttributeId())`.
     - Context attributes read by a resolver on the deciding thread resolve while `current` is the resolver token (so their `attributeRequested` spans nest inside the resolver span), and [`DefaultEvaluationContextHandler.resolveWith`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`) records `o.resolvedFrom(AttributeSource.INFORMATION_POINT)` *after* `attempt::get` returns, so nested attribute resolutions do not clobber the outer attribute's `attributeSource`. D26 worker threads run on virtual threads where `thread == Thread.currentThread()` is `false`, so they emit nothing.

3. **Exception Order & Suppression Rules (`SUPPORTED`)**:
   - [`observation.throwObserverFailure()`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) runs immediately after `rootPolicy.evaluate(evaluationContext)` (`:245`) and before debug logging, `incrementDecisionCounters` (`:253`), `createResult` (`:254`), `decisionAuditor.audit` (`:260`), and `decisionCache.putDecision` (`:263`).
   - [`DecisionObservation.isEnabled()`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`) becomes `false` on the first observer exception via [`kept(t)`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`) or [`suppressed(failure, t)`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`).
   - [`DecisionObservation.decisionFailed`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`) and [`DefaultPolicyDecisionPoint.requestFailed`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) attach a new observer exception via `failure.addSuppressed(t)` (with `failure != t` guard) and rethrow `Error` when `failure` is not an `Error`.
   - [`observation.decisionFinished(r)`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) and [`observer.requestFinished(token, response)`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultPolicyDecisionPoint.java`) both sit outside the `try` blocks that call `decisionFailed` / `requestFailed`.

4. **Opus's Deviations 1 to 7 (`SUPPORTED`, except Deviation 4 — see `P2-1`)**:
   - **Deviation 1 (`RootEvaluationContext.observationOf(context)`)**: Good optimization that avoids a megamorphic interface call across 7 context types. See `P3-1` for a 2-line fallback improvement so `EvaluationContext.getDecisionObservation()` is not dead code for non-`RootEvaluationContext` roots.
   - **Deviation 2 (Reusing `boolean observed = o.isEnabled()` in `Policy.evaluate`)**: Safe because [`DecisionObservation.responsesStaged`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`) and [`evaluationFinished`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObservation.java`) re-check `if (!isEnabled()) return;` internally, so an observer failure absorbed during child or obligation evaluation still silences `responsesStaged` and `evaluationFinished`.
   - **Deviation 3 (`functionFailed` suppression and P2-3 raw runtime exception behavior)**: Moving `arg.evaluate(context)` and `evaluateInPlace`/`evaluate` outside the `try` block in [`FunctionSpecBuilder.java:476-498`](`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`) and [`:531-537`](`xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java`) is the right way to satisfy `design.md` (`:1156-1158`): eager argument failures (`attributeFailed` or nested `functionFailed`) are reported only at the failing expression, while failures leaving a lazy (`and`, `or`, `n-of`) or higher-order (`any-of`, `all-of`, `map`, etc.) function's body are also reported for that function. Not wrapping a raw `RuntimeException` from a custom test `Expression` in `FunctionInvocationException` before the function body runs is completely benign because enclosing `Target`/`Condition`/`Rule` boundaries still map any `Exception` to Indeterminate (`processing-error`).
   - **Deviation 4 (Public `AttributeReferenceEvaluationException.forPathFailure`)**: Caused by a bug in [`DefaultPolicyInformationPoint.java:328-334`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`); see **P2-1** below.
   - **Deviations 5, 6, 7**: Defensive reset of `attributeSource = null` in `attributeRequested`, `Boolean.getBoolean("xacml4j.pip.structuredConcurrency")` in `StructuredResolverRunnerTest`, and `@SuppressWarnings({"unchecked", "rawtypes"})` are all clean.

5. **MDC Removal (`SUPPORTED`)**:
   - `MDCSupport.java` is removed and `rtk rg -i 'MDCSupport|\bMDC\b'` across all modules returns 0 matches.
   - [`PolicyDecisionPointContextFactory.createContext`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/PolicyDecisionPointContextFactory.java`) (`:199-207`) increments `correlationSequence` as a `long` and formats `correlationPrefix + "-" + Long.toHexString(correlation)` only when `getCorrelationId()` or `getCorrelationId(long)` is called.

6. **Merged `cached()` Conflict Resolution (`SUPPORTED`)**:
   - In [`DefaultPolicyInformationPoint.cached`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`), `cache.getAttributes(context)` runs first (populating the `ResolverContext`'s cache key when the cache provider computes it), `seen(context, attributeId)` checks the per-decision `DecisionSets` (only when `hasKey(context)` is true, preserving R-FABLE-sol's lazy context-key invariant), and `o.resolverCacheHit(evaluationContext, d.getId(), key)` fires **only** when `seen` is empty and `value` from the PIP cache is actually used.

7. **Pragmatism of Tests & Undone Task-36.3 Conformance Item (`SUPPORTED`)**:
   - [`DecisionObserverPdpTest`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DecisionObserverPdpTest.java`) (19 tests) thoroughly covers the 25 callbacks, token nesting, extended-Indeterminate passes, reference pinning/misses, attribute sources, resolver fallback/cache/failure, and observer failure/suppression ordering.
   - **Does the undone task-36.3 conformance run with a recording observer matter?** It is **not a blocker**, because `DecisionObserverPdpTest` already exercises the state machine directly, and touching `xacml-conformance-tests` was outside Opus's file packet. However, [`DecisionObserverPdpTest`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DecisionObserverPdpTest.java`) currently only tests `AttributeDesignatorKey` (its `Recorder.id(key)` casts `(AttributeDesignatorKey) key`), which is why **P2-1** on `AttributeSelectorKey` content resolution was not caught (and a conformance run with a recording observer would also have crashed in `Recorder.id(key)` on the first selector test). Fixing **P2-1** and **P3-2** in `xacml-pdp` covers that gap without needing to touch `xacml-conformance-tests`.

---

### 2. Findings

#### [P2-1] `DefaultPolicyInformationPoint.resolve(EvaluationContext, AttributeSelectorKey)` passes a different exception/status to `resolverFailed` than `resolveViaPip` passes to `attributeFailed` (and unnecessarily makes `forPathFailure` public)
- **Locations**:
  - [`DefaultPolicyInformationPoint.java:323-334`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`)
  - [`DefaultEvaluationContextHandler.java:475-486`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`)
  - [`AttributeReferenceEvaluationException.java:151-155`](`xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceEvaluationException.java`)
- **Problem**:
  In [`DefaultPolicyInformationPoint.java:327-334`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`):
  ```java
  try{
      r = (value != null && value.isPresent())?value.get().getContent().resolve(selectorKey):Optional.empty();
  }catch (Throwable t){
      if(observed){
          o.resolverFailed(context, d.getId(), selectorKey, parent, (t instanceof PathEvaluationException e)?
                  AttributeReferenceEvaluationException.forPathFailure(selectorKey, e):t);
      }
      throw t;
  }
  ```
  Opus noted in Deviation 4 that `DefaultPolicyInformationPoint` maps `PathEvaluationException` with `forPathFailure(selectorKey, e)` (`Status.syntaxError()`, `Origin.SYNTAX_ERROR`) for `o.resolverFailed` and rethrows raw `t` *"so the handler maps it as before"*.
  However, `DefaultPolicyInformationPoint.resolve(context, selectorKey)` is called exclusively through [`DefaultEvaluationContextHandler.resolveViaPip`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`):
  ```java
  private Optional<BagOfValues> resolveViaPip(EvaluationContext context, AttributeSelectorKey ref)
  {
      try{
          return session().resolve(context, ref);
      }catch(Exception e){
          throw failed(ref, e);
      }
  }
  ```
  As the comments at [`DefaultEvaluationContextHandler.java:342-343`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`) and [`:475-478`](`xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java`) explain, `resolveViaPip` is the PIP security/status boundary: it catches **every** `Exception` (including `PathEvaluationException`) thrown by `session().resolve(context, ref)` and maps it with `AttributeReferenceEvaluationException.forResolverFailure(ref, e)` (`Status.processingError()`, `Origin.RESOLVER_FAILURE`), whereas `forPathFailure` at `:346` only applies when the engine evaluates the selector on an `Entity` resolved via `contextSelectorId` (`:337`) or from the `RequestContext` (`:275`).
  Consequently, when `value.get().getContent().resolve(selectorKey)` in `DefaultPolicyInformationPoint.java:327` throws:
  1. If it throws `PathEvaluationException`, `o.resolverFailed` receives an `AttributeReferenceEvaluationException` with status `syntax-error` (`Origin.SYNTAX_ERROR`), while `resolveViaPip` wraps raw `t` into a **different** `AttributeReferenceEvaluationException` with status `processing-error` (`Origin.RESOLVER_FAILURE`) and passes that to `o.attributeFailed` (and uses `processing-error` for the decision).
  2. If it throws any other `Exception` (e.g., `RuntimeException`), `o.resolverFailed` receives the **unmapped** raw `Exception`, violating the [`DecisionObserver.resolverFailed`](`xacml-core/src/main/java/org/xacml4j/v30/spi/observe/DecisionObserver.java`) contract (*"after the engine has mapped a resolver exception to an `AttributeReferenceEvaluationException`"*), while `resolveViaPip` maps it to `AttributeReferenceEvaluationException` for `o.attributeFailed`.
- **Fix**:
  - In [`DefaultPolicyInformationPoint.java:328-334`](`xacml-pdp/src/main/java/org/xacml4j/v30/spi/pip/DefaultPolicyInformationPoint.java`), map `Exception e` with `mapped(d.getId(), selectorKey, e)` (`forResolverFailure`), report `mapped` to `o.resolverFailed`, and `throw mapped;` (which `forResolverFailure` in `resolveViaPip` returns as-is via `madeBy(cause, Origin.RESOLVER_FAILURE)` without allocating a second wrapper, so `resolverFailed` and `attributeFailed` receive the exact same instance):
    ```java
    }catch (Exception e){
        AttributeReferenceEvaluationException mapped = mapped(d.getId(), selectorKey, e);
        if(observed){
            o.resolverFailed(context, d.getId(), selectorKey, parent, mapped);
        }
        throw mapped;
    }catch (Error e){
        if(observed){
            o.resolverFailed(context, d.getId(), selectorKey, parent, e);
        }
        throw e;
    }
    ```
  - In [`AttributeReferenceEvaluationException.java:151`](`xacml-core/src/main/java/org/xacml4j/v30/AttributeReferenceEvaluationException.java`), revert `forPathFailure` from `public static` back to package-private `static` (eliminating Deviation 4).

#### [P3-1] `RootEvaluationContext.observationOf(context)` ignores `EvaluationContext.getDecisionObservation()` when `top` is not `RootEvaluationContext`
- **Locations**:
  - [`RootEvaluationContext.java:494-497`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`)
  - [`EvaluationContext.java:423-425`](`xacml-core/src/main/java/org/xacml4j/v30/EvaluationContext.java`)
  - [`DescendantEvaluationContext.java:172-175`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`)
- **Problem**:
  [`RootEvaluationContext.observationOf(context)`](`xacml-core/src/main/java/org/xacml4j/v30/policy/RootEvaluationContext.java`) currently returns `DecisionObservation.NONE` whenever `DescendantEvaluationContext.topOf(context)` is not an instance of `RootEvaluationContext`:
  ```java
  public static DecisionObservation observationOf(EvaluationContext context) {
      return (DescendantEvaluationContext.topOf(context) instanceof RootEvaluationContext root)
              ? root.observation : DecisionObservation.NONE;
  }
  ```
  As a result, `EvaluationContext.getDecisionObservation()` and `DescendantEvaluationContext.getDecisionObservation()` have zero callers in the codebase, and if a foreign subclass of `Rule.RuleEvaluationContext` (where [`DescendantEvaluationContext.isOwn`](`xacml-core/src/main/java/org/xacml4j/v30/policy/DescendantEvaluationContext.java`) intentionally stops the `top` walk) or a custom delegating `EvaluationContext` is used, observation silently turns off.
- **Fix**:
  Keep the fast `instanceof RootEvaluationContext root` check for the normal case and call `top.getDecisionObservation()` only on the cold fallback path:
  ```java
  public static DecisionObservation observationOf(EvaluationContext context) {
      EvaluationContext top = DescendantEvaluationContext.topOf(context);
      if (top instanceof RootEvaluationContext root) {
          return root.observation;
      }
      return (top == null) ? DecisionObservation.NONE : top.getDecisionObservation();
  }
  ```

#### [P3-2] `DecisionObserverPdpTest.Recorder.id(AttributeReferenceKey)` casts unconditionally to `AttributeDesignatorKey` and lacks a selector/content-resolver test
- **Location**:
  - [`DecisionObserverPdpTest.java:581-583`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DecisionObserverPdpTest.java`)
- **Problem**:
  `Recorder.id(AttributeReferenceKey)` casts `((AttributeDesignatorKey) key).getAttributeId()`, and `DecisionObserverPdpTest` has no test covering `AttributeSelector` / `ContentResolverDescriptor` observation (including content resolver failure or path failure on resolved content, as in `P2-1`).
- **Fix**:
  Update `Recorder.id(AttributeReferenceKey)` in [`DecisionObserverPdpTest.java:581-583`](`xacml-pdp/src/test/java/org/xacml4j/v30/pdp/DecisionObserverPdpTest.java`) to handle `AttributeSelectorKey` (e.g., `key instanceof AttributeDesignatorKey d ? d.getAttributeId() : ((AttributeSelectorKey) key).getPath()`) and add an assertion in `attributeAndResolverOutcomesAreReportedInEvaluationOrder` covering a selector and a failing content selector path so `resolverFailed` and `attributeFailed` are verified to receive the same mapped `AttributeReferenceEvaluationException`.
