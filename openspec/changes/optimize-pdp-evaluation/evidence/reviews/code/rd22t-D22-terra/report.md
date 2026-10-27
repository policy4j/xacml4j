# rd22t-D22-terra (Codex Terra 6.1, NTM worktree lane, read-only)

**Verdict: APPROVE WITH CHANGES**

- **P3 — Superclass fields are hashed after subclass fields.** [Policy.java:213](xacml-core/src/main/java/org/xacml4j/v30/policy/Policy.java:213), [PolicySet.java:230](xacml-core/src/main/java/org/xacml4j/v30/policy/PolicySet.java:230), and [BaseCompositeDecisionRule.java:225](xacml-core/src/main/java/org/xacml4j/v30/policy/BaseCompositeDecisionRule.java:225) reverse the old evaluation order, although the arithmetic matches for stable fields. For example, a supported custom condition whose `hashCode()` updates state used by a custom combining algorithm can now make a policy’s first hash differ from its next hash; a `HashMap` lookup can then miss. Compute and save `super.hashCode()` before folding the local fields in all three methods.

- **P3 — Status lookup test asserts an implementation detail.** [RuleTest.java:619](xacml-core/src/test/java/org/xacml4j/v30/policy/RuleTest.java:619) requires the returned `Status` to be the same instance, while the public contract requires the status information. Use `assertEquals` for the status; retain the distinct-rule setup.

The remaining hash seeds and field order match the replaced formulas. The lazy maps preserve first-ID order, merge into new values, and are copied into built results. The shared OK `Status` and its internally created `StatusCode` have no public mutator found.

The bag-size messages are unchanged. Public bag functions and `FunctionInvocationException.getCause()` can expose the new `IllegalArgumentException` subclass; that class change is explicitly authorized by D20(d). Its trace remains empty with the stack-trace switch enabled; the switch applies to the `CoreException` wrapper.

All 14 patch files matched the main checkout’s file hashes. This was a static, read-only review; I did not run tests.
