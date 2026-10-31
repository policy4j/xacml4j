# PR8: next cycles after batch 2, from profile 021 (Codex GPT-6.1 Sol, 2026-10-27)

Recommend canonical designator keys, result allocation cleanup, then broader scalar invocation. **D15 already adopts MethodType and MethodHandles.** Extending its LambdaMetafactory adapters is the cleaner next step; replacing them with MethodHandle fields has no demonstrated advantage here.

All percentages below refer to **profile 021 before batch 2**, not predicted percentages afterward. R = rsa2008/all; S = synthetic:1000/all; F = synthetic:1000/first-deny. Savings and C2 inlining remain **UNVERIFIED**. Custom in-process contexts and providers are trusted code whose extension contracts must remain intact.

Paths below abbreviate `xacml-core/src/main/java/org/xacml4j/v30/` as `core/`, its `policy/function/` directory as `functions/`, and `xacml-pdp/src/main/java/org/xacml4j/v30/` as `pdp/`.

**What batch 2 already addresses**

| Decision | Recorded cost addressed | What remains |
|---|---|---|
| D5, D6, D8–D10, D14 | Already represented in 021. D11 concerns JVM settings; D12 defines the cycle procedure. | Do not credit these again. |
| D7, C | Bag construction/iteration: roughly 21.2% S CPU; 22.7% R allocation. | Actual comparisons and function work. S’s 14.5% `ImmutableList$1.get` is bag traversal, not another target-list opportunity. |
| D15, I | Binary predicate invocation containers: 41.8% S allocation. | Unary/context shapes and generic `Apply`/higher-order routes. |
| D16, J | Target iterators: 13.8% S allocation; normalization iterators: 4.5% R. | Normalization buffers; combining iterators intentionally retained for collection semantics. |
| D17, K | 9.3% S allocation from `Optional`; repeated context forwarding. | Context objects, walking, reference lookup and key equality. |
| D18, L | Named per-element logging sites; old logging budget about 9.6% S CPU. | Required decision logging and logging outside those sites. |
| D19, M | Empty staging lists and associated work, about 13.3% R allocation. | Staging wrapper itself: about 2.0% R allocation; populated responses. |
| D20, N | Engine exception traces and redundant wrappers. | Generic `Method.invoke` still creates `InvocationTargetException`: **3.80% R CPU, 1.99% R allocation**, including its construction/backtrace work. |

Evidence: [design D5–D20](openspec/changes/optimize-pdp-evaluation/design.md:81), [021 summary](openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/021-profile-decide-cycle-H/summary.md:1), its six `top-*.md` reports and collapsed-stack recounts. These budgets overlap and must not be added wholesale.

**Next cycles, ranked by expected gain per risk**

| Rank | Hypothesis and code | Surviving 021 budget / expected removal | Preservation, risk and API |
|---|---|---|---|
| **1. Canonical designator keys** | Weakly intern keys in `core/AttributeDesignatorKey.java:160`; comparisons at `:98`. | Equality is **5.72% S CPU**, including 3.8% in `String.equals`; most is potentially avoidable through identity matches. | Low risk. Preserve complete key equality, issuer handling and exact identifiers; §§7.3.4–5, 7.20. Private thread-safe weak interner; existing builder API and equality fallback remain. No strong global retention. |
| **2. Result and structural-hash allocation** | Lazy empty builder maps and private shared OK status in `core/Result.java:54,87,255`; replace varargs hash arrays in `core/Status.java:197` and rule hashes, beginning `core/policy/BaseDecisionRule.java:300`. | Approximately **7.3% F allocation** from the remaining builder maps, hash arrays and OK status; structural rule hashing adds roughly **1.66% R allocation**. | Low risk. Preserve hash values, eager result-hash timing, ordering, serialization and equal-but-distinct rule lookup; §§7.17–18. No public API change. Do not cache hashes of extensible mutable values/rules. |
| **3. Broader scalar invocation** | Extend `functions/impl/DefaultFunctionInvocation.java:171`; route scalar calls through `functions/FunctionSpecBuilder.java:375–441`, `core/policy/Apply.java:264` and `FunctionReference.java:88`. | Positive unary opportunity on R; **no identified new S shape benefit**. Shared ceilings: **13.3% R allocation** in generic buffers and **3.8% R CPU** in reflection exception construction. The unary fraction is not separable from 021. | Medium risk; provisional rank. Preserve evaluation order, runtime checks, exception/status mapping and custom invocation behavior; §§7.4, 7.19. Details below. |
| **4. Bound reference resolution** | Replace repeated default-resolver map probes with PDP-owned, invalidatable links: `pdp/spi/repository/DefaultPolicyReferenceResolver.java:75`; reference checks in `core/policy/BaseCompositeDecisionRuleIDReference.java:104`. | **6.61% S CPU** is resolver `ConcurrentHashMap.get`; reference matching adds a separate **2.38%** opportunity. | Medium/high lifecycle risk. Preserve versions, repository invalidation, cache-disabled behavior, missing/cyclic-reference errors and custom resolver calls; §7.15. Possible optional SPI: `resolver.bind(reference)` → `PolicyLink.resolve()`. No cached target field on shared policy objects; custom resolvers retain their existing path. |
| **5. Restricted target index** | Immutable ordered candidate selection at `core/policy/PolicySet.java:177`; workload construction: `xacml-jmh/.../SyntheticCorpus.java:111,124,140`. | Skipped-child fraction × **32.8% S context allocation**, plus remaining lookup/comparison work. This is an opportunity budget, not a measured saving. | High semantic risk. Explicit opt-in for a narrow default-function/request-equality shape. Preserve original child order, multivalue unions, fallback children, errors, references and combining behavior; §§7.6–7, 7.14–15, 7.18, Annex C. Dynamic repositories need versioned binding first. |

Hash/result estimates come from `research/pr1-opus-allocation.md:218–246`; key/reference evidence from `research/pr2-fable-cpu.md:44–57` and the corresponding 021 stacks.

Other residual sites worth retaining in the ledger:

- **R generic invocation buffers:** 3.68% allocation in normalized buffers after J’s iterators; together with other generic lists/arrays and `Apply`’s argument array, **13.27%**. Higher-order signature validation separately accounts for about **2.98% R allocation / 1.60% CPU**. Fixed-shape adapters cannot remove all of this.
- **Request scans:** `core/RequestContext.java:285,341` account for **4.05% R / 7.93% F allocation** and **3.54% R / 9.49% F CPU** together. The two XPath-selector probes at `pdp/pdp/request/MultipleResourcesViaXPathExpressionHandler.java:186` could share traversal. Defer the public helper/API expansion; preserve subclass overrides, and do not move the work into request construction merely to improve this reused-request benchmark.
- **Contexts:** policy contexts **18.6% S allocation**, reference contexts **14.2%**. K removes neither.
- **OtherValue hashing is largely consumed by C:** excluding bag-construction/resolution stacks leaves only **0.079% R allocation / 0.020% CPU**. It is no longer a substantial independent cycle.

**Scalar invocation candidate**

AST inventory of the **16 providers / 275 annotated methods** registered by `functions/impl/XacmlDefaultFunctions.java:38`:

| Additional fixed shape | Inventory | Actual benchmark use |
|---|---|---|
| Unary | **46**: bag functions 36, arithmetic 4, numeric conversion 2, string normalization 3, logical `not` 1. | RSA contains `not` and `string-one-and-only`. Visible one-and-only stacks occupy **4.08% R CPU**, largely including error work N already addresses. Synthetic uses binary string equality, already covered by I. |
| Context + one operand | **28**: string conversions 26, XPath count functions 2. | No identified use in either corpus: **zero attributable 021 budget**. |
| Context + two operands | **10**: regex functions 6, XPath comparison functions 4. | No identified use in either corpus: **zero attributable 021 budget**. |

Representative sources: `BagFunctions.java:121`, `LogicalFunctions.java:88`, `StringFunctions.java:170`, `XPathFunctions.java:57,81`, `RegExpFunctions.java:66`, all under `functions/impl/`.

The RSA policy inventory contains string equality, `not`, one-and-only, subset, `and` and `any-of`; synthetic targets use string equality. Crucially, **`and` and `any-of` are variadic**, not fixed context-plus-one/two shapes. Their visible provider-attributed CPU is 15.34% and 7.18% R respectively; that is function/subtree work, not removable dispatch overhead.

Use small adapters: add `Unary`, reuse `Binary` for physical `(context, value)`, and add `Ternary` for `(context, value, value)`. Pass the current context explicitly; never capture it in cached bindings.

- Add a default **`invokeUnary(context, operand)`** delegating to legacy varargs. Naming it `invoke(context, Expression)` would make existing `invoke(context, null)` source calls ambiguous with `Expression...`.
- Route `Apply`’s one/two-operand cases to unary/existing pair entry points. Extend `FunctionSpecImpl.invokeWithList` similarly for eligible built-ins.
- `Match` already uses pairs; it would additionally support context-aware binary predicates such as regex. Higher-order inner predicate calls at `HigherOrderFunctions.java:79,190` can route one/two operands through `FunctionReference`; their outer variadic calls remain generic.
- Keep D15’s **exact default-factory provenance**, access restrictions, type-check fallback, runtime validation, operand order and exception mapping. Preserve custom varargs overrides; do not unconditionally redirect `Apply` to `invokeWithList`.

D15 already links a direct MethodHandle through LambdaMetafactory. Such adapters may inline when C2 devirtualizes their SAM calls; a polymorphic adapter site can inhibit that. A cached MethodHandle with one-time adaptation and `invokeExact` can also inline when its target is provable, but a final instance field alone provides no guarantee. Prefer extending the current adapters. [LambdaMetafactory](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/invoke/LambdaMetafactory.html), [MethodHandle](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/invoke/MethodHandle.html), [JEP 416](https://openjdk.org/jeps/416).

Variadic bindings could avoid reflection dispatch/wrapping, but retain required argument arrays, normalization and lazy operands. Neither `invokeExact` nor LambdaMetafactory automatically removes that packing. Treat this as separate work.

**Deferred research judgment**

PR6 indexing is now worth a **restricted design**, not its general 97–99% projection: batch 2 already removes much of the cited machinery, and `all` mixes seven requests. Start with exact default string-equality targets and a safe fallback, not general MIDD conversion.

PR5’s lazy reference-context idea (`pr5-terra-policy-structure.md:33`) retains a **14.2% S gross allocation ceiling**, reduced by additional fields/materialization. Defer implementation behind indexing: preserving stable public ancestry, external overrides, cycles and status routing makes it substantially riskier. Direct context fusion remains incompatible. PR5’s singleton/iterator/forwarding/staging ideas are covered by C/J/K/M; combining-iterator removal offers only about **0.07% S allocation** and does not justify snapshot-semantics risk.

Section references above use the [official XACML 3.0 core specification](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html). Optimization should preserve the currently tracked Target/Match behavior; semantic corrections belong in their separate changes.

Verification: source/design/research inspection, collapsed-profile aggregation, AST function inventory and corpus inspection only. **No files changed; no builds, tests or new measurements.** Exact post-batch gains and C2 inlining remain unverified.
