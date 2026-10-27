<!-- Third check RCJ3 of cycle J, Codex GPT-6.1 Sol, read-only; report from the NTM pane, paths made repository-relative. -->

FINAL REPORT (RCJ3 code reviewer, GPT-6 / Codex)

**No new P1–P3 findings. RCJ2 is resolved; both RCJ1 findings remain resolved.**

Reviewed against HEAD `21858796c85745679df07a91349d9c1bb834726b`. The scoped production diff and regression test remained unchanged during review.

[FunctionSpecBuilder.java:589](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:589) now limits direct indexing to known immutable lists and exact `Arrays.asList` implementations. Other inputs take the copy path at line 599, including the transformed copy-on-write view from RCJ2.

The copy constructor calls `toArray()`, rather than universally calling `iterator()` itself. For the Guava transformed view, inherited `AbstractCollection.toArray()` consumes its iterator; that iterator delegates to the backing copy-on-write snapshot. A plain `CopyOnWriteArrayList.toArray()` clones its backing array. Both preserve the relevant snapshot behavior. [ArrayList source](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/util/ArrayList.java), [AbstractCollection source](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/util/AbstractCollection.java), [Guava view implementation](https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/Lists.java), [copy-on-write implementation](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/util/concurrent/CopyOnWriteArrayList.java)

The probes at [FunctionSpecBuilder.java:270](xacml-core/src/main/java/org/xacml4j/v30/policy/function/FunctionSpecBuilder.java:270) cover JDK 25’s empty and larger `List.of` lists (`ListN`), singleton and two-element lists (`List12`), and all `Arrays.asList` arities. The array view reads the same live array through indexed and iterator access. Unrecognized implementations fall back to copying. [JDK list implementations](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/util/ImmutableCollections.java), [Arrays implementation](https://github.com/openjdk/jdk25u/blob/master/src/java.base/share/classes/java/util/Arrays.java)

The regression at [FunctionInvocationCharacterizationTest.java:311](xacml-core/src/test/java/org/xacml4j/v30/policy/function/FunctionInvocationCharacterizationTest.java:311) exercises replacement and removal, requires two successful invocations, and independently asserts the original `[1, 2, 3]` arguments for both.

Source comparison found no further normalization change for null entries, missing or extra arguments, optional defaults, variadic placeholders, or interleaved formal parameters. The target classes retain builder-owned immutable lists, public `Collection` getters, and their existing decision branches: [Target.java:51](xacml-core/src/main/java/org/xacml4j/v30/policy/Target.java:51), [MatchAnyOf.java:47](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAnyOf.java:47), [MatchAllOf.java:55](xacml-core/src/main/java/org/xacml4j/v30/policy/MatchAllOf.java:55).

Verification: the combiner directory is unchanged against HEAD (`git diff --exit-code` returned 0). Scoped `ast-grep scan` and `git diff --check` passed. Ripwire quality-delta exited 2 with heuristic findings. Tavily timed out; DuckDuckGo successfully fetched the official JDK sources.

Retained evidence records the intended [bounds failure before the fix](openspec/changes/optimize-pdp-evaluation/evidence/tests/032-rcj2-fix/cycleJ-rcj2-red.log:58) and [592 passing focused tests afterward](openspec/changes/optimize-pdp-evaluation/evidence/tests/032-rcj2-fix/cycleJ-rcj2-green.log:136).

No files were changed. No Maven, tests, benchmarks, or Git/br writes were run. Runtime edge cases were not independently rerun in this lane.

**Verdict: APPROVE.**
