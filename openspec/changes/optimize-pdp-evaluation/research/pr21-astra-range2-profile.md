FINAL REPORT (cod_3, Codex Astra)

**Recommendation: GO for a narrow D46, initially integer/date ranges on inline rules.** The remaining opportunity is CPU: the date rule prechecks account for 82.13% of decision CPU samples; approximately 80.49%, or 6.62 µs/op in that profiled fork, belongs to the 98 rejected rules. This is an estimated removable budget, before the replacement query's cost. AM/AO and the subsequent cycles have already removed nearly all allocation associated with rejecting those rules. A range filter should not be sold as another allocation reduction.

Keep the existing target-index opt-in. Start with the two comparison forms present in this corpus and use the existing prepared generation. Extending this to range targets on referenced policies, additional ordered types, or multi-key targets can follow separate evidence. No new public API is needed for the initial scope.

Only this report and artifacts under `target/p-range2-astra/` were written. No production code, tests, or OpenSpec files were changed.

**Runs and comparison with P-RANGE**

Built the current dirty worktree successfully with:

```sh
export PATH="$HOME/Applications/IntelliJ IDEA.app/Contents/plugins/maven-plugin/lib/maven3/bin:$PATH"
JAVA_HOME="$(rtk proxy jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am -DskipTests package
```

Environment: Apple M2 Pro, macOS 26.6.1 aarch64, Oracle GraalVM 25.0.4, C2 (`-XX:-UseJVMCICompiler`), Maven 3.9.16, JMH 1.37, async-profiler 4.5, default object headers, 2 GiB heap and G1. The host JVM received `-Djmh.ignoreLock=true`. Each combination used one thread, one fork, three 1-second warmups and three 1-second measurement iterations. Times are indicative on this shared machine. The old runs are historical comparisons, not a controlled before/after pair of individual cycles.

Completed six GC-only trials, six allocation-profile trials with GC, and one CPU-profile trial of date-ranges/index-off with GC: **13 forks, 39 measured iterations, all exit 0**. Each trial's fixture checked its expected decisions before and after measurement. No test suite was run for this analysis task.

The historical columns come from `openspec/changes/optimize-pdp-evaluation/research/pr18-astra-range-profile.md`, the original P-RANGE report, rather than the earlier supplied Opus baseline. Current time errors are JMH's reported 99.9% intervals.

| Request | Index | P-RANGE µs/op | Current µs/op ± error | P-RANGE B/op | Current B/op | Byte change |
|---|---|---:|---:|---:|---:|---:|
| integer-ranges | off | 9.258 | 6.357 ± 1.110 | 5,457.37 | 316.21 | −94.2% |
| integer-ranges | on | 12.122 | 6.374 ± 5.586 | 5,625.68 | 267.95 | −95.2% |
| date-ranges | off | 13.152 | 8.177 ± 2.641 | 6,313.78 | 301.26 | −95.2% |
| date-ranges | on | 12.977 | 8.387 ± 1.439 | 6,393.77 | 349.45 | −94.5% |
| multi-valued | off | 14.519 | 9.909 ± 1.438 | 5,681.87 | 750.52 | −86.8% |
| multi-valued | on | 5.171 | 3.533 ± 1.952 | 3,736.69 | 1,026.40 | −72.5% |

There is no significant on/off timing difference for either range case in these trials. Multi-valued indexing saves an indicative 6.38 µs/op (64.3%), with non-overlapping reported intervals, but adds 275.88 B/op now that rejecting policies is already cheap in allocation. This workload has equality targets; a range filter would contribute no additional saving.

Short-run allocation means include a higher third iteration in every GC trial. The first two iterations are stable near 312/264 B for integer off/on, 296/344 B for date, and 744/1,024 B for multi-valued. The table reports the complete means, without discarding an iteration. The cause of the final-iteration increment was not isolated. Separate forks also differ by approximately one 48-byte object in several cases, so small allocation differences should not be overinterpreted.

Allocation profiles used `event=alloc;output=collapsed;verbose=true;rawCommand=total`. The process log confirms `start,total,event=alloc` and `collapsed,total,event=alloc`. The table below sums allocation **byte weights**, not event counts; these are sampling estimates. Only stacks containing `PdpDecideBenchmark.decide` contribute to the site percentages.

| Request | Index | Profiled B/op | Decision allocation weight, bytes | Share of all allocation weight |
|---|---|---:|---:|---:|
| integer-ranges | off | 268.75 | 122,683,158 | 99.15% |
| integer-ranges | on | 267.80 | 162,528,970 | 99.36% |
| date-ranges | off | 302.10 | 100,138,817 | 98.45% |
| date-ranges | on | 349.85 | 120,061,723 | 99.13% |
| multi-valued | off | 799.03 | 235,404,863 | 99.56% |
| multi-valued | on | 1,074.84 | 750,778,984 | 99.79% |

In source citations below, `core/` means `xacml-core/src/main/java/org/xacml4j/v30/`, `pdp/` means `xacml-pdp/src/main/java/org/xacml4j/v30/`, and `jmh/` means `xacml-jmh/src/main/java/org/xacml4j/jmh/`.

**What remains per decision**

The generated policies still use overlapping intervals `[10i, 10i + 15)` and a request at position 992. Integer uses first-applicable; date uses deny-overrides. Thus they are not a controlled datatype pair (`jmh/corpus/RulesCorpus.java:268`, `:281`). The following counts follow from the generated policies and combining order; no call-counter test or instrumented production code was added.

| Workload | Rejected children in the measured branch | Remaining work | Allocation attributable to rejection |
|---|---|---|---|
| integer, either index setting | Rules 0–97; rule 98 permits; rule 99 is not visited | 198 comparison scans and request-bag lookups across 99 rule prechecks; 196 belong to rejected rules | No precheck/lookup allocation sampled; about zero B/op demonstrated at these sites, not a proof that every JVM eliminates all temporaries |
| date, either setting | Rules 0–97; rules 98 and 99 match, with Deny winning | 200 scans/lookups across 100 rule prechecks; 196 belong to rejected rules | No precheck/temporal-scan/lookup allocation sampled |
| multi-valued, index off | 92 role policies, plus three outer case checks | Eight surviving policies and their eight conditions; nine role values and eight group values | No allocation sampled in the rejected-policy prechecks |
| multi-valued, index on | No rejected role policy is visited | Existing equality index selects eight policies; their conditions and contexts remain | Selection itself is approximately 275 B/op in the allocation profile |

AM's warmed lookup reads the existing bag by `AttributeDesignatorKey` (`core/AttributeIndex.java:349`). AO allows the precheck to finish date targets through the same typed comparison used by ordinary functions (`core/policy/function/impl/DefaultFunctionInvocation.java:253`, `:264`; `core/BagStorages.java:385`, `:402`; `core/types/ISO8601Date.java:157`). The no-zone date corpus is eligible. The precheck also carries a positive match into the child's ordinary match call, so the two matching date rules do not redo their targets (`core/policy/RootEvaluationContext.java:546`; `core/policy/BaseDecisionRule.java:187`).

The allocation profiles now show fixed decision/context objects and contexts of **surviving** rules. For date off/on, rule-context allocation is 27.23%/18.78%, roughly 82/66 B/op. It represents the two matching rules, not the 98 rejected rules, and a candidate filter cannot remove it. Integer off/on has roughly 30/37 B/op there for its one visited matching rule. The remaining large sites are `pdp/pdp/PolicyDecisionPointContextFactory.java:240`, `:245`, `core/policy/Policy.java:200`, and `core/policy/PolicySet.java:196`.

Neither range/index-on allocation profile contains `TargetIndex.candidates` allocation. The root's singleton string selector reuses its candidate list (`core/policy/TargetIndex.java:103`); the inner unsupported range index returns its original children at line 87. Date/index-on does sample `Result.Builder.build` (14.41%, about 50 B/op; `core/Result.java:505`), while date/index-off does not. Different escape-analysis/inlining outcomes could explain that fixed-object difference, but compiler logs were not collected: that explanation is **UNVERIFIED**.

For multi-valued/index-on, `TargetIndex.candidates` and `candidatesFor` account for 25.63% inclusive, approximately 275.47 B/op (`core/policy/TargetIndex.java:110`, `:127`). These are the decision-local mask and materialized list for eight children. Policy and rule contexts together account for 49.09%, approximately 528 B/op. The argument-list and varargs allocation that dominated the old profile is absent from these samples after AS. A multi-key target filter cannot remove these conditions: the extra group predicates are in the condition, not the target (`jmh/corpus/RulesCorpus.java:301`).

**Date CPU and recoverable budget**

CPU profiling used a 1 ms sampling interval. It recorded 3,060 samples, of which 3,011 (98.40%) include the benchmark's decision method. The profiled fork measured 8.226 µs/op and 301.52 B/op. Inclusive rows below overlap.

| Site | Decision CPU share | Approximate time in this fork |
|---|---:|---:|
| All target prechecks, `core/policy/TargetBeforeContext.java:175` | 83.53% | 6.87 µs/op |
| Prechecks specifically under the date rule combining loop, same site under `DenyOverrides.doCombine` | 82.13% | 6.76 µs/op |
| Temporal bag scanning, `core/BagStorages.java:402` | 35.54% | 2.92 µs/op |
| Date comparison, `core/types/ISO8601Date.java:157` | 26.87% | 2.21 µs/op |
| Date-to-instant work, `core/types/ISO8601Date.java:161` | 25.27% | 2.08 µs/op |
| Request entity lookup, `core/policy/RootEvaluationContext.java:403` | 15.21% | 1.25 µs/op |
| Request bag lookup, `core/AttributeIndex.java:349` | 5.61% | 0.46 µs/op |

All 100 date rules execute both bound comparisons. Assuming comparable cost for their prechecks, 98% of the rule-precheck share belongs to rejected rules: `8.225707 × (2473 / 3011) × 0.98 = 6.62 µs/op`, or **80.49% of decision time**. This is a source-assisted estimate, not a profiler label that distinguishes a returning NoMatch from Match. It excludes additional rejection bookkeeping, and it does not subtract the new filter's work.

Only the requested date CPU profile was taken. Integer's 196/198 wasted scans and multi-valued's 92 rejected policies are source-established counts; their exact CPU percentages are **UNMEASURED**. Integer's whole-decision time of about 6.36 µs bounds its possible saving. The measured multi-valued on/off difference illustrates the CPU value of skipping the repeated checks, but includes the other effects of enabling the existing equality index.

| Proposed effect at rules:100 | Allocation saving | Expected time saving, unmeasured |
|---|---|---|
| Integer range candidates retaining rules 98 and 99 | Approximately 0 B/op from rejected rules | Planning estimate 3–5 µs/op; no integer CPU attribution was collected |
| Date range candidates retaining rules 98 and 99 | Approximately 0 B/op from rejected rules | Planning estimate 4–6 µs/op net, within the 6.62 µs rejected-precheck budget |
| Multi-valued equality workload | 0 B/op from a range filter | 0 µs/op expected; keep its existing equality path |

The expected time savings reserve work for one bag lookup, finding its extrema, endpoint searches, and materializing the surviving positions. They are targets for implementation measurement, not measured D46 results. Reusing the current mask/list approach can add roughly **100–250 B/op** for a 100-rule range query with two survivors; this is an unmeasured sizing estimate, informed by the measured 275 B/op for eight equality candidates. Do not add an unbounded request-value cache to conceal that cost. If a controlled pair does not show a useful latency gain at 100 rules, defer D46 or restrict it to the measured crossover; larger rule sets would then be the next justification. Wider bags also increase today's repeated scan work, but bags with distant extrema can retain most intervals and erase selectivity.

**Refreshed rank 1: proposed D46, ordered range candidates**

This is proposed design text for maintainer approval, not a change to `design.md`.

The PDP SHALL optionally select ordered-range candidates through its existing target-index setting. The initial implementation SHALL cover inline `Rule` lists in `Policy`, with integer and date literals and the forms `type-less-than-or-equal(L, designator)` and `type-greater-than(U, designator)`. These are the measured forms. It SHALL preserve the original policy objects and their authored child lists. Policy-set children and references SHALL keep their existing preparation, equality-index and visit paths in this first scope.

Preparation SHALL accept exact engine target/AnyOf/AllOf/Match/designator/value classes, the first AnyOf only, and one complete designator key and ordered type per range plan. Every Match in every AllOf alternative of that first AnyOf must be a supported, total bound comparison on that key; otherwise the child is always retained. `MustBePresent=true`, custom expressions, selectors and unsupported comparisons stay on fallback initially. Unsupported siblings remain in their original positions. Never infer comparison direction from a function identifier alone: require the engine FunctionSpec implementation, a direct scan for the typed literal, and preparation-time lower/equal/upper scan checks for the claimed operator, extending the existing equality certification (`core/policy/TargetIndex.java:182`, `:203`; `core/policy/function/FunctionSpecBuilder.java:414`). In-process extension declarations remain trusted; this is not a sandbox for malicious providers. No new public function bridge is proposed.

Prepare immutable sorted endpoint arrays and alternative/child positions with JDK sorting and binary search. Store one endpoint posting per bound, rather than a full mask per endpoint. Keep a decision-local BitSet of surviving alternatives; clear alternatives whose lower or upper constraint fails, then retain a child if any alternative survives. Reuse the existing ordered materialization pattern and return the original immutable list when nothing is removed. This gives linear stored postings and roughly `O(B + log E + rejected postings + retained positions)` query work for bag width B and endpoint count E. It avoids repeated category lookup, bag lookup, dispatch and temporal conversion for each rule, even though clearing rejected postings can still be linear. It is not a custom interval-tree framework.

For an authoritative, nonempty bag B of a supported ordered type:

```text
Match(L <= designator) is true exactly when max(B) >= L
Match(U >  designator) is true exactly when min(B) < U
An AllOf with those Matches is possible when both are true.
```

The literal remains the first function argument. Different Match elements may use different witnesses: `[10,20)` matches the two-value bag `{5,25}` under these two existential tests, even though no value lies inside the interval. Consequently, neither point stabbing each bag value nor intersecting the literal bounds into a conventional interval is sound for general bags. Reversed/equal-looking bounds must not be discarded during preparation. Duplicates remain in the original bag; extrema do not rewrite it.

For integers, use the engine's comparison without adding/subtracting one at boundaries. For dates, use `ISO8601Date.compareTo`, including its existing fixed UTC default when a timezone is absent; lexical order or local-date-only comparisons are insufficient. AO already supplies the comparison contract and fallback behavior; no further temporal scan implementation is needed. Later dateTime support must keep offset and nanosecond semantics; double support must preserve signed-zero equality, infinities and IEEE comparisons, with NaN falling back initially; time must use the engine's comparison and retain unsupported timezone combinations. `time-in-range` stays outside this filter.

At query time, the range path SHALL use the precheck's deciding-thread, request-resolution, observation and validation guards (`core/policy/TargetBeforeContext.java:185`). Read the request entity once and its bag once through `Entity.resolve(AttributeDesignatorKey)`, using AM's existing memoized bag. Never call `EvaluationContext.resolve`, the PIP, or a selector, and never publish to resolved attributes. Missing entity/key, empty or unexpected bags, wrong value classes, unsupported ordering or comparison failure returns the original children for ordinary evaluation. No error is translated into NoMatch. In particular, request absence does not prove absence from the PIP. Runtime validation and enabled observation use the ordinary range path initially; existing equality-index observer behavior remains unchanged.

The skipped set SHALL be an **under-approximation of proven NoMatch**. The candidate set is an **over-approximation of Match or Indeterminate**. A rejected first AnyOf is safe only when its checked comparisons are total and have no observable evaluation effects. A later AnyOf, a missing attribute, a type error, or a failing predicate must not become a shortcut that hides an earlier status or effect. A retained rule still goes through the existing AO precheck and ordinary evaluation. The range filter neither returns Permit/Deny nor seeds a fabricated positive target result.

Normative basis: literal-first existential Match evaluation (§7.6), target truth tables (§7.7), one stable attribute bag (§7.3.5), exception and missing-attribute handling (§7.19), combining outcomes/order (Annex C), and response propagation (§7.18), in the [OASIS XACML 3.0 Standard, 22 January 2013](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html), fetched 2026-10-28. The conservative eligibility restrictions above are implementation choices.

**Composition and lifetime.** Preserve the existing equality plan when a list has one; introduce a range plan when that list has no usable equality plan. This keeps the hot string-equality path small and avoids a new multi-key query. The corpus then uses equality at the outer case selector and range selection inside the selected policy. All retained children keep declaration order and duplicate positions. Existing exact built-in algorithm eligibility stays in `Policy.Prepared.children`; no policy-set algorithm or reference filtering is broadened.

The immutable range plan SHALL be part of the same `TargetIndex` object published through `Generation.indexes.policies.putIfAbsent` (`core/policy/Policy.java:211–225`). A losing preparer uses the winning complete index; no separate plan map or partial publication is introduced. The deciding root keeps its captured generation. Invalidation swaps `PreparedPolicySets.current` for subsequent decisions, while an in-flight decision continues using its old generation (`core/policy/PreparedPolicySets.java:39`, `:53`, `:74`; `core/policy/RootEvaluationContext.java:232`). Introduce no listener, reference resolution, cached field on shared policies, or mutation of a shared mask.

**Kept:** §7.3.5 bag identity; §§7.6–7.7 target results; §7.19 decisions/statuses and ordinary error paths; Annex C order and short-circuits; §7.18 obligations/advice; evaluated policy IDs; existing reference/cycle handling under §7.15; public builders and serialized forms. The observer/validator fallback and original child lists remain available for differential checks.

**Files:** principally `core/policy/TargetIndex.java`, optionally a small package-private range-plan class in that package; a package-private guard extraction in `TargetBeforeContext` only if needed to share the existing eligibility checks. The existing `Policy.Prepared`, `PreparedPolicySets` and root-generation wiring are integration points, not a reason to add a second registry. No production change to the function package, bag representation or reference machinery is expected.

**Proof and measurement gate:** reuse the existing differential PDP tests and AO temporal cases. Add outcome cases for lower/equal/upper boundaries, literal order, `{5,25}`, duplicate values and duplicate rule positions, overlapping/reversed bounds, and alternatives; include an earlier Indeterminate followed by an otherwise rejecting target, absent/PIP-backed attributes, MustBePresent, type errors, explicit/absent date zones, runtime validation, and observation. Compare decisions, complete statuses, policy IDs and ordered obligations/advice. A repository replacement and concurrent decisions must see complete old/new plans. These are behavioral tests; add no allocation, log, cache-mechanism or call-counter tests. Gate on another-family review, reactor/conformance, then a controlled batch pair. Include range counts below and above 100 and selective/wide-bag requests to establish crossover; keep rsa2008, synthetic and multi-valued as controls. The current one-fork research is sufficient to justify drafting D46, not to accept its implementation or claim its predicted gain.

**Evidence inventory and limits**

- `target/p-range2-astra/build.log`: successful package build; tests skipped.
- `gc/`, `alloc/`, `cpu/` under that directory: exact commands, process/JMH logs, JSON and exit results; allocation and CPU collapsed profiles are in their parameter-named subdirectories.
- `run.py`: executable profiling commands; `analyze.py` and `profile-summary.json`: byte-weighted allocation and CPU site rankings; `comparison.json`: historical comparison arithmetic.
- `env.json`, `source-before.json`, `source-after.json`, `source-check.json`: environment and hashes. All **3,751** inventoried source/resource/POM/jar entries were unchanged across profiling. HEAD is `8713323705611502ad0e1576554fdaa4e3fbf374`, but the dirty merged source is identified by the manifest, not HEAD alone. Jar SHA-256: `4dcfb89bb7088a30752fb140f1c901cdd2dbf58460c5795e1f9464fca884134a`.
- Source navigation used ripwire and opened source. Its name-based call edges are partial; counts in this report come from the corpus code and measured stacks, not an assumed complete call graph.
- No new filter exists yet. Net savings, crossover, additional ordered types and larger/wider workloads remain unmeasured. The recommended first scope deliberately avoids introducing another reference/cycle optimization.
