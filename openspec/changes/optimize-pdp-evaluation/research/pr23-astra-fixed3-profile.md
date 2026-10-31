# P-FIXED3: remaining allocation per decision

Codex Astra, lane cod_3. 2026-10-28. Source snapshot: the supplied main-tree reset plus the two R-AV P3 cleanup files below. HEAD is `8713323705611502ad0e1576554fdaa4e3fbf374`; the supplied merged work is uncommitted, so the 3,756-file SHA-256 inventory and benchmark-jar hash identify the measured tree. This is research, not a measured before/after optimization pair.

The remaining steady allocation is mostly policy contexts, response payload construction and PIP work. rsa2008 spends **97.676%** of sampled decide CPU in policy evaluation, including target prechecks; fixed setup, result assembly and PDP dispatch account for **2.324%**. Do not pursue more general request-path machinery to save that small CPU share.

| Workload | Requests | Decisions measured | Objects/decision | Classes | B/decision |
| --- | ---: | ---: | ---: | ---: | ---: |
| rsa2008 all, index off | 17 | 170,000 | 21.764706 | 17 | 758.588235 |
| rules:100 narrow, index on | 1 | 10,000 | 8 | 8 | 280 |
| rules:100 responses, index on | 1 | 10,000 | 72 | 34 | 2336 |
| rules:100 multi-valued, index on | 1 | 10,000 | 25 | 10 | 848 |
| synthetic:100 all, index on | 7 | 70,000 | 5.714286 | 6 | 245.714286 |
| pip:8 independent, index off, serial/cache off/latency 0 | 1 | 10,000 | 178 | 75 | 5864 |

The numbers are exact class-histogram differences for these warmed executions, not allocation samples. Displayed averages for `all` are rounded; the raw integer counts and per-request results are retained. The rsa2008 CPU/JMH fork independently measured **758.609 ± 0.118 B/op**, agreeing with the histogram's **758.588235 B/decision**. Its profiled time was **1.949 ± 0.248 µs/op**; this is indicative, not a speed comparison.

## R-AV P3 cleanup and gate

- `xacml-pdp/src/main/java/org/xacml4j/v30/pdp/DefaultEvaluationContextHandler.java:64`: document the lazy holder; restore the detailed request memo and session comments on its fields; qualify flights so nested callers run their own attempt (`:190`). Remove the two static map suppliers and construct `ConcurrentHashMap` directly in `designatorCache()` and `selectorCache()` (`:128`, `:135`). The `Supplier` import remains needed by resolution attempts.
- `xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java:197`: state that the own-context skip relies on `getCurrentPolicySet()` answering from the final outer reference. No further context-check optimization was made.
- No public API change. No new tests. No production edits in the profiling part.
- Executed `JAVA_HOME="$(rtk proxy jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-pdp -am test`, with IntelliJ Maven 3.9.16 first on PATH: **core 7,224 (2 skipped), PDP 731 (3 skipped), zero failures/errors**. The full reactor was not requested or rerun for this cleanup. Evidence: `evidence/tests/fix-av-p3-astra/`.
- Executed `rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am -DskipTests package`: exit 0. Evidence: `evidence/builds/p-fixed3-astra/`. The benchmark jar SHA-256 is `e291408228b236dc3f20e6f2acbdcb7b3b41f6476858ced8ad519058e5e65575`.
- No matching task issue was identified in the supplied beads inventory. No issue was created/closed, and no task/evidence index was marked complete; conductor tracking remains separate.

## Method and limits

1. The research-only `AllocationProbe.java` uses the real checked `PdpFixture`, real repositories/PDP and unchanged requests. It warms 200,000 decisions round-robin over each selected workload, then 50,000 on each individual request. It measures **10,000 more decisions of that one request**. rsa2008 `all` has 17 requests; synthetic `all` has seven. The aggregate weights each request equally, matching the benchmark cursor. All 28 windows (280,000 measured decisions) and fixture rechecks passed.
2. JDK **25.0.4+7-LTS-jvmci-b01**, Oracle GraalVM with **C2** (`-XX:-UseJVMCICompiler`); `-Xbatch -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -XX:+StartAttachListener -Xms2g -Xmx2g`. Flags confirm ordinary headers, 8-byte alignment and compressed references/class pointers. No preview features. Epsilon keeps unreachable allocations available for `jcmd GC.class_histogram -all`; the installed command's help explicitly documents inclusion of unreachable objects.
3. External `jcmd` takes a priming histogram, an empty control window, and before/after histograms for each measured window. No histogram text parsing or result inspection occurs inside the measured JVM window. A volatile response sink prevents dead-result elimination. Result checking and fixture rechecking occur outside it.
4. Excluded from application totals: `[Ljdk.internal.vm.FillerElement;` (VM heap filler, whose bytes can fall as TLAB space is consumed), and **one 56-byte `java.nio.HeapCharBuffer`** made by the probe's readiness print in every empty and measured window. The first control in each JVM additionally initializes two strings/two byte arrays and one `ConcurrentHashMap$ValueIterator`; those do not recur in measurement and are not subtracted from it. After the single 56-B subtraction, **every per-request non-filler histogram total equals the deciding thread's allocated-byte counter exactly**. Raw/control/normalized values all remain in `delta.json`; no class was removed by a sampling threshold.
5. Class totals are exact for this call shape. Project-class rows cite their construction sites. Histograms do not record allocation stacks: shared array and collection rows cite candidate application construction calls confirmed in source, and their individual byte split is **UNVERIFIED**. No JFR or async allocation samples were used. `javap` of the jar's Guava **31.1-jre** verifies the singleton inverse-map path discussed in R3.
6. CPU: one JMH fork, `PdpDecideBenchmark.decide`, `store=in-memory`, rsa2008 `all`, `targetIndex=false`; five 1-s warmups, five 2-s measurements, `-prof gc` and async-profiler `event=cpu;interval=100000;output=collapsed`, library `/opt/homebrew/lib/libasyncProfiler.dylib`. Host `-Djmh.ignoreLock=true`; one thread, C2, G1 and a 2-GB heap. No build overlapped this lane's profiling. Other lanes may have used the machine, so time and sample shares are indicative.
7. `responses` is 2,336 B here versus the supplied AX smoke's 2,368 B; this is a different warmed call shape/JIT run, **not evidence of a 32-B cleanup saving**. Likewise, the supplied 766-B rsa2008 number is historical; this jar's two current measurements agree at 758.6 B. No source-change attribution is inferred from that difference.

Commands, flags, raw histograms, the probe/runner/analyzers, CPU collapsed stacks, JMH JSON and source hashes are under `openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/p-fixed3-astra/`. `profile.py histograms` and `profile.py cpu` reproduce the runs with `JAVA_HOME` set to JDK 25. Maven commands include `-Dlicense.skipUpdateLicense=true`.

## What the totals mean

- Every measured decision still creates a fresh **80-B root**, a **24-B handler** and a **48-B Result**. Sharing configuration (AX), lazily grouping handler state (AV), the generation Optional (AU), and removing Result builders (AU) are already present. The root/handler own mutable request state; pooling them would need an escape/lifetime contract and is not proposed.
- rsa2008 descendant contexts total **474.353 B/decision (62.53%)**: set 158.118, set-reference 126.118, rule 118.588 and policy 71.529. Staging frames are another 63.529 B. Root setup is now a small part of the cost.
- `narrow` is exactly **eight objects**: root 80, handler 24, three contexts at 32 each, Result 48, ResponseContext 16 and its singleton list 16. Its included request content prevents the plain-result shortcut; it is not a 152-B decision yet.
- `multi-valued` creates eight policy and eight rule contexts plus two set contexts: **576 B**. The existing equality index contributes **120 B** (BitSet 24, long array 32, candidate Object array 48, immutable list 16). D46 already pre-sized these objects; this is not a new candidate-sizing proposal.
- `responses` has only **one dynamic Obligation** and three dynamic AttributeAssignments; its Advice instances and other constant responses are reused. The old mutable `DecisionRuleResponse.Builder` is gone. Guava's **ImmutableSetMultimap.Builder** still constructs three linked maps, their tables/entries and two sets for the one dynamic assignment multimap. Removing that representation is a separate compatibility/design problem, not a repeat of AX.
- `pip:8 independent` demands **two serial resolver calls**, `case` and `bulk`; the latter supplies eight backend values. It is not eight D26 attribute-function tasks. The two CompletableFutures are **D27 flights**, and the ScopedValue objects are ancestry scopes. No executor/runner/virtual-thread object appears in this measured class delta. Nine StringVals and eight new Strings/backing byte arrays are 600 B of actual resolver data, before its bags/maps. The result must also retain the resolved attributes.

## Ranked follow-ups

All savings below are **expected, unmeasured changes**, derived from the exact remaining objects and source. They are not additive where they remove the same assembly work. The rank favors the largest useful byte opportunity, with cross-workload coverage shown rather than an invented production traffic weighting. For low-cost implementation order, start with R3, R5 and R6, then measure; R1/R2 need more design.

| Rank | Item and proposed form | Expected B/decision saved | Cost / risk | Files / decision |
| --- | --- | --- | --- | --- |
| R1 | A one-category fast path for **resolved-attribute response assembly**. Avoid the outer HashMultimap, wrapped/key views and intermediate LinkedList; retain a JDK/Guava equality-dedup set where needed, and construct the final category list directly. | PIP **200–500** is a planning range, not a measured saving. The exact table below identifies the larger temporary container pool; final Attribute/Entity/Category/index data remains. Zero in the five request-backed workloads. | Medium / medium: duplicate attributes, issuers, grouped duplicate values, category order, floating-point values and concurrent root writes must keep today's response. Do not replace hash order with insertion order accidentally. | `DefaultPolicyDecisionPoint.java:360–377`; possibly a narrow internal construction path in Category/Entity/AttributeIndex. New design decision if public factories/representation are needed. AJ's `.values(v)` is already present. |
| R2 | Evaluate eligible engine rules without a dedicated child context, while preserving the current rule/status/staging scope explicitly. Keep the existing path for observed/custom/foreign contexts. | Upper bounds: rsa **118.588**, narrow **32**, responses **64**, multi **256**, synthetic **27.429**, PIP **32**. These require eliminating the child context; merely sharing one context per policy cannot deliver these bounds. | High / high: context can escape to trusted extensions, current-rule identity, errors, defaults, reentrancy, single-flight callbacks and response staging. A reusable mutable context without an escape contract is unsound. | **after D45**: Rule, Policy, combining dispatch, possibly Condition/PreparedPolicySets. Requires a new design decision. AP reference/target fusion and AV's validity skip are already applied. |
| R3 | For a descriptor declaring one context key whose bag has already resolved, read that bag by its key from the existing map and pass it directly to a package-private `ResolverCacheKey.of(String, BagOfValues)` overload. Keep the existing multi-key route and the same detached `BagOfValues[]` serialized field. | PIP **128**: two avoidable inverse SingletonImmutableBiMaps (2×48) and two singleton key-set views (2×16). Zero in the request-backed workloads. | Low–medium / medium: do not resolve keys early, retry missing/failure differently, change resolver/key order or D27/task-30 memo identity. Public `getResolvedKeys()` still returns its immutable map. | `ResolverContext.java:156`, `ResolverCacheKey.java:75–76`. No public API or serialized-form change needed. This removes Guava views left after AL, not AL's already removed builder. |
| R4 | Prepare a flat immutable sequence for assignment-expression traversal and reuse exact built-in literal assignments **inside mixed dynamic expressions**. Keep protected `attributeExpressions` and all external expression overrides. | `responses`: **32** for its one literal `level` assignment; up to **80** additional iterator bytes are a source-attribution/implementation ceiling, **UNVERIFIED** until measured. No saving claimed for already-constant responses or their immutable payload. | Medium / medium: preserve evaluation order, subclass dispatch, failures and grouped assignment-set order. Never pre-evaluate an arbitrary custom Expression. | `BaseDecisionRuleResponseExpression.java:76,131–135,170–179`. New narrow design for mixed-literal preparation; no new public API required. AN only cached wholly constant responses. |
| R5 | Reuse the existing plain successful Result before allocation, instead of allocating it and then discarding it when ResponseContext selects its already-shared response. Validate inputs and retain custom status/data cases. | rsa **25.412** (9/17 requests ×48), multi **48**, synthetic **48**; narrow/responses/PIP **0**. | Low / low: share only OK Permit/Deny/NotApplicable with no obligations/advice, included/resolved attributes or policy ids. Errors keep their own status/cause. Existing shared responses already expose shared results. | `Result.java:108`; `ResponseContext.java:60–64`. Private canonical results/factory branch; no new public API required. AU removed the builder, not this final discarded Result. |
| R6 | In Attribute.Builder's existing Iterable method, use `size()/valueAt(i)` for BagOfValues and retain the ordinary Iterable path otherwise. | PIP **48** (two BagStorage iterators ×24); zero in the other workloads. | Low / low: retain all values, multiplicity, grouped order and floating-point behavior. No prefetch, cache or outcome change. | `Attribute.java:191–195`. No public API change. AV changed Match/BagOfValues scans, not this remaining response-copy loop. |

Further leads, not booked as savings:

- `Attribute.hashCode` (`Attribute.java:135–137`) still constructs a four-element varargs array. An explicit 31-fold preserves the hash and avoids source-level temporaries, but the histogram combines this array with 13 other Object arrays in the PIP case; **whether C2 materializes each hash array is UNVERIFIED**, so no 64-B saving is claimed. Do not cache it without accounting for extensible Values, and do not change its existing equality/hash semantics as part of that fold.
- The remaining dynamic assignment multimap carries a substantial object graph, but its protected Multimap representation and Guava's public construction API constrain a clean replacement. Do not introduce a hand-written multimap or redo AX for the same result. A representation decision with external-builder/expression compatibility and serialization proof is needed before claiming a large additional response saving.
- `PolicySet.java:372` still asks the parent for its current policy; the existing own-context test skips only the current-policy-set question. CPU samples below still show this chain walk. For an engine-owned chain ending at RootEvaluationContext, an invariant may eliminate the query without adding a pointer to every context. Prove it first, retain exact foreign/mock preconditions, and mark any Policy/Condition/PreparedPolicySets work **after D45**. This is a **0-B CPU proposal**, not an allocation saving.
- Do not pool roots/handlers, remove D27 flights, cache absence/failure, discard PIP timestamps, cache resolver candidates despite public registration, or memoize whole responses across different requests. Those would buy bytes by changing contracts. No repeat proposals are made for AM–AX, D46 or regex D6; their remaining necessary objects are identified above.

## Every allocated application class

Source paths are relative to the project root: policy/value/response classes are in `xacml-core/src/main/java/org/xacml4j/v30/`; PDP/PIP classes in `xacml-pdp/src/main/java/org/xacml4j/v30/`; corpus classes in `xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/`. A Guava/JDK implementation row cites the application call that constructs it. For shared implementation classes, sites are a set, not an unmeasured per-site byte attribution. Counts below sum to every measured application allocation; exact integer deltas are in `allocations.json` and each request's `delta.json`.

### rsa2008 all, index off

| Class | Count/decision | Bytes/decision | Allocation site(s) | Can it go? |
| --- | ---: | ---: | --- | --- |
| `org.xacml4j.v30.policy.PolicySet$PolicySetDescendantEvaluationContext` | 4.941176 | 158.117647 | `PolicySet.java:198` | Keep for set identity, defaults and staging; broader context redesign is after D45. |
| `org.xacml4j.v30.policy.PolicySetIDReference$ReferencedPolicySetContext` | 3.941176 | 126.117647 | `PolicySetIDReference.java:143` | Keep reference identity and cycle checks; AP already fused reference and target. |
| `org.xacml4j.v30.policy.Rule$RuleEvaluationContext` | 3.705882 | 118.588235 | `Rule.java:74` | Candidate R2: own-engine rule context elision, after D45; preserve current-rule/status/staging semantics. |
| `org.xacml4j.v30.policy.RootEvaluationContext` | 1 | 80 | `PolicyDecisionPointContextFactory.java:244,247` | Keep: mutable state of this decision; AX already shares configuration. |
| `org.xacml4j.v30.policy.Policy$PolicyDescendantEvaluationContext` | 2.235294 | 71.529412 | `Policy.java:200` | Keep policy identity, defaults and variable scope; context redesign after D45. |
| `org.xacml4j.v30.policy.RootEvaluationContext$Staged` | 1.588235 | 63.529412 | `RootEvaluationContext.java:770` | Keep pending responses until the enclosing decision is known; AU already creates lazily. |
| `org.xacml4j.v30.Result` | 1 | 48 | `Result.java:108,125` | R5 can share only empty successful results; keep results carrying data or errors. |
| `org.xacml4j.v30.pdp.DefaultEvaluationContextHandler` | 1 | 24 | `PolicyDecisionPointContextFactory.java:239` | Keep: request-specific handler; AV already moved its lazy members. |
| `org.xacml4j.v30.policy.RootEvaluationContext$Responses` | 0.470588 | 15.058824 | `RootEvaluationContext.java:293` | Keep the first-write holder and live views; AU already shares immutable lists. |
| `java.util.concurrent.ConcurrentHashMap$Node` | 0.352941 | 11.294118 | `RootEvaluationContext.java:298; per-rule setEvaluationStatus` | Keep equality-keyed per-rule status and concurrent writes; AR already fixed deep hashes. |
| `org.xacml4j.v30.policy.FunctionInvocationException` | 0.176471 | 8.470588 | `DefaultFunctionInvocation.java:224,242,281` | Keep error class/message/cause; AW already removed avoidable formatting objects. |
| `com.google.common.collect.SingletonImmutableList` | 0.470588 | 7.529412 | `ResponseContext.java:55,64; BaseDecisionRule.java response lists; RootEvaluationContext.java:849` | Keep published immutable lists; only remove transient assembly/views, not AU's end-to-end list behavior. |
| `org.xacml4j.v30.ResponseContext` | 0.470588 | 7.529412 | `ResponseContext.java:55,64` | Keep this non-plain response. AQ already shares plain responses. |
| `org.xacml4j.v30.Status` | 0.176471 | 5.647059 | `BaseDecisionRule.java:291; Status.java:262` | Keep per-failure error/cause; shared processing-error StatusCode is already merged. |
| `[Ljava.util.concurrent.ConcurrentHashMap$Node;` | 0.058824 | 4.705882 | `RootEvaluationContext.java:298; per-rule setEvaluationStatus` | Keep equality-keyed per-rule status and concurrent writes; AR already fixed deep hashes. |
| `org.xacml4j.v30.policy.function.impl.BagFunctions$BagSizeException` | 0.117647 | 4.705882 | `BagFunctions.java:82,85` | Keep public error outcome/cause; cycle N already made this cheap. |
| `java.util.concurrent.ConcurrentHashMap` | 0.058824 | 3.764706 | `RootEvaluationContext.java:298; per-rule setEvaluationStatus` | Keep equality-keyed per-rule status and concurrent writes; AR already fixed deep hashes. |
| **Total** | **21.764706** | **758.588235** | | |

### rules:100 narrow, index on

| Class | Count/decision | Bytes/decision | Allocation site(s) | Can it go? |
| --- | ---: | ---: | --- | --- |
| `org.xacml4j.v30.policy.RootEvaluationContext` | 1 | 80 | `PolicyDecisionPointContextFactory.java:244,247` | Keep: mutable state of this decision; AX already shares configuration. |
| `org.xacml4j.v30.Result` | 1 | 48 | `Result.java:108,125` | R5 can share only empty successful results; keep results carrying data or errors. |
| `org.xacml4j.v30.policy.PolicySet$PolicySetDescendantEvaluationContext` | 1 | 32 | `PolicySet.java:198` | Keep for set identity, defaults and staging; broader context redesign is after D45. |
| `org.xacml4j.v30.policy.Policy$PolicyDescendantEvaluationContext` | 1 | 32 | `Policy.java:200` | Keep policy identity, defaults and variable scope; context redesign after D45. |
| `org.xacml4j.v30.policy.Rule$RuleEvaluationContext` | 1 | 32 | `Rule.java:74` | Candidate R2: own-engine rule context elision, after D45; preserve current-rule/status/staging semantics. |
| `org.xacml4j.v30.pdp.DefaultEvaluationContextHandler` | 1 | 24 | `PolicyDecisionPointContextFactory.java:239` | Keep: request-specific handler; AV already moved its lazy members. |
| `com.google.common.collect.SingletonImmutableList` | 1 | 16 | `ResponseContext.java:55,64; BaseDecisionRule.java response lists; RootEvaluationContext.java:849` | Keep published immutable lists; only remove transient assembly/views, not AU's end-to-end list behavior. |
| `org.xacml4j.v30.ResponseContext` | 1 | 16 | `ResponseContext.java:55,64` | Keep this non-plain response. AQ already shares plain responses. |
| **Total** | **8** | **280** | | |

### rules:100 responses, index on

| Class | Count/decision | Bytes/decision | Allocation site(s) | Can it go? |
| --- | ---: | ---: | --- | --- |
| `[Ljava.lang.Object;` | 11 | 304 | `BaseDecisionRuleResponseExpression.java:131; DecisionRuleResponse.java:79,84; RootEvaluationContext.java:853,979` | Combined array pool: class totals are exact; per-site byte split is UNVERIFIED. Retained arrays and removable temporaries must not be counted twice. |
| `[Ljava.util.HashMap$Node;` | 3 | 240 | `DecisionRuleResponse.java:79,82,84 (Guava ImmutableSetMultimap builder)` | Remaining Guava builder internals, not DecisionRuleResponse.Builder. Removal needs a separate representation design; do not repeat AX or add a custom multimap. |
| `java.util.LinkedHashMap$Entry` | 5 | 200 | `DecisionRuleResponse.java:79,82,84 (Guava ImmutableSetMultimap builder)` | Remaining Guava builder internals, not DecisionRuleResponse.Builder. Removal needs a separate representation design; do not repeat AX or add a custom multimap. |
| `java.util.LinkedHashMap` | 3 | 192 | `DecisionRuleResponse.java:79,82,84 (Guava ImmutableSetMultimap builder)` | Remaining Guava builder internals, not DecisionRuleResponse.Builder. Removal needs a separate representation design; do not repeat AX or add a custom multimap. |
| `org.xacml4j.v30.policy.RootEvaluationContext$Staged` | 3 | 120 | `RootEvaluationContext.java:770` | Keep pending responses until the enclosing decision is known; AU already creates lazily. |
| `com.google.common.collect.RegularImmutableList` | 6 | 96 | `BaseDecisionRuleResponseExpression.java:135; RootEvaluationContext.java:853,983` | Keep final immutable lists; reduce intermediary construction only where equivalence is proved. |
| `org.xacml4j.v30.policy.RootEvaluationContext$Evaluated` | 4 | 96 | `RootEvaluationContext.java:396` | Keep applicable-policy recording/order; AU already replaced the map. |
| `org.xacml4j.v30.AttributeAssignment` | 3 | 96 | `BaseDecisionRuleResponseExpression.java:170,174,179` | R4 can reuse exact literal assignments in a mixed expression; dynamic values remain per decision. |
| `org.xacml4j.v30.policy.RootEvaluationContext` | 1 | 80 | `PolicyDecisionPointContextFactory.java:244,247` | Keep: mutable state of this decision; AX already shares configuration. |
| `org.xacml4j.v30.policy.PolicySet$PolicySetDescendantEvaluationContext` | 2 | 64 | `PolicySet.java:198` | Keep for set identity, defaults and staging; broader context redesign is after D45. |
| `org.xacml4j.v30.policy.Policy$PolicyDescendantEvaluationContext` | 2 | 64 | `Policy.java:200` | Keep policy identity, defaults and variable scope; context redesign after D45. |
| `org.xacml4j.v30.policy.Rule$RuleEvaluationContext` | 2 | 64 | `Rule.java:74` | Candidate R2: own-engine rule context elision, after D45; preserve current-rule/status/staging semantics. |
| `com.google.common.collect.ImmutableSetMultimap` | 1 | 56 | `DecisionRuleResponse.java:79,82,84` | Keep set/order contract and protected multimap field. Large removal requires a new representation decision, not another AX factory. |
| `com.google.common.collect.Iterators$9` | 2 | 48 | `BaseDecisionRuleResponseExpression.java:132; DecisionRuleResponse.java:65,84 (Guava iteration/hash)` | R4 can flatten immutable expression traversal at build time; how much of this 80-B pool is removed is UNVERIFIED until measured. |
| `com.google.common.collect.SingletonImmutableList` | 3 | 48 | `ResponseContext.java:55,64; BaseDecisionRule.java response lists; RootEvaluationContext.java:849` | Keep published immutable lists; only remove transient assembly/views, not AU's end-to-end list behavior. |
| `com.google.common.collect.ImmutableMapEntry` | 2 | 48 | `DecisionRuleResponse.java:84 (Guava immutable multimap build/hash)` | Mostly final immutable map representation; builders/entry views are candidates only with a representation/assembly change. Exact per-site split is UNVERIFIED. |
| `org.xacml4j.v30.Result` | 1 | 48 | `Result.java:108,125` | R5 can share only empty successful results; keep results carrying data or errors. |
| `com.google.common.collect.ImmutableSet$RegularSetBuilderImpl` | 1 | 40 | `DecisionRuleResponse.java:79,82,84` | Keep set/order contract and protected multimap field. Large removal requires a new representation decision, not another AX factory. |
| `com.google.common.collect.RegularImmutableMap` | 1 | 40 | `DecisionRuleResponse.java:84 (Guava immutable multimap build/hash)` | Mostly final immutable map representation; builders/entry views are candidates only with a representation/assembly change. Exact per-site split is UNVERIFIED. |
| `[Lorg.xacml4j.v30.CompositeDecisionRule;` | 1 | 32 | `RootEvaluationContext.java:975,999` | Keep the policy-id snapshot/sort; AU already reduced it. |
| `com.google.common.collect.Iterators$ArrayItr` | 1 | 32 | `BaseDecisionRuleResponseExpression.java:132; DecisionRuleResponse.java:65,84 (Guava iteration/hash)` | R4 can flatten immutable expression traversal at build time; how much of this 80-B pool is removed is UNVERIFIED until measured. |
| `org.xacml4j.v30.Obligation` | 1 | 32 | `ObligationExpression.java:51; Obligation.java:56` | Keep the response whose assignment depends on the request; AX's direct factory is present. |
| `org.xacml4j.v30.policy.RootEvaluationContext$Responses` | 1 | 32 | `RootEvaluationContext.java:293` | Keep the first-write holder and live views; AU already shares immutable lists. |
| `com.google.common.collect.RegularImmutableSet` | 1 | 32 | `DecisionRuleResponse.java:79,82,84` | Keep set/order contract and protected multimap field. Large removal requires a new representation decision, not another AX factory. |
| `java.util.LinkedHashSet` | 2 | 32 | `DecisionRuleResponse.java:79,82,84 (Guava ImmutableSetMultimap builder)` | Remaining Guava builder internals, not DecisionRuleResponse.Builder. Removal needs a separate representation design; do not repeat AX or add a custom multimap. |
| `com.google.common.collect.ImmutableList$Builder` | 1 | 24 | `BaseDecisionRuleResponseExpression.java:131; RootEvaluationContext.java:979` | Keep evaluated assignment/policy-id assembly; R4 may reduce assignment-side work. |
| `com.google.common.collect.ImmutableMapEntrySet$RegularEntrySet` | 1 | 24 | `DecisionRuleResponse.java:84 (Guava immutable multimap build/hash)` | Mostly final immutable map representation; builders/entry views are candidates only with a representation/assembly change. Exact per-site split is UNVERIFIED. |
| `[Ljava.util.Map$Entry;` | 1 | 24 | `DecisionRuleResponse.java:84 (Guava immutable multimap build/hash)` | Mostly final immutable map representation; builders/entry views are candidates only with a representation/assembly change. Exact per-site split is UNVERIFIED. |
| `org.xacml4j.v30.pdp.DefaultEvaluationContextHandler` | 1 | 24 | `PolicyDecisionPointContextFactory.java:239` | Keep: request-specific handler; AV already moved its lazy members. |
| `com.google.common.collect.ImmutableSetMultimap$Builder` | 1 | 24 | `DecisionRuleResponse.java:79,82,84` | Keep set/order contract and protected multimap field. Large removal requires a new representation decision, not another AX factory. |
| `java.util.LinkedHashMap$LinkedEntrySet` | 1 | 24 | `DecisionRuleResponse.java:79,82,84 (Guava ImmutableSetMultimap builder)` | Remaining Guava builder internals, not DecisionRuleResponse.Builder. Removal needs a separate representation design; do not repeat AX or add a custom multimap. |
| `[Lcom.google.common.collect.ImmutableMapEntry;` | 1 | 24 | `DecisionRuleResponse.java:84 (Guava immutable multimap build/hash)` | Mostly final immutable map representation; builders/entry views are candidates only with a representation/assembly change. Exact per-site split is UNVERIFIED. |
| `org.xacml4j.v30.ResponseContext` | 1 | 16 | `ResponseContext.java:55,64` | Keep this non-plain response. AQ already shares plain responses. |
| `com.google.common.collect.SingletonImmutableSet` | 1 | 16 | `DecisionRuleResponse.java:84` | Keep singleton set in final assignment multimap. |
| **Total** | **72** | **2336** | | |

### rules:100 multi-valued, index on

| Class | Count/decision | Bytes/decision | Allocation site(s) | Can it go? |
| --- | ---: | ---: | --- | --- |
| `org.xacml4j.v30.policy.Policy$PolicyDescendantEvaluationContext` | 8 | 256 | `Policy.java:200` | Keep policy identity, defaults and variable scope; context redesign after D45. |
| `org.xacml4j.v30.policy.Rule$RuleEvaluationContext` | 8 | 256 | `Rule.java:74` | Candidate R2: own-engine rule context elision, after D45; preserve current-rule/status/staging semantics. |
| `org.xacml4j.v30.policy.RootEvaluationContext` | 1 | 80 | `PolicyDecisionPointContextFactory.java:244,247` | Keep: mutable state of this decision; AX already shares configuration. |
| `org.xacml4j.v30.policy.PolicySet$PolicySetDescendantEvaluationContext` | 2 | 64 | `PolicySet.java:198` | Keep for set identity, defaults and staging; broader context redesign is after D45. |
| `[Ljava.lang.Object;` | 1 | 48 | `TargetIndex.java:150,154` | Combined array pool: class totals are exact; per-site byte split is UNVERIFIED. Retained arrays and removable temporaries must not be counted twice. |
| `org.xacml4j.v30.Result` | 1 | 48 | `Result.java:108,125` | R5 can share only empty successful results; keep results carrying data or errors. |
| `[J` | 1 | 32 | `TargetIndex.java:123` | Keep selected-position bits; no repeat proposal for D46. |
| `java.util.BitSet` | 1 | 24 | `TargetIndex.java:123` | Keep multi-value candidate union; D46 already pre-sizes it. |
| `org.xacml4j.v30.pdp.DefaultEvaluationContextHandler` | 1 | 24 | `PolicyDecisionPointContextFactory.java:239` | Keep: request-specific handler; AV already moved its lazy members. |
| `com.google.common.collect.RegularImmutableList` | 1 | 16 | `TargetIndex.java:154` | Keep final immutable lists; reduce intermediary construction only where equivalence is proved. |
| **Total** | **25** | **848** | | |

### synthetic:100 all, index on

| Class | Count/decision | Bytes/decision | Allocation site(s) | Can it go? |
| --- | ---: | ---: | --- | --- |
| `org.xacml4j.v30.policy.RootEvaluationContext` | 1 | 80 | `PolicyDecisionPointContextFactory.java:244,247` | Keep: mutable state of this decision; AX already shares configuration. |
| `org.xacml4j.v30.Result` | 1 | 48 | `Result.java:108,125` | R5 can share only empty successful results; keep results carrying data or errors. |
| `org.xacml4j.v30.policy.PolicyIDReference$ReferencedPolicyContext` | 0.857143 | 34.285714 | `PolicyIDReference.java:161` | Keep merged reference/target context; AP is already applied. |
| `org.xacml4j.v30.policy.PolicySet$PolicySetDescendantEvaluationContext` | 1 | 32 | `PolicySet.java:198` | Keep for set identity, defaults and staging; broader context redesign is after D45. |
| `org.xacml4j.v30.policy.Rule$RuleEvaluationContext` | 0.857143 | 27.428571 | `Rule.java:74` | Candidate R2: own-engine rule context elision, after D45; preserve current-rule/status/staging semantics. |
| `org.xacml4j.v30.pdp.DefaultEvaluationContextHandler` | 1 | 24 | `PolicyDecisionPointContextFactory.java:239` | Keep: request-specific handler; AV already moved its lazy members. |
| **Total** | **5.714286** | **245.714286** | | |

### pip:8 independent, index off, serial/cache off/latency 0

| Class | Count/decision | Bytes/decision | Allocation site(s) | Can it go? |
| --- | ---: | ---: | --- | --- |
| `[Ljava.lang.Object;` | 14 | 608 | `Attribute.java:136,165,194; DefaultResolverRegistry.java:87; PipWorkload.java:219; AttributeSet.java:195; AttributeIndex.java:476; Result.java:401` | Combined array pool: class totals are exact; per-site byte split is UNVERIFIED. Retained arrays and removable temporaries must not be counted twice. |
| `com.google.common.collect.SingletonImmutableBiMap` | 7 | 336 | `ResolverContext.java:207,156; AttributeSet.java:55; Result.java:401` | R3 removes two inverse maps produced by resolvedKeys.values(); retain dependency/value/result maps. |
| `[Ljava.util.HashMap$Node;` | 3 | 256 | `DefaultPolicyDecisionPoint.java:360; AttributeIndex.java:476,509 (Guava)` | R1 may remove intermediate grouping; keep final category/id order and equality deduplication. |
| `[Ljava.util.concurrent.ConcurrentHashMap$Node;` | 3 | 240 | `DefaultEvaluationContextHandler.java:128,142,424,476; DefaultPolicyInformationPoint.java:240` | Keep D27 flights, first published bags and task-30 sets; do not replace concurrent maps with unsynchronized maps. |
| `org.xacml4j.v30.types.StringVal` | 9 | 216 | `PipWorkload.java:141,200` | Keep nine values returned by the two demanded resolvers. |
| `[B` | 8 | 192 | `PipWorkload.java:246` | Backing bytes of those eight backend strings; keep. |
| `java.util.concurrent.ConcurrentHashMap$Node` | 6 | 192 | `DefaultEvaluationContextHandler.java:128,142,424,476; DefaultPolicyInformationPoint.java:240` | Keep D27 flights, first published bags and task-30 sets; do not replace concurrent maps with unsynchronized maps. |
| `java.util.concurrent.ConcurrentHashMap` | 3 | 192 | `DefaultEvaluationContextHandler.java:128,142,424,476; DefaultPolicyInformationPoint.java:240` | Keep D27 flights, first published bags and task-30 sets; do not replace concurrent maps with unsynchronized maps. |
| `java.lang.String` | 8 | 192 | `PipWorkload.java:246` | Keep eight simulated backend values; this is corpus/backend work, not PDP scaffolding. |
| `[Lorg.xacml4j.v30.types.Value;` | 4 | 176 | `BagStorages.java:88,91,112` | 128 B is builder growth (4/6/9 slots); 48 B is final distinct values. A known-size hint could save 80 B here, but requires provider adoption; not ranked as an engine saving. |
| `com.google.common.collect.AbstractMapBasedMultimap$WrappedSet` | 4 | 160 | `DefaultResolverRegistry.java:85; DefaultPolicyDecisionPoint.java:363,374` | R1 may remove response-side wrappers. Keep registry's detached candidate result and registration order. |
| `java.util.Optional` | 9 | 144 | `DefaultPolicyInformationPoint.java:79,212; ResolverContext.java:189; AttributeResolverDescriptor.java:98,100; DefaultEvaluationContextHandler.java:478` | Keep public Optional outcomes and success/missing distinction; no nullable SPI proposed. |
| `com.google.common.collect.ImmutableMap$Builder` | 4 | 128 | `AttributeSet.java:195; AttributeIndex.java:476 (Guava); Result.java:401` | R1 may reduce response staging; keep AttributeSet validation, timestamps, duplicate-key failures. |
| `com.google.common.collect.ImmutableMapEntry` | 5 | 120 | `AttributeSet.java:55,228; AttributeIndex.java:82,476; Result.java:401` | Mostly final immutable map representation; builders/entry views are candidates only with a representation/assembly change. Exact per-site split is UNVERIFIED. |
| `[Ljava.util.Map$Entry;` | 4 | 112 | `AttributeSet.java:55,228; AttributeIndex.java:82,476; Result.java:401` | Mostly final immutable map representation; builders/entry views are candidates only with a representation/assembly change. Exact per-site split is UNVERIFIED. |
| `[I` | 2 | 104 | `BagStorages.java:113,203` | Counts and offsets preserve multiset/index semantics; representation work is outside this profile's recommended fixes. |
| `java.util.HashMap$Node` | 3 | 96 | `DefaultPolicyDecisionPoint.java:360,368` | R1 removes grouping nodes, while preserving exact deduplication. |
| `java.util.ArrayList` | 4 | 96 | `DefaultResolverRegistry.java:83; PipWorkload.java:219; AttributeIndex.java:509 (Guava)` | Some are necessary detached candidates/backend output; R1 targets only response construction. |
| `java.util.HashMap` | 2 | 96 | `DefaultPolicyDecisionPoint.java:360,363 (HashMultimap/HashSet internals)` | R1 can remove outer grouping for one category; one dedup set may remain. |
| `java.util.LinkedHashMap$Entry` | 2 | 80 | `DefaultPolicyDecisionPoint.java:360; AttributeIndex.java:476,509 (Guava)` | R1 may remove intermediate grouping; keep final category/id order and equality deduplication. |
| `org.xacml4j.v30.policy.RootEvaluationContext` | 1 | 80 | `PolicyDecisionPointContextFactory.java:244,247` | Keep: mutable state of this decision; AX already shares configuration. |
| `org.xacml4j.v30.BagOfValues` | 2 | 64 | `PipWorkload.java:142,200; BagOfValues.java:434` | Keep newly resolved values; do not cache backend answers merely to lower allocation. |
| `org.xacml4j.v30.spi.pip.AttributeSet` | 2 | 64 | `AttributeResolverDescriptor.java:100; AttributeSet.java:254` | Keep per-call values, complete/failure state and timestamps. |
| `com.google.common.collect.SingletonImmutableList` | 4 | 64 | `ResponseContext.java:55,64; AttributeIndex.java:82; Attribute.java:59` | Keep published immutable lists; only remove transient assembly/views, not AU's end-to-end list behavior. |
| `org.xacml4j.v30.spi.pip.ResolverContext$Default` | 2 | 64 | `ResolverContext.java:103` | Keep lazy dependency resolution and extension-facing context. |
| `org.xacml4j.v30.BagStorages$Builder` | 2 | 64 | `BagStorages.java:61` | Keep provider construction; a capacity hint is a separate API/corpus experiment. |
| `java.lang.ScopedValue$Carrier` | 2 | 64 | `DefaultEvaluationContextHandler.java:457` | Keep JDK-scoped ancestry; no hand-rolled replacement. |
| `com.google.common.collect.LinkedHashMultimap$ValueSet$1` | 2 | 64 | `DefaultResolverRegistry.java:85` | Keep candidate enumeration; AJ already removed streams, and addResolver remains public. |
| `java.util.LinkedHashMap` | 1 | 64 | `DefaultPolicyDecisionPoint.java:360; AttributeIndex.java:476,509 (Guava)` | R1 may remove intermediate grouping; keep final category/id order and equality deduplication. |
| `org.xacml4j.v30.Attribute` | 2 | 64 | `DefaultPolicyDecisionPoint.java:368; Attribute.java:200` | Keep resolved-attribute response content; R1 removes intermediary containers, not values. |
| `java.util.ImmutableCollections$Map1` | 2 | 64 | `PipWorkload.java:142,196` | Resolver's public map result; keep existing SPI. No new direct-result API proposed. |
| `java.util.concurrent.ConcurrentHashMap$KeyIterator` | 1 | 56 | `DefaultPolicyDecisionPoint.java:361` | R1 may traverse resolved entries directly; preserve weakly consistent/concurrent behavior. |
| `[Lorg.xacml4j.v30.BagOfValues;` | 2 | 48 | `ResolverCacheKey.java:76` | Keep detached key values/order; R3 keeps this array and the serialized form. |
| `java.time.Instant` | 2 | 48 | `AttributeSet.java:57` | Keep clock reads and timestamp/TTL contract, including cache-off mode. |
| `org.xacml4j.v30.spi.pip.ResolverCacheKey` | 2 | 48 | `ResolverCacheKey.java:76` | Keep key equality/serialization; R3 avoids its input views, not the key. |
| `org.xacml4j.v30.pdp.DefaultEvaluationContextHandler$Frame` | 2 | 48 | `DefaultEvaluationContextHandler.java:421` | Keep D27 ancestry/cycle semantics; no unsafe serial-only bypass. |
| `com.google.common.collect.HashMultimap` | 1 | 48 | `DefaultPolicyDecisionPoint.java:360` | R1: avoid category multimap when all resolved designators belong to one category; keep equality deduplication. |
| `org.xacml4j.v30.BagStorage$1` | 2 | 48 | `Attribute.java:193 -> BagStorage.java:102` | R6: index an input BagOfValues; retain Iterable fallback and grouped duplicate order. |
| `org.xacml4j.v30.Result` | 1 | 48 | `Result.java:108,125` | R5 can share only empty successful results; keep results carrying data or errors. |
| `java.lang.ScopedValue$Snapshot` | 2 | 48 | `DefaultEvaluationContextHandler.java:457` | Keep JDK-scoped ancestry; this is not a D26 worker task. |
| `java.util.concurrent.CompletableFuture` | 2 | 48 | `DefaultEvaluationContextHandler.java:422` | Keep D27 overlapping-caller outcome sharing, including absence and failure. |
| `com.google.common.collect.ImmutableListMultimap` | 1 | 48 | `AttributeIndex.java:82` | Keep the final response-entity index; not a mutable staging map. |
| `org.xacml4j.v30.Category` | 1 | 40 | `DefaultPolicyDecisionPoint.java:375; Category.java:66` | Keep resolved category; no payload suppression. |
| `com.google.common.collect.RegularImmutableMap` | 1 | 40 | `AttributeSet.java:55,228; AttributeIndex.java:82,476; Result.java:401` | Mostly final immutable map representation; builders/entry views are candidates only with a representation/assembly change. Exact per-site split is UNVERIFIED. |
| `org.xacml4j.v30.policy.Policy$PolicyDescendantEvaluationContext` | 1 | 32 | `Policy.java:200` | Keep policy identity, defaults and variable scope; context redesign after D45. |
| `org.xacml4j.v30.AttributeIndex` | 1 | 32 | `Entity.java:55; AttributeIndex.java:81` | Keep immutable attribute lookup/serialization of the response entity. |
| `org.xacml4j.v30.spi.pip.DefaultPolicyInformationPoint` | 1 | 32 | `DefaultPolicyInformationPoint.java:89` | Keep decision-local session; no sharing of memo state across decisions. |
| `java.util.LinkedHashMap$LinkedEntryIterator` | 1 | 32 | `AttributeIndex.java:82 (Guava builder build)` | R1 may avoid response builder traversal; preserve grouped attribute order. |
| `org.xacml4j.v30.policy.RootEvaluationContext$Responses` | 1 | 32 | `RootEvaluationContext.java:293` | Keep the first-write holder and live views; AU already shares immutable lists. |
| `com.google.common.collect.SingletonImmutableSet` | 2 | 32 | `ResolverContext.java:156 -> ImmutableBiMap.values/inverse/keySet` | R3 removes the dependency-map values view. |
| `java.util.HashMap$KeyIterator` | 1 | 32 | `DefaultPolicyDecisionPoint.java:374` | R1 may avoid intermediate iteration; retain final value order. |
| `java.util.LinkedList` | 1 | 32 | `DefaultPolicyDecisionPoint.java:370` | R1: build the final immutable category list directly. |
| `org.xacml4j.v30.types.Entity` | 1 | 32 | `DefaultPolicyDecisionPoint.java:374; Entity.java:337` | Keep response entity and its attributes. |
| `org.xacml4j.v30.pdp.DefaultEvaluationContextHandler$ResolutionState` | 1 | 32 | `DefaultEvaluationContextHandler.java:121` | Keep lazy state and publication; AV already applied. |
| `org.xacml4j.v30.policy.Rule$RuleEvaluationContext` | 1 | 32 | `Rule.java:74` | Candidate R2: own-engine rule context elision, after D45; preserve current-rule/status/staging semantics. |
| `java.util.HashMap$EntryIterator` | 1 | 32 | `DefaultPolicyDecisionPoint.java:371` | R1: avoid outer category-map traversal in a one-category path. |
| `org.xacml4j.v30.BagStorages$Values` | 1 | 32 | `BagStorages.java:140` | Keep grouped multiset values and order; X/AF already applied. |
| `org.xacml4j.v30.Category$Builder` | 1 | 24 | `DefaultPolicyDecisionPoint.java:373` | R1 can bypass/collapse final assembly through a narrow factory if worthwhile; public API decision required if exposed. |
| `com.google.common.collect.ImmutableMapEntry$NonTerminalImmutableMapEntry` | 1 | 24 | `AttributeIndex.java:82 (Guava)` | Keep final immutable index hash bucket; only a separately designed representation could remove it. |
| `org.xacml4j.v30.AttributeIndex$Builder` | 1 | 24 | `Entity.Builder -> AttributeIndex.java:108` | R1 may remove builder staging; preserve final index behavior. |
| `java.util.LinkedList$Node` | 1 | 24 | `DefaultPolicyDecisionPoint.java:372` | R1: eliminate with the intermediate LinkedList. |
| `com.google.common.collect.AbstractMapBasedMultimap$KeySet` | 1 | 24 | `DefaultPolicyDecisionPoint.java:371` | R1: avoid the intermediate category-key view. |
| `org.xacml4j.v30.pdp.DefaultEvaluationContextHandler` | 1 | 24 | `PolicyDecisionPointContextFactory.java:239` | Keep: request-specific handler; AV already moved its lazy members. |
| `java.util.LinkedHashMap$LinkedEntrySet` | 1 | 24 | `DefaultPolicyDecisionPoint.java:360; AttributeIndex.java:476,509 (Guava)` | R1 may remove intermediate grouping; keep final category/id order and equality deduplication. |
| `com.google.common.collect.AbstractMapBasedMultimap$KeySet$1` | 1 | 24 | `DefaultPolicyDecisionPoint.java:371` | R1: avoid the intermediate category-key iterator. |
| `com.google.common.collect.ImmutableListMultimap$Builder` | 1 | 24 | `AttributeIndex.java:476` | R1 representation/assembly follow-up only; retain public builder semantics. |
| `java.util.concurrent.ConcurrentHashMap$KeySetView` | 1 | 24 | `DefaultPolicyDecisionPoint.java:361` | R1 may traverse resolved entries directly; compare iterator/lambda costs before choosing a form. |
| `[Lcom.google.common.collect.ImmutableMapEntry;` | 1 | 24 | `AttributeSet.java:55,228; AttributeIndex.java:82,476; Result.java:401` | Mostly final immutable map representation; builders/entry views are candidates only with a representation/assembly change. Exact per-site split is UNVERIFIED. |
| `java.util.HashMap$EntrySet` | 1 | 16 | `DefaultPolicyDecisionPoint.java:371 (HashMultimap key iteration)` | R1: avoid outer category-map traversal in a one-category path. |
| `org.xacml4j.v30.BagStorages$Single` | 1 | 16 | `BagStorages.java:106` | Keep scalar storage; AF already avoids arrays for this bag. |
| `java.util.HashSet` | 1 | 16 | `DefaultPolicyDecisionPoint.java:363,368` | Keep deduplication or prove equal assignments cannot arise on the specialized path. |
| `java.util.HashMap$KeySet` | 1 | 16 | `DefaultPolicyDecisionPoint.java:374 (HashMultimap value iteration)` | R1 may avoid intermediate views; retain final value order. |
| `org.xacml4j.v30.ResponseContext` | 1 | 16 | `ResponseContext.java:55,64` | Keep this non-plain response. AQ already shares plain responses. |
| `com.google.common.collect.RegularImmutableList` | 1 | 16 | `Attribute.java:59` | Keep final immutable lists; reduce intermediary construction only where equivalence is proved. |
| `org.xacml4j.v30.spi.pip.DefaultPolicyInformationPoint$DecisionSets` | 1 | 16 | `DefaultPolicyInformationPoint.java:79` | Keep task-30 successful-set memo and TTL semantics. |
| **Total** | **178** | **5864** | | |

## rsa2008 CPU: top 15 frames by self time

There are **48,547 samples under PdpDecideBenchmark.decide**, out of 48,582 total; 35 outside-decide samples are excluded from this denominator. A self sample is charged only to the last frame. Inclusive totals are not added together.

| Rank | Frame | Self samples | Share of decide samples | Source / caller |
| ---: | --- | ---: | ---: | --- |
| 1 | `java/lang/String.equals` | 3,650 | 7.518% | `Value.java:133 (mostly bag/string equality); String.equals is JDK code` |
| 2 | `org/xacml4j/v30/policy/TargetBeforeContext.precheck` | 2,810 | 5.788% | `TargetBeforeContext.java:175` |
| 3 | `org/xacml4j/v30/policy/Rule.evaluateRule` | 2,730 | 5.623% | `Rule.java:117` |
| 4 | `itable stub` | 2,356 | 4.853% | `JVM interface-dispatch stub; leading callers TargetBeforeContext.java:79, BagOfValues.size, PolicySet.java:372` |
| 5 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicy` | 1,428 | 2.941% | `DescendantEvaluationContext.java:215; principal caller PolicySet.java:372` |
| 6 | `org/xacml4j/v30/policy/function/impl/HigherOrderFunctions.validateArguments` | 1,090 | 2.245% | `HigherOrderFunctions.java:433` |
| 7 | `org/xacml4j/v30/policy/DescendantEvaluationContext.isOwn` | 1,089 | 2.243% | `DescendantEvaluationContext.java:163` |
| 8 | `org/xacml4j/v30/policy/combine/impl/LegacyDenyOverridesPolicyCombineAlgorithm.doCombine` | 868 | 1.788% | `LegacyDenyOverridesPolicyCombineAlgorithm.java:56` |
| 9 | `org/xacml4j/v30/policy/DescendantEvaluationContext.getCurrentPolicySetIDReference` | 849 | 1.749% | `DescendantEvaluationContext.java:308` |
| 10 | `org/xacml4j/v30/policy/PolicySet$Prepared.children` | 790 | 1.627% | `PolicySet.java:240` |
| 11 | `com/google/common/collect/RegularImmutableMap.get` | 777 | 1.601% | `RequestContext.java:251 (mostly); AttributeIndex.java:351; Guava lookup implementation` |
| 12 | `org/xacml4j/v30/SemanticIdentifier.hashCode` | 704 | 1.450% | `SemanticIdentifier.java:102` |
| 13 | `org/xacml4j/v30/policy/BaseCompositeDecisionRule.isApplicable` | 645 | 1.329% | `BaseCompositeDecisionRule.java:208` |
| 14 | `org/xacml4j/v30/policy/function/impl/DefaultFunctionInvocation.invokeFast` | 628 | 1.294% | `DefaultFunctionInvocation.java:197` |
| 15 | `org/xacml4j/v30/types/Value$OtherValue.equals` | 624 | 1.285% | `Value.java:133` |

The `itable stub` is reported as a VM frame, not assigned to one Java method. Its leading sampled callers include target dispatch (665 samples), bag size (507), set-context construction (326), enclosure checks (299), and composite response handling (211).

| Exclusive phase | Samples | Share |
| --- | ---: | ---: |
| Policy evaluation: other | 38,865 | 80.056% |
| Policy evaluation: target precheck | 8,554 | 17.620% |
| Root/handler creation | 328 | 0.676% |
| Result/response assembly | 423 | 0.871% |
| PDP dispatch/counters/other | 377 | 0.777% |

The phases are a reproducible partition in `cpu.py`: result/response assembly first; root/handler creation second; precheck third; any remaining policy frame fourth; the remaining PDP path last. Thus **policy evaluation is 97.676%**, versus **2.324% fixed decision overhead** (0.676% root/handler creation, 0.871% result/response assembly, 0.777% dispatch/counters). Multiplying by the profiled 1.949 µs gives approximately **1.904 µs evaluation** and **0.045 µs fixed overhead**; these are indicative attribution estimates, not separately measured timings.

Condition.evaluate is **32.290% inclusive** and Apply.evaluate **30.879% inclusive**; these overlap. They explain why D45 deserves its own before/after measurement, not a prediction from this allocation profile. The higher-order validation frame is still visible, but AW's declined checked-arguments API is not being re-proposed. No source was changed to manufacture a cheaper result.

## Per-request reconciliation

Each row has 10,000 decisions. Application histogram bytes (after the documented VM/probe exclusions) and the ThreadMXBean count are identical in all rows. The empty control's decision-loop allocation is zero. This keeps heterogeneous Indeterminate, Permit, Deny and NotApplicable outcomes visible rather than treating an `all` average as one request.

| Workload | Request | Bytes/decision |
| --- | --- | ---: |
| rsa2008-all | `01-01` | 664 |
| rsa2008-all | `01-02` | 696 |
| rsa2008-all | `01-03` | 664 |
| rsa2008-all | `01-04` | 1000 |
| rsa2008-all | `02-01` | 688 |
| rsa2008-all | `02-02` | 688 |
| rsa2008-all | `02-03` | 792 |
| rsa2008-all | `02-04` | 688 |
| rsa2008-all | `03-01` | 720 |
| rsa2008-all | `03-02` | 792 |
| rsa2008-all | `03-03` | 824 |
| rsa2008-all | `04-01` | 312 |
| rsa2008-all | `04-02` | 848 |
| rsa2008-all | `04-03` | 848 |
| rsa2008-all | `04-04` | 808 |
| rsa2008-all | `05-01` | 1008 |
| rsa2008-all | `05-02` | 856 |
| rules-narrow | `narrow` | 280 |
| rules-responses | `responses` | 2336 |
| rules-multi-valued | `multi-valued` | 848 |
| synthetic-all | `first-permit` | 256 |
| synthetic-all | `first-deny` | 256 |
| synthetic-all | `middle-permit` | 256 |
| synthetic-all | `middle-deny` | 256 |
| synthetic-all | `last-permit` | 256 |
| synthetic-all | `last-deny` | 256 |
| synthetic-all | `unknown-notapplicable` | 184 |
| pip-independent | `independent` | 5864 |

## Evidence and remaining uncertainty

- Complete raw execution: `openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/p-fixed3-astra/histograms/` (six JVMs, 28 requests, command/flags/help/control/before/after/metadata/delta files), plus `cpu-rsa2008/` (JMH JSON/log and collapsed CPU stacks).
- Reproduction and analysis: `AllocationProbe.java`, `profile.py`, `analyze.py`, `cpu.py`, `write_report.py`, `validate.py`; `allocations.json`, `cpu.json`, `guava-singletons.bytecode.txt`.
- Scoped changes and snapshot: `source-before.json`, `source-after.json`, `source-changes.json`, `fix-av-p3.diff`, `validation.json`. Only the two authorized production files changed; there are no new production or test files.
- The application class counts are measured; proposed removal ranges and per-site splits of shared array/collection classes are not. One Epsilon JVM per workload does not establish a statistically stable JMH optimization pair or a concurrent-PIP profile. PIP cache-on, parallel attributes, missing/failing resolvers, selectors, and wide/dynamic external workloads are outside this packet's measured set.
- The current supplied tree does not include D45's in-progress implementation. Any proposal touching Condition, Policy or PreparedPolicySets is **after D45** and must be rechecked against its merged form. The status/error and extension-compatibility tests remain the gate for future changes; no allocation/mechanism tests were added.
