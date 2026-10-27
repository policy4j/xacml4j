<!-- Lane PF3 of the first profiling swarm (task 2.1): Codex GPT-6.1 Sol, read-only; report taken from the Codex session record after a follow-up (its first turn ended with a progress note), paths made repository-relative. -->

FINAL REPORT (PF3 JMH benchmark engineer, Codex GPT-6.1 Sol)

The strongest hypotheses are to reuse request-derived bags, remove attribute-lookup pipelines, and replace small-bag multiset machinery. Rewriting combining algorithms has a much smaller direct CPU budget.

No files were changed. No Maven, tests, benchmarks, profilers, Git writes, or beads writes were run.

Evidence paths and estimate method:

- `P` = `openspec/changes/optimize-pdp-evaluation/evidence/benchmarks/jmh/004-profile-decide-jdk25-logback`
- `B` = `openspec/changes/add-pdp-jmh-benchmarks/evidence/benchmarks/jmh/002-baseline-jdk25`
- `C` = `xacml-core/src/main/java/org/xacml4j/v30`
- `D` = `xacml-pdp/src/main/java/org/xacml4j/v30`
- `T` = `xacml-core/src/test/java/org/xacml4j/v30`
- Workload order below: **R** = rsa2008/all; **S** = synthetic:1000/all; **F** = synthetic:1000/first-deny.

Recorded environment: Oracle GraalVM 25.0.4+7-LTS-jvmci-b01, C2, Apple M2 Pro, G1, `-Xms2g -Xmx2g`, one thread. Profiles used one fork, five 1-second warmups and five 2-second measurements; CPU interval 1 ms. Evidence: `P/env.txt:123`, `P/env.txt:126`, `P/env.txt:138`, `P/command.txt:2`.

| Workload | Recorded CPU-profile time, µs/decide | CPU samples | Allocation reference, B/decide |
| --- | ---: | ---: | ---: |
| R | 14.200 ± 0.158 | 10,106 | 45,115 |
| S | 359.220 ± 39.660 | 10,091 | 1,675,683 |
| F | 2.688 ± 0.277 | 10,099 | 15,816 |

CPU times come from each `P/cpu-*/results.json`. Allocation references come from `B/results-default.json` and `B/results-scaling.json`; S is the arithmetic mean of its seven request cases, rather than a separately measured all-request result. The reference build used Log4j; P used Logback (`B/summary.md:6`, `P/summary.md:3`).

**All gains below are UNVERIFIED planning estimates.** Time estimates multiply sampled shares by profiled time, usually assuming half the identified work can be removed. Allocation estimates multiply allocation-sample shares by the older allocation reference. This assumes byte-representative allocation sampling and transfers shares between builds; P contains no `gc.alloc.rate.norm` measurement. These estimates establish priorities, not achieved savings, and must not be added together.

Aggregates below assign each collapsed stack once to its deepest `org/xacml4j/` frame, then sum its weight. This avoids adding overlapping inclusive frames.

**H1. Cache successful request-derived designator bags within each individual decision**

- **Evidence:** Attribute lookup plus bag construction accounts for `(3742 + 1169) / 10091 = 48.67%` of S CPU samples and `(38691 + 22052) / 79945 = 75.98%` of S allocation samples. Representative rows: `P/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md:78` and `:79`; allocation rows `P/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md:77`. Source checks show `DefaultEvaluationContextHandler.resolve` reads `designatorCache`, but successful request lookups return without populating it; successful PIP results populate it (`D/pdp/DefaultEvaluationContextHandler.java:109`, `:148`, `:169`). Synthetic policy targets repeatedly request the same resource attribute (`xacml-jmh/src/main/java/org/xacml4j/jmh/corpus/SyntheticCorpus.java:130`, `:139`).
- **Change:** Add a separate memo for successful request-derived bags in `DefaultEvaluationContextHandler`. Keep its lifetime within the individual evaluation context created at `D/pdp/PolicyDecisionPointContextFactory.java:176`.
- **Expected gain:** For S, removing 25–50% of the identified CPU budget gives `359.220 × 48.67% × 0.25–0.50 = 43.7–87.4 µs/decide`. Removing 50–90% of its allocation budget gives approximately **637,000–1,146,000 reference B/decide**. R and F gains are UNVERIFIED: reuse frequency was not counted, and F could regress from memo overhead.
- **Risk:** Preserve issuer, category, datatype and MustBePresent behavior, XACML 3.0 §7.3.5. Reusing the existing PIP cache would also change resolved-attribute provenance: `DefaultPolicyDecisionPoint` puts that map into `Result.resolvedAttr` (`D/pdp/DefaultPolicyDecisionPoint.java:155`, `:230`). Keep request memo entries separate. No public API change proposed. Use decision-local storage; do not share request bags across requests.
- **Test proof:** Compare complete results, including status, resolved attributes, obligations and advice. Exercise repeated designators, issuer variants, missing attributes, resolver failures, concurrent decisions and multiple-request splitting.
- **Checked / not checked:** Cache read/write paths, request callback capture and result assembly checked. Callback mutation contracts, custom PIP behavior and cache-hit frequency remain UNVERIFIED.

**H2. Fuse attribute filtering and replace lookup streams with loops**

- **Evidence:** Deepest-frame CPU shares for `AttributeIndex.*`, `Attribute.getValuesByType` and `RequestContext.getEntity` are **22.27% / 37.08% / 42.28%** for R/S/F. Examples: `P/cpu-rsa2008-all/top-cpu-rsa2008-all.md:77`, `P/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md:78`, `P/cpu-synthetic_1000-first-deny/top-cpu-synthetic_1000-first-deny.md:79`. Source builds an issuer-filtered collection, a type-filtered collection, then an immutable result (`C/AttributeIndex.java:105`, `:163`; `C/Attribute.java:114`). `RequestContext.getEntity` creates a transformed view and stream to find one entity (`C/RequestContext.java:220`, `:248`).
- **Change:** Loop directly over attributes and their values in `AttributeIndex.findValues`; replace the public filtering implementations and `RequestContext.getEntity` pipeline with equivalent loops. Preserve public signatures and collection behavior.
- **Expected gain:** At 50% removal of the lookup CPU budget: **1.58 / 66.60 / 0.568 µs/decide**. Allocation stacks ending in stream, lambda or spliterator machinery represent 22.68% / 32.25% / 35.04%; removing half gives approximately **5,117 / 270,243 / 2,771 reference B/decide**.
- **Risk:** Preserve null-issuer wildcard behavior, datatype filtering, duplicates and first-entity selection. XACML §7.3.5 governs designator selection. Returning an internal collection where callers currently receive a new collection can change mutation behavior. Keep temporary builders local.
- **Test proof:** Characterize empty results, several issuers, duplicate values, multiple requested types, encounter order and returned-collection mutation; compare full PDP results.
- **Checked / not checked:** Filtering predicates and intermediate collections checked. All downstream mutation assumptions were not inventoried. Estimates overlap H1 substantially.

**H3. Specialize empty and singleton bags; remove multiset iterator machinery**

- **Evidence:** The largest S CPU row is `Match.match → ImmutableList$1.get`, **10.7%**, at `P/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md:77`. Decompressed `cpu-synthetic_1000-all/collapsed-cpu.csv.gz`, line 631, carries 1,076 samples through `ImmutableMultiset$1.next → AbstractIndexedListIterator.next → ImmutableList$1.get`. This is bag iteration. Bag construction adds **10.51% / 11.58% / 7.03%** CPU across R/S/F. Source always builds an `ImmutableMultiset` (`C/BagOfValues.java:72`, `:280`) and Match iterates it (`C/policy/Match.java:120`).
- **Change:** Give `BagOfValues` small immutable representations, initially empty and singleton, with a measured general-bag fallback. Compare JDK/custom representations; Eclipse Collections 13.0.0 is an experiment candidate, not an assumed improvement.
- **Expected gain:** Half of bag-construction CPU plus the identified Match iterator-get samples gives **0.750 / 39.98 / 0.172 µs/decide**. Half the corresponding construction/iterator allocation budget gives approximately **4,307 / 249,576 / 1,160 reference B/decide**.
- **Risk:** Bags preserve multiplicity; sets do not. Preserve bag equality/hash behavior and datatype validation (`C/BagOfValues.java:211`, `:268`), XACML §§A.3.10–A.3.11. Keep Java equality distinct from XACML double equality, §A.3.1. Public signatures can remain unchanged; serialization compatibility needs explicit review.
- **Test proof:** Empty, singleton, duplicate-heavy and larger bags; bag-size, one-and-only, union/intersection, equality/hash and serialization. Include signed zero and NaN cases from `T/policy/function/impl/DoubleBagSetEqualityTest.java:60`.
- **Checked / not checked:** Backing representation, builder and sampled iterator chain checked. General-bag size distribution and serialization consumers remain UNVERIFIED.

**H4. Remove varargs arrays from value and identifier hashing**

- **Evidence:** Combined `Value$OtherValue.hashCode` and `SemanticIdentifier.hashCode` CPU shares are **5.85% / 7.24% / 6.65%**. Their allocation samples are **5.49% / 3.77% / 4.50%**. Examples: `P/alloc-rsa2008-all/top-alloc-rsa2008-all.md:78`, `:81`; `P/alloc-synthetic_1000-first-deny/top-alloc-synthetic_1000-first-deny.md:79`. Both use `Objects.hash(...)` (`C/types/Value.java:128`; `C/SemanticIdentifier.java:102`).
- **Change:** Expand the existing 31-based hash calculation directly, retaining its initial seed and all operands.
- **Expected gain:** Removing half the hashing CPU gives **0.415 / 13.01 / 0.089 µs/decide**. Eliminating the observed varargs-array sites corresponds to approximately **2,476 / 63,238 / 711 reference B/decide**.
- **Risk:** Preserve current hash values and equality behavior. Neither protected value fields nor identifier fields are declared immutable (`C/types/Value.java:108`; `C/SemanticIdentifier.java:53`), so caching their hashes requires a separate mutability contract. Making protected fields final would be **BREAKING**.
- **Test proof:** Compare old and candidate hashes across representative types and subclasses; verify map/multiset lookup and equal-value hash consistency.
- **Checked / not checked:** Hash operands and field declarations checked. No complete subclass mutability inventory was performed.

**H5. Shorten forwarding through internal evaluation contexts**

- **Evidence:** Deepest descendant-context CPU shares are **9.12% / 5.09% / 3.93%**. R includes 129 samples at `isValidateFuncParamsAtRuntime → itable stub` and 118 at `resolve → itable stub` (`P/cpu-rsa2008-all/top-cpu-rsa2008-all.md:83`, `:84`). Forwarders recursively call the parent (`C/policy/DescendantEvaluationContext.java:120`, `:186`, `:223`).
- **Change:** Give internal contexts direct access to shared request services for operations proven to be pure forwarding. Retain parent traversal for policy scope, variables, status and staged responses.
- **Expected gain:** Half the forwarding budget gives **0.648 / 9.15 / 0.053 µs/decide**. Forwarding alone removes no required object allocation. **Net bytes/decide are UNVERIFIED**: additional references could enlarge context objects.
- **Risk:** Context topology carries semantics. Preserve variable scope and obligation/advice staging, XACML §7.18 (`C/policy/BaseCompositeDecisionRule.java:141`, `:202`). Runtime-validation setters must remain effective. Public `EvaluationContext` implementations need the existing dispatch path.
- **Test proof:** Nested PolicySets, variable shadowing, extended Indeterminate, runtime-validation changes, failed obligations and advice propagation; compare complete results under concurrent decisions.
- **Checked / not checked:** Forwarders and staging entry points checked. Every override, receiver distribution and object layout was not checked. Reject a candidate that increases allocation.

**H6. Add a fixed-arity path for already evaluated function arguments**

- **Evidence:** Normalization/evaluation, argument copying and Match list wrapping together account for **7.17% / 4.95% / 2.24%** CPU. Source normalizes into a `LinkedList`, evaluates into an `ArrayList`, then copies into an `Object[]` (`C/policy/function/FunctionSpecBuilder.java:500`, `:601`; `C/policy/function/impl/DefaultFunctionInvocation.java:95`). Match passes two values through the varargs/list adapter (`C/policy/Match.java:121`; `C/policy/FunctionSpec.java:143`). S records 1.8% CPU in `Arrays$ArrayList.<init>` under Match (`P/cpu-synthetic_1000-all/top-cpu-synthetic_1000-all.md:83`).
- **Change:** Compile an internal fixed-arity invocation plan for eligible functions. Avoid normalizing or reevaluating already evaluated values while retaining validators and the generic optional/variadic/lazy path.
- **Expected gain:** Half the identified CPU budget gives **0.509 / 8.90 / 0.030 µs/decide**. Half the corresponding argument-container allocation budget gives approximately **2,414 / 66,665 / 381 reference B/decide**.
- **Risk:** XACML §7.4 requires type-correct evaluation; §A.3.5 requires lazy logical evaluation. Preserve optional defaults, arity errors and runtime validators. Shared function objects must not reuse mutable argument arrays. Adding abstract methods to the public `FunctionSpec` SPI would be **BREAKING**.
- **Test proof:** Fixed, optional, zero-argument and variadic functions; validation on/off; lazy operands with failures; nested higher-order calls; concurrent invocation.
- **Checked / not checked:** Normalization and copy paths checked. Eligible-call frequency and generated invocation code remain UNVERIFIED.

**H7. Prelink built-in invocations and reduce exception-wrapper construction**

- **Evidence:** Deepest `FunctionInvocationFactory$Default.doInvoke` CPU shares are **6.06% / 1.35% / 0.56%**. R includes stack-trace construction: 1.4% `fill_in_stack_trace` and 0.9% `BacktraceBuilder::push` (`P/cpu-rsa2008-all/top-cpu-rsa2008-all.md:81`, `:94`). The factory invokes reflection, catches `Throwable` and creates a `RuntimeException`; the next adapter creates `FunctionInvocationException` (`C/policy/function/FunctionInvocationFactory.java:73`; `C/policy/function/impl/DefaultFunctionInvocation.java:148`).
- **Change:** Compare prelinked typed built-in adapters with the current reflection path. Preserve the generic SPI path and the status-preserving cause handling at `DefaultFunctionInvocation.java:157`.
- **Expected gain:** Half the factory CPU budget gives **0.430 / 2.42 / 0.008 µs/decide**. Half the R factory allocation budget gives approximately **447 reference B/decide**. No S/F wrapper-allocation savings can be estimated from their samples; this is not proof of zero allocation.
- **Risk:** Preserve XACML §§7.4 and 7.19.2 error results and existing externally observable exception contracts. Use immutable invocation plans. Do not suppress required failures or their statuses.
- **Test proof:** Successful calls, parsing failures, empty/multiple-value one-and-only, custom evaluation statuses, nested causes and concurrent invocation. Retain the assertions in `T/policy/function/impl/FunctionInvocationStatusTest.java:56`.
- **Checked / not checked:** Reflection and exception wrapping checked. The whole factory CPU share is not removable reflection overhead; adapter advantage remains UNVERIFIED.

**H8. Allocate policy variable-result maps only when needed**

- **Evidence:** S allocates **1,820 / 79,945 = 2.28%** of samples at `Policy$PolicyDescendantEvaluationContext.<init> → HashMap` (`P/alloc-synthetic_1000-all/top-alloc-synthetic_1000-all.md:89`). These constructor stacks have 37/10,091 CPU samples. Every policy context eagerly creates a map (`C/policy/Policy.java:250`), including generated policies without variable definitions (`SyntheticCorpus.java:124`).
- **Change:** Initialize the private variable-result map on the first valid write; an uninitialized read returns the current absent result.
- **Expected gain:** Half the sampled S constructor CPU is `359.220 × 37/10091 × 0.5 = 0.659 µs/decide`. Removing its map allocation corresponds to approximately **38,148 reference B/decide** for S; R/F allocation budgets are approximately **129/62 B/decide**. No reliable R/F time estimate.
- **Risk:** Preserve policy-local variable scope, missing-variable behavior and extended evaluation contexts. Keep initialization local to each context; do not replace it with a shared mutable map. No public API change.
- **Test proof:** Policies with no variables, repeated variable references, nested scopes and extended Indeterminate; verify independent decisions cannot share results.
- **Checked / not checked:** Constructor, getter and setter checked (`C/policy/Policy.java:271`, `:281`). Concurrent access to one context was not established.

**H9. Remove higher-order callback and validation scaffolding selectively**

- **Evidence:** Higher-order deepest frames account for **148/10106 = 1.46%** of R CPU and **819/53229 = 1.54%** of R allocation samples. `validateArguments` alone has 96 CPU samples. Source creates argument/primitives lists, `PrimitiveTypeExpression` objects and capturing callbacks (`C/policy/function/impl/HigherOrderFunctions.java:68`, `:101`, `:218`, `:230`, `:252`).
- **Change:** Compile reusable signature metadata for eligible cases and implement measured callback paths as explicit loops. Retain dynamic checks and generic handling.
- **Expected gain:** At half the identified R budget: **0.104 µs and 347 reference B/decide**. S/F profiles provide no sampled higher-order budget; no gain is estimated for them.
- **Risk:** XACML §A.3.12 combines predicate outcomes using logical-function semantics. A later deciding result may override an earlier failure. Preserve `DecidingOperands.firstDeciding` behavior (`C/policy/function/impl/DecidingOperands.java:74`). The retained normative excerpt is `openspec/changes/modernize-core-for-jdk28-valhalla/evidence/tests/074-deciding-operand/oasis-xacml30-errata01-A.3.5-A.3.12.txt:65`.
- **Test proof:** Empty bags, argument positions, tuple combinations, error-before-true for any-of, error-before-false for all-of, and failure without a deciding result. Preserve first-failure status.
- **Checked / not checked:** Callbacks, validation construction and deciding-operand loop checked. Callback allocation after JIT optimization and receiver polymorphism remain UNVERIFIED.

Refuted or deprioritized ideas:

- **Rewrite deny-overrides first:** Combining kernels themselves account for only **1.01% / 0.55% / 1.00%** CPU. Inclusive combining time includes child evaluation. Deny already short-circuits (`C/policy/combine/impl/DenyOverrides.java:73`). A Permit cannot terminate a deny-overrides scan; preserve XACML §C.2 and the decision tables at `T/policy/combine/impl/OverrideDecisionTableTest.java:47`.
- **Replace all target-child lists to fix the 10.7% frame:** The dominant stack is the bag iterator beneath Match, as shown by decompressed S CPU line 631. Target/AnyOf/AllOf already use loops (`C/policy/Target.java:72`; `MatchAnyOf.java:61`; `MatchAllOf.java:69`).
- **Replace predicate calls with `Value.equals`:** `OtherValue.equals` has only **0.64% / 0.43% / 0.56%** deepest CPU share. Java object equality cannot generally substitute for XACML equality; signed zero and NaN tests demonstrate the distinction (`T/policy/function/impl/DoubleBagSetEqualityTest.java:73`).
- **Treat every `itable stub` sample as function megamorphism:** R has 8.8% self samples there, but attribution includes attribute filters and context forwarders (`P/cpu-rsa2008-all/top-cpu-rsa2008-all.md:7`, `:82`). Collapsed stacks do not establish receiver-type distributions or failed inlining.
- **Optimize all 78 stream sites equally:** The supplied inventory is not a CPU ranking. This lane confirmed sampled lookup sites. Parsing and provider construction need separate load/import measurements; measured decide receives a prepared request (`xacml-jmh/src/main/java/org/xacml4j/jmh/pdp/PdpDecideBenchmark.java:69`, `:119`).
- **Carry forward Log4j stack-walk estimates:** Run 004 explicitly supersedes that attribution after the backend change (`P/summary.md:8`). Run 003 is comparison evidence only.

Open questions and required gates:

- Obtain an unprofiled Logback baseline with `-prof gc`, including S/all, before assigning credible absolute byte savings.
- Measure memo hit rates, bag cardinalities and fixed-arity call frequencies. H1–H3 overlap; reprofile after each accepted change.
- Establish actual megamorphic receivers with compilation/inlining evidence before broader dispatch redesign.
- Verify allocation-profiler sampling interpretation before treating its sample fractions as byte fractions.
- Every candidate needs a same-machine, same-JDK, same-backend JMH pair with at least three forks, allocation measurements, complete-result characterization and conformance gates. None ran in this lane.

Verification performed: read-only `rtk git status --short`, scoped `rtk rg` searches, focused `rtk proxy sed`/Python source reads, and in-memory Python decompression/aggregation of all six run-004 collapsed profiles plus baseline/profile JSON. Normative excerpts were read from retained evidence; no external research was performed. No benchmark code was added.

Residual risk: these are source-supported hypotheses with sampled attribution. Speedups, byte savings, thread-safety changes and semantic equivalence remain UNVERIFIED until the named gates run.
