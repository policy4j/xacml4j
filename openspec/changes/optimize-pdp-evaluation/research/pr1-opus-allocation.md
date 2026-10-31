<!-- Lane PR1 of the second profiling swarm (after cycle H), Claude Opus 5.5, read-only; focus: allocation; report from the NTM pane, paths made repository-relative. -->

FINAL REPORT (PR1 allocation lane, Claude Opus 5.5)

## Summary

- **Inputs.** The source is profile run 021 (the cycle H jar, `jars/cycleH.jar` in `021/.../jmh.log`), with run 016 for comparison. Bytes per op are the sampled share times the run 020 `gc.alloc.rate.norm` on the same tree: rsa2008 18,774 B, synthetic:1000 `all` 135,900 B, first-deny 4,078 B, last-permit 172,100 B.
- **Method.** I attributed every collapsed stack to its deepest project frame and that frame's project callers. Object sizes come from JDK 25 default layouts: 12 B headers, compressed oops, 8 B alignment.
- **Per policy, a full scan now allocates exactly seven objects, 168 B.** Run 020 gives (172,100 − 4,078) / 1,000 = 168.0 B per policy. The seven sampled objects add up to 168 B.
  - 112 B of the 168 B (67%) can go with no decision change and no breaking API change (H2, H4, H6).
  - The two context objects (56 B) need the maintainer's BREAKING decision, which saves 24 B (H10).
- **Biggest items:**
  - Bag building and multiset iteration (H1): 22.7% of rsa2008 bytes, 16.9% of rsa2008 CPU, and 21.2% of full-scan CPU. Today it is blocked on a serialization decision. Below is a route that keeps the serialized form, so it would not need a BREAKING change.
  - The two-argument call path (H2): 41.8% of full-scan bytes.
  - Exceptions with full stack traces on the function error path (H3): 14.6% of rsa2008 CPU.

## Inventory: what is still allocated

**Per policy** (`top-alloc-synthetic_1000-all.md:77-83`). Shares are of the `all` bytes.

| Object | B | Share | Site (project caller) | Unit |
| --- | --- | --- | --- | --- |
| `Policy$PolicyDescendantEvaluationContext` | 32 | 18.6% | `Policy.java:169` ← `PolicyIDReference.java:93` ← `DenyOverrides.java:75` | policy |
| `PolicyIDReference$PolicyIDReferenceEvaluationContext` | 24 | 14.2% | `PolicyIDReference.java:80` | policy |
| `Iterators$9` | 24 | 13.8% | `Target.java:72` | target |
| `ArrayList` + `Object[2]` | 48 | 28.0% | `FunctionSpecBuilder.java:382` ← `Match.java:121` | predicate call |
| `Object[2]` (reflection) | 24 | 13.8% | `DefaultFunctionInvocation.java:96` → `FunctionInvocationFactory.java:73` | predicate call |
| `Optional` | 16 | 9.3% | `DefaultEvaluationContextHandler.java:180` ← `AttributeReference.java:108` | attribute evaluation |

**Per decision** (first-deny, 4,078 B):

| Scope | Bytes | Share |
| --- | --- | --- |
| Decision | 1,578 B | 38.7% |
| Attribute resolution (bag building is 945 B) | 1,137 B | 27.9% |
| Policy and rule | 878 B | 21.5% |
| Predicate | 410 B | 10.0% |
| Target | 75 B | 1.8% |

- About 140 B is required by the public API: `Result` 50, `ResponseContext` 18, its list 15, and the result `Status` + `StatusCode` 57.

**rsa2008** (18,774 B):

| Scope | Bytes | Share |
| --- | --- | --- |
| Predicate and function machinery | 6,692 B | 35.6% |
| Attribute resolution (bag building is 4,269 B) | 4,811 B | 25.6% |
| Policy and rule (obligation and advice staging is about 2,500 B) | 4,449 B | 23.7% |
| Decision | 2,221 B | 11.8% |
| Target | 600 B | 3.2% |

- Throwables are 1,324 B (7.05%) of the rsa2008 bytes.

## Hypotheses (most valuable first)

**H1. Array-backed `BagOfValues` that keeps today's serialized form (unblocks cycle C, design D7)**

- **Evidence**
  - rsa2008 alloc: `Entity.resolve` inclusive is 22.74% (≈ 4,269 B/op), on the chain `DefaultEvaluationContextHandler.doDesignatorResolve > Entity.resolve`. It breaks down into:
    - `BagOfValues$Builder.attributes` 8.2%;
    - `BagOfValues.<init>` 6.0%;
    - `BagOfValues$Builder.<init>` 3.1% (`LinkedHashMultiset`);
    - `Value$OtherValue.hashCode` → `Object[]` 4.2%. Each value is hashed twice: once in the `LinkedHashMultiset`, once in `RegularImmutableMultiset.create` (`top-alloc-rsa2008-all.md:78-80,88,100-102`).
  - rsa2008 CPU: `Entity.resolve` is 16.94% inclusive. Self frames: `RegularImmutableMultiset.create` 2.0%, `hashFloodingDetected` 1.3% (`top-cpu-rsa2008-all.md:79,87`).
  - first-deny: bag building is ≈ 945 B/op (23%) and `Entity.resolve` is 15.62% of CPU.
  - Full scan CPU: Guava `ImmutableMultiset` frames under `Match.match` are 21.17%. Of that, `ImmutableList$1.get` is 17.20% and `IndexedImmutableSet` is 5.98%; the self row is 14.5% (`top-cpu-synthetic_1000-all.md:77,84,102`). This is the loop `for (Value v : attributes.values())` at `Match.java:120`.
  - Source: `BagOfValues.java:64` (the `Multiset` field), `:280` (`ImmutableMultiset.builder()`, backed by `LinkedHashMultiset` in Guava 31.1-jre, `pom.xml:41`; the frames confirm it), `:74`, `:233`; `Entity.java:209-214`.
- **Change**
  - Implement D7 as written: an array in today's iteration order (equal values grouped by first occurrence), a `Multiset` view built and kept on the first `values()` call, and array access in `Match` and `value()`.
  - New part: keep the Java serialized form.
    - Declare `serialPersistentFields` = {`hashCode`: int, `type`: `BagOfValuesType`, `values`: `Multiset`}.
    - A private `writeObject` puts the `values` field from the view with `putFields`.
    - A private `readObject` reads it with `readFields` and rebuilds the array from the `Multiset` iteration order.
    - The explicit `serialVersionUID` at `BagOfValues.java:61` stays.
  - `hashCode` keeps today's value: hash through the view, or compute Σ(element hash ^ count) from the array per the Guava `Multiset` contract (UNVERIFIED against the 31.1 javadoc).
  - The builder groups with a linear scan for small bags and a `LinkedHashMap` for large ones.
- **Expected gain**
  - A one-value bag costs about 450 B today. Layout sum: `LinkedHashMultiset` 24 + `LinkedHashMap` 56 + `Node[16]` 80 + `LinkedHashMap$Entry` 40 + `Count` 16 + `Iterators$9` 24 + 2 × hash `Object[]` 48 + immutable multiset, entries, and table about 100 + entry sets 32 + `BagOfValues` 24.
  - After the change it costs about 56 B (`BagOfValues` 32 + `Value[1]` 24).
  - rsa2008: about −3,700 B/op (−20%), plus about −300 B/op of multiset iterators in `Match.match` and `BagOfValues.value`. Time about −12% (`Entity.resolve` 16.9% shrinks to the cost of an array).
  - first-deny: about −830 B/op (−20%) and about −11% time.
  - Full scan: 21.2% of 100.4 µs over about 786 iterations per op is about 27 ns per iteration. An array loop should cut the scan by −15% to −19% (estimate).
- **Risk**
  - Bag order matters in error cases (§7.6 as in D7; bug `policy4j-0972`), so the grouped order stays.
  - A.3.10 and A.3.11 results stay because `Value.equals` and `Value.hashCode` stay.
  - Thread safety: the array is immutable. The lazily built view is published racily; any copy is equal, so this is benign.
  - API: `values()` still returns a `Multiset`.
  - Serialization: adding `writeObject`/`readObject` with `serialPersistentFields` is a compatible change by the Java Object Serialization Specification (from memory, not fetched). Prove it with golden bytes.
  - Tests:
    - task 6.1 (`evidence/tests/007-cycleC-characterization`);
    - a golden-bytes test: a bag serialized by the before-tree and read by the after-tree, and the reverse;
    - `Match` error-order cases;
    - OASIS and RSA conformance.
- **Checked:** the rows and lines above, Guava 31.1-jre, and the Guava builder frames.
- **Not checked:** the serialization claim by experiment, the `Multiset` hash formula against the javadoc, and the number of bags per rsa2008 decision.

**H2. Two-argument predicate calls with no argument list and no reflective `Object[]` (finish D8)**

- **Evidence**
  - Full scan: `FunctionSpecImpl.invoke` → `ArrayList` 13.6% + its `Object[2]` 14.4% (`FunctionSpecBuilder.java:382`), and `DefaultFunctionInvocation.invoke` → `Object[]` 13.8% (`DefaultFunctionInvocation.java:96`). That is 72 B per predicate call (`top-alloc-synthetic_1000-all.md:78,80,82`).
  - Why escape analysis cannot remove them: the list goes to an unguarded `LOG.debug(..., normalizedArgs)` (`FunctionSpecBuilder.java:399,420`), to `doInvoke`, and to the interface call at `:421`. The array goes into `Method.invoke` (`FunctionInvocationFactory.java:73`). This reason is inferred.
  - CPU: `DefaultFunctionInvocation.invoke` is 12.28% of the scan, including `copyInto` 2.2% self.
  - first-deny: 222 B/op (77 + 71 + 74).
- **Change**
  - Add `default ValueExp invoke(FunctionSpec, EvaluationContext, Expression a0, Expression a1)` to `FunctionInvocation`. The default builds today's modifiable list.
  - Add default `invoke2(a, b)` and `invoke3(ctx, a, b)` to `PolicyToPlatformFunctionInvocation`; the defaults delegate to the varargs form.
  - `FunctionInvocationFactory.Default` unreflects the method once: `unreflect(m).asType(genericMethodType(n))`, then `invokeExact` with a fixed arity.
  - `FunctionSpecImpl.invoke(ctx, a0, a1)`: when `eagerPair && !validate`, evaluate the two operands into locals and call the 2-argument path. Build a debug list only inside `isDebugEnabled()`.
  - Unchanged paths: runtime validation, lazy, optional, and variadic functions, and custom implementations.
- **Expected gain**
  - Full scan: −41.8% of bytes (−56.8 KB/op on `all`; last-permit from 172.1 to 100.1 KB).
  - first-deny: −5.4%. rsa2008: −1.2% to −3.5%.
  - Time: −4% to −8% on the scan (estimate), plus fewer young collections.
- **Risk**
  - XACML: none. Argument and evaluation order and status codes are covered by the D8 tests.
  - `MethodHandle` calls do not wrap exceptions in `InvocationTargetException` (JDK javadoc, not fetched). `invocationCause` (`DefaultFunctionInvocation.java:157-167`) then finds the same first `EvaluationException` or the same root cause.
  - An `Error` must still become processing-error, so catch `Throwable` as `FunctionInvocationFactory.java:74-76` does today.
  - API: two default methods (additive, not BREAKING).
  - Tests:
    - `evidence/tests/008-cycleD-characterization-green`;
    - an `Error` thrown in a function;
    - custom `FunctionInvocation` and `PolicyToPlatformFunctionInvocation` implementations still receive a list or an array.
- **Checked:** the rows and the escape sites.
- **Not checked:** how well C2 inlines `invokeExact` on a non-constant handle. The fallback is one `LambdaMetafactory` class per function.

**H3. No wrapper exceptions on the function error path**

- **Evidence**
  - rsa2008 CPU: `Throwable.<init>` is 14.58% inclusive, in four parts (self rows `fill_in_stack_trace` 4.4% and `BacktraceBuilder::push` 2.9%, `top-cpu-rsa2008-all.md:8-9`):
    - 7.27% under `FunctionInvocationFactory$Default.doInvoke`: the `InvocationTargetException` from `Method.invoke` and `new RuntimeException(t)` at `FunctionInvocationFactory.java:76`;
    - 3.53% `CoreException.<init>`: the `FunctionInvocationException` at `DefaultFunctionInvocation.java:149`;
    - 3.78% `BagFunctions.oneAndOnlyImpl`: the `Preconditions.checkArgument` `IllegalArgumentException`, `BagFunctions.java:80-81`.
  - rsa2008 alloc: Throwables are 7.05% (1,324 B/op): `InvocationTargetException` 2.03%, `RuntimeException` 1.87%, `FunctionInvocationException` 1.75%, `IllegalArgumentException` 1.50%.
  - So every failing call creates four throwables with stack traces at a depth of about 60–70 frames.
- **Change**
  - (a) No `InvocationTargetException` and no `RuntimeException` wrapper. Use H2's handle, or unwrap the `InvocationTargetException` and rethrow a `RuntimeException` cause. Errors and checked exceptions are wrapped as today.
  - (b) Create the wrapper at `DefaultFunctionInvocation.java:149` as a private nested subclass of `FunctionInvocationException` whose `fillInStackTrace()` returns `this`. This keeps the default serialization UIDs: `FunctionInvocationException` (`@SuppressWarnings("serial")`, line 31) and `EvaluationException` have no explicit UID, so no non-private constructor is added.
  - (c) Optional, maintainer decision: built-in functions raise expected XACML errors without stack traces.
- **Expected gain**
  - rsa2008, parts (a) + (b): about −10.8% time (≈ −0.96 µs of 8.93 µs) and −1,060 B/op (−5.6%).
  - With part (c): about −14.6% time and −1,340 B/op.
  - Scan and first-deny: no change.
- **Risk**
  - XACML: none. A.3.10 one-and-only still gives Indeterminate.
  - The status comes from `FunctionInvocationException.statusFor` (`:61-75`, first `EvaluationException` in the chain, else processing-error with the root cause), and that does not change.
  - `Status.getError()` (public, `Status.java:47,139-141`) keeps the same object for (a) and (b). For (c) it would lose its stack trace.
  - Debug logs lose the wrapper frames.
  - Tests: one-and-only on an empty bag and on a two-value bag, a thrown `EvaluationException`, a `RuntimeException` with a cause chain, and an `Error`. Assert that decision, status code, status message, and the class, message, and cause of `getError()` stay the same.
- **Checked:** the rows, `statusFor`, the serialization UIDs, and the `Status` error field.
- **Not checked:** which rsa2008 requests fail.

**H4. Index loops instead of iterators on the policy tree and the request scans**

- **Evidence**
  - Full scan: `Target.match` → `Iterators$9` 13.8% (`Target.java:72`; the field is a `Collection`, `:41`).
  - rsa2008, ≈ 1,250 B/op (6.7%):
    - `MatchAllOf.match` 111 (`MatchAllOf.java:69`), `MatchAnyOf.match` 89 (`MatchAnyOf.java:61`), `Legacy*CombineAlgorithm.doCombine` 252;
    - `AttributeIndex.find` 388 (`AttributeIndex.java:124-125`, a new list and an iterator per category);
    - `RequestContext.containsAttributeValues` 255 (`RequestContext.java:288`), `getIncludeInResultAttributes` 93 (`:344` → `Category.java:106`).
  - first-deny ≈ 465 B/op: `Target` 75, `DenyOverrides.java:73` 66, `containsAttributeValues` 146, include-in-result 178.
  - first-deny CPU: include-in-result 5.17%, `containsAttributeValues` 4.31%.
  - Also the PIP lookup stream at `DefaultResolverRegistry.java:80-86`: 151 B/op on rsa2008.
- **Change**
  - Type the private fields as `ImmutableList` and loop with `for (int i...)`. Public getters stay.
  - Combining algorithms use an index loop when `decisions instanceof RandomAccess`, an iterator otherwise.
  - Add a package-private non-allocating "any attribute matches" test so include-in-result builds a list only for categories that have such attributes.
  - Replace the resolver-registry stream with a loop.
- **Expected gain**
  - Full scan: −13.8% of bytes (−18.8 KB/op).
  - first-deny: −11.4%. rsa2008: −6.7%.
  - Time: −2% to −4% on first-deny (estimate).
- **Risk**
  - Order and short-circuit stay (§7.7). The `policy4j-yeu3` and `policy4j-0972` behaviors are kept.
  - A custom non-`RandomAccess` list still uses the iterator path.
  - Iterating the request without an iterator needs an index-able view built with the request. That moves cost to request construction, which the benchmark does not measure (design D9). Measure it, or limit the change to the policy tree.
  - Tests: target, AnyOf, and AllOf truth tables; combining algorithms with a `LinkedList`.

**H5. Lazy obligation and advice staging**

- **Evidence** (rsa2008)
  - `StagedResponses.<init>` → four eager `ArrayList`s, 906 B/op (`BaseCompositeDecisionRule.java:267-270`); the object itself is 367 B/op (`:143`).
  - For-each over empty lists at `:292,303`: 833 B/op of iterators.
  - `BaseDecisionRule.evaluateObligations` and `evaluateAdvices` allocate a `LinkedList` on every call: 314 + 290 B/op (`BaseDecisionRule.java:195,244`).
  - `RootEvaluationContext.java:282,302` iterators: 157 B/op.
  - Total ≈ 2,500 B/op (13.3%). CPU: `StagedResponses` 2.85%, `evaluateAdvicesAndObligations` 2.67% (overlapping).
  - first-deny: ≈ 435 B/op.
- **Change**
  - Return `List.of()` when no expression applies.
  - Skip `addAdvices` and `addObligations` for empty collections.
  - Create the staging lists on the first add; `publish` skips empty lists.
- **Expected gain:** rsa2008 −13.3% of bytes and −2% to −4% of time. first-deny −10.7%.
- **Risk**
  - §7.18 (D9/D10): what is published and in which order stays the same; only allocation of empty lists changes.
  - A custom `EvaluationContext` no longer receives calls with empty collections.
  - Tests: `evidence/tests/009-cycleE-characterization-green` (staging), obligation and advice conformance cases.

**H6. One `Optional` per attribute and decision, not per lookup**

- **Evidence:** full scan `DefaultEvaluationContextHandler.resolve` → `Optional` 9.3%, 16 B on every memo hit (`DefaultEvaluationContextHandler.java:180`). The value also escapes into `LOG.debug` at `AttributeReference.java:109`.
- **Change:** `requestDesignators` (`:83`) stores the `Optional` returned by `Entity.resolve` (`:223-227`), and `resolve` returns the stored one. The PIP map is unchanged.
- **Expected gain:** full scan −9.3% (−12.6 KB/op); time about −1%.
- **Risk:** §7.3.5 behavior and the D5 `putIfAbsent` contract are unchanged; API: none. Tests: `evidence/tests/004-cycleA-red-green`, `006-cycleA-review-tests`.

**H7. Generic function path: fixed arity without normalization, no array copy in `Apply`, validation without lists**

- **Evidence** (rsa2008)
  - `FunctionSpecImpl.normalize`: 1,543 B/op (8.2%) and 4.38% CPU (`FunctionSpecBuilder.java:536-555`).
  - `Apply.evaluate` → `Expression[]` 263 B/op (`Apply.java:264`, `toArray` plus varargs).
  - `FunctionSpec.invoke` → `Arrays$ArrayList` 330 B/op (`FunctionSpec.java:145`).
  - `HigherOrderFunctions.requireValidArguments` and `validateArguments`: 656 B/op (new lists at `:230,252`), 1.72% CPU.
- **Change**
  - Precompute "all parameters mandatory". Then `normalize` is only a copy (`:557-573`, `:552`), so skip it and evaluate into one list. Lazy functions get one fresh modifiable copy.
  - `Apply` calls the two-argument `invoke` or `invokeWithList(invocationArguments)`.
  - Higher-order validation uses index loops, or runs once per `Apply` when all types are static.
- **Expected gain:** rsa2008 about −2,800 B/op (−14.9%) and −5% to −7% time.
- **Risk:** order unchanged; runtime validation keeps `normalize`; A.3.12 semantics (as cited in `pf3-sol-cpu.md`). Tests: D8 tests and higher-order functions with type errors (syntax-error status).

**H8. Hash codes without varargs and without whole-tree recursion**

- **Evidence**
  - `Value$OtherValue.hashCode` → `Object[]`: 885 B/op, 4.7% of rsa2008 (`Value.java:128-129`), and 6.10% CPU.
  - `RootEvaluationContext.java:199` puts each rule into a `ConcurrentHashMap<DecisionRule, Status>`. Its key hash recurses through the whole policy tree with varargs (`BaseDecisionRule.java:300-302`, `Policy.java:213-216`, `PolicySet.hashCode`). That is 312 B/op (1.66%) and 1.67% CPU on the Indeterminate path (`Match.java:135`, `DescendantEvaluationContext.java:89-95`).
- **Change**
  - Write `31 * (31 + h(a)) + h(b)`, the same value as `Objects.hash`, in `OtherValue`, `Status`, `StatusCode`, `Match`, and the rule classes.
  - Cache the structural hash of immutable policy elements. These classes are not `Serializable`; I checked `BaseDecisionRule`, `Rule`, `Policy`, `PolicySet`.
- **Expected gain:** rsa2008 −1,197 B/op (−6.4%) on its own, about −400 B/op after H1; CPU −2% to −3%.
- **Risk:** hash values stay identical, so map order is unchanged. Tests: golden hash values; `getEvaluationStatus(rule)` with an equal but distinct rule.

**H9. Fixed cost of a result and of the PDP's own reads**

- **Evidence** (first-deny)
  - `Result.ok` → `Result$Builder.<init>` → two eager `LinkedHashMap`s, 131 B/op (`Result.java:255-256`).
  - Eager varargs hash in the constructor and in `Status`/`StatusCode`: 108 B/op (`Result.java:64-69`), plus a `LinkedEntrySet` of 49 B/op.
  - A new OK status for every result: `Status.ok().build()` at `:87`, 57 B/op.
  - Maps created only to be read empty: `getMatchingObligations`/`getMatchingAdvices` 131 B/op (`RootEvaluationContext.java:451,456`, called from `DefaultPolicyDecisionPoint.java:210-211`), and `getResolvedDesignators` 67 B/op (`DefaultEvaluationContextHandler.java:157-159`, called from `DefaultPolicyDecisionPoint.java:232`).
  - CPU: `Result` frames under `requestDecision` are 13.67%.
- **Change**
  - Create the builder maps lazily, with `ImmutableMap.of()` when absent.
  - Compute `Result.hashCode` lazily.
  - Use a shared immutable OK `Status` in `Result.ok`.
  - The PDP uses internal accessors that do not create maps. The public getters keep the backed-view contract (rv006–rv008).
- **Expected gain:** first-deny about −543 B/op (−13.3%) and up to −8% time; rsa2008 about −500 B/op.
- **Risk:** `Result` equality and hash values are unchanged. A shared `Status` is visible only through `==`. Tests: `Result` equality and hash, JSON and XML marshalling, the rv006–rv008 view tests.

**H10. One context object per referenced policy (BREAKING, design D10)**

- **Evidence:** in the full scan, each referenced policy allocates two contexts, 18.6% + 14.2% of bytes (56 B per policy).
- **Change:** `PolicyIDReference.createContext` builds one context that is both the reference and the resolved policy, with the policy's variables. `Policy.createContext` already passes such a context through (`Policy.java:163-168`). Do the same for `PolicySetIDReference`.
- **Expected gain:** full scan −14.2% (−19.3 KB/op); time −1% to −2% (estimate). A variant that keeps the chain shape with a lazy view saves only about 16 B.
- **Risk:** **BREAKING**.
  - The public `getParentContext()` chain loses one level.
  - The reference context's own `evaluationStatus` copy disappears (`DescendantEvaluationContext.java:67`).
  - `isReferenceCyclic` (`PolicyIDReference.java:149-163`) and `isEnclosed` (`BaseCompositeDecisionRule.java:202-212`) still work.
  - Needs the maintainer's decision.
  - Tests: `evidence/tests/010-cycleF-characterization-green`, §7.18 staging, and a new test that records the parent chain.

**Combined effect.** If H1, H2, H4, H5, H6, H7, and H9 all land (overlaps removed), my estimate is: full scan bytes about −65%, rsa2008 bytes about −60%, first-deny bytes about −60%. Each gain still needs its own JMH pair.

## Refuted or not pursued (checked)

1. **Only guarding the debug calls** so C2 removes the pair list: not enough. The list also escapes into `doInvoke` and the interface call (`FunctionSpecBuilder.java:385,421`). The objects are still 41.8% of bytes after D8. H2 is the fix. (Escape reason inferred, not measured.)
2. **Per-request caches** for the include-in-result and multiple-decision checks: excluded by D9, because the benchmark reuses requests.
3. **Skipping the evaluated-policy map** when `ReturnPolicyIdList` is false: `getEvaluatedPolicies()` is public and can be read during evaluation. Value is small (first-deny 214 B/op).
4. **Keeping the MDC `PpId` key between `decide` calls**: changes MDC content after `decide` returns.
5. **Pooling contexts**: contexts hold per-evaluation state, and a PIP may keep a context (D5 contract).
6. **Compact object headers as the allocation fix**: after 8 B alignment only `Iterators$9` (24 → 16) and the policy context (32 → 24) shrink. That is −16 of 168 B per policy (−9.5%), by arithmetic. Cycle G stays a flag study.
7. **A decimal correlation id**: changes the id form in D1 (hex). Gain is about 48 B/op.

## Open questions

1. Maintainer: does the `serialPersistentFields` route settle D7's Open Question 1 with no BREAKING change, given a passing golden-bytes test?
2. Maintainer: H10 BREAKING decision on the parent chain (D10).
3. Maintainer: part (c) of H3. `Status.getError()` would lose its stack trace for built-in errors.
4. Which rsa2008 requests fail one-and-only? The exception share depends on that mix.
5. There is no PIP-heavy corpus (D12). The PIP path still wraps `Optional.of` per lookup (`DefaultEvaluationContextHandler.java:176-180`).
6. For the CPU lane, not allocation:
   - Unguarded Logback debug calls are 9.5% of scan CPU (turbo filters, Logback 1.3.16 at `pom.xml:40`; `CopyOnWriteArrayList.getArray` 2.2%).
   - The `isValidateFuncParamsAtRuntime` delegation chain is 3.3–3.9%.
   - Reference resolution: `ConcurrentHashMap.get` 6.6% + `String.equals` in `matches` 1.7%.

## Verification run

- Read-only scripts in `<scratch>/pr1/`:
  - `sites.py`: sites by project frame and scope, run on the three alloc sets of run 021;
  - `incl.py`: inclusive shares by pattern, CPU and alloc;
  - `chain.py`: project caller chains;
  - `peek.py`.
  - Each was run with `rtk proxy python3 -I`. Their outputs are `rsa.md`, `fd.md`, `scan.md` in the same folder.
- Sources were read and searched with `rtk rg`. Ledger and summaries of runs 016, 019, 020, 021, and `research/synthesis.md` were read.
- I did not run Maven, JMH, or a profiler. I made no git or br writes and wrote no repository file.

## Residual risk

- The profiles are 1 fork × 5 iterations of sampled allocation.
  - The full-scan inventory matches the JMH bytes exactly (168 B per policy).
  - The rsa2008 and first-deny figures are shares, not object counts.
- Time gains are estimates from CPU shares; only JMH pairs decide.
- Not tested: the escape-analysis explanations and the serialization compatibility.
- Not fetched: the Java Object Serialization Specification, the `MethodHandle` javadoc, and the Guava `Multiset` javadoc. The XACML section numbers are the ones already cited in `design.md` and `research/`.
