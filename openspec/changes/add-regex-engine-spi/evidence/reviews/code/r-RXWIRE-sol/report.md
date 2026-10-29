
FINAL REPORT (Codex Sol, GPT-6)

**Verdict: REJECT.** Six P2 findings and one P3. These are source-level findings; the proposed regression tests were not run. In-process providers, factories, and engines are assumed to be trusted code.

1. **P2 — Literal binding bypasses custom invocation factories and the provenance check.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.java:322`

   `trusted` gates method handles, but the `LiteralBinder` is retained unconditionally. With a custom factory that returns false for the regexp function, a dynamic call uses that factory while an equivalent literal `Apply` or `Match` calls the binder and can return true. Providers outside the public-class/class-loader boundary can likewise bind despite failing `isBindable`.

   **Fix:** retain the binder only when `trusted` is true.  
   **Test:** use a custom factory with a distinct result or status; equivalent literal and dynamic calls must preserve it.

2. **P2 — Bound calls accept argument types that the original method rejects.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:561`

   `BoundPattern.invoke` accepts any `Value<?>` and converts it to text. For example, binding `"1"` on `string-regexp-match` and invoking the public bound call with `IntegerVal.of(1)` returns true. Ordinary invocation rejects the integer argument and produces processing-error. The other five regexp functions similarly lose their specific second-parameter type checks.

   **Fix:** check the evaluated operand against the actual method parameter type before invoking the binding; use the existing invocation fallback on mismatch, without reevaluating the operand.  
   **Test:** compare bound and ordinary invocation for the correct type, null, and a wrong value type. This preserves the type/error boundary of [XACML §7.4 and §7.19.2](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

3. **P2 — Runtime validation disables call-site retention.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:461`

   When runtime validation is enabled, the bound call delegates to `doInvoke`, which invokes the ordinary regexp method and its bounded cache. It never initializes or uses the call-site outcome. After cache churn, the literal is compiled again, contrary to D6’s call-site retention contract. A later resource failure can therefore turn an already compiled literal into Indeterminate.

   **Fix:** preserve normalization, validation, operand evaluation, and failure reporting, then invoke the retained binding after successful validation.  
   **Test:** repeat the existing literal-plus-cache-churn outcome test with runtime validation enabled.

4. **P2 — The volatile call-site outcome does not guarantee compile-once under eviction.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:567`

   Two callers can read `bound == null`. One compiles and publishes the outcome; unrelated dynamic calls then evict the cache entry. The second caller still follows its earlier null read and recompiles. Guava coalesces overlapping loads, but does not protect this call-site initialization interval after eviction. A second compilation can fail transiently despite a successful retained outcome.

   **Fix:** use a synchronized slow path with a second read of `bound`; retain only deterministic outcomes. The initialized path remains one volatile read, and compilation holds no global lock.  
   **Test:** exercise concurrent cold literal calls alongside small-cache churn and assert stable decisions with an engine double that exposes recompilation.

5. **P2 — Provider limits remain a class-initialization snapshot.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:123` and `:285`

   Engine/profile properties are read when constructing providers, but all property-based `Matching` factories reuse static `LIMITS`. Changing `maxInputLength`, for example, before constructing a second provider has no effect. D7 explicitly specifies reading these defaults when the provider is built.

   **Fix:** read limits for provider construction. Keep the static compatibility entry points’ snapshot separate.  
   **Test:** construct two providers around a limit-property change; assert their different limit outcomes and that the first provider remains unchanged.

6. **P2 — Default selection skips duplicate-name detection for `jdk`.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:260`

   With absent properties—or explicitly `jdk`/`legacy` properties—`matching()` returns `DEFAULT`, constructed directly with `new JdkRegexEngine()`. Two providers named `jdk` therefore do not fail default provider construction. Explicit `withRegexEngine("jdk", "legacy")` does fail. The specification’s duplicate-name scenario specifically names `jdk`; the new test substitutes `twin` and misses this path.

   **Fix:** resolve the provider-selected default through `RegexEngines`, including duplicate detection. D7’s static compatibility instance can remain separate.  
   **Test:** in an isolated service-loader fixture containing a second `jdk` provider, assert failure for default, property-based, and explicit selection.

7. **P3 — Eager defaults obstruct explicit configuration and obscure configuration errors.**  
   `xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionProviderBuilder.java:85`;  
   `xacml-core/src/main/java/org/xacml4j/v30/marshal/MarshallingProvider.java:47`

   `FunctionProvider.builder()` constructs the property-selected default before callers can select an explicit engine. Thus an invalid default property prevents `.withRegexEngine("jdk", "legacy")` from overriding it. Initialization of `MarshallingProvider.funcSystemProvider` can additionally turn the configuration error into a permanent `ExceptionInInitializerError` for the interface.

   **Fix:** defer default selection until the builder’s final configuration is known. Avoid initializing property-dependent system functions merely to discover marshallers; preserve the public compatibility field through lazy delegation if necessary.  
   **Test:** invalid defaults should fail clearly when requested, while an explicitly configured provider can still build and import a policy. Fable’s supplied propagation run already records the initialization-error symptom.

**Other checks**

- **`Match` merge:** the three merged changes preserve the index loop, bag order, one value read per iteration, empty-bag behavior, and the existing status catch (`Match.java:77, :122–141`).
- **Dispatch:** the instance-handle path retains the default-factory, public/enclosing-class, non-hidden, reference-parameter, and same-loader checks (`DefaultFunctionInvocation.java:314–340`). A custom same-loader provider can receive a handle, as static providers could before; built-in fast shapes remain restricted by exact provider class. Finding 1 is the separate binder escape.
- **Caching and statuses:** deterministic compilation failures are stored as outcomes; transient exceptions and engine errors escape loading and are mapped on each attempt (`RegExpFunctions.java:521–545, :616–630`). Guava’s documented loading and exception behavior supports this implementation ([Guava 31.1-jre API](https://guava.dev/releases/31.1-jre/api/docs/com/google/common/cache/LoadingCache.html), checked 2026-10-28). The known compilation-stack-overflow classification remains FIX-RXSPI’s responsibility.
- **Laziness:** binding itself only creates a wrapper. Compilation occurs after evaluating the second operand; an empty target bag invokes no binding (`Apply.java:107, :290`; `FunctionSpecBuilder.java:464`; `RegExpFunctions.java:471, :569`). Lazy functions retain their existing route.
- **Repository defaults:** the additive accessor defaults preserve the former system-provider import behavior. External repositories must override both accessors to expose different providers; the import tool now uses those answers (`PolicyRepository.java:198–208`; `PolicyImportTool.java:81`).
- **API:** `BoundCall`, `FunctionSpec.bind`, and the invocation bridge need public visibility with the present package boundaries. Public `Matching` and the three fluent selection overloads are reasonable. `LiteralBinder` is an additional extension contract beyond regex selection; either keep that hook internal or document its evaluated-operand and concurrent-use requirements explicitly.
- **Performance:** accept the supplied cache-thrashing tradeoff for this cut. The reported increase is about 158 ns for the short dynamic miss case; warmed literal sites avoid cache lookup. No cheap bypass is justified that would weaken D6.
- **Tests:** selection, retry, retained-failure, higher-order, and post-eviction decision assertions are useful. The compile-once doubles expose D6’s public SPI contract through outcomes. `RegexCompileOnceTest.java:291` additionally pins exact LRU eviction, which is stronger than D6’s bounded-cache requirement. The missing cases are listed with the findings above.
- **`cacheRegExps`:** ignored with one class-initialization warning, as approved (`RegExpFunctions.java:132–138`).

**Verification:** scoped `git diff --check` passed. `ast-grep scan --filter '^java-' --inspect summary` passed: 14 files, three rules. Ripwire quality-delta exited 2 against HEAD `87133237`; its broader accumulated delta is **PARTIAL** evidence, not an RX-WIRE finding list. No builds, tests, or JVMs were run. The conductor’s green counts are supplied evidence.

No files changed. `REPORT-R-RXWIRE-sol.md` was not saved because the developer’s read-only lane contract prohibits creating files.
