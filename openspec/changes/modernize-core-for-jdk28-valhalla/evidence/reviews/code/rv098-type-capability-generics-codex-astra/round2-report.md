FINAL REVIEW (R4 round 2, GPT-6-Astra)

**Verdict: REQUEST_CHANGES — D13 needs two factual corrections.**

1. **R4-1 closed:** comparison with round 1 confirms only the [lazy lambda](<checkouts>/xacml4j-worktrees/policy4j/xacml-core/src/main/java/org/xacml4j/v30/types/TypeToString.java:62) changed in the Java diff; remaining references are static `TypeToString::forType` and unbound `Map.Entry::getKey/getValue`, with no equivalent early receiver evaluation.
2. **Documentation partially corrected:** [task 5.6](<checkouts>/xacml4j-worktrees/policy4j/openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md:58) names the conversion APIs; D13’s signatures and unchanged-erasure claim match the source, but its compatibility and self-bound claims require the corrections below.
3. **R4-2 deferred as instructed:** RFC822Name’s diff matches round 1; the [landing procedure](<checkouts>/xacml4j-worktrees/policy4j/openspec/changes/modernize-core-for-jdk28-valhalla/evidence/builds/087-single-branch-landing-gate/landing-procedure.md:89) explicitly preserves the maintainer’s uncommitted edit.
4. **Contract test deferred:** accepted as remaining work per the conductor’s packet; [task 5.6 remains unchecked](<checkouts>/xacml4j-worktrees/policy4j/openspec/changes/modernize-core-for-jdk28-valhalla/tasks.md:58). Bead tracking was not independently inspected.

New findings:

- **MEDIUM — R4-3: D13 overstates source compatibility.** [design.md:257](<checkouts>/xacml4j-worktrees/policy4j/openspec/changes/modernize-core-for-jdk28-valhalla/design.md:257) says callers using raw types still compile. Counterexample: `Function<Object, Value> f = XacmlTypes.STRING;` was compatible with the old declaration but conflicts with the new [Function<Object, Value<?>> superinterface](<checkouts>/xacml4j-worktrees/policy4j/xacml-core/src/main/java/org/xacml4j/v30/types/ValueType.java:39), because generic arguments are invariant. **Fix:** state that erased descriptors remain unchanged while some source callers need migration; show `Function<Object, Value<?>>` as the replacement. **Deferred test:** compile fixtures demonstrating rejection of the old assignment and acceptance of its replacement.

- **LOW — R4-4: The self-bound is satisfiable.** [design.md:248](<checkouts>/xacml4j-worktrees/policy4j/openspec/changes/modernize-core-for-jdk28-valhalla/design.md:248) incorrectly says no value satisfies `<T extends Value<T>>`. [RFC822Name](<checkouts>/xacml4j-worktrees/policy4j/xacml-core/src/main/java/org/xacml4j/v30/types/RFC822Name.java:36) extends `SelfValue<RFC822Name>`, which inherits [Value<T>](<checkouts>/xacml4j-worktrees/policy4j/xacml-core/src/main/java/org/xacml4j/v30/types/Value.java:146). It therefore satisfies that bound. **Fix:** explain that the bound excludes payload-wrapping values such as `BooleanVal`, while accepting self-valued types; `Value<?>` accommodates both. **Deferred test:** a self-bound fixture accepting RFC822Name and rejecting BooleanVal.

Verification: read-only diffs, searches, and source inspection; reviewed files stayed stable during this round. The supplied rerun reports 8,009 tests, zero failures, 18 skips; conformance 937 with 12 skips. No builds, tests, writes, or network calls ran here. Compiler reproductions and binary-client linkage remain **UNVERIFIED**.

END FINAL REVIEW R4-2
