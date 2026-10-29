=== BEGIN REPORT P-FIXED2-opus ===
FINAL REPORT (policy4j_implementer, Claude Opus 5.5): P-FIXED2, what is left on the current main tree

## Summary

This packet is analysis only. No code changed.

- **Since P-FIXED:**
  - rsa2008 `all`: 3,052 → 1,231 B/op (−60%); 2.65 → 1.92 µs/op (indicative).
  - `narrow`: 4,936 → 384 B/op (−92%); 1.59 → 0.44 µs/op with the index off, 0.31 µs/op with it on.
- **`narrow` is ten fixed objects (384 B).**
  - Root context 104 B, handler 48 B, generation `Optional` 16 B.
  - Three descendant contexts, 96 B.
  - `Result` 48 B and its builder 40 B (C2 keeps the builder on this path).
  - `ResponseContext` and its list, 32 B.
  - Nothing grows with the request: the IncludeInResult (IIR) path, the precheck and the lookups allocate nothing.
- **rsa2008 (1,231 B) by phase:**

  | Phase | B/op | Share |
  | --- | ---: | ---: |
  | Descendant contexts | 477 | 39% |
  | Obligation and advice path | 446 | 36% |
  | Decision setup | 169 | 14% |
  | `Result` and response | 65 | 5% |
  | Indeterminate failures | 47 | 4% |
  | Bag iterators | 28 | 2% |

- **The response path is the largest item left.**
  - Each obligation passes through four containers between its rule and the `Result`: the rule's list, a staging list, a root `ConcurrentHashMap` and the `Result` map. Views are then created over them.
  - Carrying immutable lists end to end saves about 380 B/op on rsa2008 (−31%) and about 2.3 KB on `responses` (−44%).
- **CPU on rsa2008 (1.92 µs/op):**

  | Phase | Share | µs |
  | --- | ---: | ---: |
  | Policy evaluation | 80% | 1.54 |
  | Precheck | 16% | 0.32 |
  | Response | 2.1% | 0.04 |
  | Handlers | 1.4% | 0.03 |

  - Inside evaluation:
    - conditions and functions 23%;
    - walks up the context chain 17%;
    - combining 14%;
    - obligations and advice 9%;
    - attribute lookup 8%.
  - The handlers and the response no longer matter; the time is in evaluation.
- **Two cheap CPU items:**
  - An extended-Indeterminate walk to the root, twice per applicable policy and policy set, in decisions that record nothing: 6.2% of rsa2008.
  - The function call shape and the higher-order argument checks on every call: 5.7% of rsa2008 and 4.4% of `multi-valued`. These are in Terra's FIX-AS files.
- **Lanes:**
  - No item touches Fable's regex files: no regex frame is under `decide` in these workloads.
  - Two items touch Terra's function package.
  - Everything else is outside both lanes.

## Method

1. Built the current main tree (my worktree after the reset; 133 files changed against HEAD 87133237):
   `mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am -DskipTests package`, JDK 25 (GraalVM, C2 through `-XX:-UseJVMCICompiler`). The jar was pinned before the runs.
2. Every run used `PdpDecideBenchmark.decide`, `-p store=in-memory`, 5 × 1 s warmup and 5 × 1 s measurement, `-prof gc`, and `-Djmh.ignoreLock=true` as instructed.
   - **Baselines:** 3 forks, `-prof gc` only.
   - **Allocation:** 1 fork with async-profiler 4.5, `event=alloc;alloc=64k;output=jfr`, converted with `jfrconv --alloc --total --lines -o collapsed` to byte-weighted stacks with line numbers.
   - **CPU:** 1 fork with `event=cpu;interval=100000;output=collapsed`.
3. **Converting to B/op:** the B/op of a stack is its share of the window's bytes × the fork's `gc.alloc.rate.norm`. 99.97 to 99.995% of the window's bytes are under `decide`.
   - Every site matches whole objects: the narrow root context is 104.4 B/op for one 104 B object, and the three contexts are 31.8 to 32.3 B/op for one 32 B object each.
   - Per-site error is therefore about ±3%, as in P-FIXED.
4. **CPU phases:** a stack belongs to the first top-level phase it contains:
   1. response (`createResult`, `ResponseContext.of`);
   2. context setup (`createEvaluationContext`);
   3. precheck (`TargetBeforeContext.precheck`);
   4. policy evaluation (any other `org.xacml4j.v30.policy` frame);
   5. handlers (the rest of the decide path).

   Inside evaluation, a stack belongs to the sub-phase that matches its deepest frame. The shares are multiplied by the 3-fork unprofiled µs/op.
5. **Times are indicative.** Load average was 6.3 to 12.2 with other lanes active. B/op is the evidence.

### Runs (unprofiled, 3 forks)

| Workload | B/op | µs/op |
| --- | ---: | ---: |
| rsa2008 `all`, index off | 1,231.1 ± 0.0 | 1.924 ± 0.095 |
| `rules:100 narrow`, index on | 384.1 ± 0.3 | 0.313 ± 0.010 |
| `rules:100 narrow`, index off (the P-FIXED setting) | 384.2 ± 0.4 | 0.441 ± 0.021 |
| `rules:100 responses`, index on | 5,056.7 ± 107.0 (forks 4,977 to 5,193) | 1.915 ± 0.046 |
| `rules:100 multi-valued`, index on | 1,025.6 ± 3.6 | 3.745 ± 0.454 |
| `synthetic:100 all`, index off | 315.5 ± 0.2 | 4.475 ± 0.604 |
| `synthetic:100 all`, index on | 315.4 ± 0.0 | 0.371 ± 0.005 |

- The profiled forks measured the same B/op, except `responses`: 5,281.0 (allocation fork) and 5,112.7 (CPU fork).
- The `responses` forks differ by up to 300 B. That is escape-analysis variance: C2 scalar-replaces some builders and iterators in one fork and not in another.

## `narrow`: every byte (384.1 B/op; profiled fork 384.0)

| Phase | Site | Object | B/op |
| --- | --- | --- | ---: |
| A decision setup | `PolicyDecisionPointContextFactory.java:245` | `RootEvaluationContext` (104 B) | 104.4 |
| | `PolicyDecisionPointContextFactory.java:240` | `DefaultEvaluationContextHandler` (48 B) | 48.4 |
| | `RootEvaluationContext.java:209` | `Optional.of(prepared.generation())` | 16.0 |
| C contexts | `PolicySet.java:196` | root policy set context | 31.8 |
| | `Policy.java:200` | policy context | 32.3 |
| | `Rule.java:74` | rule context | 32.0 |
| F2 result | `Result.java:506` | `Result` | 48.1 |
| | `Result.java:90` (`Result.ok`) | `Result.Builder`, not scalar-replaced here | 38.5 |
| F1 response | `ResponseContext.java:64` | `ResponseContext` 16.1 and `SingletonImmutableList` 16.5 | 32.6 |
| **Total** | | **10 objects** | **384.1** |

**CPU of `narrow` (0.313 µs/op):**

| Phase | Share |
| --- | ---: |
| Precheck | 29.1% |
| Combining, including the root's index lookup | 29.0% |
| Response | 14.7% |
| Walks up the context chain | 10.2% |
| Attribute lookup | 6.0% |
| Handlers | 4.1% |
| Obligations and advice | 3.9% |

Inside the response phase:
- The empty advice and obligation views (`LateValues`) read their map field through a `VarHandle` held in an instance field. That costs 7.4% (`VarHandleGuards.guard_L_L` and `getVolatile`), although C2 removes the allocation.
- The IIR duplicate check (`Result.java:323-333`) costs 3.5%.

## rsa2008: every byte (1,231.1 B/op; profiled fork 1,231.0)

| Phase | Site | Object | B/op |
| --- | --- | --- | ---: |
| **A decision setup (169.0, 14%)** | `PolicyDecisionPointContextFactory.java:245` | `RootEvaluationContext` | 103.5 |
| | `PolicyDecisionPointContextFactory.java:240` | handler | 48.4 |
| | `RootEvaluationContext.java:209` | generation `Optional` | 17.1 |
| **C descendant contexts (477.0, 39%)** | `PolicySet.java:196` | policy set contexts (5.0 per decision) | 160.3 |
| | `PolicySetIDReference.java:143` (`enter`) | merged reference contexts (4.0) | 126.8 |
| | `Rule.java:74` | rule contexts (3.7) | 118.5 |
| | `Policy.java:200` | policy contexts (2.2) | 71.5 |
| **E1 rule response lists (62.5)** | `BaseDecisionRule.java:330`, `:332` | `ArrayList` 12.4, its `Object[10]` 28.8 | 41.2 |
| | `BaseDecisionRule.java:325` | for-each iterator over the expressions | 21.2 |
| **E2 staging (187.6)** | `RootEvaluationContext.java:771`, `:773` (`Staged.staged`) | `ArrayList` 38.2, `Object[10]` 88.5 | 126.7 |
| | `RootEvaluationContext.java:693` (`frame`) | `Staged` frames | 60.9 |
| **E3 root response maps (101.0)** | `RootEvaluationContext.java:267`, `:272` | `ConcurrentHashMap` | 34.1 |
| | `RootEvaluationContext.java:427` | table 42.0, nodes 17.2 | 59.2 |
| | `RootEvaluationContext.java:428` | `computeIfPresent` lambda | 7.7 |
| **F result and response (159.9, of which 95.3 belongs to the response path)** | `Result.java:506` | `Result` | 48.5 |
| | `RootEvaluationContext.java:921`, `:915` | `LateValues` 36.4, `ConcurrentHashMap` values view 6.7 | 43.1 |
| | `Result.java:456` (`distinctById`) | `SingletonImmutableBiMap` | 22.0 |
| | `ResponseContext.java:62` → `Result.java:177` | `values()` of the bimap: its inverse 23.5 and key set 6.7 | 30.2 |
| | `ResponseContext.java:64` | `ResponseContext` and list | 16.2 |
| **S status and Indeterminate failures (46.5)** | `RootEvaluationContext.java:282`, `:308` | status map, table and nodes | 20.0 |
| | `DefaultFunctionInvocation.java:409`, `:236`; `BagFunctions.java:82` | `FunctionInvocationException` 7.7, `BagSizeException` 5.2 | 12.9 |
| | `Status`, `StatusCode` builders; `FunctionInvocationException.java:46`, `:71` | statuses 8.3, varargs 3.3, lambda 2.0 | 13.6 |
| **D bag iterators (27.5)** | `Match.java:127`, `BagOfValues.java:273` → `BagStorage.java:102` | `BagStorage$1` | 27.5 |
| **Total** | | | **1,231.1** |

**The response path in total:** E1 + E2 + E3 + 95.3 B of F = **446.4 B/op (36%)**.

## The other workloads

**`responses`** (5,281 B/op profiled; index on; `ReturnPolicyIdList` true):

| Phase | B/op |
| --- | ---: |
| Obligation and advice expressions and response objects | 1,943.5 |
| Staging | 759.7 |
| Rule lists | 741.2 |
| Root maps | 523.5 |
| `Result` maps | 635.7 |
| Policy identifier list | 272.9 (plus 185 inside the `Result`) |
| Contexts | 188.6 |
| Setup | 167.4 |
| Response | 47.4 |

The expression row is made of:
- a `LinkedHashMultimap` per builder (`DecisionRuleResponse.java:112`);
- its `ImmutableSetMultimap` copy (`:53`);
- `AttributeAssignment.hashCode` varargs, `Object[4]` per call (`AttributeAssignment.java:109`): 190.8 B/op.

**`multi-valued`** (1,025.6 B/op; index on):

| Phase | B/op | Detail |
| --- | ---: | --- |
| Contexts | 576.2 | policy contexts 259.3 (8.1 per decision) and rule contexts 251.2: 8 of the 9 roles select a policy |
| `TargetIndex` candidates for the bag of 9 roles | 275.8 | see below |
| Setup | 171.8 | |

The `TargetIndex` cost:
- `TargetIndex.java:109`: `BitSet` clone, 39.7.
- `:115`: growth of its `long[]`, 33.5.
- `:129-133`: `ImmutableList.builder()` growing from capacity 4, 187.5, and the list, 15.1.

The functions allocate nothing since cycle AS.

**`synthetic:100`** (315.5 B/op with the index off, 315.4 with it on):

| Phase | B/op |
| --- | ---: |
| Setup | 167.5 |
| Root policy set context, merged reference context (`PolicyIDReference.java:161`) and rule context | 91.7 |
| `Result` | 50.1 |
| Builder (mostly scalar-replaced) | 5.9 |

The 100 target checks allocate nothing. The index removes their time: 4.475 → 0.371 µs/op.

## CPU: where the time goes

| Workload (µs/op) | Handlers | Precheck | Policy evaluation | Response |
| --- | ---: | ---: | ---: | ---: |
| rsa2008 (1.924) | 1.4% (0.03) | 16.4% (0.32) | 80.0% (1.54) | 2.1% (0.04) |
| `narrow` (0.313) | 4.1% | 29.1% | 51.8% | 14.7% |
| `responses` (1.915) | 1.6% | 7.2% | 75.7% (obligations and advice alone 47.5%) | 15.4% |
| `multi-valued` (3.745) | 0.2% | 18.2% | 80.6% (conditions and functions alone 47.9%) | 0.8% |
| `synthetic:100`, index off (4.475) | 1.0% | 72.9% | 25.3% | 0.7% |
| `synthetic:100`, index on (0.371) | 7.3% | 15.0% | 69.9% (`TargetIndex.candidates` about 28% inclusive) | 7.0% |

**rsa2008 policy evaluation (80.0%):**

| Sub-phase | Share | µs |
| --- | ---: | ---: |
| Conditions and functions | 23.0% | 0.44 |
| Walks up the context chain | 17.1% | 0.33 |
| Combining, the rest | 14.1% | 0.27 |
| Obligations and advice | 9.2% | 0.18 |
| Attribute lookup | 8.2% | 0.16 |
| Context creation | 4.5% | 0.09 |
| Target match in context | 1.9% | |
| Reference cycle checks | 1.4% | |
| Status | 0.6% | |

Notes:
- **Walks up the context chain, inclusive** (`rsa-cpu-*.txt`):
  - `isExtendedIndeterminateEval` 6.2%: `DescendantEvaluationContext.java:150-152` goes parent by parent to the root, and `BaseCompositeDecisionRule.respond` (`:200`) and `addEvaluationResult` (`DescendantEvaluationContext.java:239`) each walk once per applicable composite rule.
  - The cycle checks `isReferenceCyclic` and `isReferenceContext`: 2.8%.
  - Other queries, 10.0%: the validity re-checks in the constructors (`PolicySet.java:193`, then `:360-363` again), `Rule.isEvaluationContextValid`, `isEnclosed`, `observationOf`, `topOf`, `isOwn`.
- **Function leaves** (`rsa-cpu-function-leaves.txt`):
  - `FunctionSpecImpl.fastShape` → `isVariadic` → `ArrayList.get`, recomputed on every call (`FunctionSpecBuilder.java:494`, `:500-508`, `:464-466`): 4.3%.
  - `string-subset` → `BagOfValues.containsAll` → `count` (`BagOfValues.java:197-203`, `:271-277`): 4.1%. This is the scan itself.
  - `HigherOrderFunctions.requireValidArguments` on every any-of pair (`:419-492`): 1.4%.
  - Exception unwinding for the Indeterminate requests: 1.0%.
- **The precheck time is the target matching itself**, done before context creation. Its share is the size of the target work: 72.9% of `synthetic` with the index off. The index is the lever: −92% time.

## Ranked: what is left by B/op

Savings are B/op from the profiles. "Computed" means derived from the field layout, not measured. Rank key: bytes saved on rsa2008 plus `narrow`, then the other workloads.

| # | Item | rsa2008 | `narrow` | Others | Active lane |
| --- | --- | ---: | ---: | --- | --- |
| 1 | Responses as immutable lists from rule to `Result` | −380 (−31%) | 0 | `responses` −2.3 KB | none |
| 2 | Rules evaluated in their policy's context | −101 (computed) | −24 | `multi` −186, `responses` −48, `synthetic` ≈ −20 | none (needs a design decision) |
| 3 | Decision setup objects | −56 (−16 measured, −40 computed) | −56 | −56 in every workload | none |
| 4 | Engine `Result` without the builder | 0 (scalar-replaced) | −38.5 | `synthetic` −6 | none |
| 5 | Bag scans without iterators | −27.5 | 0 | 0 | none |
| 6 | Indeterminate failures: status map and shared statuses | −25 to −46 | 0 | 0 | Terra (FIX-AS) for the function-package part |
| 7 | Non-constant obligations and advice without the multimap builder | 0 (constant responses since AN) | 0 | `responses` ≈ −1.5 KB | none; the `AttributeAssignment` hash is AT's mechanism outside `types` |
| 8 | Policy identifier list | 0 | 0 | `responses` ≈ −0.36 KB | none |
| 9 | `TargetIndex` candidates for multi-valued bags | 0 | 0 | `multi` −173 to −230 | none (Terra's index in batch 2) |

If all nine are done, the totals are about (UNVERIFIED sums):
- rsa2008: 1,231 → about 620 to 640 B/op (about −49%);
- `narrow`: 384 → about 265 (−31%);
- `responses`: about 5.3 → 1.0 KB;
- `multi-valued`: 1,026 → about 550 to 610;
- `synthetic`: 315 → about 235.

### 1. Responses as immutable lists from rule to `Result`

**Sites.** The E1, E2, E3 and response rows of the rsa2008 table:
- `BaseDecisionRule.java:278-335`: `new ArrayList<>()` per evaluation, and for-each over the expressions.
- `RootEvaluationContext.java:767-776`: `Staged.staged` copies into a default-capacity `ArrayList`.
- `RootEvaluationContext.java:388-429`: a `ConcurrentHashMap` per decision and kind, `putIfAbsent`, then a capturing `computeIfPresent`.
- `RootEvaluationContext.java:911-922`: `LateValues` or a values view.
- `Result.java:406-472` (`distinctById`) and `:176-199` (`values()` views).
- `ResponseContext.java:59-65`: `getObligations().isEmpty()` builds the bimap's inverse.

**Mechanism.**
1. Each rule keeps its Permit and Deny expression lists, built at construction. When all of them are constant (cycle AN), it also keeps the response list itself, so an evaluation allocates nothing.
2. A frame stages an `ImmutableList` by reference and copies only to concatenate, at exact size.
3. The root keeps one immutable list per decision and kind, published with a compare-and-set. It merges by id as today.
4. `createResult` hands that list to the `Result`. The ids are distinct by construction, so the `Result` keeps it as a list (lookup by id is a scan, as cycle AM did for IIR) and tests emptiness without views.

**Saving.**
- rsa2008: 62.5 + 126.7 + 101.0 + 95.3 − about 5 = **about −380 B/op**.
- `responses`: 741.2 + 641.6 + 523.5 + 369.1 + 48.0 = **about −2.3 KB**.
- `narrow`: 0 B, but −0.02 µs (the `VarHandle` reads).
- Time: rsa2008 about −0.05 µs, `responses` about −0.3 µs (UNVERIFIED).

**Files:**
- `BaseDecisionRule.java`;
- `RootEvaluationContext.java` (the staging members and the response maps);
- `Result.java`, `ResponseContext.java`;
- `DefaultPolicyDecisionPoint.java` (`createResult`).

**XACML risk: low.**
- §7.18: the same responses of the matching decision, the same merge by id and the same order.
- D22: `Result.hashCode` keeps the map's value (Σ id hash ^ value hash), as AM did for IIR.
- Contract to keep: `getMatchingObligations` and `getMatchingAdvices` return a live view that sees later writes and allows removal (`RootEvaluationContext.java:71-77`, rv006 to rv010, R-YZ). A view over the list held by the compare-and-set keeps that contract.
- Adopt only `ImmutableList` instances; copy anything else, because custom code may pass a list it changes later.
- Split it in two:
  - 1a: rule lists and staging by reference (rsa2008 −189);
  - 1b: root lists and `Result` lists (rsa2008 −190).

**Lane:** none. `BaseDecisionRule` was Fable's file in batch 2; the staging members are mine from FIX-AN2.

### 2. Rules evaluated in their policy's context

**Site.** `Rule.java:70-75`: `createContext` makes a 32 B context per evaluated rule, although `Rule.java:64-68` already says that "rule evaluation shares a context with a parent policy".

**Mechanism.** Under built-in rule algorithms on the deciding thread of an unobserved decision, the policy context answers `getCurrentRule()` from a slot that the algorithm sets before each rule.
- The policy context grows from 32 to 40 B: it has no padding left (parent, top, status, variables and the outer reference).
- Custom algorithms and observed decisions keep a context per rule.
- Cycle AP designed a per-policy reusable rule context: about −48 B on rsa2008, and nothing on `multi` or `narrow`, which evaluate one rule per policy. The slot design saves more.

**Saving (computed):**

| Workload | Saving |
| --- | --- |
| rsa2008 | −118.5 + 2.2 × 8 = −101 |
| `multi` | −251.2 + 8.1 × 8 = −186 |
| `narrow` | −24 |
| `responses` | −48 |
| `synthetic` | about −20 |

**Files:** `Rule.java`, `Policy.java`, `DescendantEvaluationContext.java`, `TargetBeforeContext.java` (its `getCurrentRule() == resolved` test, `:191`), and the rule combining loop.

**XACML risk: medium.**
- §7.11 and §7.19: the status of each rule. `Rule.evaluateRule` uses `setEvaluationStatusIfAbsent` (`Rule.java:120-147`). With a shared context, a second failing rule would find the first rule's status unless the slot change resets it.
- The status map is keyed by rule, so `getEvaluationStatus(rule)` must still answer per rule.
- The extended-Indeterminate rule context stays as it is.
- This needs a design decision (AP: "designed but not built").

**Lane:** none.

### 3. Decision setup objects (every decision)

**3a. The generation `Optional`.**
- Site: `RootEvaluationContext.java:193` and `:209` wrap `prepared.generation()` on every decision.
- Mechanism: `Generation` (`PreparedPolicySets.java:61`) holds its own `Optional`, created once per generation.
- Saving: −16 B, measured: rsa2008 17.1, `narrow` 16.0, `synthetic` 15.2 to 16.5.

**3b. The handler, 48 → 24 B.**
- Site: `DefaultEvaluationContextHandler.java:82-128`.
- Mechanism:
  - The two cache suppliers are the same on every construction; make them static (−8 B).
  - The five state fields created on first use move into one holder, created on the first resolution that needs one (−16 B).
- Saving: −24 B, computed.

**3c. The root, 104 → 88 B.**
- Site: `RootEvaluationContext.java:87-125`.
- Mechanism:
  - One shared configuration object per decision point holds the clock, XPath version, resolver, the validation and record flags, and the constant `extendedIndeterminateEval`. That field is always false (`:121`) but still takes a slot.
  - With item 1, the four response map fields become two.
- Saving: −16 B, computed.

- **XACML risk:** none. D27 single flight and task 30's session are unchanged.
- **Lane:** none. The PIP handler was Fable's (AL) and Astra's (AJ).

### 4. Engine `Result` without the builder

**Site.** `DefaultPolicyDecisionPoint.java:303-339`, through `Result.ok` (`Result.java:88-91`) and `build` (`:505`).

**Mechanism.**
- C2 removes the 40 B builder on rsa2008 and `responses`, but not on `narrow` (38.5 B/op) or `synthetic` (5.9).
- A direct factory for the engine's decision, status, responses, IIR list and policy identifiers removes the dependence on C2.

**Saving:** `narrow` −38.5 (measured), `synthetic` −6.

**Files:** `Result.java` (an additive public factory: one line of design), `DefaultPolicyDecisionPoint.java`.

**XACML risk:** none. **Lane:** none.

### 5. Bag scans without iterators

**Site.**
- `Match.java:127`: for-each over the bag.
- `BagOfValues.java:271-277`: `containsAll` uses for-each.
- Both reach `BagStorage.java:102` (`BagStorage$1`, 24 B).

**Mechanism.** Index loops over `size()` and `valueAt(i)`, as `TargetIndex.java:110-111` does already.

**Saving:** rsa2008 −27.5, measured.

**Files:** `Match.java`, `BagOfValues.java`. `SetFunctions` is unchanged.

**XACML risk:** none. **Lane:** none. `BagOfValues` is in `org.xacml4j.v30`, not in AT's `types`; please confirm.

### 6. Indeterminate failures

**Site.**
- The status map: `RootEvaluationContext.java:282`, `:308`, 20.0 B/op on rsa2008.
- The failure objects, 26.5 B/op: `BagFunctions.java:82`, `DefaultFunctionInvocation.java:236`, `:409`, `FunctionInvocationException.java:46`, `:71`, `StatusCode.processingError`.

**Mechanism.**
- A small array map for the status map, as in item 1.
- A shared processing-error `StatusCode`.
- The message arguments formatted only when read.
- Stack traces are off already.

**Saving:** rsa2008 −25 to −46; CPU about −0.01 µs (unwinding is 1.0%).

**Files:**
- `RootEvaluationContext.java`, `FunctionInvocationException.java`, `Status`, `StatusCode`: no active lane;
- `BagFunctions.java`, `DefaultFunctionInvocation.java`: Terra (FIX-AS).

**XACML risk: low.** §7.19: the status code and message must stay exactly as they are, for the tests and for the RSA expected responses.

### 7. Non-constant obligations and advice without the multimap builder (`responses`)

**Site.**
- `BaseDecisionRuleResponseExpression.java:131-135`: list builder per evaluation.
- `DecisionRuleResponse.java:112`: a `LinkedHashMultimap` per builder. R-AN P2-2 restored this protected field, so the builder must keep it.
- `DecisionRuleResponse.java:53`: `ImmutableSetMultimap.copyOf`.
- `AttributeAssignment.java:108-114`: `Objects.hashCode` varargs, `Object[4]` per call, three calls per assignment, 190.8 B/op.

**Mechanism.**
1. The engine builds `Obligation` and `Advice` from the evaluated immutable assignment list without the builder (an additive factory). Duplicate assignments are dropped as the set multimap drops them, and the order is kept.
2. The hash folds without the array. It gives the same value as `Arrays.hashCode`, or it can be cached, since the class is immutable.

**Saving:** `responses` about −1.5 KB, which includes the hash's 191. Expression row 1,943.5 less the needed assignments, responses and list. rsa2008 0.

**Files:** `DecisionRuleResponse.java`, `Obligation.java`, `Advice.java`, `ObligationExpression.java`, `AdviceExpression.java`, `BaseDecisionRuleResponseExpression.java`, `AttributeAssignment.java`.

**XACML risk: low.** §7.18: the same assignments, set semantics and order. The API change is additive; it needs one design line because R-AN P2-2 protects the builder.

**Lane:** none. The `AttributeAssignment` hash is AT's mechanism ("value hash codes without temporaries"), but the file is outside `types`. Assign it to Terra only if AT's file list includes it.

### 8. Policy identifier list (`responses`)

**Site.**
- `RootEvaluationContext.java:372-376` and `:277`: a `ConcurrentHashMap` by id, 272.9 B/op.
- `:877-894`: `String[]`, sort and `ArrayList`.
- `Result.java:335-340`: `ImmutableList.Builder`.

**Mechanism.** When D32 records the list, record the references in a small array and sort the references once. Give the `Result` an exact-size `ImmutableList`. Keep today's id order and the de-duplication by id.

**Saving:** `responses` about −360 of 458 B/op.

**Files:** `RootEvaluationContext.java`, `Result.java`.

**XACML risk:** none. The list content and order are unchanged; the order is not normative (section number UNVERIFIED).

**Lane:** none.

### 9. `TargetIndex` candidates for multi-valued bags (`multi-valued`)

**Site.**
- `TargetIndex.java:109`: clones the `any` bit set.
- `:115`: the clone's `long[]` grows when a higher bit is set.
- `:127-134`: `ImmutableList.builder()` starts at 4 and grows, although `selected.cardinality()` is known at `:128`.

**Mechanism.**
- Pre-size the bit set (`new BitSet(children.size())`, then `or(any)`).
- Size the builder with `builderWithExpectedSize(cardinality)`, or return a list view over the children and the bit set.

**Saving:** pre-sizing −173 (computed from the sites), the view −230.

**Files:** `TargetIndex.java`.

**XACML risk:** none. The candidates and their order are the same.

**Lane:** none today; it was Terra's index in batch 2.

## Ranked: what is left by µs/op (indicative)

| # | Item | rsa2008 | Others | Files | Active lane |
| --- | --- | ---: | --- | --- | --- |
| C1 | Extended-Indeterminate walk: no walk when the root records nothing; one walk instead of two | ≈ −0.12 (6.2%) | `multi` −0.09 (2.5%), `narrow` −0.01 (3.7%), `responses` −0.04 (half of its 4.1%) | `BaseCompositeDecisionRule.java:200`, `DescendantEvaluationContext.java:238-242` | none |
| C2 | Function shape per arity, computed once; higher-order arguments validated once | ≈ −0.11 (4.3% + 1.4%) | `multi` −0.16 (1.4% + 3.0%) | `FunctionSpecBuilder.java:464-508`, `HigherOrderFunctions.java:419-492` | **Terra (FIX-AS)** |
| C3 | Response lists (item 1) | ≈ −0.05 | `narrow` −0.02 (`VarHandle` reads, 7.4%), `responses` ≈ −0.3 | as item 1 | none |
| C4 | Validity re-checks in context creation: `PolicySet.createContext` checks `getCurrentPolicySet()` at `:193`, then the constructor checks it again with `getCurrentPolicy()` at `:360-363` | up to −0.1 (of 10%) | | `PolicySet.java`, `Policy.java`, `Rule.java` | none |
| C5 | IIR duplicate check only for lists not built by the request | 0 | `narrow` −0.011 (3.5%) | `Result.java:323-333`, `RequestContext` | none |

**C1 mechanism.**
- The root records the policy identifiers only when D32 asks for them (`RootEvaluationContext.java:373`). Yet `respond` walks the chain and `addEvaluationResult` walks it again before reaching that test.
- Ask the root through `top` first: O(1).
- `respond`'s own test can rely on the context's test for the package's own contexts. Keep it for custom contexts.
- XACML risk: none (§7.10, §7.14). The same contexts are skipped in passes.

**C2 mechanism.**
- `fastShape` re-derives `parameters.size() == 2 && isMandatory(parameters.get(0)) && isVariadic()` from a mutable `ArrayList` on every call.
- Precompute the eligible arities when the spec is built.
- Move `requireValidArguments` to the `Apply` build if it depends only on static types. That is UNVERIFIED: I did not read why it runs per call.
- XACML risk: none if the same errors are raised at the same point. A validation moved to build time could turn a runtime Indeterminate into a load-time error; keep it at runtime if the type can be dynamic.

## Not recommended

- **The single-result `ResponseContext` list** (16 B per result with responses):
  - It could be dropped by holding the `Result` and creating the list on `getResults()`.
  - But every PEP reads the results, so the real saving is 0.
- **Rule and policy-set contexts beyond item 2:** the policy set contexts (5.0 per decision) and the merged reference contexts (4.0) are one per policy set entered. That is structural after AP.
- **The precheck:** its cost is target matching. The index is the lever where targets are indexable.

## Residual risk

1. **Benchmark versus production.**
   - Item 1's views (30 B on rsa2008) are created again by a PEP that reads the obligations, so with lists that saving holds in production too.
   - Item 4 depends on C2: whether the builder survives is decided per call site.
2. **Computed figures.** Items 2, 3b, 3c and 9 (view) are from field layouts. A D12 pair must measure them.
3. **`responses` variance.** Its forks differ by 300 B through scalar replacement. Its per-site figures are from one fork (5,281 B/op).
4. **Times.** `-Djmh.ignoreLock=true` with other lanes active, load average 6.3 to 12.2. µs savings are shares of profiles, not measured.
5. **Ownership to confirm:** `BagOfValues` and `AttributeAssignment` against AT's file list; `TargetIndex` (Terra in batch 2); `BaseDecisionRule` and `BaseCompositeDecisionRule` (Fable in batch 2).
6. **XACML sections.** §7.14 and §7.18 are cited from the XACML 3.0 Plus Errata 01 text as used in FIX-AN2 (read 2026-10-28). §7.10, §7.11, §7.19 and the PolicyIdentifierList section were not re-read in this packet (UNVERIFIED).

## Raw profiles and analysis

Everything is under the worktree's `target/p-fixed2/` (gitignored):

| What | Files |
| --- | --- |
| Commands and summary | `command.txt`, `summary.md`, `runs.txt` |
| JMH results | `gc-*.json/.log` (3-fork baselines), `alloc-*.json/.log`, `cpu-*.json/.log` (profiled forks) |
| Allocation profiles | `alloc-<name>/…/jfr-alloc.jfr` (JFR) and `alloc-<name>.collapsed` (byte-weighted stacks) |
| CPU profiles | `cpu-<name>/…/collapsed-cpu.csv`, linked as `cpu-<name>.csv` |
| Analysis | `<name>-phases.txt` (B/op per phase and site), `<name>-stacks.txt` (B/op per stack), `<name>-cpu-phases.txt`, `rsa-cpu-extended-walks.txt`, `rsa-cpu-reference-checks.txt`, `rsa-cpu-chain-queries.txt`, `rsa-cpu-function-leaves.txt`, `multi-cpu-function-leaves.txt` |
| Scripts | `scripts/` (`profile.sh`, `convert.sh`, `analyze.sh`, and the Python scripts) |
=== END REPORT P-FIXED2-opus ===
