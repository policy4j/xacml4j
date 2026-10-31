=== BEGIN REPORT P-RANGE-astra ===
FINAL REPORT (cod_3, Codex Astra)

Recommend an ordered-range candidate filter first, then a multi-key AllOf filter after adding a workload that exercises it. The existing multi-valued case already selects the eight relevant policies; its remaining allocation is mostly condition argument handling. Item 4, cheap decline on unsupported sets, is excluded at the conductor's request and remains Terra's work.

Only this report and profiling artifacts under `target/p-range-astra-20261028/` were written. Production code, tests, and OpenSpec files were not edited.

**Measured runs and evidence**

Built the current worktree, including its uncommitted merged changes, with:

```sh
export PATH="$HOME/Applications/IntelliJ IDEA.app/Contents/plugins/maven-plugin/lib/maven3/bin:$PATH"
JAVA_HOME="$(rtk proxy jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am -DskipTests package
```

Result: exit 0. JDK Oracle GraalVM 25.0.4, C2 selected with `-XX:-UseJVMCICompiler`; JMH 1.37; async-profiler 4.5. The host also received `-Djmh.ignoreLock=true`. One thread, one fork per combination, three 1-second warmups and three 1-second measurements, with the benchmark's 2 GiB heap/G1 settings. Other lanes were active: times are indicative, not acceptance measurements.

Executed six GC-only trials, six allocation-profile trials with GC, and one CPU-profile trial of `date-ranges,targetIndex=false` with GC: 13 completed forks and 39 measured iterations. All phase exit codes are zero. The initial sandbox attempt could not bind JMH's local control socket; the authorized run outside the sandbox succeeded. No test suite was run for this research task.

HEAD was `8713323705611502ad0e1576554fdaa4e3fbf374`; HEAD alone does not identify the uncommitted source. The jar SHA-256 is `875c110c186acf8b07d19e63cc231cfef5a25c20c81799d5ed8e79948732e743`. `source-manifest.json` fingerprints the jar, module POMs and production Java files; `source-check.json` reports no changes across the profiling run.

Common benchmark arguments, after `rtk proxy <JDK25>/bin/java -Djmh.ignoreLock=true -XX:-UseJVMCICompiler -jar xacml-jmh/target/benchmarks.jar`:

```text
org.xacml4j.jmh.pdp.PdpDecideBenchmark.decide
-jvmArgs -XX:-UseJVMCICompiler
-p store=in-memory -p corpus=rules:100
-p request=integer-ranges,date-ranges,multi-valued -p targetIndex=false,true
-t 1 -f 1 -wi 3 -w 1s -i 3 -r 1s -foe true -prof gc
```

The allocation phase additionally used `-prof 'async:libPath=/opt/homebrew/lib/libasyncProfiler.dylib;event=alloc;output=collapsed;dir=<alloc>;verbose=true;rawCommand=total'`. The CPU phase selected only date-ranges/index-off and used `event=cpu;output=collapsed;interval=1000000`. Complete executable commands, JSON, JMH logs and profiler command logs are in each phase directory; `run.py` reproduces them.

| Request | Index | GC-only µs/op | GC-only B/op | Allocation-profile B/op | Profiled decision allocation weight, bytes |
|---|---|---:|---:|---:|---:|
| integer-ranges | off | 9.258 | 5,457.37 | 5,457.76 | 1,553,986,668 |
| integer-ranges | on | 12.122 | 5,625.68 | 5,609.47 | 1,625,289,700 |
| date-ranges | off | 13.152 | 6,313.78 | 6,314.00 | 1,338,504,711 |
| date-ranges | on | 12.977 | 6,393.77 | 3,897.90 | 950,008,044 |
| multi-valued | off | 14.519 | 5,681.87 | 5,681.94 | 1,197,995,795 |
| multi-valued | on | 5.171 | 3,736.69 | 3,736.70 | 2,138,566,673 |

Byte shares below sum the collapsed allocation weights, not sampled event counts. `alloc/process.log` records `start,total,event=alloc` and `collapsed,total,event=alloc`. The adapter discards warmup samples and records the measurement period. Sources checked 2026-10-28: [async-profiler counter selection](https://github.com/async-profiler/async-profiler/blob/v4.5/src/arguments.cpp), [profiler output implementation](https://github.com/async-profiler/async-profiler/blob/v4.5/src/profiler.cpp), [JMH AsyncProfiler adapter](https://github.com/openjdk/jmh/blob/1.37/jmh-core/src/main/java/org/openjdk/jmh/profile/AsyncProfiler.java). Context7 had no relevant async-profiler entry; these are the upstream sources.

Shares are restricted to stacks containing `PdpDecideBenchmark.decide`, covering 99.83–100% of allocation weight. The byte weights are sampling estimates, not an object census. A site's approximate B/op is its share multiplied by that same profiled fork's GC B/op. Do not apply the date/index-on profile shares to the GC-only fork: its allocation behavior differs substantially. A different C2 escape-analysis/inlining outcome is a plausible explanation, but compiler logs were not collected, so that cause is UNVERIFIED.

`profile-summary.json`, produced by `analyze.py`, contains all exclusive, inclusive and allocated-class rankings. Exclusive here means attribution to the deepest engine method, including allocations in its JDK/Guava callees; inclusive rows overlap.

**Where the bytes and CPU go**

In the source paths below, `core` means `xacml-core/src/main/java/org/xacml4j/v30/`, `pdp` means `xacml-pdp/src/main/java/org/xacml4j/v30/`, and `jmh` means `xacml-jmh/src/main/java/org/xacml4j/jmh/`.

| Allocation site | integer off/on | date off/on | multi-valued off/on |
|---|---:|---:|---:|
| `core/AttributeIndex.java:290`, new Lookup for a bag lookup | 88.23% / 85.90% | 41.44% / not sampled | 45.12% / 3.43% |
| `core/policy/Rule.java:65`, RuleEvaluationContext | 0.40% / 0.55% | 48.14% / 82.73% | 3.68% / 7.06% |
| `core/policy/function/FunctionSpecBuilder.java:767`, evaluated argument list and traversal | — | — | 24.95% / 36.72% |
| `core/policy/function/impl/DefaultFunctionInvocation.java:383`, variadic array | — | — | 10.77% / 15.40% |
| `core/policy/TargetIndex.java:69`, inclusive candidate production | absent / 2.48% | absent / 2.59% | absent / 15.15% |

The last row includes nested lookup and BitSet helper allocations and must not be added to the exclusive rows.

Date CPU profile: 3,044 samples in total; 2,987 on decision stacks (98.13%). Its GC result was 6,313.82 B/op and 12.718 µs/op. Relevant inclusive shares, which overlap:

| CPU site | Decision samples | Interpretation |
|---|---:|---|
| `core/policy/TargetBeforeContext.java:145` | 32.01% | AI attempts request-only target checking before normal evaluation |
| `core/policy/Match.java:123` | 51.39% | Normal date target evaluation remains |
| `core/policy/function/impl/DefaultFunctionInvocation.java:308` | 22.87% | Direct predicate calls, including date comparison |
| `core/types/ISO8601Date.java:157` | 16.20% | Date comparison; `instant()` at line 161 accounts for 15.33% |
| `core/policy/AttributeReference.java:113` | 14.13% | Ordinary attribute-reference evaluation |
| `core/AttributeIndex.java:288` | 10.78% | Request bag lookup; generated Lookup.hashCode accounts for 4.02% |

1. **Date versus integer.** The earlier 8,657/3,744 = 2.31× allocation ratio is supplied baseline evidence, not reproduced by this run: the GC-only off/off ratio is 1.16×. The structural reason dates have more work remains clear. Numeric comparisons carry `@XacmlFuncBagScan` (`core/policy/function/impl/NumericComparisonFunctions.java:44`, `:73`); temporal comparisons do not (`NonNumericComparisonFunctions.java:137`, `:165`). `DefaultFunctionInvocation.scan` at line 159 consequently declines dates. AI reads the request bag, gets no scan result, and falls back (`TargetBeforeContext.java:181`); normal evaluation then creates every rule context and invokes the date predicate. Integer AI can reject most rules without those contexts.

   The date/off profile attributes approximately 3,040 B/op to rule contexts and 2,617 B/op to Lookup keys. Integer/off attributes about 4,815 B/op to Lookup keys, largely AI resolving the same designator for both range matches in successive rules. Thus the new data does not support explaining all of the original byte gap as calendar temporaries. `ISO8601Date.instant()` constructs time objects in source and consumes CPU, but no date-comparison allocation site registered in these allocation samples; elimination by the JIT is an inference, not a measured compiler fact.

   The cases also differ in more than datatype. `jmh/corpus/RulesCorpus.java:237` uses first-applicable for integer rules; line 248 uses deny-overrides for date rules. At request value 992, both intervals 98 and 99 match; integer evaluation stops at rule 98/Permit, while date evaluation reaches rule 99/Deny. The date branch is second at the root, versus integer first (`RulesCorpus.java:232`). These are not a controlled datatype pair.

2. **Index overhead on ranges.** GC-only allocation increased by 168.31 B/op for integer and 79.99 B/op for date. The inner range policy has no string-equality key: `TargetIndex.java:70` returns all its rules before resolving a key or cloning a BitSet. Its registry lookup still occurs through `Policy.java:203–217`; `PolicySet.java:228–262` similarly obtains prepared children/indexes. No allocation sample isolates those map probes, and only index-off CPU was requested, so their CPU share is UNVERIFIED.

   Candidate allocation comes from the outer root's real string-equality case selector, not a fictitious range dispatch. It resolves the case attribute (`TargetIndex.java:74`), clones/grows a BitSet (`:80`, `:84`), and materializes a selected immutable list (`:87–91`). Inclusive sampled cost is about 139 B/op in integer/on and 101 B/op in date/on. Integer is already the first root child, so this selection saves little evaluation; date saves the earlier case check. The selected range policy still traverses its complete rule list and pays the AI/normal-match work above. These observations describe the profiled baseline; they are not a new cheap-decline proposal.

3. **Multi-valued after indexing.** Allocation falls 34.2%, and indicative elapsed time falls 64.4%. The request has nine role values (eight known, one unknown) and eight groups (`RulesCorpus.java:149–153`). The index skips 92 role policies and keeps eight in declaration order. Each retained policy still runs its group condition; the final policy denies (`RulesCorpus.java:273–286`).

   Remaining exclusive allocation: evaluated argument lists/iterators about 1,372 B/op (36.72%); variadic arrays about 575 B/op (15.40%); rule and policy contexts together about 524 B/op (14.02%). Inclusive index work is about 566 B/op (15.15%), including roughly 128 B/op of bag-lookup keys, masks/backing arrays, and candidate lists. The list allocations are at `FunctionSpecBuilder.evaluate:769–772`; arrays at `DefaultFunctionInvocation.varArgs:390`. Both still support the lazy `and` and the higher-order group predicates. These are condition costs, not additional target conjuncts that a multi-key index could filter.

**Shared soundness contract for both proposed filters**

The skippable set must be an under-approximation of proven NoMatch children. The retained candidate set must be an over-approximation of children that could Match or be Indeterminate. Calling the retained candidate set an under-approximation would reverse the safety condition.

The normative basis is existential comparison within each Match; conjunction within AllOf; disjunction within AnyOf; conjunction of AnyOf results in a target, with the specified Indeterminate precedence. Missing values follow MustBePresent, and repeated tests use one immutable attribute bag per decision. Sources checked 2026-10-28: [OASIS XACML 3.0 §§7.3.5–7.7 and §7.19](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html). The following restrictions are a proposed conservative implementation, not additional XACML requirements:

- Compile only the **first AnyOf** of an exact engine Target, with exact MatchAnyOf, MatchAllOf, Match and AttributeDesignator classes. Initial eligible AllOf alternatives must be fully expressible by the supported pure comparisons; an unsupported alternative keeps the child on fallback. A proven first-AnyOf NoMatch can skip the child; any other outcome leaves subsequent AnyOfs, conditions, obligations and advice to normal evaluation. This respects the actual early return in `core/policy/Target.java:76` and status writes in `Match.java:147` / `MatchAllOf.java:87`.
- Read only authoritative request-entity bags through the existing request-only capability, with the stronger AI `hasContextFreeRequestDesignators()` guard. Do not call `EvaluationContext.resolve`, a PIP, a selector, or a user expression to select candidates; do not publish values into resolved attributes. `TargetBeforeContext.java:158–185`, `RootEvaluationContext.java:389` and `pdp/pdp/DefaultEvaluationContextHandler.java:225` provide the existing path and guard. In-process extension declarations remain trusted, but an ordinary function identifier or `writesNoResponses()` alone is insufficient provenance.
- Initially exclude MustBePresent=true designators from compiled plans, as equality indexing already does (`TargetIndex.java:156`). A future relaxation may use a proven nonempty authoritative bag, but is unnecessary for this corpus. If any required request key/category/issuer/type is absent, or a bag is empty, retain the affected child and let the existing resolver decide. Absence in the request does not prove an empty bag: a PIP may supply it or fail. Preserve the missing-attribute, syntax and processing status paths in `AttributeReference.java:126–149` and `AttributeDesignator.java:95`.
- Before rejecting a child, establish that all relevant comparisons reached in its compiled first AnyOf are safe and well typed. Unknown/custom values, failed extraction/comparison, unsupported functions or runtime validation require fallback. Do not use a later false conjunct to suppress an earlier error/status write or custom effect. Do not let a NoMatch in a later AnyOf bypass an earlier Indeterminate. No eager predicate invocation is used as a speculative probe during decisions.
- Keep declaration positions, duplicates, original bags, authored policies, public serialization, combining algorithms and response staging unchanged. Remove only a child proved NoMatch; never infer a Permit/Deny from a candidate match. Keep the current implementation-class allow-list and the `on-permit-apply-second` exclusion (`PolicySet.java:216–233`). Only-one-applicable still counts all retained applicable children. For the initial extension, active decision observation and runtime validation use the ordinary path; do not invent observation events for skipped evaluation.
- Bind the plans to the same PDP-owned prepared generation and exact pinned children as D24/D25, including deferred reference positions and miss/failure fallbacks. `PreparedPolicySets.java:53–75`, `Policy.java:203`, and `PolicySet.java:228` are the integration sites. Publish immutable plans through the existing winner/snapshot path; all mutable selection BitSets are decision-local. No filter state belongs on shared policy objects, and no independent invalidation listener is introduced.

**Rank 1: ordered-range candidates, with numeric support first and temporal support behind an explicit comparison contract**

New design decision required: yes. It extends the currently restricted equality language and its type/provenance proof. No new public builder flag or public comparison bridge is proposed; the existing target-index opt-in governs it.

At preparation/load, collect literal-first lower (`L <= x`) and upper (`U > x`) constraints for each eligible AllOf alternative, keyed by the complete designator identity. Build sorted endpoint arrays plus original alternative/child positions. Use JDK sorting, binary search and BitSet; do not build a custom interval-tree library. Keep the plan immutable in the prepared generation. The current engine prepares some indexes on first demand; whether preparation occurs during load or that existing first-demand step, it must happen once per generation, never per decision.

For a known nonempty totally ordered bag B, the two independent Match predicates reduce to:

```text
lower Match is true iff max(B) >= L
upper Match is true iff min(B) < U
AllOf is possible iff both predicates are true
```

Find endpoints failing each inequality and clear their alternative positions, then map surviving alternatives to children. Lookup each distinct designator once per candidate query and compute min/max once; otherwise the current per-rule Lookup cost survives the new index. Sorted arrays with position postings give O(log N + rejected postings) work and linear stored postings. Start there; cumulative masks for every endpoint have quadratic storage in the number of alternatives and are not needed for the initial 100-rule design.

This is deliberately two bound tests, not ordinary point stabbing. For `[10,20)` and bag `{5,25}`, each Match has a different successful witness; the AllOf matches even though neither value is inside the interval. Do not drop reversed or empty-looking bounds during preparation either: independent witnesses can satisfy them. Duplicate bag values do not affect the existence tests, but the original bag must remain untouched. For singleton bags, this naturally becomes ordinary interval lookup. Keep every overlapping interval's position.

Type rules:

- Integer: use the engine's existing integer representation/comparison; compare endpoints without adding/subtracting one or overflowing at domain limits.
- Double: preserve primitive comparison semantics. Canonicalize signed zero only in private ordering keys, never in the bag; `Double.compare` by itself distinguishes zeros unlike `<=`/`>`. Initially leave any NaN-bearing bag or NaN endpoint on fallback. Support infinities only with the same strict/inclusive endpoint rules. This avoids mistaking a Java total ordering or DoubleVal equality for IEEE comparison.
- Date/dateTime: use the exact engine comparison order, including offsets and its existing implicit-UTC handling, not lexical strings, local dates alone, or epoch milliseconds. `ISO8601Date.java:157`, `ISO8601DateTime.java:166` are the comparison sites. Nanoseconds and explicit offsets must remain significant. If a provider's order depends on a live context/timezone that cannot be proven equivalent, do not compile it.
- Time: use the engine's reference-day order (`ISO8601Time.java:154–167`), without modulo-day wrapping. `time-in-range` is a different function and is excluded. Mixed timezone-present/timezone-absent comparisons must stay on fallback; the standard expressly disallows that mixture for these ordering functions. Source: [XACML 3.0 Annex A.3.8](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html). This design does not normalize or repair existing temporal edge behavior.

Provenance can reuse the existing scan contract instead of exposing another public bridge. `XacmlFuncBagScan.java:34–49` declares side-effect-free, total comparison semantics; `DefaultFunctionInvocation.java:149–170` enables it only for directly bound compatible methods. Numeric plans can certify the operator once using the engine FunctionSpec class plus lower/equal/higher scan probes, extending the established equality-probe approach in `TargetIndex.java:165`. The probes verify the actual scan behavior; an ID match alone is not enough.

Temporal comparisons currently return Scan.NONE. Extend the internal scan implementation and annotate only the audited engine temporal comparison methods, with unsupported/ambiguous inputs returning NONE and preserving ordinary evaluation. Reuse the exact typed comparator; do not add a new public value accessor or change serialized values. This also makes temporal AI prechecks possible, so measure and report that combined effect explicitly. If that total/pure scan contract cannot be established for a temporal case, keep it unindexed; do not guess provenance from its function name.

Expected effects, **unmeasured planning ranges**, for this late-match 100-rule workload:

| Workload | Present GC-only index-on | Expected resulting budget | Expected saving |
|---|---|---|---|
| integer-ranges | 5,626 B/op; 12.12 µs/op | roughly 1,000–1,800 B/op; 2–5 µs/op | about 3,800–4,600 B/op; 7–10 µs/op |
| date-ranges | 6,394 B/op; 12.98 µs/op | roughly 1,000–2,000 B/op; 2–5 µs/op | about 4,400–5,400 B/op; 8–11 µs/op |

These budgets come from removing almost all repeated key lookups and 98 of 100 date rule visits while retaining two candidate positions, ordinary matched-target evaluation, request setup and response assembly. They are not measured forecasts. Against the alternative 3,898 B/op date/profile fork, the same proposed budget implies only about 1,900–2,900 B/op saving. No measured µs/op or B/op projection is available for double/dateTime/time or large overlapping bags. Wide bag extrema can legitimately retain most children, reducing the benefit.

Proposed files: `core/policy/TargetIndex.java` with small package-private range-plan helpers; `core/policy/Policy.java` and `PolicySet.java` only as needed to attach the plan to existing prepared children; `core/policy/function/impl/DefaultFunctionInvocation.java` and `NonNumericComparisonFunctions.java` for temporal scan support. Reuse the existing generation and AI path rather than adding a registry. Extend `xacml-pdp/src/test/java/org/xacml4j/v30/pdp/TargetIndexPdpTest.java` for behavior. Add benchmark cases in `jmh/corpus/RulesCorpus.java` only in an authorized implementation task. Terra's current TargetIndex work must be integrated first.

XACML risk: moderate for integer; higher for double and temporal ordering. The major failure modes are requiring a shared bag witness, excluding an Indeterminate child, wrong endpoint strictness/zero handling, lost timezone precision, and mismatched function provenance.

**Rank 2: multi-key AllOf candidates, preserving alternative identity**

New design decision required: yes. No new public API is needed. Implement after the range plan and a corpus with several target designators, not as an assumed improvement to the present multi-valued case.

Compile eligible AllOf alternatives of the first AnyOf into conjunctions over complete designator keys. Begin with one string-equality Match per distinct key, using existing equality provenance. Keep unsupported/repeated-key forms on fallback initially; do not silently reinterpret two equality Matches on the same bag as a single membership test. Reuse the range plan for a supported pair of bounds on one key when combining the two extensions.

Give each AllOf alternative a stable ID. For each key, retain literal-to-alternative postings. Within one Match, OR the postings found for all request bag values. Across distinct Matches of the same alternative, AND the possible-alternative masks. Across AllOf alternatives of one AnyOf, OR their survivors. Only then project survivors to child positions. Missing key requirements are unconstrained for alternatives that do not mention that key; absent/unknown/error inputs retain the affected child as described above.

Do not intersect child-level masks before keeping alternative identity. A child `(role=A AND dept=X) OR (role=B AND dept=Y)` must not be treated as `(role in {A,B}) AND (dept in {X,Y})` and then assumed to match. That relaxation can be used only as a conservative prefilter, followed by ordinary evaluation; the proposed alternative IDs avoid that unnecessary false positive. Do not enumerate the Cartesian product of bag values or manufacture correlation between separate designator bags.

Use immutable Guava maps/lists for postings and JDK BitSet for decision-local selection. Compile bounded plans; a large unsupported conjunction remains on the existing evaluator. Resolve each request-only key once within this query, without adding a cross-decision cache or PIP prefetch. Candidate order remains authored order.

Expected effect on the current `multi-valued` request: approximately **0 additional B/op and 0 µs/op saved**, because every role policy has only one target key; the group checks are conditions (`RulesCorpus.java:273–286`). It should continue using the existing single-key plan. The measured remaining 1,948 B/op of argument-list/variadic allocation is not recoverable by this target-only filter on this corpus.

For a new multi-key workload, gain is **UNMEASURED**: it depends on how many equality candidates a second request key rejects. Useful headroom is the retained-child target/context/condition work, minus one request-only lookup per distinct key and mask combination. Do not multiply the current whole-response B/op by a guessed selectivity or publish an unsupported numeric speedup. Measure highly selective and nonselective second keys, alternative branches, and multivalued bags before assigning a µs/op/B/op target.

Proposed files: `core/policy/TargetIndex.java` with a package-private conjunction plan; prepared-policy plumbing only if the common range-plan integration requires it; `TargetIndexPdpTest.java` for response/ordering behavior; `jmh/corpus/RulesCorpus.java` and its existing corpus tests for independently expected multi-key decisions. No bag, Entity, PIP, resolver-cache or serialized policy changes are needed.

XACML risk: moderate–high. Avoid excluding on a missing second key, conflating AnyOf alternatives, sharing a witness across different Matches, or bypassing a status-producing earlier Match. The closed, first-AnyOf grammar and fallback rules make the risk reviewable.

**Composition, collection choice and verification**

Equality, range and multi-key constraints should be compiled into one prepared selection plan. AND independent necessary constraints, OR alternatives, keep unknown/deferred positions, and materialize one ordered candidate list. Do not layer three separately allocated filtered lists or independently mutate a shared mask. The existing AI precheck then runs normally on retained children; a filter candidate is not proof of Match, so it must not suppress target validation or condition evaluation. Keep retained-child AI behavior unchanged initially; extra proof reuse can be considered separately if profiling justifies it.

Guava RangeMap is not a drop-in overlapping-policy index. It stores disjoint ranges, and `put` replaces mappings in overlapping portions. TreeRangeMap would lose earlier policy memberships if each authored interval were inserted directly. One can build disjoint segments mapped to immutable sets of positions, but that adds endpoint partitioning, overlap expansion and the same independent-witness problem for bags. Sorted endpoint arrays with position postings fit this restricted grammar more directly. Do not mutate TreeRangeMap per decision. Source checked through Context7 on 2026-10-28: [Guava RangeMap documentation](https://github.com/google/guava/wiki/NewCollectionTypesExplained#rangemap).

Use Optional.empty() for unsupported preparation, explicit unknown/fallback outcomes internally, and the original immutable child list when no candidate restriction applies. Add no null-return API, public builder knob, interval-tree dependency, or general query-planning framework.

Verification for an authorized implementation should extend the existing differential response tests (`TargetIndexPdpTest.java:158`, `:173`, `:232`, `:258`, `:273`) rather than add mechanism tests. Pin decisions, complete status code/message/error chains, evaluated policy IDs, ordered obligations/advice and response attributes for:

- Exact lower/upper endpoints, overlapping ranges, singleton and duplicate bags, `{5,25}` against `[10,20)`, and supported integer/double/temporal boundary cases.
- Several distinct designators, OR alternatives, missing keys with both MustBePresent settings, PIP-supplied optional attributes and PIP failures, type/validation failures, and fallback custom predicates.
- First-applicable and override algorithms, only-one-applicable, nested/reference targets and repository generation changes; preserve the positional on-permit-apply-second fallback.

No allocation-count, logging, UID, stack-switch, call-counter or known-defect tests are proposed. Add a genuine multi-key benchmark and double/dateTime/time cases before claiming those gains. Run module, reactor and conformance gates and independent-family review for implementation; measure with the optimization batch. This research ran no such acceptance gate.

Residual limits: one short fork per combination, concurrent machine activity, byte-weighted sampling, and a material fork-to-fork allocation difference in date/index-on. Existing benchmark fixture checks completed as part of the successful trials (`jmh/harness/PdpFixture.java:447`); they are not a substitute for the future semantic differential tests. Production was left unchanged.

=== END REPORT P-RANGE-astra ===
