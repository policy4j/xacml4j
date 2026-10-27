<!-- Review RCJ1 of cycle J (D16), Codex GPT-6.1 Sol, read-only; report from the NTM pane, paths made repository-relative. -->

FINAL REPORT (RCJ1 code reviewer, GPT-6 / Codex)

Reviewed cycle J against HEAD `21858796c85745679df07a91349d9c1bb834726b`. The scoped production diffs and characterization tests remained unchanged during review. Cycle I was excluded.

**P2 — Indexed traversal loses snapshot semantics and can change Deny to Permit.**

[BaseDecisionCombiningAlgorithm.java:95](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/BaseDecisionCombiningAlgorithm.java:95) returns every `RandomAccess` list unchanged. [FirstApplicable.java:53](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/impl/FirstApplicable.java:53) consequently reads its current contents rather than its iterator’s snapshot.

Consider a `CopyOnWriteArrayList` containing rules A and B. A replaces B with a Permit rule, then returns `NOT_APPLICABLE`; the original B returns Deny. HEAD’s iterator evaluates the original B and returns Deny. The indexed loop evaluates its replacement and returns Permit. Removing B instead can produce `IndexOutOfBoundsException`.

`CopyOnWriteArrayList` implements `RandomAccess`, but its iterator deliberately preserves the original contents across replacements and removals. [Java 25 contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/CopyOnWriteArrayList.html)

**Smallest fix:** preserve snapshot traversal for `CopyOnWriteArrayList`, using its iterator or a snapshot before indexing. Retain iterator traversal for other implementations whose iterator semantics matter.

**Test:** combine the two-rule example above and assert Deny plus the original evaluation trace; add the removal case. These counterexamples are source-derived; runtime reproduction is **UNVERIFIED**.

**P2 — Copying sequential lists reads the entire tail before short-circuiting.**

The same [helper at line 95](xacml-core/src/main/java/org/xacml4j/v30/policy/combine/BaseDecisionCombiningAlgorithm.java:95) materializes every non-`RandomAccess` input before evaluating its first element. This differs from [D16:313](openspec/changes/optimize-pdp-evaluation/design.md:313), which specifies iterator traversal for those lists.

A concrete input is `Lists.transform` over a `LinkedList`: transformation is lazy, and the resulting view uses sequential traversal. If its first element supplies a Deny rule and transforming the second throws, HEAD’s first-applicable loop returns Deny without requesting the second element. The new copy throws before evaluating the first rule. Even an ordinary large `LinkedList` now incurs O(n) copying and storage before an immediate terminal decision. [Guava implementation](https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/Lists.java)

**Smallest fix:** restore D16’s iterator branch for non-random-access inputs.

**Test:** supply a lazy sequential view whose tail transformation fails; assert the first terminal decision succeeds and only the first element is requested. Runtime reproduction is **UNVERIFIED**.

The characterization test constructs all elements eagerly and compares three stable lists, so neither scenario is covered: [CombiningListTypesCharacterizationTest.java:81](xacml-core/src/test/java/org/xacml4j/v30/policy/combine/impl/CombiningListTypesCharacterizationTest.java:81).

Source comparison found no additional regression in the immutable target-list rewrites or stable-input normalization. The normalization phases retain the previous handling of missing, extra, null, and interleaved arguments: [FunctionSpecBuilder.java:579](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:579). Generic operand evaluation still uses an iterator at line 640; runtime validation also retains list iterators.

Verification performed: scoped diff review, caller inspection, official XACML §7.7 and §7.11–7.13 review, `ast-grep scan`, and `git diff --check`. Both static checks passed. Ripwire quality-delta exited 2 with heuristic findings; its call graph is **PARTIAL**. Serena returned “Session terminated” and was **UNAVAILABLE**.

Retained evidence records [33 passing tests before D16](openspec/changes/optimize-pdp-evaluation/evidence/tests/029-cycleJ-characterization/output.txt:61) and [587 passing focused tests afterward](openspec/changes/optimize-pdp-evaluation/evidence/tests/030-cycleJ-green/focused.txt:134). These are supplied execution records, not fresh verification of the counterexamples.

No files were changed. No Maven, tests, benchmarks, or Git/br writes were run.

**Verdict: REJECT — preserve caller-list traversal semantics before accepting cycle J.**
