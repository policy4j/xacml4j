# First PIP corpus profile and next PIP-path cycles

Lane: PA-pip-astra (Codex Astra). Date: 2026-10-27. Research only; no source changes.

The first profile favors removing resolver-selection scaffolding and unnecessary response materialization before changing single-flight machinery. Four proposed cycles follow below. The hot-cache path still spends substantial allocation on dependencies and cache-key construction. The corpus currently measures its own parallel backend implementation, not D26's production runner; it also does not exercise task 30's reuse of different attributes from one result set.

## Run and evidence

Build passed:

```sh
export PATH="$HOME/Applications/IntelliJ IDEA.app/Contents/plugins/maven-plugin/lib/maven3/bin:$PATH"
JAVA_HOME="$(jenv prefix 25)" rtk mvn -o -q -Dlicense.skipUpdateLicense=true -pl xacml-jmh -am -DskipTests package
```

Environment: IntelliJ Maven 3.9.16; Oracle GraalVM 25.0.4+7-LTS-jvmci-b01; JMH 1.37; async-profiler 4.5; macOS 26.6.1, arm64, 12 logical CPUs. Fork flags: `-XX:-UseJVMCICompiler -Xms2g -Xmx2g -XX:+UseG1GC`. C2, one benchmark thread, one fork, three 500 ms warmups and three 500 ms measurement iterations. Store `in-memory`, corpus `pip:8`, `pipMaxInFlight=32`.

This is a dirty source snapshot based on HEAD `8713323705611502ad0e1576554fdaa4e3fbf374`, including the merged but uncommitted batch changes. The benchmark JAR SHA-256 is `0a1a887f13a13ff752a05ac623f9675f02924c1fa7dd850267b8b872c0ddc62d`. The source manifest records 561 Java/POM files. Their hashes and the JAR hash were unchanged after the runs.

The separately requested FIX-UX-astra helper cleanup started after this hash check. These measurements describe the snapshot before that cleanup.

All raw output is under the worktree root's `.scratch/pa-pip-astra-20261027/`:

| Directory/file | Evidence |
| --- | --- |
| `build-command.txt`, `build.log`, `build-result.txt`, `env.txt` | Build, exit status and tool versions |
| `initial-status.txt`, `source-manifest.json` | Starting state and source/JAR hashes |
| `gc-zero/` | All 16 request/mode/cache combinations at zero latency |
| `gc-latency-200/` | One additional invocation: independent, cache off, serial and parallel, 200 microseconds |
| `alloc-serial-off/`, `alloc-parallel-off/`, `alloc-serial-on/` | Three independent-request allocation profiles |
| `cpu-serial-off/` | Independent-request serial/cache-off CPU profile, 1 ms sampling interval |
| `run-remaining.sh`, `analyze.py`, `profile-summary.json`, `profile-summary.txt`, `results-summary.json` | Reproducible commands, parsing and derived counts |

Each run directory contains its exact `command.txt`, JMH log and result JSON. Profile directories also contain `collapsed-alloc.csv` or `collapsed-cpu.csv` beneath the parameter-named subdirectory. The first sandboxed invocation could not bind JMH's localhost control socket; its failure is retained in `gc-zero/sandbox-failure.txt` and `sandbox-attempt-jmh.log`. The authorized rerun and all later invocations succeeded.

The common benchmark invocation was:

```sh
JAVA_HOME="$(jenv prefix 25)"
rtk proxy "$JAVA_HOME/bin/java" -XX:-UseJVMCICompiler \
  -jar xacml-jmh/target/benchmarks.jar PdpDecideBenchmark \
  -jvmArgs -XX:-UseJVMCICompiler -p store=in-memory -p corpus=pip:8 \
  -p pipMaxInFlight=32 -t 1 -f 1 -wi 3 -w 500ms -i 3 -r 500ms -foe true
```

The zero-latency invocation adds `-p request=independent,dependent,shared,all -p pipMode=serial,parallel -p pipCache=off,on -p pipLatencyMicros=0 -prof gc`. The additional latency invocation selects `independent`, both modes, cache off, and latency 200. Allocation profiling adds `-prof 'async:libPath=/opt/homebrew/lib/libasyncProfiler.dylib;event=alloc;output=collapsed;dir=<run-dir>'`; CPU uses `event=cpu;interval=1000000`. Each command writes JSON and a JMH log to its run directory.

Validation: 22 result records (18 GC configurations and four profiles), each with one fork and three finite measurement scores. The harness's setup and teardown checks passed; no tests were run by the `-DskipTests` build. These are short exploratory runs, not a baseline/candidate pair. Other lanes were allowed to build. Timing, CPU shares and proposed savings must be confirmed in controlled measurements.

## GC matrix

Time is microseconds per decision, with JMH's reported error. Allocation is `gc.alloc.rate.norm`, bytes per decision. Full iteration values and errors are in the JSON.

| Request | Mode | Cache | Time, us/op | B/op |
| --- | --- | --- | ---: | ---: |
| independent | serial | off | 3.495 +/- 0.689 | 10,650 |
| independent | parallel | off | 62.656 +/- 63.224 | 13,594 |
| independent | serial | on | 2.262 +/- 8.746 | 7,521 |
| independent | parallel | on | 2.580 +/- 9.922 | 7,649 |
| dependent | serial | off | 3.197 +/- 15.807 | 10,826 |
| dependent | parallel | off | 2.842 +/- 6.607 | 10,962 |
| dependent | serial | on | 2.370 +/- 3.184 | 8,993 |
| dependent | parallel | on | 2.287 +/- 1.363 | 8,969 |
| shared | serial | off | 2.583 +/- 8.553 | 9,089 |
| shared | parallel | off | 2.743 +/- 7.996 | 9,297 |
| shared | serial | on | 1.924 +/- 4.036 | 7,073 |
| shared | parallel | on | 1.815 +/- 1.364 | 6,977 |
| all | serial | off | 9.896 +/- 30.758 | 79,287 |
| all | parallel | off | 32.470 +/- 136.900 | 79,944 |
| all | serial | on | 11.173 +/- 62.186 | 78,149 |
| all | parallel | on | 10.224 +/- 56.540 | 78,058 |

At latency 200, independent/cache-off measured:

| Mode | Time, us/op | B/op |
| --- | ---: | ---: |
| serial | 2,208.964 +/- 857.776 | 14,600 |
| parallel | 331.551 +/- 114.212 | 17,691 |

Interpretation: parallel dispatch has a large cost when the simulated reads do no blocking work, while overlapping eight delayed reads helps in the 200-microsecond case. This does not establish a production crossover threshold. The delay is `Thread.sleep`, with OS scheduling granularity, and the short trials reach different compilation states at different throughputs (`PipWorkload.java:218`). Do not treat the latency-dependent allocation differences as steady-state regressions.

`all` rotates through nine cases, not just independent/dependent/shared (`PipCorpus.java:87`). Its much larger allocation is a separate workload result. It includes fresh XML entity/content construction (`PipWorkload.java:146`) and selector evaluation; no allocation profile of `all` was requested or taken, so its allocation cannot be assigned quantitatively to XML or any other site from these profiles.

## Allocation and CPU attribution

Source abbreviations below:

- **P** = `xacml-pdp/src/main/java/org/xacml4j/v30/`
- **C** = `xacml-core/src/main/java/org/xacml4j/v30/`
- **J** = `xacml-jmh/src/main/java/org/xacml4j/jmh/`

Percentages use the sum of collapsed counts for the entire fork as denominator. Allocation denominators: serial/off **9,807 samples**, parallel/off **790**, serial/on **9,781**. CPU denominator: **1,548 samples**. The parallel allocation profile is substantially coarser.

These are allocation **sample shares**, not exact allocated-byte shares. async-profiler 4.5 defaults to `COUNTER_SAMPLES`; its collapsed writer selects sample counts unless the total counter is requested. JMH 1.37 dumps `collapsed` without selecting `total`. See the primary [arguments definition](https://github.com/async-profiler/async-profiler/blob/v4.5/src/arguments.h), [collapsed writer](https://github.com/async-profiler/async-profiler/blob/v4.5/src/profiler.cpp), and [JMH adapter](https://github.com/openjdk/jmh/blob/1.37/jmh-core/src/main/java/org/openjdk/jmh/profile/AsyncProfiler.java), retrieved 2026-10-27. Context7 did not return an async-profiler library; these official sources were fetched through Tavily.

For the main allocation table, an allocation belongs to the nearest xacml4j method on its stack, before the allocated-class leaf. This includes JDK/Guava allocations below that method but avoids assigning a child's allocations again to its parent. Explicit subset and inclusive rows below must not be added to these rows.

| Allocation site/group | Serial off | Parallel off | Serial on | Source |
| --- | ---: | ---: | ---: | --- |
| Fixture parallel dispatch, including virtual-thread submission | 0 sampled | 24.18% | 0 sampled | J/corpus/PipWorkload.java:189,197 |
| Compatibility multiset from `BagOfValues.values()` | 13.28% | 10.51% | 0 sampled | C/BagOfValues.java:88; caller P/pdp/DefaultPolicyDecisionPoint.java:257 |
| Resolver candidate construction | 8.36% | 6.33% | 10.65% | P/spi/pip/DefaultResolverRegistry.java:80 |
| Handler-owned allocations, all methods | 7.32% | 6.33% | 10.85% | P/pdp/DefaultEvaluationContextHandler.java:91,143,301,330,345 |
| Resolved-attribute assembly, excluding deeper xacml4j methods | 6.74% | 5.70% | 9.68% | P/pdp/DefaultPolicyDecisionPoint.java:243 |
| `AttributeSet` and its builder/accessors | 5.40% | 3.29% | 0.40% | P/spi/pip/AttributeSet.java:53,120,189 |
| `ResolverContext` and dependency preparation | 2.91% | 3.04% | 6.20% | P/spi/pip/ResolverContext.java:102,149,181 |
| `ResolverCacheKey` and its builder | 0 sampled | 0 sampled | 4.56% | P/spi/pip/ResolverCacheKey.java:53,105,129 |
| Fixture cache lookup, excluding deeper key/context methods | 0 sampled | 0 sampled | 1.17% | J/corpus/PipWorkload.java:255 |
| D26 `StructuredResolverRunner` | **Not exercised** | **Not exercised** | **Not exercised** | P/spi/pip/StructuredResolverRunner.java:73 |

The serial/off response assembly subtree is **34.54%** of allocation samples; parallel/off **25.44%**, serial/on **27.06%**. Those inclusive figures contain bag compatibility views, attributes, entities and indexes. They are not extra savings on top of the table.

Handler breakdown, all subsets of the handler row:

| Site | Serial off | Parallel off | Serial on |
| --- | ---: | ---: | ---: |
| `resolveOnce` direct allocations: frame, future, flight-map nodes/table | 2.04% | 1.27% | 3.43% |
| Lazy flight-map object, `flights()` | 0.64% | 0.89% | 0.93% |
| `CompletableFuture` object alone | 0.33% | 0.25% | 0.65% |
| Ancestry `Frame` object alone | 0.43% | 0.13% | 0.73% |
| `ScopedValue.Carrier` and `Snapshot` objects | 1.14% | 1.01% | 1.42% |

Locations: `DefaultEvaluationContextHandler.java:146,308,309,310,336`. The much larger inclusive `ScopedValue.Carrier.call` allocation share (35.61% serial/off) is principally the resolver work performed inside the scope. It is not evidence that scope machinery allocates that share.

The parallel profile contains **14.43%** virtual-thread/continuation objects and **2.66%** `ThreadBoundFuture` objects, both subsets of fixture dispatch. These come from `PipWorkload.readParallel`, not the production runner. The production runner's futures, cancellation and prepared-context costs remain unmeasured.

On a hot cache, `ResolverContext.getCacheKey` accounts for **15.49% inclusive** allocation, including dependency resolution. `CacheProvider.getAttributes` accounts for **16.65% inclusive**; Guava `getIfPresent` itself has only **0.70% inclusive** allocation. These figures overlap. The direct key-builder group includes linked-list object/nodes (**1.56%**), builder (**0.58%**), values iterator (**0.70%**), final bag array (**0.95%**) and key object (**0.77%**). The no-cache profiles sample no key construction. A zero sampled context object on cache-on does not mean the context API was bypassed; its preparation work is visible and allocation elimination may remove the wrapper.

CPU, serial/cache-off/independent only, ranked by inclusive frame share among the smaller useful subtrees:

| Frame | CPU samples | Source |
| --- | ---: | --- |
| `BagOfValues.values` | 13.89% | C/BagOfValues.java:88 |
| `Value.OtherValue.hashCode` | 10.40% | C/types/Value.java:128 |
| `BagStorages.Builder.build` | 10.34% | C/BagStorages.java:75 |
| `DefaultResolverRegistry.getMatchingAttributeResolver` | 6.91% | P/spi/pip/DefaultResolverRegistry.java:80 |
| `ResolverContext.Default.resolveAllContextRefs` | 3.94% | P/spi/pip/ResolverContext.java:181 |
| `AttributeSet.<init>` | 3.23% | P/spi/pip/AttributeSet.java:53 |
| Handler `publish` | 1.23% | P/pdp/DefaultEvaluationContextHandler.java:345 |

Broader inclusive CPU frames: handler `resolve` **38.89%**, `resolveOnce` **38.57%**, PIP `resolve` **35.01%**, and resolved-response assembly **31.52%**. Their descendants overlap. Assigning CPU samples to the nearest xacml4j frame gives **4.33%** to the handler itself. ScopedValue leaf frames account for **0.45%**; its bound-call subtree accounts for **35.53%**. The cache-key/cache-provider CPU path was not exercised with caching enabled, and D26's runner was not exercised. No cache-library CPU ranking follows from this CPU profile.

## Four proposed cycles, in priority order

Shares below are observed target envelopes and expected opportunities, **not measured savings**. No changes to public API are needed for these proposals.

1. **Build resolver candidates without a stream pipeline.** Replace the Optional/stream/filter/collector chain with a small ordinary loop or a copy for the no-issuer case in `P/spi/pip/DefaultResolverRegistry.java:80`. Keep a detached result collection and registration order; the registry has public `addResolver` methods, so returning a live multimap view or caching candidates indefinitely would change its contract. Stream, spliterator, lambda and Optional leaves together represent **6.10% serial/off and 7.68% serial/on** allocation samples; the complete local site is 8.36%/10.65%. CPU envelope: **6.91%**. Risk: low to medium, chiefly issuer filtering, duplicate handling, result-collection ownership and fallback order. Keep the common semantic contract below; resolver selection must not invoke a resolver or eagerly resolve a dependency.

2. **Pass the bag directly into resolved-attribute assembly.** At `P/pdp/DefaultPolicyDecisionPoint.java:257`, the existing fluent API permits `.values(v)` instead of `.values(v.values())`: `BagOfValues` is already `Iterable<Value<?>>` (`C/BagOfValues.java:63`), and `Attribute.Builder.values` consumes that interface (`C/Attribute.java:191`). This avoids creating the compatibility multiset solely to iterate it. Expected target: **13.28% serial/off and 10.51% parallel/off** allocation samples, with **13.89% serial/off CPU** under materialization. Hot-cache sampled benefit is zero because cached bags already carry the compatibility view. Risk: low, with explicit checks for duplicates, grouped iteration order and floating-point values. Preserve the public `values()` method, serialized representation and all resolved-response content. The existing bag/response compatibility tests and corpus oracle should cover this; add only a missing response/order case if necessary.

3. **Reduce dependency-map and resolver-key staging.** Specialize the common single-dependency map using standard immutable-map factories in `P/spi/pip/ResolverContext.java:181`; construct the key from the already resolved ordered values without LinkedList staging (`P/spi/pip/ResolverCacheKey.java:53,108,129`). Retain the public builder and serialized bag-array fields. The context/key local groups total **10.76% on hot cache**; the key group's **4.56%** includes necessary final objects as well as removable staging. The cache-key subtree's **15.49%** is an upper envelope that includes dependencies, not an expected saving. Without caching, context preparation is **2.91%** locally, **5.89%** including dependency work. Risk: medium: preserve dependency order, lazy evaluation, retries when all dependencies were absent, content-based hash/equality, and old-stream behavior. Do not retain a live ResolverContext across attempts or decisions. Measure this together with D35 and task 30 to avoid optimizing a staging path they later replace.

4. **Create AttributeSet failure storage only when a function fails.** `P/spi/pip/AttributeSet.java:195,196` creates two immutable-map builders even for a successful synchronous resolver. Retain an empty immutable failure map until the first failure; use the existing builder when needed. The builder-constructor envelope is **2.15% serial/off and 1.01% parallel/off**, only part of which belongs to failure storage; the whole AttributeSet group (5.40%/3.29%) is not removable. Hot-cache builder benefit is unobserved. Risk: low if failure order, per-attribute failure retention, `isComplete()` and `rethrowFailure` remain unchanged. Keep the public timestamp and clock reads: the 3.23% CPU under construction is not permission to remove observable creation time or change TTL semantics. Do not cache a set containing a failed function.

I would defer a new flight/scope optimization cycle. The objects needed for flights and ancestry are a smaller target, and this single-threaded workload does not test overlapping callers of one handler. Removing the future, replacing concurrent maps with ordinary maps, or bypassing ancestry on the supposedly common path would undermine D27. The scope subtree percentages must not be used to justify that change.

## Semantic contract for every cycle

[OASIS XACML 3.0, 22 January 2013](https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html), fetched 2026-10-27:

- §7.3.5: subsequent tests use the initially tested immutable bag.
- §7.3.5 and §7.19.3: absence remains distinct from retrieval failure; MustBePresent controls empty-bag versus missing-attribute Indeterminate behavior.
- §7.18 and Annex C: keep evaluation order and short-circuits; publish obligations/advice only through matching evaluated paths.

Additional engine contracts are pinned by D26/D27 and their existing tests:

- Preserve request/PIP precedence and first published positive-bag identity. D27's overlapping top-level callers share an active outcome; nested same-handler resolutions do not wait on another flight. Ancestry includes handler identity. Remove flights on every exit, including Error; later absence/failure retries remain possible. See `P/pdp/DefaultEvaluationContextHandler.java:301-373` and design D27.
- Keep resolver registration/fallback order. A present empty bag ends fallback; an omitted member does not. If no candidate succeeds, retain the first mapped failure and the existing "Attribute resolution failed" processing-error mapping (`P/spi/pip/DefaultPolicyInformationPoint.java:75-150`).
- D26 isolates each attribute function's failure. A successful demanded attribute survives an unrelated function's failure; the failed member fails only when demanded. Workers do not publish response state. Keep timeout cancellation and permit retention until the worker actually ends (`P/spi/pip/AttributeResolverDescriptor.java:172-203`, `StructuredResolverRunner.java:73-114`, `AttributeSet.java:134-156`).

Use existing decision/status/order/serialization/API tests and the corpus oracle. Add only uncovered behavior tests. A controlled before/after JMH pair and independent review are still required before accepting any cycle.

## What D35 should take from this

1. Optimize the work before the cache as well as the cache implementation. In the hot-cache allocation profile, the provider subtree is 16.65%, of which key preparation accounts for 15.49%; Guava lookup is 0.70%. Replacing Guava alone cannot remove dependency maps and key builders. This is allocation evidence, not a CPU comparison of libraries.

2. The fixture has one shared attribute cache and one content cache, maximum size 1,024, fixed one-hour expiry (`J/corpus/PipWorkload.java:237-262`). It does not instantiate the proposed per-resolver caches, test descriptor-specific expiry, or coalesce cross-decision cold misses through a loader. Those D35 behaviors remain **UNVERIFIED** here. Its setup only verifies a warmed independent result while the backend is disabled (`PipWorkload.java:109`).

3. Keep complete positive result sets and descriptor-specific TTL/limits in the per-resolver cache. Missing members must still fall through, present empty bags remain values, and transient failures/timeouts must not become cached absence. A cache expiry must not replace a bag already published in the current decision. Keep the existing public ResolverCacheKey equality/serialization contract even if a private per-resolver cache can avoid repeating the resolver id internally.

4. Compare Guava/Caffeine/cache2k on warm hits, unique-key cold misses, expiry and concurrent same-key misses, with delayed remote reads. Use loading coalescence across decisions while keeping D27's within-handler contract. The 200-microsecond result supports measuring remote latency separately from zero-work dispatch; it does not select a library or a default concurrency threshold.

## What task 30 should take from this

The current custom corpus resolvers each return one attribute. `independent` performs eight backend reads but returns **one bag under one attribute id** (`J/corpus/PipWorkload.java:125-133`). `shared` references the same key three times (`J/corpus/PipCorpus.java:144-146`), which the handler's positive-bag cache already serves. `dependent` uses two distinct resolvers. Therefore these cases do not measure the intended saving from demanding a second member of the same AttributeSet. The default environment descriptor has multiple attributes, but these policy cases do not demand them.

Before claiming task-30 savings, add a corpus case with at least two distinct attributes from one resolver, both demanded in order; another case must leave the sibling unused. Keep full-response oracles for missing-member fallback, present empty bags, expiry and an unrelated failed function. Measure cache off as well as on. The session must reuse only an eligible present member when demanded, publish only that member, and preserve dependency timing, retry behavior and the active-flight rules.

Create the approved `forDecision()` session once per decision, with lazy memo storage, while retaining the custom-PIP default path. Do not prepublish the entire result set or introduce an eager dependency lookup just to build a memo key. Coordinate the context/key cycle above with that implementation. Its potential benefit is currently **unmeasured**; applying the profile's full PIP subtree percentage would overstate it.

Also extend the benchmark configuration to use descriptors built with `buildParallel()` when measuring D26. Today `pipMode=parallel` calls the fixture-owned executor's `invokeAll` (`PipWorkload.java:197`); every custom descriptor is built with the synchronous `build(context -> ...)` form. A D26 case should preserve the same requested results while covering prepared inputs, per-attribute failure isolation and timeout/fallback behavior. With `-t 1`, neither this matrix nor the shared-key case establishes concurrent D27 behavior. Increasing JMH threads creates concurrent decisions, which is useful for D35, but does not itself make callers share one handler.

## Verification and limits

Build passed; all 22 requested configurations/profiles completed; all JMH result records contained one fork and three finite measurement scores; setup/teardown response oracles passed. Source and JAR hashes were unchanged. Raw outputs remain in scratch, and this analysis is the only added OpenSpec file.

No source edits, new tests, task updates, dependency changes, commits or performance acceptance claim. The proposed cycles, production D26 runner costs, task-30 multi-attribute reuse, D35 TTL/loading behavior and cache-library CPU comparisons remain unmeasured or unverified as described above.
