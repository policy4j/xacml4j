
FINAL REPORT (Codex Sol, GPT-6)

Recommend starting with small-category lookup, cached bag cardinality, and existing algorithm flags. Long-string hash rejection is the largest inexpensive experiment. Prepared higher-order calls and precheck continuations need designs and stronger outcome coverage.

**Evidence limit:** the supplied CPU directory contains only `jmh.log` and `process.log`; the collapsed file named in the log is absent. Sample counts below come from `research/pr23-astra-fixed3-profile.md:258`. Call paths were confirmed in source; exact caller weights, branch misses, and C2 inlining causes remain **UNVERIFIED**. No new profile, build, test, JVM, or file write was run.

Source references below are relative to `xacml-core/src/main/java/org/xacml4j/v30/`, unless another module is named. They refer to the inspected reset tree. The final comparison found that main had subsequently added the D45 Policy-owned bundle and AZ’s no-current-policy proof; neither is credited below.

### Ranked changes

Expected reductions are **unmeasured planning estimates for total rsa2008 decide time**, not the entire sampled frame share. They are not additive.

| Rank | Change | Expected reduction | Cost and risk |
|---:|---|---:|---|
| 1 | **D49:** scan the existing category list for requests with at most four categories | 0.5–1.5% | No fields or allocation; low risk. Addresses part of the combined 3.05% map/hash budget. |
| 2 | **D50:** keep known built-in bag cardinality on `BagOfValues` | 0.4–0.9% | One scalar field, expected to fit existing padding; preserve custom storage dispatch. Bag-size interface stubs alone account for 1.04%. |
| 3 | Local cycle: reuse `builtInAlgorithm` in Policy/PolicySet preparation | 0.1–0.4% | Two repeated class-set lookups removed; no new state. **After D47 and AZ.** |
| 4 | **D48:** reject unequal long strings using their existing JDK hashes | 1–3% | No fields or allocation. Risk: additional checks can lose on short, equal, or first-use strings. |
| 5 | **D52:** compact precheck plans and continue directly into an admitted rule | 1–3% | Load-time plan data; no decision allocation. Medium risk around context validity, statuses, and precheck lifetime. **After D47 and AZ.** |
| 6 | **D51:** prepare the signature of eligible higher-order call sites | 0.5–1.5% | Load-time plan and implementation bridges; no per-call proof objects. Medium risk around fallback and exception mapping. |
| 7 | **D53:** inspect reference ancestry once during cycle detection | 0.3–0.8% | No new state. Higher correctness risk; require the missing caller breakdown before implementation. **After D47 and AZ.** |

### Hot-frame accounting

| Recorded frame | Confirmed source path and explanation | Concrete action |
|---|---|---|
| `String.equals`, **7.518%**; `Value.OtherValue.equals`, **1.285%** | Target precheck → function scan → `BagStorage.equal` → literal equality (`policy/TargetBeforeContext.java:210`, `BagStorage.java:64`). Conditions also reach `EqualityPredicates.equals` → `_eq` → value equality (`policy/function/impl/EqualityPredicates.java:106`, `:213`). Generic equality checks the value type before comparing the underlying string (`types/Value.java:145`). | D48. Identity checks already exist in both `OtherValue.equals` and JDK `String.equals`; adding another identity-only optimization provides no new shortcut. |
| `TargetBeforeContext.precheck`, **5.788%** | Combining loop → `DecisionCombiningAlgorithms.evaluateIfMatch` → precheck (`policy/combine/DecisionCombiningAlgorithms.java:67`). Each check traverses nested lists and repeatedly extracts the key, literal, and predicate before request lookup and scanning (`policy/TargetBeforeContext.java:197`). | D52 prepares those immutable facts and simplifies admission. The profile does not distinguish branch cost from inlined loads. |
| `Rule.evaluateRule`, **5.623%** | Target helper → context creation → `BaseDecisionRule.evaluate` → rule evaluation (`policy/Rule.java:117`). It checks context validity; `BaseDecisionRule.isMatch` checks it again, consumes a precheck, and reports the target result (`policy/BaseDecisionRule.java:210`). | D52’s exact-rule continuation, preserving one real validity check. Re-profile after D47; do not attribute all 5.6% to duplicate checking. |
| `itable stub`, **4.853%** | Recorded leading callers: target dispatch **665 samples / 1.370%**, bag size **507 / 1.044%**, set-context construction **326**, enclosure checks **299**, response handling **211**. `BagOfValues.size` dispatches through `BagStorage` (`BagOfValues.java:178`). Target helpers dispatch through `DecisionRule` (`policy/TargetBeforeContext.java:79`). | D50 and D52; AZ addresses part of construction. These samples prove interface dispatch occurred, not that every site is megamorphic. Preserve foreign-context enclosure behavior. |
| `getCurrentPolicy`, **2.941%** | Principally the parent check in set-context construction (`policy/PolicySet.java:372`) walking ancestors (`policy/DescendantEvaluationContext.java:215`). | **AZ already addresses this on main. No additional saving claimed.** |
| `HigherOrderFunctions.validateArguments`, **2.245%** | `Apply.evaluate` → `FunctionSpecImpl.invokeFast` → `DefaultFunctionInvocation.invokeFast` → `anyOfPair` → validation (`policy/function/impl/HigherOrderFunctions.java:83`, `:433`). It rechecks return type, bag count, predicate arity, parameter classes, and primitive types. `Apply` already validates at construction (`policy/Apply.java:194`). | D51, restricted to call sites whose static proof remains valid. |
| `DescendantEvaluationContext.isOwn`, **2.243%** | `topOf` is used by observation lookup, prechecks, staging, and preparation (`policy/DescendantEvaluationContext.java:163`; `policy/RootEvaluationContext.java:872`). It distinguishes engine contexts from external rule-context subclasses. | D52 removes repeated root discovery along its admitted path. Do not add a flag to every context merely to remove these tests. **After D47 and AZ.** |
| Legacy deny-overrides policy loop, **1.788%** | Prepared children → legacy combiner → indexed/iterator traversal → target helper (`policy/combine/impl/LegacyDenyOverridesPolicyCombineAlgorithm.java:56`). Remaining work includes child selection and outcome branches. | Reduce child-entry work through D52. Keep the combining truth table: this legacy policy algorithm turns Indeterminate into Deny, as Annex C.10 specifies. No standalone loop rewrite justified yet. |
| `getCurrentPolicySetIDReference`, **1.749%** | Reference admission, target checks, evaluation checks, and recursive cycle detection (`policy/PolicySetIDReference.java:103`, `:151`, `:173`, `:243`). Cycle detection asks the nearest-reference question twice per level; each question can itself walk ancestors. | D53. The analogous policy-reference helper already saves one getter result (`policy/PolicyIDReference.java:257`). |
| `PolicySet.Prepared.children`, **1.627%** | Every combination checks the algorithm class, obtains the generation, and looks up the prepared list (`policy/PolicySet.java:240`). The same algorithm-class answer is already stored at construction (`:92`). | Replace the repeated membership check at `:241` with `set.builtInAlgorithm`; likewise `policy/Policy.java:215`. Preserve generation, miss, validation, and index guards. **After D47 and AZ.** |
| `RegularImmutableMap.get`, **1.601%** | Mostly category lookup: handler → `RequestContext.getEntity` → `getCategory` → immutable multimap lookup (`RequestContext.java:251`, `:284`; PDP `DefaultEvaluationContextHandler.java:314`). The warmed designator-bag memo is instead a concurrent-map lookup (`AttributeIndex.java:351`). | D49. A designator key cannot hold a universal request slot: layouts differ across requests and PDPs. |
| `SemanticIdentifier.hashCode`, **1.450%** | Category lookup invokes the identifier’s three-part hash (`SemanticIdentifier.java:102`). It is intentionally recomputed: the base class exposes mutable protected fields. | D49 removes common hash calls. Do not cache the base hash; `RequestFixedCostCharacterizationTest.java:163` pins mutation compatibility. A final-`CategoryId` cache remains possible, but adds layout/serialization work for a small residual budget. |
| `BaseCompositeDecisionRule.isApplicable`, **1.329%** | Response handling tests Indeterminate and NotApplicable (`policy/BaseCompositeDecisionRule.java:188`, `:208`). `Decision.isIndeterminate` is already a field read (`Decision.java:46`). | Keep it. A self sample here does not establish avoidable method-call overhead. |
| `DefaultFunctionInvocation.invokeFast`, **1.294%** | The fixed-arity/call-shape dispatcher performs argument guards and selects fixed, logical, or higher-order execution (`policy/function/impl/DefaultFunctionInvocation.java:200`). | D51 specializes eligible higher-order sites. Do not replace the remaining dispatcher without receiver/inlining evidence. |

`Condition.evaluate` **32.290% inclusive** and `Apply.evaluate` **30.879% inclusive** overlap. They are not separate savings budgets.

A read-only inventory of the nine RSA policy files found **22 string-equality target matches over four designator keys**, with **21 distinct key/literal pairs**. Twenty literals are **41–57 characters** long. Thus repeated request values are compared against different literals; comparison-result memoization has little static coverage. All **17 RSA requests contain four categories**, supporting D49’s proposed threshold. Corpus membership is defined in `xacml-jmh/.../corpus/Rsa2008Corpus.java:65`.

### D48. Hash rejection for long built-in string equality

**Context.** RSA’s long string literals make repeated negative comparisons worth testing. JDK 25 already caches `String.hashCode`; another hash field on `StringVal` is unnecessary. Its equality implementation already checks identity. [OpenJDK 25 `String` source](https://github.com/openjdk/jdk/blob/jdk-25-ga/src/java.base/share/classes/java/lang/String.java), verified 2026-10-28.

**Mechanism.** Override equality only in final `StringVal`. Keep identity first. For two `StringVal` operands of equal length, initially **32 characters or longer**, differing underlying String hashes prove inequality. Otherwise use the existing equality behavior, including its type check and fallback for other `OtherValue` implementations. Equal hashes never prove equality. Keep `hashCode` unchanged. No interning, new field, or cache.

**Soundness.** Kept: string equality and Unicode comparison, typed equality, bag multiplicity, and all failure behavior. This is only an early rejection of unequal values. [XACML §§7.3.2, 7.20, A.3.1](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Cost and gate.** Expected **1–3%**, unmeasured; zero B/op added. Test warmed and first-use inputs: hashing can cost more than an early string mismatch. Gate on rsa2008, synthetic, and repeated-conditions, plus short/equal/unequal string benchmarks.

**Files/tests.** `types/StringVal.java`; reuse equality/hash coverage. Add only missing outcomes: long hash collisions, Unicode, equal distinct Strings, and compatible external `OtherValue` equality. Serialized fields stay unchanged.

**Open question.** Approve the guarded hash experiment? Recommend yes, with 32 as an initial threshold and removal if the controlled pair loses.

### D49. Small-request category lookup without hashing

**Context.** The hot lookup is category selection, not the designator bag. RSA requests have four categories. `RequestContext` already holds their immutable list (`RequestContext.java:63`); `Category` is final and retains its category identifier and entity (`Category.java:45`).

**Mechanism.** In `getEntity`, retain the null check. When the existing category list has at most **four entries**, scan it by index, with identifier identity followed by full equality. Return the first matching Category’s existing `optionalEntity`, or `Optional.empty`. Larger requests retain the map path. Public category collections and their order remain unchanged.

Do not put a request-dependent slot on an interned designator key. Do not add a decision-local projection or negative memo.

**Soundness.** Kept: category identifier equality, first-category selection, repeated-category behavior, request order, authoritative request bags, PIP fallback, and MustBePresent. [XACML §§7.3.4–7.3.5, 7.19.3, 7.20](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Cost and gate.** Expected **0.5–1.5%**, unmeasured; no fields, allocation, or public API. Benchmark category counts 0–8, hits at each position and misses, plus the existing PDP corpora.

**Files/tests.** `RequestContext.java`. Reuse request-order and multiple-decision coverage; add uncovered outcomes for equal nonidentical category identifiers and duplicate categories. Leave the handler and `AttributeIndex` unchanged.

**Open question.** Recommend four as the initial cutoff, retained only if measured.

### D50. Built-in bag cardinality without interface dispatch

**Context.** `size` and `isEmpty` load volatile storage and call its interface. Even one-and-only checks both (`policy/function/impl/BagFunctions.java:79`).

**Mechanism.** Keep a private transient cardinality on `BagOfValues` for the engine’s exact storage implementations. A package-private `BagStorages` helper reads their known immutable size; an unknown custom storage retains the existing delegated path. Do not call custom storage during construction to obtain a cached size. Initialize the field both in construction and after `readObject` rebuilds storage.

Use publication-safe storage for the cached scalar. Under the recorded layout, a four-byte field is expected to fill the existing padding: **32 B remains 32 B**, to be verified.

**Soundness.** Kept: duplicate counts, empty and singleton behavior, one-and-only errors/messages, custom storage behavior, equality/hash, iteration, and serialized fields. The explicit persistent-field list remains unchanged (`BagOfValues.java:65`, `:314`). [XACML §§7.3.2–7.3.5, A.3.10](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Cost and gate.** Expected **0.4–0.9%**, unmeasured; no new objects or public API. Gate on RSA, multi-valued rules, synthetic, and PIP bags.

**Files/tests.** `BagOfValues.java`, `BagStorages.java`. Reuse `BagStorageCompatibilityTest`: it already covers cardinality, custom storage, old streams, and reverse stream compatibility (`:92`, `:132`, `:200`, `:214`). Add no mechanism test.

**Open question.** Recommend caching engine storage only; preserve the custom SPI’s dispatch.

### D51. Prepared signatures for higher-order call sites

**Context.** Construction checks the higher-order signature, but evaluation repeats it. A successful load-time check alone is insufficient for arbitrary expressions, mutable custom specs, or dynamic types.

**Mechanism.** Prepare a private call-site plan for initially **any-of/all-of with one or two data operands**. Require the canonical built-in operator, a fixed FunctionReference, standard immutable parameter metadata, and statically known supported operand types. Exclude custom specs/validators and uncertain expressions from the proof.

The plan records predicate arity/types, bag position, and operator shape. It evaluates operands once in the existing order, then enters the existing higher-order body without repeating the signature walk. Retain a cheap check of the evaluated values where needed; an unexpected value takes the checked path **using those evaluated values**, never reevaluating the original expressions.

Read runtime validation once per operator call. When enabled, retain the ordinary validation route. Public direct function calls remain checked. Predicate invocation, its validation, and its exceptions remain ordinary.

Keep the plan package-private beside `Apply`/`FunctionReference`. The package boundary needs narrow implementation bridges for canonical metadata and prepared invocation; use existing types, not `Apply.CheckedArguments`, and add no `FunctionSpec` SPI method. Bridge names/signatures require API review.

**Soundness.** Kept: type errors, empty bags, predicate order, error masking, statuses/messages, and lazy logical functions. No value memoization or early resolution. [XACML §§7.4, 7.19, A.3.5, A.3.12](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Cost and gate.** Expected **0.5–1.5%**, unmeasured; preparation data only, no per-call arrays or proof objects. Gate with RSA and higher-order bag sizes, runtime validation, and custom-provider fallbacks.

**Files/tests.** `Apply`, `FunctionReference`, `FunctionSpecBuilder`, `DefaultFunctionInvocation`, `HigherOrderFunctions`, and one internal plan class. Preserve D6 binding and D45 clones. Tests pin malformed inputs, statuses/messages, later deciding values masking earlier failures, custom validation, and D34 outcomes.

**Open question.** Approve the narrow implementation bridges? Recommend approval only after API review; decline rather than expose a public proof-carrier type.

### D52. Prepared target admission and rule continuation — after D47 and AZ

**Context.** Precheck plans retain nested Match lists and repeatedly unpack immutable facts. A successful rule precheck then travels through the root slot, generic dispatch, target consumption, and repeated context checks.

**Mechanism.**

- Prepare immutable tests containing the complete designator key, literal, and proven predicate. Preserve AnyOf/AllOf/Match order; use a single-Match form and compact arrays for other plans.
- Keep the cheap empty-plan exit before root discovery. Discover the root once on a participating path.
- Under existing deciding-thread, observation, validation, request-resolution, and exact-class guards, a proven NoMatch of an inline engine child returns NotApplicable directly.
- For an exact Rule with proven Match, create or borrow its context through **D47’s existing lifecycle**, then perform the real context-validity check and enter a package-private continuation sharing the ordinary condition/response body.
- Keep pins, foreign/custom contexts, observed decisions, and occupied/reentrant admission on their existing paths. Do not leave a precheck token live during condition evaluation. Preserve cleanup in `finally`.

Do not remove FIX-AI2’s context-validity check, move a condition earlier, or introduce cached fields on every context.

**Soundness.** Kept: target truth tables, effect-specific Indeterminate, per-rule statuses, missing attributes, combining order, and response staging. Unknown scans and failures still use ordinary evaluation. [XACML §§7.6–7.7, 7.11, 7.18–7.19, Annex C](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Cost and gate.** Expected **1–3%**, unmeasured, from part of the precheck/rule/dispatch budget. Plan storage is O(matches), at policy construction; zero added decision bytes and no root fields. Re-profile after D47/AZ before setting a stronger target.

**Files/tests.** `TargetBeforeContext`, `Rule`, and small integration changes in `BaseDecisionRule`/root admission if required. **After D47 and AZ.** Preserve D45’s condition selection. Tests pin invalid contexts, distinct failing-rule statuses, worker/reentrant fallback, reference failures, observation sequences, response order, and extended passes.

**Open question.** Recommend the exact inline-rule continuation first; retain reference admission unchanged.

### D53. One ancestry walk for reference cycles — after D47 and AZ

**Context.** Recursive cycle detection repeatedly asks for the nearest reference while moving one parent upward. The nearest-reference query itself walks ancestors. The policy-set helper also repeats that query within each step.

**Mechanism.** Walk the engine-owned ancestry once and compare each actual reference owner with the entering reference, preserving the existing full equality semantics. Stop at the first foreign context or external rule-context subclass and continue with the existing public-query behavior there. Keep target-cycle and “currently evaluating” checks distinct.

No global visited set: repeated references in different branches are valid. No cached nearest-reference field or new root state.

**Soundness.** Kept: circular references and unresolved references produce the same processing-error; acyclic aliases, versions, resolution snapshots, and evaluation order remain unchanged. [XACML §7.15 and Annex C](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Cost and gate.** Expected **0.3–0.8%**, unmeasured; zero allocation/API change. Recover the caller profile first: the entire 1.749% getter share is not necessarily cycle detection.

**Files/tests.** `PolicyIDReference`, `PolicySetIDReference`; **after D47 and AZ**. Reuse reference tests, adding uncovered direct/indirect cycles, versioned aliases, sibling reuse, and foreign-context outcomes.

**Open question.** Recommend implementation only if the recovered/post-AZ profile still identifies cycle ancestry as material.

**Verification and residual risk.** Read the profile/log, traced the listed source paths, inventoried RSA XML without modifying fixtures, and fetched the official XACML standard and OpenJDK 25 String source. Ripwire’s doctor passed **7/7**; its name-based caller results were confirmed in source and treated as partial. No files changed. Runtime gains, branch behavior, and exact stack splits remain unverified; the implementation gate is independent review, reactor/conformance, and controlled same-JDK measurements including `-prof gc`.
