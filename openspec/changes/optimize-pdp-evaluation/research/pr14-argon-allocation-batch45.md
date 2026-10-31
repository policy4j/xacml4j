# Allocation Profile After Batches 4 & 5 + D24/D25 (23.1–23.3) and Proposed Batch 6 Cycles (`PA-alloc-argon`)

- **Date:** 2026-10-27
- **Author:** Lane `agyx_1` (Agy Gemini 4 Argon)
- **Tree State:** `HEAD` `8713323705611502ad0e1576554fdaa4e3fbf374` (`optimize-pdp-evaluation` worktree after Batches 4 & 5 merged — Cycles T/D29, U/D30, X/D31, Y/D32, Z/D33; D26, D27, D28 — plus D24/D25 tasks `23.1` to `23.3`: reference pinning/preparation ON, `TargetIndex` OFF)
- **JDK / JVM:** Oracle GraalVM 25.0.2+10.1 (`JAVA_HOME="$(jenv prefix 25)"`) with `-XX:-UseJVMCICompiler` (C2 JIT)
- **Artifact:** `xacml-jmh/target/benchmarks.jar` (SHA-256 `6be6340fdcfa3a7cf6ca69c43d0034fa2b985174`)
- **Baseline Compared Against:** Batch 3 JMH run `025` and `async-profiler` allocation profile `026` (`openspec/changes/optimize-pdp-evaluation/research/pr12-opus-allocation-026.md`, `LEDGER.md`)

---

## 1. JMH `-prof gc` Allocation Per Operation vs Batch 3 (Run `025` / Profile `026`)

Command parameters: `PdpDecideBenchmark`, `-f 1 -wi 3 -w 1s -i 3 -r 1s -t 1 -foe true -jvmArgs -XX:-UseJVMCICompiler -prof gc`, `store=inMemory`. Latency (`score`, `us/op`) is indicative only because other lanes share the machine; `gc.alloc.rate.norm` (`B/op`) is deterministic per operation.

| Corpus | Request | Batch 1 (`001`) B/op | Batch 3 (`025`) B/op | Current (Batch 4+5 + `23.1–23.3`) B/op | Delta vs Batch 3 (`025`) | Delta vs Batch 1 (`001`) | Indicative Latency (`us/op`, vs `025`) |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: |
| `rsa2008` | `all` | `15,431` | `10,020.4 ± 49.1` | **`6,193.6 ± 5.3`** | **`-3,826.8 B/op (-38.2%)`** | **`-9,237 B/op (-59.9%)`** | `3.74 ± 2.54` (was `4.98`) |
| `synthetic:100` | `all` | `18,622` | `7,055.0 ± 54.3` | **`6,415.1 ± 5.3`** | **`-639.9 B/op (-9.1%)`** | **`-12,207 B/op (-65.6%)`** | `4.01 ± 1.08` (was `6.31`) |
| `synthetic:1000` | `all` | `135,965` | `46,669.9 ± 59.3` | **`51,686.0 ± 412.9`** | **`+5,016.1 B/op (+10.7%)`** | **`-84,279 B/op (-62.0%)`** | `48.69 ± 22.89` (was `65.79`) |
| `synthetic:1000` | `first-deny` | `14,768` | `2,568.0 ± 0.3` | **`1,480.2 ± 4.8`** | **`-1,087.8 B/op (-42.4%)`** | **`-13,288 B/op (-90.0%)`** | `0.52 ± 0.13` (was `0.89`) |

### Key Takeaways from `-prof gc`

1. **`rsa2008|all` dropped `-38.2%` (`10,020` -> `6,193.6 B/op`)** and **`synthetic:1000|first-deny` dropped `-42.4%` (`2,568` -> `1,480.2 B/op`, `-90.0%` since Batch 1)**:
   - Cycle X (`D31`: array-backed `BagStorage` without `LinkedHashMap` / `ImmutableMultiset` for `size <= 8`) eliminated `LinkedHashMap`, `ImmutableMultiset`, and `RegularImmutableMultiset` from bag creation.
   - Cycle Y (`D32`: lazy `evaluatedPolicies` skipped unless `ReturnPolicyIdList=true` or `DEBUG` is enabled; `Result` / `ResponseContext` builder trims; `hasResolvedDesignators()`) eliminated `RootEvaluationContext.evaluatedPolicies` `ConcurrentHashMap` and `Result.Builder` / `ResponseContext.Builder` wrapper allocations.
   - Cycle U (`D30`: index loops in combining algorithms; `FunctionSpecImpl.isIndexable`; `Match` direct scan) and Cycle Z (`D33`: in-place argument evaluation in `FunctionSpecImpl.invokeNormalized`) eliminated `ImmutableList$Itr` on `Policy.rules` and `evaluate()` list copies.
2. **Why `synthetic:1000|all` increased by `+5,016 B/op` (`46,670` -> `51,686 B/op`, `+10.7%`) while its latency improved by `-26.0%` (`65.79` -> `48.69 us/op`)**:
   - In `synthetic:1000|all`, the non-child-context residue actually **decreased by `-1,088 B/op` (`~2,526 B/op` -> `~1,438 B/op`)**, matching `first-deny` (`1,480.2 B/op`).
   - However, Cycle T (`D29`) added `private final EvaluationContext top;` to `DescendantEvaluationContext` (`DescendantEvaluationContext.java:80`). Under 64-bit HotSpot with compressed oops (`-XX:+UseCompressedOops`), adding that 4-byte reference field pushed `PolicyIDReferenceEvaluationContext` (`PolicyIDReference.java:161`) across an **8-byte object alignment boundary** (from 24 B to 32 B). Across an average of ~785 visited children per operation on `synthetic:1000|all`, `785 * 8 B = +6,280 B/op` was added to `PolicyIDReferenceEvaluationContext`, yielding a net `+5,016 B/op` (`+6,280 B` child context alignment step minus `~1,264 B` saved in fixed per-decision overhead).
   - Once `23.4` (`TargetIndex`) is active, those ~784 non-matching child context pairs are skipped entirely.

---

## 2. Three Questions Answered Directly

### Q1. How much of the per-child policy and reference contexts of `synthetic:1000` (`94.8%` in profile `026`) is left with preparation on and the index off? (What `23.4`, the index, has to win.)

- **100% of the visited children's context allocations remain — now `97.22%` (`~50,248 B/op` out of `51,686.0 B/op`) on `synthetic:1000|all`, and `78.58%` (`~5,041 B/op` out of `6,415.1 B/op`) on `synthetic:100|all`**:
  - `PolicyIDReference.createContext(EvaluationContext, CompositeDecisionRule)` (`PolicyIDReference.java:92`, called by `PinnedReference.createContext` at `PinnedReference.java:50`): **`49.08%`** (`3,246 / 6,614` samples, **`~25,366 B/op`**) on `synthetic:1000|all`; **`39.53%`** (`3,466 / 8,768` samples, **`~2,536 B/op`**) on `synthetic:100|all`.
  - `Policy.createContext(EvaluationContext)` (`Policy.java:169`, called by `PolicyIDReference.createContext` at `PolicyIDReference.java:92`): **`48.14%`** (`3,184 / 6,614` samples, **`~24,882 B/op`**) on `synthetic:1000|all`; **`39.05%`** (`3,424 / 8,768` samples, **`~2,505 B/op`**) on `synthetic:100|all`.
- **Why:** Preparation (`23.3`) replaces `PolicyIDReference` children with `PinnedReference(ref, target)` (`PolicySet.java:218`), which eliminates `RootEvaluationContext.resolve(ref)` and `ref.isReferenceTo(target)` per child (improving latency by `26%`), **but without `23.4` (`TargetIndex`) `DenyOverrides.doCombine` (`DenyOverrides.java:81`) still visits every child linearly** and calls `d.createContext(context)` -> `PinnedReference.createContext` -> `new PolicyIDReferenceEvaluationContext(context)` -> `new PolicyDescendantEvaluationContext(refContext)` before `d.evaluate(...)` tests `isMatch(...)`.
- **What `23.4` (`TargetIndex`) stands to win:** Skipping the ~784 non-matching children on `synthetic:1000|all` (and ~77 on `synthetic:100|all`) will eliminate **`~50.18 KB/op` (`97.1%`) on `synthetic:1000|all`** and **`~4.98 KB/op` (`77.6%`) on `synthetic:100|all`**, collapsing both benchmarks down to the `first-deny` baseline (**`~1,480 B/op`**).

### Q2. What does preparation itself allocate per decision?

- **Almost nothing (`~14.7 B/op` directly in `RootEvaluationContext.<init>`, plus an unintended `~33.9 B/op`–`204.3 B/op` iterator regression from `PreparedList`)**:
  1. **Warm lookup in `PolicySet.Prepared.children` (`PolicySet.java:207-227`)**: **`0 B/op`** once a `PolicySet`'s `PreparedList` is cached in `Generation.sets`.
  2. **`Optional.of(prepared)` in `RootEvaluationContext.<init>` (`RootEvaluationContext.java:159`)**: **`0.99%` (`155` samples, `~14.7 B/op`) on `first-deny`** (`0.22%` / `~14.0 B/op` on `synthetic:100|all`, `0.21%` / `~13.0 B/op` on `rsa2008|all`). Because `RootEvaluationContext` wraps `PreparedPolicySets` in `Optional.of(prepared)` at line 159 and then immediately calls `prepared.map(PreparedPolicySets::generation)` at line 175 (`Optional` -> `Optional`), it allocates an `Optional` wrapper on every decision. Passing nullable `PreparedPolicySets` (or `Generation`) directly eliminates this completely.
  3. **Unintended interaction between `PreparedList` (`23.3`) and Cycle U (`U2`) combining loops (`~33.9 B/op` on `first-deny`, `~204.3 B/op` on `rsa2008|all`)**:
     In Cycle U (`U2`), all combining algorithms check:
     ```java
     Iterator<D> iterator = decisions instanceof RandomAccess && decisions instanceof ImmutableList<?>
             ? null : decisions.iterator();
     ```
     (`DenyOverrides.java:77-78`, `LegacyDenyOverridesPolicyCombineAlgorithm.java:61-62`).
     However, `PreparedList` (`PreparedPolicySets.java:68`) copies its children with `children = List.copyOf(children)`, which returns JDK `java.util.ImmutableCollections$ListN` — **not** Guava `ImmutableList<?>`. Because `decisions instanceof ImmutableList<?>` evaluates to `false` for `List.copyOf(...)`, **every prepared `PolicySet` lost `U2`'s index-loop optimization and now allocates `java.util.ImmutableCollections$ListItr` on every `PolicySet.combineDecisions` call**! Changing `List.copyOf(children)` to `ImmutableList.copyOf(children)` in `PreparedPolicySets.java:68` fixes this in 1 line.

### Q3. Which new sites appeared (or rose to the top after Batches 4 & 5 removed 38–42% of allocations)?

1. **`java.util.ImmutableCollections$ListItr` in `DenyOverrides.doCombine` (`DenyOverrides.java:78`) and `LegacyDenyOverridesPolicyCombineAlgorithm.doCombine` (`LegacyDenyOverridesPolicyCombineAlgorithm.java:62`)**:
   - **New in `23.3`**: `3.30%` (`~204.3 B/op`) on `rsa2008|all`; `2.29%` (`~33.9 B/op`) on `synthetic:1000|first-deny`; `0.44%` (`~28.5 B/op`) on `synthetic:100|all`. Caused by `PreparedList` using `List.copyOf` instead of `ImmutableList.copyOf`.
2. **`Optional.of(prepared)` in `RootEvaluationContext.<init>` (`RootEvaluationContext.java:159`)**:
   - **New in `23.2`**: `0.99%` (`~14.7 B/op`) on `synthetic:1000|first-deny`.
3. **9-Allocation Small-Bag Construction in `BagStorages` + `BagOfValues` (`BagStorages.java:45, 50, 61, 80, 81, 108, 120, 147`; `BagOfValues.java:377, 427`)**:
   - **New shape from Cycle X (`D31`)**: Cycle X removed `LinkedHashMap` and `ImmutableMultiset`, cutting bag allocations in half, **but** the new `BagStorages` implementation still allocates **9 heap objects/arrays per 1-value `VALUES` bag** (`BagOfValues$Builder`, `BagStorages$Builder`, `Value[4]` buffer in `Builder.<init>`, temporary `Value[1] distinct` and `int[1] counts` in `Builder.build` that are immediately discarded, `BagStorages$Values`, `int[2] offsets` in `Grouped.<init>`, `Value[1] values` in `Values.<init>`, and `BagOfValues`).
   - Together with `Optional.of` in `Entity.resolve` (`Entity.java:215`), this is now **#1 on `rsa2008|all` (`28.58%`, `~1,770 B/op`)** and **#1 on `synthetic:1000|first-deny` (`33.30%`, `~493 B/op`)**.
4. **Higher-order runtime validation & invocation (`HigherOrderFunctions.java:68-76, 256-274`)**:
   - Previously masked by `LinkedHashMap` and `FunctionSpecImpl.evaluate()` copies; now visibly accounts for **`5.86%` (`~363 B/op`) of `rsa2008|all`** (`validateArguments` allocating `Expression[]`, `PrimitiveTypeExpression`, `Arrays$ArrayList`, `Arrays$ArrayItr` at lines `256-274` even when `Apply` already validated the expression at build time, plus `anyOf` allocating `Expression[].clone()`, `HigherOrderFunctions$$Lambda`, and `BagStorage$1` iterator at lines `71-76`).

---

## 3. Ranked Allocation Sites by Benchmark (`async-profiler`, `event=alloc`)

### 3.1 `rsa2008` / `request=all` (`6,193.6 B/op`, down from `10,020.4 B/op` in `025`; `13,077` alloc samples)

| Rank | Subsystem / Site (`file:line`) | Leaf Allocation Type(s) | Share (%) | Est. `B/op` | Profile `026` (`B/op`, `%`) | Change vs `026` |
| ---: | --- | --- | ---: | ---: | ---: | --- |
| 1 | **Bag construction (`BagStorages` + `BagOfValues` + `Entity.resolve`)**<br>- `BagStorages.Builder.build` (`BagStorages.java:80, 81, 108`)<br>- `BagOfValues.Builder.build` (`BagOfValues.java:427`)<br>- `BagStorages.builder` (`BagStorages.java:45`)<br>- `BagStorages.Builder.<init>` (`BagStorages.java:50`)<br>- `BagStorages.Values.<init>` (`BagStorages.java:147`)<br>- `BagStorages.Grouped.<init>` (`BagStorages.java:120`)<br>- `BagOfValuesType.builder` (`BagOfValuesType.java:78`)<br>- `BagStorages.Builder.add` (`BagStorages.java:61`)<br>- `Entity.resolve` (`Entity.java:215`) | `BagStorages$Values` (`3.25%`), `int[]` (`2.83%`), `Value[]` (`2.56%`)<br>`BagOfValues` (`3.32%`)<br>`BagStorages$Builder` (`3.22%`)<br>`Value[4]` (`3.11%`)<br>`Value[]` (`2.76%`)<br>`int[]` (`2.71%`)<br>`BagOfValues$Builder` (`1.81%`)<br>`Value[]` growth (`1.70%`)<br>`Optional` (`1.31%`) | **28.58%** | **~1,770.1** | `~3,898 B/op` (`38.9%` in `ImmutableMultiset` + `LinkedHashMap` + `BagOfValues`) | **`-2,128 B/op (-54.6%)`** from Cycle X (`D31`), but 9 objects/bag remain |
| 2 | **Generic & vararg function dispatch (`Apply` + `FunctionSpecImpl` + `DefaultFunctionInvocation`)**<br>- `FunctionSpecImpl.normalize` (`FunctionSpecBuilder.java:615`)<br>- `DefaultFunctionInvocation.parameters` (`DefaultFunctionInvocation.java:351, 366`)<br>- `Apply.evaluate` (`Apply.java:264`)<br>- `FunctionSpecImpl.invoke` (`FunctionSpecBuilder.java:408`) | `ArrayList` (`3.52%`), `Object[]` (`3.44%`)<br>`Expression[]` (`3.49%`), `Object[]` (`3.37%`)<br>`Expression[]` (`4.82%`)<br>`Arrays$ArrayList` (`3.26%`) | **21.90%** | **~1,356.4** | `~2,104 B/op` (`21.0%` incl. `evaluate()` list copy) | **`-748 B/op (-35.5%)`** from Cycle Z (`D33` `evaluateInPlace`), but `Apply.toArray` -> `Arrays.asList` -> `normalize` -> `parameters` remain |
| 3 | **Policy, PolicySet, Reference, and Rule evaluation contexts**<br>- `Rule.createContext` (`Rule.java:69`)<br>- `PolicySet.createContext` (`PolicySet.java:184`)<br>- `Policy.createContext` (`Policy.java:169`)<br>- `PolicyIDReference.createContext` (`PolicyIDReference.java:92`) | `RuleEvaluationContext` (`9.13%`)<br>`PolicySetDescendantEvaluationContext` (`3.30%`)<br>`PolicyDescendantEvaluationContext` (`2.39%`)<br>`PolicyIDReferenceEvaluationContext` (`1.42%`) | **16.24%** | **~1,005.8** | `~852 B/op` (`8.5%`) | **`+154 B/op`** (`DescendantEvaluationContext.top` field added in Cycle T / `D29`) |
| 4 | **Higher-order & logical function evaluation**<br>- `HigherOrderFunctions.validateArguments` (`HigherOrderFunctions.java:256, 270, 274`)<br>- `HigherOrderFunctions.anyOf` (`HigherOrderFunctions.java:71, 73`)<br>- `DecidingOperands.firstDeciding` (`DecidingOperands.java:78`)<br>- `LogicalFunctions.operands` / `or` / `and` (`LogicalFunctions.java:74, 84, 119`) | `PrimitiveTypeExpression` (`1.38%`), `Expression[]` (`0.76%`), `Arrays$ArrayList` (`0.73%`), `Arrays$ArrayItr` (`0.68%`)<br>`Expression[].clone` (`0.54%`), `Lambda` (`0.50%`)<br>`Arrays$ArrayItr` (`0.97%`), `BagStorage$1` (`0.54%`)<br>`Arrays$ArrayList` (`0.84%`), `Lambda` (`0.96%`) | **7.90%** | **~489.3** | `~491 B/op` (`4.9%`) | Unchanged in bytes; larger relative share |
| 5 | **MDC & correlation ID formatting (queued for removal in D34)**<br>- `MDCSupport.setXacmlRequestId` (`MDCSupport.java:74, 75`)<br>- `DefaultPolicyDecisionPointContextFactory.createContext` (`PolicyDecisionPointContextFactory.java:147-149`)<br>- `MDCSupport.setPdpContext` (`MDCSupport.java:53`) | `byte[]`, `String`, `HashMap$Node` (`2.85%`)<br>`byte[]`, `String`, `Context$1` (`2.37%`)<br>`HashMap$Node` (`0.64%`) | **5.86%** | **~362.8** | `~361 B/op` (`3.6%`) | Unchanged (removed by **D34**) |
| 6 | **Per-decision handler & `requestDesignators` memo**<br>- `DefaultEvaluationContextHandler.doDesignatorResolve` (`DefaultEvaluationContextHandler.java:233`)<br>- `DefaultEvaluationContextHandler.<init>` (`DefaultEvaluationContextHandler.java:91`)<br>- `DefaultPolicyDecisionPointContextFactory$1.createEvaluationContext` (`PolicyDecisionPointContextFactory.java:209, 214`)<br>- `RootEvaluationContext.<init>` (`RootEvaluationContext.java:159, 177`) | `ConcurrentHashMap$Node[]` (`1.72%`), `Node` (`1.91%`)<br>`ConcurrentHashMap` (`0.83%`)<br>`RootEvaluationContext` (`1.26%`), `DefaultEvaluationContextHandler` (`0.80%`), `Lambda` (`0.38%`)<br>`AtomicReference` (`0.38%`), `Optional` (`0.21%`) | **7.49%** | **~463.9** | `~892 B/op` (`8.9%`) | **`-428 B/op (-48.0%)`** from Cycle Y (`D32` lazy `evaluatedPolicies`) |
| 7 | **`PreparedList` combining-loop iterator regression (`23.3` vs `U2`)**<br>- `LegacyDenyOverridesPolicyCombineAlgorithm.doCombine` (`LegacyDenyOverridesPolicyCombineAlgorithm.java:62`) | `ImmutableCollections$ListItr` (`3.30%`) | **3.30%** | **~204.3** | `~210 B/op` (`ImmutableList$Itr`, removed by `U2` then reintroduced by `23.3` `List.copyOf`) | **New regression in `23.3`** |
| 8 | **Obligation/advice staging wrapper (queued for removal in D36)**<br>- `BaseCompositeDecisionRule.evaluate` (`BaseCompositeDecisionRule.java:127, 166`) | `StagedResponses` (`2.22%` + `0.26%` ext-indet) | **2.48%** | **~153.5** | `~150 B/op` (`1.5%`) | Unchanged (removed by **D36**) |
| 9 | **Request chain & result/response wrappers**<br>- `Result.Builder.build` (`Result.java:407`)<br>- `DefaultPolicyDecisionPoint.decide` (`DefaultPolicyDecisionPoint.java:102`)<br>- `MultipleResourcesBudgetHandler.handle` (`MultipleResourcesBudgetHandler.java:82`)<br>- `AbstractRequestContextHandler.handleNext` (`AbstractRequestContextHandler.java:79`)<br>- `ResponseContext.Builder.results` / `build` (`ResponseContext.java:89, 108`) | `Result` (`0.72%`)<br>`NumberedDecisions` (`0.39%`)<br>`Budget` (`0.40%`)<br>`Collections$SingletonSet` (`0.33%`)<br>`SingletonImmutableList` (`0.42%`), `ResponseContext` (`0.36%`) | **2.62%** | **~162.3** | `~531 B/op` (`5.3%`) | **`-369 B/op (-69.4%)`** from Cycle Y (`D32`) |

---

### 3.2 `synthetic:1000` / `request=first-deny` (`1,480.2 B/op`, down from `2,568.0 B/op` in `025`; `15,647` alloc samples)

This profile represents the **steady-state floor per decision once `23.4` (`TargetIndex`) skips non-matching children**:

| Rank | Subsystem / Site (`file:line`) | Leaf Allocation Type(s) | Share (%) | Est. `B/op` | Profile `026` (`B/op`, `%`) | Change vs `026` |
| ---: | --- | --- | ---: | ---: | ---: | --- |
| 1 | **Bag construction (`BagStorages` + `BagOfValues` + `Entity.resolve`)**<br>- `BagStorages.builder` (`BagStorages.java:45`)<br>- `BagStorages.Builder.build` (`BagStorages.java:80, 81, 108`)<br>- `BagOfValues.Builder.build` (`BagOfValues.java:427`)<br>- `BagStorages.Builder.<init>` (`BagStorages.java:50`)<br>- `BagStorages.Grouped.<init>` (`BagStorages.java:120`)<br>- `BagStorages.Values.<init>` (`BagStorages.java:147`)<br>- `Entity.resolve` (`Entity.java:215`)<br>- `BagOfValuesType.builder` (`BagOfValuesType.java:78`) | `BagStorages$Builder` (`4.68%`)<br>`BagStorages$Values` (`4.52%`), `int[]` (`3.27%`), `Value[]` (`3.14%`)<br>`BagOfValues` (`4.23%`)<br>`Value[4]` (`4.19%`)<br>`int[]` (`3.30%`)<br>`Value[]` (`3.18%`)<br>`Optional` (`2.01%`)<br>`BagOfValues$Builder` (`0.78%`) | **33.30%** | **~492.9** | `~927 B/op` (`36.1%`) | **`-434 B/op (-46.8%)`** from Cycle X (`D31`), now 1/3 of entire decision |
| 2 | **Per-decision handler, `RootEvaluationContext`, & `requestDesignators`**<br>- `DefaultEvaluationContextHandler.doDesignatorResolve` (`DefaultEvaluationContextHandler.java:233`)<br>- `DefaultEvaluationContextHandler.<init>` (`DefaultEvaluationContextHandler.java:91`)<br>- `DefaultPolicyDecisionPointContextFactory$1.createEvaluationContext` (`PolicyDecisionPointContextFactory.java:209, 214`)<br>- `RootEvaluationContext.<init>` (`RootEvaluationContext.java:159, 177`) | `ConcurrentHashMap$Node[]` (`7.16%`), `Node` (`3.11%`)<br>`ConcurrentHashMap` (`3.26%`)<br>`RootEvaluationContext` (`5.35%`), `DefaultEvaluationContextHandler` (`3.81%`), `Lambda` (`1.63%`)<br>`AtomicReference` (`1.04%`), `Optional` (`0.99%`) | **26.35%** | **~390.0** | `~665 B/op` (`25.9%`) | **`-275 B/op (-41.4%)`** from Cycle Y (`D32` lazy `evaluatedPolicies`) |
| 3 | **Logical & generic function dispatch (`and` in Rule Condition)**<br>- `FunctionSpecImpl.normalize` (`FunctionSpecBuilder.java:615`)<br>- `DefaultFunctionInvocation.parameters` (`DefaultFunctionInvocation.java:351, 366`)<br>- `Apply.evaluate` (`Apply.java:264`)<br>- `FunctionSpecImpl.invoke` (`FunctionSpecBuilder.java:408`)<br>- `LogicalFunctions.and` / `operands` (`LogicalFunctions.java:74, 84`)<br>- `DecidingOperands.firstDeciding` (`DecidingOperands.java:78`) | `ArrayList` (`2.26%`), `Object[]` (`2.20%`)<br>`Object[]` (`1.97%`), `Expression[]` (`1.92%`)<br>`Expression[]` (`2.96%`)<br>`Arrays$ArrayList` (`2.16%`)<br>`Lambda` (`1.04%`), `Arrays$ArrayList` (`1.04%`)<br>`Arrays$ArrayItr` (`1.01%`) | **16.56%** | **~245.1** | `~339 B/op` (`13.2%`) | **`-94 B/op (-27.7%)`** from Cycle Z (`D33`) |
| 4 | **MDC & correlation ID formatting (queued for removal in D34)**<br>- `MDCSupport.setXacmlRequestId` (`MDCSupport.java:74, 75`)<br>- `DefaultPolicyDecisionPointContextFactory.createContext` (`PolicyDecisionPointContextFactory.java:147-149`)<br>- `MDCSupport.setPdpContext` (`MDCSupport.java:53`) | `byte[]` (`2.13%`), `HashMap$Node` (`1.72%`), `String` (`1.10%`)<br>`byte[]` (`2.91%`), `Context$1` (`1.64%`), `String` (`1.64%`)<br>`HashMap$Node` (`1.77%`) | **12.91%** | **~191.1** | `~195 B/op` (`7.6%`) | Unchanged (removed by **D34**) |
| 5 | **Single matching child contexts (`PolicySet`, `PolicyIDReference`, `Policy`, `Rule`, `StagedResponses`)**<br>- `BaseCompositeDecisionRule.evaluate` (`BaseCompositeDecisionRule.java:127`)<br>- `PolicySet.createContext` (`PolicySet.java:184`)<br>- `PolicyIDReference.createContext` (`PolicyIDReference.java:92`)<br>- `Policy.createContext` (`Policy.java:169`)<br>- `Rule.createContext` (`Rule.java:69`) | `StagedResponses` (`2.20%`)<br>`PolicySetDescendantEvaluationContext` (`2.12%`)<br>`PolicyIDReferenceEvaluationContext` (`2.10%`)<br>`PolicyDescendantEvaluationContext` (`2.01%`)<br>`RuleEvaluationContext` (`1.83%`) | **10.26%** | **~151.9** | `~144 B/op` (`5.6%`) | `+8 B/op` (`DescendantEvaluationContext.top`); `StagedResponses` (`32.5 B/op`) removed by **D36** |
| 6 | **Request chain, `PreparedList` iterator, & `Result`/`ResponseContext`**<br>- `Result.Builder.build` (`Result.java:407`)<br>- `DenyOverrides.doCombine` (`DenyOverrides.java:78`)<br>- `MultipleResourcesBudgetHandler.handle` (`MultipleResourcesBudgetHandler.java:82`)<br>- `DefaultPolicyDecisionPoint.decide` (`DefaultPolicyDecisionPoint.java:102`)<br>- `ResponseContext.Builder.results` / `build` (`ResponseContext.java:89, 108`)<br>- `AbstractRequestContextHandler.handleNext` (`AbstractRequestContextHandler.java:79`) | `Result` (`3.31%`)<br>`ImmutableCollections$ListItr` (`2.29%`)<br>`Budget` (`1.63%`)<br>`NumberedDecisions` (`1.61%`)<br>`SingletonImmutableList` (`1.12%`), `ResponseContext` (`1.11%`)<br>`Collections$SingletonSet` (`0.97%`) | **12.04%** | **~178.2** | `~300 B/op` (`11.7%`) | **`-122 B/op (-40.7%)`** from Cycle Y (`D32`), offset by `+34 B/op` `ListItr` in `23.3` |

---

### 3.3 `synthetic:100` / `request=all` (`6,415.1 B/op`, down from `7,055.0 B/op` in `025`; `8,768` alloc samples)

| Rank | Subsystem / Site (`file:line`) | Leaf Allocation Type(s) | Share (%) | Est. `B/op` | Profile `026` (`B/op`, `%`) |
| ---: | --- | --- | ---: | ---: | ---: |
| 1 | `PolicyIDReference.createContext` <- `PinnedReference.createContext` (`PolicyIDReference.java:92`) | `PolicyIDReferenceEvaluationContext` | **39.53%** | **~2,535.9** | `~2,208 B/op` (`31.3%`) |
| 2 | `Policy.createContext` <- `PolicyIDReference.createContext` (`Policy.java:169`) | `PolicyDescendantEvaluationContext` | **39.05%** | **~2,505.1** | `~2,215 B/op` (`31.4%`) |
| 3 | Bag construction (`BagStorages` + `BagOfValues` + `Entity.resolve`) | `BagStorages$Values`, `BagOfValues`, `BagStorages$Builder`, `Value[]`, `int[]`, `Optional` | **6.22%** | **~399.0** | `~924 B/op` (`13.1%`) |
| 4 | Per-decision handler, `RootEvaluationContext`, & `requestDesignators` (`DefaultEvaluationContextHandler`, `PolicyDecisionPointContextFactory`) | `RootEvaluationContext`, `ConcurrentHashMap`, `Node[]`, `Node`, `DefaultEvaluationContextHandler` | **6.00%** | **~384.9** | `~663 B/op` (`9.4%`) |
| 5 | MDC & correlation ID formatting (`MDCSupport`, `createContext`) | `byte[]`, `String`, `HashMap$Node` | **3.58%** | **~229.7** | `~226 B/op` (`3.2%`) |
| 6 | `Rule.createContext` (`Rule.java:69`), `PolicySet.createContext` (`PolicySet.java:184`), `StagedResponses` (`BaseCompositeDecisionRule.java:127`) | `RuleEvaluationContext` (`1.14%`), `PolicySetDescendantEvaluationContext` (`0.57%`), `StagedResponses` (`0.59%`) | **2.30%** | **~147.8** | `~148 B/op` (`2.1%`) |
| 7 | `DenyOverrides.doCombine` (`DenyOverrides.java:78`) | `ImmutableCollections$ListItr` | **0.44%** | **~28.5** | `0 B/op` after `U2` (new in `23.3`) |

---

### 3.4 `synthetic:1000` / `request=all` (`51,686.0 B/op`, vs `46,669.9 B/op` in `025`; `6,614` alloc samples)

| Rank | Subsystem / Site (`file:line`) | Leaf Allocation Type(s) | Share (%) | Est. `B/op` | Profile `026` (`B/op`, `%`) |
| ---: | --- | --- | ---: | ---: | ---: |
| 1 | `PolicyIDReference.createContext` <- `PinnedReference.createContext` (`PolicyIDReference.java:92`) | `PolicyIDReferenceEvaluationContext` | **49.08%** | **~25,366.2** | `~22,122 B/op` (`47.4%`) |
| 2 | `Policy.createContext` <- `PolicyIDReference.createContext` (`Policy.java:169`) | `PolicyDescendantEvaluationContext` | **48.14%** | **~24,881.7** | `~22,122 B/op` (`47.4%`) |
| 3 | Bag construction (`BagStorages` + `BagOfValues` + `Entity.resolve`) | `BagStorages$Values`, `BagOfValues`, `BagStorages$Builder`, `Value[]`, `int[]`, `Optional` | **0.88%** | **~454.8** | `~933 B/op` (`2.0%`) |
| 4 | Per-decision handler, `RootEvaluationContext`, & `requestDesignators` | `RootEvaluationContext`, `ConcurrentHashMap`, `Node[]`, `Node`, `DefaultEvaluationContextHandler` | **0.76%** | **~392.8** | `~653 B/op` (`1.4%`) |
| 5 | MDC & correlation ID formatting (`MDCSupport`, `createContext`) | `byte[]`, `String`, `HashMap$Node` | **0.35%** | **~180.9** | `~187 B/op` (`0.4%`) |
| 6 | Logical & generic function dispatch (`FunctionSpecImpl`, `Apply`, `DefaultFunctionInvocation`, `LogicalFunctions`) | `ArrayList`, `Object[]`, `Expression[]`, `Arrays$ArrayList` | **0.36%** | **~186.1** | `~327 B/op` (`0.7%`) |
| 7 | `Rule.createContext`, `PolicySet.createContext`, `StagedResponses`, `Result`, `ResponseContext` | Contexts & wrappers | **0.43%** | **~223.5** | `~326 B/op` (`0.7%`) |

---

## 4. Proposed Cycles for Batch 6 (5 Cycles: AA–AE)

Before Batch 6 starts, note what **already-queued work** will remove from the current numbers:
- **`23.4` (`TargetIndex`)**: removes **`~50.18 KB/op` (`97.1%`) on `synthetic:1000|all`** and **`~4.98 KB/op` (`77.6%`) on `synthetic:100|all`**, bringing both down to `first-deny` (`~1,480 B/op`).
- **D34 (`DecisionObserver` SPI + `MDCSupport` removal, task group 36)**: removes **`~363 B/op` (`5.86%`) on `rsa2008|all`** and **`~191 B/op` (`12.91%`) on `synthetic:1000|first-deny`**.
- **D36 (`StagedResponses` removal)**: removes **`~154 B/op` (`2.48%`) on `rsa2008|all`** and **`~33 B/op` (`2.20%`) on `synthetic:1000|first-deny`**.

After `23.4`, D34, and D36 land, the remaining allocations are **`~5,677 B/op` on `rsa2008|all`** and **`~1,256 B/op` on `synthetic:1000` (`all` and `first-deny`)**. The 5 cycles below target **75–80%** of that remaining budget:

| Cycle | Name & Target Files | Expected Savings (`rsa2008|all`) | Expected Savings (`syn1000|first-deny` & indexed `syn1000|all`) | Risk | Collisions with Queued Work |
| --- | --- | ---: | ---: | :---: | --- |
| **AA** | **Single-Value & Empty `BagStorage` Fast Path** (`BagStorages`, `BagOfValues`, `BagOfValuesType`, `Entity`) | **`~1,400–1,550 B/op` (`22.6–25.0%`)** | **`~380–430 B/op` (`25.7–29.0%`)** | Low | **None** (disjoint from all queued tasks; can run immediately) |
| **AB** | **Direct List/Binary Function Dispatch & Zero-Alloc Higher-Order/Logical Evaluation** (`Apply`, `FunctionSpecBuilder`, `DefaultFunctionInvocation`, `HigherOrderFunctions`, `LogicalFunctions`, `DecidingOperands`) | **`~1,550–1,800 B/op` (`25.0–29.1%`)** | **`~180–215 B/op` (`12.2–14.5%`)** | Low–Med | **None** (disjoint from all queued tasks; can run immediately) |
| **AC** | **`PreparedList` `ImmutableList` Fix & Target-Before-Context Evaluation for Non-Matching Rules/Policies** (`PreparedPolicySets`, `Rule`, `Policy`, `PinnedReference`, `PolicyIDReference`) | **`~650–850 B/op` (`10.5–13.7%`)** | **`~34–65 B/op` (`2.3–4.4%`)** + **`~50 KB/op` on unindexed `syn1000|all`** | Med | Collides with **Terra's D24/D25 fix & `23.4`**, **D36**, and **D34**; fold the 1-line `PreparedList` fix into `23.4` now, run the rest after D34 & D36 |
| **AD** | **Compact Request-Designator Memo & Per-Decision Context/Handler Trim** (`DefaultEvaluationContextHandler`, `RootEvaluationContext`, `PolicyDecisionPointContextFactory`, `AbstractRequestContextHandler`) | **`~320–400 B/op` (`5.2–6.5%`)** | **`~240–290 B/op` (`16.2–19.6%`)** | Low | Collides with **D34**, **Fable's task 30 (`pip.forDecision()`)**, and **Terra's D24/D25 fix**; schedule after D34 and task 30 merge |
| **AE** | **Memoized Single-Value Bags on Immutable `AttributeIndex`** (`AttributeIndex`, `Entity`) | **`~250–350 B/op` (`4.0–5.6%`)** | **`~80–110 B/op` (`5.4–7.4%`)** | Low | Complements Cycle AA; disjoint from queued PDP/PIP work |

---

### Cycle AA: Single-Value & Empty `BagStorage` Fast Path

- **Target Sites (`file:line`)**:
  - `BagStorages.java:45, 50, 61, 80, 81, 108, 120, 147` (`BagStorages.builder`, `Builder.<init>`, `Builder.add`, `Builder.build`, `Grouped.<init>`, `Values.<init>`)
  - `BagOfValues.java:82-85, 377-428` (`BagOfValues.<init>`, `BagOfValues.Builder`)
  - `Entity.java:209-216` (`Entity.resolve(String, ValueType, String)`)
- **What & Why**:
  1. Currently, every 1-value `VALUES` bag resolved from the request (`Entity.resolve` at `Entity.java:215`) allocates **9 heap objects/arrays**: `BagOfValues$Builder`, `BagStorages$Builder`, `Value[4]` in `Builder.<init>`, temporary `Value[1] distinct` and `int[1] counts` in `Builder.build()` (immediately discarded after `Values.<init>`), `BagStorages$Values`, `int[2] offsets` in `Grouped.<init>`, `Value[1] values` in `Values.<init>`, and `BagOfValues`.
  2. Introduce `BagStorages.Single` (1 field `final Value<?> value`; `size() = 1`, `distinctSize() = 1`, `multisetHashCode() = value.hashCode() ^ 1`, direct `equal`, `compareIntegers`, `compareDoubles`, `count`, `valueAt(0)`) and `BagStorages.Empty` (singleton).
  3. Add a package-private fast factory `BagOfValues.ofSingle(BagOfValuesType type, Value<?> value)` (validating `assertExpressionType(value, type)` and constructing `new BagOfValues(type, new BagStorages.Single(value))` — or holding `Single` without `BagOfValues$Builder` / `BagStorages$Builder`). Use it in `Entity.resolve` (`Entity.java:215`) when `v.size() == 1` and in `BagFunctions.*Bag(T... values)` when `values.length == 1`.
  4. In `BagStorages.Builder`, store the first element in `private Value<?> first` and allocate `Value<?>[] values` only when the 2nd element is added; when `size == 0` return `Empty.INSTANCE`; when `size == 1` return `new Single(first)` (0 arrays!). For `2 <= size <= 8` in `Kind.VALUES`, when all elements are distinct (`distinctSize == size`), pass `distinct` directly to `Values` without allocating a second `Value<?>[size]` copy in `Values.<init>`.
- **Expected Share & Risk**:
  - **`22.6–25.0%` (`~1,400–1,550 B/op`) on `rsa2008|all`**; **`25.7–29.0%` (`~380–430 B/op`) on `synthetic:1000|first-deny`**.
  - **Risk:** Low. Strictly internal to `org.xacml4j.v30` bag storage representation.
- **Semantics to Keep**:
  - XACML 3.0 §7.3.2 bag multiset semantics: `size()`, `distinctSize()`, `valueAt(0)`, `distinctValueAt(0)`, `distinctCountAt(0)`, `contains(v)`, `equals(Object)`, `hashCode()` (`31 * (31 + (value.hashCode() ^ 1)) + type.hashCode()`), `assertExpressionType` (rejecting `DynamicType` and mismatched `ValueType`), IEEE `compareDoubles` (`NaN` and `-0.0`), and Java serialization (`writeObject`/`readObject`).
- **Collisions with Queued Work**:
  - **None.** Completely disjoint from Terra's extended-Indeterminate pass fix, D24/D25 (`23.4`), D36, D34, and Fable's task 30 / Y fixes.

---

### Cycle AB: Direct List/Binary Function Dispatch & Zero-Alloc Higher-Order/Logical Evaluation

- **Target Sites (`file:line`)**:
  - `Apply.java:264` (`Apply.evaluate`)
  - `FunctionSpecBuilder.java:386-424, 606-651` (`FunctionSpecImpl.invokeWithList`, `invoke`, `normalize`)
  - `DefaultFunctionInvocation.java:185-196, 330-393` (`DefaultFunctionInvocation.create`, `invoke`, `parameters`)
  - `HigherOrderFunctions.java:62-105, 111-171, 227-285` (`anyOf`, `allOf`, `anyOfAny`, `allOfAny`, `anyOfAll`, `allOfAll`, `validateArguments`)
  - `LogicalFunctions.java:67-85, 113-120` (`and`, `or`, `operands`)
  - `DecidingOperands.java:54-93` (`anyTrue`, `allTrue`, `firstDeciding`)
- **What & Why**:
  1. **Eliminate `Apply.evaluate` `Expression[]` + `Arrays.asList` (`8.08%` of `rsa2008`, `5.12%` of `first-deny`)**:
     In `Apply.java:264`, `invocationArguments` is already an `ImmutableList<Expression>`. Calling `spec.invoke(context, invocationArguments.toArray(new Expression[]{}))` allocates an `Expression[]` on every `Apply.evaluate` call, and for every non-`eagerSingle`/`eagerPair` function `FunctionSpecImpl.invoke` (`FunctionSpecBuilder.java:408`) immediately wraps that array in `Arrays.asList(arguments)` and calls `invokeWithList`!
     Instead, have `Apply.evaluate` call `spec.invokeWithList(context, invocationArguments)` directly, and move the 1-arg (`eagerSingle && direct != null`) and 2-arg (`eagerPair`) fast-path dispatch into `FunctionSpecImpl.invokeWithList` (`FunctionSpecBuilder.java:386`).
  2. **Direct `(EvaluationContext, List<Expression>)` invocation for lazy variadic functions (`and`, `or`, `n-of`)**:
     When `evaluateParameters == true` (`and`, `or`, `n-of`) and `!context.isValidateFuncParamsAtRuntime()`, `invocationArguments` needs no eager evaluation and (when `arguments.size() >= mandatoryCount` with no omitted optional parameters) no `normalize()` copy (`FunctionSpecBuilder.java:615`), no `Object[] params` (`DefaultFunctionInvocation.java:351`), and no `Expression[] varArgArray` (`DefaultFunctionInvocation.java:366`).
     In `LogicalFunctions.and` and `LogicalFunctions.or` + `DecidingOperands`, add an index-loop overload over `List<Expression>` (and `Expression[]` for direct callers) that calls `isTrue(context, list.get(i))` directly without `Arrays.asList(values)`, `Arrays$ArrayItr`, or capturing `LogicalFunctions$$Lambda` (`2.77%` of `rsa2008`, `3.09%` of `first-deny`).
  3. **Zero-allocation `HigherOrderFunctions.validateArguments` and binary `anyOf`/`allOf` fast path (`5.86%` of `rsa2008`)**:
     - In `HigherOrderFunctions.validateArguments(FunctionReference, Expression[], boolean, boolean)` (`HigherOrderFunctions.java:248-285`), validate primitive parameter specs directly against `arguments[i].getEvaluatesTo().getValueType()` without allocating `new Expression[arguments.length]`, `new PrimitiveTypeExpression(...)`, `Arrays.asList(primitives)`, or `ListIterator` when `ref` has 1 or 2 mandatory parameters (`FunctionParamValueTypeSpec.isValidParamType(type.getValueType())`).
     - In `HigherOrderFunctions.anyOf` (`HigherOrderFunctions.java:62-77`) and `allOf` (`HigherOrderFunctions.java:90-105`), when `arguments.length == 2` (the standard binary predicate case `any-of(pred, scalar, bag)` or `any-of(pred, bag, scalar)`):
       - For `anyOf` with `bagIndex == 1 && arguments[0] instanceof Value<?> literal && !bag.isEmpty()`, try `ref.scan(context, literal, bag)` first (just like `Match.match` at `Match.java:114`).
       - Otherwise, iterate `bag` by index `for (int i = 0, n = bag.size(); i < n; i++)` using `bag.valueAt(i)` (with `DecidingOperands` error- deferral state inlined in the loop) and call `ref.invoke(context, arg0, arg1)` (2-arg direct call!) instead of `arguments.clone()` + `HigherOrderFunctions$$Lambda` + `BagStorage$1` iterator + vararg `ref.invoke(context, parameters)`.
- **Expected Share & Risk**:
  - **`25.0–29.1%` (`~1,550–1,800 B/op`) on `rsa2008|all`**; **`12.2–14.5%` (`~180–215 B/op`) on `synthetic:1000|first-deny`**.
  - **Risk:** Low–Medium. Self-contained in `xacml-core` function dispatch and higher-order/logical functions.
- **Semantics to Keep**:
  - XACML 3.0 Annex C / §A.3.5 (`and`, `or`, `n-of`) and §A.3.12 (`any-of`, `all-of`, `any-of-any`, `map`): strict left-to-right operand evaluation; short-circuit on the first deciding operand (`True` for `or`/`any-of`, `False` for `and`/`all-of`); if an earlier operand throws `EvaluationException` (Indeterminate) and a later operand evaluates to the deciding boolean, the deciding boolean wins; if no operand decides and at least one failed, rethrow the **first** `EvaluationException`.
  - Runtime parameter validation when `context.isValidateFuncParamsAtRuntime()` is `true`, and direct Java calls to `HigherOrderFunctions.anyOf(...)` with invalid arguments must still throw `EvaluationException` with `Status.syntaxError()`.
- **Collisions with Queued Work**:
  - **None.** Cycle Z (`D33`) is already merged.

---

### Cycle AC: `PreparedList` `ImmutableList` Fix & Target-Before-Context Evaluation for Non-Matching Rules/Policies

- **Target Sites (`file:line`)**:
  - `PreparedPolicySets.java:65-70` (`PreparedList`)
  - `Rule.java:65-70, 107-142` (`Rule.createContext`, `Rule.evaluate`)
  - `BaseDecisionRule.java:51, 129-144` (`BaseDecisionRule.target`, `isMatch`)
  - `Target.java:39, 67-76` (`Target.match`) and `Match.java:43, 108-134` (`Match.match`)
  - `PolicyIDReference.java:88-93` and `PinnedReference.java:48-62`
- **What & Why**:
  1. **1-Line `PreparedList` Fix (`PreparedPolicySets.java:68`)**:
     Change `children = List.copyOf(children);` to `children = ImmutableList.copyOf(children);`.
     Because `DenyOverrides.doCombine` (`DenyOverrides.java:77`) and all other combining algorithms from Cycle U (`U2`) test `decisions instanceof RandomAccess && decisions instanceof ImmutableList<?>`, using `ImmutableList.copyOf(children)` immediately restores index-loop iteration for every prepared `PolicySet`, eliminating `ImmutableCollections$ListItr` (`3.30%` / `~204.3 B/op` on `rsa2008|all`, `2.29%` / `~33.9 B/op` on `first-deny`).
  2. **Evaluate `Rule` Target Before Allocating `RuleEvaluationContext` (`9.13%` / `~565.3 B/op` on `rsa2008|all`)**:
     Note that `23.4` (`TargetIndex`) indexes `PolicySet` children, **not** `Rule`s inside a `Policy`. In `rsa2008|all`, `Rule.createContext` allocates ~18 `RuleEvaluationContext` instances per decision (`565.3 B/op`), ~80% of which immediately return `MatchResult.NOMATCH` on their `Target`!
     Why was `RuleEvaluationContext` created before `Target.match`? Only because:
     - `BaseDecisionRule.isMatch(context)` checks `isEvaluationContextValid(context)` (`this == context.getCurrentRule()`), and
     - If `Match.match(context)` catches `EvaluationException`, line 130 calls `context.setEvaluationStatus(context.getCurrentRule(), e.getStatus())`.
     When the caller in a combining algorithm (or a package-private `Rule.evaluateInPolicy(EvaluationContext policyContext)` entry point called when `policyContext` is an internal `DescendantEvaluationContext`) evaluates `target` first:
     - If `target` is `MatchResult.NOMATCH` (and no predicate required `RuleEvaluationContext`, which holds whenever `target` uses standard `direct` predicates), return `Decision.NOT_APPLICABLE` immediately with **0 context allocations**!
     - Only when `target` is `MATCH` (and the `Rule` has a `Condition`, `Advice`, `Obligation`, or an active D34 `DecisionObserver`) or `INDETERMINATE` is `RuleEvaluationContext` needed (and on `MATCH` with `condition == null` and no obligations/advice, `Rule` simply returns `effect.toDecision()` with **0 allocations**!).
  3. **Single Context (or Target-First Precheck) for `PinnedReference` (`PolicyIDReference` -> `Policy`)**:
     When `TargetIndex` (`23.4`) is off (the default on `PolicyDecisionPointBuilder` unless `.targetIndex()` is called!) or when a `TargetIndex` bucket has multiple candidate policies, `PinnedReference.createContext` (`PinnedReference.java:50` -> `PolicyIDReference.java:92`) allocates **two** 32-byte contexts (`PolicyIDReferenceEvaluationContext` + `PolicyDescendantEvaluationContext` = 64 B) per child policy before testing `Policy.getTarget()`.
     - Pre-evaluating `target.getTarget()` on the parent context when all `Match` predicates in `target.getTarget()` are `direct` (`!evalContextRequired`) and `target.getDefaults() == null` skips both context allocations on `MatchResult.NOMATCH` (saving up to `~50.2 KB/op` on unindexed `synthetic:1000|all`).
- **Expected Share & Risk**:
  - **`10.5–13.7%` (`~650–850 B/op`) on `rsa2008|all`** (`~204 B/op` from `PreparedList` + `~450–650 B/op` from `Rule` / `Policy` contexts); **`2.3–4.4%` (`~34–65 B/op`) on `first-deny`**; and **`~50 KB/op` (`97%`) on `synthetic:1000|all` when `targetIndex` is off**.
  - **Risk:** Low for `PreparedList` (1-line change); Medium for `Rule` / `PinnedReference` target-before-context evaluation.
- **Semantics to Keep**:
  - XACML 3.0 §7.11 (`Rule` evaluation), §7.12 (`Policy` evaluation), §7.15 (`PolicyIdReference` cyclic check `isReferenceCyclic`):
    - On `MatchResult.INDETERMINATE` in `Match.match(context)` or `BaseDecisionRule.isMatch(context)`, `top.setEvaluationStatus(rule, status)` and `context.setEvaluationStatus(status)` must record the exact `Rule` (or `Policy`) and `Status` as before.
    - Any subclass of public `Rule.RuleEvaluationContext` (`!Rule.isOwnContext(context)`) or any `Target` containing a non-`direct` function (`evalContextRequired == true`) must take the existing context-first path so custom functions and custom context subclasses see `context.getCurrentRule()` / `context.getCurrentPolicy()`.
- **Collisions with Queued Work**:
  - `PreparedPolicySets.java:68` touches the same file as **Terra's D24/D25 fix and `23.4` (`TargetIndex`)** — recommend folding the 1-line `ImmutableList.copyOf` change into `23.4`.
  - `Rule.java`, `Policy.java`, `BaseCompositeDecisionRule.java` touch the same methods as **D36 (`StagedResponses`)** and **D34 (`DecisionObserver` SPI)**. Schedule the context-elision part of Cycle AC after D34 and D36 merge.

---

### Cycle AD: Compact Request-Designator Memo & Per-Decision Context/Handler Trim

- **Target Sites (`file:line`)**:
  - `DefaultEvaluationContextHandler.java:91, 192, 228-238` (`requestDesignators` `ConcurrentHashMap`)
  - `PolicyDecisionPointContextFactory.java:209-225` (`createEvaluationContext`, `request::getEntity` lambda)
  - `RootEvaluationContext.java:87, 102, 158-181, 298-307` (`Optional<Generation>`, `AtomicReference<Duration> combinedDecisionCacheTTL`)
  - `AbstractRequestContextHandler.java:78-80` (`Collections.singleton`) and `ResponseContext.java:86-94` (`ResponseContext.Builder.results`)
- **What & Why**:
  1. **Replace eager `new ConcurrentHashMap<>()` for `requestDesignators` in `DefaultEvaluationContextHandler` (`13.53%` / `~200.1 B/op` on `first-deny`, `4.46%` / `~276.4 B/op` on `rsa2008`)**:
     Every decision eagerly allocates `new ConcurrentHashMap<>()` in `DefaultEvaluationContextHandler.<init>` (`DefaultEvaluationContextHandler.java:91`), and the first resolved request attribute allocates `ConcurrentHashMap$Node[16]` (80 B) + `ConcurrentHashMap$Node` (32 B per attribute).
     Most decisions resolve only 1–6 distinct `AttributeDesignatorKey`s from the request. Replace the eager `ConcurrentHashMap` with a lazy `VarHandle`-published compact table (or a lock-free / synchronized open-addressed `Object[16]` array storing `AttributeDesignatorKey, Optional<BagOfValues>` pairs for `<= 6` keys, spilling to `ConcurrentHashMap` only when a 7th distinct request key is added).
  2. **Eliminate `Optional<PreparedPolicySets>`, `Optional<Generation>`, and `AtomicReference<Duration>` in `RootEvaluationContext.<init>` (`2.03%` / `~30.0 B/op` on `first-deny`)**:
     - Store nullable `Generation generation` directly in `RootEvaluationContext` (`RootEvaluationContext.java:87, 158-175`) instead of wrapping `PreparedPolicySets` in `Optional.of(prepared)` and calling `prepared.map(PreparedPolicySets::generation)`.
     - Replace `private AtomicReference<Duration> combinedDecisionCacheTTL = new AtomicReference<>(defaultDecisionCacheTTL)` (`RootEvaluationContext.java:102, 177`) with `private volatile Duration combinedDecisionCacheTTL = defaultDecisionCacheTTL` updated via a `VarHandle` CAS loop in `setDecisionCacheTTL(Duration)` (matching the 9 existing `VarHandle` fields in `RootEvaluationContext`). `setDecisionCacheTTL` is never called unless a PIP resolver sets a custom TTL, so this saves an `AtomicReference` allocation on 100% of decisions.
  3. **Pass `RequestContext` (or make `RequestContext` implement `RequestContextCallback`) instead of `request::getEntity` lambda (`1.63%` / `~24.1 B/op` on `first-deny`)**:
     In `PolicyDecisionPointContextFactory.java:209`, `new DefaultEvaluationContextHandler(request::getEntity, pip)` allocates a capturing lambda on every decision. Having `DefaultEvaluationContextHandler` accept `RequestContext` (or `request` directly) eliminates that lambda.
  4. **Return `ImmutableList.of(context.requestDecision(request))` in `AbstractRequestContextHandler.handleNext` (`AbstractRequestContextHandler.java:79`)**:
     Currently `handleNext` wraps the single `Result` in `Collections.singleton(...)` (`14.4 B/op`), and `ResponseContext.Builder.results` (`ResponseContext.java:89`) immediately copies that `SingletonSet` into a new `SingletonImmutableList` (`16.6 B/op`) because `Collections.singleton` is not an `ImmutableList`. Returning `ImmutableList.of(...)` in `handleNext` lets `ImmutableList.copyOf(r)` in `ResponseContext.Builder.results` reuse the same `SingletonImmutableList` instance with zero copy!
- **Expected Share & Risk**:
  - **`16.2–19.6%` (`~240–290 B/op`) on `synthetic:1000|first-deny`** (and indexed `synthetic:1000|all`); **`5.2–6.5%` (`~320–400 B/op`) on `rsa2008|all`**.
  - **Risk:** Low.
- **Semantics to Keep**:
  - XACML 3.0 §7.3.5: within one decision, the first non-empty `Optional<BagOfValues>` resolved from the request for a given `AttributeDesignatorKey` wins (`putIfAbsent` semantics) and is returned identically on every subsequent test of that key in the same decision, including under concurrent D26 attribute evaluation. Missing request attributes (`Optional.empty()`) are never stored in `requestDesignators` so PIP fallback still runs.
- **Collisions with Queued Work**:
  - Touches `DefaultEvaluationContextHandler`, `PolicyDecisionPointContextFactory`, and `RootEvaluationContext`, which collide with **D34 (`DecisionObserver` SPI)**, **Fable's task 30 (`PolicyInformationPoint.forDecision()`)**, and **Terra's D24/D25 fix**. Schedule Cycle AD after D34 and task 30 merge.

---

### Cycle AE: Memoized `Optional<BagOfValues>` on Immutable `AttributeIndex` (`Entity`)

- **Target Sites (`file:line`)**:
  - `Entity.java:48, 209-216` (`Entity.resolve(String, ValueType, String)`)
  - `AttributeIndex.java` (`findValues`)
- **What & Why**:
  - Both `Entity` and its backing `AttributeIndex` are **deeply immutable** once built (`Entity.Builder.build()`).
  - Yet `Entity.resolve(String id, ValueType type, String issuer)` (`Entity.java:209-216`) looks up `Collection<Value<?>> v = attributeIndex.findValues(id, type, issuer)` and constructs a **new** `BagOfValues` and a **new** `Optional.of(bag)` on every decision (and if an `Entity` is resolved via an `Entity` attribute or selector, on every lookup).
  - If `AttributeIndex` (which already indexes attributes by `(id, type, issuer)` in an immutable table at `Entity` build time) stores or lazily memoizes the `Optional<BagOfValues>` per attribute entry (or builds the `BagOfValues` in `AttributeIndex` instead of a `List<Value<?>>` that is only ever converted to a `BagOfValues` in `Entity.resolve`), then `Entity.resolve(id, type, issuer)` returns a pre-built `Optional<BagOfValues>` in **0 bytes** for any reused `Entity` / `RequestContext`, and for a freshly built `RequestContext` avoids building both a `List<Value<?>>` in `AttributeIndex` and a separate `BagOfValues` in `Entity.resolve`!
- **Expected Share & Risk**:
  - Saves up to **`~493 B/op` (`33.3%`) on `first-deny`** and **`~1,600 B/op` (`25.8%`) on `rsa2008|all`** for reused `Entity` instances, and **`~80–350 B/op`** even for single-use `RequestContext` instances by not duplicating `AttributeIndex` storage and `BagStorage`.
  - **Risk:** Low. `Entity`, `AttributeIndex`, and `BagOfValues` are all immutable value types in `xacml-core`.
- **Semantics to Keep**:
  - `Entity.findValues(id, type, issuer)` must still return an unmodifiable `Collection<T>` in insertion order, and `Entity.resolve(id, type, issuer)` must return `Optional.empty()` when no matching values exist and `Optional.of(BagOfValues)` when present.
- **Collisions with Queued Work**:
  - **None** with queued PDP/PIP tasks (D24/D25, D34, D36, task 30). Interacts only with **Cycle AA** (`BagOfValues` / `BagStorages`), so AA and AE can be combined or sequenced.
