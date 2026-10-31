FINAL REPORT (Codex Sol, GPT-6.1)

**PARTIAL: source analysis and design completed; fresh profiling and report-file creation are blocked by this lane’s read-only access contract.** No JVM, build, or test was run. Neither the requested `target/` profiles nor `REPORT-P-COND-sol.md` was created.

The source points first to regex preparation. Generic literal folding has no eligible calls in these three workloads.

**Retained baseline**

I parsed Opus’s retained `rules1.json` and `rules100.json`, with the index off:

| Workload | B/op | µs/op |
|---|---:|---:|
| `rules:1`, repeated-conditions | 2,680.0 | 0.968 |
| `rules:100`, repeated-conditions | 194,276.2 | 63.007 |
| Increment per additional rule | 1,935.3 | 0.627 |

These are Opus’s measurements, not a fresh baseline of this worktree. Times remain indicative. [I-CORPUS report](`.ntm/worktrees/xacml4j--batch2/cc_1/REPORT-I-CORPUS-opus.md`).

The complete conditions differ by department literal. The repeated subtree is the email expression; the department’s one-and-only extraction also repeats. Memoizing whole conditions would therefore miss the principal opportunity. [RulesCorpus.java:259](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/RulesCorpus.java:259`).

**Source findings—not allocation-profile attribution**

| Candidate | Finding |
|---|---|
| Pattern compilation | The cache flag defaults to false. Under the recorded default configuration, every regex evaluation takes `Pattern.compile(jdk)`. |
| Pattern preparation | Translation and repetition scanning happen before the cache lookup, even when caching is enabled. The scanner constructs an `ArrayDeque`. |
| Matching | Each call creates a matcher and deadline wrapper. These remain necessary after pattern precompilation. |
| String intermediates | Normalize-space and lower-case each construct a `StringVal`; this input also requires two changed strings. |
| Boolean values | `BooleanVal` returns cached true/false instances. Fresh BooleanVal objects do not explain the cost. Boolean factory staging still needs profiling. |
| One-and-only/bags | Successful one-and-only returns the existing value. Request bags are memoized; repeated bag construction is not supported by the inspected path. |
| Apply arrays | Built-in applies already use `invokeWithList`; there is no argument array for every Apply. |
| Remaining invocation staging | Regex’s evaluated arguments use an `ArrayList`; lazy `and` packs an `Expression[2]`. Its operand loop itself is already direct. |

Relevant sites: [RegExpFunctions.java:178](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:178`), [regex preparation and matching:388](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/RegExpFunctions.java:388`), [StringConversionFunctions.java:42](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/function/impl/StringConversionFunctions.java:42`), [Apply.java:268](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/Apply.java:268`), [FunctionSpecBuilder.java:769](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:769`).

**The 1.9 KB/rule is not quantitatively accounted for yet.** Assigning its remainder to regex compilation without the requested byte-weighted profile would be unsupported.

**Provisional ranking by gain and risk**

All estimates below are **unmeasured**. Concrete post-change µs/op forecasts remain **UNVERIFIED**.

| Rank | Fix | Expected effect on repeated-conditions | rsa2008 / synthetic:100 |
|---:|---|---|---|
| 1 | Literal regex binding through the regex SPI | Removes 100 warm-path translations, scans and compilations per decision; matching remains. Byte/time saving requires profiling. | No regex work removed in either corpus. |
| 2 | Simplify regexp conversion staging | Removes intermediate Optional/map/supplier staging while retaining the selected converter. Source allocation envelope: 0–8 KB/decision after escape analysis. | No regex work removed. |
| 3 | Canonical identities plus decision-local success memo | Normalized-email prefix evaluated once rather than 100 times. Illustrative fully materialized string model saves about 17.3 KB net, giving roughly 177 KB/op. | No eligible normalization prefix. No memo map; a new root reference could cost 0–8 B/decision. CPU overhead unmeasured. |
| 4 | Prepared condition plan | `and`’s array alone is a 2.4 KB/decision source-size envelope; regex’s evaluated list/backing array another 4.8 KB. Actual survival and CPU cost require profiling. | No condition path in synthetic. RSA applicability and overhead need measurement. |
| 5 | General all-literal folding | No eligible calls found in this repeated condition. | Static census found none in the nine RSA policies; synthetic has none. Defer. |

The string estimate assumes ordinary JDK 25 headers, compressed references, Latin-1 strings, and all relevant objects materializing: approximately 176 B per normalization pair, 99 avoided pairs, and approximately 160 B for a one-entry memo. It is an allocation model, **not profiler evidence**.

Do not add these savings: a shared whole-email result would already remove most conversion and invocation staging.

**1. Literal regex preparation: use the existing SPI design**

The separate regex change already specifies this in **D6**: bind a literal pattern at its call site on first evaluation, with no expression evaluation at load. Reuse that decision rather than introducing another pattern cache. [Regex SPI design:161](`openspec/changes/add-regex-engine-spi/design.md:161`).

Bind through `RegexEngine.compile(...) → CompiledRegex`, preserving engine, profile and limits. Share the compiled artifact; keep matcher state and budget per call. JDK patterns support concurrent sharing; matchers do not. [OpenJDK 25 Pattern source](https://raw.githubusercontent.com/openjdk/jdk/jdk-25-ga/src/java.base/share/classes/java/util/regex/Pattern.java).

Malformed or unused expressions must not become new policy-load failures. Keep the SPI’s approved deferred error mapping and budget rules. Actual eager load-time compilation would depart from D6 and needs a maintainer decision.

Proposed files: regex SPI implementation, `RegExpFunctions`, and the SPI’s `Apply`/`Match` binding sites. **Existing regex D6; no duplicate optimization D-number needed.** This overlaps the regex change and Fable’s invocation work.

Before implementation, the existing `cacheRegExps=true` setting is a useful profiling control: it isolates compilation savings while leaving translation and scanning in place.

**2. Conversion staging: a local cycle**

At `RegExpFunctions.matches`, unwrap the existing converter once, invoke it, check the converted result, then call the matcher. Preserve the current missing-converter and null-conversion error paths.

Do not replace conversion with `StringVal.get()` indiscriminately: `TypeToString` discovers capabilities through its SPI. [TypeToString.java:292](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/types/TypeToString.java:292`).

Proposed file: `RegExpFunctions.java` only. No public API change; **local cycle**, preferably integrated with the regex owner. Proceed only if the profile shows material surviving staging.

**3. Common subexpressions: new design decision required**

Use Guava interning for immutable canonical plan identities. Hash-consing alone saves no evaluation work; it is a prerequisite for the memo, not a standalone runtime optimization. Guava supplies a thread-safe weak interner. [Official Interners source](https://raw.githubusercontent.com/google/guava/master/guava/src/com/google/common/collect/Interners.java).

Do not blindly intern `Apply` using existing equality: `FunctionSpecImpl.equals()` compares function IDs. The canonical key must distinguish actual binding/provider identity and factory provenance, ordered children, typed literals, and the complete designator including `MustBePresent`. [FunctionSpecBuilder.java:820](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:820`).

Initial eligibility should be narrow:

- Known default built-ins with proven purity; custom specs, factories, validators and contexts retain their existing paths.
- Immutable primitive values; no selectors/content, implicit time, randomness or unresolved variable scope.
- Successful normalized-email evaluation. Its one-and-only input proves a positive bag.
- No failure memoization. A successful expression that consumed absence or masked a failure is also unsafe to cache generally: it could suppress a later retry.

Store canonical plans in the **D24/D25 generation**, but values in the **individual root decision**. The generation is shared across decisions; it must never retain request-dependent results. A lazy JDK `ConcurrentHashMap` is sufficient, bounded by prepared repeat candidates. Evaluate outside map callbacks and publish with `putIfAbsent`; do not introduce another flight mechanism.

Memoize maximal expensive repeated nodes, not every nested node. Keep the public AST unchanged through a sidecar plan. A minimal internal shape is:

```java
generation.conditions(policy).evaluate(rule, context)
```

For the first cut, disable sharing when D34 observation is enabled, preserving callback sequences. An expression-memo event or changed tracing contract is a maintainer question.

Proposed files: new `PreparedConditions`, `Condition`, generation registry, `RootEvaluationContext`, and trusted function metadata integration. **New D required** for purity, scope, retry and observation contracts. Public purity API additions remain an Open Question.

**4. Prepared plans and literal folding: defer broad compilation**

A prepared plan must preserve original operand order and failure handling. In particular, do not move department comparison before regex evaluation. `and` evaluates left-to-right; false can determine the result despite an earlier error.

Use existing dispatch and exception mapping. Retain static type errors, runtime-validation behavior and custom-function fallbacks. A general compiler is not justified yet by the relatively small identified staging envelopes.

Generic all-literal folding removes no work in these controls. If resumed elsewhere, fold only successful, type-correct, context-free known built-ins; retain failing expressions for normal demanded evaluation.

An existing lower-risk control is explicit policy factoring through `VariableDefinition`: its evaluator already caches successful values. A factored corpus variant would help measure the sharing opportunity without first building a new engine facility. [VariableDefinition.java:115](`.ntm/worktrees/xacml4j--batch2/cod_2/xacml-core/src/main/java/org/xacml4j/v30/policy/VariableDefinition.java:115`).

These contracts follow XACML §§7.3.5, 7.4, 7.8, 7.19 and function rules A.3.5, A.3.10 and A.3.13. [Official XACML 3.0 standard](https://docs.oasis-open.org/xacml/3.0/errata01/os/xacml-3.0-core-spec-errata01-os-complete.html).

**Ownership and validation**

Fable overlap: `FunctionSpecBuilder.java` contains `FunctionSpecImpl`; also `FunctionSpec`, invocation handling and `RootEvaluationContext`. A condition-sidecar approach can avoid changes to `BaseDecisionRule`, combining implementations and `TargetBeforeContext`.

Terra overlap: generation/`Policy` preparation hooks may share files with index work. No `TargetIndex` change is proposed.

Tests should compare public outcomes for positive/negative email, missing and multivalued email, error masking by `and`, transient failure retries, separate decisions, binding/MustBePresent distinctions, and D34 sequences. Reuse existing function and serialization coverage.

**Required profiling remains open**

Prepared flags, **not executed**:

- Host JVM: `-Djmh.ignoreLock=true`.
- Workload: `store=in-memory corpus=rules:100 request=repeated-conditions targetIndex=false`.
- C2 fork flags and `-prof gc`.
- Allocation profiler: `event=alloc;output=collapsed;rawCommand=total;dir=target/p-cond-sol/alloc`.
- CPU profiler: `event=cpu;interval=1000000;output=collapsed;dir=target/p-cond-sol/cpu`.

`rawCommand=total` is important: JMH forwards it to async-profiler, selecting metric totals instead of sample counts. [JMH 1.37 adapter](https://raw.githubusercontent.com/openjdk/jmh/1.37/jmh-core/src/main/java/org/openjdk/jmh/profile/AsyncProfiler.java), [async-profiler 4.5 options](https://raw.githubusercontent.com/async-profiler/async-profiler/v4.5/docs/ProfilerOptions.md).

Use disjoint allocation categories and reconcile byte-weighted sampled estimates with GC B/op. Do not sum overlapping CPU frames.

Executed verification: source inspection, retained JSON parsing, static XML census and primary documentation lookup. **Remaining:** fresh byte-total and CPU profiles, measured site attribution, defensible numeric forecasts, and saving the report. The lane’s access configuration must permit artifact writes to finish those steps.
