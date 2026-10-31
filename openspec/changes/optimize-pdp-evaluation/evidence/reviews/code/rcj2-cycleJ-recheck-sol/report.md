<!-- Re-check RCJ2 of cycle J, Codex GPT-6.1 Sol, read-only; report from the NTM pane, paths made repository-relative. -->

FINAL REPORT (RCJ2 code reviewer, GPT-6 / Codex)

Reviewed against HEAD `21858796c85745679df07a91349d9c1bb834726b`. The scoped diffs and regression tests remained unchanged during review.

**P2 — Normalization still loses caller-list snapshot semantics.**

[FunctionSpecBuilder.java:583](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:583) indexes every `RandomAccess` input directly. This includes `CopyOnWriteArrayList` and random-access views backed by it. HEAD used the input’s `listIterator()`, preserving its snapshot; the new `size()` and separate `get()` calls can observe different versions.

A deterministic counterexample uses `Lists.transform` over a `CopyOnWriteArrayList` containing arguments `[1, 2, 3]`. Transforming argument 1 replaces argument 3 with 4. HEAD’s transformed iterator retains the backing snapshot and supplies `[1, 2, 3]`; the indexed implementation supplies `[1, 2, 4]`. Removing the last argument instead can cause a bounds exception.

Guava’s transformed random-access view delegates iteration to the backing iterator but delegates indexed reads to the backing `get()`. [Guava implementation](https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/Lists.java) Copy-on-write iterators preserve their original contents across replacements and removals. [Java 25 contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/CopyOnWriteArrayList.html)

**Smallest fix:** snapshot arbitrary caller lists before indexing; retain the no-copy path only for known immutable inputs.

**Test:** invoke a three-parameter function through `invokeWithList` using the transformed list above. Capture the invocation arguments and require `[1, 2, 3]`; add the removal variant. This counterexample is source-derived; runtime reproduction is **UNVERIFIED**.

**RCJ1 dispositions**

- **Resolved — combiner snapshot semantics.** `git diff --exit-code HEAD -- xacml-core/src/main/java/org/xacml4j/v30/policy/combine` returned 0 with no diff. [FirstApplicable.java:52](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/FirstApplicable.java:52) again uses the original iterator. The regression at [CombiningListTypesCharacterizationTest.java:177](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/CombiningListTypesCharacterizationTest.java:177) requires Deny and the original rule trace.

- **Resolved — eager combiner materialization.** Restoring HEAD removes the copy before combination. The regression at [line 206](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/CombiningListTypesCharacterizationTest.java:206) makes tail access fail and requires the first rule’s Deny result and trace.

The non-random-access normalization copy does not introduce the combiner’s early-exit problem: HEAD normalization also consumed every remaining argument before invoking the function. Target traversal has neither concern: its lists are built and owned as immutable lists at [Target.java:51](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java:51), [MatchAnyOf.java:47](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java:47), and [MatchAllOf.java:55](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java:55).

Verification: source and HEAD comparisons, scoped `ast-grep scan`, and `git diff --check` passed. Ripwire quality-delta exited 2 with heuristic findings. Retained evidence records both intended failures before restoration and [589 passing focused tests afterward](openspec/changes/optimize-pdp-evaluation/evidence/tests/031-rcj1-fix/cycleJ-rcj1-green.log:134).

No files were changed. No Maven, tests, benchmarks, or Git/br writes were run. The new normalization counterexample remains untested.

**Verdict: REJECT — preserve snapshot semantics for caller-supplied normalization inputs.**
